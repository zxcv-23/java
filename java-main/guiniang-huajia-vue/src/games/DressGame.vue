<script setup>
import { ref, computed, onUnmounted } from 'vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 云裳换装记 ============ */
const DRESS = [
  { n: '桃夭粉', c: '#F291B2', c2: '#D2658C', hi: '#FFE0EC', edge: '#F9D9A4' },
  { n: '月白银', c: '#E8E3F3', c2: '#BEB4D6', hi: '#FFFFFF', edge: '#E4CE96' },
  { n: '天青蓝', c: '#9CC6E8', c2: '#6C9AC8', hi: '#E4F3FF', edge: '#F7E0AE' },
  { n: '榴花红', c: '#E2706A', c2: '#B94A4E', hi: '#FFD9D2', edge: '#FFDFA4' },
  { n: '竹青绿', c: '#A3CFAC', c2: '#71A482', hi: '#E2F6E6', edge: '#F1E0AC' }
]

const HAIR = [
  {
    n: '垂鬟髻',
    back: 'M160 82 C126 80,110 106,108 140 C104 186,102 226,106 258 L112 274 C138 284,182 284,208 274 L214 258 C218 226,216 186,212 140 C210 106,194 80,160 82Z',
    crown: 'M160 68 C132 68,114 90,112 124 C112 132,113 140,115 147 C116 132,119 118,125 109 C133 100,146 96,160 96 C174 96,187 100,195 109 C201 118,204 132,205 147 C207 140,208 132,208 124 C206 90,188 68,160 68Z',
    locks: 'M126 126 C120 160,118 206,121 252 C122 264,126 272,132 276 C136 268,136 250,134 226 C131 194,131 158,134 128Z ' +
           'M194 126 C200 160,202 206,199 252 C198 264,194 272,188 276 C184 268,184 250,186 226 C189 194,189 158,186 128Z',
    bun: 'M160 30 C139 30,126 46,127 64 C128 82,142 96,160 96 C178 96,192 82,193 64 C194 46,181 30,160 30Z',
    ornament: 'bun-wreath'
  },
  {
    n: '双环髻',
    back: 'M160 84 C128 82,112 108,110 142 C106 188,104 228,108 258 L114 274 C138 282,182 282,206 274 L212 258 C216 228,214 188,210 142 C208 108,192 82,160 84Z',
    crown: 'M160 68 C132 68,114 90,112 124 C112 132,113 140,115 147 C116 132,119 118,125 109 C133 100,146 96,160 96 C174 96,187 100,195 109 C201 118,204 132,205 147 C207 140,208 132,208 124 C206 90,188 68,160 68Z',
    locks: 'M116 130 C110 168,108 216,111 260 C112 272,116 280,121 284 C125 276,125 256,123 232 C120 200,121 162,124 132Z ' +
           'M204 130 C210 168,212 216,209 260 C208 272,204 280,199 284 C195 276,195 256,197 232 C200 200,199 162,196 132Z',
    bun: 'M114 38 C96 38,84 51,85 67 C86 84,98 95,114 95 C130 95,142 84,143 67 C144 51,132 38,114 38Z ' +
         'M206 38 C188 38,176 51,177 67 C178 84,190 95,206 95 C222 95,234 84,235 67 C236 51,224 38,206 38Z',
    ornament: 'double-ring'
  },
  {
    n: '及腰长发',
    back: 'M160 84 C124 82,108 110,106 146 C102 200,100 254,106 296 C108 306,112 311,118 313 L128 305 C124 270,124 210,128 168 C130 140,140 118,160 116 C180 118,190 140,192 168 C196 210,196 270,192 305 L202 313 C208 311,212 306,214 296 C220 254,218 200,214 146 C212 110,196 82,160 84Z',
    crown: 'M160 68 C132 68,114 90,112 124 C112 132,113 140,115 147 C116 132,119 118,125 109 C133 100,146 96,160 96 C174 96,187 100,195 109 C201 118,204 132,205 147 C207 140,208 132,208 124 C206 90,188 68,160 68Z',
    locks: 'M128 132 C120 178,117 240,121 292 C122 304,126 310,131 313 C135 302,135 282,132 250 C129 200,130 160,134 132Z ' +
           'M192 132 C200 178,203 240,199 292 C198 304,194 310,189 313 C185 302,185 282,188 250 C191 200,190 160,186 132Z',
    bun: '',
    ornament: 'long-hair'
  }
]

