<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { adminFetch, clearAdmin, persistAdmin } from '../utils/adminApi'

// 后台主界面：条目管理 / 回收站 / 账号安全。
// 所有后台接口统一走 adminApi（自动携带 JWT，401 自动退回登录页）。
const props = defineProps({
  admin: { type: Object, default: null }
})
const emit = defineEmits(['logout', 'back'])

const API_BASE = 'http://127.0.0.1:8081'

// ---------- 外部联动站点（后台跳转入口）----------
// 兜底值与后端 application-dev.properties 的 partner.site.* 保持一致；
// 后端在线时由 fetchPartnerSite() 覆盖，换目标站只需改后端配置。
const partnerSite = ref({
  name: '帅哥美女相亲阁',
  url: 'http://127.0.0.1:5173/',
  enabled: true
})

async function fetchPartnerSite() {
  try {
    const res = await fetch(API_BASE + '/api/partner/site')
    if (!res.ok) return
    const json = await res.json()
    if (json && json.code === 200 && json.data && json.data.url) {
      partnerSite.value = {
        name: json.data.name || partnerSite.value.name,
        url: json.data.url,
        enabled: json.data.enabled !== false
      }
    }
  } catch (e) {
    // 后端未起：保留兜底地址，入口仍可跳转
  }
}

function partnerHref() {
  const raw = partnerSite.value.url
  if (!raw) return ''
  try {
    const u = new URL(raw, window.location.href)
    if (!u.searchParams.has('from')) u.searchParams.set('from', 'judge')
    return u.toString()
  } catch (e) {
    return raw
  }
}

function openPartnerSite() {
  const href = partnerHref()
  if (!href) { flash('联动站点尚未配置'); return }
  const win = window.open(href, '_blank', 'noopener,noreferrer')
  if (!win) {
    window.location.href = href   // 被浏览器拦截弹窗时降级为当前页跳转
  } else {
    flash(`已为你打开「${partnerSite.value.name}」♥`)
  }
}

const adminName = computed(() =>
  (props.admin && (props.admin.name || props.admin.account)) || '判官'
)

// ---------- 标签页 ----------
const tab = ref('items')
const tabs = [
  { key: 'items', label: '条目管理', icon: '📜' },
  { key: 'recycle', label: '回收站', icon: '🕯️' },
  { key: 'account', label: '账号安全', icon: '🔑' }
]
const isRecycle = computed(() => tab.value === 'recycle')

function switchTab(key) {
  if (tab.value === key) return
  tab.value = key
  page.value = 1
  notice.value = ''
  if (key !== 'account') fetchItems()
}

// ---------- 列表与分页 ----------
const keyword = ref('')
const categoryId = ref('')
const statusFilter = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)
const loading = ref(false)
const loaded = ref(false)
const items = ref([])
const notice = ref('')
const backendReady = ref(true)
const stats = ref({ total: 0, online: 0, offline: 0, deleted: 0, categories: 0 })

const pageStart = computed(() => total.value ? (page.value - 1) * pageSize.value + 1 : 0)
const pageEnd = computed(() => Math.min(page.value * pageSize.value, total.value))

function buildQuery() {
  const p = new URLSearchParams()
  if (keyword.value.trim()) p.set('keyword', keyword.value.trim())
  if (categoryId.value) p.set('categoryId', String(categoryId.value))
  if (statusFilter.value !== '') p.set('status', statusFilter.value)
  p.set('deleted', isRecycle.value ? 'true' : 'false')
  p.set('page', String(page.value))
  p.set('size', String(pageSize.value))
  return p.toString()
}

onMounted(() => {
  verifySession()
  fetchStats()
  fetchItems()
  fetchCategories()
  fetchPartnerSite()
  window.addEventListener('keydown', onEsc)
})
onUnmounted(() => window.removeEventListener('keydown', onEsc))

// 进入后台先自检身份：token 过期/被禁用时 adminFetch 会广播 401
async function verifySession() {
  try { await adminFetch('/api/admin/me') } catch (e) { /* 已由请求层处理 */ }
}

async function fetchStats() {
  try {
    const data = await adminFetch('/api/manage/stats')
    if (data) stats.value = data
  } catch (e) { /* 统计失败不阻断列表 */ }
}

async function fetchItems() {
  loading.value = true
  notice.value = ''
  try {
    const data = await adminFetch('/api/manage/items?' + buildQuery())
    items.value = (data && Array.isArray(data.list)) ? data.list : []
    total.value = data ? Number(data.total || 0) : 0
    pages.value = data ? Number(data.pages || 0) : 0
    page.value = data ? Number(data.page || 1) : 1
    loaded.value = true
    backendReady.value = true
    if (!items.value.length) {
      notice.value = isRecycle.value
        ? '回收站空空如也，没有被移除的条目。'
        : '生死簿空空如也，尚无条目。'
    }
  } catch (e) {
    backendReady.value = false
    items.value = []
    total.value = 0
    pages.value = 0
    notice.value = e.message || '无法载入条目列表'
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  page.value = 1
  fetchItems()
}
function resetFilter() {
  keyword.value = ''
  categoryId.value = ''
  statusFilter.value = ''
  page.value = 1
  fetchItems()
}
function goPage(n) {
  if (n < 1 || n === page.value || (pages.value && n > pages.value)) return
  page.value = n
  fetchItems()
}
function onSizeChange() {
  page.value = 1
  fetchItems()
}

// 点击涟漪
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

// ---------- 轻提示 ----------
const toast = ref('')
let toastTimer = null
function flash(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 2400)
}

