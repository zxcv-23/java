<script setup>
import { ref, computed, onMounted } from 'vue'

// 备忘录：前台登录用户私有的便签板。
// 鉴权沿用登录时下发的 JWT（guiniang_user.token），接口挂在 /api/profile/memos 下，
// 由后端 JwtInterceptor 解析用户身份并按 user_id 隔离，前端不传 userId。
const props = defineProps({
  user: { type: Object, default: null }
})
const emit = defineEmits(['back', 'needLogin'])

const API_BASE = 'http://192.168.68.22:8081'
const MEMO_API = API_BASE + '/api/profile/memos'
const token = computed(() => (props.user && props.user.token) || '')

const COLORS = [
  { key: 'violet', label: '紫藤', swatch: 'linear-gradient(135deg,#E7D9FA,#C6A6EE)' },
  { key: 'pink', label: '桃夭', swatch: 'linear-gradient(135deg,#FFD9E7,#FFAFCB)' },
  { key: 'gold', label: '鎏金', swatch: 'linear-gradient(135deg,#FFF0C9,#F4D58B)' },
  { key: 'mint', label: '青瓷', swatch: 'linear-gradient(135deg,#D9F3E8,#A8DCC8)' }
]
const TABS = [
  { key: 'all', label: '全部' },
  { key: 'todo', label: '待办' },
  { key: 'done', label: '已完成' }
]

const memos = ref([])
const loading = ref(false)
const error = ref('')
const filter = ref('all')
const keyword = ref('')
const saving = ref(false)
const busyId = ref(null)

const showEditor = ref(false)
const editingId = ref(null)
const form = ref({ title: '', content: '', color: 'violet' })

const toast = ref('')
let toastTimer = null
function flash(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 2400)
}

const counts = computed(() => ({
  all: memos.value.length,
  todo: memos.value.filter(m => m.done !== 1).length,
  done: memos.value.filter(m => m.done === 1).length
}))

const visible = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return memos.value.filter(m => {
    const okFilter = filter.value === 'all'
      || (filter.value === 'todo' ? m.done !== 1 : m.done === 1)
    const text = ((m.title || '') + ' ' + (m.content || '')).toLowerCase()
    return okFilter && (!kw || text.includes(kw))
  })
})

// 统一请求：自动带 JWT，401 时抛给 App.vue 重新登录，非 200 抛后端提示
async function request(method, path, body) {
  const res = await fetch(MEMO_API + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token.value
    },
    ...(body === undefined ? {} : { body: JSON.stringify(body) })
  })
  if (res.status === 401) {
    emit('needLogin')
    throw new Error('登录已失效，请重新缔结婚约')
  }
  let json
  try { json = await res.json() } catch { throw new Error('后端响应异常') }
  if (json.code !== 200) throw new Error(json.message || '操作失败')
  return json.data
}

