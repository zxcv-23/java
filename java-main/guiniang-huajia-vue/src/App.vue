<script setup>
import { ref, onMounted } from 'vue'
import NightSky from './components/NightSky.vue'
import AuroraBackdrop from './components/AuroraBackdrop.vue'
import LoginCard from './components/LoginCard.vue'
import RegisterCard from './components/RegisterCard.vue'
import HeartLayer from './components/HeartLayer.vue'
import HomePage from './components/HomePage.vue'

const heartLayer = ref(null)
const mode = ref('login') // 'login' | 'register' | 'home'
const currentUser = ref(null)

// 读取已存储的用户（「来世也要记得我」存 localStorage，否则 sessionStorage）
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

// 退出登录 → 清除会话并返回登录页
function logout() {
  window.localStorage.removeItem('guiniang_user')
  window.sessionStorage.removeItem('guiniang_user')
  currentUser.value = null
  mode.value = 'login'
}
</script>

<template>
  <AuroraBackdrop />
  <div class="moon" aria-hidden="true"></div>
  <NightSky />
  <div v-if="mode !== 'home'" class="switch" role="tablist">
    <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">缔 结 婚 约</button>
    <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">录 入 新 生</button>
  </div>
  <transition name="swap" mode="out-in">
    <LoginCard v-if="mode === 'login'" key="login" @celebrate="onCelebrate" @switch="mode = 'register'" @loggedIn="goHome" />
    <RegisterCard v-else-if="mode === 'register'" key="register" @celebrate="onCelebrate" @switch="mode = 'login'" @loggedIn="goHome" />
    <HomePage v-else key="home" :user="currentUser" @logout="logout" />
  </transition>
  <HeartLayer ref="heartLayer" />
</template>

<style scoped>
/* ---------- 月亮 ---------- */
.moon{
  position:fixed; left:50%; top:5%; transform:translateX(-50%);
  width:180px; height:180px; border-radius:50%;
  background:radial-gradient(circle at 38% 36%, #FFF6FB 0%, #F7D9EA 55%, #E9B9D6 100%);
  box-shadow:0 0 70px 24px rgba(247,200,230,.45), 0 0 160px 50px rgba(190,120,180,.25);
  z-index:1;
  animation:moonPulse 9s ease-in-out infinite;
}
@keyframes moonPulse{
  0%,100%{ box-shadow:0 0 70px 24px rgba(247,200,230,.4), 0 0 160px 50px rgba(190,120,180,.22); }
  50%{ box-shadow:0 0 90px 30px rgba(247,200,230,.55), 0 0 200px 64px rgba(190,120,180,.32); }
}
.moon::after{
  content:""; position:absolute; right:30px; top:34px; width:34px; height:34px; border-radius:50%;
  background:rgba(200,150,190,.28);
  box-shadow:-46px 36px 0 -6px rgba(200,150,190,.22), 28px 60px 0 -12px rgba(200,150,190,.2);
}

/* ---------- 登录 / 注册 分段切换 ---------- */
.switch{
  position:relative; z-index:5; display:flex; gap:4px; padding:5px; margin-bottom:14px;
  background:rgba(255,247,251,.14); border:1px solid rgba(255,179,200,.4); border-radius:999px;
  backdrop-filter:blur(6px); -webkit-backdrop-filter:blur(6px);
}
.switch button{
  border:none; background:transparent; color:#FFE6F0; font-size:13.5px; letter-spacing:2px;
  padding:9px 20px; border-radius:999px; cursor:pointer; transition:.2s; font-family:inherit;
}
.switch button.active{
  background:linear-gradient(180deg,#FF8FB4,#E84A78); color:#fff;
  box-shadow:0 4px 12px rgba(232,74,120,.5);
}
.swap-enter-active, .swap-leave-active{ transition:opacity .3s ease, transform .3s ease; }
.swap-enter-from{ opacity:0; transform:translateY(14px) scale(.98); }
.swap-leave-to{ opacity:0; transform:translateY(-14px) scale(.98); }
</style>
