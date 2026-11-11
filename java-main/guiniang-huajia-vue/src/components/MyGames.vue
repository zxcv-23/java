<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import MatchGame from '../games/MatchGame.vue'
import DressGame from '../games/DressGame.vue'
import MakeupGame from '../games/MakeupGame.vue'
import PoetryGame from '../games/PoetryGame.vue'
import SeekGame from '../games/SeekGame.vue'
import TeaGame from '../games/TeaGame.vue'
import RhythmGame from '../games/RhythmGame.vue'
import WeaveGame from '../games/WeaveGame.vue'

const emit = defineEmits(['close'])

/* ============ 小游戏清单（八款全开 · 简体中文 · UTF-8） ============ */
const GAMES = [
  { id: 'xiaoxiao', name: '落樱消消乐', mark: '消', cat: '消除', tags: ['三消', '闯关'],
    desc: '拾起三瓣樱花，消去一院春愁。', color: 'linear-gradient(135deg,#FF9EC4,#E4548D)',
    rule: '点相邻两枚花签交换位置，三枚同色连成一线即消；二十步内冲击一千二百分，连锁消除另有加成。' },
  { id: 'huanshang', name: '云裳换装记', mark: '裳', cat: '换装', tags: ['搭配', '收集'],
    desc: '罗裙、发髻、披帛，妆成一场花朝。', color: 'linear-gradient(135deg,#C9A0F0,#8A5FD0)',
    rule: '为画中人挑裙色、发型、披帛与发饰，立绘可随鼠标轻转；合心的造型能收进本地衣柜，随时再穿。' },
  { id: 'zhuangxia', name: '胭脂妆匣', mark: '妆', cat: '换装', tags: ['妆容', '休闲'],
    desc: '点一抹朱唇，描两弯远山黛。', color: 'linear-gradient(135deg,#FFB3A7,#E8708F)',
    rule: '择眉形、眼影、胭脂、唇色与花钿，铜镜里的妆面即时变化；定妆后评一个妆品，看看能得几分。' },
  { id: 'feihualing', name: '飞花令', mark: '诗', cat: '休闲', tags: ['诗词', '接龙'],
    desc: '以诗会友，一句一令，看谁先折枝。', color: 'linear-gradient(135deg,#8FD6C0,#4FA98E)',
    rule: '择「花」「月」「春」「风」一字为令，与社长轮流接诗；接错或超时罚酒，三盅为限，接满六轮即胜。' },
  { id: 'xunfang', name: '宫墙寻芳', mark: '寻', cat: '解谜', tags: ['寻物', '解谜'],
    desc: '雕栏画栋间，寻回遗落的花笺。', color: 'linear-gradient(135deg,#FFC98A,#E39A4F)',
    rule: '在宫墙、花枝、灯影间点出藏着的五件信物，点空扣三秒；六十秒内寻齐即算圆满。' },
  { id: 'chalia', name: '茶寮物语', mark: '茶', cat: '经营', tags: ['经营', '合成'],
    desc: '煮一盏清茶，待四方来客。', color: 'linear-gradient(135deg,#A8D8A0,#5FA86A)',
    rule: '推盘让同味相撞，一路由茶芽合成贡茶；方向键、WASD 或滑动皆可，最高分留在本地。' },
  { id: 'xiange', name: '弦歌行', mark: '弦', cat: '休闲', tags: ['音律', '节奏'],
    desc: '按宫商角徵羽，抚一曲春江月。', color: 'linear-gradient(135deg,#9FC6FF,#6A8EDE)',
    rule: '音符落到金线时击弦，D / F / J / K 对应宫商角徵；越准分越高，连击另有加成，曲终评一个琴品。' },
  { id: 'zhifang', name: '锦绣织坊', mark: '锦', cat: '经营', tags: ['拼图', '手作'],
    desc: '织就云锦，拼出十里红妆。', color: 'linear-gradient(135deg,#F2B0D8,#C06AA8)',
    rule: '点两块锦片互换，把散落的纹样拼回整幅；团花、缠枝、云纹三种纹样可换，九块十六块任选。' }
]
const PLAY = {
  xiaoxiao: MatchGame,
  huanshang: DressGame,
  zhuangxia: MakeupGame,
  feihualing: PoetryGame,
  xunfang: SeekGame,
  chalia: TeaGame,
  xiange: RhythmGame,
  zhifang: WeaveGame
}
const CATS = ['全部', '消除', '换装', '解谜', '经营', '休闲']

const cat = ref('全部')
const query = ref('')
const view = ref('list')          // list | detail | play
const current = ref(null)

const filtered = computed(() => GAMES.filter(g => {
  const okCat = cat.value === '全部' || g.cat === cat.value
  const text = (g.name + g.desc + g.tags.join('') + g.cat).toLowerCase()
  return okCat && (!query.value || text.includes(query.value.trim().toLowerCase()))
}))
const playComp = computed(() => (current.value ? PLAY[current.value.id] : null))
const indexOf = id => String(GAMES.findIndex(g => g.id === id) + 1).padStart(2, '0')

