# AI-NOTES —— 给 AI 助手的快速上下文（后端 springBootTest）

> 本文件是写给 AI 助手（WorkBuddy/Claude 等）看的，目的是让下次会话**跳过不必要的排查**，直接干活。人类用户请看上级目录 README.md。

## 项目概况
- Spring Boot 3.4.5 + Java 17/21 + MyBatis-Plus 3.5.7 + MySQL 8，端口 **8081**，profile=dev
- 当前版本路径：`C:\Users\user\IdeaProjects\java\java-main\springBootTest`（前端同级 `guiniang-huajia-vue`）
- 前台注册/登录 + 个人中心：
  - `POST /api/user/register` `{"username","password"}`（用户名 2~20 位，密码 6~32 位，BCrypt）
  - `POST /api/user/login` `{"account","password"}` → 用户信息 + **scope=user 的 JWT**
  - `GET /api/profile`、`PUT /api/profile`、`POST /api/profile/avatar|password|logout`（需 `Authorization: Bearer <token>`，由 JwtInterceptor 保护）
  - `GET /api/home`、`GET /api/search`、`GET /user/test`（公开）
- 已配置 CORS（`config/CorsConfig.java`），前端直连 8081
- **统一返回结构**：`Result<T>` = `{code:200|400|500, message, data}`；分页用 `common/PageResult<T>` = `{list,total,page,size,pages}`

## 后台管理（2026-11 鉴权加固版）
- 默认管理员 **`admin / admin123`**（首次启动自动创建，登录后请到「账号安全」立即改密）
- **所有后台接口都要带 `Authorization: Bearer <后台JWT>`**，唯一例外是 `/api/admin/login`
- JWT 通过 `scope` 声明区分身份，`security/JwtService.java` 统一签发：
  - `generateToken(...)` → `scope=user`（24h，供 JwtInterceptor 校验）
  - `generateAdminToken(...)` → `scope=admin`（8h，`jwt.admin-expiration`，供 AdminJwtInterceptor 校验）
  - 旧版签发、没有 scope 的前台 token 按 `user` 兼容处理，用户无需重新登录
- `config/WebConfig.java` 注册两套拦截器，互不干扰：
  - `JwtInterceptor` → `/api/profile/**`，只接受 scope=user
  - `AdminJwtInterceptor` → `/api/manage/**` + `/api/admin/**`，排除 `/api/admin/login`；校验签名/过期/scope、管理员是否被禁用、`token_version` 是否一致
  - 401 直接返回 `{"code":401,"message":...}`，前端 adminApi 会清会话并跳回登录页
- 后台接口清单（均在 `controller/AdminController.java`）：
  | 方法 | 路径 | 说明 |
  |------|------|------|
  | POST | `/api/admin/login` | 登录（公开），返回 `AdminInfo{id,account,name,token}` |
  | GET | `/api/admin/me` | 身份自检 |
  | POST | `/api/admin/logout` | 自增 token_version，旧 token 立即失效 |
  | PUT | `/api/admin/password` | `{oldPassword,newPassword}` 改自己口令，返回**新 token** |
  | GET | `/api/manage/stats` | 概览计数 total/online/offline/deleted/categories |
  | GET | `/api/manage/items` | 分页列表，参数 `keyword,categoryId,status,deleted,page,size` |
  | POST | `/api/manage/items` | 新增条目 |
  | PUT | `/api/manage/items/{id}` | 局部修改（null 字段保持原值） |
  | PUT | `/api/manage/items/{id}/status?status=0\|1` | 上/下线 |
  | DELETE | `/api/manage/items/{id}` | 逻辑删除（进回收站） |
  | POST | `/api/manage/items/{id}/restore` | 从回收站恢复 |
- `deleted=true` 走回收站查询；这是自定义 `@Select`，不受 `@TableLogic` 影响，能看到已删除行。恢复用 `ItemMapper.restore()` 手写 UPDATE。
- `GlobalExceptionHandler` 已补 `RuntimeException` 兜底，返回 `code=500` + 可读原因并打日志，不再只显示「请稍后再试」。

