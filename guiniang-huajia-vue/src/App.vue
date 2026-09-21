<script setup>
import { ref } from 'vue'
import NightSky from './components/NightSky.vue'
import LoginCard from './components/LoginCard.vue'
import HeartLayer from './components/HeartLayer.vue'

const heartLayer = ref(null)

// 登录成功后：LoginCard 抛出 celebrate 事件（携带卡片位置），交给爱心雨图层
function onCelebrate(rect) {
  heartLayer.value && heartLayer.value.rain(rect)
}
</script>

<template>
  <div class="moon" aria-hidden="true"></div>
  <NightSky />
  <LoginCard @celebrate="onCelebrate" />
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
</style>
