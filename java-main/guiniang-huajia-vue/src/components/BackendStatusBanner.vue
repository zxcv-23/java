<script setup>
import { ref, onMounted } from 'vue'

// 后端连通性探测：启动后请求 /api/home，失败则在页面顶部挂出横幅提示
const API_BASE = 'http://127.0.0.1:8081'
const down = ref(false)
const dismissed = ref(false)

onMounted(async () => {
  try {
    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), 4000)
    const res = await fetch(API_BASE + '/api/home', { signal: controller.signal })
    clearTimeout(timer)
    down.value = !(res.ok)
  } catch (e) {
    down.value = true
  }
})
</script>

<template>
  <transition name="banner">
    <div v-if="down && !dismissed" class="backend-banner" role="alert">
      <span class="bb-ico">🕯️</span>
      <span class="bb-text">冥府信道未通 · 后端服务未启动，部分功能暂不可用（请先运行 springBootTest 后端）</span>
      <button class="bb-close" type="button" @click="dismissed = true" aria-label="关闭提示">✕</button>
    </div>
  </transition>
</template>

<style scoped>
.backend-banner{
  position:fixed; left:50%; top:10px; transform:translateX(-50%); z-index:60;
  display:flex; align-items:center; gap:10px;
  max-width:min(860px, 94vw);
  padding:9px 14px 9px 16px; border-radius:999px;
  background:linear-gradient(180deg, rgba(106,58,160,.92), rgba(58,28,110,.92));
  border:1px solid rgba(244,213,139,.45);
  color:#FFE9F3; font-size:12.5px; letter-spacing:1px;
  box-shadow:0 10px 30px rgba(13,4,32,.5), 0 0 24px rgba(155,107,214,.35);
  backdrop-filter:blur(10px); -webkit-backdrop-filter:blur(10px);
}
.bb-ico{ font-size:15px; }
.bb-text{ line-height:1.5; }
.bb-close{
  flex:0 0 auto; border:none; background:transparent; cursor:pointer;
  color:rgba(255,233,243,.7); font-size:13px; padding:4px 6px; border-radius:50%;
  transition:.2s;
}
.bb-close:hover{ color:#fff; background:rgba(255,255,255,.14); }
.banner-enter-active,.banner-leave-active{ transition:opacity .4s, transform .4s var(--ease-out); }
.banner-enter-from,.banner-leave-to{ opacity:0; transform:translate(-50%,-14px); }
</style>
