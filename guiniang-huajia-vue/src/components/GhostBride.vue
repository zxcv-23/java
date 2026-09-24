<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

// happy：登录成功后害羞脸红（父组件通过 props 传入）
defineProps({ happy: { type: Boolean, default: false } })

// ---------- 瞳孔跟随鼠标 ----------
const pupilA = ref(null)
const pupilB = ref(null)
const lookA = ref({ transform: 'translate(0px, 0px)' })
const lookB = ref({ transform: 'translate(0px, 0px)' })

function onMove(e) {
  ;[[pupilA.value, lookA], [pupilB.value, lookB]].forEach(([el, target]) => {
    if (!el) return
    const r = el.getBoundingClientRect()
    const dx = e.clientX - (r.left + r.width / 2)
    const dy = e.clientY - (r.top + r.height / 2)
    const dist = Math.hypot(dx, dy)
    const ang = Math.atan2(dy, dx)
    const k = Math.min(dist, 220) / 220
    target.value = {
      transform: `translate(${(Math.cos(ang) * 4 * k).toFixed(2)}px, ${(Math.sin(ang) * 4 * k).toFixed(2)}px)`,
    }
  })
}

onMounted(() => document.addEventListener('mousemove', onMove))
onBeforeUnmount(() => document.removeEventListener('mousemove', onMove))
</script>

<template>
  <!-- ============ 鬼娘花嫁 ============ -->
  <svg class="mascot" :class="{ happy }" viewBox="0 0 240 300" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
    <defs>
      <radialGradient id="skin" cx="0.4" cy="0.35" r="0.8">
        <stop offset="0" stop-color="#FFF4F8"/><stop offset="1" stop-color="#F7DCE8"/>
      </radialGradient>
      <linearGradient id="hair" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#F2ECFB"/><stop offset="1" stop-color="#D6CBEE"/>
      </linearGradient>
      <linearGradient id="horn" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#FF92B4"/><stop offset="1" stop-color="#E84A78"/>
      </linearGradient>
      <radialGradient id="eye" cx="0.5" cy="0.4" r="0.7">
        <stop offset="0" stop-color="#FFC2D6"/><stop offset="0.7" stop-color="#D85A8E"/><stop offset="1" stop-color="#9E2E63"/>
      </radialGradient>
      <linearGradient id="dress" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#FFFFFF"/><stop offset="1" stop-color="#F3E9F5"/>
      </linearGradient>
    </defs>

    <!-- 影子 -->
    <ellipse cx="120" cy="288" rx="58" ry="8" fill="#0E0620" opacity="0.3"/>

    <g class="hair">
      <!-- 头发后层 -->
      <path d="M120 36 C66 36 50 76 56 128 C60 176 44 214 52 256 C72 260 82 236 86 206 C90 240 112 256 120 226 C128 256 150 240 154 206 C158 236 168 260 188 256 C196 214 180 176 184 128 C190 76 174 36 120 36 Z" fill="url(#hair)"/>
    </g>

    <!-- 头纱（半透明） -->
    <path d="M70 70 C60 120 58 170 64 214 C90 206 96 170 100 140 C112 170 128 170 140 140 C144 170 150 206 176 214 C182 170 180 120 170 70 C150 50 90 50 70 70 Z"
          fill="#FFFFFF" opacity="0.4"/>

    <!-- 脸 -->
    <ellipse cx="120" cy="96" rx="50" ry="48" fill="url(#skin)"/>

    <!-- 刘海 -->
    <path d="M74 80 C92 58 148 58 166 80 C152 66 134 84 120 74 C106 84 88 66 74 80 Z" fill="url(#hair)"/>
    <path d="M74 80 C84 70 96 76 100 86 C90 84 82 86 78 92 Z" fill="#C9BCE8" opacity="0.6"/>
    <path d="M166 80 C156 70 144 76 140 86 C150 84 158 86 162 92 Z" fill="#C9BCE8" opacity="0.6"/>

    <!-- 鬼角 -->
    <path d="M96 54 C86 40 80 28 76 14 C86 24 96 38 106 48 Z" fill="url(#horn)" stroke="#C93363" stroke-width="1.2"/>
    <path d="M144 54 C154 40 160 28 164 14 C154 24 144 38 134 48 Z" fill="url(#horn)" stroke="#C93363" stroke-width="1.2"/>

    <!-- 头花/冠 -->
    <circle cx="120" cy="42" r="7" fill="#FF8FB4"/>
    <circle cx="108" cy="46" r="4.5" fill="#FFB3C8"/>
    <circle cx="132" cy="46" r="4.5" fill="#FFB3C8"/>
    <circle cx="120" cy="42" r="2.4" fill="#F4D58B"/>

    <!-- 腮红（默认隐藏，成功显示） -->
    <ellipse class="blush" cx="86" cy="112" rx="11" ry="6" fill="#FF8FB4"/>
    <ellipse class="blush" cx="154" cy="112" rx="11" ry="6" fill="#FF8FB4"/>

    <!-- 眼睛 -->
    <g>
      <ellipse class="iris" cx="100" cy="94" rx="13" ry="16" fill="url(#eye)" stroke="#7A2350" stroke-width="1.4"/>
      <ellipse cx="100" cy="100" rx="8" ry="9" fill="#FFE3EE" opacity="0.45"/>
      <circle ref="pupilA" class="pupil" cx="100" cy="94" r="5.4" fill="#3A1226" :style="lookA"/>
      <circle cx="97" cy="90" r="2.1" fill="#fff"/>
      <ellipse class="eyelid" cx="100" cy="94" rx="13.5" ry="16.5" fill="#F7DCE8"/>
    </g>
    <g>
      <ellipse class="iris" cx="140" cy="94" rx="13" ry="16" fill="url(#eye)" stroke="#7A2350" stroke-width="1.4"/>
      <ellipse cx="140" cy="100" rx="8" ry="9" fill="#FFE3EE" opacity="0.45"/>
      <circle ref="pupilB" class="pupil" cx="140" cy="94" r="5.4" fill="#3A1226" :style="lookB"/>
      <circle cx="137" cy="90" r="2.1" fill="#fff"/>
      <ellipse class="eyelid" cx="140" cy="94" rx="13.5" ry="16.5" fill="#F7DCE8"/>
    </g>

    <!-- 嘴 + 小虎牙 -->
    <path d="M112 120 Q120 128 128 120" fill="none" stroke="#9E2E63" stroke-width="2.4" stroke-linecap="round"/>
    <path d="M114 121 l2.4 0 l-1.2 4 Z" fill="#fff" stroke="#D8A" stroke-width="0.4"/>
    <path d="M126 121 l-2.4 0 l1.2 4 Z" fill="#fff" stroke="#D8A" stroke-width="0.4"/>

    <!-- 婚纱 -->
    <path d="M90 138 C78 160 68 206 60 282 L180 282 C172 206 162 160 150 138 C140 152 100 152 90 138 Z"
          fill="url(#dress)" stroke="#E9D7EC" stroke-width="1.5"/>
    <!-- 领口 -->
    <path d="M90 138 C104 150 136 150 150 138 C140 156 100 156 90 138 Z" fill="#FFE3EE"/>
    <!-- 胸前蝴蝶结 -->
    <path d="M120 150 l-16 -8 v16 Z" fill="#FF8FB4"/>
    <path d="M120 150 l16 -8 v16 Z" fill="#FF8FB4"/>
    <circle cx="120" cy="150" r="4" fill="#E84A78"/>
    <!-- 裙摆荷叶边 -->
    <path d="M60 282 q15 -14 30 0 q15 -14 30 0 q15 -14 30 0 q15 -14 30 0" fill="none" stroke="#E9D7EC" stroke-width="2"/>
    <!-- 裙身竖褶 -->
    <path d="M96 152 C92 200 88 240 84 278" fill="none" stroke="#EFE2F1" stroke-width="1.4"/>
    <path d="M120 154 L120 280" fill="none" stroke="#EFE2F1" stroke-width="1.4"/>
    <path d="M144 152 C148 200 152 240 156 278" fill="none" stroke="#EFE2F1" stroke-width="1.4"/>
  </svg>
  <!-- ========== /鬼娘花嫁 ========== -->
