<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const emit = defineEmits(['close'])

/* ============ 小游戏清单（简体中文 · UTF-8） ============ */
const GAMES = [
  { id:'xiaoxiao', name:'落樱消消乐', mark:'消', cat:'消除', tags:['三消','闯关'],
    desc:'拾起三瓣樱花，消去一院春愁。', color:'linear-gradient(135deg,#FF9EC4,#E4548D)', playable:true,
    rule:'点击相邻两格交换花签，三枚同色连成一线即可消去。20 步之内尽力拿高分，连锁消除还有额外加成。' },
  { id:'huanshang', name:'云裳换装记', mark:'裳', cat:'换装', tags:['搭配','收集'],
    desc:'罗裙、发髻、披帛，妆成一场花朝。', color:'linear-gradient(135deg,#C9A0F0,#8A5FD0)', playable:true,
    rule:'为画中人挑选裙色、发型、披帛与发饰，自由搭配出你心里的古风模样，还能一键随机换一套。' },
  { id:'zhuangxia', name:'胭脂妆匣', mark:'妆', cat:'换装', tags:['妆容','休闲'],
    desc:'点一抹朱唇，描两弯远山黛。', color:'linear-gradient(135deg,#FFB3A7,#E8708F)', playable:false,
    rule:'调胭脂、点花钿、描眉形，为不同场合搭配宜喜宜嗔的妆容。妆匣即将开启，敬请期待。' },
  { id:'feihualing', name:'飞花令', mark:'诗', cat:'休闲', tags:['诗词','接龙'],
    desc:'以诗会友，一句一令，看谁先折枝。', color:'linear-gradient(135deg,#8FD6C0,#4FA98E)', playable:false,
    rule:'以「花」「月」「春」为令，轮流接出含令字的诗句，接不上者罚酒一盅。诗社正在筹备中。' },
  { id:'xunfang', name:'宫墙寻芳', mark:'寻', cat:'解谜', tags:['寻物','解谜'],
    desc:'雕栏画栋间，寻回遗落的花笺。', color:'linear-gradient(135deg,#FFC98A,#E39A4F)', playable:false,
    rule:'在层层宫苑图景里找出隐藏的信物，拼合线索，解开一段被岁月藏起的故事。' },
  { id:'chalia', name:'茶寮物语', mark:'茶', cat:'经营', tags:['经营','合成'],
    desc:'煮一盏清茶，待四方来客。', color:'linear-gradient(135deg,#A8D8A0,#5FA86A)', playable:false,
    rule:'合成茶叶、添置器物、招呼来客，把一间山间小茶寮慢慢经营成远近闻名的雅集之地。' },
  { id:'xiange', name:'弦歌行', mark:'弦', cat:'休闲', tags:['音律','节奏'],
    desc:'按宫商角徵羽，抚一曲春江月。', color:'linear-gradient(135deg,#9FC6FF,#6A8EDE)', playable:false,
    rule:'跟随节拍轻点琴弦，古琴、琵琶、笛箫轮番登场，一曲终了看能得几分知音。' },
  { id:'zhifang', name:'锦绣织坊', mark:'锦', cat:'经营', tags:['拼图','手作'],
    desc:'织就云锦，拼出十里红妆。', color:'linear-gradient(135deg,#F2B0D8,#C06AA8)', playable:false,
    rule:'把散落的纹样碎片拼回完整锦缎，解锁团花、缠枝、云纹等传统纹样图鉴。' }
]
const CATS = ['全部','消除','换装','解谜','经营','休闲']

const cat = ref('全部')
const query = ref('')
const view = ref('list')          // list | detail | match | dress
const current = ref(null)
const noted = ref(false)

const filtered = computed(() => GAMES.filter(g => {
  const okCat = cat.value === '全部' || g.cat === cat.value
  const text = (g.name + g.desc + g.tags.join('') + g.cat).toLowerCase()
  return okCat && (!query.value || text.includes(query.value.trim().toLowerCase()))
}))

/* ============ 轮播 ============ */
const SLIDES = [
  { tag:'花朝限定', title:'云裳换装记 · 春日新装已上架', desc:'罗裙十二色，发簪三十六式，今日妆成谁家模样' },
  { tag:'每日福利', title:'签到领一瓣桃花 · 攒花换限定时装', desc:'连签七日，可得「月白罗裙」与珍珠步摇' },
  { tag:'闯关挑战', title:'落樱消消乐 · 第 30 关已开启', desc:'消去满庭落樱，通关解锁「海棠春睡」主题' }
]
const slideIdx = ref(0)
let slideTimer = null
function showSlide(i) { slideIdx.value = (i + SLIDES.length) % SLIDES.length }
function stepSlide(d) { showSlide(slideIdx.value + d); restartSlides() }
function pickSlide(i) { showSlide(i); restartSlides() }
function restartSlides() {
  clearInterval(slideTimer)
  slideTimer = setInterval(() => showSlide(slideIdx.value + 1), 5200)
}

/* ============ 装饰：星点 / 花瓣 / 樱花 ============ */
const stars = Array.from({ length: 64 }, () => {
  const s = Math.random() * 2.2 + 1
  return {
    w: s.toFixed(1) + 'px', h: s.toFixed(1) + 'px',
    left: (Math.random() * 100).toFixed(1) + '%',
    top: (Math.random() * 78).toFixed(1) + '%',
    delay: (Math.random() * 4).toFixed(1) + 's'
  }
})
const petals = Array.from({ length: 26 }, () => ({
  left: (Math.random() * 100).toFixed(1) + '%',
  drift: (Math.random() * 260 - 130).toFixed(0) + 'px',
  spin: (Math.random() * 720 + 360).toFixed(0) + 'deg',
  dur: (Math.random() * 9 + 9).toFixed(1) + 's',
  delay: (-Math.random() * 18).toFixed(1) + 's',
  scale: (Math.random() * .7 + .6).toFixed(2)
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
  [[40,22],[95,58],[150,92],[198,132],[60,112],[172,168],[214,220],[118,150],[30,80]], '#FFB9D7')
const blossomR = makeBlossoms(
  [[400,18],[350,52],[294,92],[248,134],[386,110],[276,172],[236,220],[330,152],[420,80]], '#FFC4DD')

/* ============ 弹层流程 ============ */
function openDetail(g) { current.value = g; noted.value = false; view.value = 'detail' }
function back() { view.value = 'list'; current.value = null }
function doSearch() { query.value = query.value.trim() }
function pickCat(c) { cat.value = c }
function markNoted() { noted.value = true }
function startPlay() {
  const g = current.value
  if (!g || !g.playable) return
  noted.value = false
  if (g.id === 'xiaoxiao') { initMatch(); view.value = 'match' }
  else { view.value = 'dress' }
}
function onKey(e) {
  if (e.key !== 'Escape') return
  if (view.value !== 'list') back()
  else emit('close')
}
onMounted(() => {
  document.addEventListener('keydown', onKey)
  restartSlides()
})
onUnmounted(() => {
  document.removeEventListener('keydown', onKey)
  clearInterval(slideTimer)
  clearTimeout(toastTimer)
})

/* ============ 小游戏一：落樱消消乐 ============ */
const TYPES = [
  { ch:'樱', c:'#F06EA0' }, { ch:'棠', c:'#A97ADE' }, { ch:'叶', c:'#69B98A' },
  { ch:'灯', c:'#EF9D55' }, { ch:'月', c:'#6FA8DE' }
]
const SIZE = 6
const board = ref([])
const sel = ref(null)
const busy = ref(false)
const moves = ref(20)
const score = ref(0)
const clearing = ref(new Set())
const toastMsg = ref('')
let toastTimer = null
const sleep = ms => new Promise(r => setTimeout(r, ms))

function commit() { board.value = board.value.map(row => row.slice()) }
function flash(msg) {
  toastMsg.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toastMsg.value = ''), 1600)
}
function newType() { return TYPES[Math.floor(Math.random() * TYPES.length)] }

