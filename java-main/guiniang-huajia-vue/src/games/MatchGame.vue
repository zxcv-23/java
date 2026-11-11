<script setup>
import { ref, computed, onUnmounted } from 'vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 落樱消消乐 ============ */
const TYPES = [
  { ch: '樱', a: '#FFC5DC', b: '#E1568B', glow: 'rgba(255,140,185,.55)' },
  { ch: '棠', a: '#DFC2FB', b: '#8A5FD0', glow: 'rgba(180,130,250,.5)' },
  { ch: '叶', a: '#C2EFD3', b: '#4E9E74', glow: 'rgba(120,225,170,.45)' },
  { ch: '灯', a: '#FFE0B8', b: '#DE8A3E', glow: 'rgba(255,185,100,.5)' },
  { ch: '月', a: '#CBE3FB', b: '#5C8CC8', glow: 'rgba(130,185,245,.5)' }
]
const SIZE = 6
const MOVES = 20
const TARGET = 1200

const board = ref([])
const sel = ref(null)
const busy = ref(false)
const moves = ref(MOVES)
const score = ref(0)
const combo = ref(0)
const comboFlash = ref('')
const clearing = ref(new Set())
const sparks = ref([])
const toast = ref('')
const over = ref(false)

let toastTimer = null
let sparkSeq = 0
const timers = []
const sleep = ms => new Promise(r => setTimeout(r, ms))
const later = (fn, ms) => { const id = setTimeout(fn, ms); timers.push(id); return id }

function commit() { board.value = board.value.map(row => row.slice()) }
function say(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1700)
}
function newType() { return TYPES[Math.floor(Math.random() * TYPES.length)] }
const progress = computed(() => Math.min(100, Math.round(score.value / TARGET * 100)))

function burst(r, c, t) {
  for (let i = 0; i < 5; i++) {
    const a = Math.random() * Math.PI * 2
    const dist = 16 + Math.random() * 34
    const id = ++sparkSeq
    sparks.value.push({
      id, r, c, color: t.b,
      dx: Math.cos(a) * dist, dy: Math.sin(a) * dist - 8,
      size: 3 + Math.random() * 4, rot: Math.random() * 360
    })
    later(() => { sparks.value = sparks.value.filter(s => s.id !== id) }, 720)
  }
}
function sparksAt(r, c) { return sparks.value.filter(s => s.r === r && s.c === c) }

function findMatches() {
  const b = board.value
  const m = new Set()
  for (let r = 0; r < SIZE; r++) {
    let run = 1
    for (let c = 1; c <= SIZE; c++) {
      if (c < SIZE && b[r][c] && b[r][c - 1] && b[r][c].ch === b[r][c - 1].ch) run++
      else { if (run >= 3) for (let k = c - run; k < c; k++) m.add(r + ',' + k); run = 1 }
    }
  }
  for (let c = 0; c < SIZE; c++) {
    let run = 1
    for (let r = 1; r <= SIZE; r++) {
      if (r < SIZE && b[r][c] && b[r - 1][c] && b[r][c].ch === b[r - 1][c].ch) run++
      else { if (run >= 3) for (let k = r - run; k < r; k++) m.add(k + ',' + c); run = 1 }
    }
  }
  return m
}
function buildBoard() {
  for (let attempt = 0; attempt < 40; attempt++) {
    const b = []
    for (let r = 0; r < SIZE; r++) {
      const row = []
      for (let c = 0; c < SIZE; c++) {
        let t, guard = 0
        do { t = newType(); guard++ }
        while (guard < 40 && ((c >= 2 && row[c - 1] === t && row[c - 2] === t) ||
                              (r >= 2 && b[r - 1][c] === t && b[r - 2][c] === t)))
        row.push(t)
      }
      b.push(row)
    }
    board.value = b
    if (findMatches().size === 0) return
  }
}
function init() {
  sel.value = null; busy.value = false; moves.value = MOVES
  score.value = 0; combo.value = 0; comboFlash.value = ''
  clearing.value = new Set(); sparks.value = []; toast.value = ''; over.value = false
  buildBoard()
}
init()

