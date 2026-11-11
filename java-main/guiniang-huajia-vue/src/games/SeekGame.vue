<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 宫墙寻芳 ============ */
const ITEMS = [
  { n: '花笺', k: 'note', poem: '一纸未寄的春信' },
  { n: '玉簪', k: 'pin', poem: '鬓边遗落的旧物' },
  { n: '香囊', k: 'sachet', poem: '犹带沉水香气' },
  { n: '铜钱', k: 'coin', poem: '磨得发亮的旧钱' },
  { n: '玉佩', k: 'jade', poem: '系着半截红绳' }
]
const SPOTS = [
  { x: 14, y: 33 }, { x: 32, y: 29 }, { x: 16, y: 21 },
  { x: 9, y: 55 }, { x: 26, y: 58 }, { x: 38, y: 67 },
  { x: 46, y: 62 }, { x: 58, y: 71 }, { x: 86, y: 66 },
  { x: 52, y: 76 }, { x: 60, y: 86 }, { x: 33, y: 83 }
]

const TOTAL = 5
const targets = ref([])
const found = ref(0)
const time = ref(60)
const over = ref(false)
const win = ref(false)
const msg = ref('')
const ripples = ref([])
const hintId = ref(null)
let timer = null
let ripSeq = 0
const timers = []
const later = (fn, ms) => { const id = setTimeout(fn, ms); timers.push(id) }

const list = computed(() => targets.value.filter(t => t.found))

function shuffle(a) {
  const b = a.slice()
  for (let i = b.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[b[i], b[j]] = [b[j], b[i]]
  }
  return b
}
function init() {
  const spots = shuffle(SPOTS).slice(0, TOTAL)
  const items = shuffle(ITEMS)
  targets.value = spots.map((s, i) => ({
    id: 't' + i, x: s.x, y: s.y, item: items[i], found: false
  }))
  found.value = 0
  time.value = 60
  over.value = false
  win.value = false
  msg.value = ''
  hintId.value = null
  ripples.value = []
}
init()

