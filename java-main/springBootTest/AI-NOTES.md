# AI-NOTES —— 给 AI 助手的快速上下文（后端 springBootTest）

> 本文件是写给 AI 助手（WorkBuddy/Claude 等）看的，目的是让下次会话**跳过不必要的排查**，直接干活。人类用户请看 README.md。

## 项目概况
- Spring Boot 3.4 + Java 21 + MyBatis-Plus 3.5.7 + MySQL 8，端口 **8081**，profile=dev
- 登录/注册小项目（表 `user`）。**注册只填用户名+密码，但 `User` 实体还有 email/phone/gender/bio/token_version 五个扩展列（个人中心用）**，建表必须带上，否则登录必崩（见下方坑 4）。接口契约：
  - `POST /api/user/register` `{"username","password"}`（用户名 2~20 位，密码 6~32 位，BCrypt）
  - `POST /api/user/login` `{"account","password"}` → 返回用户信息 + JWT
  - `GET /api/profile`、`PUT /api/profile`、`POST /api/profile/avatar|password|logout`（需 `Authorization: Bearer <token>`）
  - `GET /api/home`、`GET /api/search`、`GET /user/test`（自检）
- 已配置 CORS（`config/CorsConfig.java`），前端直连 8081
- 当前版本路径：`D:\workSpace\java\java-main\springBootTest`（前端同级 `guiniang-huajia-vue`）

## 环境事实（已验证）
- Java 21: `C:\Program Files\Zulu\zulu-21`（PATH 里的 java 也可用）
- MySQL 8: `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`，root/123456，库 `guiniang`
- Maven: PATH 里没有；本地缓存在 `~/.m2/wrapper/dists/apache-maven-*/<hash>/apache-maven-*/bin/mvn.cmd`（用 cmd/bat 调用没问题）
  - `.mvn/wrapper/maven-wrapper.properties` 已补全，`mvnw.cmd` 可作兜底
- 依赖缓存 `~/.m2/repository` 完整，构建只要几秒
- 测试数据：`scripttest01 / secret123`（id=1）

## ⚠️ 本机坑（每次都要注意）
1. **环境变量 `SERVER__PORT` / `SERVER__HOST` 会覆盖 `server.port`**（Spring 宽松绑定）。启动前必须清除，否则 Tomcat 抢别的端口启动失败。`start-backend.bat` 已自动处理。
2. **Git Bash 里 `mvn`/`mvnw` 报 "Unable to access jarfile"**——Git Bash 把 `/c/...` 路径传给 Windows java。解决：用 `.bat`（PowerShell 里 `& .\start-backend.bat` 可跑通），或 `cygpath -w` 转换路径后直接调 classworlds 启动器。
3. **MyBatis-Spring 兼容性**：pom 已锁 `mybatis-spring` 3.0.6（3.0.3 与 Spring Boot 3.4 不兼容）。不要降级。
4. **数据库 schema 漂移（本项目最容易踩的坑）**：`entity/User.java` 有 13 个字段，`user` 表必须全都有。缺 `email`/`phone`/`gender`/`bio`/`token_version` 时：
   - **注册能成功**（MyBatis-Plus 插入只写非 null 字段，看不出来）
   - **登录必 500**：`Unknown column 'email' in 'field list'`（selectOne 查全字段）
   - 前端 `GlobalExceptionHandler` 没兜住非业务异常，页面只显示「结契失败，请稍后再试」，**看不到真实原因**——排查时直接看后端控制台或自己 curl 一次登录接口
   - 修复：执行 `sql\fix_user_table.sql`（幂等、不删数据，只补缺失列），**不要 DROP 库**。`start-backend.bat` 已改为检测这 5 个列并自动补齐。
   - 判定"新旧版本"不能再只看 email 列是否存在——现在正确结构**本来就该有** email。
5. **沙箱限制**：AI 环境里 Bash/PowerShell 都禁止直接调 `cmd.exe`，但 PowerShell 允许 `& .\xxx.bat` 方式运行批处理。

## 启动方式（电脑每次重置，target 会被删除）
- **推荐：用户双击 `start-backend.bat`**（相对路径；自动：清环境变量→杀 8081 旧进程→检查/初始化 DB→构建→启动）
- 停止：`stop-backend.bat`（按端口 8081 杀进程）
- AI 手动启动（Git Bash）：
  ```bash
  cd <本目录>
  unset SERVER__PORT SERVER__HOST
  java -jar target/springBootTest-0.0.1-SNAPSHOT.jar --server.port=8081   # jar 不在就先用 maven 构建
  ```

## 验证
```bash
curl http://127.0.0.1:8081/user/test
curl -X POST http://127.0.0.1:8081/api/user/register -H "Content-Type: application/json" -d '{"username":"t01","password":"secret123"}'
curl -X POST http://127.0.0.1:8081/api/user/login -H "Content-Type: application/json" -d '{"account":"t01","password":"secret123"}'
```

## 姊妹项目
前端 `../guiniang-huajia-vue`（Vite + Vue 3，端口 5173，见其 AI-NOTES.md）。上级目录有 `start-all.bat` / `stop-all.bat` 一键脚本。
