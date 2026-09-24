<script setup>
import { ref } from 'vue'
import NightSky from './components/NightSky.vue'
import LoginCard from './components/LoginCard.vue'
import RegisterCard from './components/RegisterCard.vue'
import HeartLayer from './components/HeartLayer.vue'

const heartLayer = ref(null)
const mode = ref('login') // 'login' | 'register'

// 登录/注册成功后：子组件抛出 celebrate 事件（携带卡片位置），交给爱心雨图层
function onCelebrate(rect) {
  heartLayer.value && heartLayer.value.rain(rect)
}
</script>

<template>
  <div class="moon" aria-hidden="true"></div>
  <NightSky />
  <div class="switch" role="tablist">
    <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">缔 结 婚 约</button>
    <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">录 入 新 生</button>
  </div>
  <transition name="swap" mode="out-in">
    <LoginCard v-if="mode === 'login'" key="login" @celebrate="onCelebrate" @switch="mode = 'register'" />
    <RegisterCard v-else key="register" @celebrate="onCelebrate" @switch="mode = 'login'" />
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