function ripple(x, y, hit) {
  const id = ++ripSeq
  ripples.value.push({ id, x, y, hit })
  later(() => { ripples.value = ripples.value.filter(r => r.id !== id) }, 760)
}
function pick(t) {
  if (over.value || t.found) return
  t.found = true
  found.value++
  ripple(t.x, t.y, true)
  msg.value = '寻得「' + t.item.n + '」· ' + t.item.poem
  if (found.value >= TOTAL) finish(true)
}
function missPoint(e) {
  if (over.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  const x = (e.clientX - rect.left) / rect.width * 100
  const y = (e.clientY - rect.top) / rect.height * 100
  ripple(x, y, false)
  time.value = Math.max(0, time.value - 3)
  msg.value = '此处只有风过 · 时辰 -3'
  if (time.value <= 0) finish(false)
}
function useHint() {
  if (over.value || time.value <= 6) return
  const left = targets.value.filter(t => !t.found)
  if (!left.length) return
  const t = left[Math.floor(Math.random() * left.length)]
  hintId.value = t.id
  time.value = Math.max(1, time.value - 5)
  msg.value = '灯影一晃 · 似有微光（时辰 -5）'
  later(() => { if (hintId.value === t.id) hintId.value = null }, 1800)
}
function finish(w) {
  if (over.value) return
  over.value = true
  win.value = w
  if (w) time.value = Math.max(0, time.value)
}
onMounted(() => {
  timer = setInterval(() => {
    if (over.value) return
    time.value--
    if (time.value <= 0) { time.value = 0; finish(false) }
  }, 1000)
})
onUnmounted(() => {
  clearInterval(timer)
  timers.forEach(clearTimeout)
})
</script>

<template>
  <div class="gx-game seek">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#FFC98A,#E39A4F)">寻</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 寻物</p>
        <h2 class="gx-title">宫墙寻芳</h2>
      </div>
      <div class="gx-headright">
        <button class="gx-btn ghost mini" type="button" @click="init">重开一场</button>
      </div>
    </header>

    <div class="gx-hud">
      <span class="gx-stat">已寻 <b>{{ found }}</b> / {{ TOTAL }}</span>
      <span class="gx-stat jade" :class="{ low: time <= 12 }">时辰 <b>{{ time }}</b>s</span>
      <span class="gx-spacer"></span>
      <button class="gx-btn gold mini" type="button" :disabled="over || time <= 6" @click="useHint">点灯提示 · 5s</button>
    </div>

    <div class="gx-stage">
      <div class="gx-stage-in">
        <div class="sk-stage" @click="missPoint">
          <svg class="sk-svg" viewBox="0 0 720 420" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
            <defs>
              <linearGradient id="skSky" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#251440" />
                <stop offset=".45" stop-color="#45255F" />
                <stop offset="1" stop-color="#1B0F2B" />
              </linearGradient>
              <radialGradient id="skMoon" cx=".42" cy=".38" r=".62">
                <stop offset="0" stop-color="#FFFDF3" />
                <stop offset=".6" stop-color="#FFE9C4" />
                <stop offset="1" stop-color="#FFD8A8" stop-opacity=".2" />
              </radialGradient>
              <radialGradient id="skMoonGlow" cx=".5" cy=".5" r=".5">
                <stop offset="0" stop-color="#FFF0D0" stop-opacity=".55" />
                <stop offset="1" stop-color="#FFE0B0" stop-opacity="0" />
              </radialGradient>
              <linearGradient id="skWall" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#8C3A46" />
                <stop offset=".55" stop-color="#6E2836" />
                <stop offset="1" stop-color="#43151F" />
              </linearGradient>
              <linearGradient id="skRoof" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#4C3A58" />
                <stop offset=".5" stop-color="#33243F" />
                <stop offset="1" stop-color="#22162C" />
              </linearGradient>
              <linearGradient id="skGround" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#2B1B38" />
                <stop offset="1" stop-color="#160C22" />
              </linearGradient>
              <radialGradient id="skLamp" cx=".5" cy=".5" r=".5">
                <stop offset="0" stop-color="#FFC98A" stop-opacity=".85" />
                <stop offset=".45" stop-color="#FF9A6A" stop-opacity=".35" />
                <stop offset="1" stop-color="#FF8A5A" stop-opacity="0" />
              </radialGradient>
              <linearGradient id="skWater" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#6E5AA8" stop-opacity=".65" />
                <stop offset="1" stop-color="#2A1B4A" stop-opacity=".9" />
              </linearGradient>
              <filter id="skBlur" x="-40%" y="-40%" width="180%" height="180%">
                <feGaussianBlur stdDeviation="10" />
              </filter>
            </defs>

            <rect width="720" height="420" fill="url(#skSky)" />
            <circle cx="576" cy="78" r="96" fill="url(#skMoonGlow)" />
            <circle cx="576" cy="78" r="34" fill="url(#skMoon)" />
            <circle cx="576" cy="78" r="34" fill="none" stroke="#FFF6DE" stroke-width="1" opacity=".5" />

            <!-- 云 -->
            <g opacity=".28" fill="#C9A8E0">
              <ellipse cx="150" cy="86" rx="88" ry="14" />
              <ellipse cx="210" cy="72" rx="52" ry="10" />
              <ellipse cx="470" cy="120" rx="70" ry="11" />
              <ellipse cx="520" cy="110" rx="40" ry="8" />
            </g>

            <!-- 远山 -->
            <path d="M0 196 L110 138 L196 182 L300 128 L412 184 L520 146 L628 188 L720 154 L720 262 L0 262 Z"
                  fill="#3B2453" opacity=".85" />
            <path d="M0 214 L96 176 L190 212 L312 168 L430 214 L548 178 L660 216 L720 190 L720 262 L0 262 Z"
                  fill="#2C1A40" opacity=".9" />

            <!-- 左侧殿宇屋顶 -->
            <path d="M-10 250 L-10 196 L86 148 L182 196 L182 250 Z" fill="url(#skRoof)" />
            <path d="M-10 196 L86 148 L182 196" fill="none" stroke="#5E4A6E" stroke-width="3" />
            <g stroke="#5E4A6E" stroke-width="1.1" opacity=".42" fill="none">
              <path d="M-6 210 Q40 190 86 166 Q132 190 178 210" />
              <path d="M-4 224 Q42 204 86 182 Q130 204 176 224" />
              <path d="M-2 238 Q44 218 86 198 Q128 218 174 238" />
            </g>
            <path d="M60 166 L60 250 M110 155 L110 250" stroke="#241830" stroke-width="2" opacity=".6" />

            <!-- 宫墙 -->
            <rect x="0" y="248" width="720" height="96" fill="url(#skWall)" />
            <!-- 墙面砖缝 -->
            <g stroke="#3A1420" stroke-width="1" opacity=".26">
              <path d="M0 272 H720 M0 296 H720 M0 320 H720" />
              <path d="M60 248 V272 M160 248 V272 M260 248 V272 M360 248 V272 M460 248 V272 M560 248 V272 M660 248 V272" />
              <path d="M110 272 V296 M210 272 V296 M310 272 V296 M410 272 V296 M510 272 V296 M610 272 V296 M710 272 V296" />
              <path d="M60 296 V320 M160 296 V320 M260 296 V320 M360 296 V320 M460 296 V320 M560 296 V320 M660 296 V320" />
              <path d="M110 320 V344 M210 320 V344 M310 320 V344 M410 320 V344 M510 320 V344 M610 320 V344" />
            </g>
            <rect x="0" y="238" width="720" height="14" fill="#39273F" />
            <g fill="#4B354F">
              <circle cx="12" cy="245" r="5" /><circle cx="44" cy="245" r="5" /><circle cx="76" cy="245" r="5" />
              <circle cx="108" cy="245" r="5" /><circle cx="140" cy="245" r="5" /><circle cx="172" cy="245" r="5" />
              <circle cx="204" cy="245" r="5" /><circle cx="236" cy="245" r="5" /><circle cx="268" cy="245" r="5" />
              <circle cx="300" cy="245" r="5" /><circle cx="332" cy="245" r="5" /><circle cx="364" cy="245" r="5" />
              <circle cx="396" cy="245" r="5" /><circle cx="428" cy="245" r="5" /><circle cx="460" cy="245" r="5" />
              <circle cx="492" cy="245" r="5" /><circle cx="524" cy="245" r="5" /><circle cx="556" cy="245" r="5" />
              <circle cx="588" cy="245" r="5" /><circle cx="620" cy="245" r="5" /><circle cx="652" cy="245" r="5" />
              <circle cx="684" cy="245" r="5" /><circle cx="716" cy="245" r="5" />
            </g>
            <rect x="0" y="248" width="720" height="3" fill="#A95563" opacity=".45" />

            <!-- 墙面窗棂 -->
            <g>
              <rect x="452" y="272" width="104" height="58" rx="3" fill="#FFE1A8" opacity=".22" />
              <rect x="452" y="272" width="104" height="58" rx="3" fill="none" stroke="#E9C173" stroke-width="2" opacity=".65" />
              <path d="M486 272 V330 M522 272 V330 M452 291 H556 M452 311 H556"
                    stroke="#E9C173" stroke-width="1.2" opacity=".5" />
            </g>

            <!-- 廊柱 -->
            <g>
              <rect x="72" y="238" width="20" height="106" fill="#5A1D2A" />
              <rect x="72" y="238" width="6" height="106" fill="#8C4050" opacity=".5" />
              <rect x="66" y="232" width="32" height="9" rx="2" fill="#4A1622" />
            </g>

            <!-- 灯笼 -->
            <g>
              <circle cx="618" cy="300" r="54" fill="url(#skLamp)" filter="url(#skBlur)" />
              <path d="M618 246 V262" stroke="#E9C173" stroke-width="2" />
              <rect x="610" y="260" width="16" height="5" rx="2" fill="#E9C173" />
              <ellipse cx="618" cy="292" rx="18" ry="24" fill="#D9503F" />
              <ellipse cx="618" cy="292" rx="18" ry="24" fill="none" stroke="#FFD9A8" stroke-width="1.4" opacity=".7" />
              <path d="M604 284 Q618 292 632 284 M604 300 Q618 292 632 300" stroke="#FFE0B8" stroke-width="1" fill="none" opacity=".55" />
              <rect x="610" y="314" width="16" height="5" rx="2" fill="#E9C173" />
              <path d="M614 319 V336 M622 319 V336" stroke="#E9C173" stroke-width="1.5" opacity=".8" />
            </g>

            <!-- 花枝 -->
            <g stroke="#6B3F60" stroke-width="4" stroke-linecap="round" fill="none" opacity=".9">
              <path d="M24 -6 C 60 40, 86 82, 96 140" />
              <path d="M62 46 C 92 40, 118 48, 138 66" />
              <path d="M84 92 C 60 104, 46 124, 44 148" />
            </g>
            <g fill="#FFB9D7" opacity=".92">
              <circle cx="60" cy="40" r="4" /><circle cx="70" cy="48" r="3.2" /><circle cx="52" cy="52" r="3.2" />
              <circle cx="112" cy="56" r="4" /><circle cx="124" cy="62" r="3.2" /><circle cx="136" cy="70" r="3.6" />
              <circle cx="52" cy="126" r="4" /><circle cx="44" cy="138" r="3.2" /><circle cx="58" cy="148" r="3.6" />
              <circle cx="88" cy="112" r="3" />
            </g>
            <g fill="#FFF0C9" opacity=".85">
              <circle cx="60" cy="40" r="1.4" /><circle cx="112" cy="56" r="1.4" /><circle cx="52" cy="126" r="1.4" />
            </g>

            <!-- 地面 -->
            <rect x="0" y="344" width="720" height="76" fill="url(#skGround)" />
            <g stroke="#4A3358" stroke-width="1" opacity=".35">
              <path d="M0 372 H720 M0 400 H720 M180 344 V420 M360 344 V420 M540 344 V420 M90 344 V420 M270 344 V420 M450 344 V420 M630 344 V420" />
            </g>

            <!-- 池塘 -->
            <ellipse cx="512" cy="386" rx="156" ry="26" fill="url(#skWater)" />
            <g stroke="#B9A8E8" fill="none" opacity=".45">
              <path d="M414 380 q18 -6 36 0 t36 0" />
              <path d="M466 394 q20 -6 40 0 t40 0" />
              <path d="M500 370 q16 -5 32 0" />
            </g>
            <ellipse cx="512" cy="386" rx="156" ry="26" fill="none" stroke="#8F7BC4" stroke-width="1" opacity=".35" />

            <!-- 石阶边影 -->
            <path d="M262 344 L392 344 L404 362 L250 362 Z" fill="#3A2848" opacity=".7" />
            <path d="M250 362 L404 362 L416 380 L238 380 Z" fill="#31213E" opacity=".8" />

            <!-- 夜雾与暗角，压出景深 -->
            <ellipse cx="330" cy="352" rx="430" ry="26" fill="#C4AEDC" opacity=".10" filter="url(#skBlur)" />
            <ellipse cx="150" cy="404" rx="260" ry="30" fill="#8E74B8" opacity=".12" filter="url(#skBlur)" />
            <radialGradient id="skVig" cx="0.5" cy="0.44" r="0.74">
              <stop offset="0.52" stop-color="#000000" stop-opacity="0" />
              <stop offset="1" stop-color="#0A0412" stop-opacity="0.58" />
            </radialGradient>
            <rect width="720" height="420" fill="url(#skVig)" />
          </svg>

          <!-- 信物热点 -->
          <button v-for="t in targets" :key="t.id" type="button" class="sk-spot"
                  :class="{ found: t.found, hinted: hintId === t.id }"
                  :style="{ left: t.x + '%', top: t.y + '%' }"
                  :aria-label="t.found ? t.item.n : '可疑之处'"
                  @click.stop="pick(t)">
            <svg viewBox="0 0 24 24" class="sk-icon" aria-hidden="true">
              <g v-if="t.item.k === 'note'">
                <rect x="6" y="4" width="12" height="16" rx="1.6" fill="#F3E3C4" stroke="#C0A276" stroke-width="1" />
                <path d="M9 9h6M9 12h6M9 15h4" stroke="#A98763" stroke-width="1" />
                <circle cx="15.5" cy="16.5" r="1.6" fill="#C94F60" />
              </g>
              <g v-else-if="t.item.k === 'pin'">
                <path d="M4 20 L18 6" stroke="#E9C173" stroke-width="2" stroke-linecap="round" />
                <circle cx="19" cy="5" r="3" fill="#F291B2" stroke="#E9C173" stroke-width="1" />
                <path d="M4 20 l-1 1" stroke="#F7E0AE" stroke-width="1.4" />
              </g>
              <g v-else-if="t.item.k === 'sachet'">
                <path d="M8 8 h8 l2 9 a4 4 0 0 1 -4 4 h-4 a4 4 0 0 1 -4 -4 Z" fill="#B0607C" stroke="#F0C6D6" stroke-width="1" />
                <path d="M9 8 C10 5,14 5,15 8" stroke="#F0C6D6" stroke-width="1.2" fill="none" />
                <path d="M9 13 q3 2 6 0" stroke="#F0D9A8" stroke-width="1" fill="none" />
              </g>
              <g v-else-if="t.item.k === 'coin'">
                <circle cx="12" cy="12" r="8" fill="#D9B364" stroke="#8A6A32" stroke-width="1" />
                <rect x="9.4" y="9.4" width="5.2" height="5.2" rx="0.6" fill="#241528" />
                <circle cx="12" cy="12" r="8" fill="none" stroke="#FFF0C2" stroke-width=".8" opacity=".7" />
              </g>
              <g v-else>
                <circle cx="12" cy="11" r="7" fill="none" stroke="#9FE0C8" stroke-width="2.4" />
                <circle cx="12" cy="11" r="3" fill="#7FD0B4" opacity=".55" />
                <path d="M12 18 v4" stroke="#E05C6E" stroke-width="1.6" />
              </g>
            </svg>
          </button>

          <!-- 点击波纹 -->
          <span v-for="r in ripples" :key="r.id" class="sk-ripple"
                :class="{ hit: r.hit }" :style="{ left: r.x + '%', top: r.y + '%' }"></span>

          <div v-if="over" class="gx-result">
            <div class="gx-result-card">
              <div class="gx-seal">{{ win ? '寻' : '憾' }}</div>
              <h3>{{ win ? '芳踪尽得' : '更漏已尽' }}</h3>
              <p>
                {{ win ? '五件信物俱已寻回，宫墙深处似有人轻轻一叹。' : '只寻得 ' + found + ' 件，余下的还在灯影里。' }}<br>
                用时 {{ 60 - time }} 秒。
              </p>
              <ul class="sk-found-list">
                <li v-for="t in list" :key="t.id">{{ t.item.n }} · {{ t.item.poem }}</li>
              </ul>
              <div class="gx-foot">
                <button class="gx-btn primary" type="button" @click="init">再寻一场</button>
                <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <p class="gx-hint">在宫墙、花枝、灯影间点出藏着的五件信物；点空扣三秒。{{ msg }}</p>
    <div class="gx-foot">
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.sk-stage{ position:relative; aspect-ratio:720 / 420; cursor:crosshair; }
.sk-svg{ position:absolute; inset:0; width:100%; height:100%; display:block; }
.sk-spot{
  position:absolute; width:34px; height:34px; transform:translate(-50%,-50%);
  display:flex; align-items:center; justify-content:center;
  background:transparent; border:0; padding:0; cursor:pointer;
  opacity:.62; transition:.25s cubic-bezier(.22,1,.36,1); z-index:3;
}
.sk-icon{ width:24px; height:24px; filter:drop-shadow(0 2px 3px rgba(0,0,0,.6)); }
.sk-spot:hover{ opacity:1; transform:translate(-50%,-50%) scale(1.18); }
.sk-spot.found{
  opacity:1; cursor:default; pointer-events:none;
  transform:translate(-50%,-50%) scale(1.1);
}
.sk-spot.found .sk-icon{ filter:drop-shadow(0 0 10px rgba(255,220,140,.95)) drop-shadow(0 0 20px rgba(255,180,90,.6)); }
.sk-spot.hinted{ animation:sk-hint 1s ease-in-out 2; opacity:1; }
@keyframes sk-hint{
  0%,100%{ filter:none; transform:translate(-50%,-50%) scale(1) }
  50%{ filter:drop-shadow(0 0 16px rgba(255,230,150,1)); transform:translate(-50%,-50%) scale(1.35) }
}
.sk-ripple{
  position:absolute; width:14px; height:14px; margin:-7px 0 0 -7px; border-radius:50%;
  border:2px solid rgba(255,255,255,.55); pointer-events:none; z-index:4;
  animation:sk-rip .75s ease-out forwards;
}
.sk-ripple.hit{ border-color:rgba(255,214,130,.95); box-shadow:0 0 20px rgba(255,200,120,.8); }
@keyframes sk-rip{
  0%{ transform:scale(.4); opacity:1 }
  100%{ transform:scale(5.2); opacity:0 }
}
.gx-stat.low b{ color:#FF9E9E; animation:gx-breathe 1s infinite; }
.sk-found-list{
  list-style:none; margin:0 0 16px; padding:10px 12px; border-radius:10px; text-align:left;
  background:rgba(255,255,255,.06); border:1px solid rgba(233,193,115,.22);
  display:flex; flex-direction:column; gap:5px;
}
.sk-found-list li{ font-size:11px; letter-spacing:.5px; color:rgba(255,238,248,.8); }
@media (max-width:640px){
  .sk-spot{ width:28px; height:28px; }
  .sk-icon{ width:20px; height:20px; }
}
</style>