## ⚠️ 数据库 schema 漂移（本项目最容易踩的坑）
- `entity/User.java` 有 13 个字段，缺 `email/phone/gender/bio/token_version` 时**注册能成功但登录必 500**。修复：`sql/fix_user_table.sql`（幂等、不删数据）。
- `entity/Admin.java` 现在有 **`token_version`** 列。旧库（如从 `sql/guiniang.sql` 导入）缺该列时，后台 token 校验会报 `Unknown column 'token_version'`。
  - `schema.sql` 的 `CREATE TABLE IF NOT EXISTS admin` 已含该列（全新库自动有）
  - **旧库由 `AdminServiceImpl.ensureTokenVersionColumn()` 在启动时用 information_schema 检查并幂等 `ALTER TABLE` 自动补齐**（不阻断启动）
  - 手工兜底：`sql/fix_admin_table.sql`
  - 判定结构是否就绪：`SHOW COLUMNS FROM admin LIKE 'token_version';`
- 绝不 DROP 库/表来"修"结构，所有迁移脚本都是补列、不删数据。

## 环境事实（已验证 2026-11-04）
- Java：本机 `java -version` 为 **Zulu 21.0.10**；pom `<java.version>17</java.version>` 以 `--release 17` 编译通过
- MySQL 8：`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`，root/123456，**业务库名是 `dd`**（不是 `guiniang`），3306 在跑
- Maven：PATH 里没有；可用缓存 `C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9`
  - **Git Bash 里 `mvn` / `mvnw` 会报 "Unable to access jarfile"，且本环境 PATH 缺 coreutils（ls/head/dirname 不可用）**
  - 可靠构建方式：直接用 java 调 classworlds 启动器，例如
    ```bash
    java -classpath "<maven-home>/boot/plexus-classworlds-2.8.0.jar" \
      "-Dclassworlds.conf=<maven-home>/bin/m2.conf" "-Dmaven.home=<maven-home>" \
      "-Dmaven.multiModuleProjectDirectory=<项目目录>" \
      org.codehaus.plexus.classworlds.launcher.Launcher -f "<项目目录>/pom.xml" -DskipTests package
    ```
- 依赖缓存 `~/.m2/repository` 完整，构建约 17 秒
- 测试数据：`scripttest01 / secret123`（id=1）

## ⚠️ 本机坑
1. **环境变量 `SERVER__PORT` / `SERVER__HOST` 会覆盖 `server.port`**（Spring 宽松绑定）。启动前必须清除，`start-backend.bat` 已自动处理；命令行 `--server.port=xxxx` 优先级更高，可临时用别的端口做隔离验证。
2. **MyBatis-Spring 兼容性**：pom 已锁 `mybatis-spring` 3.0.6（3.0.3 与 Spring Boot 3.4 不兼容）。不要降级。
3. **沙箱限制**：AI 环境里 Bash/PowerShell 都禁止直接调 `cmd.exe`；PowerShell 允许 `& .\xxx.bat`。
4. 改了后端代码后，**正在运行的进程不会热更新**：需重跑 `start-backend.bat`（会先杀 8081 旧进程再启动新 jar）。

## 启动方式（电脑每次重置，target 会被删除）
- **推荐：用户双击 `start-backend.bat`**（清环境变量→杀 8081 旧进程→检查/初始化 DB→构建→启动）
- 停止：`stop-backend.bat`
- AI 手动隔离验证（不打扰 8081 现有实例）：
  ```bash
  unset SERVER__PORT SERVER__HOST
  java -jar target/springBootTest-0.0.1-SNAPSHOT.jar --server.port=8082
  ```

## 验证
```bash
curl http://192.168.119.42:8081/user/test
curl -i http://192.168.119.42:8081/api/manage/items          # 应为 401（未登录）
curl -X POST http://192.168.119.42:8081/api/admin/login -H "Content-Type: application/json" \
     -d '{"account":"admin","password":"admin123"}'      # 取 data.token
curl -H "Authorization: Bearer <token>" "http://192.168.119.42:8081/api/manage/items?page=1&size=10"
```
2026-11-04 已跑过 17 项端到端用例（登录/401/作用域隔离/分页/筛选/统计/增删改/回收站/恢复/改密校验/登出失效），全部通过。

## 姊妹项目
前端 `../guiniang-huajia-vue`（Vite 6 + Vue 3，端口 5173，见其 AI-NOTES.md）。上级目录有 `start-all.bat` / `stop-all.bat` 一键脚本。

