<script setup>
import { ref, onMounted } from 'vue'
import NightSky from './components/NightSky.vue'
import AuroraBackdrop from './components/AuroraBackdrop.vue'
import LoginCard from './components/LoginCard.vue'
import RegisterCard from './components/RegisterCard.vue'
import HeartLayer from './components/HeartLayer.vue'
import HomePage from './components/HomePage.vue'
import AdminLogin from './components/AdminLogin.vue'
import AdminDashboard from './components/AdminDashboard.vue'
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

// 登录/注册成功 → 短暂展示爱心雨后跳转首页
let homeTimer = null
function goHome() {
  currentUser.value = readStoredUser()
  clearTimeout(homeTimer)
  homeTimer = setTimeout(() => {
    mode.value = 'home'
  }, 1500)
}

// 退出前台登录 → 清除会话并返回登录页
function logout() {
  window.localStorage.removeItem('guiniang_user')
  window.sessionStorage.removeItem('guiniang_user')
  currentUser.value = null
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
  <AuroraBackdrop />
  <BackendStatusBanner />
  <div class="moon" aria-hidden="true"><span class="moon-ring"></span></div>
  <NightSky />
  <div v-if="mode === 'login' || mode === 'register'" class="switch" role="tablist">
    <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">缔 结 婚 约</button>
    <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">录 入 新 生</button>
  </div>
  <transition name="swap" mode="out-in">
    <LoginCard v-if="mode === 'login'" key="login" @celebrate="onCelebrate" @switch="mode = 'register'" @loggedIn="goHome" />
    <RegisterCard v-else-if="mode === 'register'" key="register" @celebrate="onCelebrate" @switch="mode = 'login'" @loggedIn="goHome" />
    <HomePage v-else-if="mode === 'home'" key="home" :user="currentUser" @logout="logout" @goAdmin="goAdmin" />
    <AdminLogin v-else-if="mode === 'admin-login'" key="admin-login" @loggedIn="goAdminHome" @back="backToHome" />
    <AdminDashboard v-else-if="mode === 'admin'" key="admin" :admin="currentAdmin" @logout="adminLogout" @back="backToHome" />
  </transition>
  <HeartLayer ref="heartLayer" />
</template>

<style scoped>
/* ---------- 月亮 ---------- */
.moon{
  position:fixed; left:50%; top:5%; transform:translateX(-50%);
  width:180px; height:180px; border-radius:50%;
  background:radial-gradient(circle at 38% 34%, #FFF8FC 0%, #F7D9EA 52%, #E9B9D6 100%);
  box-shadow:0 0 80px 26px rgba(247,200,230,.42), 0 0 180px 56px rgba(190,120,180,.22), inset -18px -22px 48px rgba(170,110,165,.30);
  z-index:1;
  animation:moonPulse 9s ease-in-out infinite;
}
@keyframes moonPulse{
  0%,100%{ box-shadow:0 0 70px 24px rgba(247,200,230,.40), 0 0 170px 52px rgba(190,120,180,.20), inset -18px -22px 48px rgba(170,110,165,.28); }
  50%{ box-shadow:0 0 96px 32px rgba(247,200,230,.55), 0 0 210px 66px rgba(190,120,180,.32), inset -18px -22px 48px rgba(170,110,165,.34); }
}
.moon::after{
  content:""; position:absolute; right:30px; top:34px; width:34px; height:34px; border-radius:50%;
  background:rgba(200,150,190,.28);
  box-shadow:-46px 36px 0 -6px rgba(200,150,190,.22), 28px 60px 0 -12px rgba(200,150,190,.2);
}
/* 描金月环：缓缓旋转的金丝 + 虚线，营造月夜婚礼的华美 */
.moon-ring{
  position:absolute; inset:-36px; z-index:-1; border-radius:50%;
  border:1px solid rgba(244,213,139,.24);
  box-shadow:0 0 46px 8px rgba(244,213,139,.10), inset 0 0 38px rgba(244,213,139,.10);
  animation:ringSpin 70s linear infinite;
}
.moon-ring::before{
  content:""; position:absolute; inset:16px; border-radius:50%;
  border:1px dashed rgba(255,179,200,.30);
}
@keyframes ringSpin{ to{ transform:rotate(360deg); } }

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