/* ============ 轮播 ============ */
const SLIDES = [
  { tag: '全部开放', title: '八款花间小游戏 · 今日齐开', desc: '消除、换装、诗词、寻物、经营、音律、拼图，点开即玩' },
  { tag: '新上线', title: '胭脂妆匣 · 描一笔远山黛', desc: '眉形眼影胭脂唇色花钿，铜镜里即时换妆' },
  { tag: '新上线', title: '弦歌行 · 按宫商角徵抚一曲', desc: '四弦节奏，击准金线，曲终评一个琴品' }
]
const slideIdx = ref(0)
let slideTimer = null
function showSlide(i) { slideIdx.value = (i + SLIDES.length) % SLIDES.length }
function stepSlide(d) { showSlide(slideIdx.value + d); restartSlides() }
function pickSlide(i) { showSlide(i); restartSlides() }
function restartSlides() {
  clearInterval(slideTimer)
  slideTimer = setInterval(() => showSlide(slideIdx.value + 1), 5400)
}

/* ============ 装饰：星点 / 花瓣 / 樱花 ============ */
const stars = Array.from({ length: 72 }, () => {
  const s = Math.random() * 2.2 + 1
  return {
    w: s.toFixed(1) + 'px', h: s.toFixed(1) + 'px',
    left: (Math.random() * 100).toFixed(1) + '%',
    top: (Math.random() * 80).toFixed(1) + '%',
    delay: (Math.random() * 4).toFixed(1) + 's'
  }
})
const petals = Array.from({ length: 28 }, () => ({
  left: (Math.random() * 100).toFixed(1) + '%',
  drift: (Math.random() * 260 - 130).toFixed(0) + 'px',
  spin: (Math.random() * 720 + 360).toFixed(0) + 'deg',
  dur: (Math.random() * 9 + 9).toFixed(1) + 's',
  delay: (-Math.random() * 18).toFixed(1) + 's',
  scale: (Math.random() * .7 + .6).toFixed(2)
}))
const motes = Array.from({ length: 16 }, () => ({
  left: (Math.random() * 100).toFixed(1) + '%',
  bottom: (Math.random() * 40).toFixed(1) + '%',
  dur: (Math.random() * 8 + 9).toFixed(1) + 's',
  delay: (-Math.random() * 14).toFixed(1) + 's',
  size: (Math.random() * 3 + 2).toFixed(1) + 'px'
}))
function makeBlossoms(pts, color) {
  const list = [], cores = []
  pts.forEach(([x, y]) => {
    for (let k = 0; k < 5; k++) {
      const a = k * 72 * Math.PI / 180
      list.push({
        key: x + '-' + y + '-' + k,
        cx: +(x + Math.cos(a) * 9).toFixed(1),
        cy: +(y + Math.sin(a) * 9).toFixed(1),
        rot: k * 72, fill: color
      })
    }
    cores.push({ key: x + '-' + y, cx: x, cy: y })
  })
  return { list, cores }
}
const blossomL = makeBlossoms(
  [[40, 22], [95, 58], [150, 92], [198, 132], [60, 112], [172, 168], [214, 220], [118, 150], [30, 80]], '#FFB9D7')
const blossomR = makeBlossoms(
  [[400, 18], [350, 52], [294, 92], [248, 134], [386, 110], [276, 172], [236, 220], [330, 152], [420, 80]], '#FFC4DD')

/* ============ 弹层流程 ============ */
function openDetail(g) { current.value = g; view.value = 'detail' }
function startPlay() { if (current.value && PLAY[current.value.id]) view.value = 'play' }
function back() { view.value = 'list'; current.value = null }
function backDetail() { view.value = 'detail' }
function doSearch() { query.value = query.value.trim() }
function pickCat(c) { cat.value = c }
function onKey(e) {
  if (e.key !== 'Escape') return
  if (view.value === 'play') { view.value = 'detail'; return }
  if (view.value === 'detail') { back(); return }
  emit('close')
}
onMounted(() => {
  document.addEventListener('keydown', onKey)
  restartSlides()
})
onUnmounted(() => {
  document.removeEventListener('keydown', onKey)
  clearInterval(slideTimer)
})
</script>

