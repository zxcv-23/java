# AI-NOTES —— 给 AI 助手的快速上下文（前端 guiniang-huajia-vue）

> 本文件是写给 AI 助手看的，目的是让下次会话跳过不必要的排查。人类用户请看上级目录 README.md。

## 项目概况
- Vite 6 + Vue 3（仅 vue 一个运行时依赖，安装很快），`<script setup>` 组合式 API
- dev server: `http://127.0.0.1:5173`（见 vite.config.js）
- 页面组件在 `src/components/`；**后台统一请求层在 `src/utils/adminApi.js`**
- 当前版本路径：`C:\Users\user\IdeaProjects\java\java-main\guiniang-huajia-vue`

## 与后端的对接
- **直连**（无代理）：`http://127.0.0.1:8081`
- 后台已配 CORS；**必须先启动后端**（`../springBootTest/start-backend.bat`）
- 前台接口 `/api/user/login`、`/api/user/register`、`/api/home`、`/api/search` 公开；`/api/profile/**` 带用户 JWT
- **后台所有接口都带管理员 JWT**，由 `src/utils/adminApi.js` 统一注入

## 后台管理（2026-11 鉴权加固版）
- `src/utils/adminApi.js`：
  - `readAdmin()` / `saveAdmin()` / `persistAdmin()` / `clearAdmin()` 管会话（key: `guiniang_admin`）
  - `adminLogin(account, password)` 公开登录
  - `adminFetch(path, options)` 自动加 `Authorization: Bearer <token>`、解析 `Result.data`、失败抛带可读 `message` 的 Error
  - **HTTP 401 → 清会话 + `window.dispatchEvent(new Event('admin:unauthorized'))`**；`App.vue` 监听后清 `currentAdmin` 并把 `mode` 切回 `admin-login`
- `AdminLogin.vue`：**已移除「演示身份进入」**（旧版 demo 能在后端无鉴权时真实写库，安全加固后不允许）。登录成功写 localStorage。
- `App.vue`：`goAdmin()` 会拒绝旧的 `{demo:true}` 残留会话；监听 `admin:unauthorized`。
- `AdminDashboard.vue` 三个标签页：
  | 标签 | 功能 |
  |------|------|
  | 条目管理 | 分页（5/10/20/50 条每页）、关键词 + **分类下拉** + 状态筛选、新增/修改/上下线/删除（逻辑删除进回收站） |
  | 回收站 | 查 `deleted=true` 的条目，支持「恢复」 |
  | 账号安全 | 修改自己口令（`PUT /api/admin/password`），成功后用返回的**新 token** 续期 |
- 顶部 4 张统计卡取 `GET /api/manage/stats` 的真实计数，不再由前端列表推算
- 「退出」先调 `/api/admin/logout` 让服务端作废旧 token，再清本地会话
- 进入后台时 `onMounted` 会先调 `/api/admin/me` 自检，token 过期/被禁用会被请求层自动弹回登录页

## 页面路由（无 vue-router）
- 全部切换由 `src/App.vue` 的 `mode` 状态机控制：`login | register | home | admin-login | admin | profile`，用 `<transition name="swap">` 做过渡。
- 登录态存在 `localStorage/sessionStorage` 的 `guiniang_user`（含 token），刷新后 `onMounted` 自动回 home。
- **个人中心保存资料的跳转链路**（2026-11 新增）：
  `PersonalCenter` 保存成功 → `emit('saved', {nickname, avatar})` → `App.profileSaved()` 合并进 `currentUser` 并 `persistUser()` 写回存储 → 900ms 后 `mode='home'` → `App.homeFlash` 通过 `:flash-msg` 传给 `HomePage`，由 `watch(flashMsg, {immediate:true})` 调 `flashToast` 展示一次。
  注意：`HomePage` 的 `userName` / `userAvatar` 是 `computed`，改 `props.user` 会即时反映到导航栏，不要把二者改回 setup 常量。
- `main.js` 注册了全局指令 `v-reveal` / `v-tilt`，后台统计卡用了 `v-tilt`。

## 环境事实
- Node.js 已装；本环境托管版 `C:\Users\user\.workbuddy\binaries\node\versions\22.22.2-3\node.exe`
- 每次开机 `node_modules` 会被删除 → 启动前需 `npm install`
- **本环境 Git Bash 的 PATH 缺 coreutils（`ls`/`head`/`dirname` 不可用），且 `npm` 脚本因 `/usr/bin/env bash` 失败**。校验构建请直接用 node 调 vite：
  ```bash
  "<node.exe>" "<项目目录>/node_modules/vite/bin/vite.js" build "<项目目录>"
  ```

## 启动方式
- **推荐：用户双击 `start-frontend.bat`**（自动：npm install（失败自动换 npmmirror 镜像）→ npm run dev）
- 停止：`stop-frontend.bat`（按端口 5173 杀进程）
- 手动：`npm install && npm run dev`

## 验证
```bash
curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:5173/   # 应为 200
```
2026-11-04：`vite build` 通过（35 个模块）。

## ⚠️ 注意
- **改了后端代码后必须重启后端**：前端固定打 8081，若 8081 上还是旧进程，新前端会拿到旧版「数组」格式的列表（新版要求 `data.list`），表现为列表空白。重跑 `start-backend.bat` 即可。
- 默认管理员 `admin / admin123`，首次登录请到「账号安全」改密。
