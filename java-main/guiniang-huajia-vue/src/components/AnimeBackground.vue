<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'

/**
 * 3D 动态背景
 * - 第 1 层：AI 生成的「鬼娘花嫁」动漫人物图（3D 视差 + 缓慢推拉呼吸）
 * - 第 2 层：Canvas 发光粒子（樱花瓣 / 星火），按深度 z 分层，随鼠标产生视差
 * - 第 3 层：暗角 + 上下渐变压暗，保证前景内容可读
 * 全部仅用 transform / opacity，rAF 驱动，标签页隐藏时自动暂停。
 */
const stageRef = ref(null)
const innerRef = ref(null)
const canvasRef = ref(null)

const reduceMotion =
  typeof window !== 'undefined' &&
  window.matchMedia &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches

let raf = 0
let ctx = null
let W = 0, H = 0, DPR = 1
let mx = 0, my = 0          // 当前（缓动后）鼠标偏移 -0.5..0.5
let tx = 0, ty = 0          // 目标鼠标偏移
let running = true
let startedAt = performance.now()

const petals = []   // 前景樱花瓣（下落 + 旋转 + 发光）
const sparks = []   // 星火（上浮 + 闪烁）
const rand = (a, b) => a + Math.random() * (b - a)

function initParticles() {
  petals.length = 0
  sparks.length = 0
  for (let i = 0; i < 34; i++) {
    petals.push({
      x: Math.random(), y: Math.random(),
      z: rand(0.35, 1),                 // 深度：越大越近、视差越强
      s: rand(5, 11),                   // 尺寸
      vy: rand(0.14, 0.4),              // 下落速度（%/帧）
      ph: rand(0, Math.PI * 2),         // 摆动相位
      rot: rand(0, Math.PI * 2),
      vr: rand(-0.012, 0.012),
      rose: Math.random() < 0.7,        // 粉樱 / 白瓣
    })
  }
  for (let i = 0; i < 48; i++) {
    sparks.push({
      x: Math.random(), y: Math.random(),
      z: rand(0.3, 1),
      s: rand(0.8, 2.4),
      vy: rand(-0.16, -0.04),
      vx: rand(-0.05, 0.05),
      ph: rand(0, Math.PI * 2),
      tw: rand(0.6, 1.8),               // 闪烁频率
      gold: Math.random() < 0.5,        // 金焰 / 粉光
    })
  }
}

function resize() {
  if (!canvasRef.value) return
  DPR = Math.min(window.devicePixelRatio || 1, 2)
  W = window.innerWidth
  H = window.innerHeight
  canvasRef.value.width = Math.round(W * DPR)
  canvasRef.value.height = Math.round(H * DPR)
  canvasRef.value.style.width = W + 'px'
  canvasRef.value.style.height = H + 'px'
  ctx = canvasRef.value.getContext('2d')
  ctx.setTransform(DPR, 0, 0, DPR, 0, 0)
}