<template>
  <Teleport to="body">
  <div class="mg-mask" @click.self="emit('close')">
    <section class="mg-panel" role="dialog" aria-modal="true" aria-label="我的游戏">

      <!-- ============ 背景层 ============ -->
      <div class="mg-bg">
        <div class="mg-moon"></div>
        <div class="mg-mist"></div>
        <div class="mg-aurora"></div>
        <span v-for="(s, i) in stars" :key="'s' + i" class="mg-star"
              :style="{ width: s.w, height: s.h, left: s.left, top: s.top, animationDelay: s.delay }"></span>

        <!-- 飘带 -->
        <svg class="mg-ribbon" viewBox="0 0 1080 700" fill="none" aria-hidden="true">
          <defs>
            <linearGradient id="mgRibG" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0" stop-color="#FFC2DC" stop-opacity="0" />
              <stop offset=".45" stop-color="#FFB0D2" stop-opacity=".55" />
              <stop offset="1" stop-color="#A87BE0" stop-opacity="0" />
            </linearGradient>
          </defs>
          <path d="M-60 120 C 220 40, 380 260, 640 190 C 860 130, 980 250, 1140 170"
                stroke="url(#mgRibG)" stroke-width="34" stroke-linecap="round" opacity=".35" />
          <path d="M-40 520 C 240 610, 420 430, 700 520 C 900 585, 1010 470, 1130 540"
                stroke="url(#mgRibG)" stroke-width="22" stroke-linecap="round" opacity=".25" />
        </svg>

        <svg class="mg-branch left" viewBox="0 0 420 320" fill="none" aria-hidden="true">
          <g stroke="#6B3F60" stroke-width="4" stroke-linecap="round" opacity=".85">
            <path d="M-10 20 C 80 60, 150 120, 210 250" /><path d="M80 55 C 130 45, 170 60, 205 95" />
            <path d="M140 118 C 175 130, 205 155, 225 190" /><path d="M20 10 C 45 45, 55 80, 60 120" />
          </g>
          <g opacity=".92">
            <ellipse v-for="b in blossomL.list" :key="b.key" :cx="b.cx" :cy="b.cy" rx="6" ry="9"
                     :transform="'rotate(' + b.rot + ' ' + b.cx + ' ' + b.cy + ')'"
                     :fill="b.fill" opacity=".9" />
            <circle v-for="c in blossomL.cores" :key="c.key" :cx="c.cx" :cy="c.cy" r="3.5" fill="#FFF0C9" />
          </g>
        </svg>

        <svg class="mg-branch right" viewBox="0 0 440 320" fill="none" aria-hidden="true">
          <g stroke="#6B3F60" stroke-width="4" stroke-linecap="round" opacity=".85">
            <path d="M450 10 C 370 55, 300 120, 245 260" /><path d="M370 50 C 320 40, 280 55, 245 92" />
            <path d="M310 120 C 275 132, 250 158, 228 195" /><path d="M420 5 C 400 45, 392 82, 388 122" />
          </g>
          <g opacity=".92">
            <ellipse v-for="b in blossomR.list" :key="b.key" :cx="b.cx" :cy="b.cy" rx="6" ry="9"
                     :transform="'rotate(' + b.rot + ' ' + b.cx + ' ' + b.cy + ')'"
                     :fill="b.fill" opacity=".9" />
            <circle v-for="c in blossomR.cores" :key="c.key" :cx="c.cx" :cy="c.cy" r="3.5" fill="#FFF0C9" />
          </g>
        </svg>

        <svg class="mg-silhouette" viewBox="0 0 300 460" aria-hidden="true">
          <defs><linearGradient id="mgSilG" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stop-color="#FFE9F6" /><stop offset="1" stop-color="#C9A0E0" />
          </linearGradient></defs>
          <g fill="url(#mgSilG)">
            <path d="M150 62 C 122 62,112 92,114 118 C 100 128,92 152,96 176 C 88 190,80 215,78 240 C 60 270,52 340,48 430 L252 430 C 246 340,236 270,220 240 C 218 215,210 190,202 176 C 206 152,198 128,186 118 C 188 92,178 62,150 62Z" />
            <path d="M112 118 C 80 140,62 220,72 300 C 78 250,92 205,112 176Z" />
            <path d="M188 118 C 220 140,238 220,228 300 C 222 250,208 205,188 176Z" />
            <ellipse cx="150" cy="58" rx="30" ry="34" />
            <path d="M120 50 C 112 12,188 12,180 50 C 186 30,170 20,150 20 C 130 20,114 30,120 50Z" />
          </g>
        </svg>

        <svg class="mg-lantern" viewBox="0 0 120 220" aria-hidden="true">
          <defs><linearGradient id="mgLanG" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stop-color="#FF8A7A" /><stop offset="1" stop-color="#C8394F" />
          </linearGradient></defs>
          <path d="M60 0 L60 26" stroke="#E8C98A" stroke-width="3" />
          <rect x="44" y="24" width="32" height="9" rx="3" fill="#E8C98A" />
          <ellipse cx="60" cy="86" rx="44" ry="56" fill="url(#mgLanG)" stroke="#F6D7A0" stroke-width="2" />
          <g stroke="#F8D9A8" stroke-width="1.6" opacity=".7">
            <path d="M60 30 L60 142" /><path d="M32 46 Q60 60 88 46" /><path d="M20 86 Q60 100 100 86" /><path d="M32 126 Q60 112 88 126" />
          </g>
          <rect x="44" y="138" width="32" height="9" rx="3" fill="#E8C98A" />
          <g stroke="#E8B06A" stroke-width="2"><path d="M50 147 L48 196" /><path d="M60 147 L60 202" /><path d="M70 147 L72 196" /></g>
          <circle cx="60" cy="86" r="18" fill="#FFE2B0" opacity=".35" />
        </svg>
      </div>

      <!-- 飘落花瓣 + 浮尘 -->
      <div class="mg-petals" aria-hidden="true">
        <span v-for="(p, i) in petals" :key="'p' + i" class="mg-petal"
              :style="{ left: p.left, '--drift': p.drift, '--spin': p.spin, '--scale': p.scale,
                        animationDuration: p.dur, animationDelay: p.delay }"></span>
        <span v-for="(m, i) in motes" :key="'m' + i" class="mg-mote"
              :style="{ left: m.left, bottom: m.bottom, width: m.size, height: m.size,
                        animationDuration: m.dur, animationDelay: m.delay }"></span>
      </div>

      <!-- 顶部工具条 -->
      <div class="mg-topbar">
        <span class="mg-brand">
          <svg viewBox="0 0 24 24" class="mg-brand-ico" aria-hidden="true">
            <path d="M12 3v3" stroke="#FFE9B8" stroke-width="1.4" stroke-linecap="round" />
            <path d="M9 7h6l1.4 6.2a4.4 4.4 0 0 1-8.8 0Z" fill="#E0576F" stroke="#FFE0B8" stroke-width="1" />
            <path d="M12 14v5" stroke="#E9C173" stroke-width="1.2" />
            <path d="M10 21h4" stroke="#E9C173" stroke-width="1.2" stroke-linecap="round" />
          </svg>
          我的游戏 · 花间小筑
        </span>
        <span class="mg-live">八款全开</span>
        <button class="mg-topclose" type="button" @click="emit('close')" aria-label="关闭">✕</button>
      </div>

      <!-- ============ 主体 ============ -->
      <div class="mg-scroll">
        <main class="mg-page">
          <header class="mg-hero">
            <h1>今夕，想寻些什么？</h1>
            <p>一花一叶皆有意 · 且随落樱入闲梦</p>
          </header>

          <section class="mg-banner">
            <button class="mg-arrow prev" type="button" @click="stepSlide(-1)" aria-label="上一条">‹</button>
            <span class="mg-b-tag">{{ SLIDES[slideIdx].tag }}</span>
            <h2 class="mg-b-title">{{ SLIDES[slideIdx].title }}</h2>
            <p class="mg-b-desc">{{ SLIDES[slideIdx].desc }}</p>
            <div class="mg-dots">
              <span v-for="(s, i) in SLIDES" :key="'d' + i" class="mg-dot"
                    :class="{ on: slideIdx === i }" @click="pickSlide(i)"></span>
            </div>
            <button class="mg-arrow next" type="button" @click="stepSlide(1)" aria-label="下一条">›</button>
          </section>

          <div class="mg-searchbar">
            <span class="mg-glass">⌕</span>
            <input v-model="query" type="text" placeholder="寻一处风景，寻一段闲情……"
                   autocomplete="off" aria-label="搜索小游戏" @keydown.enter="doSearch">
            <button class="mg-btn-main" type="button" @click="doSearch">寻一寻</button>
          </div>

          <nav class="mg-tabs">
            <button v-for="c in CATS" :key="c" type="button" class="mg-tab"
                    :class="{ on: cat === c }" @click="pickCat(c)">{{ c }}</button>
          </nav>

          <section class="mg-recommend">
            <div class="mg-rec-head">
              <span class="mg-rec-badge">今日推荐</span>
              <span class="mg-rec-sub">每日甄选 · 点开即玩</span>
            </div>
            <div class="mg-grid">
              <article v-for="g in filtered" :key="g.id" class="mg-card" v-tilt="{ strength: 8 }"
                       :style="{ '--mark': g.color }" @click="openDetail(g)">
                <span class="mg-index">{{ indexOf(g.id) }}</span>
                <span class="mg-watermark" aria-hidden="true">{{ g.mark }}</span>
                <span class="mg-play-hint">即刻可玩</span>
                <div class="mg-mark">{{ g.mark }}</div>
                <h3>{{ g.name }}</h3>
                <p class="mg-desc">{{ g.desc }}</p>
                <div class="mg-tags">
                  <span v-for="t in g.tags" :key="t" class="mg-tag">{{ t }}</span>
                </div>
                <span class="mg-card-glow"></span>
              </article>
            </div>
            <p v-if="!filtered.length" class="mg-empty">没寻着这款小游戏，换个词再试试～</p>
          </section>

          <footer class="mg-footer">
            花开花落自有时，且偷半日闲
            <small>花间小游戏 · 八款齐开 · 全部文案为简体中文 / UTF-8</small>
          </footer>
        </main>
      </div>

      <!-- ============ 详情弹层 ============ -->
      <div v-if="view === 'detail' && current" class="mg-overlay" @click.self="back">
        <div class="mg-modal">
          <button class="mg-modal-close" type="button" @click="back" aria-label="关闭">×</button>
          <div class="mg-m-head">
            <div class="mg-m-mark" :style="{ background: current.color }">{{ current.mark }}</div>
            <div>
              <h2>{{ current.name }}</h2>
              <div class="mg-m-status">{{ current.cat }} · {{ current.tags.join(' / ') }} · 已上线</div>
            </div>
          </div>
          <p class="mg-m-desc">{{ current.desc }}</p>
          <p class="mg-m-label">玩法小记</p>
          <p class="mg-m-rule">{{ current.rule }}</p>
          <div class="mg-m-actions">
            <button type="button" class="mg-btn primary" @click="startPlay">开始试玩</button>
            <button type="button" class="mg-btn ghost" @click="back">再看看别的</button>
          </div>
        </div>
      </div>

      <!-- ============ 游戏舞台 ============ -->
      <div v-if="view === 'play' && playComp" class="mg-overlay play" @click.self="backDetail">
        <div class="mg-arena">
          <button class="mg-modal-close arena-close" type="button" @click="backDetail" aria-label="关闭">×</button>
          <component :is="playComp" :game="current" @back="backDetail" />
        </div>
      </div>

    </section>
  </div>
  </Teleport>
