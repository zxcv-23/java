<script setup>
import { ref, computed, onMounted } from 'vue'
import HeroCrest from './HeroCrest.vue'
import BannerCarousel from './BannerCarousel.vue'

// 登录后由父组件传入的当前用户（来自 localStorage/sessionStorage 的 guiniang_user）
const props = defineProps({
  user: { type: Object, default: null }
})
const emit = defineEmits(['logout', 'goAdmin', 'goProfile'])

// ---------- 接口地址 ----------
const API_BASE = 'http://127.0.0.1:8081'
const SEARCH_API = API_BASE + '/api/search'
const HOME_API = API_BASE + '/api/home'

// ---------- 导航栏 ----------
const navItems = ['首页', '姻缘簿', '花轿信使', '红妆阁', '往生簿']
const activeNav = ref('首页')
const pageToast = ref('')
let toastTimer = null
function flashToast(msg) {
  pageToast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (pageToast.value = ''), 2200)
}

// ---------- 左侧导航侧边栏 ----------
const railOpen = ref(false)
const railIcons = { '首页': '🏮', '姻缘簿': '💞', '花轿信使': '📜', '红妆阁': '🌹', '往生簿': '🕯️' }
function goNav(item) {
  onNav(item)
  railOpen.value = false
}

// 当前夫君名讳（优先取后端返回的 account / username / name）
const userName = props.user
  ? (props.user.account || props.user.username || props.user.name || '夫君')
  : '夫君'
function onLogout() {
  emit('logout')
}

// 进入后台管理系统（点击导航栏「后台管理」）
function goAdmin() {
  emit('goAdmin')
}

// 进入个人中心（点击导航栏「个人中心」）
function goProfile() {
  emit('goProfile')
}

// ---------- 首页数据（展示功能） ----------
const categories = ref([])      // 分类树（分级）
const featured = ref([])        // 首页推荐条目
const banners = ref([])        // 首页轮播标语（驱动高级动态主题）
const hotTags = ref(['月老', '红妆', '姻缘簿', '花轿', '合卺酒', '往生']) // 兜底热门标签

// ---------- 搜索 / 分类筛选状态 ----------
const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const notice = ref('')
const demoMode = ref(false)

// 分级分类：选中的分类ID（null=全部）；展开的一级分类ID（用于显示二级）
const selectedCategoryId = ref(null)
const expandedTopId = ref(null)
const expandedTop = computed(() => categories.value.find(c => c.id === expandedTopId.value) || null)

// 结果区标题
const resultTitle = computed(() => {
  if (!searched.value) return '今日推荐 · 月老牵线'
  const cat = findCat(selectedCategoryId.value)
  const n = results.value.length
  if (keyword.value && cat) return `「${keyword.value}」于 ${cat.name} · 寻得 ${n} 段`
  if (keyword.value) return `「${keyword.value}」· 寻得 ${n} 段姻缘`
  if (cat) return `${cat.name} · ${n} 件藏品`
  return `寻得 ${n} 段姻缘`
})

onMounted(fetchHome)

async function fetchHome() {
  try {
    const res = await fetch(HOME_API)
    const json = await res.json()
    if (res.ok && json.code === 200 && json.data) {
      categories.value = json.data.categories || []
      featured.value = json.data.featured || []
      banners.value = Array.isArray(json.data.banners) ? json.data.banners : []
      if (Array.isArray(json.data.hotTags) && json.data.hotTags.length) {
        hotTags.value = json.data.hotTags
      }
    }
  } catch (e) {
    // 后端未起：保留兜底热门标签，首页仍可渲染
  }
}

// 在分类树中按 id 查找节点
function findCat(id) {
  if (id == null) return null
  const walk = (list) => {
    for (const c of list) {
      if (c.id === id) return c
      if (c.children && c.children.length) {
        const hit = walk(c.children)
        if (hit) return hit
      }
    }
    return null
  }
  return walk(categories.value)
}