// ---------- 分类下拉 ----------
const categories = ref([])
async function fetchCategories() {
  try {
    const res = await fetch(API_BASE + '/api/home')
    const json = await res.json()
    const tree = (json.code === 200 && json.data && json.data.categories) || []
    const flat = []
    const walk = (nodes, prefix) => {
      (nodes || []).forEach(n => {
        flat.push({ id: n.id, label: prefix + n.name })
        if (n.children && n.children.length) walk(n.children, prefix + '　')
      })
    }
    walk(tree, '')
    categories.value = flat
  } catch (e) {
    categories.value = []
  }
}

// ---------- 修改 ----------
const showEdit = ref(false)
const editSaving = ref(false)
const editError = ref('')
const editId = ref(null)
const blankForm = () => ({ emoji: '', title: '', description: '', tag: '', author: '', categoryId: '', status: 1, content: '' })
const editForm = ref(blankForm())

function openEdit(it) {
  editId.value = it.id
  editError.value = ''
  editForm.value = {
    emoji: it.emoji || '',
    title: it.title || '',
    description: it.desc || '',
    tag: it.tag || '',
    author: it.author || '',
    categoryId: it.categoryId || '',
    status: it.status == null ? 1 : it.status,
    content: it.content || ''
  }
  showEdit.value = true
}
function closeEdit() {
  if (editSaving.value) return
  showEdit.value = false
}
async function saveEdit() {
  const f = editForm.value
  if (!f.title || !f.title.trim()) { editError.value = '标题不能为空'; return }
  editSaving.value = true
  editError.value = ''
  try {
    const data = await adminFetch(`/api/manage/items/${editId.value}`, {
      method: 'PUT',
      body: {
        title: f.title.trim(),
        description: f.description,
        emoji: f.emoji,
        tag: f.tag,
        author: f.author,
        content: f.content,
        categoryId: f.categoryId === '' ? null : Number(f.categoryId),
        status: Number(f.status)
      }
    })
    replaceRow(data)
    showEdit.value = false
    fetchStats()
    flash('条目已修改 ♥')
  } catch (e) {
    editError.value = e.message || '保存失败，请稍后再试'
  } finally {
    editSaving.value = false
  }
}

// ---------- 新增 ----------
const showCreate = ref(false)
const createSaving = ref(false)
const createError = ref('')
const createForm = ref(blankForm())

function openCreate() {
  createError.value = ''
  createForm.value = blankForm()
  showCreate.value = true
}
function closeCreate() {
  if (createSaving.value) return
  showCreate.value = false
}
async function saveCreate() {
  const f = createForm.value
  if (!f.title || !f.title.trim()) { createError.value = '标题不能为空'; return }
  createSaving.value = true
  createError.value = ''
  try {
    await adminFetch('/api/manage/items', {
      method: 'POST',
      body: {
        title: f.title.trim(),
        description: f.description,
        emoji: f.emoji,
        tag: f.tag,
        author: f.author,
        content: f.content,
        categoryId: f.categoryId === '' ? null : Number(f.categoryId),
        status: Number(f.status)
      }
    })
    showCreate.value = false
    page.value = 1
    await fetchItems()
    fetchStats()
    flash('新条目已落笔 ♥')
  } catch (e) {
    createError.value = e.message || '创建失败，请稍后再试'
  } finally {
    createSaving.value = false
  }
}

// ---------- 上/下线 ----------
const busyId = ref(null)
async function toggleStatus(it) {
  const next = it.status === 1 ? 0 : 1
  busyId.value = it.id
  try {
    const data = await adminFetch(`/api/manage/items/${it.id}/status?status=${next}`, { method: 'PUT' })
    replaceRow(data)
    fetchStats()
    flash(next === 1 ? '已上线 ♥' : '已下线')
  } catch (e) {
    flash(e.message || '操作失败')
  } finally {
    busyId.value = null
  }
}

// ---------- 删除 ----------
const showDelete = ref(false)
const deleting = ref(false)
const delTarget = ref(null)
const delError = ref('')

function askDelete(it) {
  delTarget.value = it
  delError.value = ''
  showDelete.value = true
}
function closeDelete() {
  if (deleting.value) return
  showDelete.value = false
}
async function confirmDelete() {
  const it = delTarget.value
  if (!it) return
  deleting.value = true
  delError.value = ''
  try {
    await adminFetch(`/api/manage/items/${it.id}`, { method: 'DELETE' })
    showDelete.value = false
    await fetchItems()
    fetchStats()
    flash('条目已移入回收站')
  } catch (e) {
    delError.value = e.message || '删除失败，请稍后再试'
  } finally {
    deleting.value = false
  }
}

// ---------- 回收站恢复 ----------
async function restoreItem(it) {
  busyId.value = it.id
  try {
    await adminFetch(`/api/manage/items/${it.id}/restore`, { method: 'POST' })
    await fetchItems()
    fetchStats()
    flash('条目已重回生死簿 ♥')
  } catch (e) {
    flash(e.message || '恢复失败')
  } finally {
    busyId.value = null
  }
}

// ---------- 退出登录 ----------
async function doLogout() {
  try { await adminFetch('/api/admin/logout', { method: 'POST' }) } catch (e) { /* 本地仍要登出 */ }
  clearAdmin()
  emit('logout')
}

