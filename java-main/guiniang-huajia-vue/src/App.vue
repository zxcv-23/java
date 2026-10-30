<script setup>
import { ref, onMounted } from 'vue'
import AnimeBackground from './components/AnimeBackground.vue'
import LoginCard from './components/LoginCard.vue'
import RegisterCard from './components/RegisterCard.vue'
import HeartLayer from './components/HeartLayer.vue'
import HomePage from './components/HomePage.vue'
import AdminLogin from './components/AdminLogin.vue'
import AdminDashboard from './components/AdminDashboard.vue'
import PersonalCenter from './components/PersonalCenter.vue'
import BackendStatusBanner from './components/BackendStatusBanner.vue'

const heartLayer = ref(null)
// 模式：login | register | home | admin-login | admin
const mode = ref('login')
const currentUser = ref(null)
const currentAdmin = ref(null)

// 读取已存储的前台用户（「来世也要记得我」存 localStorage，否则 sessionStorage）
function readStoredUser() {
  try {
    const raw =
      window.localStorage.getItem('guiniang_user') ||
      window.sessionStorage.getItem('guiniang_user')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

// 读取已存储的后台管理员（含 token）
function readStoredAdmin() {
  try {
    const raw =
      window.localStorage.getItem('guiniang_admin') ||
      window.sessionStorage.getItem('guiniang_admin')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

// 已登录则直接进入首页（刷新不丢会话）
onMounted(() => {
  const u = readStoredUser()
  if (u) {
    currentUser.value = u
    mode.value = 'home'
  }
})

// 登录/注册成功后：子组件抛出 celebrate 事件（携带卡片位置），交给爱心雨图层
function onCelebrate(rect) {
  heartLayer.value && heartLayer.value.rain(rect)
}

// 退出前台登录 → 清除会话并返回登录页
function logout() {
  window.localStorage.removeItem('guiniang_user')
  window.sessionStorage.removeItem('guiniang_user')
  currentUser.value = null
  pendingProfile = false
  mode.value = 'login'
}

// ---------- 后台管理路由 ----------
// 从首页导航栏「后台管理」进入：已有有效 token 直接进后台，否则进登录页
function goAdmin() {
  const a = readStoredAdmin()
  if (a && a.token) {
    currentAdmin.value = a
    mode.value = 'admin'
  } else {
    mode.value = 'admin-login'
  }
}

// ---------- 个人中心路由 ----------
// 未登录时点了「个人中心」→ 先去登录，登录成功后自动回到个人资料编辑页
let pendingProfile = false

// 从首页导航栏「个人中心」进入：已登录直接进编辑页，否则先去登录页
function goProfile() {
  if (currentUser.value && currentUser.value.token) {
    pendingProfile = false
    mode.value = 'profile'
  } else {
    pendingProfile = true
    mode.value = 'login'
  }
}

// 个人资料编辑页里登录失效 → 回登录页，登录成功后仍回到编辑页
function profileNeedLogin() {
  pendingProfile = true
  mode.value = 'login'
}

// 登录/注册成功 → 短暂展示爱心雨后跳转首页
let homeTimer = null
function goHome() {
  currentUser.value = readStoredUser()
  clearTimeout(homeTimer)
  homeTimer = setTimeout(() => {
    // 若是从「个人中心」被引导来登录的，登录后直接回编辑页
    if (pendingProfile && currentUser.value && currentUser.value.token) {
      pendingProfile = false
      mode.value = 'profile'
    } else {
      mode.value = 'home'
    }
  }, 1500)
}

// 个人中心改密后后端返回新 token，同步刷新本地登录态（沿用原存储位置）
function refreshUserToken(newToken) {
  const next = { ...(currentUser.value || {}), token: newToken }
  currentUser.value = next
  try {
    const raw = window.localStorage.getItem('guiniang_user')
    if (raw) {
      window.localStorage.setItem('guiniang_user', JSON.stringify(next))
      return
    }
    const sess = window.sessionStorage.getItem('guiniang_user')
    if (sess) window.sessionStorage.setItem('guiniang_user', JSON.stringify(next))
  } catch { /* 忽略持久化异常 */ }
}

// 后台登录成功 → 进入后台主界面
function goAdminHome(admin) {
  currentAdmin.value = admin
  mode.value = 'admin'
}

// 退出后台 → 清除后台会话，回到后台登录页
function adminLogout() {
  window.localStorage.removeItem('guiniang_admin')
  window.sessionStorage.removeItem('guiniang_admin')
  currentAdmin.value = null
  mode.value = 'admin-login'
}

// 返回前台首页（保留前后台会话）
function backToHome() {
  mode.value = 'home'
}
</script>

<template>
  <AnimeBackground />
  <BackendStatusBanner />
  <div v-if="mode === 'login' || mode === 'register'" class="switch" role="tablist">
    <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">缔 结 婚 约</button>
    <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">录 入 新 生</button>
  </div>
  <transition name="swap" mode="out-in">
    <LoginCard v-if="mode === 'login'" key="login" @celebrate="onCelebrate" @switch="mode = 'register'" @loggedIn="goHome" />
    <RegisterCard v-else-if="mode === 'register'" key="register" @celebrate="onCelebrate" @switch="mode = 'login'" @loggedIn="goHome" />
    <HomePage v-else-if="mode === 'home'" key="home" :user="currentUser" @logout="logout" @goAdmin="goAdmin" @goProfile="goProfile" />
    <AdminLogin v-else-if="mode === 'admin-login'" key="admin-login" @loggedIn="goAdminHome" @back="backToHome" />
    <AdminDashboard v-else-if="mode === 'admin'" key="admin" :admin="currentAdmin" @logout="adminLogout" @back="backToHome" />
    <PersonalCenter v-else-if="mode === 'profile'" key="profile" :user="currentUser" @back="backToHome" @logout="logout" @tokenRefreshed="refreshUserToken" @needLogin="profileNeedLogin" />
  </transition>
  <HeartLayer ref="heartLayer" />
</template>

<style scoped>
/* ---------- 登录 / 注册 分段切换（描金玻璃胶囊） ---------- */
.switch{
  position:relative; z-index:5; display:flex; gap:4px; padding:6px; margin-bottom:14px;
  background:linear-gradient(180deg, rgba(255,247,251,.16), rgba(255,247,251,.07));
  border:1px solid var(--hairline);
  border-radius:999px;
  backdrop-filter:blur(10px) saturate(140%); -webkit-backdrop-filter:blur(10px) saturate(140%);
  box-shadow:var(--shadow-1), inset 0 1px 0 rgba(255,255,255,.18);
}
.switch button{
  position:relative; border:none; background:transparent; color:#FFE6F0; font-size:13.5px; letter-spacing:2px;
  padding:9px 22px; border-radius:999px; cursor:pointer; transition:.25s var(--ease-out); font-family:inherit;
}
.switch button:hover{ color:#fff; }
.switch button.active{
  color:#fff;
  background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 6px 16px rgba(232,74,120,.5), inset 0 1px 0 rgba(255,255,255,.35);
}
.switch button.active::after{
  content:""; position:absolute; inset:0; border-radius:999px;
  background:var(--sheen); background-size:220% 100%;
  animation:shimmer 4s linear infinite; opacity:.35; pointer-events:none;
}
.swap-enter-active, .swap-leave-active{ transition:opacity .35s ease, transform .35s var(--ease-out), filter .35s ease; }
.swap-enter-from{ opacity:0; transform:translateY(16px) scale(.98); filter:blur(6px); }
.swap-leave-to{ opacity:0; transform:translateY(-16px) scale(.98); filter:blur(6px); }
</style>
