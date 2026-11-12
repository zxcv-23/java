# AI-NOTES —— 给 AI 助手的快速上下文（前端 guiniang-huajia-vue）

> 本文件是写给 AI 助手看的，目的是让下次会话跳过不必要的排查。人类用户请看上级目录 README.md。

## 项目概况
- Vite 6 + Vue 3（仅 vue 一个运行时依赖，安装很快），`<script setup>` 组合式 API
- dev server: `http://192.168.68.22:5173`（见 vite.config.js）
- 页面组件在 `src/components/`；**后台统一请求层在 `src/utils/adminApi.js`**
- 当前版本路径：`C:\Users\user\IdeaProjects\java\java-main\guiniang-huajia-vue`

## 与后端的对接
- **直连**（无代理）：`http://192.168.68.22:8081`
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
curl -s -o /dev/null -w "%{http_code}" http://192.168.68.22:5173/   # 应为 200
```
2026-11-04：`vite build` 通过（35 个模块）。

## ⚠️ 注意
- **改了后端代码后必须重启后端**：前端固定打 8081，若 8081 上还是旧进程，新前端会拿到旧版「数组」格式的列表（新版要求 `data.list`），表现为列表空白。重跑 `start-backend.bat` 即可。
- 默认管理员 `admin / admin123`，首次登录请到「账号安全」改密。

## 2026-11-05 首页功能补齐（HomePage.vue）
- 卡片现在**可点击** → 详情弹层：`GET /api/items/{id}`，展示正文 / 作者 / 浏览量 / 收藏数，并自动给浏览量 +1。
- 登录用户可「收入珍藏」：`POST /api/profile/favorites/{id}/toggle`；导航栏「我的珍藏」→ `GET /api/profile/favorites`。请求带 `Authorization: Bearer <user.token>`，401 会提示重新登录；未登录点收藏只弹提示、不发请求。
- 结果区新增视图切换：今日推荐 / 全部藏品（分页 + 加载更多）/ 热门榜，走 `GET /api/items?page=&size=&sort=new|hot`；另有 `GET /api/items/hot?limit=` 备用。
- 卡片渲染统一用 `visibleCards` computed，标题用 `resultHeading` computed；**不要再新增第三套 v-for 卡片循环**，否则筛选/分页状态会打架。
- 详情与收藏是两套独立弹层（`showDetail` / `showFavorites`），从珍藏列表点条目会先关列表再开详情。
- 全部改动只在 `HomePage.vue` 内，未触碰 `App.vue` 的状态机。


## 备忘录模块（2026-11-09 新增）
- `src/components/MemoBoard.vue`：便签板页面（新增/编辑/删除、完成勾选、置顶、配色、筛选、搜索）。
- 路由：`App.vue` 的 mode 增加 `memo`；未登录点「备忘录」会先去登录，登录后自动回到备忘录（pendingMemo，逻辑同个人中心）。
- 入口：`HomePage.vue` 顶部导航 `.nav-memo` 与左侧 rail `.rail-memo`，emit `goMemo`。
- 接口：`GET/POST /api/profile/memos`、`PUT/DELETE /api/profile/memos/{id}`，带 `Authorization: Bearer <guiniang_user.token>`；401 时 emit `needLogin`。
- 注意：后端必须重启（新 jar）才有这些接口；`start-backend.bat` 会先杀掉 8081 旧进程再重新打包。


## 花间小游戏（2026-11 全量重做，8 款全开）

### 目录结构
```
src/games/            ← 游戏全部独立在此，与 components 解耦
  theme.css           ← 共享视觉层（.gx-* 工具类 + --gx-* 变量），main.js 已全局引入
  MatchGame.vue       落樱消消乐   6×6 三消
  DressGame.vue       云裳换装记   SVG 仕女换装 + 视差
  MakeupGame.vue      胭脂妆匣     铜镜妆面
  PoetryGame.vue      飞花令       诗句对答
  SeekGame.vue        宫墙寻芳     找物解谜
  TeaGame.vue         茶寮物语     2048 式合成
  RhythmGame.vue      弦歌行       四轨节奏（Web Audio 合成）
  WeaveGame.vue       锦绣织坊     拼图 + 图鉴
  WeavePattern.vue    └ 纹样子组件（团花/缠枝/云纹）
