<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 弦歌行 · 四弦节奏 ============ */
const LANES = [
  { n: '宫', key: 'D', freq: 261.63, c: '#FFB3D2' },
  { n: '商', key: 'F', freq: 293.66, c: '#D9B6FA' },
  { n: '角', key: 'J', freq: 329.63, c: '#A8DFF0' },
  { n: '徵', key: 'K', freq: 392.00, c: '#FFD9A0' }
]
const TRAVEL = 2100      // 音符从出现到判定线的时长(ms)
const PERFECT = 130
const GOOD = 235
const TOTAL_NOTES = 42
const BEST_KEY = 'guiniang_rhythm_best'

const state = ref('idle')      // idle | playing | done
const notes = ref([])
const clock = ref(0)
const score = ref(0)
const combo = ref(0)
const maxCombo = ref(0)
const stat = ref({ perfect: 0, good: 0, miss: 0 })
const flash = ref([false, false, false, false])
const waves = ref([])
const muted = ref(false)
const best = ref(0)
const endAt = ref(0)

let raf = 0
let zero = 0
let ac = null
let waveSeq = 0
const timers = []
const later = (fn, ms) => { const id = setTimeout(fn, ms); timers.push(id) }

function readBest() {
  try { return Number(localStorage.getItem(BEST_KEY) || 0) || 0 } catch { return 0 }
}
best.value = readBest()

function ensureAudio() {
  if (!ac) {
    const Ctx = window.AudioContext || window.webkitAudioContext
    if (Ctx) ac = new Ctx()
  }
  if (ac && ac.state === 'suspended') ac.resume()
}
function pluck(freq, miss) {
  if (!ac || muted.value) return
  const t = ac.currentTime
  const osc = ac.createOscillator()
  const gain = ac.createGain()
  const lp = ac.createBiquadFilter()
  lp.type = 'lowpass'
  lp.frequency.value = miss ? 420 : 2600
  osc.type = miss ? 'sine' : 'triangle'
  osc.frequency.setValueAtTime(freq, t)
  if (!miss) osc.frequency.exponentialRampToValueAtTime(freq * 0.994, t + 0.4)
  const peak = miss ? 0.05 : 0.16
  gain.gain.setValueAtTime(0.0001, t)
  gain.gain.exponentialRampToValueAtTime(peak, t + 0.012)
  gain.gain.exponentialRampToValueAtTime(0.0001, t + (miss ? 0.28 : 0.6))
  osc.connect(lp).connect(gain).connect(ac.destination)
  osc.start(t)
  osc.stop(t + 0.65)
}
function buildChart() {
  const list = []
  let t = 900
  for (let i = 0; i < TOTAL_NOTES; i++) {
    list.push({ id: i, lane: Math.floor(Math.random() * LANES.length), t0: t, judged: '', ok: false })
    const gap = i % 8 === 7 ? 300 : 430 + Math.random() * 240
    t += gap
  }
  endAt.value = t + TRAVEL + 900
  return list
}
function start() {
  notes.value = buildChart()
  score.value = 0
  combo.value = 0
  maxCombo.value = 0
  stat.value = { perfect: 0, good: 0, miss: 0 }
  waves.value = []
  clock.value = 0
  state.value = 'playing'
  ensureAudio()
  zero = performance.now()
  cancelAnimationFrame(raf)
  raf = requestAnimationFrame(loop)
}
function loop() {
  clock.value = performance.now() - zero
  for (const n of notes.value) {
    if (!n.judged && clock.value > n.t0 + TRAVEL + GOOD) {
      n.judged = 'miss'
      stat.value.miss++
      combo.value = 0
      pluck(LANES[n.lane].freq, true)
    }
  }
  if (clock.value > endAt.value) { finish(); return }
  raf = requestAnimationFrame(loop)
}
function finish() {
  cancelAnimationFrame(raf)
  state.value = 'done'
  if (score.value > best.value) {
    best.value = score.value
    try { localStorage.setItem(BEST_KEY, String(best.value)) } catch (e) { /* ignore */ }
  }
}
function judge(n, kind) {
  n.judged = kind
  n.ok = true
  combo.value++
  if (combo.value > maxCombo.value) maxCombo.value = combo.value
  if (kind === 'perfect') { stat.value.perfect++; score.value += 100 + Math.min(60, combo.value * 2) }
  else { stat.value.good++; score.value += 55 + Math.min(30, combo.value) }
  const id = ++waveSeq
  waves.value.push({ id, lane: n.lane, kind })
  later(() => { waves.value = waves.value.filter(w => w.id !== id) }, 620)
}
function press(lane) {
  if (state.value !== 'playing') return
  ensureAudio()
  flash.value[lane] = true
  later(() => { flash.value[lane] = false }, 130)
  let target = null
  let dist = 1e9
  for (const n of notes.value) {
    if (n.judged || n.lane !== lane) continue
    const d = Math.abs(clock.value - (n.t0 + TRAVEL))
    if (d < dist) { dist = d; target = n }
  }
  if (target && dist <= GOOD) {
    judge(target, dist <= PERFECT ? 'perfect' : 'good')
    pluck(LANES[lane].freq, false)
  } else {
    pluck(LANES[lane].freq, true)
  }
}
function keyHandler(e) {
  if (e.repeat) return
  const k = e.key.toUpperCase()
  const lane = LANES.findIndex(l => l.key === k)
  if (lane >= 0) { e.preventDefault(); press(lane) }
}
onMounted(() => window.addEventListener('keydown', keyHandler))
onUnmounted(() => {
  window.removeEventListener('keydown', keyHandler)
  cancelAnimationFrame(raf)
  timers.forEach(clearTimeout)
  if (ac) { try { ac.close() } catch (e) { /* ignore */ } }
})