// 导航点击：命中真实分类则直接筛选，否则提示未开放；首页则回到推荐
function onNav(item) {
  activeNav.value = item
  if (item === '首页') {
    clearCategory()
    return
  }
  const top = categories.value.find(c => c.name === item)
  if (top) {
    selectTop(top)
    return
  }
  flashToast(`「${item}」尚未开放，敬请期待～`)
}

// 选择一级分类：展开二级并触发该分类（含子孙）搜索
function selectTop(c) {
  selectedCategoryId.value = c.id
  expandedTopId.value = (expandedTopId.value === c.id) ? null : c.id
  onSearch('', c.id)
}

// 选择二级分类：仅该分类（含其子孙，若有）
function selectSub(s) {
  selectedCategoryId.value = s.id
  onSearch('', s.id)
}

// 清空分类：回到首页推荐
function clearCategory() {
  selectedCategoryId.value = null
  expandedTopId.value = null
  if (keyword.value.trim()) {
    onSearch(keyword.value, null)
  } else {
    searched.value = false
    results.value = []
    notice.value = ''
  }
}

// 搜索（支持关键词 + 分类）
async function onSearch(q, catId) {
  const kw = (q ?? keyword.value).trim()
  keyword.value = kw
  const cid = catId !== undefined ? catId : selectedCategoryId.value
  if (!kw && cid == null) {
    notice.value = '留下你想寻觅之物，或择一分类，月老方能牵线～'
    return
  }
  loading.value = true
  searched.value = true
  notice.value = ''
  demoMode.value = false
  results.value = []
  try {
    const params = new URLSearchParams()
    if (kw) params.set('keyword', kw)
    if (cid != null) params.set('categoryId', String(cid))
    const res = await fetch(`${SEARCH_API}?${params.toString()}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
    const json = await res.json()
    if (res.ok && json.code === 200) {
      results.value = Array.isArray(json.data) ? json.data : []
      if (!results.value.length) notice.value = json.message || '冥河寂寂，未见与此相关的姻缘。'
    } else {
      notice.value = json.message || '寻觅受阻，请稍后再试'
    }
  } catch (e) {
    // 后端未起：明确提示，并以「示例」卡片展示排版，绝不伪造真实数据
    notice.value = '冥府信道未通 · 后端搜索接口待实现。下列为示例展示，真实数据将由接口返回。'
    demoMode.value = true
    results.value = buildDemo(kw)
  } finally {
    loading.value = false
  }
}

// 后端未实现时的示例卡片（明确标注「示例」，便于后续替换为真实返回）
function buildDemo(kw) {
  return [
    { id: 1, emoji: '💞', title: `${kw || '姻缘'} · 前世姻缘`, desc: '示例展示 · 真实数据待后端接口返回', tag: '示例' },
    { id: 2, emoji: '🌹', title: `${kw || '红妆'} · 红妆信物`, desc: '示例展示 · 真实数据待后端接口返回', tag: '示例' },
    { id: 3, emoji: '🌙', title: `${kw || '月老'} · 月老题名`, desc: '示例展示 · 真实数据待后端接口返回', tag: '示例' }
  ]
}

function onHotTag(tag) {
  onSearch(tag, selectedCategoryId.value)
}
</script>

<template>
  <div class="home">
    <!-- ============ 导航栏 ============ -->
    <header class="nav">
      <button class="rail-toggle" type="button" @click="railOpen = !railOpen" aria-label="展开/收起导航">☰</button>
      <div class="brand">
        <span class="brand-ico">💕</span>
        <span class="brand-name">鬼娘花嫁</span>
        <span class="brand-sub">月夜婚礼</span>
      </div>

      <button class="nav-admin" type="button" @click="goAdmin" title="进入后台管理系统">
        <span class="admin-ico">🛡️</span> 后台管理
      </button>

      <button class="nav-profile" type="button" @click="goProfile" title="进入个人中心">
        <span class="profile-ico">💞</span> 个人中心
      </button>

      <div class="nav-user">
        <span class="user-chip" title="当前夫君">
          <span class="user-avatar">🌹</span>
          <span class="user-name">{{ userName }}</span>
        </span>
        <button class="logout" type="button" @click="onLogout">退 出</button>
      </div>
    </header>

    <!-- ============ 左侧导航侧边栏（描金玻璃滑轨） ============ -->
    <aside class="rail" :class="{ open: railOpen }" aria-label="侧边导航">
      <div class="rail-head">
        <button class="rail-grip" type="button" @click="railOpen = !railOpen" aria-label="收起/展开导航">{{ railOpen ? '✕' : '☰' }}</button>
        <span class="rail-brand">花嫁道</span>
      </div>
      <button
        v-for="item in navItems" :key="'r' + item" type="button"
        class="rail-item" :class="{ active: activeNav === item }"
        @click="goNav(item)"
      >
        <span class="ri-ico">{{ railIcons[item] || '💮' }}</span>
        <span class="ri-txt">{{ item }}</span>
      </button>
    </aside>

    <transition name="toast">
      <div v-if="pageToast" class="page-toast">{{ pageToast }}</div>
    </transition>

    <!-- ============ 英雄区 + 搜索栏 ============ -->
    <section class="hero">
      <HeroCrest class="hero-crest" :size="128" />
      <h1 class="hero-title">夫君，今夜想寻觅什么？</h1>
      <p class="hero-sub">月老在此执笔，为你牵起每一段尘缘</p>

      <BannerCarousel :banners="banners" />

      <form class="search" @submit.prevent="onSearch()" role="search">
        <span class="search-ico">🔍</span>
        <input
          v-model="keyword" type="text" maxlength="40"
          class="search-input"
          placeholder="寻人、寻物、寻一段往生姻缘…"
          aria-label="搜索"
          @input="notice = ''"
        />
        <button class="search-btn" type="submit" :disabled="loading">
          {{ loading ? '月老牵线中…' : '寻 觅' }}
        </button>
      </form>

      <!-- ============ 分级分类筛选 ============ -->
      <div class="cats" v-if="categories.length">
        <button
          class="cat-chip" :class="{ active: selectedCategoryId == null }"
          type="button" @click="clearCategory()"
        >全 部</button>
        <button
          v-for="c in categories" :key="c.id" type="button"
          class="cat-chip" :class="{ active: selectedCategoryId === c.id, open: expandedTopId === c.id }"
          @click="selectTop(c)"
        >{{ c.icon }} {{ c.name }}</button>
      </div>

      <transition name="sub">
        <div class="subcats" v-if="expandedTop && expandedTop.children && expandedTop.children.length">
          <button
            v-for="s in expandedTop.children" :key="s.id" type="button"
            class="sub-chip" :class="{ active: selectedCategoryId === s.id }"
            @click="selectSub(s)"
          >{{ s.icon }} {{ s.name }}</button>
        </div>
      </transition>

      <div class="hot">
        <span class="hot-label">热门搜寻</span>
        <button
          v-for="t in hotTags" :key="t" type="button"
          class="hot-tag" @click="onHotTag(t)"
        >{{ t }}</button>
      </div>
    </section>

    <!-- ============ 结果区 / 首页展示 ============ -->
    <section class="result">
      <h2 class="result-title" v-if="searched">{{ resultTitle }}</h2>
      <h2 class="result-title" v-else>今日推荐 · 月老牵线</h2>

      <p v-if="!searched && !featured.length && !loading" class="result-hint">
        在上方输入框寻觅，或择一分类 / 点选热门标签，月老即刻为你牵线。
      </p>

      <p v-if="notice" class="result-notice" :class="{ demo: demoMode }">{{ notice }}</p>

      <!-- 搜索结果 -->
      <div v-if="searched && results.length" class="cards">
        <article v-for="(r, i) in results" :key="r.id" class="card" v-reveal :style="{ transitionDelay: (i % 8) * 70 + 'ms' }">
          <div class="card-emoji">{{ r.emoji || '💮' }}</div>
          <h3 class="card-title">{{ r.title }}</h3>
          <p class="card-desc">{{ r.desc }}</p>
          <div class="card-foot">
            <span v-if="r.categoryName" class="card-cat">{{ r.categoryName }}</span>
            <span v-if="r.tag" class="card-tag">{{ r.tag }}</span>
          </div>
        </article>
      </div>

      <!-- 首页展示（推荐） -->
      <div v-if="!searched && featured.length" class="cards">
        <article v-for="(r, i) in featured" :key="r.id" class="card" v-reveal :style="{ transitionDelay: (i % 8) * 70 + 'ms' }">
          <div class="card-emoji">{{ r.emoji || '💮' }}</div>
          <h3 class="card-title">{{ r.title }}</h3>
          <p class="card-desc">{{ r.desc }}</p>
          <div class="card-foot">
            <span v-if="r.categoryName" class="card-cat">{{ r.categoryName }}</span>
            <span v-if="r.tag" class="card-tag">{{ r.tag }}</span>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* 让首页铺满视口、随内容滚动，避免被 body 的居中裁切 */
.home{
  align-self: stretch; justify-self: stretch;
  width:100%; min-height:100vh;
  display:flex; flex-direction:column;
  position:relative; z-index:5;
  padding-left:60px;
}
@media (max-width:720px){ .home{ padding-left:0; } }

/* ---------- 导航栏 ---------- */
.nav{
  position:sticky; top:0; z-index:20;
  display:flex; align-items:center; gap:18px;
  padding:12px clamp(16px,4vw,40px);
  background:linear-gradient(180deg, rgba(43,22,74,.86), rgba(43,22,74,.52));
  border-bottom:1px solid var(--hairline);
  backdrop-filter:blur(14px) saturate(140%); -webkit-backdrop-filter:blur(14px) saturate(140%);
  box-shadow:0 8px 28px rgba(20,8,40,.42), inset 0 1px 0 rgba(255,255,255,.10);
}
.brand{ display:flex; align-items:baseline; gap:8px; }
.brand-ico{ font-size:18px; filter:drop-shadow(0 2px 4px rgba(232,74,120,.5)); }
.brand-name{
  font-family:Georgia,"Songti SC","STSong","SimSun",serif;
  font-size:20px; letter-spacing:2px; color:var(--ivory);
  text-shadow:0 0 16px rgba(255,179,200,.5);
}
.brand-sub{ font-size:11px; letter-spacing:3px; color:var(--gold); opacity:.8; }

/* ---------- 左侧导航侧边栏（描金玻璃滑轨） ---------- */
.rail-toggle{
  display:grid; place-items:center; flex:0 0 auto;
  width:38px; height:38px; border-radius:12px; cursor:pointer;
  border:1px solid rgba(244,213,139,.5); background:rgba(255,247,251,.08);
  color:var(--gold); font-size:16px; transition:.2s var(--ease-out);
}
.rail-toggle:hover{ color:#fff; background:rgba(244,213,139,.18); box-shadow:0 0 14px rgba(244,213,139,.3); }
.nav-admin{ margin-left:auto; }

.rail{
  position:fixed; left:0; top:64px; bottom:0; z-index:19;
  width:216px; padding:14px 10px;
  display:flex; flex-direction:column; gap:6px;
  background:linear-gradient(180deg, rgba(43,22,74,.82), rgba(43,22,74,.5));
  border-right:1px solid var(--hairline);
  border-radius:0 18px 18px 0;
  backdrop-filter:blur(16px) saturate(140%); -webkit-backdrop-filter:blur(16px) saturate(140%);
  box-shadow:10px 0 34px rgba(13,4,32,.38);
  overflow:hidden;
  transform:translateX(calc(-100% + 60px));
  transition:transform .38s var(--ease-out);
}
.rail:hover,.rail:focus-within,.rail.open{ transform:none; }
.rail-head{ display:flex; align-items:center; gap:8px; margin-bottom:6px; padding-left:2px; }
.rail-grip{
  width:36px; height:36px; flex:0 0 36px; border-radius:12px; cursor:pointer;
  border:1px solid rgba(244,213,139,.5); background:rgba(255,247,251,.08);
  color:var(--gold); font-size:15px; transition:.2s;
}
.rail-grip:hover{ color:#fff; background:rgba(244,213,139,.18); }
.rail-brand{ font-size:12px; letter-spacing:3px; color:var(--gold); opacity:.85; white-space:nowrap; }
.rail-item{
  display:flex; align-items:center; gap:12px; width:100%;
  border:none; background:transparent; cursor:pointer;
  color:var(--rose); font-size:14px; letter-spacing:2px; font-family:inherit;
  padding:11px 12px; border-radius:14px; transition:.22s var(--ease-out);
  white-space:nowrap; text-align:left;
}
.rail-item .ri-ico{ font-size:17px; width:24px; text-align:center; filter:drop-shadow(0 2px 6px rgba(232,74,120,.4)); }
.rail-item:hover{ color:#fff; background:rgba(255,179,200,.14); }
.rail-item.active{
  color:#fff; background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 4px 14px rgba(232,74,120,.5), inset 0 1px 0 rgba(255,255,255,.3);
}
@media (max-width:720px){
  .rail{ top:0; z-index:40; width:min(240px,80vw); transform:translateX(-110%); border-radius:0; }
  .rail.open{ transform:none; box-shadow:20px 0 60px rgba(13,4,32,.6); }
}

.nav-user{ display:flex; align-items:center; gap:10px; }

/* ---------- 后台管理入口按钮 ---------- */
.nav-admin{
  position:relative; display:flex; align-items:center; gap:6px; overflow:hidden;
  border:1px solid rgba(155,107,214,.7); cursor:pointer;
  color:#fff; font-size:13px; letter-spacing:1px; font-family:inherit;
  padding:8px 16px; border-radius:999px; transition:.22s var(--ease-out); white-space:nowrap;
  background:linear-gradient(180deg, rgba(155,107,214,.46), rgba(106,58,160,.46));
  box-shadow:0 4px 14px rgba(106,58,160,.38), inset 0 1px 0 rgba(255,255,255,.18);
}
.nav-admin:hover{ color:#fff; background:linear-gradient(180deg,#9B6BD6,#6A3AA0); box-shadow:0 6px 18px rgba(106,58,160,.55); }
.nav-admin .admin-ico{ font-size:14px; }
.nav-admin::after{
  content:""; position:absolute; inset:0; border-radius:999px;
  background:var(--sheen); background-size:220% 100%;
  animation:shimmer 5s linear infinite; opacity:0; transition:opacity .25s;
}
.nav-admin:hover::after{ opacity:.3; }

/* ---------- 个人中心入口按钮（与后台管理相邻，改用主站粉金描边区分） ---------- */
.nav-profile{
  position:relative; display:flex; align-items:center; gap:6px; overflow:hidden;
  border:1px solid rgba(255,179,200,.75); cursor:pointer;
  color:#fff; font-size:13px; letter-spacing:1px; font-family:inherit;
  padding:8px 16px; border-radius:999px; transition:.22s var(--ease-out); white-space:nowrap;
  background:linear-gradient(180deg, rgba(255,143,180,.46), rgba(232,74,120,.46));
  box-shadow:0 4px 14px rgba(232,74,120,.38), inset 0 1px 0 rgba(255,255,255,.18);
}
.nav-profile:hover{ color:#fff; background:linear-gradient(180deg,#FF8FB4,#E84A78); box-shadow:0 6px 18px rgba(232,74,120,.55); }
.nav-profile .profile-ico{ font-size:14px; }
.nav-profile::after{
  content:""; position:absolute; inset:0; border-radius:999px;
  background:var(--sheen); background-size:220% 100%;
  animation:shimmer 5s linear infinite; opacity:0; transition:opacity .25s;
}
.nav-profile:hover::after{ opacity:.3; }
.user-chip{
  display:flex; align-items:center; gap:8px;
  padding:5px 12px 5px 6px; border-radius:999px;
  background:rgba(255,247,251,.12); border:1px solid rgba(255,179,200,.4);
}
.user-avatar{
  width:28px; height:28px; display:grid; place-items:center; border-radius:50%;
  background:linear-gradient(180deg,#FF8FB4,#E84A78); font-size:15px;
}
.user-name{ font-size:13px; color:var(--ivory); max-width:120px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.logout{
  border:1px solid rgba(255,179,200,.5); background:transparent; color:var(--rose);
  font-size:13px; letter-spacing:2px; padding:8px 16px; border-radius:999px; cursor:pointer; transition:.2s;
}
.logout:hover{ color:#fff; background:rgba(232,74,120,.25); }

.page-toast{
  position:fixed; left:50%; top:76px; transform:translateX(-50%); z-index:30;
  padding:9px 20px; border-radius:999px; font-size:13px; color:#fff;
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  box-shadow:0 8px 20px rgba(106,58,160,.5);
}
.toast-enter-active,.toast-leave-active{ transition:opacity .3s, transform .3s; }
.toast-enter-from,.toast-leave-to{ opacity:0; transform:translate(-50%,-10px); }

/* ---------- 英雄区 + 搜索栏 ---------- */
.hero{
  flex:0 0 auto; text-align:center;
  padding:clamp(40px,8vh,90px) 16px 24px;
}
.hero-crest{
  margin:0 auto 4px; display:block;
}
.hero-title{
  margin:6px 0 6px; font-size:clamp(24px,4.4vw,36px); letter-spacing:3px; line-height:1.25;
  font-family:Georgia,"Songti SC","STSong","SimSun",serif;
  background:linear-gradient(100deg,#FFE6F0 0%,#FFB3C8 30%,#F4D58B 56%,#FF8FB4 80%,#FFF7FB 100%);
  background-size:220% auto; -webkit-background-clip:text; background-clip:text; color:transparent;
  animation:shimmer 9s linear infinite;
  filter:drop-shadow(0 2px 18px rgba(255,179,200,.35));
}
.hero-sub{ margin:0 0 26px; font-size:13px; letter-spacing:5px; color:var(--gold); opacity:.9; text-shadow:0 0 14px rgba(244,213,139,.4); }

.search{
  display:flex; align-items:center; gap:10px;
  width:min(640px,92vw); margin:0 auto;
  padding:8px 8px 8px 18px;
  background:linear-gradient(180deg, rgba(255,247,251,.18), rgba(255,247,251,.08));
  border:2px solid var(--gold);
  border-radius:999px;
  backdrop-filter:blur(10px) saturate(140%); -webkit-backdrop-filter:blur(10px) saturate(140%);
  box-shadow:0 18px 46px rgba(20,8,40,.45), 0 0 0 6px rgba(255,179,200,.14), 0 0 30px rgba(244,213,139,.18);
  transition:box-shadow .3s var(--ease-out), border-color .3s;
}
.search:focus-within{ border-color:#FFE9B8; box-shadow:0 18px 46px rgba(20,8,40,.5), 0 0 0 6px rgba(255,179,200,.20), 0 0 44px rgba(244,213,139,.32); }
.search-ico{ font-size:18px; opacity:.7; }
.search-input{
  flex:1; min-width:0; border:none; outline:none; background:transparent;
  color:var(--ivory); font-size:15px; font-family:inherit; padding:10px 4px;
}
.search-input::placeholder{ color:rgba(255,230,240,.55); }
.search-btn{
  position:relative; overflow:hidden; flex:0 0 auto; border:none; cursor:pointer;
  padding:12px 26px; border-radius:999px;
  font-family:Georgia,"Songti SC",serif; font-size:15px; font-weight:700; letter-spacing:3px; color:#fff;
  background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 6px 0 #C93363, 0 12px 22px rgba(232,74,120,.45), inset 0 1px 0 rgba(255,255,255,.3);
  transition:transform .14s, filter .2s, box-shadow .14s;
}
.search-btn::after{
  content:""; position:absolute; top:0; left:-60%; width:50%; height:100%;
  background:var(--sheen); transform:skewX(-20deg); opacity:0; transition:left .6s ease, opacity .3s; pointer-events:none;
}
.search-btn:hover{ filter:brightness(1.05); transform:translateY(-2px); }
.search-btn:hover::after{ left:120%; opacity:.6; }
.search-btn:active{ transform:translateY(3px); box-shadow:0 2px 0 #C93363, 0 6px 12px rgba(232,74,120,.35); }
.search-btn:disabled{ cursor:default; filter:grayscale(.2); }

/* ---------- 分级分类筛选 ---------- */
.cats{
  display:flex; flex-wrap:wrap; gap:8px; justify-content:center; align-items:center;
  width:min(760px,94vw); margin:18px auto 0;
}
.cat-chip{
  border:1px solid rgba(244,213,139,.5); background:rgba(255,247,251,.08);
  color:var(--gold); font-size:13px; letter-spacing:1px;
  padding:7px 15px; border-radius:999px; cursor:pointer; transition:.2s var(--ease-out); font-family:inherit;
}
.cat-chip:hover{ color:#fff; background:rgba(244,213,139,.2); box-shadow:0 4px 12px rgba(231,184,90,.28); }
.cat-chip.active{
  color:#2b164a; font-weight:700;
  background:linear-gradient(180deg,#F4D58B,#E7B85A);
  border-color:var(--gold);
  box-shadow:0 4px 14px rgba(231,184,90,.5), inset 0 1px 0 rgba(255,255,255,.4);
}
.subcats{
  display:flex; flex-wrap:wrap; gap:7px; justify-content:center; align-items:center;
  width:min(640px,94vw); margin:10px auto 0;
}
.sub-chip{
  border:1px solid rgba(255,179,200,.4); background:rgba(255,247,251,.06);
  color:var(--rose); font-size:12.5px; letter-spacing:1px;
  padding:5px 13px; border-radius:999px; cursor:pointer; transition:.18s; font-family:inherit;
}
.sub-chip:hover{ color:#fff; background:rgba(255,111,156,.26); }
.sub-chip.active{ color:#fff; background:linear-gradient(180deg,#9B6BD6,#6A3AA0); border-color:#9B6BD6; }
.sub-enter-active,.sub-leave-active{ transition:opacity .25s, transform .25s; }
.sub-enter-from,.sub-leave-to{ opacity:0; transform:translateY(-6px); }

.hot{ display:flex; flex-wrap:wrap; gap:8px; justify-content:center; align-items:center; margin-top:16px; }
.hot-label{ font-size:12px; letter-spacing:2px; color:var(--rose); opacity:.8; }
.hot-tag{
  border:1px solid rgba(255,179,200,.45); background:rgba(255,247,251,.1);
  color:var(--rose); font-size:12.5px; letter-spacing:1px;
  padding:6px 14px; border-radius:999px; cursor:pointer; transition:.18s; font-family:inherit;
}
.hot-tag:hover{ color:#fff; background:rgba(255,111,156,.28); border-color:var(--pink); }

/* ---------- 结果区 / 首页展示 ---------- */
.result{ flex:1 1 auto; width:min(980px,92vw); margin:0 auto; padding:18px 0 60px; }
.result-title{
  text-align:center; margin:6px 0 18px;
  font-size:clamp(17px,2.4vw,22px); letter-spacing:2px;
  font-family:Georgia,"Songti SC","STSong","SimSun",serif;
  background:linear-gradient(100deg,#FFE6F0,#FFB3C8 40%,#F4D58B 75%,#FF8FB4);
  background-size:200% auto; -webkit-background-clip:text; background-clip:text; color:transparent;
  animation:shimmer 8s linear infinite;
  filter:drop-shadow(0 2px 14px rgba(255,179,200,.35));
}
.result-hint{ text-align:center; color:var(--rose); opacity:.7; font-size:13.5px; letter-spacing:1px; }
.result-notice{
  text-align:center; margin:8px auto 22px; max-width:680px;
  font-size:13px; letter-spacing:1px; line-height:1.7;
  color:var(--gold);
  padding:12px 18px; border-radius:14px;
  background:rgba(255,247,251,.08); border:1px dashed rgba(244,213,139,.5);
}
.result-notice.demo{ color:var(--rose); border-color:rgba(255,179,200,.5); }

.cards{
  display:grid; gap:18px;
  grid-template-columns:repeat(auto-fill, minmax(220px,1fr));
  perspective:1400px;
}
.card{
  position:relative; padding:24px 22px 26px; text-align:center;
  background:linear-gradient(160deg, rgba(255,253,254,.97) 0%, rgba(253,243,248,.93) 100%);
  border-radius:22px; overflow:hidden;
  box-shadow:var(--shadow-2), inset 0 0 0 1px rgba(255,179,200,.22), inset 0 1px 0 rgba(255,255,255,.7);
  transition:transform .4s var(--ease-out), box-shadow .4s var(--ease-out);
}
/* 悬停时旋转金粉描边 */
.card::before{
  content:""; position:absolute; inset:0; border-radius:inherit; padding:2px;
  background:conic-gradient(from var(--angle), transparent 0deg, var(--gold) 80deg, var(--pink) 160deg, transparent 250deg);
  -webkit-mask:linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite:xor; mask-composite:exclude;
  opacity:0; transition:opacity .4s;
}
/* 悬停高光扫过 */
.card::after{
  content:""; position:absolute; top:0; left:-60%; width:55%; height:100%;
  background:linear-gradient(120deg, transparent, rgba(255,255,255,.5), transparent);
  transform:skewX(-20deg); transition:left .7s ease; pointer-events:none;
}
.card:hover{
  transform:translateY(-8px) rotateX(3deg);
  box-shadow:0 34px 66px rgba(232,74,120,.42), 0 0 36px rgba(244,213,139,.30), 0 0 60px rgba(255,143,180,.28), inset 0 0 0 1px rgba(244,213,139,.35);
}
.card:hover::before{ opacity:1; animation:spinBorder 4s linear infinite; }
.card:hover::after{ left:120%; }
@property --angle{ syntax:'<angle>'; initial-value:0deg; inherits:false; }
@keyframes spinBorder{ to{ --angle:360deg; } }
.card-emoji{ font-size:30px; }
.card-title{
  margin:10px 0 6px; font-size:17px; letter-spacing:1px; color:var(--ink);
  font-family:Georgia,"Songti SC","STSong","SimSun",serif;
}
.card-desc{ margin:0 0 12px; font-size:12.5px; color:var(--ink-soft); line-height:1.6; }
.card-foot{ display:flex; gap:8px; justify-content:center; align-items:center; flex-wrap:wrap; }
.card-cat{
  display:inline-block; font-size:11px; letter-spacing:1px; color:#fff;
  padding:3px 12px; border-radius:999px;
  background:linear-gradient(180deg,#6A3AA0,#4A2680);
}
.card-tag{
  display:inline-block; font-size:11px; letter-spacing:1px; color:#fff;
  padding:3px 12px; border-radius:999px;
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
}

@media (max-width:680px){
  .nav{ flex-wrap:wrap; gap:10px; }
  .brand-sub{ display:none; }
}
</style>
