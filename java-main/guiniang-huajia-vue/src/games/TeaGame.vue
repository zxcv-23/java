<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 茶寮物语 · 4×4 合成 ============ */
const N = 4
const LEVELS = [
  { ch: '芽', n: '茶芽', a: '#EAF8DA', b: '#A8CE86' },
  { ch: '叶', n: '嫩叶', a: '#D6EFC0', b: '#86B566' },
  { ch: '焙', n: '初焙', a: '#BFE3A6', b: '#6B9C4C' },
  { ch: '青', n: '青茶', a: '#A5D68A', b: '#54853C' },
  { ch: '白', n: '白茶', a: '#F2EEDF', b: '#C0B695' },
  { ch: '绿', n: '绿茶', a: '#8FCB75', b: '#41702F' },
  { ch: '乌', n: '乌龙', a: '#E0A876', b: '#A86236' },
  { ch: '袍', n: '红袍', a: '#D98468', b: '#96392B' },
  { ch: '饼', n: '茶饼', a: '#C08A5E', b: '#7A4A2C' },
  { ch: '贡', n: '贡茶', a: '#FFE9A8', b: '#C2943E' }
]
const WIN_LEVEL = 10        // LEVELS 第 10 味「贡茶」的等级值
const BEST_KEY = 'guiniang_tea_best'

const grid = ref([])
const score = ref(0)
const best = ref(0)
const over = ref(false)
const win = ref(false)
const keepGoing = ref(false)
const toast = ref('')
const pops = ref(new Set())
const steam = ref([])
let toastTimer = null

function emptyGrid() {
  return Array.from({ length: N }, () => Array(N).fill(0))
}
function readBest() {
  try { return Number(localStorage.getItem(BEST_KEY) || 0) || 0 } catch { return 0 }
}
function say(m) {
  toast.value = m
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1500)
}
function addTile() {
  const empties = []
  grid.value.forEach((row, r) => row.forEach((v, c) => { if (!v) empties.push([r, c]) }))
  if (!empties.length) return false
  const [r, c] = empties[Math.floor(Math.random() * empties.length)]
  grid.value[r][c] = Math.random() < 0.88 ? 1 : 2
  return true
}
function canMove() {
  const g = grid.value
  for (let r = 0; r < N; r++) for (let c = 0; c < N; c++) {
    if (!g[r][c]) return true
    if (c < N - 1 && g[r][c] === g[r][c + 1]) return true
    if (r < N - 1 && g[r][c] === g[r + 1][c]) return true
  }
  return false
}
function init() {
  grid.value = emptyGrid()
  score.value = 0
  over.value = false
  win.value = false
  keepGoing.value = false
  pops.value = new Set()
  addTile(); addTile()
  best.value = readBest()
  steam.value = Array.from({ length: 7 }, (_, i) => ({
    id: i,
    left: (12 + Math.random() * 76).toFixed(1) + '%',
    delay: (Math.random() * 5).toFixed(1) + 's',
    dur: (4 + Math.random() * 3).toFixed(1) + 's',
    size: (10 + Math.random() * 12).toFixed(0) + 'px'
  }))
}
init()

