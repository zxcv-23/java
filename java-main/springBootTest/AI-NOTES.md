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
- Java：PATH 里是 `C:\Program Files\Zulu\zulu-21` 的说明已过时，本机实际是 **Microsoft jdk-17**（`java -version` 可用），pom `<java.version>17</java.version>` 编译通过
- MySQL 8：`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`，root/123456，库 `guiniang`，3306 在跑
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
curl http://127.0.0.1:8081/user/test
curl -i http://127.0.0.1:8081/api/manage/items          # 应为 401（未登录）
curl -X POST http://127.0.0.1:8081/api/admin/login -H "Content-Type: application/json" \
     -d '{"account":"admin","password":"admin123"}'      # 取 data.token
curl -H "Authorization: Bearer <token>" "http://127.0.0.1:8081/api/manage/items?page=1&size=10"
```
2026-11-04 已跑过 17 项端到端用例（登录/401/作用域隔离/分页/筛选/统计/增删改/回收站/恢复/改密校验/登出失效），全部通过。

## 姊妹项目
前端 `../guiniang-huajia-vue`（Vite 6 + Vue 3，端口 5173，见其 AI-NOTES.md）。上级目录有 `start-all.bat` / `stop-all.bat` 一键脚本。
