<script setup>
import { ref, onBeforeUnmount } from 'vue'

// ---------- 全屏爱心雨：数据驱动 v-for，替代原来的 createElement + setTimeout remove ----------
const icons = ['💗', '💞', '💕', '🌸', '✨', '🌹', '💍']
const hearts = ref([])
let uid = 0
const timers = []

// box：LoginCard 卡片的 getBoundingClientRect()，粒子从卡片区域升起
function rain(box) {
  for (let i = 0; i < 22; i++) {
    const id = ++uid
    hearts.value.push({
      id,
      icon: icons[Math.floor(Math.random() * icons.length)],
      left: box.left + Math.random() * box.width,
      top: box.top + box.height * (0.3 + Math.random() * 0.45),
      delay: i * 0.045,
      size: 14 + Math.random() * 16,
    })
    timers.push(setTimeout(() => {
      hearts.value = hearts.value.filter(h => h.id !== id)
    }, 2600 + i * 60))
  }
}

onBeforeUnmount(() => timers.forEach(clearTimeout))

defineExpose({ rain })
</script>

<template>
  <div class="hearts" aria-hidden="true">
    <span
      v-for="h in hearts" :key="h.id" class="heart-fx"
      :style="{ left: h.left + 'px', top: h.top + 'px', animationDelay: h.delay + 's', fontSize: h.size + 'px' }"
    >{{ h.icon }}</span>
  </div>
</template>

<style scoped>
.hearts{ position:fixed; inset:0; pointer-events:none; z-index:9; overflow:hidden; }
.heart-fx{ position:absolute; font-size:20px; animation:floatUp 1.7s ease-out forwards; }
@keyframes floatUp{
  0%{ opacity:0; transform:translateY(0) scale(.4) rotate(0);}
  25%{ opacity:1;}
  100%{ opacity:0; transform:translateY(-210px) scale(1.3) rotate(180deg);}
}
</style>
