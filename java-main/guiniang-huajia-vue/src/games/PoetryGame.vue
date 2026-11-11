<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 飞花令 ============ */
const POEMS = {
  花: [
    { s: '花间一壶酒，独酌无相亲', a: '李白' },
    { s: '落花人独立，微雨燕双飞', a: '晏几道' },
    { s: '夜来风雨声，花落知多少', a: '孟浩然' },
    { s: '待到重阳日，还来就菊花', a: '孟浩然' },
    { s: '竹外桃花三两枝，春江水暖鸭先知', a: '苏轼' },
    { s: '人闲桂花落，夜静春山空', a: '王维' },
    { s: '沾衣欲湿杏花雨，吹面不寒杨柳风', a: '志南' },
    { s: '春城无处不飞花，寒食东风御柳斜', a: '韩翃' },
    { s: '桃花潭水深千尺，不及汪伦送我情', a: '李白' },
    { s: '忽如一夜春风来，千树万树梨花开', a: '岑参' },
    { s: '感时花溅泪，恨别鸟惊心', a: '杜甫' },
    { s: '落红不是无情物，化作春泥更护花', a: '龚自珍' }
  ],
  月: [
    { s: '举头望明月，低头思故乡', a: '李白' },
    { s: '海上生明月，天涯共此时', a: '张九龄' },
    { s: '明月松间照，清泉石上流', a: '王维' },
    { s: '露从今夜白，月是故乡明', a: '杜甫' },
    { s: '月落乌啼霜满天，江枫渔火对愁眠', a: '张继' },
    { s: '春江潮水连海平，海上明月共潮生', a: '张若虚' },
    { s: '明月几时有，把酒问青天', a: '苏轼' },
    { s: '我寄愁心与明月，随君直到夜郎西', a: '李白' },
    { s: '深林人不知，明月来相照', a: '王维' },
    { s: '月出惊山鸟，时鸣春涧中', a: '王维' },
    { s: '二十四桥明月夜，玉人何处教吹箫', a: '杜牧' },
    { s: '星垂平野阔，月涌大江流', a: '杜甫' }
  ],
  春: [
    { s: '春眠不觉晓，处处闻啼鸟', a: '孟浩然' },
    { s: '好雨知时节，当春乃发生', a: '杜甫' },
    { s: '春风又绿江南岸，明月何时照我还', a: '王安石' },
    { s: '春色满园关不住，一枝红杏出墙来', a: '叶绍翁' },
    { s: '等闲识得东风面，万紫千红总是春', a: '朱熹' },
    { s: '春潮带雨晚来急，野渡无人舟自横', a: '韦应物' },
    { s: '谁言寸草心，报得三春晖', a: '孟郊' },
    { s: '春蚕到死丝方尽，蜡炬成灰泪始干', a: '李商隐' },
    { s: '野火烧不尽，春风吹又生', a: '白居易' },
    { s: '春宵一刻值千金，花有清香月有阴', a: '苏轼' },
    { s: '国破山河在，城春草木深', a: '杜甫' },
    { s: '两个黄鹂鸣翠柳，一行白鹭上青天', a: '杜甫' }
  ],
  风: [
    { s: '风急天高猿啸哀，渚清沙白鸟飞回', a: '杜甫' },
    { s: '夜来风雨声，花落知多少', a: '孟浩然' },
    { s: '大风起兮云飞扬', a: '刘邦' },
    { s: '不知细叶谁裁出，二月春风似剪刀', a: '贺知章' },
    { s: '千里莺啼绿映红，水村山郭酒旗风', a: '杜牧' },
    { s: '随风潜入夜，润物细无声', a: '杜甫' },
    { s: '长风破浪会有时，直挂云帆济沧海', a: '李白' },
    { s: '东风夜放花千树', a: '辛弃疾' },
    { s: '风住尘香花已尽，日晚倦梳头', a: '李清照' },
    { s: '溪云初起日沉阁，山雨欲来风满楼', a: '许浑' },
    { s: '春风得意马蹄疾，一日看尽长安花', a: '孟郊' },
    { s: '北风卷地白草折，胡天八月即飞雪', a: '岑参' }
  ]
}
const LING = ['花', '月', '春', '风']
const ROUND_WIN = 6

