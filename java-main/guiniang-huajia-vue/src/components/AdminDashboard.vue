<script setup>
import { ref, computed, onMounted } from 'vue'

// 后台主界面：条目管理列表。
// 数据来自 /api/manage/items（后端接口就绪后自动展示真实数据）；
// 接口未实现/后端未启动时给出明确提示，不伪造数据。
const props = defineProps({
  admin: { type: Object, default: null }
})
const emit = defineEmits(['logout', 'back'])

const API_BASE = 'http://127.0.0.1:8081'

const adminName = computed(() =>
  (props.admin && (props.admin.name || props.admin.account)) || '判官'
)
const isDemo = computed(() => !!(props.admin && props.admin.demo))

// ---------- 列表状态 ----------
const keyword = ref('')
const categoryId = ref('')
const loading = ref(false)
const loaded = ref(false)     // 至少成功拉取过一次
const items = ref([])
const notice = ref('')
const backendReady = ref(true)

const total = computed(() => items.value.length)
const onlineCount = computed(() => items.value.filter(i => i.status === 1).length)
const offlineCount = computed(() => total.value - onlineCount.value)

onMounted(fetchItems)

async function fetchItems() {
  loading.value = true
  notice.value = ''
  try {
    const params = new URLSearchParams()
    if (keyword.value.trim()) params.set('keyword', keyword.value.trim())
    if (categoryId.value) params.set('categoryId', String(categoryId.value))
    const res = await fetch(`${API_BASE}/api/manage/items?${params.toString()}`, {
      headers: { 'Content-Type': 'application/json' }
    })
    if (res.ok) {
      const json = await res.json()
      if (json.code === 200) {
        items.value = Array.isArray(json.data) ? json.data : []
        loaded.value = true
        backendReady.value = true
        if (!items.value.length) notice.value = json.message || '生死簿空空如也，尚无条目。'
        return
      }
      notice.value = json.message || '接口返回异常'
    } else if (res.status === 404) {
      backendReady.value = false
      notice.value = '后台管理接口（/api/manage/items）尚未实现，列表暂不可用。待后端补齐后此处将自动展示真实数据。'
    } else {
      notice.value = '冥府信道受阻（HTTP ' + res.status + '）'
    }
  } catch (e) {
    backendReady.value = false
    notice.value = '冥府信道未通 · 后端未启动，无法载入条目列表。'
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  keyword.value = ''
  categoryId.value = ''
  fetchItems()
}

// 点击涟漪（微微发光的科技感反馈）
function spawnRipple(e) {
  const el = e.currentTarget
  if (!el) return
  const rect = el.getBoundingClientRect()
  const d = Math.max(rect.width, rect.height) * 1.15
  const span = document.createElement('span')
  span.className = 'tech-ripple'
  span.style.width = span.style.height = d + 'px'
  span.style.left = (e.clientX - rect.left - d / 2) + 'px'
  span.style.top = (e.clientY - rect.top - d / 2) + 'px'
  el.appendChild(span)
  setTimeout(() => span.remove(), 680)
}

function statusText(s) { return s === 1 ? '上线' : '下线' }
</script>

<template>
  <div class="dash">
    <!-- ============ 顶栏 ============ -->
    <header class="topbar">
      <div class="brand">
        <span class="brand-ico">🛡️</span>
        <span class="brand-name">判官台</span>
        <span class="brand-sub">后台管理</span>
      </div>

      <div class="top-actions">
        <button class="ghost" type="button" @click="emit('back')" @pointerdown="spawnRipple">← 返回首页</button>
        <span class="admin-chip" :title="isDemo ? '演示身份 · 未真实鉴权' : '已登录管理员'">
          <span class="admin-avatar">{{ isDemo ? '🎭' : '🧿' }}</span>
          <span class="admin-name">{{ adminName }}<em v-if="isDemo">演示</em></span>
        </span>
        <button class="logout" type="button" @click="emit('logout')" @pointerdown="spawnRipple">退 出</button>
      </div>
    </header>

    <!-- ============ 概览 ============ -->
    <section class="stats">
      <div v-tilt="{ strength: 7 }" class="stat violet">
        <span class="stat-num">{{ total }}</span>
        <span class="stat-label">条目总数</span>
      </div>
      <div v-tilt="{ strength: 7 }" class="stat pink">
        <span class="stat-num">{{ backendReady ? onlineCount : '—' }}</span>
        <span class="stat-label">上线中</span>
      </div>
      <div v-tilt="{ strength: 7 }" class="stat gold">
        <span class="stat-num">{{ backendReady ? offlineCount : '—' }}</span>
        <span class="stat-label">已下线</span>
      </div>
      <div v-tilt="{ strength: 7 }" class="stat" :class="backendReady ? 'ok' : 'bad'">
        <span class="stat-num">{{ backendReady ? '通' : '断' }}</span>
        <span class="stat-label">冥府信道</span>
      </div>
    </section>

    <!-- ============ 筛选 ============ -->
    <section class="filter">
      <form class="filter-form" @submit.prevent="fetchItems">
        <input v-model="keyword" type="text" maxlength="40" placeholder="按标题 / 标签搜寻" />
        <input v-model="categoryId" type="number" min="1" placeholder="分类ID（可选）" class="cid" />
        <button class="btn-main" type="submit" :disabled="loading" @pointerdown="spawnRipple">
          {{ loading ? '翻检中…' : '查 阅' }}
        </button>
        <button class="btn-ghost" type="button" @click="resetFilter" @pointerdown="spawnRipple">重 置</button>
      </form>
    </section>

    <!-- ============ 列表 ============ -->
    <section class="list-wrap">
      <p v-if="notice" class="notice" :class="{ warn: !backendReady }">{{ notice }}</p>

      <div v-if="items.length" class="table" role="table">
        <div class="tr head" role="row">
          <span class="th emoji">图</span>
          <span class="th">标题</span>
          <span class="th desc">描述</span>
          <span class="th">分类</span>
          <span class="th">标签</span>
          <span class="th">状态</span>
        </div>
        <div v-for="it in items" :key="it.id" class="tr" role="row">
          <span class="td emoji">{{ it.emoji || '💮' }}</span>
          <span class="td title-cell">{{ it.title }}</span>
          <span class="td desc">{{ it.desc }}</span>
          <span class="td">{{ it.categoryName || '—' }}</span>
          <span class="td">{{ it.tag || '—' }}</span>
          <span class="td">
            <span class="status" :class="{ on: it.status === 1 }">{{ statusText(it.status) }}</span>
          </span>
        </div>
      </div>

      <p v-else-if="!loading && loaded && !items.length && !backendReady" class="empty">
        待后端接口就绪后，此处将呈现条目管理列表。
      </p>
    </section>
  </div>
</template>

<style scoped>
.dash{
  position:relative; z-index:5;
  width:100%; min-height:100vh;
  display:flex; flex-direction:column;
}

/* ---------- 顶栏 ---------- */
.topbar{
  position:sticky; top:0; z-index:20;
  display:flex; align-items:center; justify-content:space-between; gap:16px;
  padding:12px clamp(16px,4vw,40px);
  background:linear-gradient(180deg, rgba(43,22,74,.9), rgba(43,22,74,.55));
  border-bottom:1px solid rgba(155,107,214,.45);
  backdrop-filter:blur(14px) saturate(140%); -webkit-backdrop-filter:blur(14px) saturate(140%);
  box-shadow:0 8px 28px rgba(20,8,40,.42), inset 0 1px 0 rgba(255,255,255,.10);
}
.brand{ display:flex; align-items:baseline; gap:8px; }
.brand-ico{ font-size:18px; }
.brand-name{
  font-family:var(--font-serif); font-size:20px; letter-spacing:2px; color:var(--ivory);
  text-shadow:0 0 16px rgba(155,107,214,.6);
}
.brand-sub{ font-size:11px; letter-spacing:3px; color:var(--gold); opacity:.8; }

.top-actions{ display:flex; align-items:center; gap:10px; }
.ghost{
  border:1px solid rgba(255,179,200,.5); background:transparent; color:var(--rose);
  font-size:13px; letter-spacing:2px; padding:8px 16px; border-radius:999px; cursor:pointer; transition:.2s;
}
.ghost:hover{ color:#fff; background:rgba(232,74,120,.22); }
.admin-chip{
  display:flex; align-items:center; gap:8px;
  padding:5px 12px 5px 6px; border-radius:999px;
  background:rgba(255,247,251,.12); border:1px solid rgba(155,107,214,.55);
}
.admin-avatar{
  width:28px; height:28px; display:grid; place-items:center; border-radius:50%;
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0); font-size:15px;
}
.admin-name{ font-size:13px; color:var(--ivory); display:flex; align-items:center; gap:6px; }
.admin-name em{
  font-style:normal; font-size:10px; letter-spacing:1px; color:#2b164a;
  padding:1px 8px; border-radius:999px;
  background:linear-gradient(180deg,#F4D58B,#E7B85A);
}
.logout{
  border:1px solid rgba(255,179,200,.5); background:transparent; color:var(--rose);
  font-size:13px; letter-spacing:2px; padding:8px 16px; border-radius:999px; cursor:pointer; transition:.2s;
}
.logout:hover{ color:#fff; background:rgba(232,74,120,.25); }

/* ---------- 概览统计 ---------- */
.stats{
  width:min(980px,92vw); margin:26px auto 0;
  display:grid; gap:14px;
  grid-template-columns:repeat(auto-fit, minmax(160px,1fr));
}
.stat{
  padding:18px 20px; border-radius:var(--radius-lg); text-align:center;
  background:linear-gradient(180deg, rgba(255,247,251,.13), rgba(255,247,251,.07));
  border:1px solid var(--hairline);
  box-shadow:var(--shadow-1), inset 0 1px 0 rgba(255,255,255,.14);
}
.stat-num{
  display:block; font-family:var(--font-serif); font-size:28px; font-weight:700;
  background:linear-gradient(120deg,#FFE6F0,#F4D58B);
  -webkit-background-clip:text; background-clip:text; color:transparent;
}
.stat-label{ display:block; margin-top:4px; font-size:12px; letter-spacing:2px; color:var(--rose); opacity:.85; }
.stat.violet{ border-color:rgba(155,107,214,.55); box-shadow:var(--shadow-1), 0 0 20px rgba(155,107,214,.18); }
.stat.ok .stat-num{ background:linear-gradient(120deg,#C9F7D8,#7BE3A2); -webkit-background-clip:text; background-clip:text; }
.stat.bad .stat-num{ background:linear-gradient(120deg,#FFC9D9,#FF7B9E); -webkit-background-clip:text; background-clip:text; }

/* ---------- 筛选 ---------- */
.filter{ width:min(980px,92vw); margin:20px auto 0; }
.filter-form{ display:flex; gap:10px; flex-wrap:wrap; }
.filter-form input{
  flex:1 1 220px; min-width:0;
  padding:11px 14px; border-radius:999px; outline:none;
  border:1px solid rgba(155,107,214,.45);
  background:rgba(23,12,43,.45); color:var(--ivory);
  font-size:13.5px; font-family:inherit; transition:.2s;
}
.filter-form input::placeholder{ color:rgba(255,233,243,.4); }
.filter-form input:focus{ border-color:#B98CF0; box-shadow:var(--focus-ring-violet); }
.filter-form .cid{ flex:0 1 160px; }
.btn-main{
  border:none; cursor:pointer; padding:11px 24px; border-radius:999px;
  font-family:var(--font-serif); font-size:14px; font-weight:700; letter-spacing:3px; color:#fff;
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  box-shadow:0 5px 0 #4A2680, 0 10px 18px rgba(106,58,160,.45);
  transition:transform .14s, filter .2s;
}
.btn-main:hover{ filter:brightness(1.08); transform:translateY(-2px); }
.btn-main:disabled{ cursor:default; filter:grayscale(.25); }
.btn-ghost{
  border:1px solid rgba(244,213,139,.5); background:transparent; color:var(--gold);
  font-size:13px; letter-spacing:2px; padding:11px 20px; border-radius:999px; cursor:pointer; transition:.2s;
}
.btn-ghost:hover{ color:#fff; background:rgba(244,213,139,.14); }

/* ---------- 列表 ---------- */
.list-wrap{ width:min(980px,92vw); margin:18px auto 60px; flex:1; }
.notice{
  margin:14px 0; padding:12px 16px; border-radius:var(--radius-md);
  font-size:13px; line-height:1.7; letter-spacing:.5px;
  color:var(--gold); background:rgba(255,247,251,.08);
  border:1px dashed rgba(244,213,139,.5);
}
.notice.warn{ color:#FFC9D9; border-color:rgba(255,111,156,.5); background:rgba(232,74,120,.1); }
.empty{ text-align:center; color:var(--rose); opacity:.75; font-size:13.5px; letter-spacing:1px; padding:30px 0; }

.table{
  border-radius:var(--radius-lg); overflow:hidden;
  background:linear-gradient(180deg, rgba(255,247,251,.12), rgba(255,247,251,.06));
  border:1px solid rgba(155,107,214,.4);
  box-shadow:var(--shadow-1);
}
.tr{ display:grid; grid-template-columns:44px 1.2fr 2fr .8fr .8fr 70px; gap:10px; align-items:center;
     padding:11px 16px; border-bottom:1px solid rgba(155,107,214,.18); }
.tr:last-child{ border-bottom:none; }
.tr.head{ background:rgba(106,58,160,.28); }
.th{ font-size:12px; letter-spacing:2px; color:var(--gold); text-align:left; }
.td{ font-size:13px; color:var(--ivory); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.td.emoji{ font-size:18px; text-align:center; }
.title-cell{ font-family:var(--font-serif); }
.desc{ opacity:.8; }
.status{
  display:inline-block; font-size:11px; letter-spacing:1px; padding:3px 12px; border-radius:999px;
  color:#FFC9D9; background:rgba(232,74,120,.18); border:1px solid rgba(255,111,156,.4);
}
.status.on{ color:#C9F7D8; background:rgba(60,180,110,.16); border-color:rgba(123,227,162,.45); }

@media (max-width:720px){
  .tr{ grid-template-columns:36px 1.4fr 1.6fr 60px; }
  .tr .desc, .tr .th.desc{ display:none; }
  .topbar{ flex-wrap:wrap; }
}

/* ---------- 微微发光的科技感交互 ---------- */
.btn-main,.btn-ghost,.ghost,.logout{ position:relative; overflow:hidden; }
.tech-ripple{
  position:absolute; border-radius:50%; pointer-events:none; z-index:1;
  background:radial-gradient(circle, rgba(185,140,240,.55) 0%, rgba(185,140,240,.2) 42%, transparent 66%);
  transform:scale(0); animation:techRipple .62s ease-out forwards;
}
@keyframes techRipple{ to{ transform:scale(1); opacity:0; } }

/* 列表行：悬停辉光 + 左侧霓虹描边 */
.tr{ position:relative; transition:background .25s var(--ease-out), box-shadow .25s var(--ease-out); }
.tr:not(.head):hover{
  background:rgba(155,107,214,.14);
  box-shadow:inset 0 0 26px rgba(155,107,214,.22), inset 2px 0 0 #B98CF0;
}

/* 状态标签：霓虹微光 */
.status{ box-shadow:0 0 10px rgba(255,111,156,.35); }
.status.on{ box-shadow:0 0 12px rgba(123,227,162,.42); }

/* 概览卡：悬停辉光描边 */
.stat{ transition:box-shadow .3s var(--ease-out), border-color .3s, transform .3s var(--ease-out); }
.stat:hover{ border-color:rgba(185,140,240,.7); box-shadow:var(--shadow-1), 0 0 26px rgba(155,107,214,.4); }

/* 按钮：悬停微光 */
.btn-main{ box-shadow:0 5px 0 #4A2680, 0 10px 18px rgba(106,58,160,.45), 0 0 18px rgba(155,107,214,.4); }
.ghost:hover,.logout:hover{ box-shadow:0 0 16px rgba(232,74,120,.35); }
.btn-ghost:hover{ box-shadow:0 0 16px rgba(244,213,139,.35); }
</style>
