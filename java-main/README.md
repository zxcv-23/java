# 启动脚本使用说明

本项目包含两个子项目：

| 子项目 | 说明 | 地址 |
|--------|------|------|
| `springBootTest` | Spring Boot 3.4 后端（Java 21 + MyBatis-Plus + MySQL） | http://localhost:8081 |
| `guiniang-huajia-vue` | Vue 3 + Vite 前端 | http://127.0.0.1:5173 |

前端通过 `fetch('http://127.0.0.1:8081/api/user/...')` 直连后端（后端已配置 CORS）。

## 快速开始

| 脚本 | 作用 |
|------|------|
| **`start-all.bat`** | 一键启动前后端（各开一个窗口） |
| `stop-all.bat` | 一键停止前后端 |
| `springBootTest\start-backend.bat` | 单独启动后端 |
| `springBootTest\stop-backend.bat` | 单独停止后端 |
| `guiniang-huajia-vue\start-frontend.bat` | 单独启动前端 |
| `guiniang-huajia-vue\stop-frontend.bat` | 单独停止前端 |

双击运行即可，无需命令行。脚本为 **GBK 编码、相对路径**，项目目录整体移动后依然可用。

> 两个子项目里各有一个 `AI-NOTES.md`，是写给 AI 助手的快速上下文，请勿删除。

## 脚本内置的自动化逻辑

**后端 `start-backend.bat`：**
1. 清除 `SERVER__PORT` / `SERVER__HOST` 环境变量 —— 防止外部环境变量通过 Spring 宽松绑定覆盖配置文件里的 `server.port=8081`
2. 结束占用 8081 端口的旧进程，避免端口冲突
3. 检查 MySQL `guiniang` 库：不存在则自动执行 `sql\init.sql` 建库建表；若检测到**旧版表结构**（含 email 列）会提示是否重建
4. `target\springBootTest-0.0.1-SNAPSHOT.jar` 不存在时自动用 Maven 构建（跳过测试）；Maven 按顺序查找：`~/.m2/wrapper/dists` 缓存 → PATH 中的 `mvn` → 项目自带 `mvnw.cmd`
5. 以 `--server.port=8081` 显式启动（双保险）

**前端 `start-frontend.bat`：**
1. `node_modules` 不存在时自动 `npm install`（失败自动换国内镜像 npmmirror 重试）
2. 启动 Vite 开发服务器（热更新）

**停止脚本（`stop-*.bat`）：** 按端口（8081 / 5173）查找监听进程并结束。

## 前置条件

- **Java 21**（脚本优先用 `C:\Program Files\Zulu\zulu-21`，找不到则用 PATH 中的 `java`）
- **MySQL 8** 在本机 3306 运行，账号 root / 123456（与 `application-dev.properties` 一致）
- **Node.js 18+**（建议 20 或 22）和 npm

## 手动命令（不想用脚本时）

```bash
# 后端
cd springBootTest
mvn -DskipTests clean package
java -jar target/springBootTest-0.0.1-SNAPSHOT.jar --server.port=8081

# 前端
cd guiniang-huajia-vue
npm install
npm run dev
```

## 常见问题

- **后端启动报端口被占 / 端口不是 8081**：启动前环境里有 `SERVER__PORT` 变量（脚本已自动清除）。手动启动时先执行 `set SERVER__PORT=`（cmd）或 `unset SERVER__PORT`（Git Bash）
- **Git Bash 里 `mvn` / `mvnw` 报 "Unable to access jarfile"**：Git Bash 把 Unix 路径传给了 Windows 版 java，属已知兼容问题，请改用本脚本的 `.bat` 方式
- **注册接口报 500（Field 'email' doesn't have a default value）**：数据库里是旧版表结构，运行 `start-backend.bat` 按提示重建，或手动执行：`mysql -uroot -p123456 -e "DROP DATABASE guiniang;"` 然后导入 `sql\init.sql`
- **前端页面打开但注册/登录无响应**：后端没启动，先跑 `start-backend.bat`
- **改了后端代码**：删掉 `target\springBootTest-0.0.1-SNAPSHOT.jar` 再跑脚本即可触发重新构建
- **电脑重置后（target / node_modules 被删）**：直接双击脚本即可，会自动重新构建 / 重装依赖
