# 投稿/评奖平台 · 后端启动说明

## 环境要求

| 组件 | 要求 | 检查命令 |
|---|---|---|
| JDK | 17+（本项目按 25 开发） | `java -version` |
| MySQL | 8.x，root 密码已设置 | — |
| VS Code 插件 | Extension Pack for Java、Spring Boot Extension Pack | 状态栏出现 "Java: Ready" |

## 首次启动步骤

1. **改数据库密码**：打开 `src/main/resources/application.yml`，
   把 `spring.datasource.password` 的 `root` 改成你本机 MySQL 的 root 密码。
2. **建库**：连接串已带 `createDatabaseIfNotExist=true`，正常情况下**不用手动建库**。
   如果想手动执行建表脚本，在项目 `sql/` 目录下有 `schema.sql`
   （MySQL 里执行：`source schema.sql` 或用 VS Code 的 Database Client 插件运行）。
3. **启动**（二选一）：
   - VS Code：侧边栏 Spring Boot Dashboard → 找到 `submission-award-platform` → 点 Run
   - 命令行：`./mvnw spring-boot:run`（Windows cmd 用 `mvnw.cmd spring-boot:run`）
4. **验证**：浏览器访问 http://localhost:8080/api/auth/me
   返回 `{"code":1,"message":"未登录",...}` 即启动成功（拦截器在工作）。

## 内置演示账号（首次启动自动创建）

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| reviewer / reviewer2 | review123 | 审稿人（评委） |
| author | author123 | 作者 |

## 已实现接口（步骤 1）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/register | 注册（只允许 AUTHOR/REVIEWER） |
| POST | /api/auth/login | 登录（Session） |
| POST | /api/auth/logout | 登出 |
| GET  | /api/auth/me | 当前登录用户 |
| POST | /api/manuscripts | 投稿（multipart: title/category/description/file） |
| GET  | /api/manuscripts/mine | 我的稿件列表 |
| GET  | /api/manuscripts/{id} | 稿件详情（鉴权：管理员/本人作者/被分配审稿人） |
| GET  | /api/manuscripts/{id}/logs | 稿件流转记录（同上鉴权） |
| POST | /api/manuscripts/{id}/revisions | 作者修改重投（退修改状态专用，multipart） |

## 已实现接口（步骤 2）

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET  | /api/admin/manuscripts/pending | 待初审稿件池 | 管理员 |
| GET  | /api/admin/manuscripts/reviewing | 外审中稿件池（终审工作台） | 管理员 |
| GET  | /api/admin/reviewers | 审稿人列表（分配弹窗数据源） | 管理员 |
| GET  | /api/admin/stats | 仪表盘统计（各状态稿件数/用户数/奖项数） | 管理员 |
| POST | /api/admin/manuscripts/{id}/pass-initial | 初审通过并分配审稿人 body:{reviewerIds:[]} | 管理员 |
| POST | /api/admin/manuscripts/{id}/reject-initial | 初审退稿 | 管理员 |
| POST | /api/admin/manuscripts/{id}/assign | 追加审稿人 body:{reviewerIds:[]} | 管理员 |
| GET  | /api/admin/manuscripts/{id}/opinions | 查看稿件全部审稿意见 | 管理员 |
| POST | /api/admin/manuscripts/{id}/final | 终审决议 body:{decision:"ACCEPTED/REVISE/REJECTED"} | 管理员 |
| GET  | /api/reviews/mine | 我的评审任务 | 审稿人 |
| POST | /api/reviews/{assignmentId}/opinion | 提交审稿意见（一人一票） | 审稿人 |
| GET  | /api/awards | 奖项列表 | 登录用户 |
| POST | /api/awards | 创建奖项批次 body:{name,description} | 管理员 |
| POST | /api/awards/{id}/start | 开启评审 | 管理员 |
| POST | /api/awards/{id}/nominations | 提名录用稿件 body:{manuscriptId} | 管理员 |
| GET  | /api/awards/{id}/nominations | 提名列表（公示后全员可见） | 登录用户 |
| POST | /api/awards/{id}/nominations/{nid}/score | 评委打分（可覆盖更新） | 审稿人 |
| POST | /api/awards/{id}/publish?winnerCount=1 | 公示：算终评分+排名定奖 | 管理员 |

### 评奖规则（公示时执行）

- 每个提名作品：评委数 ≥ 3 时去掉最高分、最低分后取均值，否则直接取均值
- 按终评分降序排名，前 `winnerCount` 名 WON（获奖），其余 LOST（落选）
- 奖项状态机：PREPARING（筹备）→ REVIEWING（评审中）→ PUBLISHED（已公示）

## 状态机（核心设计）

```
PENDING_INITIAL(待初审) ──> UNDER_REVIEW(外审中) ──> ACCEPTED(已录用)
        │                       │      └─> REJECTED(已退稿)
        └──> REJECTED           └─> REVISE(退修改) ──> UNDER_REVIEW(重投送审)
```

- 所有状态变更只能走 `ManuscriptService.transition()`，先查合法流转表
  （`ManuscriptStatus.ALLOWED_TRANSITIONS`），非法流转抛异常。
- 每次流转写一条 `operation_log`，实现全程留痕。
- 作者端没有任何修改状态的接口——"作者不能自己把稿件改成已录用"由架构保证。

## 目录结构

```
backend/
├── pom.xml                 Maven 构建配置
├── mvnw / mvnw.cmd         Maven Wrapper（免装 Maven）
├── sql/schema.sql          7 张表建表脚本
└── src/main/
    ├── java/com/dm/platform/
    │   ├── PlatformApplication.java   启动入口
    │   ├── common/        Result / 异常 / 状态机定义
    │   ├── config/        Web配置 / 演示数据初始化
    │   ├── controller/    接口层
    │   ├── dto/           请求/响应对象
    │   ├── entity/        数据库实体
    │   ├── interceptor/   登录拦截器
    │   ├── mapper/        MyBatis 数据访问
    │   └── service/       业务逻辑（投稿/状态机）
    └── resources/application.yml   配置（数据源/上传/会话）
```