async function resolveMatches() {
  let chain = 0
  while (true) {
    const m = findMatches()
    if (!m.size) break
    chain++
    const gain = m.size * 10 * chain
    score.value += gain
    m.forEach(k => {
      const [r, c] = k.split(',').map(Number)
      const t = board.value[r][c]
      if (t) burst(r, c, t)
      board.value[r][c] = null
    })
    clearing.value = new Set(m)
    commit()
    if (chain > 1) {
      comboFlash.value = '连击 ×' + chain + '  +' + gain
      later(() => (comboFlash.value = ''), 900)
    }
    await sleep(220)
    const b = board.value
    for (let c = 0; c < SIZE; c++) {
      let write = SIZE - 1
      for (let r = SIZE - 1; r >= 0; r--) {
        if (b[r][c] !== null) {
          const v = b[r][c]
          if (write !== r) b[r][c] = null
          b[write][c] = v
          write--
        }
      }
      for (let r = write; r >= 0; r--) b[r][c] = newType()
    }
    clearing.value = new Set()
    sel.value = null
    commit()
    await sleep(160)
  }
  if (score.value >= TARGET) finish(true)
}
async function trySwap(a, b2) {
  if (busy.value || over.value) return
  busy.value = true
  const b = board.value
  const tmp = b[a.r][a.c]
  b[a.r][a.c] = b[b2.r][b2.c]
  b[b2.r][b2.c] = tmp
  sel.value = null
  commit()
  if (findMatches().size === 0) {
    await sleep(170)
    const t2 = b[a.r][a.c]
    b[a.r][a.c] = b[b2.r][b2.c]
    b[b2.r][b2.c] = t2
    commit()
    say('这两枚换不出花样，再寻一对～')
    busy.value = false
    return
  }
  moves.value--
  await resolveMatches()
  busy.value = false
  commit()
  if (!over.value && moves.value <= 0) finish(false)
}
function finish(win) {
  if (over.value) return
  over.value = true
  busy.value = false
  say('')
  if (win) score.value += moves.value * 25
}
function onCell(r, c) {
  if (busy.value || over.value || moves.value <= 0) return
  const s = sel.value
  if (!s) { sel.value = { r, c }; return }
  if (s.r === r && s.c === c) { sel.value = null; return }
  if (Math.abs(s.r - r) + Math.abs(s.c - c) !== 1) { sel.value = { r, c }; return }
  trySwap(s, { r, c })
}
onUnmounted(() => {
  clearTimeout(toastTimer)
  timers.forEach(clearTimeout)
})
</script>