function lineOf(g, i, dir) {
  if (dir === 'left') return g[i].slice()
  if (dir === 'right') return g[i].slice().reverse()
  if (dir === 'up') return g.map(row => row[i])
  return g.map(row => row[i]).reverse()
}
function writeLine(g, i, dir, line) {
  if (dir === 'left') { g[i] = line.slice(); return }
  if (dir === 'right') { g[i] = line.slice().reverse(); return }
  if (dir === 'up') { line.forEach((v, r) => { g[r][i] = v }); return }
  const rev = line.slice().reverse()
  rev.forEach((v, r) => { g[r][i] = v })
}
function collapse(line) {
  const a = line.filter(v => v)
  const out = []
  let gain = 0
  for (let i = 0; i < a.length; i++) {
    if (a[i] === a[i + 1] && a[i] < WIN_LEVEL) {
      out.push(a[i] + 1)
      gain += Math.pow(2, a[i]) * 5
      i++
    } else out.push(a[i])
  }
  while (out.length < N) out.push(0)
  return { out, gain }
}
function move(dir) {
  if (over.value) return
  const g = grid.value.map(row => row.slice())
  let moved = false, gain = 0
  for (let i = 0; i < N; i++) {
    const line = lineOf(g, i, dir)
    const { out, gain: gg } = collapse(line)
    gain += gg
    if (out.some((v, k) => v !== line[k])) moved = true
    writeLine(g, i, dir, out)
  }
  if (!moved) {
    say('这一步推不动茶盘')
    return
  }
  score.value += gain
  grid.value = g
  addTile()
  if (score.value > best.value) {
    best.value = score.value
    try { localStorage.setItem(BEST_KEY, String(best.value)) } catch (e) { /* ignore */ }
  }
  if (!keepGoing.value && g.flat().some(v => v >= WIN_LEVEL)) {
    win.value = true
    return
  }
  if (!canMove()) over.value = true
}
function keyHandler(e) {
  const k = e.key
  const map = {
    ArrowLeft: 'left', ArrowRight: 'right', ArrowUp: 'up', ArrowDown: 'down',
    a: 'left', d: 'right', w: 'up', s: 'down',
    A: 'left', D: 'right', W: 'up', S: 'down'
  }
  if (!map[k]) return
  e.preventDefault()
  move(map[k])
}
let tx = 0, ty = 0
function onTouchStart(e) {
  const t = e.changedTouches[0]
  tx = t.clientX; ty = t.clientY
}
function onTouchEnd(e) {
  const t = e.changedTouches[0]
  const dx = t.clientX - tx, dy = t.clientY - ty
  if (Math.abs(dx) < 24 && Math.abs(dy) < 24) return
  if (Math.abs(dx) > Math.abs(dy)) move(dx > 0 ? 'right' : 'left')
  else move(dy > 0 ? 'down' : 'up')
}
onMounted(() => window.addEventListener('keydown', keyHandler))
onUnmounted(() => {
  window.removeEventListener('keydown', keyHandler)
  clearTimeout(toastTimer)
})

const tiles = computed(() => {
  const list = []
  grid.value.forEach((row, r) => row.forEach((v, c) => {
    if (v) list.push({ r, c, v, lv: LEVELS[v - 1] || LEVELS[LEVELS.length - 1] })
  }))
  return list
})
const topLevel = computed(() => Math.max(1, ...grid.value.flat()))
const progress = computed(() => Math.min(100, Math.round((topLevel.value - 1) / (WIN_LEVEL - 1) * 100)))
</script>

