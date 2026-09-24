package com.study.module.system.config;

import com.study.module.system.resource.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * 受管 SQL 历史校验和对账的回归测试。
 */
@ExtendWith(MockitoExtension.class)
class GeneratedSqlMigrationRunnerTest {

    private static final String SCRIPT_NAME = "20260725_wrong_question_resource_sync.sql";

    private static final String LEGACY_CHECKSUM = "9a7a2f7044c83052733d1e6021edc48a";

    private static final String CURRENT_CHECKSUM = "72a57d2110d786236bcc81b5c0b8cede";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DataSource dataSource;

    @Mock
    private ResourceService resourceService;

    @Test
    void shouldReconcileOnlyKnownHistoricalChecksum() {
        GeneratedSqlMigrationRunner runner = new GeneratedSqlMigrationRunner(
                jdbcTemplate, dataSource, resourceService);
        mockAuditInsert();
        when(jdbcTemplate.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq(SCRIPT_NAME)))
                .thenReturn(Collections.singletonList(LEGACY_CHECKSUM));
        when(jdbcTemplate.update(anyString(), eq(CURRENT_CHECKSUM), eq(SCRIPT_NAME), eq(LEGACY_CHECKSUM)))
                .thenReturn(1);

        ReflectionTestUtils.invokeMethod(runner, "executeIfNecessary", currentScriptResource());

        verify(jdbcTemplate).update(
                "UPDATE sys_generated_sql_migration SET script_checksum = ? "
                        + "WHERE script_name = ? AND script_checksum = ?",
                CURRENT_CHECKSUM, SCRIPT_NAME, LEGACY_CHECKSUM);
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(
                eq("UPDATE sys_generated_sql_migration_audit "
                                + "SET status = ?, error_message = ?, finish_time = NOW() WHERE id = ?"),
                statusCaptor.capture(), anyString(), eq(1L));
        org.junit.jupiter.api.Assertions.assertEquals("RECONCILED", statusCaptor.getValue());
    }

    @Test
    void shouldRejectUnknownHistoricalChecksum() {
        GeneratedSqlMigrationRunner runner = new GeneratedSqlMigrationRunner(
                jdbcTemplate, dataSource, resourceService);
        mockAuditInsert();
        when(jdbcTemplate.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq(SCRIPT_NAME)))
                .thenReturn(Collections.singletonList("unexpected-checksum"));

        assertThrows(IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(runner, "executeIfNecessary", currentScriptResource()));
    }

    @Test
    void mysqlCompatibleMigrationCreatesOnlyMissingObjects() {
        GeneratedSqlMigrationRunner runner = new GeneratedSqlMigrationRunner(
                jdbcTemplate, dataSource, resourceService);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0, 1);

        ReflectionTestUtils.invokeMethod(runner, "ensureReviewIndependentAnswerSchema");

        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.startsWith("ALTER TABLE `sys_review_record`"));
        verify(jdbcTemplate, never()).execute(org.mockito.ArgumentMatchers.startsWith("CREATE INDEX"));
    }

    private void mockAuditInsert() {
        doAnswer(invocation -> {
            KeyHolder keyHolder = invocation.getArgument(1);
            keyHolder.getKeyList().add(Collections.singletonMap("id", 1L));
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));
    }

    private ClassPathResource currentScriptResource() {
        return new ClassPathResource("sql/generated/" + SCRIPT_NAME);
    }
}
