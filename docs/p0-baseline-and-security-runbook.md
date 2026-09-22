# P0 基线与安全发布运行手册

## 已实现的工程基线

- `sql/generated/` 中的受管 SQL 在启动时按名称顺序执行，具备 MySQL 命名锁、执行历史、校验和和失败审计；历史脚本不可修改。
- 题目采集任务使用客户端请求标识去重，OCR 页面和超时恢复均写入 Micrometer 指标。
- `sys_question_capture_event` 保存任务创建、OCR 结果、超时恢复及关键人工确认动作，便于按任务、用户、OCR 模型和失败原因排查。
- 文件上传同时校验类型白名单、大小、文件名与可识别格式的文件头；上传策略签名绑定当前登录用户，不能跨账号重放。Nacos 中开启 `file.security.virus-scan-enabled=true` 后，会通过 ClamAV INSTREAM 协议扫描上传和远程导入文件。
- 远程 URL 导入文件也会经过文件头与病毒扫描；来源文件名不参与本地路径构造，失败时会清理已写入的临时对象。
- 文件下载地址需要文件拥有者或系统管理员授权；异步 OCR、PDF 分页、净化图任务显式携带任务归属用户并再次校验文件归属。签名下载链接为短期浏览器令牌，下载端会拒绝任何越出文件根目录的路径。
- 用户禁用、删除或管理员更新资料后会删除 Redis 中的访问、刷新令牌；角色/菜单/资源变更会在事务提交后刷新在线用户的权限缓存。
- 已过期访问令牌会先完成签名校验，再使用异常中携带的已验签声明定位同一账号的 refresh token；不会在续签分支之前解析已过期令牌。
- PDF 分页失败产生的来源文件占位页会重新提交 PDF 分页，而不会将 PDF 文件错误发送至 OCR。

## 发布前检查

1. 确认 Nacos 中的 `security.ignore.urls` 未错误放行 `/api/user`、`/api/role`、`/api/menu`、`/api/resource`、`/api/dict` 和 `/api/file` 管理接口。
2. 在预发数据库备份后启动一次系统服务，确认 `sys_generated_sql_migration` 和 `sys_generated_sql_migration_audit` 中新增脚本状态均为 `SUCCESS`。
3. 为新增的 `@PreAuthorize` 资源确认父资源、菜单资源关联和目标角色菜单关联均已存在；系统管理员会自动拥有全部资源。
4. 将旧数据中归属为空的文件按业务记录补齐 `file.create_id`。在未补齐前，普通用户无法通过新下载地址读取这类文件，避免历史公共文件造成越权泄露。
5. 检查对象存储/本地文件根目录不允许匿名目录浏览，并确保文件目录不被 Nginx 直接暴露；CDN 仅可回源受控下载端点，启用签名/Token 鉴权与 Referer 防盗链。原图生命周期由对象存储规则管理：建议原图保留 180 天、净化图和已确认题图按学习档案保留策略执行；任何自动删除规则先在非生产桶做恢复演练。
6. 生产环境配置 `file.security.virus-scan-enabled=true`、`virus-scan-host`、`virus-scan-port` 与 `virus-scan-timeout`；保持 `virus-scan-fail-open=false`，扫描服务故障时拒绝文件。

## 必测场景

| 类别 | 场景 | 预期 |
|---|---|---|
| 鉴权 | 无 Token 访问系统设置写接口 | 返回未登录或拒绝访问 |
| 权限 | 有 Token、无对应资源访问用户/角色/菜单/资源/字典管理接口 | 返回无访问权限 |
| 会话 | 禁用、删除、管理员更新账户后使用旧 Token | Redis 在线会话失效，接口不能继续访问 |
| 会话 | 已过期 access token 携带有效 refresh token | 响应返回新的 access token；客户端重试后按新令牌认证 |
| 权限刷新 | 在线用户被授予或撤销角色/菜单/资源 | 下一请求使用更新后的权限集合 |
| 文件 | 以 `.pdf` 名称上传 PNG 内容 | 被文件内容校验拒绝 |
| 文件 | 用户 A 请求用户 B 的文件下载地址 | 返回无访问权限 |
| 文件 | 用户 A 使用用户 B 签发的上传策略上传 | 返回无效上传凭证 |
| 文件 | 远程 URL 导入伪装成图片的非图片内容 | 被内容校验/病毒扫描拒绝，落盘临时文件被清理 |
| 文件 | 修改下载签名中保存的相对路径为 `../` 路径 | 下载端拒绝，不能读取文件根目录外内容 |
| 采集 | 同一 `clientRequestId` 重复提交 | 返回同一任务 ID，不重复创建页面 |
| 采集 | OCR 失败、超时重试、题块合并/拆分/确认 | 页面保留原图，任务可恢复，审计事件完整 |
| 采集 | 无 HTTP 会话的 OCR/净化/PDF 异步任务 | 仅能读取该采集任务所属学生的私有文件；OCR 或净化失败不阻断错题浏览和复习 |