function findMatches() {
  const b = board.value
  const m = new Set()
  for (let r = 0; r < SIZE; r++) {
    let run = 1
    for (let c = 1; c <= SIZE; c++) {
      if (c < SIZE && b[r][c] && b[r][c-1] && b[r][c].ch === b[r][c-1].ch) run++
      else { if (run >= 3) for (let k = c - run; k < c; k++) m.add(r + ',' + k); run = 1 }
    }
  }
  for (let c = 0; c < SIZE; c++) {
    let run = 1
    for (let r = 1; r <= SIZE; r++) {
      if (r < SIZE && b[r][c] && b[r-1][c] && b[r][c].ch === b[r-1][c].ch) run++
      else { if (run >= 3) for (let k = r - run; k < r; k++) m.add(k + ',' + c); run = 1 }
    }
  }
  return m
}
function buildBoard() {
  do {
    const b = []
    for (let r = 0; r < SIZE; r++) {
      const row = []
      for (let c = 0; c < SIZE; c++) {
        let t, guard = 0
        do {
          t = newType(); guard++
        } while (guard < 40 && ((c >= 2 && row[c-1] === t && row[c-2] === t) ||
                                (r >= 2 && b[r-1][c] === t && b[r-2][c] === t)))
        row.push(t)
      }
      b.push(row)
    }
    board.value = b
  } while (findMatches().size > 0)
}
function initMatch() {
  sel.value = null; busy.value = false; moves.value = 20; score.value = 0
  clearing.value = new Set(); toastMsg.value = ''
  buildBoard()
}
async function resolveMatches() {
  let combo = 0
  while (true) {
    const m = findMatches()
    if (!m.size) break
    combo++
    score.value += m.size * 10 * combo
    m.forEach(k => { const p = k.split(','); board.value[+p[0]][+p[1]] = null })
    clearing.value = new Set(m)
    commit()
    await sleep(210)
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
    await sleep(170)
  }
}
async function trySwap(a, b2) {
  if (busy.value) return
  busy.value = true
  const b = board.value
  const tmp = b[a.r][a.c]
  b[a.r][a.c] = b[b2.r][b2.c]
  b[b2.r][b2.c] = tmp
  sel.value = null
  commit()
  if (findMatches().size === 0) {
    await sleep(160)
    const t2 = b[a.r][a.c]
    b[a.r][a.c] = b[b2.r][b2.c]
    b[b2.r][b2.c] = t2
    commit()
    flash('这两枚换不出花样，再试一对～')
    busy.value = false
    return
  }
  moves.value--
  await resolveMatches()
  busy.value = false
  commit()
  if (moves.value <= 0) flash('步数用尽 · 本局得 ' + score.value + ' 分，点「再来一局」吧')
}
function onCell(r, c) {
  if (busy.value || moves.value <= 0) return
  const s = sel.value
  if (!s) { sel.value = { r, c }; return }
  if (s.r === r && s.c === c) { sel.value = null; return }
  if (Math.abs(s.r - r) + Math.abs(s.c - c) !== 1) { sel.value = { r, c }; return }
  trySwap(s, { r, c })
}

/* ============ 小游戏二：云裳换装记 ============ */
/* 立绘坐标系 320 × 440，所有可变部件都以该坐标系绘制 */

/* 裙色：c 主色 / c2 暗部 / hi 亮部光晕 / edge 描金 */
const DRESS = [
  { n:'桃夭粉', c:'#F291B2', c2:'#D2658C', hi:'#FFE0EC', edge:'#F9D9A4' },
  { n:'月白银', c:'#E8E3F3', c2:'#BEB4D6', hi:'#FFFFFF', edge:'#E4CE96' },
  { n:'天青蓝', c:'#9CC6E8', c2:'#6C9AC8', hi:'#E4F3FF', edge:'#F7E0AE' },
  { n:'榴花红', c:'#E2706A', c2:'#B94A4E', hi:'#FFD9D2', edge:'#FFDFA4' },
  { n:'竹青绿', c:'#A3CFAC', c2:'#71A482', hi:'#E2F6E6', edge:'#F1E0AC' }
]

/* 发型：back 后发 / top 顶发与鬓 / bun 发髻 / strands 发丝 / ornament 随发型的小饰物 */
const HAIR = [
  {
    n:'垂鬟髻',
    /* 后发：从肩后披落，不遮挡脸 */
    back:'M160 82 C126 80,110 106,108 140 C104 186,102 226,106 258 L112 274 C138 284,182 284,208 274 L214 258 C218 226,216 186,212 140 C210 106,194 80,160 82Z',
    /* 顶发：中分发际线，前额露出一片，两侧鬓角贴颊 */
    crown:'M160 68 C132 68,114 90,112 124 C112 132,113 140,115 147 C116 132,119 118,125 109 C133 100,146 96,160 96 C174 96,187 100,195 109 C201 118,204 132,205 147 C207 140,208 132,208 124 C206 90,188 68,160 68Z',
    /* 垂在胸前的两缕长发 */
    locks:'M126 126 C120 160,118 206,121 252 C122 264,126 272,132 276 C136 268,136 250,134 226 C131 194,131 158,134 128Z ' +
          'M194 126 C200 160,202 206,199 252 C198 264,194 272,188 276 C184 268,184 250,186 226 C189 194,189 158,186 128Z',
    bun:'M160 30 C139 30,126 46,127 64 C128 82,142 96,160 96 C178 96,192 82,193 64 C194 46,181 30,160 30Z',
    ornament:'bun-wreath'
  },
  {
    n:'双环髻',
    back:'M160 84 C128 82,112 108,110 142 C106 188,104 228,108 258 L114 274 C138 282,182 282,206 274 L212 258 C216 228,214 188,210 142 C208 108,192 82,160 84Z',
    crown:'M160 68 C132 68,114 90,112 124 C112 132,113 140,115 147 C116 132,119 118,125 109 C133 100,146 96,160 96 C174 96,187 100,195 109 C201 118,204 132,205 147 C207 140,208 132,208 124 C206 90,188 68,160 68Z',
    locks:'M116 130 C110 168,108 216,111 260 C112 272,116 280,121 284 C125 276,125 256,123 232 C120 200,121 162,124 132Z ' +
          'M204 130 C210 168,212 216,209 260 C208 272,204 280,199 284 C195 276,195 256,197 232 C200 200,199 162,196 132Z',
    bun:'M114 38 C96 38,84 51,85 67 C86 84,98 95,114 95 C130 95,142 84,143 67 C144 51,132 38,114 38Z ' +
        'M206 38 C188 38,176 51,177 67 C178 84,190 95,206 95 C222 95,234 84,235 67 C236 51,224 38,206 38Z',
    ornament:'double-ring'
  },
  {
    n:'及腰长发',
    back:'M160 84 C124 82,108 110,106 146 C102 200,100 254,106 296 C108 306,112 311,118 313 L128 305 C124 270,124 210,128 168 C130 140,140 118,160 116 C180 118,190 140,192 168 C196 210,196 270,192 305 L202 313 C208 311,212 306,214 296 C220 254,218 200,214 146 C212 110,196 82,160 84Z',
    crown:'M160 68 C132 68,114 90,112 124 C112 132,113 140,115 147 C116 132,119 118,125 109 C133 100,146 96,160 96 C174 96,187 100,195 109 C201 118,204 132,205 147 C207 140,208 132,208 124 C206 90,188 68,160 68Z',
    locks:'M128 132 C120 178,117 240,121 292 C122 304,126 310,131 313 C135 302,135 282,132 250 C129 200,130 160,134 132Z ' +
          'M192 132 C200 178,203 240,199 292 C198 304,194 310,189 313 C185 302,185 282,188 250 C191 200,190 160,186 132Z',
    bun:'',
    ornament:'long-hair'
  }
]

const SHAWL = [
  { n:'不披', c:'transparent', grad:false },
  { n:'月白', c:'#F4EAF9', grad:true },
  { n:'霞粉', c:'#FFC2DC', grad:true },
  { n:'鹅黄', c:'#FFE3AC', grad:true }
]

/* 发饰：type 决定绘制哪一组 */
const PIN = [
  { n:'珍珠钗', type:'pearl' },
  { n:'金步摇', type:'buyao' },
  { n:'花钿', type:'huadian' }
]