</template>

<style scoped>
.mascot{
  width:180px; height:auto; margin:2px auto 0; display:block;
  animation:float 4.6s ease-in-out infinite;
  filter:drop-shadow(0 14px 16px rgba(30,10,50,.4));
}
@keyframes float{ 0%,100%{ transform:translateY(0) rotate(-1deg);} 50%{ transform:translateY(-9px) rotate(1deg);} }
.mascot.happy{ animation:blush 1s ease-in-out 2; }
@keyframes blush{ 0%,100%{ transform:scale(1) rotate(0);} 30%{ transform:scale(1.07) rotate(-3deg);} 65%{ transform:scale(1.07) rotate(3deg);} }

.hair{ transform-box:fill-box; transform-origin:center top; animation:sway 6s ease-in-out infinite; }
@keyframes sway{ 0%,100%{ transform:rotate(-1.2deg);} 50%{ transform:rotate(1.6deg);} }
.eyelid{ transform-box:fill-box; transform-origin:center; animation:blink 5.2s infinite; }
@keyframes blink{ 0%,92%,100%{ transform:scaleY(0);} 95%,97%{ transform:scaleY(1);} }
.pupil{ transition:transform .12s ease-out; }
.blush{ opacity:0; transition:opacity .5s; }
.mascot.happy .blush{ opacity:.75; }
</style>