async function load() {
  if (!token.value) {
    error.value = '还未缔结婚约（未登录），无法查看备忘录。'
    return
  }
  loading.value = true
  error.value = ''
  try {
    memos.value = (await request('GET', '')) || []
  } catch (e) {
    error.value = /Failed to fetch|NetworkError|load failed/i.test(e.message)
      ? '冥府信道未通 · 请确认后端已启动，且 memo 表已创建。'
      : e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)

function openCreate() {
  editingId.value = null
  form.value = { title: '', content: '', color: 'violet' }
  showEditor.value = true
}
function openEdit(m) {
  editingId.value = m.id
  form.value = { title: m.title || '', content: m.content || '', color: m.color || 'violet' }
  showEditor.value = true
}

async function save() {
  const title = form.value.title.trim()
  if (!title) { flash('给这条备忘起个标题吧'); return }
  saving.value = true
  try {
    const payload = { title, content: form.value.content, color: form.value.color }
    if (editingId.value == null) {
      await request('POST', '', payload)
      flash('备忘已记下 ♥')
    } else {
      await request('PUT', '/' + editingId.value, payload)
      flash('备忘已更新')
    }
    showEditor.value = false
    await load()
  } catch (e) {
    flash(e.message)
  } finally {
    saving.value = false
  }
}

async function toggleDone(m) {
  busyId.value = m.id
  const wasDone = m.done === 1
  try {
    await request('PUT', '/' + m.id, { done: wasDone ? 0 : 1 })
    await load()
    flash(wasDone ? '已重新列为待办' : '已完成，真棒 ♥')
  } catch (e) { flash(e.message) } finally { busyId.value = null }
}

async function togglePin(m) {
  busyId.value = m.id
  const wasPinned = m.pinned === 1
  try {
    await request('PUT', '/' + m.id, { pinned: wasPinned ? 0 : 1 })
    await load()
    flash(wasPinned ? '已取消置顶' : '已置顶 📌')
  } catch (e) { flash(e.message) } finally { busyId.value = null }
}

async function remove(m) {
  if (!window.confirm(`确定撕下「${m.title}」这张备忘吗？`)) return
  busyId.value = m.id
  try {
    await request('DELETE', '/' + m.id)
    await load()
    flash('备忘已撕下')
  } catch (e) { flash(e.message) } finally { busyId.value = null }
}

// 兼容 LocalDateTime 的两种 Jackson 序列化形态：ISO 字符串或 [y,m,d,h,mi] 数组
function formatTime(v) {
  if (!v) return ''
  if (Array.isArray(v)) {
    const [y, mo, d, h, mi] = v
    const p = n => String(n ?? '').padStart(2, '0')
    return `${y}-${p(mo)}-${p(d)} ${p(h)}:${p(mi)}`
  }
  return String(v).replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <div class="memo-board">
    <!-- ============ 顶部 ============ -->
    <header class="memo-head">
      <button class="back" type="button" @click="emit('back')">← 返回花嫁道</button>
      <div class="memo-brand">
        <span class="memo-emoji">📜</span>
        <div>
          <h1>备 忘 录</h1>
          <p>信使奔走阴阳，递送相思。—— 今改作你的随身手札</p>
        </div>
      </div>
      <div class="memo-stats">
        <span>共 <b>{{ counts.all }}</b> 条</span>
        <span>待办 <b>{{ counts.todo }}</b></span>
        <span>已完成 <b>{{ counts.done }}</b></span>
      </div>
    </header>

    <!-- ============ 工具栏 ============ -->
    <div class="memo-toolbar">
      <div class="tabs">
        <button
          v-for="t in TABS" :key="t.key" type="button"
          :class="{ on: filter === t.key }" @click="filter = t.key"
        >{{ t.label }} <em>{{ counts[t.key] }}</em></button>
      </div>
      <input
        v-model="keyword" class="memo-search" type="text" maxlength="60"
        placeholder="搜寻标题或正文…" aria-label="搜索备忘"
      />
      <button class="add-btn" type="button" @click="openCreate">＋ 写一条</button>
    </div>

    <!-- ============ 内容区 ============ -->
    <div v-if="loading" class="memo-state">月老翻查手札中…</div>
    <div v-else-if="error" class="memo-state error">{{ error }}</div>
    <div v-else-if="!visible.length" class="memo-empty">
      <div class="empty-ico">🪶</div>
      <p>{{ keyword ? '没有找到相符的备忘' : '还没有备忘，点「写一条」记下心事吧' }}</p>
    </div>

    <div v-else class="memo-grid">
      <article
        v-for="m in visible" :key="m.id"
        class="memo-paper"
        :class="['paper-' + (m.color || 'violet'), { 'is-done': m.done === 1, 'is-pinned': m.pinned === 1 }]"
      >
        <span v-if="m.pinned === 1" class="pin-mark" title="已置顶">📌</span>
        <div class="memo-line">
          <button
            class="check" type="button" :class="{ on: m.done === 1 }"
            :disabled="busyId === m.id"
            :aria-label="m.done === 1 ? '标记为待办' : '标记为完成'"
            @click="toggleDone(m)"
          >{{ m.done === 1 ? '✓' : '' }}</button>
          <h3 class="memo-title">{{ m.title }}</h3>
        </div>
        <p v-if="m.content" class="memo-content">{{ m.content }}</p>
        <p v-else class="memo-content placeholder">（未写下正文）</p>
        <div class="memo-foot">
          <span class="memo-time">{{ formatTime(m.updateTime || m.createTime) }}</span>
          <div class="memo-ops">
            <button type="button" :disabled="busyId === m.id" :title="m.pinned === 1 ? '取消置顶' : '置顶'" @click="togglePin(m)">{{ m.pinned === 1 ? '📌' : '📍' }}</button>
            <button type="button" title="编辑" @click="openEdit(m)">✎</button>
            <button type="button" :disabled="busyId === m.id" title="删除" @click="remove(m)">✕</button>
          </div>
        </div>
      </article>
    </div>

    <!-- ============ 编辑 / 新增弹层 ============ -->
    <transition name="fade">
      <div v-if="showEditor" class="editor-mask" @click.self="showEditor = false">
        <div class="editor" role="dialog" aria-modal="true" aria-label="编辑备忘">
          <h3>{{ editingId == null ? '写一条备忘' : '编辑备忘' }}</h3>
          <label for="memo-title">标题</label>
          <input id="memo-title" v-model="form.title" class="editor-input" type="text" maxlength="120" placeholder="给这张便签起个名字…" />
          <label for="memo-content">正文</label>
          <textarea id="memo-content" v-model="form.content" class="editor-area" rows="6" maxlength="5000" placeholder="想记下的事，都写在这里…"></textarea>
          <label>便签配色</label>
          <div class="colors">
            <button
              v-for="c in COLORS" :key="c.key" type="button"
              :class="{ on: form.color === c.key }" :style="{ background: c.swatch }"
              @click="form.color = c.key"
            ><span>{{ c.label }}</span></button>
          </div>
          <div class="editor-actions">
            <button class="ghost" type="button" @click="showEditor = false">取 消</button>
            <button class="primary" type="button" :disabled="saving" @click="save">{{ saving ? '正在记下…' : '记 下' }}</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="fade"><div v-if="toast" class="memo-toast">{{ toast }}</div></transition>
  </div>
</template>

<style scoped>
.memo-board{
  width:min(1080px, 100%); max-height:calc(100vh - 96px); overflow-y:auto;
  padding:26px 30px 34px; border-radius:var(--radius-xl);
  color:var(--ink); font-family:var(--font-sans);
  background:linear-gradient(180deg, rgba(255,247,251,.16), rgba(255,247,251,.07));
  border:1px solid var(--hairline);
  backdrop-filter:blur(18px) saturate(150%); -webkit-backdrop-filter:blur(18px) saturate(150%);
  box-shadow:var(--shadow-3), inset 0 1px 0 rgba(255,255,255,.2);
}
.memo-board::-webkit-scrollbar{ width:8px; }
.memo-board::-webkit-scrollbar-thumb{ background:rgba(244,213,139,.35); border-radius:8px; }

/* ---------- 顶部 ---------- */
.memo-head{ display:flex; align-items:center; gap:20px; flex-wrap:wrap; margin-bottom:22px; }
.back{
  border:1px solid var(--hairline); background:rgba(255,247,251,.10); color:#FFE6F0;
  font-family:inherit; font-size:12.5px; letter-spacing:1px; padding:9px 16px;
  border-radius:999px; cursor:pointer; transition:.22s var(--ease-out); white-space:nowrap;
}
.back:hover{ background:rgba(255,111,156,.22); color:#fff; transform:translateX(-3px); }
.memo-brand{ display:flex; align-items:center; gap:14px; flex:1 1 320px; }
.memo-emoji{ font-size:36px; filter:drop-shadow(0 6px 14px rgba(244,213,139,.4)); }
.memo-brand h1{
  margin:0; font-size:26px; letter-spacing:6px; font-family:var(--font-serif);
  color:#FFF7FB; text-shadow:0 3px 18px rgba(255,111,156,.55);
}
.memo-brand p{ margin:5px 0 0; font-size:12px; letter-spacing:1px; color:rgba(255,230,240,.72); }
.memo-stats{ display:flex; gap:8px; flex-wrap:wrap; }
.memo-stats span{
  font-size:12px; letter-spacing:1px; color:#FFF0F6; padding:7px 13px; border-radius:999px;
  background:linear-gradient(180deg, rgba(155,107,214,.30), rgba(106,58,160,.28));
  border:1px solid rgba(255,179,200,.28);
}
.memo-stats b{ color:var(--gold); font-size:14px; }

/* ---------- 工具栏 ---------- */
.memo-toolbar{ display:flex; gap:12px; align-items:center; flex-wrap:wrap; margin-bottom:22px; }
.tabs{ display:flex; gap:6px; padding:5px; border-radius:999px; background:rgba(255,247,251,.10); border:1px solid var(--hairline); }
.tabs button{
  border:none; background:transparent; color:rgba(255,230,240,.8); font-family:inherit;
  font-size:12.5px; letter-spacing:1px; padding:8px 16px; border-radius:999px; cursor:pointer; transition:.22s;
}
.tabs button em{ font-style:normal; opacity:.7; margin-left:3px; }
.tabs button.on{
  color:#fff; background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  box-shadow:0 5px 14px rgba(106,58,160,.45), inset 0 1px 0 rgba(255,255,255,.25);
}
.memo-search{
  flex:1 1 200px; min-width:160px; font-family:inherit; font-size:13px; color:#fff;
  padding:10px 16px; border-radius:999px; outline:none;
  border:1px solid var(--hairline); background:rgba(255,247,251,.08);
}
.memo-search::placeholder{ color:rgba(255,230,240,.55); }
.memo-search:focus{ border-color:var(--gold); box-shadow:var(--focus-ring); }
.add-btn{
  border:none; font-family:inherit; font-size:13px; letter-spacing:2px; color:#fff;
  padding:11px 22px; border-radius:999px; cursor:pointer; white-space:nowrap;
  background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 8px 20px rgba(232,74,120,.45), inset 0 1px 0 rgba(255,255,255,.35);
  transition:.22s var(--ease-out);
}
.add-btn:hover{ transform:translateY(-2px); box-shadow:0 12px 26px rgba(232,74,120,.6); }

/* ---------- 状态 ---------- */
.memo-state{ padding:60px 10px; text-align:center; font-size:14px; letter-spacing:1px; color:rgba(255,230,240,.85); }
.memo-state.error{ color:#FFC4D6; }
.memo-empty{ padding:56px 10px; text-align:center; color:rgba(255,230,240,.8); }
.empty-ico{ font-size:44px; margin-bottom:12px; filter:drop-shadow(0 6px 16px rgba(244,213,139,.35)); }
.memo-empty p{ margin:0; font-size:13.5px; letter-spacing:1px; }

/* ---------- 便签 ---------- */
.memo-grid{ display:grid; grid-template-columns:repeat(auto-fill, minmax(240px, 1fr)); gap:20px; }
.memo-paper{
  --paper1:#FBF3FF; --paper2:#EFE0FA; --accent:#9B6BD6;
  position:relative; display:flex; flex-direction:column; min-height:196px;
  padding:22px 18px 14px; border-radius:12px; color:var(--ink);
  background:linear-gradient(160deg, var(--paper1), var(--paper2));
  border:1px solid rgba(255,255,255,.7);
  box-shadow:0 14px 30px rgba(13,4,32,.38), inset 0 1px 0 rgba(255,255,255,.9);
  transform:rotate(-.5deg); transition:transform .25s var(--ease-out), box-shadow .25s var(--ease-out), opacity .25s;
}
.memo-paper:nth-child(even){ transform:rotate(.6deg); }
.memo-paper:hover{ transform:translateY(-6px) rotate(0deg); box-shadow:0 22px 44px rgba(13,4,32,.5); z-index:2; }
.memo-paper::before{
  content:""; position:absolute; top:-9px; left:50%; width:60px; height:20px; transform:translateX(-50%) rotate(-3deg);
  background:rgba(255,255,255,.55); border:1px solid rgba(255,255,255,.7);
  box-shadow:0 2px 6px rgba(13,4,32,.16);
}
.paper-pink{ --paper1:#FFF5F8; --paper2:#FFE1EC; --accent:#E84A78; }
.paper-gold{ --paper1:#FFFAEC; --paper2:#FBEBC6; --accent:#C79A3B; }
.paper-mint{ --paper1:#F2FBF7; --paper2:#DDF2E8; --accent:#4FA98E; }
.memo-paper.is-pinned{ box-shadow:0 14px 30px rgba(13,4,32,.38), 0 0 0 2px rgba(244,213,139,.75), inset 0 1px 0 rgba(255,255,255,.9); }
.memo-paper.is-done{ opacity:.7; }
.memo-paper.is-done .memo-title{ text-decoration:line-through; color:var(--ink-soft); }
.pin-mark{ position:absolute; top:-14px; right:8px; font-size:20px; filter:drop-shadow(0 3px 5px rgba(13,4,32,.3)); }

.memo-line{ display:flex; align-items:flex-start; gap:10px; margin-bottom:10px; }
.check{
  flex:0 0 auto; width:20px; height:20px; margin-top:2px; border-radius:6px; cursor:pointer;
  border:2px solid var(--accent); background:transparent; color:#fff; font-size:12px; line-height:16px;
  font-family:inherit; transition:.2s;
}
.check.on{ background:var(--accent); border-color:var(--accent); }
.check:disabled{ opacity:.5; cursor:wait; }
.memo-title{
  margin:0; font-size:16px; line-height:1.5; letter-spacing:.5px; word-break:break-word;
  font-family:var(--font-serif); color:#3B2540;
}
.memo-content{
  flex:1; margin:0 0 14px; font-size:12.8px; line-height:1.85; letter-spacing:.3px;
  color:var(--ink-soft); white-space:pre-wrap; word-break:break-word;
  border-left:2px solid rgba(0,0,0,.06); padding-left:10px;
}
.memo-content.placeholder{ opacity:.55; font-style:italic; }
.memo-foot{
  display:flex; align-items:center; justify-content:space-between; gap:8px;
  padding-top:10px; border-top:1px dashed rgba(74,42,58,.22);
}
.memo-time{ font-size:11px; letter-spacing:.5px; color:rgba(74,42,58,.6); }
.memo-ops{ display:flex; gap:5px; }
.memo-ops button{
  width:26px; height:26px; border-radius:50%; cursor:pointer; font-size:12px; line-height:1;
  border:1px solid rgba(74,42,58,.16); background:rgba(255,255,255,.5); color:#5A3B52;
  transition:.2s; font-family:inherit;
}
.memo-ops button:hover:not(:disabled){ background:var(--accent); color:#fff; border-color:var(--accent); }
.memo-ops button:disabled{ opacity:.4; cursor:wait; }

/* ---------- 编辑弹层 ---------- */
.editor-mask{
  position:fixed; inset:0; z-index:60; display:grid; place-items:center; padding:20px;
  background:rgba(23,12,43,.62); backdrop-filter:blur(8px); -webkit-backdrop-filter:blur(8px);
}
.editor{
  width:min(480px, 100%); padding:28px; border-radius:var(--radius-lg);
  background:linear-gradient(180deg,#FFF9FC,#F7EDF5);
  border:1px solid rgba(244,213,139,.5); box-shadow:var(--shadow-3); color:var(--ink);
}
.editor h3{ margin:0 0 18px; font-family:var(--font-serif); font-size:20px; letter-spacing:3px; color:#4A2A3A; }
.editor label{ display:block; margin:14px 0 7px; font-size:12px; letter-spacing:1px; color:var(--ink-soft); }
.editor-input, .editor-area{
  width:100%; font-family:inherit; font-size:13.5px; color:var(--ink); outline:none;
  padding:11px 14px; border-radius:12px; border:1px solid rgba(155,107,214,.35); background:#fff;
  transition:.2s; resize:vertical;
}
.editor-input:focus, .editor-area:focus{ border-color:#9B6BD6; box-shadow:var(--focus-ring-violet); }
.colors{ display:flex; gap:10px; flex-wrap:wrap; }
.colors button{
  width:72px; height:44px; border-radius:10px; cursor:pointer; font-family:inherit;
  font-size:11.5px; letter-spacing:1px; color:#4A2A3A; border:2px solid transparent;
  box-shadow:0 4px 12px rgba(13,4,32,.18); transition:.2s;
}
.colors button span{ background:rgba(255,255,255,.55); padding:2px 8px; border-radius:999px; }
.colors button.on{ border-color:#6A3AA0; transform:translateY(-2px); box-shadow:0 8px 18px rgba(106,58,160,.35); }
.editor-actions{ display:flex; justify-content:flex-end; gap:12px; margin-top:24px; }
.editor-actions button{
  font-family:inherit; font-size:13px; letter-spacing:2px; padding:10px 24px;
  border-radius:999px; cursor:pointer; transition:.22s;
}
.editor-actions .ghost{ border:1px solid rgba(155,107,214,.45); color:#6A3AA0; background:transparent; }
.editor-actions .ghost:hover{ background:rgba(155,107,214,.12); }
.editor-actions .primary{
  border:none; color:#fff; background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  box-shadow:0 8px 18px rgba(106,58,160,.4);
}
.editor-actions .primary:disabled{ opacity:.55; cursor:wait; }

/* ---------- 提示 ---------- */
.memo-toast{
  position:fixed; left:50%; bottom:38px; transform:translateX(-50%); z-index:70;
  padding:12px 24px; border-radius:999px; font-size:13px; letter-spacing:1px; color:#fff;
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  box-shadow:0 12px 30px rgba(13,4,32,.5); white-space:nowrap;
}
.fade-enter-active, .fade-leave-active{ transition:opacity .22s ease; }
.fade-enter-from, .fade-leave-to{ opacity:0; }
</style>
