# AI-NOTES —— 给 AI 助手的快速上下文（后端 springBootTest）

> 本文件是写给 AI 助手（WorkBuddy/Claude 等）看的，目的是让下次会话**跳过不必要的排查**，直接干活。人类用户请看上级目录 README.md。

## 项目概况
- Spring Boot 3.4.5 + Java 17/21 + MyBatis-Plus 3.5.7 + MySQL 8，端口 **8081**，profile=dev
- 当前版本路径：`D:\workSpace\java\java-main\springBootTest`（前端同级 `guiniang-huajia-vue`）
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
- MySQL 8：`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`，root/123456，库 `guiniang`，3306 在跑
- Maven：PATH 里没有；可用缓存 `C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9`
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
curl http://127.0.0.1:8081/user/test
curl -i http://127.0.0.1:8081/api/manage/items          # 应为 401（未登录）
curl -X POST http://127.0.0.1:8081/api/admin/login -H "Content-Type: application/json" \
     -d '{"account":"admin","password":"admin123"}'      # 取 data.token
curl -H "Authorization: Bearer <token>" "http://127.0.0.1:8081/api/manage/items?page=1&size=10"
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
- 项目实际路径：`D:\workSpace\java\java-main\springBootTest`（不再是 `C:\Users\user\IdeaProjects\...`）
- Java：`java -version` 为 **Zulu 21.0.10**；pom `<java.version>17</java.version>` 以 `--release 17` 编译，正常通过
- Maven home：`C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9`
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
- `application-dev.properties`：`jdbc:mysql://127.0.0.1:3306/guiniang`，root/123456，
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
