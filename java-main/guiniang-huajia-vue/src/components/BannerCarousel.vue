<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'

// 由 HomePage 传入后端 banners；为空时使用内置兜底标语
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

function go(i) {
  active.value = (i + list.value.length) % list.value.length
}
function next() { go(active.value + 1) }

function start() {
  stop()
  timer = setInterval(() => { if (!paused.value) next() }, 5200)
}
function stop() { if (timer) { clearInterval(timer); timer = null } }

watch(() => props.banners, () => { active.value = 0; start() })

onMounted(start)
onBeforeUnmount(stop)

function hexA(hex, a) {
  const h = hex.replace('#', '')
  const r = parseInt(h.substring(0, 2), 16)
  const g = parseInt(h.substring(2, 4), 16)
  const b = parseInt(h.substring(4, 6), 16)
  return `rgba(${r},${g},${b},${a})`
}
</script>

<template>
  <div
    class="banner glass"
    @mouseenter="paused = true"
    @mouseleave="paused = false"
  >
    <div class="slides">
      <div
        v-for="(b, i) in list" :key="b.id"
        class="slide"
        :class="{ on: i === active }"
        :style="{
          background: `radial-gradient(120% 140% at 12% 0%, ${hexA(b.accent, .28)} 0%, rgba(20,8,40,.0) 55%)`,
          borderColor: hexA(b.accent, .5)
        }"
      >
        <span class="b-tag" :style="{ color: b.accent, borderColor: hexA(b.accent, .6) }">{{ b.tag }}</span>
        <h2 class="b-title gold-text">{{ b.title }}</h2>
        <p class="b-sub">{{ b.subtitle }}</p>
      </div>
    </div>

    <div class="dots">
      <button
        v-for="(b, i) in list" :key="'d' + b.id"
        type="button" class="dot" :class="{ on: i === active }"
        :style="i === active ? { background: b.accent } : {}"
        :aria-label="`第 ${i + 1} 条`"
        @click="go(i)"
      ></button>
    </div>
  </div>
</template>

<style scoped>
.banner{
  position:relative; width:min(860px,94vw); margin:6px auto 8px;
  border-radius:var(--radius-xl); padding:26px 30px 30px;
  overflow:hidden; min-height:148px;
  box-shadow:0 24px 60px rgba(13,4,32,.5);
}
.slides{ position:relative; min-height:104px; }
.slide{
  position:absolute; inset:0; display:flex; flex-direction:column; justify-content:center;
  border:1px solid transparent; border-radius:18px; padding:14px 18px;
  opacity:0; transform:translateY(14px) scale(.99);
  transition:opacity .8s var(--ease-out), transform .8s var(--ease-out);
  pointer-events:none;
}
.slide.on{ opacity:1; transform:none; pointer-events:auto; }
.b-tag{
  align-self:flex-start; font-size:11px; letter-spacing:2px;
  padding:3px 12px; border:1px solid; border-radius:999px; margin-bottom:10px;
}
.b-title{
  margin:0 0 8px; font-family:var(--font-serif); font-size:clamp(20px,3.4vw,30px);
  letter-spacing:2px; font-weight:700;
}
.b-sub{ margin:0; font-size:13px; letter-spacing:1px; color:var(--rose); opacity:.92; line-height:1.7; }

.dots{ display:flex; gap:8px; justify-content:center; margin-top:18px; }
.dot{
  width:9px; height:9px; padding:0; border-radius:999px; cursor:pointer;
  background:rgba(255,179,200,.35); border:none; transition:.25s;
}
.dot.on{ width:26px; }
.dot:hover{ background:rgba(255,179,200,.7); }
</style>
