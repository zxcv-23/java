<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import WeavePattern from './WeavePattern.vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 锦绣织坊 ============ */
const KINDS = [
  { k: 'tuan', n: '团花纹', poem: '团花簇锦，圆满如意' },
  { k: 'chanzhi', n: '缠枝纹', poem: '缠枝不断，生生不息' },
  { k: 'yun', n: '云纹', poem: '如意云头，平步青云' }
]
const ATLAS_KEY = 'guiniang_weave_atlas'

const N = ref(3)
const kindIdx = ref(0)
const order = ref([])
const sel = ref(null)
const moves = ref(0)
const secs = ref(0)
const done = ref(false)
const justDone = ref(false)
const atlas = ref([])
let timer = null

const kind = computed(() => KINDS[kindIdx.value])
const cell = computed(() => 300 / N.value)
const total = computed(() => N.value * N.value)
const fixed = computed(() => order.value.filter((v, i) => v === i).length)
const ratio = computed(() => Math.round(fixed.value / total.value * 100))

function vb(p) {
  return `${(p % N.value) * cell.value} ${Math.floor(p / N.value) * cell.value} ${cell.value} ${cell.value}`
}
function shuffle(a) {
  const b = a.slice()
  for (let i = b.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[b[i], b[j]] = [b[j], b[i]]
  }
  return b
}
function readAtlas() {
  try { const a = JSON.parse(localStorage.getItem(ATLAS_KEY) || '[]'); return Array.isArray(a) ? a : [] }
  catch { return [] }
}
function reset() {
  let arr
  let guard = 0
  do {
    arr = shuffle(Array.from({ length: total.value }, (_, i) => i))
    guard++
  } while (guard < 30 && arr.every((v, i) => v === i))
  order.value = arr
  sel.value = null
  moves.value = 0
  secs.value = 0
  done.value = false
  justDone.value = false
}
function setSize(n) {
  if (N.value === n) return
  N.value = n
  reset()
}
function tap(i) {
  if (done.value) return
  const s = sel.value
  if (s === null) { sel.value = i; return }
  if (s === i) { sel.value = null; return }
  const a = order.value.slice()
  const t = a[s]
  a[s] = a[i]
  a[i] = t
  order.value = a
  moves.value++
  sel.value = null
  if (a.every((v, k) => v === k)) complete()
}
function complete() {
  done.value = true
  justDone.value = true
  const k = kind.value.k
  if (!atlas.value.includes(k)) {
    atlas.value = [...atlas.value, k]
    try { localStorage.setItem(ATLAS_KEY, JSON.stringify(atlas.value)) } catch (e) { /* ignore */ }
  }
}
function nextPattern() {
  kindIdx.value = (kindIdx.value + 1) % KINDS.length
  reset()
}
atlas.value = readAtlas()
reset()
onMounted(() => {
  timer = setInterval(() => { if (!done.value) secs.value++ }, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="gx-game weave">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#F2B0D8,#C06AA8)">锦</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 拼图</p>
        <h2 class="gx-title">锦绣织坊</h2>
      </div>
      <div class="gx-headright">
        <button class="gx-btn ghost mini" type="button" @click="reset">重新起织</button>
      </div>
    </header>

    <div class="gx-hud">
      <span class="gx-stat">纹样 <b>{{ kind.n }}</b></span>
      <span class="gx-stat jade">已归位 <b>{{ fixed }}</b> / {{ total }}</span>
      <span class="gx-stat">换手 <b>{{ moves }}</b></span>
      <span class="gx-stat">时辰 <b>{{ secs }}</b>s</span>
      <span class="gx-spacer"></span>
      <div class="bar"><i :style="{ width: ratio + '%' }"></i></div>
    </div>

    <div class="gx-stage">
      <div class="gx-stage-in wv-wrap">
        <div class="wv-loom">
          <div class="wv-panel" :class="{ done }">
            <div class="wv-grid" :style="{ gridTemplateColumns: 'repeat(' + N + ',1fr)' }">
              <div v-for="(p, i) in order" :key="i" class="wv-piece"
                   :class="{ sel: sel === i, ok: p === i }" @click="tap(i)">
                <div class="wv-cloth">
                  <svg class="wv-svg" :viewBox="vb(p)" preserveAspectRatio="xMidYMid slice">
                    <WeavePattern :type="kind.k" />
                  </svg>
                </div>
                <span class="wv-no">{{ i + 1 }}</span>
              </div>
            </div>
            <span class="wv-shine"></span>
          </div>
          <p class="wv-poem">{{ kind.poem }}</p>
        </div>

        <div class="wv-side">
          <div class="wv-group">
            <p>纹样</p>
            <div class="wv-chips">
              <button v-for="(k, i) in KINDS" :key="k.k" type="button" class="gx-chip"
                      :class="{ on: kindIdx === i }" @click="kindIdx = i; reset()">{{ k.n }}</button>
            </div>
          </div>
          <div class="wv-group">
            <p>幅面</p>
            <div class="wv-chips">
              <button type="button" class="gx-chip" :class="{ on: N === 3 }" @click="setSize(3)">三三 · 九块</button>
              <button type="button" class="gx-chip" :class="{ on: N === 4 }" @click="setSize(4)">四四 · 十六块</button>
            </div>
          </div>
          <div class="wv-group">
            <p>图鉴 <b>{{ atlas.length }} / {{ KINDS.length }}</b></p>
            <div class="wv-atlas">
              <span v-for="k in KINDS" :key="k.k" class="atlas-item"
                    :class="{ got: atlas.includes(k.k) }" :title="k.n">{{ k.n.slice(0, 1) }}</span>
            </div>
          </div>
          <p class="gx-hint">点两块互换位置，把散落的锦片拼回整幅。归位的碎块会亮起金边。</p>
        </div>

        <div v-if="justDone" class="gx-result">
          <div class="gx-result-card">
            <div class="gx-seal">锦</div>
            <h3>{{ kind.n }} · 织成</h3>
            <p>换手 {{ moves }} 次，用时 {{ secs }} 秒。<br>{{ kind.poem }}。<br>
              图鉴已收 {{ atlas.length }} / {{ KINDS.length }}。
            </p>
            <div class="gx-foot">
              <button class="gx-btn primary" type="button" @click="reset">再织一幅</button>
              <button class="gx-btn gold" type="button" @click="nextPattern">换一种纹样</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="gx-foot">
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.wv-wrap{ display:flex; gap:22px; align-items:center; justify-content:center; flex-wrap:wrap; padding:18px 20px 20px; }
.wv-loom{
  position:relative; flex:none; width:min(330px,76vw);
}
.wv-panel{
  position:relative; overflow:hidden; border-radius:14px; padding:10px;
  background:
    repeating-linear-gradient(90deg, rgba(0,0,0,.18) 0 1px, transparent 1px 6px),
    linear-gradient(160deg,#5B3A28,#3D2418 60%,#281710);
  border:1px solid rgba(233,193,115,.45);
  box-shadow:inset 0 0 22px rgba(0,0,0,.5), 0 16px 36px rgba(0,0,0,.45);
}
.wv-grid{ display:grid; gap:3px; position:relative; z-index:2; }
.wv-piece{
  position:relative; aspect-ratio:1; cursor:pointer; overflow:hidden;
  transition:transform .18s cubic-bezier(.22,1,.36,1), box-shadow .2s;
  box-shadow:inset 0 0 0 1px rgba(0,0,0,.35);
}
.wv-cloth{
  position:absolute; inset:0;
  background:
    repeating-linear-gradient(45deg, rgba(255,255,255,.055) 0 1px, transparent 1px 4px),
    repeating-linear-gradient(-45deg, rgba(0,0,0,.10) 0 1px, transparent 1px 5px),
    linear-gradient(160deg,#96354C,#6E2138 55%,#4A1424);
}
.wv-svg{ position:absolute; inset:0; width:100%; height:100%; display:block; }
.wv-no{
  position:absolute; right:3px; bottom:2px; font-size:8px; letter-spacing:0;
  color:rgba(255,240,220,.35); z-index:2;
}
.wv-piece:hover{ z-index:3; transform:scale(1.03); box-shadow:0 0 0 1px rgba(255,220,160,.5), 0 6px 16px rgba(0,0,0,.5); }
.wv-piece.sel{
  z-index:4; transform:scale(1.05);
  box-shadow:0 0 0 2px #FFE9B8, 0 0 22px rgba(255,210,130,.75);
}
.wv-piece.ok::after{
  content:""; position:absolute; inset:0; box-shadow:inset 0 0 0 1px rgba(255,215,140,.5); pointer-events:none;
}
.wv-shine{
  position:absolute; inset:0; z-index:3; pointer-events:none; opacity:0;
  background:linear-gradient(115deg,transparent 32%,rgba(255,240,200,.55) 46%,transparent 60%);
  background-size:220% 100%;
}
.wv-panel.done .wv-shine{ animation:gx-shimmer 2.2s linear infinite; opacity:1; }
.wv-panel.done{ box-shadow:inset 0 0 22px rgba(0,0,0,.4), 0 0 34px rgba(255,200,120,.45); }
.wv-poem{
  margin:10px 0 0; text-align:center; font-family:var(--gx-serif); font-size:11.5px;
  letter-spacing:2px; color:rgba(255,232,244,.6);
}
.wv-side{ flex:1; min-width:210px; display:flex; flex-direction:column; gap:12px; }
.wv-group{ padding-bottom:10px; border-bottom:1px dashed rgba(233,193,115,.2); }
.wv-group:last-of-type{ border-bottom:0; padding-bottom:0; }
.wv-group p{
  margin:0 0 7px; font-size:10.5px; letter-spacing:2px; color:rgba(255,232,244,.5);
  display:flex; align-items:center; gap:7px;
}
.wv-group p b{ font-family:var(--gx-serif); font-size:12px; color:#FFE9B8; }
.wv-chips{ display:flex; gap:7px; flex-wrap:wrap; }
.wv-atlas{ display:flex; gap:8px; }
.atlas-item{
  width:32px; height:32px; border-radius:9px; display:flex; align-items:center; justify-content:center;
  font-family:var(--gx-serif); font-size:14px; color:rgba(255,232,244,.28);
  background:rgba(255,255,255,.05); border:1px solid rgba(255,255,255,.1);
  transition:.25s;
}
.atlas-item.got{
  color:#5A3A18; background:linear-gradient(140deg,#FFEFC6,#E9C173);
  border-color:transparent; box-shadow:0 0 14px rgba(255,205,130,.45), inset 0 1px 0 rgba(255,255,255,.6);
}
.bar{ width:110px; height:6px; border-radius:6px; overflow:hidden; background:rgba(255,255,255,.12); }
.bar i{ display:block; height:100%; border-radius:6px; background:linear-gradient(90deg,#F2B0D8,#E9C173); transition:width .3s; }
@media (max-width:640px){
  .wv-wrap{ gap:14px; padding:14px; }
  .wv-loom{ width:min(300px,80vw); }
  .wv-side{ min-width:100%; }
}
</style>