</template>

<style scoped>
/* ---------- 浮层容器 ---------- */
.mg-mask{
  position:fixed; inset:0; z-index:60;
  background:rgba(13,4,32,.68); backdrop-filter:blur(7px);
  display:flex; align-items:center; justify-content:center; padding:16px;
}
.mg-panel{
  --serif:"Noto Serif SC","Source Han Serif SC","Songti SC","STSong","SimSun","Microsoft YaHei",serif;
  --sans:"PingFang SC","Microsoft YaHei","Hiragino Sans GB","Heiti SC",sans-serif;
  --ink:#3B2B45; --pink:#F2719F; --pink-deep:#D94F83; --gold:#FFD98A;
  position:relative; display:flex; flex-direction:column; overflow:hidden;
  width:min(1080px,94vw); height:min(96vh,1080px);
  border-radius:24px; border:1px solid rgba(255,225,245,.35);
  box-shadow:0 30px 90px rgba(10,3,26,.7);
  color:#fff; font-family:var(--sans);
  background:#241339;
  animation:mg-in .3s cubic-bezier(.22,1,.36,1);
}
@keyframes mg-in{ from{ opacity:0; transform:scale(.96) translateY(12px) } to{ opacity:1; transform:none } }

/* ---------- 背景层 ---------- */
.mg-bg{ position:absolute; inset:0; z-index:0; overflow:hidden;
  background:
    radial-gradient(900px 620px at 72% 8%, rgba(255,196,224,.30), transparent 70%),
    radial-gradient(700px 520px at 12% 30%, rgba(170,120,255,.28), transparent 72%),
    radial-gradient(900px 700px at 50% 100%, rgba(120,60,150,.35), transparent 70%),
    linear-gradient(160deg,#2B1745 0%,#43265F 38%,#2A1740 68%,#1A0F2C 100%);
}
.mg-bg::after{ content:""; position:absolute; inset:0;
  background:linear-gradient(180deg,rgba(20,10,35,.10) 0%,rgba(20,10,35,0) 30%,rgba(20,10,35,.28) 100%); }
.mg-moon{ position:absolute; top:52px; left:54%; width:250px; height:250px; border-radius:50%;
  background:radial-gradient(circle at 40% 38%,rgba(255,246,232,.60),rgba(255,224,240,.16) 55%,transparent 72%);
  filter:blur(3px); }
.mg-aurora{
  position:absolute; left:-20%; right:-20%; top:-30%; height:80%;
  background:conic-gradient(from 200deg at 60% 40%,transparent 0deg,rgba(180,120,255,.18) 70deg,transparent 130deg,rgba(255,150,200,.16) 210deg,transparent 280deg);
  filter:blur(40px); animation:mg-drift 26s ease-in-out infinite;
}
@keyframes mg-drift{ 0%,100%{ transform:translate3d(0,0,0) rotate(0deg) } 50%{ transform:translate3d(24px,10px,0) rotate(6deg) } }
.mg-star{ position:absolute; border-radius:50%; background:#fff; opacity:.6;
  animation:mg-twinkle 3.6s ease-in-out infinite; }
@keyframes mg-twinkle{ 0%,100%{ opacity:.18; transform:scale(.7) } 50%{ opacity:.85; transform:scale(1.15) } }
.mg-mist{ position:absolute; left:-10%; right:-10%; bottom:-6%; height:42%;
  background:radial-gradient(ellipse at 50% 100%,rgba(255,190,225,.20),transparent 65%);
  filter:blur(26px); }
.mg-ribbon{ position:absolute; inset:0; width:100%; height:100%; pointer-events:none; opacity:.9; }
.mg-branch{ position:absolute; pointer-events:none; }
.mg-branch.left{ top:-30px; left:-60px; width:420px; opacity:.9; }
.mg-branch.right{ top:-40px; right:-70px; width:440px; opacity:.85; }
.mg-silhouette{ position:absolute; top:16%; left:50%; width:420px; opacity:.17;
  filter:blur(1px); pointer-events:none; }
.mg-lantern{ position:absolute; right:3.5%; top:38%; width:120px; transform-origin:50% 0;
  animation:mg-sway 5.5s ease-in-out infinite;
  filter:drop-shadow(0 0 22px rgba(255,120,120,.5)); }
@keyframes mg-sway{ 0%,100%{ transform:rotate(-3deg) } 50%{ transform:rotate(3deg) } }

/* ---------- 飘落花瓣 / 浮尘 ---------- */
.mg-petals{ position:absolute; inset:0; z-index:1; pointer-events:none; overflow:hidden; }
.mg-petal{ position:absolute; top:-6vh; width:11px; height:15px; border-radius:80% 0 80% 0;
  background:linear-gradient(140deg,#FFE1EE,#FF9EC4 70%,#F477AA);
  box-shadow:0 0 6px rgba(255,170,210,.5);
  animation:mg-fall linear infinite; }
@keyframes mg-fall{
  0%{ transform:translate3d(0,-8vh,0) rotate(0deg) scale(var(--scale)); opacity:0; }
  8%{ opacity:.95; }
  100%{ transform:translate3d(var(--drift),112vh,0) rotate(var(--spin)) scale(var(--scale)); opacity:0; }
}
.mg-mote{
  position:absolute; border-radius:50%; background:rgba(255,235,190,.85);
  box-shadow:0 0 8px rgba(255,220,150,.8);
  animation:mg-mote-rise linear infinite; opacity:0;
}
@keyframes mg-mote-rise{
  0%{ transform:translate3d(0,0,0) scale(.6); opacity:0 }
  25%{ opacity:.75 }
  100%{ transform:translate3d(18px,-320px,0) scale(1.1); opacity:0 }
}

/* ---------- 顶部工具条 ---------- */
.mg-topbar{ position:relative; z-index:3; flex:none; display:flex; align-items:center;
  gap:10px; padding:12px 20px 0; }
.mg-brand{ font-family:var(--serif); font-size:12px; letter-spacing:2px;
  color:#FFF3FA; padding:5px 14px 5px 9px; border-radius:20px;
  background:rgba(40,18,66,.42); border:1px solid rgba(255,225,245,.28);
  text-shadow:0 2px 10px rgba(30,10,50,.6);
  display:inline-flex; align-items:center; gap:7px; }
.mg-brand-ico{ width:17px; height:17px; }
.mg-live{
  font-size:10px; letter-spacing:2px; color:#5A3A18; padding:4px 11px; border-radius:20px;
  background:linear-gradient(120deg,#FFEFC6,#E9C173);
  box-shadow:0 4px 12px rgba(200,150,60,.3), inset 0 1px 0 rgba(255,255,255,.6);
}
.mg-spacer{ flex:1; }
.mg-topclose{ margin-left:auto; width:32px; height:32px; border-radius:50%; cursor:pointer;
  border:1px solid rgba(255,225,245,.55); background:rgba(255,255,255,.14);
  color:#fff; font-size:15px; line-height:1; transition:.2s; }
.mg-topclose:hover{ background:rgba(255,255,255,.38); }

/* ---------- 滚动内容 ---------- */
.mg-scroll{ position:relative; z-index:2; flex:1; overflow-y:auto; overflow-x:hidden;
  container-type:inline-size; }
.mg-scroll::-webkit-scrollbar{ width:7px; }
.mg-scroll::-webkit-scrollbar-thumb{ background:rgba(255,213,235,.35); border-radius:7px; }
.mg-page{ max-width:1080px; margin:0 auto; padding:14px 24px 28px; }

.mg-hero{ text-align:center; margin-bottom:14px; }
.mg-hero h1{ font-family:var(--serif); font-size:34px; font-weight:600; letter-spacing:4px;
  margin:0 0 10px; color:#FFF6EF;
  text-shadow:0 2px 18px rgba(255,150,200,.65),0 0 40px rgba(180,120,255,.5); }
.mg-hero p{ margin:0; font-family:var(--serif); font-size:14px; letter-spacing:3px;
  color:rgba(255,232,244,.82); }

/* 轮播 */
.mg-banner{ position:relative; width:min(600px,100%); margin:0 auto 14px; padding:16px 58px 16px;
  border-radius:20px; text-align:center;
  background:linear-gradient(115deg,rgba(180,120,220,.42),rgba(240,150,200,.34) 55%,rgba(150,110,220,.42));
  border:1px solid rgba(255,225,245,.45);
  box-shadow:0 12px 40px rgba(40,15,70,.35),inset 0 1px 0 rgba(255,255,255,.35);
  backdrop-filter:blur(14px); overflow:hidden; }
.mg-banner::before{ content:""; position:absolute; inset:0;
  background:linear-gradient(90deg,transparent,rgba(255,255,255,.12),transparent); pointer-events:none; }
.mg-b-tag{ display:inline-block; font-size:11px; letter-spacing:2px; padding:3px 12px;
  border-radius:20px; background:rgba(255,255,255,.28); border:1px solid rgba(255,255,255,.45);
  margin-bottom:10px; }
.mg-b-title{ font-family:var(--serif); font-size:25px; letter-spacing:2px; color:#FFF7D9;
  margin:0 0 8px; text-shadow:0 2px 12px rgba(120,50,110,.5); }
.mg-b-desc{ font-size:13px; color:rgba(255,244,250,.9); margin:0; letter-spacing:1px; }
.mg-arrow{ position:absolute; top:50%; transform:translateY(-50%); width:32px; height:32px;
  border-radius:50%; border:1px solid rgba(255,255,255,.55); background:rgba(255,255,255,.16);
  color:#fff; font-size:16px; cursor:pointer; display:flex; align-items:center;
  justify-content:center; transition:.2s; }
.mg-arrow:hover{ background:rgba(255,255,255,.4); }
.mg-arrow.prev{ left:14px; } .mg-arrow.next{ right:14px; }
.mg-dots{ display:flex; gap:7px; justify-content:center; margin-top:14px; }
.mg-dot{ width:7px; height:7px; border-radius:20px; background:rgba(255,255,255,.4);
  cursor:pointer; transition:.25s; }
.mg-dot.on{ width:22px; background:#FFF2C9; }

/* 搜索 */
.mg-searchbar{ width:min(560px,100%); margin:0 auto 12px; display:flex; align-items:center; gap:10px;
  background:rgba(255,250,252,.90); border:1px solid rgba(255,255,255,.8); border-radius:40px;
  padding:7px 8px 7px 18px; box-shadow:0 8px 30px rgba(35,12,60,.3); }
.mg-glass{ font-size:17px; color:#A06A9A; }
.mg-searchbar input{ flex:1; min-width:0; border:0; outline:0; background:transparent;
  font-size:14px; color:#4A3355; font-family:var(--sans); }
.mg-searchbar input::placeholder{ color:#A892AD; }
.mg-btn-main{ border:0; cursor:pointer; color:#fff; font-size:14px; letter-spacing:3px;
  padding:10px 22px; border-radius:30px; background:linear-gradient(120deg,#F878A7,#E4548D);
  box-shadow:0 4px 14px rgba(226,80,140,.45); transition:.2s; font-family:var(--sans); }
.mg-btn-main:hover{ filter:brightness(1.08); transform:translateY(-1px); }

/* 分类 */
.mg-tabs{ display:flex; justify-content:center; flex-wrap:wrap; gap:6px; margin-bottom:16px; }
.mg-tab{ border:0; background:transparent; color:rgba(255,235,248,.85); font-size:13px;
  letter-spacing:1px; padding:8px 18px; border-radius:24px; cursor:pointer;
  transition:.2s; font-family:var(--sans); }
.mg-tab:hover{ background:rgba(255,255,255,.14); }
.mg-tab.on{ background:linear-gradient(120deg,#FFE6A6,#FFC96F); color:#6B4360;
  font-weight:700; box-shadow:0 4px 14px rgba(255,190,100,.35); }

/* 推荐区 */
.mg-rec-head{ text-align:center; margin-bottom:12px; }
.mg-rec-badge{ display:inline-block; font-family:var(--serif); font-size:14px; letter-spacing:4px;
  color:#6C4560; padding:6px 22px; border-radius:20px;
  background:linear-gradient(120deg,#FFE9AD,#FFCF78);
  box-shadow:0 5px 16px rgba(255,190,100,.3); }
.mg-rec-sub{ display:block; margin-top:9px; font-size:12px; letter-spacing:2px;
  color:rgba(255,232,246,.75); }
.mg-grid{ display:grid; grid-template-columns:repeat(4,1fr); gap:14px; }

/* 卡片：3D 倾斜由 v-tilt 提供，这里只管质感 */
.mg-card{ position:relative; border-radius:18px; padding:18px 14px 14px; text-align:center;
  cursor:pointer; min-height:178px; overflow:hidden;
  background:linear-gradient(170deg,rgba(255,253,252,.97),rgba(248,238,248,.93));
  border:1px solid rgba(255,255,255,.9);
  box-shadow:0 10px 26px rgba(32,12,55,.28);
  transition:box-shadow .25s ease; }
.mg-card::before{ content:""; position:absolute; top:0; left:0; right:0; height:4px;
  background:var(--mark,#E4548D); opacity:.9; }
.mg-card::after{
  content:""; position:absolute; inset:0; border-radius:18px; pointer-events:none;
  box-shadow:inset 0 0 0 1px rgba(233,193,115,.18);
}
.mg-card:hover{ box-shadow:0 20px 42px rgba(32,12,55,.46); }
.mg-index{
  position:absolute; left:12px; top:11px; font-family:var(--serif); font-size:11px;
  letter-spacing:1px; color:rgba(120,90,130,.32);
}
.mg-watermark{
  position:absolute; right:-4px; bottom:-30px; line-height:1; pointer-events:none;
  font-family:var(--serif); font-size:104px; font-weight:700;
  color:rgba(120,70,130,.055); transition:.3s;
}
.mg-card:hover .mg-watermark{ color:rgba(120,70,130,.085); }
.mg-mark{ width:48px; height:48px; margin:4px auto 12px; border-radius:15px;
  display:flex; align-items:center; justify-content:center; font-family:var(--serif);
  font-size:24px; color:#fff; background:var(--mark,#E4548D);
  box-shadow:0 8px 18px rgba(90,50,120,.28), inset 0 1px 0 rgba(255,255,255,.45);
  position:relative; transition:transform .25s cubic-bezier(.22,1,.36,1); }
.mg-mark::after{
  content:""; position:absolute; inset:3px; border-radius:12px;
  border:1px solid rgba(255,255,255,.35);
}
.mg-card:hover .mg-mark{ transform:translateY(-2px) scale(1.06); }
.mg-card h3{ font-family:var(--serif); font-size:19px; letter-spacing:2px;
  color:var(--ink); margin:0 0 8px; }
.mg-desc{ font-size:12px; line-height:1.7; color:#8A7A90; margin:0 0 14px; min-height:40px; }
.mg-tags{ display:flex; gap:6px; justify-content:center; flex-wrap:wrap; }
.mg-tag{ font-size:11px; padding:4px 11px; border-radius:20px; color:#75509C; background:#F0E6FA; }
.mg-play-hint{ position:absolute; right:10px; top:10px; font-size:10px;
  color:#C790B0; letter-spacing:1px; }
.mg-card-glow{
  position:absolute; left:50%; bottom:-40%; width:120%; height:70%; transform:translateX(-50%);
  background:radial-gradient(ellipse at 50% 100%, rgba(255,150,200,.22), transparent 70%);
  opacity:0; transition:opacity .3s; pointer-events:none;
}
.mg-card:hover .mg-card-glow{ opacity:1; }
.mg-empty{ text-align:center; padding:50px 0; color:rgba(255,235,248,.85);
  font-family:var(--serif); letter-spacing:2px; }
.mg-footer{ text-align:center; margin-top:22px; font-family:var(--serif); font-size:13px;
  letter-spacing:3px; color:rgba(255,228,244,.65); }
.mg-footer small{ display:block; margin-top:8px; font-size:11px; letter-spacing:1px;
  opacity:.7; font-family:var(--sans); }

/* ---------- 弹层 ---------- */
.mg-overlay{ position:absolute; inset:0; z-index:20;
  background:rgba(24,10,40,.62); backdrop-filter:blur(6px);
  display:flex; align-items:center; justify-content:center; padding:20px;
  animation:mg-fade .2s ease; }
.mg-overlay.play{
  background:radial-gradient(120% 90% at 50% 42%, rgba(22,9,38,.90), rgba(6,2,14,.97));
  backdrop-filter:blur(16px) saturate(1.05);
}
@keyframes mg-fade{ from{ opacity:0 } to{ opacity:1 } }
.mg-modal{ position:relative; width:min(520px,94%); max-height:calc(100% - 8px);
  overflow:auto; border-radius:22px; color:var(--ink);
  background:linear-gradient(165deg,#FFFAFD,#F8EEF8); padding:30px 30px 28px;
  box-shadow:0 24px 70px rgba(20,5,40,.55); border:1px solid rgba(255,255,255,.9); }
.mg-modal-close{ position:absolute; right:16px; top:14px; width:30px; height:30px;
  border-radius:50%; border:0; cursor:pointer; background:#F1E4F1; color:#8A6A92;
  font-size:16px; line-height:30px; text-align:center; z-index:5; }
.mg-modal-close:hover{ background:#E6D2E8; }
.mg-m-head{ display:flex; align-items:center; gap:14px; margin-bottom:14px; }
.mg-m-mark{ width:52px; height:52px; border-radius:16px; flex:none;
  display:flex; align-items:center; justify-content:center;
  font-family:var(--serif); font-size:26px; color:#fff;
  box-shadow:0 8px 20px rgba(120,60,130,.28), inset 0 1px 0 rgba(255,255,255,.4); }
.mg-m-head h2{ font-family:var(--serif); font-size:23px; letter-spacing:2px; margin:0 0 5px; }
.mg-m-status{ font-size:11px; color:#A07BB0; letter-spacing:1px; }
.mg-m-desc{ font-size:14px; line-height:1.9; color:#6D5A75; margin:0 0 14px; }
.mg-m-label{ font-family:var(--serif); font-size:13px; letter-spacing:2px;
  color:#8A5F8E; margin:16px 0 8px; }
.mg-m-rule{ font-size:13px; line-height:1.9; color:#7A687F; background:#F6EDF8;
  border-radius:12px; padding:12px 14px; margin:0; }
.mg-m-actions{ display:flex; gap:10px; margin-top:22px; flex-wrap:wrap; }
.mg-btn{ border:0; cursor:pointer; border-radius:26px; padding:11px 22px; font-size:14px;
  letter-spacing:2px; transition:.2s; font-family:var(--sans); }
.mg-btn.primary{ background:linear-gradient(120deg,#F878A7,#E4548D); color:#fff;
  box-shadow:0 5px 16px rgba(226,80,140,.35); }
.mg-btn.primary:hover{ filter:brightness(1.08); }
.mg-btn.ghost{ background:#EFE2F1; color:#7A5A86; }
.mg-btn.ghost:hover{ background:#E5D2E9; }

/* ---------- 游戏舞台外框 ---------- */
.mg-arena{
  position:relative; width:min(1020px,97%); max-height:calc(100% - 4px);
  display:flex; flex-direction:column; overflow:auto;
  border-radius:24px;
  background:
    radial-gradient(120% 70% at 50% 0%, rgba(255,180,220,.10), transparent 60%),
    linear-gradient(168deg,#2E1B46,#1C1030 58%,#150B23);
  border:1px solid rgba(233,193,115,.38);
  box-shadow:0 34px 90px rgba(4,1,12,.8), inset 0 1px 0 rgba(255,255,255,.12);
  animation:mg-in .28s cubic-bezier(.22,1,.36,1);
}
.mg-arena::-webkit-scrollbar{ width:8px; }
.mg-arena::-webkit-scrollbar-thumb{ background:rgba(255,213,235,.28); border-radius:8px; }
.arena-close{ background:rgba(255,255,255,.12); color:#FFE9F4; }
.arena-close:hover{ background:rgba(255,255,255,.3); }

/* ---------- 响应式 ---------- */
@container (max-width:860px){
  .mg-grid{ grid-template-columns:repeat(2,1fr); }
  .mg-hero h1{ font-size:29px; }
  .mg-silhouette{ display:none; }
  .mg-lantern{ width:80px; top:30%; }
}
@media (max-width:860px){
  .mg-grid{ grid-template-columns:repeat(2,1fr); }
  .mg-hero h1{ font-size:29px; }
  .mg-silhouette{ display:none; }
  .mg-lantern{ width:80px; top:30%; }
}
@media (max-width:480px){
  .mg-grid{ grid-template-columns:repeat(2,1fr); gap:11px; }
  .mg-card{ padding:16px 12px; min-height:174px; }
  .mg-card h3{ font-size:17px; }
  .mg-banner{ padding:18px 44px; }
  .mg-b-title{ font-size:20px; }
  .mg-modal{ padding:24px 18px; }
  .mg-page{ padding:14px 14px 34px; }
  .mg-overlay{ padding:10px; }
  .mg-arena{ width:100%; }
}
</style>