// ---------- 账号安全：修改口令 ----------
const pwdForm = ref({ oldPassword: '', newPassword: '', confirm: '' })
const pwdSaving = ref(false)
const pwdError = ref('')
async function savePassword() {
  const f = pwdForm.value
  if (!f.oldPassword || !f.newPassword) { pwdError.value = '请填写原口令与新口令'; return }
  if (f.newPassword.length < 6) { pwdError.value = '新口令至少 6 位'; return }
  if (f.newPassword !== f.confirm) { pwdError.value = '两次输入的新口令不一致'; return }
  pwdSaving.value = true
  pwdError.value = ''
  try {
    const data = await adminFetch('/api/admin/password', {
      method: 'PUT',
      body: { oldPassword: f.oldPassword, newPassword: f.newPassword }
    })
    if (data && data.token) persistAdmin(Object.assign({}, props.admin || {}, data))
    pwdForm.value = { oldPassword: '', newPassword: '', confirm: '' }
    flash('口令已更换，新令牌已生效 ♥')
  } catch (e) {
    pwdError.value = e.message || '更换失败，请稍后再试'
  } finally {
    pwdSaving.value = false
  }
}

function replaceRow(vo) {
  if (!vo) { fetchItems(); return }
  const i = items.value.findIndex(x => x.id === vo.id)
  if (i === -1) { fetchItems(); return }
  items.value.splice(i, 1, vo)
}

