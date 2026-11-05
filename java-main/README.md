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
3. 检查 MySQL `guiniang` 库：不存在则自动执行 `sql\init.sql` 建库建表；`user` 表缺字段（email / phone / gender / bio / token_version）时自动执行 `sql\fix_user_table.sql` 补齐，**不删除任何数据**
4. `target\springBootTest-0.0.1-SNAPSHOT.jar` 不存在时自动用 Maven 构建（跳过测试）；Maven 按顺序查找：`~/.m2/wrapper/dists` 缓存 → PATH 中的 `mvn` → 项目自带 `mvnw.cmd`
5. 以 `--server.port=8081` 显式启动（双保险）

**前端 `start-frontend.bat`：**
1. `node_modules` 不存在时自动 `npm install`（失败自动换国内镜像 npmmirror 重试）
2. 启动 Vite 开发服务器（热更新）

**停止脚本（`stop-*.bat`）：** 按端口（8081 / 5173）查找监听进程并结束。

## 后台管理（判官台）

前端首页导航栏点「后台管理」进入，默认账号 **`admin / admin123`**（首次启动后端自动创建）。**请登录后在「账号安全」页立即改密。**

- 所有后台接口都需要管理员 JWT，请求头 `Authorization: Bearer <token>`；登录接口除外。凭证有效期 8 小时，过期自动退回登录页。
- 前端已移除「演示身份进入」——鉴权加固后不再允许无凭证进入并写入真实数据。
- 功能：条目管理（分页、关键词/分类/状态筛选、新增/修改/上下线/删除）、回收站（恢复逻辑删除的条目）、账号安全（改密）、真实统计看板。
- 管理员表 `admin` 新增 `token_version` 列用于「改密/登出即失效」。旧库启动时会自动幂等补列；如需手工执行：`mysql -uroot -p123456 < springBootTest\sql\fix_admin_table.sql`（不删数据）。
- 接口契约与排错要点见 `springBootTest\AI-NOTES.md`。

## 前置条件

- **Java 17+**（脚本优先用 `C:\Program Files\Zulu\zulu-21`，找不到则用 PATH 中的 `java`）
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
- **注册成功但点「缔结婚约」没反应 / 提示「结契失败，请稍后再试」**：多数是 `user` 表缺字段（`Unknown column 'email'` 导致登录 500）。执行 `mysql -uroot -p123456 < springBootTest\sql\fix_user_table.sql` 补齐字段即可，**不需要删库**；也可以直接重跑 `start-backend.bat`（会自动补齐）
- **前端页面打开但注册/登录无响应**：后端没启动，先跑 `start-backend.bat`
- **改了后端代码**：删掉 `target\springBootTest-0.0.1-SNAPSHOT.jar` 再跑脚本即可触发重新构建；**正在运行的 8081 进程不会热更新，必须重跑 `start-backend.bat`**（脚本会先杀旧进程）
- **后台列表空白 / 一直跳回登录页**：多半是 8081 上还跑着旧版后端（旧接口返回数组且无鉴权），重跑 `start-backend.bat` 即可；若提示 `Unknown column 'token_version'`，执行 `springBootTest\sql\fix_admin_table.sql`
- **电脑重置后（target / node_modules 被删）**：直接双击脚本即可，会自动重新构建 / 重装依赖

## 功能一览（2026-11 补充）

| 端 | 功能 |
|----|------|
| 前台 | 注册 / 登录 / 个人中心（头像上传、改密、登出）、首页推荐与轮播、分级分类搜索、**条目详情**（正文 / 作者 / 浏览量）、**分页浏览与热门榜**、**收藏与「我的珍藏」** |
| 后台判官台 | 条目管理（分页 / 筛选 / 增删改 / 上下线 / 回收站）、**分类管理**、**轮播标语管理**、统计看板、账号安全（均需管理员 JWT） |

- 首页卡片现在可以点开看详情；打开一次详情，该条目浏览量 +1，卡片上会显示浏览与收藏数。
- 「全部藏品」分页加载，「热门榜」按浏览量排序，「我的珍藏」需登录后使用。
- 数据库结构（浏览量列、收藏表、旧库补列）由后端启动时自动幂等补齐，无需手工执行 SQL。
- 完整接口契约见 `springBootTest\AI-NOTES.md`。