## OCR 样本评测

采集 20–50 份已获授权的真实试卷，按学科、清晰度、手写量分层。每次模型或参数变更均记录：样本版本、题块召回率、字段确认后准确率、人工修正率、单页 P95、失败率和净化成功率。不得将供应商宣传指标直接作为上线验收结果。

## 监控与告警建议

- `question.capture.ocr.pages`：按结果、供应商、模型、学科统计 OCR 页面成功/失败。
- `question.capture.ocr.duration`：按相同维度监控 OCR 页面耗时 P95。
- `question.capture.clean.pages`、`question.capture.clean.duration`：监控净化图生成成功率与耗时；失败仅降级为原图确认。
- `question.capture.recovery.tasks`：监控自动恢复和最终失败数。
- `question.capture.executor.*`：监控线程池队列、活跃线程及拒绝执行；`question.capture.executor.rejected` 表示队列已满、已触发调用方执行降级。
- 基于 `sys_question_capture_event` 建立任务失败原因、OCR 模型、人工修正动作和长期失败任务看板。

### P0-08 告警阈值与处置

生产监控按 5 分钟窗口聚合，以下阈值为上线门槛；连续两个窗口触发才升级告警，避免单次供应商波动造成噪声。

| 指标 | 告警阈值 | 首要处置 |
|---|---:|---|
| OCR 成功率 | `< 95%` | 查看 `OCR_PAGE/FAILED` 事件，区分供应商、模型、学科和错误码；必要时暂停新 OCR 任务并保留人工录入入口。 |
| OCR 单页 P95 | `> 30s` | 检查供应商轮询、网络与模型变更；任务仍异步执行，不阻塞错题查看、确认和复习。 |
| 净化图失败率 | `> 5%` | 检查图片解码及存储；页面继续使用原图。 |
| 任务恢复最终失败数 | `> 0` | 根据任务/文件 ID 重试，核对 `retry_count` 与失败原因。 |
| 执行器队列深度 | `> 40 / 50` 持续 10 分钟 | 扩容消费者或限流上传；检查 OCR P95。 |
| 执行器拒绝数 | `> 0` | 立即检查积压和实例资源；CallerRuns 仅为不丢任务的临时降级。 |

Actuator 已暴露 `metrics` 端点。监控系统应抓取 `question.capture.*` 与执行器指标，并以 `sys_question_capture_event` 关联任务、用户、文件和 OCR 模型进行根因定位；若接入 Prometheus，请在 Nacos 中额外启用对应 registry 与受限抓取网络，不能直接暴露管理端口到公网。

## 2026-09-11 本次验证记录与待执行环境验收

### 已完成的本地代码验证

- `mvn -q -pl study-module-system -am test`：通过。覆盖 P0 采集/权限相关既有测试，并新增了 P1 文档导出与 P2 家长周报送达状态测试。
- `cd frontend && npm run build`：通过。仅有 Vue CLI 的 Sass 旧接口与包体积警告。
- `git diff --check`：通过。

### 2026-09-14 P0 回归补充

- `mvn -pl study-module-system -am test`：31 个测试全部通过；新增过期 access token 续签、会话撤销、PDF 分页失败重试及跨学生采集任务恢复拒绝用例。
- `cd frontend && npm run build`：通过；仍有 Sass legacy API 和包体积告警，未阻断 P0 发布。
- 发布基线、迁移与回滚登记见 [20260914 P0 发布基线](releases/20260914-p0-baseline.md)。

### 2026-09-14 P0-07 / P0-08 补充

- 上传策略已绑定签发用户；所有本地上传、远程 URL 导入和文件入库均执行扩展名、魔数/内容与 ClamAV 扫描校验。部署时仍必须启用 ClamAV 的 fail-closed 配置。
- 异步 PDF 分页、OCR、图片净化均使用“任务归属用户 + 文件服务归属校验”获取短期私有下载链接，不依赖线程中的 HTTP 登录态。
- 已新增按 OCR 供应商、模型、学科聚合的成功率/P95 指标，以及净化图成功率、线程池拒绝计数与结构化成功/失败审计事件；阈值及处置见本手册的 P0-08 小节。
- 采集中心统一仅接受 JPG、JPEG、PNG、PDF；DOC/DOCX 在两个导入入口均被前端禁选并说明原因。上传中的文件及未完成采集任务在浏览器本地保留 24 小时，可恢复后继续创建或确认。
- 新增 `sys_wrong_question_timeline`：手工录入、OCR 题块确认、订正、答案曝光、复习反馈及自动掌握状态变更都写入带来源的学习证据，详情页按时间展示。