const SHAWL = [
  { n: '不披', c: 'transparent', grad: false },
  { n: '月白', c: '#F4EAF9', grad: true },
  { n: '霞粉', c: '#FFC2DC', grad: true },
  { n: '鹅黄', c: '#FFE3AC', grad: true }
]

const PIN = [
  { n: '珍珠钗', type: 'pearl' },
  { n: '金步摇', type: 'buyao' },
  { n: '花钿', type: 'huadian' }
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

/* 鼠标视差：让立绘有一点儿"活"过来的错觉 */
const rx = ref(0), ry = ref(0)
function onMove(e) {
  const el = e.currentTarget
  const rect = el.getBoundingClientRect()
  const px = (e.clientX - rect.left) / rect.width - .5
  const py = (e.clientY - rect.top) / rect.height - .5
  ry.value = +(px * 12).toFixed(2)
  rx.value = +(-py * 8).toFixed(2)
}
function onLeave() { rx.value = 0; ry.value = 0 }

/* 衣柜：本地留存造型 */
const KEY = 'guiniang_looks'
function readLooks() {
  try { const a = JSON.parse(localStorage.getItem(KEY) || '[]'); return Array.isArray(a) ? a : [] }
  catch { return [] }
}
const looks = ref(readLooks())
const tip = ref('')
let tipTimer = null
function say(msg) {
  tip.value = msg
  clearTimeout(tipTimer)
  tipTimer = setTimeout(() => (tip.value = ''), 1600)
}
function keepLook() {
  const item = { name: lookName.value, d: di.value, h: hi.value, s: si.value, p: pi.value, c: curDress.value.c }
  looks.value = [item, ...looks.value].slice(0, 12)
  try { localStorage.setItem(KEY, JSON.stringify(looks.value)) } catch (e) { /* 隐私模式忽略 */ }
  say('已收进衣柜 · ' + lookName.value)
}
function wearLook(l) {
  di.value = l.d; hi.value = l.h; si.value = l.s; pi.value = l.p
  say('已换上 · ' + l.name)
}
onUnmounted(() => clearTimeout(tipTimer))
</script>

<template>
  <div class="gx-game dress">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#C9A0F0,#8A5FD0)">裳</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 换装</p>
        <h2 class="gx-title">云裳换装记</h2>
      </div>
      <div class="gx-headright">
        <span class="d-count">衣柜 {{ looks.length }}</span>
      </div>
    </header>

    <div class="gx-stage">
      <div class="gx-stage-in d-wrap">
        <div class="d-stage" @pointermove="onMove" @pointerleave="onLeave">
          <div class="d-plate" :style="{ transform: 'rotateX(' + rx + 'deg) rotateY(' + ry + 'deg)' }">
            <svg class="d-svg" viewBox="0 0 320 440" xmlns="http://www.w3.org/2000/svg"
                 role="img" aria-label="换装立绘">
              <defs>
                <linearGradient id="dRobeG" x1="0.2" y1="0" x2="0.8" y2="1">
                  <stop offset="0" :stop-color="curDress.hi" />
                  <stop offset="0.42" :stop-color="curDress.c" />
                  <stop offset="1" :stop-color="curDress.c2" />
                </linearGradient>
                <linearGradient id="dSleeveG" x1="0" y1="0" x2="1" y2="1">
                  <stop offset="0" :stop-color="curDress.c" />
                  <stop offset="1" :stop-color="curDress.c2" />
                </linearGradient>
                <linearGradient id="dShawlG" x1="0" y1="0" x2="0.6" y2="1">
                  <stop offset="0" :stop-color="curShawl.c" stop-opacity="0.95" />
                  <stop offset="1" :stop-color="curShawl.c" stop-opacity="0.45" />
                </linearGradient>
                <linearGradient id="dSkinG" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0" stop-color="#FFF1E7" />
                  <stop offset="1" stop-color="#F6D8C8" />
                </linearGradient>
                <linearGradient id="dHairG" x1="0.1" y1="0" x2="0.9" y2="1">
                  <stop offset="0" stop-color="#4E3A5C" />
                  <stop offset="0.45" stop-color="#2C1F38" />
                  <stop offset="1" stop-color="#1A1122" />
                </linearGradient>
                <linearGradient id="dGoldG" x1="0" y1="0" x2="1" y2="1">
                  <stop offset="0" stop-color="#FFF0C2" />
                  <stop offset="0.5" stop-color="#E9C173" />
                  <stop offset="1" stop-color="#C79A4C" />
                </linearGradient>
                <radialGradient id="dHaloG" cx="0.5" cy="0.45" r="0.55">
                  <stop offset="0" stop-color="#FFFFFF" stop-opacity="0.95" />
                  <stop offset="0.62" stop-color="#FFEAF3" stop-opacity="0.55" />
                  <stop offset="1" stop-color="#FFDCEC" stop-opacity="0" />
                </radialGradient>
                <linearGradient id="dFloorG" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0" stop-color="#4A2C60" stop-opacity="0" />
                  <stop offset="1" stop-color="#2A1740" stop-opacity="0.95" />
                </linearGradient>
                <radialGradient id="dBlushG" cx="0.5" cy="0.5" r="0.5">
                  <stop offset="0" stop-color="#FF9DB4" stop-opacity="0.55" />
                  <stop offset="1" stop-color="#FF9DB4" stop-opacity="0" />
                </radialGradient>
                <filter id="dSoft" x="-30%" y="-30%" width="160%" height="160%">
                  <feGaussianBlur stdDeviation="6" />
                </filter>
              </defs>

              <circle cx="160" cy="196" r="126" fill="url(#dHaloG)" />
              <circle cx="160" cy="196" r="104" fill="none" stroke="#EBCFE0" stroke-width="0.9" opacity="0.55" />
              <ellipse cx="160" cy="196" rx="140" ry="120" fill="url(#dHaloG)" opacity="0.5" filter="url(#dSoft)" />
              <path d="M0 402 H320 V440 H0 Z" fill="url(#dFloorG)" />
              <ellipse cx="160" cy="405" rx="86" ry="11" fill="#B79FC4" opacity="0.22" filter="url(#dSoft)" />

              <path :d="curHair.back" fill="url(#dHairG)" />
              <path :d="curHair.back" fill="none" stroke="#9A7EAE" stroke-width="0.9" opacity="0.28" />

              <path d="M160 168 C150 168,142 172,136 178 C132 190,130 214,128 240
                       C124 274,114 330,104 386 C100 396,98 401,97 406
                       L223 406 C222 401,220 396,216 386 C206 330,196 274,192 240
                       C190 214,188 190,184 178 C178 172,170 168,160 168 Z"
                    fill="url(#dRobeG)" />
              <path d="M160 168 C150 168,142 172,136 178 C132 190,130 214,128 240
                       C124 274,114 330,104 386 C100 396,98 401,97 406
                       L132 406 C134 344,144 268,152 200 C154 182,156 173,160 168 Z"
                    fill="#FFFFFF" opacity="0.16" />
              <path d="M160 168 C170 168,178 172,184 178 C188 190,190 214,192 240
                       C196 274,206 330,216 386 C220 396,222 401,223 406
                       L196 406 C194 344,184 268,168 200 C166 182,164 173,160 168 Z"
                    fill="#000000" opacity="0.09" />
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

              <path d="M97 406 C130 399,190 399,223 406" fill="none" :stroke="curDress.edge" stroke-width="5.5" opacity="0.95" />
              <path d="M97 406 C130 399,190 399,223 406" fill="none" stroke="#FFFFFF" stroke-width="1.3" opacity="0.7" />
              <g :stroke="curDress.edge" stroke-width="1.5" fill="none" opacity="0.7" stroke-linecap="round">
                <path d="M118 384 q9 -6 18 0 q9 6 18 0" />
                <path d="M166 384 q9 -6 18 0 q9 6 18 0" />
                <path d="M140 356 q10 -7 20 0 q10 7 20 0" />
                <path d="M132 330 q9 -6 18 0 q9 6 18 0" opacity="0.7" />
              </g>

              <path d="M160 172 C150 174,142 180,139 188 L160 232 L181 188 C178 180,170 174,160 172 Z"
                    fill="#FFFDFE" opacity="0.94" />
              <path d="M139 188 L160 232 L157 236 L136 191 Z" :fill="curDress.edge" opacity="0.9" />
              <path d="M181 188 L160 232 L163 236 L184 191 Z" :fill="curDress.edge" opacity="0.9" />
              <path d="M160 232 L146 258 L160 250 L174 258 Z" fill="#FFFFFF" opacity="0.75" />

              <path d="M126 242 C144 254,176 254,194 242 L190 262 C174 273,146 273,130 262 Z"
                    :fill="curDress.c2" />
              <path d="M126 242 C144 254,176 254,194 242" fill="none" :stroke="curDress.edge"
                    stroke-width="2.4" opacity="0.95" />
              <path d="M130 262 C146 273,174 273,190 262" fill="none" stroke="#000000" stroke-width="1"
                    opacity="0.12" />
              <circle cx="160" cy="266" r="7" fill="none" stroke="url(#dGoldG)" stroke-width="2.2" />
              <circle cx="160" cy="266" r="2.8" fill="#F7A8C4" opacity="0.9" />

              <g :stroke="curDress.edge" fill="none" stroke-width="1.4" opacity="0.9">
                <path d="M147 174 C154 186,166 186,173 174" />
                <path d="M151 177 C156 185,164 185,169 177" opacity="0.6" />
              </g>
              <circle cx="160" cy="184" r="2.6" :fill="curDress.c" stroke="url(#dGoldG)" stroke-width="1" />

              <path d="M137 186 C118 194,100 218,91 250 C86 272,84 292,87 305
                       C100 310,113 302,119 288 C127 268,133 236,142 210 Z"
                    fill="url(#dSleeveG)" />
              <path d="M183 186 C202 194,220 218,229 250 C234 272,236 292,233 305
                       C220 310,207 302,201 288 C193 268,187 236,178 210 Z"
                    fill="url(#dSleeveG)" />
              <path d="M87 305 C100 310,113 302,119 288 L117 299 C112 309,100 315,89 310 Z"
                    fill="#FFFFFF" opacity="0.5" />
              <path d="M233 305 C220 310,207 302,201 288 L203 299 C208 309,220 315,231 310 Z"
                    fill="#FFFFFF" opacity="0.36" />
              <path d="M141 212 C134 240,128 268,122 288" fill="none" stroke="#FFFFFF" stroke-width="1.3" opacity="0.32" />
              <path d="M179 212 C186 240,192 268,198 288" fill="none" stroke="#FFFFFF" stroke-width="1.3" opacity="0.26" />
              <path d="M133 206 C126 236,120 266,114 292" fill="none" stroke="#000000" stroke-width="1" opacity="0.06" />
              <path d="M187 206 C194 236,200 266,206 292" fill="none" stroke="#000000" stroke-width="1" opacity="0.06" />
              <g :stroke="curDress.edge" stroke-width="1.5" fill="none" opacity="0.62">
                <path d="M92 294 C98 284,106 280,114 284" />
                <path d="M228 294 C222 284,214 280,206 284" />
              </g>

              <path d="M149 150 L171 150 L171 172 C171 182,149 182,149 172 Z" fill="#F3D0BE" />
              <path d="M149 168 C155 175,165 175,171 168 L171 172 C171 182,149 182,149 172 Z"
                    fill="#000000" opacity="0.10" />

              <path d="M160 74 C137 74,122 94,122 121 C122 138,127 152,134 163
                       C141 173,150 181,160 181 C170 181,179 173,186 163
                       C193 152,198 138,198 121 C198 94,183 74,160 74 Z"
                    fill="url(#dSkinG)" />
              <path d="M129 150 C134 162,144 174,160 181 C176 174,186 162,191 150
                       C185 164,175 174,160 178 C145 174,135 164,129 150 Z"
                    fill="#E7B9A8" opacity="0.20" />
              <path d="M146 95 C151 92,169 92,174 95 C168 88,152 88,146 95 Z"
                    fill="#E7B9A8" opacity="0.25" />
              <ellipse cx="140" cy="144" rx="10" ry="6" fill="url(#dBlushG)" opacity="0.8" />
              <ellipse cx="180" cy="144" rx="10" ry="6" fill="url(#dBlushG)" opacity="0.8" />

              <path d="M134 118 C140 112,150 111,156 115" fill="none" stroke="#7A5C6C" stroke-width="1.5"
                    stroke-linecap="round" opacity="0.7" />
              <path d="M164 115 C170 111,180 112,186 118" fill="none" stroke="#7A5C6C" stroke-width="1.5"
                    stroke-linecap="round" opacity="0.7" />
              <path d="M137 131 C143 126,152 126,156 130" fill="none" stroke="#3A2632" stroke-width="1.7"
                    stroke-linecap="round" />
              <path d="M164 130 C168 126,177 126,183 131" fill="none" stroke="#3A2632" stroke-width="1.7"
                    stroke-linecap="round" />
              <path d="M138 131 C143 127,151 127,155 130" fill="none" stroke="#8A5C66" stroke-width="1"
                    stroke-linecap="round" opacity="0.55" />
              <path d="M165 130 C169 127,177 127,182 131" fill="none" stroke="#8A5C66" stroke-width="1"
                    stroke-linecap="round" opacity="0.55" />
              <path d="M157 129 l4 -2.4 M184 130 l-4 -2.4" stroke="#2E1D28" stroke-width="1.3"
                    stroke-linecap="round" opacity="0.9" />
              <ellipse cx="147" cy="130" rx="2.6" ry="2.3" fill="#4A2E3C" />
              <ellipse cx="173" cy="130" rx="2.6" ry="2.3" fill="#4A2E3C" />
              <circle cx="148" cy="129.3" r="0.9" fill="#FFFFFF" opacity="0.95" />
              <circle cx="174" cy="129.3" r="0.9" fill="#FFFFFF" opacity="0.95" />
              <ellipse cx="146" cy="125" rx="7" ry="3" fill="#E88FA6" opacity="0.18" />
              <ellipse cx="174" cy="125" rx="7" ry="3" fill="#E88FA6" opacity="0.18" />
              <path d="M158 134 C158.6 138,158.6 141,156 143" fill="none" stroke="#DFB0A0"
                    stroke-width="1.1" stroke-linecap="round" opacity="0.7" />

              <path d="M154 154.5 C157 151.5,159 152.5,160 154 C161 152.5,163 151.5,166 154.5
                       C163 159.5,157 159.5,154 154.5 Z" fill="#E07C90" />
              <path d="M157 154 C158.5 153.2,161.5 153.2,163 154" fill="none" stroke="#FFE7EC"
                    stroke-width="0.8" stroke-linecap="round" />
              <path d="M155 154 C157 152,159 153,160 154" fill="none" stroke="#FFE3EA" stroke-width="0.8" />
              <path d="M160 106 l2.4 4.6 -2.4 4.6 -2.4 -4.6 Z" fill="#E2687F" opacity="0.9" />
              <circle cx="160" cy="111" r="1" fill="#FFF0C2" />

              <path v-if="curHair.bun" :d="curHair.bun" fill="url(#dHairG)" />
              <path v-if="curHair.bun" :d="curHair.bun" fill="none" stroke="#9A7EAE" stroke-width="1"
                    opacity="0.45" />
              <ellipse v-if="curHair.bun" cx="151" cy="60" rx="10" ry="6.5" fill="#B79BC9"
                       opacity="0.30" transform="rotate(-18 151 60)" />
              <path v-if="curHair.bun" d="M143 84 C150 90,170 90,177 84" fill="none"
                    stroke="#8F739F" stroke-width="1" opacity="0.4" />

              <path :d="curHair.crown" fill="url(#dHairG)" />
              <path d="M146 95 C151 99,155 101,160 99 C165 101,169 99,174 95"
                    fill="none" stroke="#7A5E8C" stroke-width="1.2" opacity="0.55" stroke-linecap="round" />
              <path d="M160 79 C160 86,160 90,160 96" fill="none" stroke="#A98BBE"
                    stroke-width="0.9" opacity="0.45" stroke-linecap="round" />
              <path d="M116 130 C114 144,115 156,118 168" fill="none" stroke="#A98BBE"
                    stroke-width="1.1" opacity="0.30" stroke-linecap="round" />
              <path d="M204 130 C206 144,205 156,202 168" fill="none" stroke="#A98BBE"
                    stroke-width="1.1" opacity="0.26" stroke-linecap="round" />

              <path :d="curHair.locks" fill="url(#dHairG)" />
              <path :d="curHair.locks" fill="none" stroke="#A98BBE" stroke-width="0.9" opacity="0.30" />

              <g v-if="curShawl.grad">
                <path d="M132 190 C104 208,88 250,86 300 C85 330,92 356,104 372"
                      fill="none" stroke="url(#dShawlG)" stroke-width="17" stroke-linecap="round" />
                <path d="M188 190 C216 208,232 250,234 300 C235 330,228 356,216 372"
                      fill="none" stroke="url(#dShawlG)" stroke-width="17" stroke-linecap="round" />
                <path d="M132 190 C104 208,88 250,86 300" fill="none" stroke="#FFFFFF"
                      stroke-width="1.6" opacity="0.5" stroke-linecap="round" />
                <path d="M188 190 C216 208,232 250,234 300" fill="none" stroke="#FFFFFF"
                      stroke-width="1.6" opacity="0.4" stroke-linecap="round" />
                <path d="M104 372 l-6 14 M216 372 l6 14" :stroke="curShawl.c" stroke-width="3" opacity="0.7"
                      stroke-linecap="round" />
                <circle cx="132" cy="192" r="4.5" fill="url(#dGoldG)" />
                <circle cx="188" cy="192" r="4.5" fill="url(#dGoldG)" />
              </g>

              <g v-if="curPin.type === 'pearl'">
                <path d="M130 96 C146 86,174 86,190 96" fill="none" stroke="url(#dGoldG)"
                      stroke-width="2.2" stroke-linecap="round" />
                <circle cx="160" cy="88" r="4.4" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1.2" />
                <circle cx="143" cy="90" r="3.1" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                <circle cx="177" cy="90" r="3.1" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                <circle cx="128" cy="99" r="2.4" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                <circle cx="192" cy="99" r="2.4" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                <path d="M160 84 l1.6 3.2 -1.6 3.2 -1.6 -3.2 Z" fill="#F7C8D8" />
              </g>
              <g v-else-if="curPin.type === 'buyao'">
                <path d="M186 96 L150 78" stroke="url(#dGoldG)" stroke-width="2.6" stroke-linecap="round" />
                <circle cx="150" cy="78" r="4.6" :fill="curDress.c" stroke="url(#dGoldG)" stroke-width="1.6" />
                <path d="M186 96 C190 112,186 126,180 136" fill="none" stroke="url(#dGoldG)"
                      stroke-width="1.6" stroke-linecap="round" />
                <circle cx="180" cy="138" r="3.6" :fill="curDress.c" />
                <path d="M180 142 l3 8 M180 142 l-3 8" stroke="#E9C173" stroke-width="1.2" stroke-linecap="round" />
                <path d="M138 100 C132 110,132 118,136 124" fill="none" stroke="url(#dGoldG)"
                      stroke-width="1.2" stroke-linecap="round" opacity="0.85" />
                <circle cx="136" cy="126" r="2.6" :fill="curDress.c" opacity="0.9" />
              </g>
              <g v-else>
                <path d="M186 100 L146 82" stroke="url(#dGoldG)" stroke-width="2.2" stroke-linecap="round" />
                <g fill="#F4A0BC">
                  <circle cx="146" cy="82" r="3" />
                  <circle cx="152" cy="78" r="2.4" />
                  <circle cx="141" cy="77" r="2.4" />
                  <circle cx="147" cy="73" r="2.4" />
                </g>
                <circle cx="146" cy="78" r="1.5" fill="#FFF0C2" />
              </g>

              <g v-if="curHair.ornament === 'bun-wreath'">
                <path d="M130 62 C144 54,176 54,190 62" fill="none" stroke="url(#dGoldG)"
                      stroke-width="1.8" stroke-linecap="round" opacity="0.9" />
                <circle cx="160" cy="56" r="3" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                <path d="M160 60 l1.2 2.4 -1.2 2.4 -1.2 -2.4 Z" fill="#F2A0BC" />
              </g>
              <g v-else-if="curHair.ornament === 'double-ring'">
                <path d="M114 58 C109 65,109 72,114 78" fill="none" stroke="url(#dGoldG)"
                      stroke-width="1.6" stroke-linecap="round" opacity="0.85" />
                <path d="M206 58 C211 65,211 72,206 78" fill="none" stroke="url(#dGoldG)"
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

              <path d="M160 280 C157 322,155 366,155 402" fill="none" stroke="#FFFFFF" stroke-width="1.6"
                    opacity="0.20" />
              <path d="M112 330 C108 356,105 382,104 402" fill="none" stroke="#000000" stroke-width="1"
                    opacity="0.06" />
              <path d="M208 330 C212 356,215 382,216 402" fill="none" stroke="#000000" stroke-width="1"
                    opacity="0.06" />
            </svg>

            <div class="d-gloss"></div>
            <div class="d-tag">{{ curDress.n }} · {{ curHair.n }}</div>
          </div>
          <div class="d-shadow"></div>
        </div>

        <div class="d-opts">
          <div class="d-opt">
            <p>裙色 <b>{{ curDress.n }}</b></p>
            <div class="d-swatches">
              <button v-for="(d, i) in DRESS" :key="'d' + i" type="button" class="d-swatch"
                      :class="{ on: di === i }"
                      :style="{ background: 'linear-gradient(140deg,' + d.hi + ',' + d.c + ' 55%,' + d.c2 + ')' }"
                      :title="d.n" :aria-label="d.n" @click="di = i" />
            </div>
          </div>
          <div class="d-opt">
            <p>发型 <b>{{ curHair.n }}</b></p>
            <div class="d-chips">
              <button v-for="(h, i) in HAIR" :key="'h' + i" type="button" class="gx-chip"
                      :class="{ on: hi === i }" @click="hi = i">{{ h.n }}</button>
            </div>
          </div>
          <div class="d-opt">
            <p>披帛 <b>{{ curShawl.n }}</b></p>
            <div class="d-chips">
              <button v-for="(s, i) in SHAWL" :key="'s' + i" type="button" class="gx-chip"
                      :class="{ on: si === i }" @click="si = i">{{ s.n }}</button>
            </div>
          </div>
          <div class="d-opt">
            <p>发饰 <b>{{ curPin.n }}</b></p>
            <div class="d-chips">
              <button v-for="(p, i) in PIN" :key="'p' + i" type="button" class="gx-chip"
                      :class="{ on: pi === i }" @click="pi = i">{{ p.n }}</button>
            </div>
          </div>
          <div class="d-look">{{ lookName }}</div>
          <div class="d-acts">
            <button class="gx-btn gold mini" type="button" @click="keepLook">收进衣柜</button>
            <button class="gx-btn ghost mini" type="button" @click="randomLook">随机一套</button>
          </div>
        </div>
      </div>

      <div v-if="looks.length" class="d-closet">
        <span class="d-closet-label">衣柜</span>
        <button v-for="(l, i) in looks" :key="'l' + i" type="button" class="d-look-chip" @click="wearLook(l)">
          <i :style="{ background: l.c }"></i>{{ l.name }}
        </button>
      </div>
    </div>

    <div class="gx-toast">{{ tip }}</div>
    <div class="gx-foot">
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.d-count{ font-size:11px; letter-spacing:2px; color:rgba(255,232,244,.6); }
.d-wrap{
  display:flex; gap:20px; align-items:stretch; flex-wrap:wrap;
  padding:16px 18px 18px; perspective:1400px;
}
.d-stage{
  position:relative; width:min(266px,62vw); flex:none; margin:0 auto;
  transform-style:preserve-3d;
}
.d-plate{
  position:relative; border-radius:20px; overflow:hidden;
  aspect-ratio:320 / 440;
  background:
    radial-gradient(70% 46% at 50% 34%, rgba(255,214,236,.30), transparent 70%),
    radial-gradient(120% 80% at 50% 0%, #43265F 0%, #2C1A46 46%, #180D28 100%);
  border:1px solid rgba(233,193,115,.42);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.18),
    inset 0 -18px 34px rgba(20,8,36,.45),
    0 18px 40px rgba(20,6,40,.6),
    0 0 34px rgba(255,150,200,.16);
  transition:transform .25s cubic-bezier(.22,1,.36,1);
  transform-style:preserve-3d;
}
.d-plate::before{
  content:""; position:absolute; inset:7px; border-radius:14px; pointer-events:none; z-index:3;
  border:1px solid rgba(233,193,115,.55);
  box-shadow:inset 0 0 0 1px rgba(255,255,255,.6);
}
.d-svg{ width:100%; height:100%; display:block; filter:drop-shadow(0 10px 18px rgba(60,20,70,.28));
  transform:scale(1.12); transform-origin:50% 48%; }
.d-gloss{
  position:absolute; inset:0; z-index:2; pointer-events:none; mix-blend-mode:screen;
  background:linear-gradient(115deg,transparent 30%,rgba(255,255,255,.45) 46%,transparent 58%);
  opacity:0; transition:opacity .3s;
}
.d-plate:hover .d-gloss{ opacity:1; animation:gx-shimmer 2.4s linear infinite; background-size:220% 100%; }
.d-tag{
  position:absolute; left:0; right:0; bottom:0; z-index:4;
  padding:26px 12px 9px; text-align:center;
  font-family:var(--gx-serif); font-size:11px; letter-spacing:2px; color:#FFD9EC;
  background:linear-gradient(180deg,rgba(24,13,40,0),rgba(20,10,34,.92) 58%);
}
.d-shadow{
  height:16px; margin:6px 22px 0; border-radius:50%;
  background:radial-gradient(ellipse,rgba(120,60,130,.45),transparent 70%);
  filter:blur(6px);
}
.d-opts{ flex:1; min-width:210px; display:flex; flex-direction:column; justify-content:center; gap:11px; }
.d-opt{ padding-bottom:9px; border-bottom:1px dashed rgba(233,193,115,.22); }
.d-opt:last-of-type{ border-bottom:0; padding-bottom:0; }
.d-opt p{
  margin:0 0 7px; font-size:10.5px; letter-spacing:2px; color:rgba(255,232,244,.5);
  display:flex; align-items:center; gap:7px;
}
.d-opt p b{ font-family:var(--gx-serif); font-weight:600; font-size:12px; letter-spacing:1px; color:#FFE9B8; }
.d-chips{ display:flex; gap:7px; flex-wrap:wrap; }
.d-swatches{ display:flex; gap:9px; }
.d-swatch{
  width:29px; height:29px; border-radius:50%; border:2px solid rgba(255,255,255,.85); cursor:pointer; padding:0;
  box-shadow:0 0 0 1px rgba(233,193,115,.4), 0 3px 9px rgba(0,0,0,.35),
             inset 0 -4px 8px rgba(0,0,0,.18), inset 0 3px 6px rgba(255,255,255,.5);
  transition:.2s cubic-bezier(.22,1,.36,1);
}
.d-swatch:hover{ transform:translateY(-2px) scale(1.07); }
.d-swatch.on{
  box-shadow:0 0 0 2px #fff, 0 0 0 4px #E9C173, 0 6px 16px rgba(226,80,140,.45),
             inset 0 -4px 8px rgba(0,0,0,.18), inset 0 3px 6px rgba(255,255,255,.5);
  transform:scale(1.05);
}
.d-look{
  font-family:var(--gx-serif); font-size:11.5px; color:#EBD9EC; letter-spacing:1px;
  padding:9px 12px; border-radius:11px;
  background:rgba(255,255,255,.07); border:1px solid rgba(233,193,115,.25);
  display:flex; align-items:center; gap:8px; line-height:1.6;
}
.d-look::before{ content:"造型"; font-size:9.5px; letter-spacing:2px; color:#C9A8B8;
  padding:2px 6px; border-radius:5px; background:rgba(255,255,255,.1); flex:none; }
.d-acts{ display:flex; gap:8px; flex-wrap:wrap; }
.d-closet{
  display:flex; align-items:center; gap:8px; flex-wrap:wrap;
  padding:0 18px 16px;
}
.d-closet-label{ font-size:10px; letter-spacing:2px; color:rgba(255,232,244,.45); }
.d-look-chip{
  display:inline-flex; align-items:center; gap:6px; cursor:pointer;
  border:1px solid rgba(255,225,245,.22); background:rgba(255,255,255,.06);
  color:#EBD9EC; border-radius:14px; padding:4px 10px 4px 5px; font-size:10.5px; letter-spacing:.5px;
  transition:.2s;
}
.d-look-chip:hover{ background:rgba(255,255,255,.16); transform:translateY(-1px); }
.d-look-chip i{ width:14px; height:14px; border-radius:50%; box-shadow:inset 0 0 0 1px rgba(255,255,255,.45); }
@media (max-width:640px){
  .d-stage{ width:min(230px,66vw); }
  .d-opts{ min-width:100%; }
}
</style>