<template>
  <div class="gx-game tea">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#A8D8A0,#5FA86A)">茶</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 合成</p>
        <h2 class="gx-title">茶寮物语</h2>
      </div>
      <div class="gx-headright">
        <button class="gx-btn ghost mini" type="button" @click="init">重开茶席</button>
      </div>
    </header>

    <div class="gx-hud">
      <span class="gx-stat">茶金 <b>{{ score }}</b></span>
      <span class="gx-stat jade">最佳 <b>{{ best }}</b></span>
      <span class="gx-stat">这一味 <b>{{ LEVELS[topLevel - 1].n }}</b></span>
      <span class="gx-spacer"></span>
      <div class="bar"><i :style="{ width: progress + '%' }"></i></div>
    </div>

    <div class="gx-stage">
      <div class="gx-stage-in tea-wrap">
        <div class="tea-board"
             @touchstart.passive="onTouchStart" @touchend.passive="onTouchEnd">
          <span v-for="s in steam" :key="'st' + s.id" class="steam"
                :style="{ left: s.left, animationDelay: s.delay, animationDuration: s.dur, width: s.size, height: s.size }"></span>
          <div class="tea-grid">
            <div v-for="i in N * N" :key="'c' + i" class="tea-cell"></div>
          </div>
          <div class="tea-tiles">
            <div v-for="t in tiles" :key="'t' + t.r + '-' + t.c" class="tea-tile"
                 :class="{ hi: t.v >= 7 }"
                 :style="{ '--c': t.c, '--r': t.r, '--a': t.lv.a, '--b': t.lv.b }">
              <span class="tea-ch">{{ t.lv.ch }}</span>
              <span class="tea-name">{{ t.lv.n }}</span>
            </div>
          </div>

          <div v-if="win && !keepGoing" class="gx-result">
            <div class="gx-result-card">
              <div class="gx-seal">贡</div>
              <h3>一盏贡茶</h3>
              <p>茶烟起处，满座皆惊。你已合出「贡茶」，<br>茶寮自此名动一方。</p>
              <div class="gx-foot">
                <button class="gx-btn gold" type="button" @click="keepGoing = true; win = false">继续煮茶</button>
                <button class="gx-btn primary" type="button" @click="init">再开一席</button>
              </div>
            </div>
          </div>
          <div v-if="over" class="gx-result">
            <div class="gx-result-card">
              <div class="gx-seal">尽</div>
              <h3>茶盘已满</h3>
              <p>本席得茶金 {{ score }}，最高一味 {{ LEVELS[topLevel - 1].n }}。<br>添水再煮，又是一席好茶。</p>
              <div class="gx-foot">
                <button class="gx-btn primary" type="button" @click="init">再开一席</button>
                <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
              </div>
            </div>
          </div>
        </div>

        <div class="tea-side">
          <div class="tea-pad">
            <button class="pad-btn up" type="button" @click="move('up')" aria-label="向上">↑</button>
            <button class="pad-btn left" type="button" @click="move('left')" aria-label="向左">←</button>
            <button class="pad-btn down" type="button" @click="move('down')" aria-label="向下">↓</button>
            <button class="pad-btn right" type="button" @click="move('right')" aria-label="向右">→</button>
          </div>
          <p class="tea-tip">方向键 / WASD<br>或在茶盘上滑动</p>
          <div class="tea-chain">
            <span v-for="(l, i) in LEVELS" :key="l.n" class="chain-dot"
                  :style="{ '--a': l.a, '--b': l.b }" :title="l.n"
                  :class="{ on: topLevel - 1 === i }"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="gx-toast">{{ toast }}</div>
    <p class="gx-hint">同味相撞即合成更高一味，一路推到「贡茶」。每次推盘都会落下一枚新茶。</p>
    <div class="gx-foot">
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.tea-wrap{ display:flex; gap:18px; align-items:center; justify-content:center; flex-wrap:wrap; padding:18px 20px 20px; }
.tea-board{
  position:relative; width:min(400px,82vw); aspect-ratio:1; padding:10px; border-radius:18px;
  background:
    radial-gradient(120% 90% at 30% 10%, rgba(255,220,170,.16), transparent 60%),
    repeating-linear-gradient(92deg, rgba(0,0,0,.16) 0 1px, transparent 1px 7px),
    linear-gradient(160deg,#5B3A28,#3D2418 60%,#2A1710);
  border:1px solid rgba(233,193,115,.4);
  box-shadow:inset 0 0 26px rgba(0,0,0,.55), 0 14px 32px rgba(0,0,0,.45);
  touch-action:none;
}
.tea-grid{ position:absolute; inset:10px; display:grid; grid-template-columns:repeat(4,1fr); grid-template-rows:repeat(4,1fr); gap:8px; }
.tea-cell{ border-radius:12px; background:rgba(0,0,0,.22); box-shadow:inset 0 2px 5px rgba(0,0,0,.45), inset 0 -1px 0 rgba(255,255,255,.06); }
.tea-tiles{ position:absolute; inset:10px; z-index:2; pointer-events:none; }
.tea-tile{
  position:absolute;
  left:calc(var(--c) * (100% + 8px) / 4);
  top:calc(var(--r) * (100% + 8px) / 4);
  width:calc((100% - 24px) / 4);
  height:calc((100% - 24px) / 4);
  border-radius:13px; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:1px;
  background:radial-gradient(circle at 34% 24%, var(--a), var(--b) 78%);
  box-shadow:
    inset 0 2px 3px rgba(255,255,255,.55),
    inset 0 -5px 9px rgba(0,0,0,.28),
    0 5px 12px rgba(0,0,0,.4),
    0 0 0 1px rgba(255,255,255,.14);
  animation:gx-pop .22s cubic-bezier(.22,1,.36,1);
  transition:left .16s cubic-bezier(.22,1,.36,1), top .16s cubic-bezier(.22,1,.36,1);
  overflow:hidden;
}
.tea-tile::after{
  content:""; position:absolute; top:-30%; left:-60%; width:55%; height:160%;
  background:linear-gradient(90deg,transparent,rgba(255,255,255,.35),transparent);
  transform:rotate(18deg); opacity:0; transition:opacity .2s;
}
.tea-tile:hover::after{ opacity:1; }
.tea-tile.hi{ box-shadow:inset 0 2px 3px rgba(255,255,255,.6), inset 0 -5px 9px rgba(0,0,0,.28), 0 6px 16px rgba(0,0,0,.45), 0 0 18px rgba(255,205,120,.35); }
.tea-ch{ font-family:var(--gx-serif); font-size:22px; font-weight:700; color:#FFFDF6; text-shadow:0 1px 2px rgba(0,0,0,.35); }
.tea-name{ font-size:8.5px; letter-spacing:1px; color:rgba(255,255,255,.85); }
.steam{
  position:absolute; bottom:22%; border-radius:50%; pointer-events:none; z-index:5;
  background:radial-gradient(circle,rgba(255,255,255,.42),transparent 70%);
  filter:blur(4px); animation:tea-steam linear infinite; opacity:0;
}
@keyframes tea-steam{
  0%{ transform:translateY(0) scale(.7); opacity:0 }
  30%{ opacity:.55 }
  100%{ transform:translateY(-86px) scale(1.6); opacity:0 }
}
.tea-side{ display:flex; flex-direction:column; align-items:center; gap:12px; }
.tea-pad{ position:relative; width:132px; height:132px; }
.pad-btn{
  position:absolute; width:42px; height:42px; border-radius:12px; cursor:pointer;
  border:1px solid rgba(233,193,115,.35); background:rgba(255,255,255,.08); color:#FFE9B8;
  font-size:15px; transition:.18s;
}
.pad-btn:hover{ background:rgba(255,255,255,.2); transform:scale(1.05); }
.pad-btn.up{ left:45px; top:0; } .pad-btn.down{ left:45px; bottom:0; }
.pad-btn.left{ left:0; top:45px; } .pad-btn.right{ right:0; top:45px; }
.tea-tip{ margin:0; font-size:10px; line-height:1.8; letter-spacing:1px; color:rgba(255,232,244,.45); text-align:center; }
.tea-chain{ display:flex; gap:5px; flex-wrap:wrap; justify-content:center; max-width:130px; }
.chain-dot{
  width:14px; height:14px; border-radius:50%;
  background:radial-gradient(circle at 34% 28%, var(--a), var(--b) 78%);
  box-shadow:inset 0 -2px 4px rgba(0,0,0,.3), 0 2px 5px rgba(0,0,0,.35);
  opacity:.55; transition:.2s;
}
.chain-dot.on{ opacity:1; transform:scale(1.25); box-shadow:0 0 12px rgba(255,210,130,.7), inset 0 -2px 4px rgba(0,0,0,.3); }
.bar{ width:110px; height:6px; border-radius:6px; overflow:hidden; background:rgba(255,255,255,.12); }
.bar i{ display:block; height:100%; border-radius:6px; background:linear-gradient(90deg,#A8D8A0,#E9C173); transition:width .35s; }
@media (max-width:640px){
  .tea-board{ width:min(300px,82vw); }
  .tea-side{ flex-direction:row; gap:14px; flex-wrap:wrap; justify-content:center; }
  .tea-chain{ display:none; }
}
</style>