// Esc 关闭弹窗（保存/删除/新增进行中不打断）
function onEsc(e) {
  if (e.key !== 'Escape') return
  if (showDelete.value) closeDelete()
  else if (showEdit.value) closeEdit()
  else if (showCreate.value) closeCreate()
}
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
        <button
          v-if="partnerSite.enabled" class="ghost partner-link" type="button"
          :title="'前往 ' + partnerSite.name + '（' + partnerSite.url + '）'"
          @click="openPartnerSite" @pointerdown="spawnRipple"
        >↗ {{ partnerSite.name }}</button>
        <span class="channel" :class="backendReady ? 'on' : 'off'">
          {{ backendReady ? '● 信道已通' : '○ 信道断开' }}
        </span>
        <span class="admin-chip" title="已通过服务端校验的管理员">
          <span class="admin-avatar">🧿</span>
          <span class="admin-name">{{ adminName }}</span>
        </span>
        <button class="logout" type="button" @click="doLogout" @pointerdown="spawnRipple">退 出</button>
      </div>
    </header>

    <!-- ============ 标签页 ============ -->
    <nav class="tabs" role="tablist">
      <button
        v-for="t in tabs" :key="t.key" type="button" class="tab"
        :class="{ active: tab === t.key }" role="tab" :aria-selected="tab === t.key"
        @click="switchTab(t.key)"
      >
        <span class="tab-ico">{{ t.icon }}</span>{{ t.label }}
        <em v-if="t.key === 'recycle' && stats.deleted">{{ stats.deleted }}</em>
      </button>
    </nav>

    <!-- ============ 概览 ============ -->
    <section class="stats">
      <div v-tilt="{ strength: 7 }" class="stat violet">
        <span class="stat-num">{{ stats.total }}</span>
        <span class="stat-label">条目总数</span>
      </div>
      <div v-tilt="{ strength: 7 }" class="stat pink">
        <span class="stat-num">{{ stats.online }}</span>
        <span class="stat-label">上线中</span>
      </div>
      <div v-tilt="{ strength: 7 }" class="stat gold">
        <span class="stat-num">{{ stats.offline }}</span>
        <span class="stat-label">已下线</span>
      </div>
      <div v-tilt="{ strength: 7 }" class="stat">
        <span class="stat-num">{{ stats.deleted }}</span>
        <span class="stat-label">回收站</span>
      </div>
    </section>

    <!-- ============ 账号安全 ============ -->
    <section v-if="tab === 'account'" class="account">
      <h2 class="acc-title">账号安全</h2>
      <p class="acc-tip">
        当前管理员：<b>{{ adminName }}</b>。后台凭证有效期 8 小时，过期后需重新登录。
        首次使用默认账号 <code>admin / admin123</code> 者，请务必立即更换口令。
      </p>
      <form class="acc-form" @submit.prevent="savePassword">
        <label class="mf">
          <span>原口令</span>
          <input v-model="pwdForm.oldPassword" type="password" autocomplete="current-password"
                 maxlength="40" placeholder="请输入当前口令" />
        </label>
        <label class="mf">
          <span>新口令（6~40 位）</span>
          <input v-model="pwdForm.newPassword" type="password" autocomplete="new-password"
                 maxlength="40" placeholder="请输入新口令" />
        </label>
        <label class="mf">
          <span>确认新口令</span>
          <input v-model="pwdForm.confirm" type="password" autocomplete="new-password"
                 maxlength="40" placeholder="再输入一次新口令" />
        </label>
        <p v-if="pwdError" class="modal-err">{{ pwdError }}</p>
        <div class="acc-foot">
          <button class="btn-main" type="submit" :disabled="pwdSaving" @pointerdown="spawnRipple">
            {{ pwdSaving ? '更换中…' : '更 换 口 令' }}
          </button>
        </div>
        <p class="acc-note">更换成功后旧令牌立即失效，系统会自动用新令牌续期，无需重新登录。</p>
      </form>
    </section>

    <!-- ============ 条目管理 / 回收站 ============ -->
    <template v-else>
      <section class="filter">
        <form class="filter-form" @submit.prevent="applyFilter">
          <input v-model="keyword" type="text" maxlength="40" placeholder="按标题 / 标签 / 描述搜寻" />
          <select v-model="categoryId" class="sel" aria-label="按分类筛选">
            <option value="">全部分类</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.label }}</option>
          </select>
          <select v-model="statusFilter" class="sel status-sel" aria-label="按状态筛选">
            <option value="">全部状态</option>
            <option value="1">上线</option>
            <option value="0">下线</option>
          </select>
          <button class="btn-main" type="submit" :disabled="loading" @pointerdown="spawnRipple">
            {{ loading ? '翻检中…' : '查 阅' }}
          </button>
          <button class="btn-ghost" type="button" @click="resetFilter" @pointerdown="spawnRipple">重 置</button>
        </form>
      </section>

      <section class="list-wrap">
        <div class="list-head">
          <h2 class="list-title">
            {{ isRecycle ? '回收站' : '条目管理' }} <em>{{ total }} 条</em>
          </h2>
          <button v-if="!isRecycle" class="btn-add" type="button" @click="openCreate" @pointerdown="spawnRipple">
            ＋ 新增条目
          </button>
          <span v-else class="recycle-hint">逻辑删除的条目可在此恢复</span>
        </div>

        <p v-if="notice" class="notice" :class="{ warn: !backendReady }">{{ notice }}</p>

        <div v-if="items.length" class="table" role="table">
          <div class="tr head" role="row">
            <span class="th emoji">图</span>
            <span class="th">标题</span>
            <span class="th desc">描述</span>
            <span class="th cat">分类</span>
            <span class="th tag">标签</span>
            <span class="th">状态</span>
            <span class="th act">操作</span>
          </div>
          <div v-for="it in items" :key="it.id" class="tr" role="row">
            <span class="td emoji">{{ it.emoji || '💮' }}</span>
            <span class="td title-cell">{{ it.title }}</span>
            <span class="td desc">{{ it.desc }}</span>
            <span class="td cat">{{ it.categoryName || '—' }}</span>
            <span class="td tag">{{ it.tag || '—' }}</span>
            <span class="td">
              <button
                v-if="!isRecycle" class="status" :class="{ on: it.status === 1 }"
                type="button" :disabled="busyId === it.id"
                :title="it.status === 1 ? '点击下线' : '点击上线'"
                @click="toggleStatus(it)"
              >{{ statusText(it.status) }}</button>
              <span v-else class="status static" :class="{ on: it.status === 1 }">{{ statusText(it.status) }}</span>
            </span>
            <span class="td act">
              <template v-if="!isRecycle">
                <button class="row-btn edit" type="button" @click="openEdit(it)" title="修改此条目">修改</button>
                <button class="row-btn del" type="button" @click="askDelete(it)" title="移入回收站">删除</button>
              </template>
              <button v-else class="row-btn recover" type="button" :disabled="busyId === it.id"
                      @click="restoreItem(it)" title="恢复此条目">恢 复</button>
            </span>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="pages > 0" class="pager">
          <span class="page-info">
            第 {{ pageStart }}–{{ pageEnd }} 条 / 共 {{ total }} 条 · 第 {{ page }}/{{ pages }} 页
          </span>
          <div class="pager-ctl">
            <select v-model.number="pageSize" class="size-sel" @change="onSizeChange" aria-label="每页条数">
              <option :value="5">5 条/页</option>
              <option :value="10">10 条/页</option>
              <option :value="20">20 条/页</option>
              <option :value="50">50 条/页</option>
            </select>
            <button type="button" :disabled="page <= 1 || loading" @click="goPage(page - 1)">‹ 上一页</button>
            <button type="button" :disabled="page >= pages || loading" @click="goPage(page + 1)">下一页 ›</button>
          </div>
        </div>

        <p v-else-if="!loading && loaded && !backendReady" class="empty">
          待后端接口就绪后，此处将呈现条目管理列表。
        </p>
      </section>
    </template>

    <!-- ============ 轻提示 ============ -->
    <transition name="fade">
      <p v-if="toast" class="dash-toast">{{ toast }}</p>
    </transition>

    <!-- ============ 修改条目弹窗 ============ -->
    <transition name="fade">
      <div v-if="showEdit" class="mask" @click.self="closeEdit">
        <div class="modal" role="dialog" aria-modal="true" aria-label="修改条目">
          <header class="modal-head">
            <h2>修改条目 <em>ID {{ editId }}</em></h2>
            <button class="modal-x" type="button" :disabled="editSaving" @click="closeEdit">✕</button>
          </header>

          <form class="modal-body" @submit.prevent="saveEdit">
            <div class="mrow">
              <label class="mf emoji-f">
                <span>图标</span>
                <input v-model="editForm.emoji" maxlength="20" placeholder="💮" />
              </label>
              <label class="mf grow">
                <span>标题 *</span>
                <input v-model="editForm.title" maxlength="120" placeholder="条目标题" />
              </label>
            </div>

            <label class="mf">
              <span>描述</span>
              <input v-model="editForm.description" maxlength="500" placeholder="一句话描述" />
            </label>

            <div class="mrow">
              <label class="mf grow">
                <span>分类</span>
                <select v-model="editForm.categoryId">
                  <option value="">未分类</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.label }}</option>
                </select>
              </label>
              <label class="mf">
                <span>标签</span>
                <input v-model="editForm.tag" maxlength="50" placeholder="如：红妆" />
              </label>
            </div>

            <div class="mrow">
              <label class="mf grow">
                <span>作者 / 来源</span>
                <input v-model="editForm.author" maxlength="50" placeholder="可留空" />
              </label>
              <label class="mf">
                <span>状态</span>
                <select v-model.number="editForm.status">
                  <option :value="1">上线</option>
                  <option :value="0">下线</option>
                </select>
              </label>
            </div>

            <label class="mf">
              <span>正文</span>
              <textarea v-model="editForm.content" rows="4" placeholder="条目详情（可留空）"></textarea>
            </label>

            <p v-if="editError" class="modal-err">{{ editError }}</p>

            <footer class="modal-foot">
              <button class="btn-ghost" type="button" :disabled="editSaving" @click="closeEdit">取 消</button>
              <button class="btn-main" type="submit" :disabled="editSaving">
                {{ editSaving ? '落笔中…' : '保 存 修 改' }}
              </button>
            </footer>
          </form>
        </div>
      </div>
    </transition>

    <!-- ============ 新增条目弹窗 ============ -->
    <transition name="fade">
      <div v-if="showCreate" class="mask" @click.self="closeCreate">
        <div class="modal" role="dialog" aria-modal="true" aria-label="新增条目">
          <header class="modal-head">
            <h2>新增条目 <em>落笔生死簿</em></h2>
            <button class="modal-x" type="button" :disabled="createSaving" @click="closeCreate">✕</button>
          </header>

          <form class="modal-body" @submit.prevent="saveCreate">
            <div class="mrow">
              <label class="mf emoji-f">
                <span>图标</span>
                <input v-model="createForm.emoji" maxlength="20" placeholder="💮" />
              </label>
              <label class="mf grow">
                <span>标题 *</span>
                <input v-model="createForm.title" maxlength="120" placeholder="条目标题" />
              </label>
            </div>

            <label class="mf">
              <span>描述</span>
              <input v-model="createForm.description" maxlength="500" placeholder="一句话描述" />
            </label>

            <div class="mrow">
              <label class="mf grow">
                <span>分类</span>
                <select v-model="createForm.categoryId">
                  <option value="">未分类</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.label }}</option>
                </select>
              </label>
              <label class="mf">
                <span>标签</span>
                <input v-model="createForm.tag" maxlength="50" placeholder="如：红妆" />
              </label>
            </div>

            <div class="mrow">
              <label class="mf grow">
                <span>作者 / 来源</span>
                <input v-model="createForm.author" maxlength="50" placeholder="可留空" />
              </label>
              <label class="mf">
                <span>状态</span>
                <select v-model.number="createForm.status">
                  <option :value="1">上线</option>
                  <option :value="0">下线</option>
                </select>
              </label>
            </div>

            <label class="mf">
              <span>正文</span>
              <textarea v-model="createForm.content" rows="4" placeholder="条目详情（可留空）"></textarea>
            </label>

            <p v-if="createError" class="modal-err">{{ createError }}</p>

            <footer class="modal-foot">
              <button class="btn-ghost" type="button" :disabled="createSaving" @click="closeCreate">取 消</button>
              <button class="btn-main" type="submit" :disabled="createSaving">
                {{ createSaving ? '落笔中…' : '创 建 条 目' }}
              </button>
            </footer>
          </form>
        </div>
      </div>
    </transition>

    <!-- ============ 删除确认弹窗 ============ -->
    <transition name="fade">
      <div v-if="showDelete" class="mask" @click.self="closeDelete">
        <div class="modal narrow" role="dialog" aria-modal="true" aria-label="确认删除">
          <header class="modal-head">
            <h2>移出生死簿</h2>
            <button class="modal-x" type="button" :disabled="deleting" @click="closeDelete">✕</button>
          </header>
          <div class="modal-body">
            <p class="del-tip">
              将移除条目 <b>「{{ delTarget && delTarget.title }}」</b>
              <em v-if="delTarget">（ID {{ delTarget.id }}）</em>。<br />
              此为逻辑删除：首页与搜索立即不再展示，条目会进入「回收站」，可随时恢复。
            </p>
            <p v-if="delError" class="modal-err">{{ delError }}</p>
            <footer class="modal-foot">
              <button class="btn-ghost" type="button" :disabled="deleting" @click="closeDelete">再 想 想</button>
              <button class="btn-danger" type="button" :disabled="deleting" @click="confirmDelete">
                {{ deleting ? '移除中…' : '确 认 删 除' }}
              </button>
            </footer>
          </div>
        </div>
      </div>
    </transition>
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
/* 外部联动站点入口：与「返回首页」区分开，带一点粉紫描金 */
.partner-link{
  color:#FFD9E7;
  border-color:rgba(232,74,120,.55);
  background:linear-gradient(180deg, rgba(232,74,120,.20), rgba(106,58,160,.18));
}
.partner-link:hover{
  color:#fff;
  background:linear-gradient(180deg, rgba(232,74,120,.38), rgba(106,58,160,.30));
  box-shadow:0 0 16px rgba(232,74,120,.35);
}
.channel{ font-size:11px; letter-spacing:1px; padding:4px 10px; border-radius:999px; white-space:nowrap; }
.channel.on{ color:#C9F7D8; background:rgba(60,180,110,.14); border:1px solid rgba(123,227,162,.4); }
.channel.off{ color:#FFC9D9; background:rgba(232,74,120,.14); border:1px solid rgba(255,111,156,.45); }
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
.logout{
  border:1px solid rgba(255,179,200,.5); background:transparent; color:var(--rose);
  font-size:13px; letter-spacing:2px; padding:8px 16px; border-radius:999px; cursor:pointer; transition:.2s;
}
.logout:hover{ color:#fff; background:rgba(232,74,120,.25); }

/* ---------- 标签页 ---------- */
.tabs{
  width:min(980px,92vw); margin:20px auto 0;
  display:flex; gap:8px; flex-wrap:wrap;
  padding:6px; border-radius:999px;
  background:linear-gradient(180deg, rgba(255,247,251,.12), rgba(255,247,251,.06));
  border:1px solid var(--hairline);
  box-shadow:var(--shadow-1), inset 0 1px 0 rgba(255,255,255,.12);
}
.tab{
  flex:0 0 auto; cursor:pointer; font-family:inherit;
  border:1px solid transparent; background:transparent; color:var(--rose);
  font-size:13px; letter-spacing:2px; padding:9px 20px; border-radius:999px; transition:.25s var(--ease-out);
}
.tab:hover{ color:#fff; background:rgba(155,107,214,.22); }
.tab.active{
  color:#fff; border-color:rgba(244,213,139,.45);
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  box-shadow:0 5px 16px rgba(106,58,160,.5), inset 0 1px 0 rgba(255,255,255,.3);
}
.tab-ico{ margin-right:6px; }
.tab em{
  font-style:normal; margin-left:6px; font-size:10px; padding:1px 7px; border-radius:999px;
  color:#2b164a; background:linear-gradient(180deg,#F4D58B,#E7B85A);
}

/* ---------- 概览统计 ---------- */
.stats{
  width:min(980px,92vw); margin:22px auto 0;
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

/* ---------- 筛选 ---------- */
.filter{ width:min(980px,92vw); margin:20px auto 0; }
.filter-form{ display:flex; gap:10px; flex-wrap:wrap; }
.filter-form input, .filter-form .sel{
  flex:1 1 180px; min-width:0;
  padding:11px 14px; border-radius:999px; outline:none;
  border:1px solid rgba(155,107,214,.45);
  background:rgba(23,12,43,.45); color:var(--ivory);
  font-size:13.5px; font-family:inherit; transition:.2s;
}
.filter-form input::placeholder{ color:rgba(255,233,243,.4); }
.filter-form .sel{ flex:0 1 160px; cursor:pointer; }
.filter-form .status-sel{ flex:0 1 130px; }
.filter-form .sel option{ background:#2b164a; color:var(--ivory); }
.filter-form input:focus, .filter-form .sel:focus{ border-color:#B98CF0; box-shadow:var(--focus-ring-violet); }
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
.list-head{
  display:flex; align-items:center; justify-content:space-between; gap:12px;
  margin:0 0 12px;
}
.list-title{
  margin:0; font-family:var(--font-serif); font-size:18px; letter-spacing:2px; color:var(--ivory);
  display:flex; align-items:baseline; gap:8px;
}
.list-title em{ font-style:normal; font-size:12px; letter-spacing:1px; color:var(--gold); opacity:.8; }
.recycle-hint{ font-size:12px; color:var(--rose); opacity:.75; letter-spacing:1px; }
.btn-add{
  position:relative; overflow:hidden;
  border:none; cursor:pointer; padding:10px 20px; border-radius:999px;
  font-family:var(--font-serif); font-size:13.5px; font-weight:700; letter-spacing:2px; color:#fff;
  background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 5px 0 #A82B53, 0 10px 18px rgba(232,74,120,.4), 0 0 18px rgba(255,111,156,.4);
  transition:transform .14s, filter .2s;
}
.btn-add:hover:not(:disabled){ filter:brightness(1.08); transform:translateY(-2px); }
.btn-add:disabled{ cursor:default; filter:grayscale(.3); }
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
.tr{ display:grid; grid-template-columns:44px 1.2fr 2fr .8fr .8fr 82px 132px; gap:10px; align-items:center;
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
  font-family:inherit; cursor:pointer; transition:.2s;
}
.status:hover:not(:disabled):not(.static){ color:#fff; background:rgba(232,74,120,.42); box-shadow:0 0 14px rgba(255,111,156,.55); }
.status:disabled{ cursor:wait; opacity:.6; }
.status.static{ cursor:default; }
.status.on{ color:#C9F7D8; background:rgba(60,180,110,.16); border-color:rgba(123,227,162,.45); }
.status.on:hover:not(:disabled):not(.static){ color:#0d2d1a; background:rgba(123,227,162,.55); box-shadow:0 0 14px rgba(123,227,162,.5); }

/* ---------- 行内操作按钮 ---------- */
.td.act{ display:flex; align-items:center; gap:6px; overflow:visible; }
.row-btn{
  font-family:inherit; font-size:11.5px; letter-spacing:1px;
  padding:5px 10px; border-radius:999px; cursor:pointer; transition:.2s;
  border:1px solid transparent; background:transparent;
}
.row-btn.edit{ color:#D9C3FF; border-color:rgba(155,107,214,.6); background:rgba(155,107,214,.14); }
.row-btn.edit:hover{ color:#fff; background:rgba(155,107,214,.5); box-shadow:0 0 14px rgba(185,140,240,.45); }
.row-btn.del{ color:#FFC9D9; border-color:rgba(255,111,156,.5); background:rgba(232,74,120,.12); }
.row-btn.del:hover{ color:#fff; background:rgba(201,51,99,.72); box-shadow:0 0 14px rgba(255,111,156,.45); }
.row-btn.recover{ color:#C9F7D8; border-color:rgba(123,227,162,.55); background:rgba(60,180,110,.14); }
.row-btn.recover:hover:not(:disabled){ color:#0d2d1a; background:rgba(123,227,162,.55); box-shadow:0 0 14px rgba(123,227,162,.45); }
.row-btn:disabled{ cursor:wait; opacity:.55; }

/* ---------- 分页 ---------- */
.pager{
  display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap;
  margin-top:14px; padding:10px 4px;
}
.page-info{ font-size:12.5px; letter-spacing:1px; color:var(--rose); opacity:.85; }
.pager-ctl{ display:flex; align-items:center; gap:8px; }
.pager-ctl button{
  font-family:inherit; font-size:12px; letter-spacing:1px; cursor:pointer;
  padding:7px 14px; border-radius:999px; color:var(--gold);
  border:1px solid rgba(244,213,139,.5); background:rgba(244,213,139,.08); transition:.2s;
}
.pager-ctl button:hover:not(:disabled){ color:#fff; background:rgba(244,213,139,.22); }
.pager-ctl button:disabled{ cursor:default; opacity:.4; }
.size-sel{
  font-family:inherit; font-size:12px; cursor:pointer; color:var(--ivory);
  padding:7px 12px; border-radius:999px; outline:none;
  border:1px solid rgba(155,107,214,.5); background:rgba(23,12,43,.55);
}
.size-sel option{ background:#2b164a; color:var(--ivory); }

/* ---------- 账号安全 ---------- */
.account{
  width:min(560px,92vw); margin:24px auto 60px; padding:26px 28px 28px;
  border-radius:var(--radius-lg);
  background:linear-gradient(180deg, rgba(255,247,251,.13), rgba(255,247,251,.06));
  border:1px solid rgba(155,107,214,.45);
  box-shadow:var(--shadow-1), inset 0 1px 0 rgba(255,255,255,.14);
}
.acc-title{
  margin:0 0 10px; font-family:var(--font-serif); font-size:19px; letter-spacing:2px; color:var(--ivory);
}
.acc-tip{ margin:0 0 20px; font-size:12.5px; line-height:1.9; color:var(--gold); opacity:.9; }
.acc-tip b{ color:var(--ivory); }
.acc-tip code{ color:#FFE6F0; font-family:inherit; }
.acc-form .mf{ display:block; margin-bottom:14px; }
.acc-form .mf > span{
  display:block; margin:0 0 6px 2px; font-size:12px; letter-spacing:1.5px; color:var(--gold); opacity:.9;
}
.acc-form .mf input{
  width:100%; padding:11px 13px; border-radius:12px; outline:none;
  border:1px solid rgba(155,107,214,.5); background:rgba(23,12,43,.6);
  color:var(--ivory); font-size:13.5px; font-family:inherit; transition:.2s;
}
.acc-form .mf input::placeholder{ color:rgba(255,233,243,.35); }
.acc-form .mf input:focus{ border-color:#B98CF0; box-shadow:var(--focus-ring-violet); }
.acc-foot{ display:flex; justify-content:flex-end; margin-top:6px; }
.acc-note{ margin:14px 0 0; font-size:11.5px; line-height:1.7; color:var(--rose); opacity:.7; }

/* ---------- 弹窗 ---------- */
.mask{
  position:fixed; inset:0; z-index:60;
  display:grid; place-items:center; padding:24px 16px;
  background:rgba(12,5,24,.62);
  backdrop-filter:blur(6px) saturate(130%); -webkit-backdrop-filter:blur(6px) saturate(130%);
}
.modal{
  width:min(560px,94vw); max-height:calc(100vh - 60px); overflow-y:auto;
  border-radius:var(--radius-lg); text-align:left;
  background:linear-gradient(180deg, rgba(48,26,82,.98), rgba(32,16,58,.98));
  border:1px solid rgba(155,107,214,.65);
  box-shadow:0 24px 60px rgba(10,4,22,.7), 0 0 0 1px rgba(244,213,139,.18) inset, 0 0 40px rgba(106,58,160,.35);
  animation:modalIn .3s var(--ease-out) both;
}
.modal.narrow{ width:min(430px,94vw); }
@keyframes modalIn{ from{ opacity:0; transform:translateY(16px) scale(.97); } to{ opacity:1; transform:none; } }
.modal-head{
  display:flex; align-items:center; justify-content:space-between; gap:12px;
  padding:16px 20px; border-bottom:1px solid rgba(155,107,214,.32);
}
.modal-head h2{
  margin:0; font-family:var(--font-serif); font-size:17px; letter-spacing:2px; color:var(--ivory);
  display:flex; align-items:baseline; gap:8px;
}
.modal-head h2 em{ font-style:normal; font-size:11px; letter-spacing:1px; color:var(--gold); opacity:.8; }
.modal-x{
  border:none; background:transparent; color:var(--rose); font-size:14px; cursor:pointer;
  padding:4px 8px; border-radius:8px; transition:.2s;
}
.modal-x:hover:not(:disabled){ color:#fff; background:rgba(232,74,120,.3); }
.modal-x:disabled{ opacity:.4; cursor:default; }

.modal-body{ padding:18px 20px 20px; }
.mrow{ display:flex; gap:12px; }
.mrow > .mf{ flex:0 1 130px; }
.mrow > .mf.grow{ flex:1 1 auto; }
.mf{ display:block; margin-bottom:12px; }
.mf > span{
  display:block; margin:0 0 6px 2px;
  font-size:12px; letter-spacing:1.5px; color:var(--gold); opacity:.9;
}
.mf input, .mf select, .mf textarea{
  width:100%; padding:10px 13px; border-radius:12px; outline:none;
  border:1px solid rgba(155,107,214,.5);
  background:rgba(23,12,43,.6); color:var(--ivory);
  font-size:13.5px; font-family:inherit; transition:.2s;
}
.mf input::placeholder, .mf textarea::placeholder{ color:rgba(255,233,243,.35); }
.mf input:focus, .mf select:focus, .mf textarea:focus{ border-color:#B98CF0; box-shadow:var(--focus-ring-violet); }
.mf select option{ background:#2b164a; color:var(--ivory); }
.mf textarea{ resize:vertical; line-height:1.7; }
.emoji-f input{ text-align:center; font-size:16px; }

.modal-err{
  margin:4px 0 10px; padding:8px 12px; border-radius:10px; font-size:12.5px; line-height:1.6;
  color:#FFC9D9; background:rgba(232,74,120,.14); border:1px solid rgba(255,111,156,.45);
}
.del-tip{ margin:0 0 14px; font-size:13px; line-height:1.9; color:var(--ivory); opacity:.92; }
.del-tip b{ color:var(--gold); }
.del-tip em{ font-style:normal; color:var(--rose); opacity:.85; }

.modal-foot{ display:flex; justify-content:flex-end; gap:10px; margin-top:6px; }
.modal-foot .btn-main{ padding:11px 22px; letter-spacing:2px; }
.btn-danger{
  border:none; cursor:pointer; padding:11px 22px; border-radius:999px;
  font-family:var(--font-serif); font-size:14px; font-weight:700; letter-spacing:2px; color:#fff;
  background:linear-gradient(180deg,#FF7B9E,#C93363);
  box-shadow:0 5px 0 #8E1F44, 0 10px 18px rgba(201,51,99,.42), 0 0 18px rgba(255,111,156,.35);
  transition:transform .14s, filter .2s;
}
.btn-danger:hover:not(:disabled){ filter:brightness(1.08); transform:translateY(-2px); }
.btn-danger:disabled{ cursor:default; filter:grayscale(.3); }

.dash-toast{
  position:fixed; left:50%; top:80px; transform:translateX(-50%); z-index:70;
  margin:0; padding:10px 22px; border-radius:999px; font-size:13px; letter-spacing:1px; color:#fff;
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  border:1px solid rgba(244,213,139,.35);
  box-shadow:0 10px 26px rgba(20,8,40,.55);
}
.fade-enter-active,.fade-leave-active{ transition:opacity .25s ease; }
.fade-enter-from,.fade-leave-to{ opacity:0; }

@media (max-width:720px){
  .tr{ grid-template-columns:36px 1.2fr 78px 128px; }
  .tr .desc, .tr .th.desc, .tr .cat, .tr .th.cat, .tr .tag, .tr .th.tag{ display:none; }
  .topbar{ flex-wrap:wrap; }
  .mrow{ flex-wrap:wrap; }
  .mrow > .mf, .mrow > .mf.grow{ flex:1 1 100%; }
  .filter-form .sel, .filter-form .status-sel{ flex:1 1 46%; }
  .pager{ flex-direction:column; align-items:flex-start; }
}

/* ---------- 微微发光的科技感交互 ---------- */
.btn-main,.btn-ghost,.ghost,.logout{ position:relative; overflow:hidden; }
.tech-ripple{
  position:absolute; border-radius:50%; pointer-events:none; z-index:1;
  background:radial-gradient(circle, rgba(185,140,240,.55) 0%, rgba(185,140,240,.2) 42%, transparent 66%);
  transform:scale(0); animation:techRipple .62s ease-out forwards;
}
@keyframes techRipple{ to{ transform:scale(1); opacity:0; } }

.tr{ position:relative; transition:background .25s var(--ease-out), box-shadow .25s var(--ease-out); }
.tr:not(.head):hover{
  background:rgba(155,107,214,.14);
  box-shadow:inset 0 0 26px rgba(155,107,214,.22), inset 2px 0 0 #B98CF0;
}

.status{ box-shadow:0 0 10px rgba(255,111,156,.35); }
.status.on{ box-shadow:0 0 12px rgba(123,227,162,.42); }

.stat{ transition:box-shadow .3s var(--ease-out), border-color .3s, transform .3s var(--ease-out); }
.stat:hover{ border-color:rgba(185,140,240,.7); box-shadow:var(--shadow-1), 0 0 26px rgba(155,107,214,.4); }

.btn-main{ box-shadow:0 5px 0 #4A2680, 0 10px 18px rgba(106,58,160,.45), 0 0 18px rgba(155,107,214,.4); }
.ghost:hover,.logout:hover{ box-shadow:0 0 16px rgba(232,74,120,.35); }
.btn-ghost:hover{ box-shadow:0 0 16px rgba(244,213,139,.35); }
</style>