const di = ref(0), hi = ref(0), si = ref(1), pi = ref(0)
const curDress = computed(() => DRESS[di.value])
const curHair = computed(() => HAIR[hi.value])
const curShawl = computed(() => SHAWL[si.value])
const curPin = computed(() => PIN[pi.value])
const lookName = computed(() => curDress.value.n + ' · ' + curHair.value.n + ' · ' +
  (curShawl.value.n === '不披' ? '素衣' : curShawl.value.n + '披帛') + ' · ' + curPin.value.n)
function randomLook() {
  di.value = Math.floor(Math.random() * DRESS.length)
  hi.value = Math.floor(Math.random() * HAIR.length)
  si.value = Math.floor(Math.random() * SHAWL.length)
  pi.value = Math.floor(Math.random() * PIN.length)
}
</script>

<template>
  <Teleport to="body">
  <div class="mg-mask" @click.self="emit('close')">
    <section class="mg-panel" role="dialog" aria-modal="true" aria-label="我的游戏">

      <!-- ============ 背景层：月夜 / 星点 / 樱花枝 / 剪影 / 灯笼 ============ -->
      <div class="mg-bg">
        <div class="mg-moon"></div>
        <div class="mg-mist"></div>
        <span v-for="(s, i) in stars" :key="'s' + i" class="mg-star"
              :style="{ width: s.w, height: s.h, left: s.left, top: s.top, animationDelay: s.delay }"></span>

        <svg class="mg-branch left" viewBox="0 0 420 320" fill="none" aria-hidden="true">
          <g stroke="#6B3F60" stroke-width="4" stroke-linecap="round" opacity=".85">
            <path d="M-10 20 C 80 60, 150 120, 210 250"/><path d="M80 55 C 130 45, 170 60, 205 95"/>
            <path d="M140 118 C 175 130, 205 155, 225 190"/><path d="M20 10 C 45 45, 55 80, 60 120"/>
          </g>
          <g opacity=".92">
            <ellipse v-for="b in blossomL.list" :key="b.key" :cx="b.cx" :cy="b.cy" rx="6" ry="9"
                     :transform="'rotate(' + b.rot + ' ' + b.cx + ' ' + b.cy + ')'"
                     :fill="b.fill" opacity=".9"/>
            <circle v-for="c in blossomL.cores" :key="c.key" :cx="c.cx" :cy="c.cy" r="3.5" fill="#FFF0C9"/>
          </g>
        </svg>

        <svg class="mg-branch right" viewBox="0 0 440 320" fill="none" aria-hidden="true">
          <g stroke="#6B3F60" stroke-width="4" stroke-linecap="round" opacity=".85">
            <path d="M450 10 C 370 55, 300 120, 245 260"/><path d="M370 50 C 320 40, 280 55, 245 92"/>
            <path d="M310 120 C 275 132, 250 158, 228 195"/><path d="M420 5 C 400 45, 392 82, 388 122"/>
          </g>
          <g opacity=".92">
            <ellipse v-for="b in blossomR.list" :key="b.key" :cx="b.cx" :cy="b.cy" rx="6" ry="9"
                     :transform="'rotate(' + b.rot + ' ' + b.cx + ' ' + b.cy + ')'"
                     :fill="b.fill" opacity=".9"/>
            <circle v-for="c in blossomR.cores" :key="c.key" :cx="c.cx" :cy="c.cy" r="3.5" fill="#FFF0C9"/>
          </g>
        </svg>

        <svg class="mg-silhouette" viewBox="0 0 300 460" aria-hidden="true">
          <defs><linearGradient id="mgSilG" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stop-color="#FFE9F6"/><stop offset="1" stop-color="#C9A0E0"/>
          </linearGradient></defs>
          <g fill="url(#mgSilG)">
            <path d="M150 62 C 122 62,112 92,114 118 C 100 128,92 152,96 176 C 88 190,80 215,78 240 C 60 270,52 340,48 430 L252 430 C 246 340,236 270,220 240 C 218 215,210 190,202 176 C 206 152,198 128,186 118 C 188 92,178 62,150 62Z"/>
            <path d="M112 118 C 80 140,62 220,72 300 C 78 250,92 205,112 176Z"/>
            <path d="M188 118 C 220 140,238 220,228 300 C 222 250,208 205,188 176Z"/>
            <ellipse cx="150" cy="58" rx="30" ry="34"/>
            <path d="M120 50 C 112 12,188 12,180 50 C 186 30,170 20,150 20 C 130 20,114 30,120 50Z"/>
          </g>
        </svg>

        <svg class="mg-lantern" viewBox="0 0 120 220" aria-hidden="true">
          <defs><linearGradient id="mgLanG" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stop-color="#FF8A7A"/><stop offset="1" stop-color="#C8394F"/>
          </linearGradient></defs>
          <path d="M60 0 L60 26" stroke="#E8C98A" stroke-width="3"/>
          <rect x="44" y="24" width="32" height="9" rx="3" fill="#E8C98A"/>
          <ellipse cx="60" cy="86" rx="44" ry="56" fill="url(#mgLanG)" stroke="#F6D7A0" stroke-width="2"/>
          <g stroke="#F8D9A8" stroke-width="1.6" opacity=".7">
            <path d="M60 30 L60 142"/><path d="M32 46 Q60 60 88 46"/><path d="M20 86 Q60 100 100 86"/><path d="M32 126 Q60 112 88 126"/>
          </g>
          <rect x="44" y="138" width="32" height="9" rx="3" fill="#E8C98A"/>
          <g stroke="#E8B06A" stroke-width="2"><path d="M50 147 L48 196"/><path d="M60 147 L60 202"/><path d="M70 147 L72 196"/></g>
          <circle cx="60" cy="86" r="18" fill="#FFE2B0" opacity=".35"/>
        </svg>
      </div>

      <!-- 飘落花瓣 -->
      <div class="mg-petals" aria-hidden="true">
        <span v-for="(p, i) in petals" :key="'p' + i" class="mg-petal"
              :style="{ left: p.left, '--drift': p.drift, '--spin': p.spin, '--scale': p.scale,
                        animationDuration: p.dur, animationDelay: p.delay }"></span>
      </div>

      <!-- 顶部工具条（保留浮层身份 + 关闭） -->
      <div class="mg-topbar">
        <span class="mg-brand">🎐 我的游戏 · 花间小筑</span>
        <button class="mg-topclose" type="button" @click="emit('close')" aria-label="关闭">✕</button>
      </div>

      <!-- ============ 主体：复刻 index.html 的整页布局 ============ -->
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
              <article v-for="g in filtered" :key="g.id" class="mg-card"
                       :style="{ '--mark': g.color }" @click="openDetail(g)">
                <span class="mg-play-hint">{{ g.playable ? '可试玩' : '即将上线' }}</span>
                <div class="mg-mark">{{ g.mark }}</div>
                <h3>{{ g.name }}</h3>
                <p class="mg-desc">{{ g.desc }}</p>
                <div class="mg-tags">
                  <span v-for="t in g.tags" :key="t" class="mg-tag">{{ t }}</span>
                </div>
              </article>
            </div>
            <p v-if="!filtered.length" class="mg-empty">没寻着这款小游戏，换个词再试试～</p>
          </section>

          <footer class="mg-footer">
            花开花落自有时，且偷半日闲
            <small>花间小游戏 · 全部文案为简体中文 / UTF-8</small>
          </footer>
        </main>
      </div>

      <!-- ============ 弹层：详情 / 三消 / 换装（复刻 index.html 的白色 modal） ============ -->
      <div v-if="view !== 'list'" class="mg-overlay" @click.self="back">
        <div class="mg-modal" :class="{ wide: view === 'match' || view === 'dress' }">
          <button class="mg-modal-close" type="button" @click="back" aria-label="关闭">×</button>

          <!-- 详情 -->
          <template v-if="view === 'detail' && current">
            <div class="mg-m-head">
              <div class="mg-m-mark" :style="{ background: current.color }">{{ current.mark }}</div>
              <div>
                <h2>{{ current.name }}</h2>
                <div class="mg-m-status">{{ current.cat }} · {{ current.tags.join(' / ') }}</div>
              </div>
            </div>
            <p class="mg-m-desc">{{ current.desc }}</p>
            <p class="mg-m-label">玩法小记</p>
            <p class="mg-m-rule">{{ current.rule }}</p>
            <div class="mg-m-actions">
              <button v-if="current.playable" type="button" class="mg-btn primary" @click="startPlay">开始试玩</button>
              <button v-else type="button" class="mg-btn primary" :disabled="noted" @click="markNoted">
                {{ noted ? '已记下 · 静候佳音' : '记下啦，上线喊我' }}
              </button>
              <button type="button" class="mg-btn ghost" @click="back">再看看别的</button>
            </div>
          </template>

          <!-- 落樱消消乐 -->
          <template v-else-if="view === 'match'">
            <div class="mg-m-head">
              <div class="mg-m-mark" style="background:linear-gradient(135deg,#FF9EC4,#E4548D)">消</div>
              <div>
                <h2>落樱消消乐</h2>
                <div class="mg-m-status">试玩版 · 点击相邻花签交换</div>
              </div>
            </div>
            <div class="mg-game-bar">
              <span>得分 <b>{{ score }}</b></span>
              <span>剩余步数 <b>{{ moves }}</b></span>
              <button type="button" class="mg-btn ghost small" @click="initMatch">重新开始</button>
            </div>
            <div class="mg-board">
              <template v-for="(row, r) in board" :key="'r' + r">
                <div v-for="(t, c) in row" :key="r + '-' + c" class="mg-cell"
                     :class="{ sel: sel && sel.r === r && sel.c === c, clearing: clearing.has(r + ',' + c), blank: !t }"
                     :style="t ? { background: t.c, boxShadow: '0 3px 8px ' + t.c + '55' } : {}"
                     @click="onCell(r, c)">{{ t ? t.ch : '' }}</div>
              </template>
            </div>
            <div class="mg-toast">{{ toastMsg }}</div>
            <div class="mg-m-actions">
              <button type="button" class="mg-btn primary" @click="back">返回推荐</button>
            </div>
          </template>

          <!-- 云裳换装记 -->
          <template v-else-if="view === 'dress'">
            <div class="mg-m-head">
              <div class="mg-m-mark" style="background:linear-gradient(135deg,#C9A0F0,#8A5FD0)">裳</div>
              <div>
                <h2>云裳换装记</h2>
                <div class="mg-m-status">试玩版 · 自由搭配古风造型</div>
              </div>
            </div>
            <div class="mg-dress-wrap">
              <div class="mg-doll">
                <svg class="mg-doll-svg" viewBox="0 0 320 440" xmlns="http://www.w3.org/2000/svg"
                     role="img" aria-label="换装立绘">
                  <defs>
                    <linearGradient id="mgRobeG" x1="0.2" y1="0" x2="0.8" y2="1">
                      <stop offset="0" :stop-color="curDress.hi" />
                      <stop offset="0.42" :stop-color="curDress.c" />
                      <stop offset="1" :stop-color="curDress.c2" />
                    </linearGradient>
                    <linearGradient id="mgSleeveG" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0" :stop-color="curDress.c" />
                      <stop offset="1" :stop-color="curDress.c2" />
                    </linearGradient>
                    <linearGradient id="mgShawlG" x1="0" y1="0" x2="0.6" y2="1">
                      <stop offset="0" :stop-color="curShawl.c" stop-opacity="0.95" />
                      <stop offset="1" :stop-color="curShawl.c" stop-opacity="0.45" />
                    </linearGradient>
                    <linearGradient id="mgSkinG" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0" stop-color="#FFF1E7" />
                      <stop offset="1" stop-color="#F6D8C8" />
                    </linearGradient>
                    <linearGradient id="mgHairG" x1="0.1" y1="0" x2="0.9" y2="1">
                      <stop offset="0" stop-color="#4E3A5C" />
                      <stop offset="0.45" stop-color="#2C1F38" />
                      <stop offset="1" stop-color="#1A1122" />
                    </linearGradient>
                    <linearGradient id="mgGoldG" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0" stop-color="#FFF0C2" />
                      <stop offset="0.5" stop-color="#E9C173" />
                      <stop offset="1" stop-color="#C79A4C" />
                    </linearGradient>
                    <radialGradient id="mgHaloG" cx="0.5" cy="0.45" r="0.55">
                      <stop offset="0" stop-color="#FFFFFF" stop-opacity="0.95" />
                      <stop offset="0.62" stop-color="#FFEAF3" stop-opacity="0.55" />
                      <stop offset="1" stop-color="#FFDCEC" stop-opacity="0" />
                    </radialGradient>
                    <linearGradient id="mgFloorG" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0" stop-color="#EAD8EE" stop-opacity="0" />
                      <stop offset="1" stop-color="#DCC4E4" stop-opacity="0.85" />
                    </linearGradient>
                    <radialGradient id="mgBlushG" cx="0.5" cy="0.5" r="0.5">
                      <stop offset="0" stop-color="#FF9DB4" stop-opacity="0.55" />
                      <stop offset="1" stop-color="#FF9DB4" stop-opacity="0" />
                    </radialGradient>
                    <filter id="mgSoft" x="-30%" y="-30%" width="160%" height="160%">
                      <feGaussianBlur stdDeviation="6" />
                    </filter>
                  </defs>

                  <!-- 背景：月晕 / 光柱 / 地面 -->
                  <circle cx="160" cy="196" r="126" fill="url(#mgHaloG)" />
                  <circle cx="160" cy="196" r="104" fill="none" stroke="#EBCFE0" stroke-width="0.9" opacity="0.55" />
                  <ellipse cx="160" cy="196" rx="140" ry="120" fill="url(#mgHaloG)" opacity="0.5" filter="url(#mgSoft)" />
                  <path d="M0 402 H320 V440 H0 Z" fill="url(#mgFloorG)" />
                  <ellipse cx="160" cy="405" rx="86" ry="11" fill="#B79FC4" opacity="0.22" filter="url(#mgSoft)" />

                  <!-- 0. 颈饰：璎珞 -->
                  <!-- 1. 后发 -->
                  <path :d="curHair.back" fill="url(#mgHairG)" />
                  <path :d="curHair.back" fill="none" stroke="#9A7EAE" stroke-width="0.9" opacity="0.28" />

                  <!-- 2. 裙身（外袍） -->
                  <path d="M160 168 C150 168,142 172,136 178 C132 190,130 214,128 240
                           C124 274,114 330,104 386 C100 396,98 401,97 406
                           L223 406 C222 401,220 396,216 386 C206 330,196 274,192 240
                           C190 214,188 190,184 178 C178 172,170 168,160 168 Z"
                        fill="url(#mgRobeG)" />
                  <!-- 左半受光 -->
                  <path d="M160 168 C150 168,142 172,136 178 C132 190,130 214,128 240
                           C124 274,114 330,104 386 C100 396,98 401,97 406
                           L132 406 C134 344,144 268,152 200 C154 182,156 173,160 168 Z"
                        fill="#FFFFFF" opacity="0.16" />
                  <!-- 右半阴影 -->
                  <path d="M160 168 C170 168,178 172,184 178 C188 190,190 214,192 240
                           C196 274,206 330,216 386 C220 396,222 401,223 406
                           L196 406 C194 344,184 268,168 200 C166 182,164 173,160 168 Z"
                        fill="#000000" opacity="0.09" />
                  <!-- 百褶 -->
                  <g fill="none" stroke="#FFFFFF" stroke-width="1.1" opacity="0.20">
                    <path d="M141 268 C136 312,127 360,120 404" />
                    <path d="M152 272 C149 316,144 362,140 404" />
                    <path d="M179 272 C182 316,187 362,191 404" />
                    <path d="M190 268 C195 312,204 360,211 404" />
                  </g>
                  <g fill="none" stroke="#000000" stroke-width="1" opacity="0.07">
                    <path d="M128 240 C124 274,114 330,104 386" />
                    <path d="M192 240 C196 274,206 330,216 386" />
                  </g>

                  <!-- 裙摆描金边 + 云纹 -->
                  <path d="M97 406 C130 399,190 399,223 406" fill="none" :stroke="curDress.edge" stroke-width="5.5" opacity="0.95" />
                  <path d="M97 406 C130 399,190 399,223 406" fill="none" stroke="#FFFFFF" stroke-width="1.3" opacity="0.7" />
                  <g :stroke="curDress.edge" stroke-width="1.5" fill="none" opacity="0.7" stroke-linecap="round">
                    <path d="M118 384 q9 -6 18 0 q9 6 18 0" />
                    <path d="M166 384 q9 -6 18 0 q9 6 18 0" />
                    <path d="M140 356 q10 -7 20 0 q10 7 20 0" />
                    <path d="M132 330 q9 -6 18 0 q9 6 18 0" opacity="0.7" />
                  </g>

                  <!-- 3. 内衬抹胸 + 交领 -->
                  <path d="M160 172 C150 174,142 180,139 188 L160 232 L181 188 C178 180,170 174,160 172 Z"
                        fill="#FFFDFE" opacity="0.94" />
                  <path d="M139 188 L160 232 L157 236 L136 191 Z" :fill="curDress.edge" opacity="0.9" />
                  <path d="M181 188 L160 232 L163 236 L184 191 Z" :fill="curDress.edge" opacity="0.9" />
                  <path d="M160 232 L146 258 L160 250 L174 258 Z" fill="#FFFFFF" opacity="0.75" />

                  <!-- 腰带 + 玉环 -->
                  <path d="M126 242 C144 254,176 254,194 242 L190 262 C174 273,146 273,130 262 Z"
                        :fill="curDress.c2" />
                  <path d="M126 242 C144 254,176 254,194 242" fill="none" :stroke="curDress.edge"
                        stroke-width="2.4" opacity="0.95" />
                  <path d="M130 262 C146 273,174 273,190 262" fill="none" stroke="#000000" stroke-width="1"
                        opacity="0.12" />
                  <circle cx="160" cy="266" r="7" fill="none" stroke="url(#mgGoldG)" stroke-width="2.2" />
                  <circle cx="160" cy="266" r="2.8" fill="#F7A8C4" opacity="0.9" />

                  <!-- 颈间璎珞（在衣领之上） -->
                  <g :stroke="curDress.edge" fill="none" stroke-width="1.4" opacity="0.9">
                    <path d="M147 174 C154 186,166 186,173 174" />
                    <path d="M151 177 C156 185,164 185,169 177" opacity="0.6" />
                  </g>
                  <circle cx="160" cy="184" r="2.6" :fill="curDress.c" stroke="url(#mgGoldG)" stroke-width="1" />

                  <!-- 4. 宽袖（大袖衫） -->
                  <path d="M137 186 C118 194,100 218,91 250 C86 272,84 292,87 305
                           C100 310,113 302,119 288 C127 268,133 236,142 210 Z"
                        fill="url(#mgSleeveG)" />
                  <path d="M183 186 C202 194,220 218,229 250 C234 272,236 292,233 305
                           C220 310,207 302,201 288 C193 268,187 236,178 210 Z"
                        fill="url(#mgSleeveG)" />
                  <!-- 袖口亮边（垂坠感） -->
                  <path d="M87 305 C100 310,113 302,119 288 L117 299 C112 309,100 315,89 310 Z"
                        fill="#FFFFFF" opacity="0.5" />
                  <path d="M233 305 C220 310,207 302,201 288 L203 299 C208 309,220 315,231 310 Z"
                        fill="#FFFFFF" opacity="0.36" />
                  <!-- 袖褶 -->
                  <path d="M141 212 C134 240,128 268,122 288" fill="none" stroke="#FFFFFF" stroke-width="1.3" opacity="0.32" />
                  <path d="M179 212 C186 240,192 268,198 288" fill="none" stroke="#FFFFFF" stroke-width="1.3" opacity="0.26" />
                  <path d="M133 206 C126 236,120 266,114 292" fill="none" stroke="#000000" stroke-width="1" opacity="0.06" />
                  <path d="M187 206 C194 236,200 266,206 292" fill="none" stroke="#000000" stroke-width="1" opacity="0.06" />
                  <!-- 袖口描金 -->
                  <g :stroke="curDress.edge" stroke-width="1.5" fill="none" opacity="0.62">
                    <path d="M92 294 C98 284,106 280,114 284" />
                    <path d="M228 294 C222 284,214 280,206 284" />
                  </g>

                  <!-- 5. 脖颈 -->
                  <path d="M149 150 L171 150 L171 172 C171 182,149 182,149 172 Z" fill="#F3D0BE" />
                  <path d="M149 168 C155 175,165 175,171 168 L171 172 C171 182,149 182,149 172 Z"
                        fill="#000000" opacity="0.10" />

                  <!-- 6. 脸 -->
                  <path d="M160 74 C137 74,122 94,122 121 C122 138,127 152,134 163
                           C141 173,150 181,160 181 C170 181,179 173,186 163
                           C193 152,198 138,198 121 C198 94,183 74,160 74 Z"
                        fill="url(#mgSkinG)" />
                  <!-- 下颌柔和阴影 -->
                  <path d="M129 150 C134 162,144 174,160 181 C176 174,186 162,191 150
                           C185 164,175 174,160 178 C145 174,135 164,129 150 Z"
                        fill="#E7B9A8" opacity="0.20" />
                  <!-- 额头发际处的一点阴影，让发际线更自然 -->
                  <path d="M146 95 C151 92,169 92,174 95 C168 88,152 88,146 95 Z"
                        fill="#E7B9A8" opacity="0.25" />
                  <ellipse cx="140" cy="144" rx="10" ry="6" fill="url(#mgBlushG)" opacity="0.8" />
                  <ellipse cx="180" cy="144" rx="10" ry="6" fill="url(#mgBlushG)" opacity="0.8" />

                  <!-- 眉眼 -->
                  <!-- 远山眉：细长上挑 -->
                  <path d="M134 118 C140 112,150 111,156 115" fill="none" stroke="#7A5C6C" stroke-width="1.5"
                        stroke-linecap="round" opacity="0.7" />
                  <path d="M164 115 C170 111,180 112,186 118" fill="none" stroke="#7A5C6C" stroke-width="1.5"
                        stroke-linecap="round" opacity="0.7" />
                  <!-- 凤眼：上眼睑一条长弧，下睑极轻 -->
                  <path d="M137 131 C143 126,152 126,156 130" fill="none" stroke="#3A2632" stroke-width="1.7"
                        stroke-linecap="round" />
                  <path d="M164 130 C168 126,177 126,183 131" fill="none" stroke="#3A2632" stroke-width="1.7"
                        stroke-linecap="round" />
                  <path d="M138 131 C143 127,151 127,155 130" fill="none" stroke="#8A5C66" stroke-width="1"
                        stroke-linecap="round" opacity="0.55" />
                  <path d="M165 130 C169 127,177 127,182 131" fill="none" stroke="#8A5C66" stroke-width="1"
                        stroke-linecap="round" opacity="0.55" />
                  <!-- 眼尾上挑的睫毛 -->
                  <path d="M157 129 l4 -2.4 M184 130 l-4 -2.4" stroke="#2E1D28" stroke-width="1.3"
                        stroke-linecap="round" opacity="0.9" />
                  <!-- 瞳 -->
                  <ellipse cx="147" cy="130" rx="2.6" ry="2.3" fill="#4A2E3C" />
                  <ellipse cx="173" cy="130" rx="2.6" ry="2.3" fill="#4A2E3C" />
                  <circle cx="148" cy="129.3" r="0.9" fill="#FFFFFF" opacity="0.95" />
                  <circle cx="174" cy="129.3" r="0.9" fill="#FFFFFF" opacity="0.95" />
                  <!-- 眼影 -->
                  <ellipse cx="146" cy="125" rx="7" ry="3" fill="#E88FA6" opacity="0.18" />
                  <ellipse cx="174" cy="125" rx="7" ry="3" fill="#E88FA6" opacity="0.18" />
                  <!-- 鼻影 -->
                  <path d="M158 134 C158.6 138,158.6 141,156 143" fill="none" stroke="#DFB0A0"
                        stroke-width="1.1" stroke-linecap="round" opacity="0.7" />

                  <!-- 唇 + 眉心花钿 -->
                  <path d="M154 154.5 C157 151.5,159 152.5,160 154 C161 152.5,163 151.5,166 154.5
                           C163 159.5,157 159.5,154 154.5 Z" fill="#E07C90" />
                  <path d="M157 154 C158.5 153.2,161.5 153.2,163 154" fill="none" stroke="#FFE7EC"
                        stroke-width="0.8" stroke-linecap="round" />
                  <path d="M155 154 C157 152,159 153,160 154" fill="none" stroke="#FFE3EA" stroke-width="0.8" />
                  <path d="M160 106 l2.4 4.6 -2.4 4.6 -2.4 -4.6 Z" fill="#E2687F" opacity="0.9" />
                  <circle cx="160" cy="111" r="1" fill="#FFF0C2" />

                  <!-- 7. 发髻（压在顶发之下） -->
                  <path v-if="curHair.bun" :d="curHair.bun" fill="url(#mgHairG)" />
                  <path v-if="curHair.bun" :d="curHair.bun" fill="none" stroke="#9A7EAE" stroke-width="1"
                        opacity="0.45" />
                  <ellipse v-if="curHair.bun" cx="151" cy="60" rx="10" ry="6.5" fill="#B79BC9"
                           opacity="0.30" transform="rotate(-18 151 60)" />
                  <path v-if="curHair.bun" d="M143 84 C150 90,170 90,177 84" fill="none"
                        stroke="#8F739F" stroke-width="1" opacity="0.4" />

                  <!-- 8. 顶发（中分发际线 + 鬓角贴颊） -->
                  <path :d="curHair.crown" fill="url(#mgHairG)" />
                  <!-- 额前碎发 -->
                  <path d="M146 95 C151 99,155 101,160 99 C165 101,169 99,174 95"
                        fill="none" stroke="#7A5E8C" stroke-width="1.2" opacity="0.55" stroke-linecap="round" />
                  <!-- 中缝高光 -->
                  <path d="M160 79 C160 86,160 90,160 96" fill="none" stroke="#A98BBE"
                        stroke-width="0.9" opacity="0.45" stroke-linecap="round" />
                  <!-- 鬓角内侧的高光 -->
                  <path d="M116 130 C114 144,115 156,118 168" fill="none" stroke="#A98BBE"
                        stroke-width="1.1" opacity="0.30" stroke-linecap="round" />
                  <path d="M204 130 C206 144,205 156,202 168" fill="none" stroke="#A98BBE"
                        stroke-width="1.1" opacity="0.26" stroke-linecap="round" />

                  <!-- 9. 胸前长发 -->
                  <path :d="curHair.locks" fill="url(#mgHairG)" />
                  <path :d="curHair.locks" fill="none" stroke="#A98BBE" stroke-width="0.9" opacity="0.30" />

                  <!-- 10. 披帛 -->
                  <g v-if="curShawl.grad">
                    <path d="M132 190 C104 208,88 250,86 300 C85 330,92 356,104 372"
                          fill="none" stroke="url(#mgShawlG)" stroke-width="17" stroke-linecap="round" />
                    <path d="M188 190 C216 208,232 250,234 300 C235 330,228 356,216 372"
                          fill="none" stroke="url(#mgShawlG)" stroke-width="17" stroke-linecap="round" />
                    <path d="M132 190 C104 208,88 250,86 300" fill="none" stroke="#FFFFFF"
                          stroke-width="1.6" opacity="0.5" stroke-linecap="round" />
                    <path d="M188 190 C216 208,232 250,234 300" fill="none" stroke="#FFFFFF"
                          stroke-width="1.6" opacity="0.4" stroke-linecap="round" />
                    <path d="M104 372 l-6 14 M216 372 l6 14" :stroke="curShawl.c" stroke-width="3" opacity="0.7"
                          stroke-linecap="round" />
                    <circle cx="132" cy="192" r="4.5" fill="url(#mgGoldG)" />
                    <circle cx="188" cy="192" r="4.5" fill="url(#mgGoldG)" />
                  </g>

                  <!-- 11. 发饰 -->
                  <g v-if="curPin.type === 'pearl'">
                    <path d="M130 96 C146 86,174 86,190 96" fill="none" stroke="url(#mgGoldG)"
                          stroke-width="2.2" stroke-linecap="round" />
                    <circle cx="160" cy="88" r="4.4" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1.2" />
                    <circle cx="143" cy="90" r="3.1" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                    <circle cx="177" cy="90" r="3.1" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                    <circle cx="128" cy="99" r="2.4" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                    <circle cx="192" cy="99" r="2.4" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                    <path d="M160 84 l1.6 3.2 -1.6 3.2 -1.6 -3.2 Z" fill="#F7C8D8" />
                  </g>
                  <g v-else-if="curPin.type === 'buyao'">
                    <path d="M186 96 L150 78" stroke="url(#mgGoldG)" stroke-width="2.6" stroke-linecap="round" />
                    <circle cx="150" cy="78" r="4.6" :fill="curDress.c" stroke="url(#mgGoldG)" stroke-width="1.6" />
                    <path d="M186 96 C190 112,186 126,180 136" fill="none" stroke="url(#mgGoldG)"
                          stroke-width="1.6" stroke-linecap="round" />
                    <circle cx="180" cy="138" r="3.6" :fill="curDress.c" />
                    <path d="M180 142 l3 8 M180 142 l-3 8" stroke="#E9C173" stroke-width="1.2" stroke-linecap="round" />
                    <path d="M138 100 C132 110,132 118,136 124" fill="none" stroke="url(#mgGoldG)"
                          stroke-width="1.2" stroke-linecap="round" opacity="0.85" />
                    <circle cx="136" cy="126" r="2.6" :fill="curDress.c" opacity="0.9" />
                  </g>
                  <g v-else>
                    <path d="M186 100 L146 82" stroke="url(#mgGoldG)" stroke-width="2.2" stroke-linecap="round" />
                    <g fill="#F4A0BC">
                      <circle cx="146" cy="82" r="3" />
                      <circle cx="152" cy="78" r="2.4" />
                      <circle cx="141" cy="77" r="2.4" />
                      <circle cx="147" cy="73" r="2.4" />
                    </g>
                    <circle cx="146" cy="78" r="1.5" fill="#FFF0C2" />
                  </g>

                  <!-- 发箍 / 髻饰 -->
                  <g v-if="curHair.ornament === 'bun-wreath'">
                    <path d="M130 62 C144 54,176 54,190 62" fill="none" stroke="url(#mgGoldG)"
                          stroke-width="1.8" stroke-linecap="round" opacity="0.9" />
                    <circle cx="160" cy="56" r="3" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                    <path d="M160 60 l1.2 2.4 -1.2 2.4 -1.2 -2.4 Z" fill="#F2A0BC" />
                  </g>
                  <g v-else-if="curHair.ornament === 'double-ring'">
                    <path d="M114 58 C109 65,109 72,114 78" fill="none" stroke="url(#mgGoldG)"
                          stroke-width="1.6" stroke-linecap="round" opacity="0.85" />
                    <path d="M206 58 C211 65,211 72,206 78" fill="none" stroke="url(#mgGoldG)"
                          stroke-width="1.6" stroke-linecap="round" opacity="0.85" />
                    <circle cx="114" cy="67" r="2.6" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                    <circle cx="206" cy="67" r="2.6" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                  </g>
                  <g v-else>
                    <path d="M122 132 C112 150,110 176,114 200" fill="none" :stroke="curDress.edge"
                          stroke-width="1.6" stroke-linecap="round" opacity="0.7" />
                    <path d="M198 132 C208 150,210 176,206 200" fill="none" :stroke="curDress.edge"
                          stroke-width="1.6" stroke-linecap="round" opacity="0.7" />
                    <circle cx="126" cy="164" r="2.4" :fill="curDress.c" opacity="0.9" />
                    <circle cx="194" cy="164" r="2.4" :fill="curDress.c" opacity="0.9" />
                  </g>

                  <!-- 12. 衣褶 / 光影收尾 -->
                  <path d="M160 280 C157 322,155 366,155 402" fill="none" stroke="#FFFFFF" stroke-width="1.6"
                        opacity="0.20" />
                  <path d="M112 330 C108 356,105 382,104 402" fill="none" stroke="#000000" stroke-width="1"
                        opacity="0.06" />
                  <path d="M208 330 C212 356,215 382,216 402" fill="none" stroke="#000000" stroke-width="1"
                        opacity="0.06" />
                </svg>
                <div class="mg-doll-tag">{{ curDress.n }} · {{ curHair.n }}</div>
              </div>
              <div class="mg-opts">
                <div class="mg-opt">
                  <p>裙色 <b>{{ curDress.n }}</b></p>
                  <div class="mg-chips">
                    <button v-for="(d, i) in DRESS" :key="'d' + i" type="button" class="mg-swatch"
                            :class="{ on: di === i }" :style="{ background: 'linear-gradient(140deg,' + d.hi + ',' + d.c + ' 55%,' + d.c2 + ')' }"
                            :title="d.n" :aria-label="d.n" @click="di = i" />
                  </div>
                </div>
                <div class="mg-opt">
                  <p>发型 <b>{{ curHair.n }}</b></p>
                  <div class="mg-chips">
                    <button v-for="(h, i) in HAIR" :key="'h' + i" type="button" class="mg-chip"
                            :class="{ on: hi === i }" @click="hi = i">{{ h.n }}</button>
                  </div>
                </div>
                <div class="mg-opt">
                  <p>披帛 <b>{{ curShawl.n }}</b></p>
                  <div class="mg-chips">
                    <button v-for="(s, i) in SHAWL" :key="'s' + i" type="button" class="mg-chip"
                            :class="{ on: si === i }" @click="si = i">{{ s.n }}</button>
                  </div>
                </div>
                <div class="mg-opt">
                  <p>发饰 <b>{{ curPin.n }}</b></p>
                  <div class="mg-chips">
                    <button v-for="(p, i) in PIN" :key="'p' + i" type="button" class="mg-chip"
                            :class="{ on: pi === i }" @click="pi = i">{{ p.n }}</button>
                  </div>
                </div>
              </div>
            </div>
            <p class="mg-look-name">{{ lookName }}</p>
            <div class="mg-m-actions">
              <button type="button" class="mg-btn primary" @click="randomLook">随机换一套</button>
              <button type="button" class="mg-btn ghost" @click="back">返回推荐</button>
            </div>
          </template>
        </div>
      </div>

    </section>
  </div>
  </Teleport>