function noteY(n) {
  return ((clock.value - n.t0) / TRAVEL) * 100
}
const accuracy = computed(() => {
  const j = stat.value.perfect + stat.value.good + stat.value.miss
  if (!j) return 0
  return Math.round((stat.value.perfect + stat.value.good * 0.6) / j * 100)
})
const rank = computed(() => {
  const a = accuracy.value
  if (a >= 92) return '知音'
  if (a >= 78) return '清角'
  if (a >= 60) return '下里'
  return '初学'
})
</script>

<template>
  <div class="gx-game rhythm">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#9FC6FF,#6A8EDE)">弦</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 音律</p>
        <h2 class="gx-title">弦歌行</h2>
      </div>
      <div class="gx-headright">
        <button class="gx-btn ghost mini" type="button" @click="muted = !muted">{{ muted ? '开声' : '静音' }}</button>
      </div>
    </header>

    <div class="gx-hud">
      <span class="gx-stat">得分 <b>{{ score }}</b></span>
      <span class="gx-stat jade">连击 <b>{{ combo }}</b></span>
      <span class="gx-stat">准度 <b>{{ accuracy }}%</b></span>
      <span class="gx-stat">最佳 <b>{{ best }}</b></span>
      <span class="gx-spacer"></span>
      <span class="rh-life">
        <i :class="{ on: state === 'playing' }">宫</i><i :class="{ on: state === 'playing' }">商</i>
        <i :class="{ on: state === 'playing' }">角</i><i :class="{ on: state === 'playing' }">徵</i>
      </span>
    </div>

    <div class="gx-stage">
      <div class="gx-stage-in">
        <div class="rh-stage">
          <div class="rh-lanes">
            <div v-for="(l, i) in LANES" :key="l.n" class="rh-lane" :class="{ flash: flash[i] }"
                 :style="{ '--c': l.c }" @click="press(i)">
              <span class="rh-string"></span>
              <span class="rh-line"></span>
              <span class="rh-key">{{ l.key }}</span>
              <span class="rh-note-name">{{ l.n }}</span>
              <span v-if="state === 'idle'" class="rh-demo" :style="{ '--d': (i * 0.55) + 's' }"></span>
              <span v-for="w in waves.filter(x => x.lane === i)" :key="w.id" class="rh-wave"
                    :class="w.kind"></span>
            </div>
          </div>

          <span v-for="n in notes" :key="n.id" v-show="!n.judged" class="rh-note"
                :class="{ soon: noteY(n) > 88 }"
                :style="{ left: (n.lane * 25 + 12.5) + '%', top: noteY(n) + '%',
                          '--c': LANES[n.lane].c }"></span>

          <div class="rh-hitline"></div>

          <div v-if="state === 'idle'" class="rh-cover">
            <p class="rh-cover-title">按宫商角徵，抚一曲春江月</p>
            <p class="rh-cover-sub">D · F · J · K 对应四弦；也可直接点弦。音符落到金线时击弦。</p>
            <button class="gx-btn gold" type="button" @click="start">起弦</button>
          </div>

          <div v-if="state === 'done'" class="gx-result">
            <div class="gx-result-card">
              <div class="gx-seal">{{ rank.slice(0, 1) }}</div>
              <h3>{{ rank }} · {{ score }} 分</h3>
              <p>
                妙音 {{ stat.perfect }} · 和音 {{ stat.good }} · 失弦 {{ stat.miss }}<br>
                最高连击 {{ maxCombo }}，准度 {{ accuracy }}%。
              </p>
              <div class="gx-foot">
                <button class="gx-btn primary" type="button" @click="start">再抚一曲</button>
                <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <p class="gx-hint">音符落至金线时击弦：越准越高分，连击另有加成。错过会断连击。</p>
    <div class="gx-foot">
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.rh-stage{
  position:relative; height:min(420px,54vh); margin:0 auto; padding:0 14px; max-width:520px;
  overflow:hidden;
}
.rh-stage::before{
  content:""; position:absolute; z-index:0; left:14px; right:14px; top:0; bottom:0; border-radius:16px;
  background:
    radial-gradient(90% 54% at 50% 104%, rgba(255,176,110,.22), transparent 72%),
    linear-gradient(180deg,rgba(60,32,70,.55),rgba(28,14,44,.35));
  box-shadow:inset 0 0 40px rgba(0,0,0,.45);
  pointer-events:none;
}
.rh-lanes{ position:relative; z-index:1; display:grid; grid-template-columns:repeat(4,1fr); gap:8px; height:100%; }
.rh-lane{
  position:relative; border-radius:12px; cursor:pointer; overflow:hidden;
  background:linear-gradient(180deg,rgba(255,255,255,.02),rgba(255,214,170,.10));
  border:1px solid rgba(233,193,115,.22);
  box-shadow:inset 0 -60px 70px -40px rgba(255,170,110,.40);
  transition:background .12s, box-shadow .12s;
}
.rh-lane.flash{
  background:linear-gradient(180deg,rgba(255,255,255,.06),rgba(255,255,255,.18));
  box-shadow:inset 0 0 26px rgba(255,255,255,.22), 0 0 22px rgba(255,214,170,.5);
}
.rh-string{
  position:absolute; left:50%; top:0; bottom:0; width:2px; transform:translateX(-50%);
  background:linear-gradient(180deg,transparent,rgba(255,240,210,.55) 16%,rgba(255,255,255,.95) 52%,rgba(255,240,210,.5) 84%,transparent);
  opacity:.9; box-shadow:0 0 10px rgba(255,220,170,.45);
}
.rh-lane.flash .rh-string{ opacity:1; box-shadow:0 0 12px var(--c); }
.rh-line{
  position:absolute; left:4%; right:4%; bottom:13%; height:2px;
  background:linear-gradient(90deg,transparent,rgba(233,193,115,.9),transparent);
  box-shadow:0 0 14px rgba(255,200,120,.7);
}
.rh-key{
  position:absolute; left:50%; bottom:3%; transform:translateX(-50%);
  width:26px; height:26px; border-radius:50%; display:flex; align-items:center; justify-content:center;
  font-size:10px; letter-spacing:0; color:#3B2B45; font-weight:700;
  background:linear-gradient(140deg,#FFF2D4,#E9C173);
  box-shadow:0 0 12px rgba(255,205,130,.55), inset 0 1px 0 rgba(255,255,255,.7);
}
.rh-note-name{
  position:absolute; left:50%; top:6%; transform:translateX(-50%);
  font-family:var(--gx-serif); font-size:12px; color:rgba(255,236,205,.55); letter-spacing:1px;
  text-shadow:0 0 10px rgba(255,200,140,.35);
}
.rh-note{
  position:absolute; width:26px; height:26px; margin:-13px 0 0 -13px; border-radius:50% 50% 50% 0;
  transform:rotate(-45deg); pointer-events:none; z-index:3;
  background:radial-gradient(circle at 34% 30%, #fff, var(--c) 55%, rgba(24,10,36,.92));
  box-shadow:0 0 16px rgba(255,214,240,.55), 0 2px 6px rgba(0,0,0,.4);
}
.rh-note.soon{ filter:brightness(1.25); }
.rh-note::after{
  content:""; position:absolute; inset:4px; border-radius:50%; border:1px solid rgba(255,255,255,.55);
}
.rh-wave{
  position:absolute; left:50%; bottom:13%; width:20px; height:20px; margin-left:-10px; margin-bottom:-10px;
  border-radius:50%; border:2px solid rgba(255,235,180,.9); pointer-events:none;
  animation:rh-wave .6s ease-out forwards;
}
.rh-wave.good{ border-color:rgba(160,220,255,.85); }
@keyframes rh-wave{
  0%{ transform:scale(.5); opacity:1 }
  100%{ transform:scale(3.6); opacity:0 }
}
.rh-hitline{
  position:absolute; left:14px; right:14px; bottom:calc(13% - 1px); height:2px; pointer-events:none;
  background:linear-gradient(90deg,transparent,rgba(255,220,150,.35),transparent);
}
.rh-demo{
  position:absolute; left:50%; top:-10%; width:22px; height:22px; margin-left:-11px;
  border-radius:50% 50% 50% 0; transform:rotate(-45deg);
  background:radial-gradient(circle at 34% 30%, #fff, var(--c) 58%, rgba(24,10,36,.9));
  box-shadow:0 0 14px rgba(255,214,240,.45);
  opacity:0; animation:rh-demo 3.2s linear infinite; animation-delay:var(--d);
  pointer-events:none;
}
@keyframes rh-demo{
  0%{ top:-10%; opacity:0 }
  12%{ opacity:.38 }
  86%{ opacity:.38 }
  100%{ top:84%; opacity:0 }
}
.rh-cover{
  position:absolute; inset:0; z-index:6; display:flex; flex-direction:column;
  align-items:center; justify-content:center; gap:12px; text-align:center; padding:20px;
  background:radial-gradient(66% 54% at 50% 50%, rgba(18,8,30,.84) 0%, rgba(14,6,24,.46) 66%, rgba(14,6,24,.08) 100%);
}
.rh-cover-title{ margin:0; font-family:var(--gx-serif); font-size:17px; letter-spacing:3px; color:#FFE9B8; }
.rh-cover-sub{ margin:0; font-size:11px; line-height:1.9; letter-spacing:1px; color:rgba(255,232,244,.62); max-width:300px; }
.rh-life{ display:flex; gap:5px; }
.rh-life i{
  width:20px; height:20px; border-radius:7px; font-style:normal; font-size:9px;
  display:flex; align-items:center; justify-content:center;
  color:rgba(255,232,244,.4); background:rgba(255,255,255,.06);
  border:1px solid rgba(255,255,255,.1); transition:.3s;
}
.rh-life i.on{ color:#FFE9B8; border-color:rgba(233,193,115,.45); box-shadow:0 0 10px rgba(255,200,120,.3); }
@media (max-width:640px){
  .rh-stage{ height:min(330px,46vh); }
  .rh-note{ width:22px; height:22px; margin:-11px 0 0 -11px; }
}
</style>
