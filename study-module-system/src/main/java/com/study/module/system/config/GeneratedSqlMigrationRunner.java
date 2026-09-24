package com.study.module.system.config;

import com.study.module.system.resource.service.ResourceService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * 启动时执行受管目录中的增量 SQL 脚本。
 */
@Component
@Slf4j
public class GeneratedSqlMigrationRunner implements ApplicationRunner {

    private static final String GENERATED_SQL_PATTERN = "classpath*:sql/generated/*.sql";

    private static final String MIGRATION_LOCK_NAME = "study:generated-sql-migration";

    private static final int MIGRATION_LOCK_TIMEOUT_SECONDS = 60;

    /**
     * 已核验的历史脚本校验和对账：2026-07-25 的脚本原文已不在当前代码仓库，
     * 仅允许当时数据库记录的校验和切换到当前受管脚本的校验和。
     */
    private static final String LEGACY_WRONG_QUESTION_RESOURCE_SYNC_SCRIPT =
            "20260725_wrong_question_resource_sync.sql";

    private static final String LEGACY_WRONG_QUESTION_RESOURCE_SYNC_CHECKSUM =
            "9a7a2f7044c83052733d1e6021edc48a";

    private static final String CURRENT_WRONG_QUESTION_RESOURCE_SYNC_CHECKSUM =
            "72a57d2110d786236bcc81b5c0b8cede";

    /** 此脚本使用了 MariaDB 的 IF NOT EXISTS DDL 语法；MySQL 8 需要等价的条件执行。 */
    private static final String REVIEW_INDEPENDENT_ANSWER_SCRIPT =
            "20260914_review_record_independent_answer.sql";

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    private final ResourceService resourceService;