## 2026-11-05 功能完善

### 新增：前台条目浏览与收藏
| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/items` | 公开 | 分页浏览，参数 `keyword,categoryId,sort=new\|hot,page,size`（size 上限 50），返回 `PageResult<ItemSearchVO>` |
| GET | `/api/items/hot?limit=8` | 公开 | 热门榜，按 `view_count` 倒序 |
| GET | `/api/items/{id}` | 公开 | 详情（含 content/author/浏览量/收藏数），**每次调用 view_count +1**；下线/已删除返回业务提示 |
| GET | `/api/profile/favorites` | 用户 JWT | 我的收藏列表 |
| GET | `/api/profile/favorites/{itemId}` | 用户 JWT | 查询是否已收藏 |
| POST | `/api/profile/favorites/{itemId}/toggle` | 用户 JWT | 收藏/取消，返回 `{itemId,favorited,favoriteCount}` |

- 旧 `GET /api/search` 保留不变，老前端照常可用。
- 关键词统一做 LIKE 通配符转义（`!` 作为 ESCAPE 字符），修复「搜 `%` 命中全部条目」；`%` `_` 现在按普通字符匹配。
- `item` 新增 `view_count` 列；新增 `favorite` 表（唯一键 user_id + item_id），收藏表用物理删除，结构简单。
- 收藏接口放在 `/api/profile/**` 下，直接复用 `JwtInterceptor`，用户ID由服务端从 token 解析。

### 新增：后台分类 / 轮播管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/api/manage/categories` | 分类树（带 itemCount）/ 新增 |
| PUT/DELETE | `/api/manage/categories/{id}` | 局部修改 / 删除（有子分类或藏品时拒绝，并防父子成环） |
| GET/POST | `/api/manage/banners` | 列表（含隐藏）/ 新增 |
| PUT/DELETE | `/api/manage/banners/{id}` | 局部修改 / 删除 |
| PUT | `/api/manage/banners/{id}/status?status=0\|1` | 展示 / 隐藏 |

全部走已有 `/api/manage/**` 管理员 JWT 拦截，未登录仍返回 401。

### 结构自愈（重要）
- 新增 `config/SchemaMigration.java`（ApplicationRunner）：启动时用 information_schema 幂等补 `item.view_count`、`user` 扩展列、`admin.token_version`，并确保 `favorite` 表存在；失败只记日志、不阻断启动。
- **已移除 `schema.sql` 里的 DELIMITER 存储过程**：Spring 的 SQL 脚本执行器不支持该语法，旧写法每次启动都静默报错（被 `continue-on-error=true` 吞掉），这正是历史上「注册成功但登录 500」的根因。现在补列统一由 Java 迁移器负责，`schema.sql` 只保留纯 SQL。
- 旧库无需手工执行 `fix_*.sql` 也能自愈；手工脚本仍保留作兜底。

### 本机环境事实（2026-11-05 复核）
- 项目实际路径：`C:\Users\user\IdeaProjects\java\java-main\springBootTest`（2026-11-06 复核，D 盘那份已不存在）
- Java：`java -version` 为 **Zulu 21.0.10**；pom `<java.version>17</java.version>` 以 `--release 17` 编译，正常通过
- Maven home：`C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9`
- 可靠构建命令（Git Bash 里直接跑 `mvn` 仍会报 Unable to access jarfile）：
  ```bash
  java -classpath "<maven-home>/boot/plexus-classworlds-2.8.0.jar" \
    "-Dclassworlds.conf=<maven-home>/bin/m2.conf" "-Dmaven.home=<maven-home>" \
    "-Dmaven.multiModuleProjectDirectory=<项目目录>" \
    org.codehaus.plexus.classworlds.launcher.Launcher -f "<项目目录>/pom.xml" -DskipTests package
  ```
- 8081 常驻旧进程时，重新打包可能因 Windows 文件占用失败；可加 `-Dproject.build.directory=<项目目录>/target-verify` 输出到独立目录，再用 `--server.port=8082` 隔离验证。

## 2026-11-05 第二轮：8081 旧进程导致「红妆阁 · 0 件藏品」

### 现象
用户截图显示首页「红妆阁 · 0 件藏品」，且已出现「今日推荐 / 全部藏品 / 热门榜 / 我的珍藏」四个视图按钮。
按钮说明新前端已生效，但数据为 0。

### 根因（不是数据库没连上）
- `application-dev.properties`：`jdbc:mysql://127.0.0.1:3306/dd`（旧笔记里的 guiniang 已作废），root/123456，
  `spring.sql.init.mode=always` 启动即幂等建表灌数据。MySQL 3306 与 8081 都在监听，
  库内 `item=18 / category=13 / banner=4`，`item.view_count` 列也已存在。
- 真正原因：**8081 跑的是改造前的旧 jar**。`log/xx.log` 显示 PID 2760 于 **14:05:06** 启动，
  而新 class 14:18 编译、新 jar 14:24 才生成 —— 进程没重启，内存里始终是旧代码。
- 旧 jar 没有 `/api/items`、`/api/items/hot`、收藏、后台分类/轮播接口，也没有 `SchemaMigration`；
  实测 `/api/items` 请求会**挂起超时**（不是 404），`/api/home` 却正常返回，很容易误判成「接口偶发故障」。
- 前端 `onNav('红妆阁')` → `selectTop()` → `onSearch('', 3)` → `/api/search?categoryId=3`，
  旧后端拿不到正确结果，`resultTitle` 就渲染成「红妆阁 · 0 件藏品」。

### 修复
停止 PID 2760，用新 jar 重新在 8081 启动；前端无需改动，刷新即可。
`ItemMapper.search()` / `browsePage()` 都带 `WITH RECURSIVE` 子孙分类查询，重启后：
- `/api/search?categoryId=3` → **4 条**：红妆合集、胭脂盒、嫁衣、凤冠霞帔
- `/api/items?categoryId=3` → `total=4`；`/api/search?categoryId=1` → 6 条；`categoryId=10` → 2 条
- 分类树：`id=3 红妆阁` → 子 `10 嫁衣` / `11 胭脂`；导航 `navItems` 与 4 个一级分类一一对应

### 在 8081 正式实例上复跑 32 项端到端用例
`32/32 passed`（home / featured viewCount / 详情 / 浏览量自增 / 分页 / 热门榜 / LIKE 转义 /
旧 search 兼容 / 401 鉴权 / 收藏开关与列表 / 后台分类与轮播 CRUD）。
验证产生的测试用户 `wb_verify_20261105`、`WB验证分类`、`WB验证标语`、favorite 记录、
item 18 的浏览量已全部清理，库内回到 `18 / 13 / 4`。

### 排查与防复发（重要）
1. **判断 8081 是否新代码**：`GET /api/items?page=1&size=8` 应返回 `code=200` 且 `total=18`；
   旧 jar 会超时或 404。更快：看 `log/xx.log` 里 `Starting SpringBootTestApplication ... with PID`
   的时间戳，与 `target/*.jar` 的修改时间对比，进程早于 jar 就是旧代码。
2. **改了后端源码必须三步**：重新打包 jar → 停 8081 → 重启。`start-backend.bat` 只有在
   `target` 下没有 jar 时才会自动构建，jar 已存在会直接 `:RUN` 跑旧包，所以代码改完别只双击脚本，
   要么先删 `target/*.jar`，要么显式重新 package。
3. 8081 被 jar 占用导致 `mvn package` 的 repackage 失败时，输出到独立目录再覆盖回主 target。

## 2026-11-06：后端起不来 + 「注册的用户一直没保存」根因与修复

### 现场事实（已用 mysql / binlog 核实）
- 本机 MySQL 只有业务库 **`dd`**（admin=1, banner=4, category=13, item=18, **user=0**），
  **没有 `guiniang` 库**；`dd.user` 的 AUTO_INCREMENT 为 NULL，说明这张表从未插入过任何行。
- binlog（`client087-bin.000002`）只有一次 `CREATE DATABASE dd` + 导入 dump 的 36 条 INSERT
  （item/category/banner/admin），**没有任何 `INSERT INTO user`，也没有 guiniang 字样**。
  即：之前那些「注册成功」根本没有写进这台 MySQL（可能是在纯前端演示页上注册的）。
- `guiniang-huajia-vue/guinhuajia-Vue.html` 是**单文件前端演示页**，
  `onRegisterSubmit()` 只有 `setTimeout` 假成功，不发任何请求；
  真正调后端的是 Vite 项目的 `RegisterCard.vue` 和 `static/center.html`（`POST /api/user/register` → 8081）。
- `log/xx.log` 08:53:56 显示应用启动直接失败：`No schema scripts found at location 'classpath:schema.sql'`，
  8081 当时也没有监听，所以后端根本没在提供服务。

### 三个叠加的根因
1. **库名写错**：`application-dev.properties` 连 `guiniang`，实际库是 `dd`。
   即使能启动，也会连到不存在的库；而用户在 Navicat 里看的是 `dd`。
2. **启动即崩**：`spring.sql.init.mode=always` 配了**硬性** `classpath:schema.sql`，
   IDEA 增量编译时资源偶尔没进 classpath，缺脚本就抛异常让整个 ApplicationContext 启动失败。
3. **启动脚本会删库**：`start-backend.bat` 在 `USE guiniang` 失败时会
   `DROP DATABASE IF EXISTS guiniang` 再跑含 `DROP TABLE IF EXISTS user` 的 `init.sql`，
   `start-all.bat` 每次都会走这条分支 —— 这是「数据看起来总被清空」的最大隐患。

### 修复（全部幂等、不删任何数据）
- `application-dev.properties`：库名改 `dd`；脚本位置改为 **`optional:classpath:schema.sql` /
  `optional:classpath:data.sql`**（Spring Boot 3.4 的 `optional:` 前缀：脚本缺失时跳过，不再阻断启动），
  `continue-on-error=true` 保留。
- `application.properties`：修好写坏的 `logging.pattern.file`（原值把整行前缀写进了日志文件），
  修正包名 `com.fengling` → `com.fenglin.springboottest`。
- `SchemaMigration.java`：新增 `ensureUserTable()`，用 `CREATE TABLE IF NOT EXISTS` 兜底，
  即使 SQL 脚本被跳过也保证 user 表存在；`schema.sql` 的 user 定义补上 `token_version`。
- `start-backend.bat`：**删掉 DROP DATABASE**，库名统一 `dd`，建库后用
  `mysql -D dd < schema.sql` / `data.sql` 幂等初始化；并改成**每次都重新打包**，避免跑旧 jar。
- `sql/init.sql` 改为只 `CREATE DATABASE IF NOT EXISTS dd; USE dd;`（不再 DROP TABLE）；
  `sql/fix_user_table.sql` / `fix_admin_table.sql` 的库名同步改 `dd`。
- 判定是否修好：`GET http://192.168.119.42:8081/user/test` 能通，
  且 `SELECT COUNT(*) FROM dd.user;` 在注册后会增加。

### 注意
- 之前注册过的账号**无法从这台 MySQL 恢复**（binlog / 数据目录都没有对应记录）。
  `sql/backup/user_backup_20261102.sql` 里只有 2026-11-02 的两个旧账号 `ff` / `probe_ai_01`，
  且是旧表结构（缺 email/phone/gender/bio/token_version），需要时可按新结构手工 INSERT，别整文件导入（含 DROP TABLE）。
- 千万别把 `sql/guiniang.sql`（Navicat dump）直接导进已有库：它每个表前都有 `DROP TABLE IF EXISTS`。

### 验证记录（2026-11-06）
- Maven `-DskipTests package` → BUILD SUCCESS；用新 jar 在 **8083** 隔离端口启动成功，`/user/test` → 200。
- `POST /api/user/register` → `code=200`，`dd.user` 行数 **0 → 1**（id=1 / status=1 / token_version=0）；
  同账号 `POST /api/user/login` → 200 并返回 JWT；`GET /api/home` → 200（分类数据正常）。
  测试账号 `wb_verify_20261106` 已删除，`dd.user` 回到 0 行。
- 排查期间 `dd` 库曾被外部操作 `DROP DATABASE dd`（binlog 可查），
  已用 `sql/guiniang.sql` 原样恢复：admin=1 / banner=4 / category=13 / item=18 / user=0，
  启动时 `SchemaMigration` 还补建了 `favorite` 表。**没有丢任何业务数据。**
- 复验方式：`GET http://192.168.119.42:8081/user/test` 能通；注册一个账号后
  `SELECT COUNT(*) FROM dd.user;` 应 +1（记得在 Navicat 里切到「数据」页看，设计页只有字段定义）。

## 相亲阁跳转页（2026-11-06 新增）

项目原本就有一个「前往相亲阁」按钮（`AdminDashboard.vue` + `PartnerController`），
但 `partner.site.url` 指向 `127.0.0.1:5173/` 前端首页，并没有真正的相亲展示内容。
本次把它补成一个可用的**搜索式相亲展示 / 挑选页**。

**入口位置（同日调整）**：按钮已从后台管理页**移到首页导航栏**
（`guiniang-huajia-vue/src/components/HomePage.vue` 的 `.nav-partner`）。
`AdminDashboard.vue` 里的 `partnerSite` / `fetchPartnerSite()` / `openPartnerSite()` / `.partner-link`
整段已删除；首页通过 `/api/partner/site` 读同一份配置，跳转时带 `from=home`，
`dating.html` 只要 URL 带 `from` 参数就显示「从主站跳转过来」的提示。

### 新增文件
- `src/main/resources/static/dating.html`：Google 风格极简搜索页（浅色）、性别/关键词/「只看已心动」筛选、
  嘉宾卡片、详情弹层、心动名单抽屉（存 `localStorage`，key `dating_picks_v1`）。
  右上角有「去 Google 搜相亲资料」外链，页面会先读 `/api/partner/site` 的 `googleUrl`。
- `controller/DatingController.java`：`GET /api/dating/candidates?keyword=&gender=&limit=`。
- `dto/DatingCandidateVO.java`：脱敏嘉宾视图。

### 接口 / 端点
| 端点 | 说明 |
| --- | --- |
| `GET /dating.html` | 相亲展示页（后端静态资源，换端口不用改配置） |
| `GET /api/dating/candidates` | 只读嘉宾列表，`status=1`，最多 100 条 |
| `GET /api/partner/site` | 返回 `{name,url,googleUrl,enabled}`；相对 url 会按当前请求补全为绝对地址 |
| `GET /go/partner` | 302 到 `partner.site.url`（默认 `/dating.html`） |
| `GET /go/google` | 302 到 `partner.site.google-url` |

### 重要约定
- **隐私**：`/api/dating/candidates` 是公开接口，只投影 id / nickname / avatar / gender / bio / 入阁日期，
  **不返回** password、phone、email、username、tokenVersion，也不要直接返回 `User` 实体。
- **演示数据兜底**：`dd.user` 目前 0 行，页面在真实列表为空或后端不可用时改展示 6 位内置演示嘉宾，
  并在页面顶部黄条明确标注「演示资料」；注册用户出现后自动切换为真实数据。演示数据不入库。
- **改目标站**：只想跳转外部相亲网站的话，把 `application-dev.properties` 的
  `partner.site.url` 改成那个完整网址即可，一行搞定；写相对路径则留在本服务内。
  目标协议只允许 `/`（不含 `//`）与 `http(s)://`，避免开放重定向。
- Google 外链在国内网络通常不可直接访问，需要的话把 `partner.site.google-url` 换成任意可访问的搜索站。


## 备忘录模块（2026-11-09 新增）
- 表 `memo`：`src/main/resources/schema.sql` 里 `CREATE TABLE IF NOT EXISTS`，启动自动建表；也可手动执行 `sql/create_memo_table.sql`（幂等）。
- 字段：id / user_id / title / content / done / pinned / color(violet|pink|gold|mint) / create_time / update_time；**物理删除**，实体不含 deleted 字段（避免全局 logic-delete-field 生效）。
- 接口挂在 `/api/profile/memos`，直接复用 JwtInterceptor（`/api/profile/**`），userId 由 token 解析注入，前端不传：
  - GET 列表 / POST 新增 / PUT /{id} 部分更新 / DELETE /{id}
- 分层：entity/Memo、mapper/MemoMapper、dto/MemoRequest、service/MemoService(+impl)、controller/MemoController。所有查询/修改/删除都带 `eq(user_id)`，越权返回 400「这条备忘不存在，或不属于你」。
- 联调：临时实例 8082 跑过 12 项用例（401 拦截、CRUD、部分更新、数据隔离），全部通过；测试数据已清理。