src/components/MyGames.vue   ← 外壳：列表 / 详情 / 开玩 三段状态机
```

### MyGames.vue 状态机（无 vue-router）
- `view: 'list' | 'detail' | 'play'`，`current` 存当前游戏对象。
- `GAMES` 数组（8 项，字段 `id/name/mark/cat/tags/desc/color/rule`，`mark` 是卡片上的单字徽记，`rule` 给详情页做玩法说明）；`PLAY` 映射 `id → 组件`。**GAMES 里 8 项与 PLAY 里 8 个组件一一对应，全部可玩，没有 `playable` 开关也没有「敬请期待」占位**。
- 播放态用 `<component :is="PLAY[current.id]" @exit="backToDetail" />`，**游戏组件统一 emit `exit`** 才能退回详情页。
- 列表区：搜索框（名称/描述/标签）+ 品类标签页（全部/消除/换装/解谜/经营/休闲）+ 横向轮播 `SLIDES`。
- 卡片用全局指令 `v-tilt`（main.js 注册）做 3D 倾斜；页面用 `<Teleport to="body">` 全屏覆盖，避免被首页布局挤压。
- 背景是纯 CSS/SVG 合成：月轮、雾霭、极光、花枝、剪影、灯笼、落瓣、浮尘 —— 分层 `position:absolute`，不要再套图片。

### theme.css 约定（改主题只动这里）
- `--gx-*` 变量：底色/描金/宣纸/墨色/胭脂/黛青等；`.gx-game` 根容器、`.gx-head` 标题栏（含 `.gx-medallion` 圆形徽章）、`.gx-hud` 数据条、`.gx-stage` 舞台、`.gx-btn` 描金按钮、`.gx-chip` 小标签、`.gx-result` 结算层、`.gx-seal` 印章。
- 动画：`gx-enter` / `gx-breathe` / `gx-shimmer` / `gx-pop`。
- 游戏组件内**只写布局与专属动效**，配色优先用变量，保证 8 款观感一致。

### 各游戏实现要点 / 坑
- **MatchGame**：6×6、5 花材、20 步、目标 1200。棋盘用 CSS 3D 瓦片 + `tile-shine` 高光；三消判定后掉落要按列补位，别整体重排。
- **DressGame**：整幅 SVG 仕女（`DRESS 5 / HAIR 3 / SHAWL 4(含无) / PIN 3`），鼠标 `rotateX/rotateY` 视差。衣柜存 `guiniang_looks`；**背景必须是深色渐变**（早期白底与主题冲突）。
- **MakeupGame**：铜镜 SVG，`BROWS 4 / SHADOW 5 / BLUSH 3 / LIPS 4 / HUADIAN 5`。`viewBox="92 44 136 136"` 是多次调过的取景框，改发型/脸型务必同步检查会不会切掉发髻。定妆评分按「胭脂/花钿/眼影是否上妆」计数映射 `七分/八分/九分/十分`，**不要再按数组下标硬算**（旧版会得到 undefined）。
- **PoetryGame**：4 令字各 12 句，对手「社长」从剩余诗句里随机接；3 条命 + 每回合 30 秒；诗句区是滚动长卷。
- **SeekGame**：宫墙夜景 SVG（砖缝、屋檐、暗角），5 个热点坐标在**画面上半部**；「点灯」给一次提示；命中出涟漪。
- **TeaGame**：4×4 合成，10 级 `茶芽 → … → 贡茶`。**`WIN_LEVEL = 10`**（等于数组长度，早期写成 9 导致永远不判胜）。瓦片必须放在 `.tea-tiles` 网格里，**不要用绝对定位 + left/top 百分比**（会错位）。支持 WASD / 方向键 / 触屏 / 屏幕方向键。
- **RhythmGame**：四轨（宫商角徵，键位 D F J K），`requestAnimationFrame` 落珠 + Web Audio 现场合成拨弦音（不加载音频文件）；判定 perfect/good/miss 与连击。舞台 `.gr-stage::before` 提供呼吸光晕，琴弦要够亮。
- **WeaveGame / WeavePattern**：3×3 / 4×4 交换式拼图，三套纹样；`WeavePattern` 里花朵坐标**在 `<script setup>` 里预计算成数组**，模板内不要写 `Math.random()`/函数调用（SSR 与重渲染都会不一致）。

### 存档 key（全部 localStorage，纯前端，不走后端）
| key | 归属 | 内容 |
|-----|------|------|
| `guiniang_looks` | 云裳换装记 | 保存的衣柜造型列表 |
| `guiniang_tea_best` | 茶寮物语 | 历史最高茶级 |
| `guiniang_rhythm_best` | 弦歌行 | 历史最高连击 |
| `guiniang_weave_atlas` | 锦绣织坊 | 已收录纹样图鉴 |

### 验证方式（本环境没有浏览器自动化 skill 时的做法）
1. SSR 冒烟：`vite.createServer({ root: '<项目绝对路径>' })` + `vue/server-renderer` 的 `renderToString` 逐个渲染 8 个组件，检查 html 长度非空。**`root` 必须给绝对路径**，给 `process.cwd()` 会全部报 "Failed to load url /src/games/X.vue"。
2. 真实交互：用 Chrome `--headless --remote-debugging-port` + CDP WebSocket 注入 Runtime.evaluate 点按钮、读状态（点卡片数、选中数、moves、已找到数等），比截图更可靠。
3. 构建：`node node_modules/vite/bin/vite.js build <项目目录>`（见上文 coreutils 缺失说明）。

### 新增一款游戏的步骤
1. 在 `src/games/` 建 `XxxGame.vue`，根节点套 `.gx-game`，顶部用 `.gx-head`，结束时 `emit('exit')`。
2. 在 `MyGames.vue` 的 `GAMES` 加一项（`id` 唯一、字段齐全），在 `PLAY` 里登记组件。
3. 品类若为新类，需同步 `CATS` 标签数组。
4. 有存档就沿用 `guiniang_` 前缀命名。

## 姻缘阁「我的主页」入口（2026-11-12 新增）

姻缘阁（`springBootTest/src/main/resources/static/dating.html`，端口 8081）新增了作品集 / 点赞 / 私信 / 联系方式，
但它是**后端静态页，与 Vue 站点（5173）不同源**，读不到主站的 `localStorage`。所以登录态靠 URL 参数传递。

### `components/HomePage.vue` 改动点
- 新增 `currentAuth()`：从 `localStorage` / `sessionStorage` 读 `guiniang_user`，拿到 `{ id, token, nickname, avatar }`。
  **token 字段名必须和登录页写入的一致**，改登录逻辑时记得同步这里。
- `openPartnerSite()`：打开姻缘阁时在 URL 上追加 `token/uid/name/avatar`，页脚 `from=home` 保留。
- 新增 `openMyPartnerProfile()`：追加 `viewer=<我的id>`，落地后 `dating.html` 会自动打开「我的主页」。
- 导航栏新增按钮 **「🎬 我的主页」**（`v-if="partnerSite.enabled && props.user && props.user.id"`），
  只有登录态下才出现，点击直达自己的作品集页。

### 传参约定（dating.html 侧 `absorbAuthFromUrl()`）
```
?from=home&viewer=<uid>&token=<jwt>&uid=<uid>&name=<昵称>&avatar=<头像URL>
```
- `dating.html` 收到后写入自己域下的 `localStorage['dating_auth_v1']`，
  再用 `history.replaceState` 把 `token/uid/name/avatar` 从地址栏抹掉（避免 token 出现在截图/分享链接里）。
- 昵称和头像可能含中文/特殊字符，拼 URL 时必须 `encodeURIComponent`。
- token 24 小时过期；过期后姻缘阁会提示「主站登录态已过期」，点「去登录」跳回 5173 登录页。
- 跨源只能靠 URL 传，**不要**试图用 cookie（8081 与 5173 不同端口，SameSite/域名都不对）。
