<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'

/**
 * 顶部横幅轮播（升级版）
 * - 横向滑动轨道 + 淡入缩放混合过渡
 * - 左右箭头 / 指示点 / 自动播放进度条
 * - 悬停暂停，切换时进度条同步重置
 */
const props = defineProps({
  banners: { type: Array, default: () => [] }
})

const fallback = [
  { id: 0, title: '月夜为媒，红妆为信', subtitle: '以三生石上旧精魂，许你一段不灭姻缘。', tag: '鬼娘花嫁', accent: '#E84A78' },
  { id: 1, title: '十里红妆，铺作星河', subtitle: '胭脂钗环、凤冠霞帔，皆待有心人逐件拣选。', tag: '红妆阁', accent: '#F4D58B' },
  { id: 2, title: '忘川彼岸，花叶不相见', subtitle: '纵身死而名相守，青石双碑刻两世同心。', tag: '往生簿', accent: '#9B6BD6' }
]

const list = computed(() => (props.banners && props.banners.length ? props.banners : fallback))
const active = ref(0)
const paused = ref(false)
let timer = null

const DURATION = 5200

function go(i) {
  active.value = (i + list.value.length) % list.value.length
  restart()
}
function next() { go(active.value + 1) }
function prev() { go(active.value - 1) }

function start() {
  stop()
  timer = setInterval(() => { if (!paused.value) next() }, DURATION)
}
function stop() { if (timer) { clearInterval(timer); timer = null } }
function restart() { start() }

watch(() => props.banners, () => { active.value = 0; start() })

onMounted(start)
onBeforeUnmount(stop)

function accentOf(b) { return (b && b.accent) || '#E84A78' }
function hexA(hex, a) {
  const h = (hex || '#E84A78').replace('#', '')
  const r = parseInt(h.substring(0, 2), 16)
  const g = parseInt(h.substring(2, 4), 16)
  const b = parseInt(h.substring(4, 6), 16)
  return `rgba(${r},${g},${b},${a})`
}
</script>

<template>
  <div
    class="banner glass"
    :class="{ paused }"
    @mouseenter="paused = true"
    @mouseleave="paused = false"
  >
    <!-- 滑动轨道 -->
    <div class="viewport">
      <div class="track" :style="{ transform: `translateX(-${active * 100}%)` }">
        <div
          v-for="(b, i) in list" :key="b.id"
          class="slide"
          :class="{ on: i === active }"
          :style="{
            background: `radial-gradient(120% 140% at 12% 0%, ${hexA(accentOf(b), .28)} 0%, rgba(20,8,40,.0) 55%)`,
            borderColor: hexA(accentOf(b), .5)
          }"
        >
          <span class="b-tag" :style="{ color: accentOf(b), borderColor: hexA(accentOf(b), .6) }">{{ b.tag }}</span>
          <h2 class="b-title gold-text">{{ b.title }}</h2>
          <p class="b-sub">{{ b.subtitle }}</p>
        </div>
      </div>

      <!-- 左右箭头 -->
      <button class="arrow left" type="button" aria-label="上一条" @click.stop="prev">‹</button>
      <button class="arrow right" type="button" aria-label="下一条" @click.stop="next">›</button>
    </div>

    <!-- 指示点 + 自动播放进度 -->
    <div class="foot">
      <div class="dots">
        <button
          v-for="(b, i) in list" :key="'d' + b.id"
          type="button" class="dot" :class="{ on: i === active }"
          :style="i === active ? { background: accentOf(b), boxShadow: `0 0 8px ${hexA(accentOf(b), .8)}` } : {}"
          :aria-label="`第 ${i + 1} 条`"
          @click="go(i)"
        ></button>
      </div>
      <div class="progress" aria-hidden="true">
        <span
          :key="active"
          class="bar"
          :style="{
            background: `linear-gradient(90deg, ${hexA(accentOf(list[active]), .2)}, ${accentOf(list[active])})`,
            animationDuration: DURATION + 'ms',
            animationPlayState: paused ? 'paused' : 'running'
          }"
        ></span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.banner{
  position:relative; width:min(860px,94vw); margin:6px auto 8px;
  border-radius:var(--radius-xl); padding:20px 20px 16px;
  overflow:hidden; min-height:148px;
  box-shadow:0 24px 60px rgba(13,4,32,.5), 0 0 30px rgba(244,213,139,.12);
}