function draw(t) {
  if (!ctx) return
  ctx.clearRect(0, 0, W, H)

  // 鼠标缓动
  mx += (tx - mx) * 0.06
  my += (ty - my) * 0.06

  // ---- 樱花瓣 ----
  for (const p of petals) {
    p.y += p.vy * p.z * 0.0016 * 16
    p.rot += p.vr
    p.ph += 0.01
    if (p.y > 1.08) { p.y = -0.08; p.x = Math.random() }
    const sway = Math.sin(p.ph) * 18 * p.z
    // 深度视差：近处粒子随鼠标偏移更大
    const px = p.x * W + sway - mx * 70 * p.z
    const py = p.y * H - my * 42 * p.z
    const s = p.s * (0.6 + p.z * 0.8)

    ctx.save()
    ctx.translate(px, py)
    ctx.rotate(p.rot)
    ctx.shadowColor = p.rose ? 'rgba(255,143,180,.85)' : 'rgba(255,240,246,.85)'
    ctx.shadowBlur = 10 * p.z
    const g = ctx.createRadialGradient(0, 0, 0, 0, 0, s)
    if (p.rose) {
      g.addColorStop(0, 'rgba(255,196,218,.95)')
      g.addColorStop(1, 'rgba(255,143,180,.06)')
    } else {
      g.addColorStop(0, 'rgba(255,247,251,.95)')
      g.addColorStop(1, 'rgba(255,247,251,.05)')
    }
    ctx.fillStyle = g
    ctx.beginPath()
    ctx.ellipse(0, 0, s, s * 0.62, 0, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }

  // ---- 星火（叠加发光）----
  ctx.globalCompositeOperation = 'lighter'
  for (const sp of sparks) {
    sp.y += sp.vy * sp.z * 0.0012 * 16
    sp.x += sp.vx * sp.z * 0.0012 * 16
    sp.ph += 0.02 * sp.tw
    if (sp.y < -0.06) { sp.y = 1.05; sp.x = Math.random() }
    if (sp.x < -0.04) sp.x = 1.03
    if (sp.x > 1.04) sp.x = -0.03
    const tw = 0.45 + 0.55 * Math.abs(Math.sin(sp.ph))
    const px = sp.x * W - mx * 90 * sp.z
    const py = sp.y * H - my * 55 * sp.z
    const r = sp.s * (0.5 + sp.z) * (0.7 + tw * 0.6)

    const g = ctx.createRadialGradient(px, py, 0, px, py, r * 4)
    if (sp.gold) {
      g.addColorStop(0, `rgba(255,240,200,${0.9 * tw})`)
      g.addColorStop(0.35, `rgba(255,225,150,${0.35 * tw})`)
      g.addColorStop(1, 'rgba(255,225,150,0)')
    } else {
      g.addColorStop(0, `rgba(255,220,235,${0.9 * tw})`)
      g.addColorStop(0.35, `rgba(255,143,180,${0.32 * tw})`)
      g.addColorStop(1, 'rgba(255,143,180,0)')
    }
    ctx.fillStyle = g
    ctx.beginPath()
    ctx.arc(px, py, r * 4, 0, Math.PI * 2)
    ctx.fill()
  }
  ctx.globalCompositeOperation = 'source-over'

  // ---- 背景图 3D 视差 ----
  if (innerRef.value) {
    innerRef.value.style.transform =
      `rotateX(${(-my * 3.2).toFixed(2)}deg) rotateY(${(mx * 4.6).toFixed(2)}deg) ` +
      `translate3d(${(mx * -24).toFixed(1)}px, ${(my * -16).toFixed(1)}px, 0)`
  }
}

function loop(now) {
  if (!running) return
  draw(now - startedAt)
  raf = requestAnimationFrame(loop)
}

function onPointerMove(e) {
  tx = e.clientX / window.innerWidth - 0.5
  ty = e.clientY / window.innerHeight - 0.5
}

function onVisibility() {
  if (document.hidden) {
    running = false
    cancelAnimationFrame(raf)
  } else if (!reduceMotion) {
    running = true
    startedAt = performance.now()
    raf = requestAnimationFrame(loop)
  }
}

onMounted(() => {
  initParticles()
  resize()
  window.addEventListener('resize', resize)
  if (reduceMotion) {
    draw(0)   // 仅渲染一帧静态画面
    return
  }
  window.addEventListener('pointermove', onPointerMove, { passive: true })
  document.addEventListener('visibilitychange', onVisibility)
  raf = requestAnimationFrame(loop)
})

onBeforeUnmount(() => {
  running = false
  cancelAnimationFrame(raf)
  window.removeEventListener('resize', resize)
  window.removeEventListener('pointermove', onPointerMove)
  document.removeEventListener('visibilitychange', onVisibility)
})
</script>

<template>
  <div ref="stageRef" class="anime-bg" aria-hidden="true">
    <!-- 3D 舞台：动漫人物图 + 视差 -->
    <div class="stage">
      <div
        ref="innerRef"
        class="art"
        :style="{ backgroundImage: 'url(/bg-guiniang.png)' }"
      ></div>
    </div>

    <!-- 发光粒子 -->
    <canvas ref="canvasRef" class="particles"></canvas>

    <!-- 可读性渐变 + 暗角 -->
    <div class="veil"></div>
  </div>
</template>

<style scoped>
.anime-bg{
  position:fixed; inset:0; z-index:0; overflow:hidden;
  pointer-events:none;
  background:var(--bg3);
}
/* 3D 舞台 */
.stage{
  position:absolute; inset:-4%;
  perspective:1200px;
  transform-style:preserve-3d;
}
/* 动漫人物图层：缓慢推拉呼吸（ken-burns），视差 transform 由 JS 写入 */
.art{
  position:absolute; inset:0;
  background-size:cover;
  background-position:center 28%;
  transform-style:preserve-3d;
  will-change:transform;
  animation:breath 26s ease-in-out infinite alternate;
  filter:saturate(1.06) contrast(1.03);
}
@keyframes breath{
  0%  { transform:scale(1.05) translate3d(0,0,0); }
  100%{ transform:scale(1.13) translate3d(-1.4%, -1.2%, 0); }
}
/* 粒子层 */
.particles{
  position:absolute; inset:0;
  mix-blend-mode:screen;
}
/* 可读性：顶部导航压暗 + 底部压暗 + 四角暗角 */
.veil{
  position:absolute; inset:0;
  background:
    linear-gradient(180deg, rgba(23,12,43,.55) 0%, rgba(23,12,43,0) 22%),
    linear-gradient(0deg, rgba(23,12,43,.55) 0%, rgba(23,12,43,0) 26%),
    radial-gradient(120% 90% at 50% 42%, rgba(23,12,43,0) 46%, rgba(23,12,43,.5) 100%);
}
@media (prefers-reduced-motion: reduce){
  .art{ animation:none; transform:none; }
}
</style>