    /**
     * 初始化受管 SQL 迁移执行器。
     */
    public GeneratedSqlMigrationRunner(JdbcTemplate jdbcTemplate, DataSource dataSource,
                                       ResourceService resourceService) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.resourceService = resourceService;
    }

    /**
     * 执行尚未执行过的受管 SQL 脚本。
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Connection lockConnection = DataSourceUtils.getConnection(dataSource);
        try {
            if (!acquireMigrationLock(lockConnection)) {
                throw new IllegalStateException("等待受管 SQL 迁移锁超时");
            }
            createMigrationHistoryTable();
            createMigrationAuditTable();
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(GENERATED_SQL_PATTERN);
            Arrays.sort(resources, (left, right) -> left.getFilename().compareTo(right.getFilename()));
            for (Resource resource : resources) {
                executeIfNecessary(resource);
            }
            syncPreAuthorizeResources();
        } finally {
            releaseMigrationLock(lockConnection);
            DataSourceUtils.releaseConnection(lockConnection, dataSource);
        }
    }

    /**
     * 创建记录已执行脚本的历史表。
     */
    private void createMigrationHistoryTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_generated_sql_migration` ("
                + "`script_name` varchar(255) NOT NULL COMMENT '脚本文件名',"
                + "`script_checksum` char(32) NOT NULL COMMENT '脚本内容校验值',"
                + "`execute_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',"
                + "PRIMARY KEY (`script_name`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='受管 SQL 脚本执行历史'");
    }

    /**
     * 创建记录每次执行结果的迁移审计表。
     */
    private void createMigrationAuditTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_generated_sql_migration_audit` ("
                + "`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',"
                + "`script_name` varchar(255) NOT NULL COMMENT '脚本文件名',"
                + "`script_checksum` char(32) NOT NULL COMMENT '脚本内容校验值',"
                + "`status` varchar(16) NOT NULL COMMENT '执行状态',"
                + "`error_message` varchar(1000) DEFAULT NULL COMMENT '失败原因',"
                + "`start_time` datetime NOT NULL COMMENT '开始时间',"
                + "`finish_time` datetime DEFAULT NULL COMMENT '结束时间',"
                + "PRIMARY KEY (`id`),"
                + "KEY `idx_generated_sql_audit_script` (`script_name`, `start_time`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='受管 SQL 脚本执行审计'");
    }

    /**
     * 在脚本未执行时执行并记录执行历史。
     */
    private void executeIfNecessary(Resource resource) throws IOException {
        String scriptName = resource.getFilename();
        String checksum = checksum(resource);
        List<String> checksums = jdbcTemplate.query(
                "SELECT script_checksum FROM sys_generated_sql_migration WHERE script_name = ?",
                (resultSet, rowNum) -> resultSet.getString("script_checksum"), scriptName);
        if (!checksums.isEmpty()) {
            if (!checksum.equals(checksums.get(0))) {
                if (reconcileKnownHistoricalChecksum(scriptName, checksums.get(0), checksum)) {
                    return;
                }
                String errorMessage = "已执行的受管 SQL 脚本内容发生变化：" + scriptName;
                Long auditId = createMigrationAudit(scriptName, checksum);
                completeMigrationAudit(auditId, "FAILED", errorMessage);
                log.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            return;
        }
        Long auditId = createMigrationAudit(scriptName, checksum);
        try {
            executeScript(resource);
            jdbcTemplate.update("INSERT INTO sys_generated_sql_migration (script_name, script_checksum, execute_time) "
                            + "VALUES (?, ?, NOW())",
                    scriptName, checksum);
            completeMigrationAudit(auditId, "SUCCESS", null);
            log.info("已执行受管 SQL 脚本：{}", scriptName);
        } catch (RuntimeException e) {
            completeMigrationAudit(auditId, "FAILED", abbreviateErrorMessage(e));
            log.error("执行受管 SQL 脚本失败：{}", scriptName, e);
            throw e;
        }
    }

    private void executeScript(Resource resource) {
        if (REVIEW_INDEPENDENT_ANSWER_SCRIPT.equals(resource.getFilename())) {
            ensureReviewIndependentAnswerSchema();
            return;
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.addScript(resource);
        populator.execute(dataSource);
    }

    /** 保留原脚本及其校验和，按 MySQL 8 支持的 DDL 完成同一迁移，兼容失败后重试。 */
    private void ensureReviewIndependentAnswerSchema() {
        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'sys_review_record' AND COLUMN_NAME = 'is_independent'",
                Integer.class);
        if (columnCount == null || columnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE `sys_review_record` ADD COLUMN `is_independent` "
                    + "tinyint NOT NULL DEFAULT 0 COMMENT '是否答案曝光前主动作答：0否，1是' AFTER `is_correct`");
        }
        Integer indexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'sys_review_record' "
                        + "AND INDEX_NAME = 'idx_review_record_user_independent_time'",
                Integer.class);
        if (indexCount == null || indexCount == 0) {
            jdbcTemplate.execute("CREATE INDEX `idx_review_record_user_independent_time` "
                    + "ON `sys_review_record` (`user_id`, `is_independent`, `review_time`)");
        }
    }

    /**
     * 对唯一已人工核验的历史校验和差异进行一次性对账。
     *
     * <p>此处不接受任意脚本或任意校验和的变更，避免将历史脚本篡改静默放行。
     * 对账结果独立记入审计表，便于后续追踪。</p>
     */
    private boolean reconcileKnownHistoricalChecksum(String scriptName, String storedChecksum,
                                                      String currentChecksum) {
        if (!LEGACY_WRONG_QUESTION_RESOURCE_SYNC_SCRIPT.equals(scriptName)
                || !LEGACY_WRONG_QUESTION_RESOURCE_SYNC_CHECKSUM.equals(storedChecksum)
                || !CURRENT_WRONG_QUESTION_RESOURCE_SYNC_CHECKSUM.equals(currentChecksum)) {
            return false;
        }

        Long auditId = createMigrationAudit(scriptName, currentChecksum);
        int updatedRows = jdbcTemplate.update("UPDATE sys_generated_sql_migration "
                        + "SET script_checksum = ? WHERE script_name = ? AND script_checksum = ?",
                currentChecksum, scriptName, storedChecksum);
        if (updatedRows != 1) {
            String errorMessage = "历史受管 SQL 校验和对账失败，记录已发生变化：" + scriptName;
            completeMigrationAudit(auditId, "FAILED", errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        String auditMessage = "一次性历史校验和对账：已核验 " + storedChecksum
                + " -> " + currentChecksum;
        completeMigrationAudit(auditId, "RECONCILED", auditMessage);
        log.warn("已完成受管 SQL 历史校验和对账：{}，{} -> {}", scriptName, storedChecksum, currentChecksum);
        return true;
    }

    /**
     * 写入待执行的迁移审计记录。
     */
    private Long createMigrationAudit(String scriptName, String checksum) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO sys_generated_sql_migration_audit "
                            + "(script_name, script_checksum, status, start_time) VALUES (?, ?, 'RUNNING', NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, scriptName);
            statement.setString(2, checksum);
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("创建受管 SQL 迁移审计记录失败：" + scriptName);
        }
        return keyHolder.getKey().longValue();
    }

    /**
     * 更新迁移审计记录的最终状态。
     */
    private void completeMigrationAudit(Long auditId, String status, String errorMessage) {
        jdbcTemplate.update("UPDATE sys_generated_sql_migration_audit "
                        + "SET status = ?, error_message = ?, finish_time = NOW() WHERE id = ?",
                status, errorMessage, auditId);
    }

    /**
     * 获取 MySQL 命名锁，避免多实例同时执行同一批迁移。
     */
    private boolean acquireMigrationLock(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            statement.setString(1, MIGRATION_LOCK_NAME);
            statement.setInt(2, MIGRATION_LOCK_TIMEOUT_SECONDS);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 1;
            }
        }
    }

    /**
     * 释放当前连接持有的 MySQL 命名锁。
     */
    private void releaseMigrationLock(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setString(1, MIGRATION_LOCK_NAME);
            statement.executeQuery();
        } catch (SQLException e) {
            log.warn("释放受管 SQL 迁移锁失败", e);
        }
    }

    /**
     * 压缩异常信息，确保能够写入审计字段。
     */
    private String abbreviateErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            return exception.getClass().getSimpleName();
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    /**
     * 计算脚本内容校验值，防止已执行脚本被静默修改。
     */
    private String checksum(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        }
    }

    /**
     * 扫描全部 Controller 权限注解，并补齐缺失的资源记录。
     */
    private void syncPreAuthorizeResources() {
        int createdCount = resourceService.createPre().size();
        log.info("权限资源同步完成，新增资源数：{}", createdCount);
    }
}