/* ---------- 滑动轨道 ---------- */
.viewport{ position:relative; overflow:hidden; border-radius:18px; }
.track{
  display:flex; align-items:stretch;
  transition:transform .75s cubic-bezier(.22,1,.36,1);
  will-change:transform;
}
.slide{
  flex:0 0 100%; min-width:100%;
  display:flex; flex-direction:column; justify-content:center;
  border:1px solid transparent; border-radius:18px; padding:16px 20px;
  min-height:118px;
  opacity:.35; transform:scale(.965);
  filter:blur(1.5px);
  transition:opacity .75s var(--ease-out), transform .75s var(--ease-out), filter .75s var(--ease-out);
}
.slide.on{ opacity:1; transform:none; filter:none; }

.b-tag{
  align-self:flex-start; font-size:11px; letter-spacing:2px;
  padding:3px 12px; border:1px solid; border-radius:999px; margin-bottom:10px;
}
.b-title{
  margin:0 0 8px; font-family:var(--font-serif); font-size:clamp(20px,3.4vw,30px);
  letter-spacing:2px; font-weight:700;
}
.b-sub{ margin:0; font-size:13px; letter-spacing:1px; color:var(--rose); opacity:.92; line-height:1.7; }

/* ---------- 左右箭头（描金玻璃圆钮 + 悬停辉光） ---------- */
.arrow{
  position:absolute; top:50%; transform:translateY(-50%); z-index:3;
  width:34px; height:34px; border-radius:50%;
  display:grid; place-items:center;
  border:1px solid rgba(244,213,139,.55); cursor:pointer;
  color:var(--gold); font-size:20px; line-height:1; font-family:serif;
  background:linear-gradient(180deg, rgba(255,247,251,.14), rgba(255,247,251,.06));
  backdrop-filter:blur(8px); -webkit-backdrop-filter:blur(8px);
  box-shadow:0 6px 16px rgba(13,4,32,.4);
  transition:.22s var(--ease-out);
  opacity:.55;
}
.banner:hover .arrow{ opacity:1; }
.arrow:hover{
  color:#fff; border-color:var(--gold);
  background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 6px 18px rgba(232,74,120,.55), 0 0 16px rgba(244,213,139,.4);
}
.arrow:active{ transform:translateY(-50%) scale(.92); }
.arrow.left{ left:10px; }
.arrow.right{ right:10px; }

/* ---------- 指示点 + 进度条 ---------- */
.foot{ display:flex; align-items:center; gap:14px; margin-top:14px; padding:0 6px; }
.dots{ display:flex; gap:8px; }
.dot{
  width:9px; height:9px; padding:0; border-radius:999px; cursor:pointer;
  background:rgba(255,179,200,.35); border:none; transition:.25s;
}
.dot.on{ width:26px; }
.dot:hover{ background:rgba(255,179,200,.7); }

.progress{
  flex:1; height:3px; border-radius:999px; overflow:hidden;
  background:rgba(255,179,200,.16);
}
.bar{
  display:block; height:100%; width:0; border-radius:inherit;
  animation:barRun linear forwards;
  box-shadow:0 0 8px rgba(244,213,139,.6);
}
@keyframes barRun{ from{ width:0; } to{ width:100%; } }
.banner.paused .bar{ animation-play-state:paused; }

@media (prefers-reduced-motion: reduce){
  .track,.slide{ transition:none; }
  .bar{ animation:none; width:100%; }
}
</style>
