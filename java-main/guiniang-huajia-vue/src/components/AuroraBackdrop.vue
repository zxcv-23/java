<script setup>
// 高级动态感背景：缓慢流动的极光网格（非人物、非具象）
</script>

<template>
  <div class="aurora" aria-hidden="true">
    <span class="blob b1"></span>
    <span class="blob b2"></span>
    <span class="blob b3"></span>
    <span class="blob b4"></span>
    <span class="grain"></span>
  </div>
</template>

<style scoped>
.aurora{
  position:fixed; inset:0; z-index:0; overflow:hidden; pointer-events:none;
  background:
    radial-gradient(circle at 50% -10%, rgba(255,179,200,.16) 0%, rgba(255,179,200,0) 42%),
    radial-gradient(circle at 50% 12%, var(--bg1) 0%, var(--bg2) 46%, var(--bg3) 100%);
}
.blob{
  position:absolute; border-radius:50%; filter:blur(70px); opacity:.55;
  mix-blend-mode:screen; will-change:transform;
}
.b1{ width:46vw; height:46vw; left:-8vw; top:-6vw;
  background:radial-gradient(circle, rgba(255,111,156,.85), rgba(255,111,156,0) 70%);
  animation:drift1 22s ease-in-out infinite; }
.b2{ width:40vw; height:40vw; right:-6vw; top:8vh;
  background:radial-gradient(circle, rgba(155,107,214,.8), rgba(155,107,214,0) 70%);
  animation:drift2 27s ease-in-out infinite; }
.b3{ width:38vw; height:38vw; left:18vw; bottom:-10vw;
  background:radial-gradient(circle, rgba(244,213,139,.7), rgba(244,213,139,0) 70%);
  animation:drift3 31s ease-in-out infinite; }
.b4{ width:30vw; height:30vw; right:14vw; bottom:-4vw;
  background:radial-gradient(circle, rgba(232,74,120,.7), rgba(232,74,120,0) 70%);
  animation:drift1 25s ease-in-out infinite reverse; }

@keyframes drift1{
  0%,100%{ transform:translate(0,0) scale(1); }
  33%{ transform:translate(8vw,6vh) scale(1.12); }
  66%{ transform:translate(-6vw,10vh) scale(.94); }
}
@keyframes drift2{
  0%,100%{ transform:translate(0,0) scale(1); }
  50%{ transform:translate(-10vw,8vh) scale(1.16); }
}
@keyframes drift3{
  0%,100%{ transform:translate(0,0) scale(1); }
  40%{ transform:translate(6vw,-8vh) scale(1.1); }
  75%{ transform:translate(-4vw,-4vh) scale(.92); }
}

/* 细微噪点，提升质感（纯 CSS 渐变模拟） */
.grain{
  position:absolute; inset:0; opacity:.05; mix-blend-mode:overlay;
  background-image:
    repeating-linear-gradient(0deg, rgba(255,255,255,.6) 0 1px, transparent 1px 3px),
    repeating-linear-gradient(90deg, rgba(255,255,255,.6) 0 1px, transparent 1px 3px);
  background-size:3px 3px;
}
</style>