const ling = ref('')
const pool = ref([])
const used = ref(new Set())
const scroll = ref([])
const options = ref([])
const turn = ref('player')
const life = ref(3)
const score = ref(0)
const combo = ref(0)
const time = ref(30)
const over = ref(false)
const win = ref(false)
const msg = ref('')
const started = computed(() => !!ling.value)
let timer = null

const rest = () => pool.value.filter(p => !used.value.has(p.i))

function begin(l) {
  ling.value = l
  pool.value = POEMS[l].map((p, i) => ({ ...p, i })).sort(() => Math.random() - .5)
  used.value = new Set()
  scroll.value = []
  options.value = []
  score.value = 0
  combo.value = 0
  life.value = 3
  over.value = false
  win.value = false
  msg.value = ''
  turn.value = 'ai'
  setTimeout(aiMove, 500)
}
function aiMove() {
  if (over.value) return
  const left = rest()
  if (!left.length) { finish(true); return }
  const p = left[Math.floor(Math.random() * left.length)]
  used.value.add(p.i)
  scroll.value.unshift({ who: '社长', s: p.s, a: p.a })
  turn.value = 'player'
  time.value = 30
  buildOptions()
}
function buildOptions() {
  const left = rest()
  const good = left.filter(p => p.s.includes(ling.value))
  const bag = left.slice()
  const picked = []
  if (good.length) picked.push(good[Math.floor(Math.random() * good.length)])
  while (picked.length < 4 && bag.length) {
    const k = Math.floor(Math.random() * bag.length)
    const cand = bag.splice(k, 1)[0]
    if (!picked.includes(cand)) picked.push(cand)
  }
  options.value = picked.sort(() => Math.random() - .5)
}
function choose(p) {
  if (turn.value !== 'player' || over.value) return
  if (!p.s.includes(ling.value)) {
    life.value--
    combo.value = 0
    msg.value = '此句不见「' + ling.value + '」字，罚酒一盅'
    if (life.value <= 0) { finish(false); return }
    time.value = 30
    buildOptions()
    return
  }
  used.value.add(p.i)
  combo.value++
  score.value += 10 + combo.value * 3
  scroll.value.unshift({ who: '我', s: p.s, a: p.a, mine: true })
  msg.value = ''
  turn.value = 'ai'
  if (scroll.value.length >= ROUND_WIN * 2) { finish(true); return }
  setTimeout(aiMove, 900)
}
function finish(w) {
  if (over.value) return
  over.value = true
  win.value = w
  if (w) score.value += 60
}
onMounted(() => {
  timer = setInterval(() => {
    if (!started.value || over.value || turn.value !== 'player') return
    time.value--
    if (time.value <= 0) {
      life.value--
      combo.value = 0
      msg.value = '沉吟太久，罚酒一盅'
      if (life.value <= 0) { finish(false); return }
      time.value = 30
      buildOptions()
    }
  }, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="gx-game poetry">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#8FD6C0,#4FA98E)">诗</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 诗词</p>
        <h2 class="gx-title">飞花令</h2>
      </div>
      <div class="gx-headright">
        <button v-if="started" class="gx-btn ghost mini" type="button" @click="begin(ling)">重开一局</button>
      </div>
    </header>

    <div class="gx-hud">
      <span class="gx-stat">令字 <b>{{ ling || '未定' }}</b></span>
      <span class="gx-stat jade">雅句 <b>{{ scroll.length }}</b></span>
      <span class="gx-stat">得分 <b>{{ score }}</b></span>
      <span class="gx-spacer"></span>
      <span class="py-life">
        <i v-for="k in 3" :key="k" :class="{ off: k > life }"></i>
      </span>
      <span v-if="started && !over" class="py-clock" :class="{ warn: time <= 8 }">{{ time }}s</span>
    </div>

    <div class="gx-stage">
      <div class="gx-stage-in py-wrap">
        <!-- 卷轴 -->
        <div class="py-scroll">
          <div class="py-rod top"></div>
          <div class="py-paper">
            <div v-if="!started" class="py-empty">
              <p>花间一局，以诗为令。</p>
              <p>择一字，与社长对句。</p>
            </div>
            <ul v-else class="py-lines">
              <li v-for="(l, i) in scroll" :key="i" :class="{ mine: l.mine }">
                <span class="py-who">{{ l.who }}</span>
                <p>{{ l.s }}</p>
                <span class="py-author">—— {{ l.a }}</span>
              </li>
            </ul>
          </div>
          <div class="py-rod bottom"></div>
          <div v-if="ling" class="py-seal">{{ ling }}</div>
        </div>

        <!-- 对句区 -->
        <div class="py-main">
          <div v-if="!started" class="py-choose">
            <p class="py-ask">今日以何字为令？</p>
            <div class="py-ling">
              <button v-for="l in LING" :key="l" type="button" class="py-ling-btn" @click="begin(l)">{{ l }}</button>
            </div>
            <p class="gx-hint">轮流接出含令字的诗句，接不上或接错罚酒一盅；三盅为限。</p>
          </div>
          <template v-else>
            <p class="py-turn">
              <template v-if="turn === 'player' && !over">轮到你 · 请接一句含「{{ ling }}」的诗</template>
              <template v-else-if="over">{{ win ? '一局终了 · 你赢了' : '一局终了 · 社长胜' }}</template>
              <template v-else>社长正在沉吟……</template>
            </p>
            <div class="py-options">
              <button v-for="(p, i) in options" :key="i" type="button" class="py-opt"
                      :disabled="turn !== 'player' || over" @click="choose(p)">
                <span class="py-opt-s">{{ p.s }}</span>
                <span class="py-opt-a">{{ p.a }}</span>
              </button>
            </div>
          </template>
        </div>

        <div v-if="over" class="gx-result">
          <div class="gx-result-card">
            <div class="gx-seal">{{ win ? '胜' : '惜' }}</div>
            <h3>{{ win ? '诗成夺锦' : '酒尽诗残' }}</h3>
            <p>共接 {{ scroll.length }} 句，得 {{ score }} 分。<br>
              {{ win ? '社长拱手：后生可畏，明日再会。' : '社长举杯：诗兴未尽，明日再来。' }}</p>
            <div class="gx-foot">
              <button class="gx-btn primary" type="button" @click="begin(ling)">再来一局</button>
              <button class="gx-btn ghost" type="button" @click="ling = ''">换一字</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="gx-toast">{{ msg }}</div>
    <div class="gx-foot">
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.py-wrap{ display:flex; gap:20px; align-items:stretch; flex-wrap:wrap; padding:18px 20px 20px; }
.py-scroll{ position:relative; width:min(300px,100%); flex:none; display:flex; flex-direction:column; }
.py-rod{
  height:12px; border-radius:6px; flex:none;
  background:linear-gradient(180deg,#7C5A38,#4E3620 60%,#2E1E12);
  box-shadow:0 3px 8px rgba(0,0,0,.5), inset 0 1px 0 rgba(255,215,160,.35);
}
.py-paper{
  flex:1; min-height:250px; max-height:300px; overflow-y:auto; padding:16px 16px 14px;
  background:
    radial-gradient(120% 80% at 50% 0%, rgba(255,255,255,.85), transparent 70%),
    linear-gradient(180deg,#FBF3E2,#F2E5CB 70%,#EADCBE);
  box-shadow:inset 0 0 30px rgba(140,100,50,.22), inset 0 1px 0 rgba(255,255,255,.7);
  border-left:1px solid rgba(140,100,50,.18); border-right:1px solid rgba(140,100,50,.18);
}
.py-empty{ text-align:center; padding-top:60px; color:#8A6A4A; font-family:var(--gx-serif); letter-spacing:2px; font-size:13px; }
.py-empty p{ margin:0 0 8px; }
.py-lines{ list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:12px; }
.py-lines li{ animation:gx-enter .35s ease; }
.py-who{ font-size:9.5px; letter-spacing:2px; color:#A98A64; }
.py-lines li p{
  margin:3px 0 2px; font-family:var(--gx-serif); font-size:13.5px; line-height:1.85;
  color:#4A362A; letter-spacing:.5px;
}
.py-lines li.mine p{ color:#8E2F44; }
.py-author{ font-size:10px; color:#A08A6C; letter-spacing:1px; }
.py-seal{
  position:absolute; right:12px; bottom:26px; width:44px; height:44px; border-radius:8px;
  display:flex; align-items:center; justify-content:center;
  font-family:var(--gx-serif); font-size:24px; color:#FFF0EE;
  background:linear-gradient(150deg,#D9463F,#A8271F);
  box-shadow:0 4px 14px rgba(120,20,20,.45), inset 0 1px 0 rgba(255,255,255,.35);
  transform:rotate(-6deg); opacity:.92;
}
.py-main{ flex:1; min-width:240px; display:flex; flex-direction:column; justify-content:center; gap:10px; }
.py-ask{ margin:0 0 4px; font-family:var(--gx-serif); font-size:17px; letter-spacing:3px; color:#FFE9B8; text-align:center; }
.py-ling{ display:flex; gap:12px; justify-content:center; flex-wrap:wrap; margin:6px 0 12px; }
.py-ling-btn{
  width:62px; height:62px; border-radius:16px; cursor:pointer;
  font-family:var(--gx-serif); font-size:28px; color:#FFF2E4;
  background:linear-gradient(150deg,rgba(217,70,63,.9),rgba(140,36,36,.9));
  border:1px solid rgba(255,220,180,.4);
  box-shadow:0 8px 20px rgba(120,20,20,.4), inset 0 1px 0 rgba(255,255,255,.3);
  transition:.22s cubic-bezier(.22,1,.36,1);
}
.py-ling-btn:hover{ transform:translateY(-4px) scale(1.04); box-shadow:0 14px 28px rgba(160,40,40,.5), 0 0 26px rgba(255,140,120,.45); }
.py-turn{
  margin:0 0 10px; font-family:var(--gx-serif); font-size:13px; letter-spacing:2px;
  color:#FFE0B8; text-align:center;
}
.py-options{ display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.py-opt{
  text-align:left; cursor:pointer; padding:12px 13px; border-radius:13px;
  background:linear-gradient(165deg,rgba(255,255,255,.13),rgba(255,255,255,.05));
  border:1px solid rgba(233,193,115,.28);
  box-shadow:inset 0 1px 0 rgba(255,255,255,.18), 0 6px 16px rgba(0,0,0,.22);
  transition:.2s cubic-bezier(.22,1,.36,1); display:flex; flex-direction:column; gap:5px;
}
.py-opt:hover:not(:disabled){
  transform:translateY(-3px); border-color:rgba(255,180,212,.6);
  box-shadow:0 12px 24px rgba(0,0,0,.35), 0 0 20px rgba(255,150,190,.2);
}
.py-opt:disabled{ opacity:.5; cursor:default; }
.py-opt-s{ font-family:var(--gx-serif); font-size:12.5px; line-height:1.8; color:#FFF1E8; letter-spacing:.5px; }
.py-opt-a{ font-size:10px; color:rgba(255,232,244,.5); letter-spacing:1px; }
.py-life{ display:flex; gap:5px; align-items:center; }
.py-life i{
  width:13px; height:13px; border-radius:50%;
  background:radial-gradient(circle at 35% 30%,#FFD9E4,#E0567A 70%,#A83350);
  box-shadow:0 0 10px rgba(255,110,150,.55), inset 0 1px 0 rgba(255,255,255,.5);
  transition:.3s;
}
.py-life i.off{ background:#3A2A44; box-shadow:inset 0 1px 3px rgba(0,0,0,.5); }
.py-clock{ font-family:var(--gx-serif); font-size:13px; letter-spacing:1px; color:#B6F0DA; }
.py-clock.warn{ color:#FF9E9E; animation:gx-breathe 1s infinite; }
@media (max-width:700px){
  .py-wrap{ gap:14px; padding:14px; }
  .py-scroll{ width:100%; }
  .py-paper{ max-height:190px; min-height:170px; }
  .py-options{ grid-template-columns:1fr; }
}
</style>