### 本机运行态结论

2026-09-11 的实际运行态检查结果如下：

- Nacos `8848` 就绪接口返回 `200 OK`；服务端当前关闭认证。客户端携带旧的 Nacos 用户名/密码会触发无效登录，因此已从 system-service 的 Dubbo 注册配置及各服务 bootstrap 中移除这些字段。
- `system-service` 与 `gateway-service` 已均注册到目标命名空间 `ad55cf5c-9725-4e8a-8e51-f684f1de5211`，各有 1 个健康实例。
- 网关实际监听在 `8090`；其 system-service 路由前缀为 `/api/sys/**`，`/api/sys/verify` 和 `/api/sys/user/currentUserInfo` 均返回 `200`。裸路径 `/api/verify` 不匹配网关路由，返回 `404` 属预期行为。
- 本机未发现 MySQL `3306`、Redis `6379` 或 ClamAV `3310` 监听；MySQL/Redis 由 Nacos 配置指向远端实例。`file-service.yaml` 仍不存在，病毒扫描配置和 ClamAV 仍需完成部署与验收。

Nacos 注册与网关转发阻断已解除，但本次**仍不能**把 P0 标记为“真实环境验收通过”：文件安全、OCR 样本、权限/会话与完整业务链路仍需按下列步骤验收。生产环境应启用 Nacos 认证，并通过受管密钥或环境变量配置凭据，不得把密码提交到仓库。

### 部署前必须配置

在对应 Nacos 数据集或受管密钥中配置，敏感值不得提交到仓库：

```yaml
file:
  security:
    virus-scan-enabled: true
    virus-scan-host: clamav.internal
    virus-scan-port: 3310
    virus-scan-timeout: 10000
    virus-scan-fail-open: false

wrong-question:
  ocr:
    paddle:
      enabled: true
      token: ${OCR_PROVIDER_TOKEN}

review:
  practice-export:
    # 可嵌入中文的 TTF/OTF 绝对路径；缺失时中文 PDF 会明确失败，不生成乱码文件。
    pdf-font-path: /opt/study/fonts/NotoSansCJKsc-Regular.otf

mail:
  host: smtp.example.com
  port: 587
  user-name: ${SMTP_USERNAME}
  pass-word: ${SMTP_PASSWORD}
  from-address: noreply@example.com

guardian:
  weekly-report:
    cron: "0 0 8 ? * MON"
    retry-cron: "0 0 9-20 ? * MON"
```

### 迁移与端到端验收步骤

1. 在数据库备份完成后启动系统，确认 `sys_generated_sql_migration` 与审计表中以下新增脚本均为 `SUCCESS`：
   - `20260909_student_guardian_rel.sql`
   - `20260911_practice_paper_export_task.sql`
   - `20260914_wrong_question_timeline.sql`
   - `20260911_guardian_delivery_login_upgrade.sql`
2. 使用非管理员学生账号完成“登录 → 上传 → OCR 处理/失败重试 → 题块确认 → 订正 → 今日复习 → 报告”。同一 `clientRequestId` 重复提交不得创建第二个采集任务。
3. 使用学生 A、学生 B 和家长账号完成受控绑定；验证家长仅能查看已确认孩子的汇总与最近登录，解绑后立即访问失败；家长不可提交订正、答案、复习反馈或掌握状态。
4. 用实际 SMTP 测试一条仅邮件订阅和一条站内+邮件订阅。故意提供不可达 SMTP，确认 `email_last_sent_week` 不更新、周一重试任务继续尝试；恢复 SMTP 后才更新该字段。站内消息成功不能掩盖邮件失败。
5. 以包含中文题干的练习卷分别导出 PDF、DOCX，下载并人工打开；确认 PDF 字体正常、DOCX 可被 Office/WPS 打开、历史下载只对创建用户可见。
6. 用 20–50 份获授权真实试卷完成文档中“OCR 样本评测”要求，并记录题块召回率、字段确认准确率、人工修正率、P95 与失败率；达到规划阈值后再签署 P0 验收。
