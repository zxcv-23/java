<script setup>
// ---------- 夜空氛围：星星 / 花瓣 / 萤火（原 createElement 循环改为数据驱动 v-for） ----------
const starGlyphs = ['✦', '✧', '⋆', '✺']
const petalGlyphs = ['🌸', '💮', '🌷', '🥀']
const rand = (min, max) => min + Math.random() * (max - min)

const stars = Array.from({ length: 26 }, (_, i) => ({
  glyph: starGlyphs[i % starGlyphs.length],
  left: rand(0, 100), top: rand(0, 70),
  size: rand(9, 19), dur: rand(2.5, 6.5), delay: -rand(0, 6),
}))

const petals = Array.from({ length: 18 }, (_, i) => ({
  glyph: petalGlyphs[i % petalGlyphs.length],
  left: rand(0, 100),
  size: rand(14, 26), dur: rand(8, 17), delay: -rand(0, 16),
}))

const fireflies = Array.from({ length: 12 }, () => ({
  left: rand(0, 100), top: rand(8, 93),
  dur: rand(5, 11), delay: -rand(0, 8),
}))
</script>

<template>
  <div class="sky" aria-hidden="true">
    <span
      v-for="(s, i) in stars" :key="'st' + i" class="star"
      :style="{ left: s.left + '%', top: s.top + '%', fontSize: s.size + 'px', animationDuration: s.dur + 's', animationDelay: s.delay + 's' }"
    >{{ s.glyph }}</span>

    <span
      v-for="(p, i) in petals" :key="'pe' + i" class="petal"
      :style="{ left: p.left + '%', fontSize: p.size + 'px', animationDuration: p.dur + 's', animationDelay: p.delay + 's' }"
    >{{ p.glyph }}</span>

    <span
      v-for="(f, i) in fireflies" :key="'fl' + i" class="firefly"
      :style="{ left: f.left + '%', top: f.top + '%', animationDuration: f.dur + 's', animationDelay: f.delay + 's' }"
    ></span>
  </div>
</template>

<style scoped>
.sky{ position:fixed; inset:0; overflow:hidden; pointer-events:none; z-index:2; }

.star{ position:absolute; color:#FFF3FA; font-size:13px; text-shadow:0 0 6px #fff; animation:twinkle linear infinite; }
@keyframes twinkle{ 0%,100%{ opacity:.15; transform:scale(.8);} 50%{ opacity:1; transform:scale(1.15);} }

.petal{ position:absolute; top:-8vh; font-size:18px; will-change:transform; animation:fall linear infinite; opacity:0; }
@keyframes fall{
  0%  { transform:translate(0,-10vh) rotate(0); opacity:0; }
  8%  { opacity:.95; }
  100%{ transform:translate(46px,116vh) rotate(420deg); opacity:.7; }
}

.firefly{
  position:absolute; width:7px; height:7px; border-radius:50%;
  background:radial-gradient(circle, #FFF6C9 0%, rgba(255,246,201,.5) 45%, rgba(255,246,201,0) 75%);
  box-shadow:0 0 10px 3px rgba(255,243,170,.7);
  animation:drift ease-in-out infinite;
}
@keyframes drift{
  0%{ transform:translate(0,0); opacity:.25; }
  50%{ transform:translate(34px,-30px); opacity:1; }
  100%{ transform:translate(-22px,-58px); opacity:.25; }
}
</style>
