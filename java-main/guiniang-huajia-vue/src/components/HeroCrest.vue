<script setup>
// 高级装饰性徽记（替代幼稚人物）：旋转金环 + 八瓣花饰 + 辉光核心
// 纯几何纹样，无人物、无表情，营造「高级动态感」。
defineProps({
  size: { type: Number, default: 132 }
})
</script>

<template>
  <svg
    class="crest"
    :style="{ width: size + 'px', height: size + 'px' }"
    viewBox="0 0 200 200"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <defs>
      <radialGradient id="crestGlow" cx="50%" cy="50%" r="50%">
        <stop offset="0%" stop-color="#FFF4D6" stop-opacity=".9"/>
        <stop offset="55%" stop-color="#F4D58B" stop-opacity=".35"/>
        <stop offset="100%" stop-color="#F4D58B" stop-opacity="0"/>
      </radialGradient>
      <linearGradient id="crestGold" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#FCE6B0"/>
        <stop offset="45%" stop-color="#F4D58B"/>
        <stop offset="100%" stop-color="#E7B85A"/>
      </linearGradient>
      <linearGradient id="crestPink" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#FFB3C8"/>
        <stop offset="100%" stop-color="#E84A78"/>
      </linearGradient>
    </defs>

    <!-- 辉光核心（呼吸） -->
    <circle class="glow" cx="100" cy="100" r="92" fill="url(#crestGlow)"/>

    <!-- 外环：细金环 + 旋转刻度 -->
    <g class="ring-outer">
      <circle cx="100" cy="100" r="86" fill="none" stroke="url(#crestGold)" stroke-width="1.4" stroke-dasharray="2 7" opacity=".8"/>
      <circle cx="100" cy="100" r="78" fill="none" stroke="url(#crestGold)" stroke-width="1" opacity=".5"/>
    </g>

    <!-- 内环：小圆点，反向旋转 -->
    <g class="ring-dots">
      <circle v-for="n in 12" :key="n"
        :cx="100 + 70 * Math.cos((n*30 - 90) * Math.PI/180)"
        :cy="100 + 70 * Math.sin((n*30 - 90) * Math.PI/180)"
        r="2.4" fill="url(#crestGold)"/>
    </g>

    <!-- 八瓣花饰（缓慢旋转） -->
    <g class="petals">
      <g v-for="p in 8" :key="'p'+p" :transform="`rotate(${p*45} 100 100)`">
        <path d="M100 44 C108 60 108 74 100 86 C92 74 92 60 100 44 Z"
              fill="url(#crestPink)" opacity=".9"/>
      </g>
    </g>

    <!-- 核心：双环交叠（婚契意象，几何抽象） + 中心宝石 -->
    <g class="core">
      <circle cx="92" cy="100" r="26" fill="none" stroke="url(#crestGold)" stroke-width="2.4"/>
      <circle cx="108" cy="100" r="26" fill="none" stroke="url(#crestPink)" stroke-width="2.4"/>
      <circle cx="100" cy="100" r="9" fill="url(#crestGold)"/>
      <circle cx="100" cy="100" r="9" fill="none" stroke="#fff" stroke-opacity=".7" stroke-width="1"/>
    </g>
  </svg>
</template>

<style scoped>
.crest{
  display:block; margin:0 auto;
  filter:drop-shadow(0 10px 22px rgba(232,74,120,.35));
  animation:floaty 6s ease-in-out infinite;
}
@keyframes floaty{ 0%,100%{ transform:translateY(0); } 50%{ transform:translateY(-8px); } }

.glow{ transform-box:fill-box; transform-origin:center; animation:breathe 4.5s ease-in-out infinite; }
@keyframes breathe{ 0%,100%{ opacity:.55; transform:scale(.96);} 50%{ opacity:1; transform:scale(1.06);} }

.ring-outer{ transform-box:fill-box; transform-origin:center; animation:spin 38s linear infinite; }
.ring-dots{ transform-box:fill-box; transform-origin:center; animation:spin 26s linear infinite reverse; }
.petals{ transform-box:fill-box; transform-origin:center; animation:spin 60s linear infinite; }
.core{ transform-box:fill-box; transform-origin:center; animation:spin 90s linear infinite; }

@keyframes spin{ to{ transform:rotate(360deg); } }
</style>
