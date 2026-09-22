## 智错本学习系统

面向 K12 学生的 PC Web 错题整理、订正、间隔复习和专项练习系统。

### 当前能力

- 错题录入、图片/PDF 采集、OCR 切题确认和失败恢复
- 订正记录、知识点与错因、间隔复习、专项练习和个人学情报告
- 题库审核、重复题处理、相似题练习、题目举报和实验管理
- JWT 登录、RBAC、文件签名下载、操作日志及采集事件审计

本期范围为学生 PC Web 和系统管理后台；不包含学生手机端、教师端、班级或学校能力。

### 模块

```text
study
├── study-api                    # 跨服务 DTO、Provider 与常量
├── study-common                 # 核心、MyBatis、Redis、安全、Dubbo、Excel 基础组件
├── study-gateway                # 网关服务（8080）
├── study-module-system          # 学生学习、题库、文件与系统设置服务（8081）
└── study-module-schedule        # 复习提醒及后台定时任务（8083）
```

系统运行依赖 MySQL、Redis 与 Nacos；运行时配置由 Nacos 提供。

### 常用命令

```bash
# 验证系统服务及其依赖模块
mvn -pl study-module-system -am test

# 跳过测试进行编译验证
mvn -pl study-module-system -am test -DskipTests

# 启动系统服务
mvn -pl study-module-system spring-boot:run
```

### 发布与迁移

`study-module-system/src/main/resources/sql/generated/` 中的 SQL 会在应用启动时由受管迁移执行器按文件名顺序执行，执行记录与校验和写入数据库；已经执行过的脚本不得修改。如需回滚，请新增反向迁移脚本，不要修改历史文件。

P0 上线验证、权限回归和采集样本评测清单见 [P0 发布运行手册](docs/p0-baseline-and-security-runbook.md)。