<template>
  <div class="gx-game match">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#FF9EC4,#E4548D)">消</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 消除</p>
        <h2 class="gx-title">落樱消消乐</h2>
      </div>
      <div class="gx-headright">
        <button class="gx-btn ghost mini" type="button" @click="init">重新开始</button>
      </div>
    </header>

    <div class="gx-hud">
      <span class="gx-stat">得分 <b>{{ score }}</b></span>
      <span class="gx-stat jade">剩余步数 <b>{{ moves }}</b></span>
      <span class="gx-stat">目标 <b>{{ TARGET }}</b></span>
      <span class="gx-spacer"></span>
      <div class="bar"><i :style="{ width: progress + '%' }"></i></div>
    </div>

    <div class="gx-stage">
      <div class="gx-stage-in board-wrap">
        <div class="board">
          <template v-for="(row, r) in board" :key="'r' + r">
            <div v-for="(t, c) in row" :key="r + '-' + c" class="cell" @click="onCell(r, c)">
              <div v-if="t" class="tile"
                   :class="{ sel: sel && sel.r === r && sel.c === c, clearing: clearing.has(r + ',' + c) }"
                   :style="{ '--a': t.a, '--b': t.b, '--glow': t.glow }">
                <span class="tile-ch">{{ t.ch }}</span>
                <i class="tile-shine"></i>
              </div>
              <span v-for="s in sparksAt(r, c)" :key="s.id" class="spark"
                    :style="{ background: s.color, width: s.size + 'px', height: s.size + 'px',
                              '--dx': s.dx + 'px', '--dy': s.dy + 'px', '--rot': s.rot + 'deg' }"></span>
            </div>
          </template>
        </div>

        <transition name="pop">
          <div v-if="comboFlash" class="combo">{{ comboFlash }}</div>
        </transition>

        <div v-if="over" class="gx-result">
          <div class="gx-result-card">
            <div class="gx-seal">{{ score >= TARGET ? '樱' : '惜' }}</div>
            <h3>{{ score >= TARGET ? '满庭花雨' : '春事未了' }}</h3>
            <p v-if="score >= TARGET">本局 {{ score }} 分，落樱已尽数收进花囊。<br>余 {{ moves }} 步化作 {{ moves * 25 }} 分花红。</p>
            <p v-else>本局 {{ score }} 分，还差 {{ Math.max(0, TARGET - score) }} 分。<br>再拾三瓣樱花，续一院春愁。</p>
            <div class="gx-foot">
              <button class="gx-btn primary" type="button" @click="init">再来一局</button>
              <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <p class="gx-hint">点相邻两枚花签交换位置，三枚同色连成一线即可消去；连锁消除有额外加成。{{ toast }}</p>
    <div class="gx-foot">
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.board-wrap{ display:flex; align-items:center; justify-content:center; padding:18px 16px 20px; }
.board{
  display:grid; grid-template-columns:repeat(6,1fr); gap:9px;
  width:min(486px,80vw); padding:14px; border-radius:18px;
  background:
    radial-gradient(120% 80% at 50% 0%, rgba(255,200,230,.10), transparent 62%),
    linear-gradient(160deg,rgba(255,255,255,.07),rgba(255,255,255,.02));
  border:1px solid rgba(233,193,115,.28);
  box-shadow:inset 0 0 30px rgba(0,0,0,.38), 0 10px 30px rgba(6,2,16,.35);
}
.cell{ position:relative; aspect-ratio:1; }
.tile{
  position:absolute; inset:0; border-radius:14px; cursor:pointer;
  display:flex; align-items:center; justify-content:center;
  background:radial-gradient(circle at 32% 24%, var(--a), var(--b) 76%);
  box-shadow:
    inset 0 2px 3px rgba(255,255,255,.55),
    inset 0 -5px 9px rgba(0,0,0,.26),
    0 5px 12px rgba(0,0,0,.38),
    0 0 0 1px rgba(255,255,255,.16);
  transition:transform .17s cubic-bezier(.22,1,.36,1), opacity .2s, box-shadow .2s;
  overflow:hidden;
}
.tile::after{
  content:""; position:absolute; inset:auto 12% 10% 12%; height:16%;
  border-radius:50%; background:rgba(255,255,255,.20); filter:blur(3px);
}
.tile-ch{
  font-family:var(--gx-serif); font-size:21px; font-weight:600; color:#fff;
  text-shadow:0 1px 0 rgba(0,0,0,.28), 0 0 12px var(--glow);
  transform:translateY(-1px);
}
.tile-shine{
  position:absolute; top:-40%; left:-70%; width:60%; height:180%;
  background:linear-gradient(90deg,transparent,rgba(255,255,255,.42),transparent);
  transform:rotate(18deg); opacity:0; transition:opacity .2s;
}
.tile:hover .tile-shine{ opacity:1; animation:gx-shimmer 1.6s linear infinite; }
.tile:hover{ transform:translateY(-2px) scale(1.03); }
.tile.sel{
  transform:scale(1.09); z-index:3;
  box-shadow:0 0 0 2px #fff, 0 0 0 4px rgba(255,214,120,.9), 0 0 26px var(--glow), inset 0 2px 4px rgba(255,255,255,.5);
}
.tile.clearing{ transform:scale(.15) rotate(18deg); opacity:0; }
.spark{
  position:absolute; left:50%; top:50%; border-radius:50%; z-index:5; pointer-events:none;
  animation:spark .68s cubic-bezier(.2,.7,.3,1) forwards;
  box-shadow:0 0 10px currentColor;
}
@keyframes spark{
  0%{ transform:translate(-50%,-50%) scale(.9); opacity:1 }
  100%{ transform:translate(calc(-50% + var(--dx)),calc(-50% + var(--dy))) scale(.2) rotate(var(--rot)); opacity:0 }
}
.combo{
  position:absolute; left:50%; top:14%; transform:translateX(-50%);
  font-family:var(--gx-serif); font-size:19px; letter-spacing:2px; color:#FFE9B8;
  text-shadow:0 0 18px rgba(255,190,110,.7); pointer-events:none; z-index:7;
}
.pop-leave-active{ transition:.5s; }
.pop-leave-to{ opacity:0; transform:translate(-50%,-16px); }
.bar{
  width:120px; height:6px; border-radius:6px; overflow:hidden;
  background:rgba(255,255,255,.12); box-shadow:inset 0 1px 2px rgba(0,0,0,.35);
}
.bar i{
  display:block; height:100%; border-radius:6px;
  background:linear-gradient(90deg,#FFC98A,#FF8FB4 60%,#E4548D);
  box-shadow:0 0 10px rgba(255,150,190,.6);
  transition:width .35s cubic-bezier(.22,1,.36,1);
}
@media (max-width:640px){
  .board{ width:min(360px,86vw); gap:6px; padding:9px; }
  .tile-ch{ font-size:18px; }
}
</style>
