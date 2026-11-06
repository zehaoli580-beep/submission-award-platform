# 数字媒体作品投稿/评奖平台

> 「数字媒体工程」课程结课大作业。基于 **Spring Boot 3 + Vue 3** 的前后端分离系统，
> 实现数字媒体作品（图片/视频）从 **投稿 → 初审 → 双人盲审 → 终审录用 → 提名 → 评奖打分 → 公示排名** 的完整业务闭环。

仓库地址：https://github.com/zehaoli580-beep/submission-award-platform

---

## 一、这个项目是什么

一个三角色投稿评奖平台：

| 角色 | 职责 | 演示账号 |
|---|---|---|
| 管理员 ADMIN | 初审稿件、分配审稿人、终审决议、创建奖项、提名作品、公示 | `admin / admin123` |
| 审稿人 REVIEWER | 外审打分评语（盲审，看不到作者）、评奖阶段当评委打分 | `reviewer / review123`<br>`reviewer2 / review123` |
| 作者 AUTHOR | 上传作品投稿（图片/视频）、查看进度、退修改后重投 | `author / author123`<br>`author2 / author123` |

核心业务流（两道关）：

```
第一道关·能不能录用:  投稿 → 初审(管理员核格式) → 外审(2人盲评打分①) → 终审(录用/退修改/退稿)
第二道关·谁拿奖:      提名(只能提名已录用稿件) → 评委打分②(≥3人) → 公示(去最高最低取均值排名次)
```

## 二、技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.5 (JDK 25) + MyBatis 3.0.5 + MySQL 8.4.7 |
| 前端 | Vue 3 + Vite + Element Plus + Axios + Pinia |
| 认证 | Session + BCrypt 密码加密 |
| 构建 | Maven Wrapper（自带 mvnw，无需单独安装 Maven） |

数据库共 **8 张表**：user / manuscript / review_assignment / review_opinion /
award / nomination / nomination_score / operation_log，建表脚本见 `backend/sql/schema.sql`（幂等，可重复执行）。

## 三、目录结构

```
submission-award-platform/
├── backend/                  # Spring Boot 后端
│   ├── src/main/java/com/dm/platform/
│   │   ├── common/           # Result 统一响应 / BusinessException / ManuscriptStatus 状态机 / Auths 角色守卫
│   │   ├── config/           # WebConfig / LoginInterceptor / DataInitializer(自动建5个演示账号)
│   │   ├── controller/       # 5 个 Controller 共 24 个接口
│   │   ├── service/          # AuthService / ManuscriptService / ReviewService / AwardService
│   │   ├── mapper/           # 8 个 MyBatis 注解式 Mapper
│   │   └── entity/ dto/ interceptor/
│   ├── sql/schema.sql        # 建库建表脚本
│   └── application.yml       # 端口 8080 / 上传目录 / 扩展名白名单
├── frontend/                 # Vue 3 前端
│   └── src/
│       ├── views/            # Login / Layout / AuthorHome / ReviewerHome / AdminHome / AwardList(领奖台)
│       ├── components/MediaPreview.vue   # 图片缩略图+视频在线播放(全平台复用)
│       ├── api/ store/ router/ utils/
├── docs/
│   ├── 项目说明.md           # 本文档
│   ├── 截图清单.md           # 报告所需的 22 张截图指引
│   └── 演示脚本.md           # 六幕业务闭环演示(含账号密码与讲解词)
└── .gitignore
```

## 四、如何跑起来（四步）

前置：本机需有 **JDK 25**、**Node.js**、**MySQL 8.4.7**（绿色版在 `D:\dev-tools\mysql-8.4.7`，双击 `start-mysql.bat` 启动，等黑窗出现 ready 字样）。

```bash
# 1. 启动 MySQL（双击 start-mysql.bat，等待 ready）

# 2. 启动后端（首次运行自动建库建表+创建演示账号）
cd backend
./mvnw spring-boot:run            # Windows 下: mvnw.cmd spring-boot:run
# 看到 Tomcat started on port 8080 即成功

# 3. 启动前端（新开终端）
cd frontend
npm install                       # 首次需要
npm run dev
# 看到 Local: http://localhost:5173

# 4. 浏览器访问
http://localhost:5173             # 用上文演示账号登录
```

> 前端 Vite 已配置代理：`/api` 与 `/files` 请求自动转发到后端 8080，无需改任何配置。

## 五、核心设计（面试可讲）

1. **状态机驱动的稿件流转**：`ManuscriptStatus` 定义合法流转表（待初审→外审中→已录用/退修改/退稿），任何接口跳状态直接抛异常。
2. **事务保证原子性**：初审通过与分配审稿人在同一 `@Transactional` 中，分配失败则初审状态一并回滚。
3. **盲审**：审稿人接口返回的 DTO 在后端层面就不含作者信息（`ReviewTaskView` 无 authorId），抓包绕过前端也没用。
4. **一人一票**：评委打分靠数据库 UNIQUE KEY 约束，重复提交走 upsert 覆盖更新。
5. **评奖算法**：公示时终评分 = 去掉最高分和最低分后取均值（评委≥3人时），防人情分/恶意分；按终评分排名定金银铜。
6. **完备性校验**：终审要求所有审稿人意见齐；公示要求每个提名都有打分；提名只能是已录用稿件且同奖项内唯一。
7. **全程留痕**：每次状态变更/分配/打分都写 operation_log，管理员后台可查。
8. **越权拦截**：LoginInterceptor + Auths.requireRole 双层校验，E2E 越权测试 10 项全拒。

## 六、测试情况

- 后端编译通过；E2E 全流程 31 步（5 账号：投稿→初审分配→双盲审→终审→建奖项→提名→打分→公示）全部通过。
- 演示数据库中留有 2 篇已录用稿件 + 2 个已公示奖项（终评分 90.00 / 67.50，用于验证去极值算法）。
- 前端组件走查通过（图标注册、ElMessageBox 异常捕获等已修复）。

## 七、常见问题

| 现象 | 原因与解决 |
|---|---|
| 后端启动报连接数据库失败 | MySQL 未 ready 就启动了后端；等黑窗最后一行出现 ready 再启动 |
| 视频预览黑屏有声音 | 浏览器不支持 H.265；换 H.264 编码的视频（演示视频用 `桌面\大作业素材\演示视频-H264.mp4`） |
| `npm run dev` 报端口占用 | 关掉占用 5173 的进程，或换端口后同步改 vite 代理 |
| 想重置数据 | 停后端，DROP DATABASE 后重建（schema.sql 幂等），重启后端自动初始化 |

## 八、相关文档

- `docs/截图清单.md` —— 结课报告 22 张截图指引（按四阶段）
- `docs/演示脚本.md` —— 验收现场六幕演示脚本（含每步账号密码与讲解词）
- `backend/README.md` / `frontend/README.md` —— 前后端各自的启动说明与接口清单