</template>

<style scoped>
/* ---------- 浮层容器（居中大面板，取代原来的左侧窄抽屉） ---------- */
.mg-mask{
  position:fixed; inset:0; z-index:60;
  background:rgba(13,4,32,.66); backdrop-filter:blur(6px);
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
.mg-star{ position:absolute; border-radius:50%; background:#fff; opacity:.6;
  animation:mg-twinkle 3.6s ease-in-out infinite; }
@keyframes mg-twinkle{ 0%,100%{ opacity:.18; transform:scale(.7) } 50%{ opacity:.85; transform:scale(1.15) } }
.mg-mist{ position:absolute; left:-10%; right:-10%; bottom:-6%; height:42%;
  background:radial-gradient(ellipse at 50% 100%,rgba(255,190,225,.20),transparent 65%);
  filter:blur(26px); }
.mg-branch{ position:absolute; pointer-events:none; }
.mg-branch.left{ top:-30px; left:-60px; width:420px; opacity:.9; }
.mg-branch.right{ top:-40px; right:-70px; width:440px; opacity:.85; }
.mg-silhouette{ position:absolute; top:16%; left:50%; width:420px; opacity:.17;
  filter:blur(1px); pointer-events:none; }
.mg-lantern{ position:absolute; right:3.5%; top:38%; width:120px; transform-origin:50% 0;
  animation:mg-sway 5.5s ease-in-out infinite;
  filter:drop-shadow(0 0 22px rgba(255,120,120,.5)); }
@keyframes mg-sway{ 0%,100%{ transform:rotate(-3deg) } 50%{ transform:rotate(3deg) } }

/* ---------- 飘落花瓣 ---------- */
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

/* ---------- 顶部工具条 ---------- */
.mg-topbar{ position:relative; z-index:3; flex:none; display:flex; align-items:center;
  justify-content:space-between; padding:12px 20px 0; }
.mg-brand{ font-family:var(--serif); font-size:12px; letter-spacing:2px;
  color:#FFF3FA; padding:5px 14px; border-radius:20px;
  background:rgba(40,18,66,.42); border:1px solid rgba(255,225,245,.28);
  text-shadow:0 2px 10px rgba(30,10,50,.6); }
.mg-topclose{ width:32px; height:32px; border-radius:50%; cursor:pointer;
  border:1px solid rgba(255,225,245,.55); background:rgba(255,255,255,.14);
  color:#fff; font-size:15px; line-height:1; transition:.2s; }
.mg-topclose:hover{ background:rgba(255,255,255,.38); }

/* ---------- 滚动内容 ---------- */
.mg-scroll{ position:relative; z-index:2; flex:1; overflow-y:auto; overflow-x:hidden;
  container-type:inline-size; }
.mg-scroll::-webkit-scrollbar{ width:7px; }
.mg-scroll::-webkit-scrollbar-thumb{ background:rgba(255,213,235,.35); border-radius:7px; }
.mg-page{ max-width:1080px; margin:0 auto; padding:14px 24px 28px; }

/* 大标题 */
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

/* 推荐区 + 网格卡片 */
.mg-rec-head{ text-align:center; margin-bottom:12px; }
.mg-rec-badge{ display:inline-block; font-family:var(--serif); font-size:14px; letter-spacing:4px;
  color:#6C4560; padding:6px 22px; border-radius:20px;
  background:linear-gradient(120deg,#FFE9AD,#FFCF78);
  box-shadow:0 5px 16px rgba(255,190,100,.3); }
.mg-rec-sub{ display:block; margin-top:9px; font-size:12px; letter-spacing:2px;
  color:rgba(255,232,246,.75); }
.mg-grid{ display:grid; grid-template-columns:repeat(4,1fr); gap:14px; }
.mg-card{ position:relative; border-radius:18px; padding:16px 14px 14px; text-align:center;
  cursor:pointer; min-height:168px; overflow:hidden;
  background:linear-gradient(170deg,rgba(255,253,252,.96),rgba(250,241,249,.92));
  border:1px solid rgba(255,255,255,.9);
  box-shadow:0 10px 26px rgba(32,12,55,.28);
  transition:transform .25s ease,box-shadow .25s ease; }
.mg-card::before{ content:""; position:absolute; top:0; left:0; right:0; height:4px;
  background:var(--mark,#E4548D); opacity:.85; }
.mg-card:hover{ transform:translateY(-7px); box-shadow:0 18px 38px rgba(32,12,55,.42); }
.mg-mark{ width:46px; height:46px; margin:2px auto 12px; border-radius:14px;
  display:flex; align-items:center; justify-content:center; font-family:var(--serif);
  font-size:23px; color:#fff; background:var(--mark,#E4548D);
  box-shadow:0 6px 16px rgba(90,50,120,.25); }
.mg-card h3{ font-family:var(--serif); font-size:19px; letter-spacing:2px;
  color:var(--ink); margin:0 0 8px; }
.mg-desc{ font-size:12px; line-height:1.7; color:#8A7A90; margin:0 0 14px; min-height:40px; }
.mg-tags{ display:flex; gap:6px; justify-content:center; flex-wrap:wrap; }
.mg-tag{ font-size:11px; padding:4px 11px; border-radius:20px; color:#75509C; background:#F0E6FA; }
.mg-play-hint{ position:absolute; right:10px; top:10px; font-size:10px;
  color:#C790B0; letter-spacing:1px; }
.mg-empty{ text-align:center; padding:50px 0; color:rgba(255,235,248,.85);
  font-family:var(--serif); letter-spacing:2px; }
.mg-footer{ text-align:center; margin-top:22px; font-family:var(--serif); font-size:13px;
  letter-spacing:3px; color:rgba(255,228,244,.65); }
.mg-footer small{ display:block; margin-top:8px; font-size:11px; letter-spacing:1px;
  opacity:.7; font-family:var(--sans); }

/* ---------- 弹层（白色 modal） ---------- */
.mg-overlay{ position:absolute; inset:0; z-index:20;
  background:rgba(24,10,40,.62); backdrop-filter:blur(6px);
  display:flex; align-items:center; justify-content:center; padding:20px;
  animation:mg-fade .2s ease; }
@keyframes mg-fade{ from{ opacity:0 } to{ opacity:1 } }
.mg-modal{ position:relative; width:min(520px,94%); max-height:calc(100% - 8px);
  overflow:auto; border-radius:22px; color:var(--ink);
  background:linear-gradient(165deg,#FFFAFD,#F8EEF8); padding:30px 30px 28px;
  box-shadow:0 24px 70px rgba(20,5,40,.55); border:1px solid rgba(255,255,255,.9); }
.mg-modal.wide{ width:min(600px,94%); }
.mg-modal-close{ position:absolute; right:16px; top:14px; width:30px; height:30px;
  border-radius:50%; border:0; cursor:pointer; background:#F1E4F1; color:#8A6A92;
  font-size:16px; line-height:30px; text-align:center; }
.mg-modal-close:hover{ background:#E6D2E8; }
.mg-m-head{ display:flex; align-items:center; gap:14px; margin-bottom:14px; }
.mg-m-mark{ width:52px; height:52px; border-radius:16px; flex:none;
  display:flex; align-items:center; justify-content:center;
  font-family:var(--serif); font-size:26px; color:#fff; }
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
.mg-btn.primary:disabled{ opacity:.7; cursor:default; }
.mg-btn.ghost{ background:#EFE2F1; color:#7A5A86; }
.mg-btn.ghost:hover{ background:#E5D2E9; }
.mg-btn.small{ padding:7px 14px; font-size:12px; letter-spacing:1px; }

/* 消消乐 */
.mg-game-bar{ display:flex; justify-content:space-between; align-items:center;
  margin:6px 0 14px; font-size:13px; color:#7A5F85; }
.mg-game-bar b{ font-size:19px; color:var(--pink-deep); font-family:var(--serif); }
.mg-board{ display:grid; grid-template-columns:repeat(6,1fr); gap:6px;
  background:#F0E2F2; padding:8px; border-radius:14px; }
.mg-cell{ aspect-ratio:1; border-radius:11px; cursor:pointer;
  display:flex; align-items:center; justify-content:center;
  font-family:var(--serif); font-size:20px; color:#fff; font-weight:600;
  user-select:none; transition:transform .16s ease,opacity .16s ease,box-shadow .16s ease; }
.mg-cell:hover{ transform:scale(1.06); }
.mg-cell.sel{ box-shadow:0 0 0 3px #fff,0 0 0 5px var(--pink-deep);
  transform:scale(1.08); z-index:2; }
.mg-cell.clearing{ transform:scale(.1); opacity:0; }
.mg-cell.blank{ background:transparent !important; box-shadow:none !important; }
.mg-toast{ height:20px; text-align:center; font-size:12px; color:var(--pink-deep);
  margin-top:10px; letter-spacing:1px; }

/* 换装 —— 立绘画框 */
.mg-dress-wrap{ display:flex; gap:22px; align-items:stretch; flex-wrap:wrap; }
.mg-doll{
  position:relative; width:258px; flex:none; border-radius:20px; overflow:hidden;
  aspect-ratio:320 / 440; isolation:isolate;
  background:
    radial-gradient(120% 80% at 50% 0%, #FFFDFE 0%, #FDF1F7 42%, #F3E2EE 100%);
  border:1px solid #EEDAE8;
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.95),
    inset 0 -18px 34px rgba(190,150,205,.16),
    0 10px 26px rgba(120,70,130,.13);
}
/* 描金内框 */
.mg-doll::before{
  content:""; position:absolute; inset:7px; border-radius:14px; pointer-events:none; z-index:2;
  border:1px solid rgba(233,193,115,.55);
  box-shadow:inset 0 0 0 1px rgba(255,255,255,.6);
}
/* 四角花钿装饰 */
.mg-doll::after{
  content:""; position:absolute; inset:0; pointer-events:none; z-index:2; opacity:.7;
  background:
    radial-gradient(circle at 15px 15px, rgba(233,193,115,.85) 1.4px, transparent 2px),
    radial-gradient(circle at calc(100% - 15px) 15px, rgba(233,193,115,.85) 1.4px, transparent 2px),
    radial-gradient(circle at 15px calc(100% - 15px), rgba(233,193,115,.85) 1.4px, transparent 2px),
    radial-gradient(circle at calc(100% - 15px) calc(100% - 15px), rgba(233,193,115,.85) 1.4px, transparent 2px);
}
.mg-doll-svg{ width:100%; height:100%; display:block;
  filter:drop-shadow(0 10px 18px rgba(120,70,120,.16)); }
/* 立绘上的造型名签 */
.mg-doll-tag{
  position:absolute; left:0; right:0; bottom:0; z-index:3;
  padding:22px 12px 9px; text-align:center;
  font-family:var(--serif); font-size:11px; letter-spacing:2px; color:#8A5F8E;
  background:linear-gradient(180deg,rgba(255,250,253,0),rgba(255,250,253,.88) 55%);
}

/* 换装 —— 控件区 */
.mg-opts{ flex:1; min-width:200px; display:flex; flex-direction:column; justify-content:center; gap:13px; }
.mg-opt{ padding-bottom:11px; border-bottom:1px dashed rgba(200,160,200,.28); }
.mg-opt:last-child{ padding-bottom:0; border-bottom:0; }
.mg-opt p{ margin:0 0 8px; font-size:11px; letter-spacing:2px; color:#A085A8;
  display:flex; align-items:center; gap:7px; }
.mg-opt p b{ font-family:var(--serif); font-weight:600; font-size:12px;
  letter-spacing:1px; color:#7A4E78; }
.mg-chips{ display:flex; gap:8px; flex-wrap:wrap; align-items:center; }
.mg-chip{
  position:relative; border:1px solid #E9D8EC; background:#fff; color:#6E5578;
  border-radius:20px; padding:7px 13px; font-size:12px; cursor:pointer;
  transition:.2s cubic-bezier(.22,1,.36,1); font-family:var(--sans);
  box-shadow:0 1px 2px rgba(120,70,120,.06);
}
.mg-chip:hover{ border-color:#E9A6C2; color:#C2527E; transform:translateY(-1px); }
.mg-chip.on{
  background:linear-gradient(120deg,#FF8FB4,#E4548D); color:#fff; border-color:transparent;
  box-shadow:0 6px 14px rgba(226,80,140,.34), inset 0 1px 0 rgba(255,255,255,.35);
}
/* 色板：立体漆器感 */
.mg-swatch{
  width:30px; height:30px; border-radius:50%; border:2px solid #fff; cursor:pointer; padding:0;
  box-shadow:0 0 0 1px #E6D4E9, 0 3px 8px rgba(120,70,120,.16), inset 0 -4px 8px rgba(0,0,0,.14), inset 0 3px 6px rgba(255,255,255,.5);
  transition:.2s cubic-bezier(.22,1,.36,1);
}
.mg-swatch:hover{ transform:translateY(-2px) scale(1.06); }
.mg-swatch.on{
  box-shadow:0 0 0 2px #fff, 0 0 0 4px #E9C173, 0 6px 14px rgba(226,80,140,.3),
             inset 0 -4px 8px rgba(0,0,0,.14), inset 0 3px 6px rgba(255,255,255,.5);
  transform:scale(1.04);
}
.mg-look-name{
  font-family:var(--serif); font-size:13px; color:#7A5E82; letter-spacing:1px;
  margin:16px 0 0; padding:10px 14px; border-radius:12px;
  background:linear-gradient(120deg,#FBF1F8,#F6EAF4);
  border:1px solid rgba(233,193,115,.35);
  display:flex; align-items:center; gap:8px;
}
.mg-look-name::before{ content:"造型"; font-size:10px; letter-spacing:2px; color:#B08BAA;
  padding:2px 7px; border-radius:6px; background:rgba(255,255,255,.75);
  border:1px solid rgba(233,193,115,.5); flex:none; }

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
  .mg-card{ padding:16px 12px; min-height:170px; }
  .mg-card h3{ font-size:17px; }
  .mg-banner{ padding:18px 44px; }
  .mg-b-title{ font-size:20px; }
  .mg-modal{ padding:24px 18px; }
  .mg-doll{ margin:0 auto; }
  .mg-page{ padding:14px 14px 34px; }
}
</style>
