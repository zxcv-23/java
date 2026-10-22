# AI-NOTES —— 给 AI 助手的快速上下文（前端 guiniang-huajia-vue）

> 本文件是写给 AI 助手看的，目的是让下次会话跳过不必要的排查。人类用户请看上级目录 README.md。

## 项目概况
- Vite 6 + Vue 3（仅 vue 一个运行时依赖，安装很快）
- dev server: `http://127.0.0.1:5173`（见 vite.config.js）
- 页面组件在 `src/components/`（LoginCard、RegisterCard、NightSky 等）

## 与后端的对接
- **直连**（无代理）：`fetch('http://127.0.0.1:8081/api/user/login')` / `.../register`
- 后端已配 CORS；**必须先启动后端**（`../springBootTest/start-backend.bat`）

## 环境事实
- Node.js 已装（`where npm` 可找到）
- 每次开机 `node_modules` 会被删除 → 启动前需 `npm install`

## 启动方式
- **推荐：用户双击 `start-frontend.bat`**（自动：npm install（失败自动换 npmmirror 镜像）→ npm run dev）
- 停止：`stop-frontend.bat`（按端口 5173 杀进程）
- AI 手动（Git Bash）：
  ```bash
  cd <项目目录>
  [ -d node_modules ] || npm install
  npm run dev   # 后台任务方式运行，注意 Git Bash 里直接跑没问题
  ```

## 验证
```bash
curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:5173/   # 应为 200
```
