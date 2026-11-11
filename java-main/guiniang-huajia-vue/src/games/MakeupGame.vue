<script setup>
import { ref, computed } from 'vue'

defineProps({ game: { type: Object, default: null } })
const emit = defineEmits(['back'])

/* ============ 胭脂妆匣 ============ */
const BROWS = [
  { n: '远山黛', poem: '远山含黛', d: 'M131 119 C140 111,151 110,157 115', mirror: 'M163 115 C169 110,180 111,189 119' },
  { n: '柳叶眉', poem: '柳叶初生', d: 'M131 121 C140 108,152 108,158 117', mirror: 'M162 117 C168 108,180 108,189 121' },
  { n: '新月眉', poem: '新月如钩', d: 'M135 119 C142 111,151 112,156 117', mirror: 'M164 117 C169 112,178 111,185 119' },
  { n: '剑眉', poem: '剑眉星目', d: 'M131 123 C140 117,150 111,157 107', mirror: 'M163 107 C170 111,180 117,189 123' }
]
const SHADOW = [
  { n: '不扫', c: 'transparent', o: 0 },
  { n: '桃夭', c: '#F07BA0', o: 0.34 },
  { n: '藕荷', c: '#B98AD8', o: 0.32 },
  { n: '杏子', c: '#F0A868', o: 0.30 },
  { n: '青黛', c: '#6F8FC4', o: 0.28 }
]
const BLUSH = [
  { n: '素面', o: 0, r: 9 },
  { n: '薄施', o: 0.30, r: 10 },
  { n: '浓抹', o: 0.52, r: 12 }
]
const LIPS = [
  { n: '绛红', poem: '点绛唇', c: '#C73A55', shape: 'small' },
  { n: '胭脂', poem: '胭脂初透', c: '#E07C90', shape: 'petal' },
  { n: '蜜桃', poem: '桃夭灼灼', c: '#F2879C', shape: 'full' },
  { n: '豆沙', poem: '豆沙含露', c: '#B96A75', shape: 'petal' }
]
const HUADIAN = [
  { n: '素面', type: 'none' },
  { n: '梅钿', type: 'plum' },
  { n: '莲钿', type: 'lotus' },
  { n: '蝶钿', type: 'butterfly' },
  { n: '珠钿', type: 'pearl' }
]

const bi = ref(0), si = ref(1), ci = ref(1), li = ref(1), ni = ref(0)
const curBrow = computed(() => BROWS[bi.value])
const curShadow = computed(() => SHADOW[si.value])
const curBlush = computed(() => BLUSH[ci.value])
const curLip = computed(() => LIPS[li.value])
const curHua = computed(() => HUADIAN[ni.value])

const lookName = computed(() => curBrow.value.poem + ' · ' + curLip.value.poem)
const grade = computed(() => {
  const extra = (si.value > 0 ? 1 : 0) + (ci.value > 0 ? 1 : 0) + (ni.value > 0 ? 1 : 0)
  return ['七分', '八分', '九分', '十分'][extra]
})

const done = ref(false)
function finish() { done.value = true }
function reset() {
  bi.value = 0; si.value = 1; ci.value = 1; li.value = 1; ni.value = 0; done.value = false
}
</script>

<template>
  <div class="gx-game makeup">
    <header class="gx-head">
      <div class="gx-medallion" style="--mc:linear-gradient(135deg,#FFB3A7,#E8708F)">妆</div>
      <div class="gx-headtxt">
        <p class="gx-kicker">花间小游戏 · 妆容</p>
        <h2 class="gx-title">胭脂妆匣</h2>
      </div>
      <div class="gx-headright">
        <button class="gx-btn ghost mini" type="button" @click="reset">卸妆重来</button>
      </div>
    </header>

    <div class="gx-stage">
      <div class="gx-stage-in mk-wrap">
        <!-- 铜镜 -->
        <div class="mk-mirror">
          <div class="mk-frame">
            <svg class="mk-face" viewBox="92 44 136 136" role="img" aria-label="妆面预览">
              <defs>
                <radialGradient id="mkSkin" cx="0.42" cy="0.34" r="0.78">
                  <stop offset="0" stop-color="#FFF6EE" />
                  <stop offset="0.68" stop-color="#FCE3D6" />
                  <stop offset="1" stop-color="#F2CDBC" />
                </radialGradient>
                <linearGradient id="mkHair" x1="0.1" y1="0" x2="0.9" y2="1">
                  <stop offset="0" stop-color="#4E3A5C" />
                  <stop offset="0.5" stop-color="#2C1F38" />
                  <stop offset="1" stop-color="#1A1122" />
                </linearGradient>
                <radialGradient id="mkBlush" cx="0.5" cy="0.5" r="0.5">
                  <stop offset="0" :stop-color="curShadow.c" :stop-opacity="curBlush.o" />
                  <stop offset="1" stop-color="#FF9DB4" stop-opacity="0" />
                </radialGradient>
                <radialGradient id="mkHalo" cx="0.5" cy="0.4" r="0.6">
                  <stop offset="0" stop-color="#FFF8F0" stop-opacity=".95" />
                  <stop offset="1" stop-color="#FFE1EC" stop-opacity="0" />
                </radialGradient>
                <filter id="mkSoft" x="-40%" y="-40%" width="180%" height="180%">
                  <feGaussianBlur stdDeviation="4" />
                </filter>
              </defs>

              <ellipse cx="160" cy="120" rx="62" ry="72" fill="url(#mkHalo)" />
              <!-- 后发 -->
              <path d="M160 68 C126 66,110 92,108 128 C104 168,102 196,106 214 L118 220
                       C114 190,116 158,120 138 C126 110,138 96,160 96
                       C182 96,194 110,200 138 C204 158,206 190,202 220 L214 214
                       C218 196,216 168,212 128 C210 92,194 66,160 68 Z" fill="url(#mkHair)" />
              <!-- 脖颈 -->
              <path d="M146 158 L174 158 L174 182 C174 192,146 192,146 182 Z" fill="#F3D0BE" />
              <!-- 脸 -->
              <path d="M160 74 C137 74,122 94,122 121 C122 138,127 152,134 163
                       C141 173,150 181,160 181 C170 181,179 173,186 163
                       C193 152,198 138,198 121 C198 94,183 74,160 74 Z"
                    fill="url(#mkSkin)" />
              <path d="M129 150 C134 162,144 174,160 181 C176 174,186 162,191 150
                       C185 164,175 174,160 178 C145 174,135 164,129 150 Z"
                    fill="#E7B9A8" opacity="0.16" />
              <!-- 腮红 / 眼影底色 -->
              <ellipse cx="139" cy="146" :rx="curBlush.r + 2" :ry="curBlush.r * 0.72"
                       fill="url(#mkBlush)" />
              <ellipse cx="181" cy="146" :rx="curBlush.r + 2" :ry="curBlush.r * 0.72"
                       fill="url(#mkBlush)" />
              <ellipse cx="146" cy="123" rx="10" ry="5"
                       :fill="curShadow.c" :opacity="curShadow.o" filter="url(#mkSoft)" />
              <ellipse cx="174" cy="123" rx="10" ry="5"
                       :fill="curShadow.c" :opacity="curShadow.o" filter="url(#mkSoft)" />

              <!-- 眉 -->
              <path :d="curBrow.d" fill="none" stroke="#5C3E4E" stroke-width="1.9" stroke-linecap="round" />
              <path :d="curBrow.mirror" fill="none" stroke="#5C3E4E" stroke-width="1.9" stroke-linecap="round" />

              <!-- 眼 -->
              <path d="M137 132 C143 127,152 127,157 131" fill="none" stroke="#3A2632" stroke-width="1.9" stroke-linecap="round" />
              <path d="M163 131 C168 127,177 127,183 132" fill="none" stroke="#3A2632" stroke-width="1.9" stroke-linecap="round" />
              <path d="M157 130 l4 -2.6 M184 131 l-4 -2.6" stroke="#2E1D28" stroke-width="1.4" stroke-linecap="round" />
              <ellipse cx="147" cy="131" rx="3" ry="2.6" fill="#4A2E3C" />
              <ellipse cx="173" cy="131" rx="3" ry="2.6" fill="#4A2E3C" />
              <circle cx="148" cy="130.2" r="1" fill="#FFFFFF" opacity="0.95" />
              <circle cx="174" cy="130.2" r="1" fill="#FFFFFF" opacity="0.95" />
              <path d="M158 136 C158.6 140,158.6 143,156 145" fill="none" stroke="#DFB0A0" stroke-width="1.1" stroke-linecap="round" opacity="0.65" />

              <!-- 花钿 -->
              <g v-if="curHua.type === 'plum'" fill="#D94F6E">
                <circle cx="160" cy="111" r="3.4" />
                <circle cx="160" cy="104" r="2" />
                <circle cx="166" cy="108" r="2" />
                <circle cx="163" cy="115" r="2" />
                <circle cx="157" cy="115" r="2" />
                <circle cx="154" cy="108" r="2" />
                <circle cx="160" cy="110" r="1.3" fill="#FFF0C2" />
              </g>
              <g v-else-if="curHua.type === 'lotus'" fill="#E2687F">
                <path d="M160 108 L164 114 L160 118 L156 114 Z" />
                <path d="M153 111 C155 113,156 116,156 118 C153 117,152 114,153 111 Z" opacity=".85" />
                <path d="M167 111 C165 113,164 116,164 118 C167 117,168 114,167 111 Z" opacity=".85" />
                <circle cx="160" cy="113" r="1.1" fill="#FFF0C2" />
              </g>
              <g v-else-if="curHua.type === 'butterfly'" fill="#D95C86">
                <path d="M160 111 C156 105,150 106,152 111 C150 116,156 117,160 111 Z" />
                <path d="M160 111 C164 105,170 106,168 111 C170 116,164 117,160 111 Z" />
                <path d="M160 108 L160 114" stroke="#8A3F58" stroke-width="1" />
              </g>
              <g v-else-if="curHua.type === 'pearl'">
                <circle cx="160" cy="111" r="3" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
                <circle cx="159" cy="110" r="1" fill="#FFFFFF" />
              </g>

              <!-- 唇 -->
              <path v-if="curLip.shape === 'small'"
                    d="M154 155 C157 152,159 153.5,160 155 C161 153.5,163 152,166 155
                       C163 159,157 159,154 155 Z" :fill="curLip.c" />
              <path v-else-if="curLip.shape === 'petal'"
                    d="M152.5 154 C156 150,159 152.5,160 155 C161 152.5,164 150,167.5 154
                       C164 160,156 160,152.5 154 Z" :fill="curLip.c" />
              <ellipse v-else cx="160" cy="155" rx="9.5" ry="6.4" :fill="curLip.c" />
              <path d="M156.5 154.6 C158.5 153.6,161.5 153.6,163.5 154.6" fill="none"
                    stroke="#FFE7EC" stroke-width="0.9" stroke-linecap="round" opacity="0.9" />
              <ellipse cx="157.5" cy="156.6" rx="2.4" ry="1.1" fill="#FFFFFF" opacity="0.28" />

              <!-- 发顶与发饰 -->
              <path d="M160 50 C134 50,118 76,116 114 C116 128,118 140,120 148
                       C121 130,122 108,127 96 C134 86,146 80,160 80
                       C174 80,186 86,193 96 C198 108,199 130,200 148
                       C202 140,204 128,204 114 C202 76,186 50,160 50 Z" fill="url(#mkHair)" />
              <path d="M160 66 C160 74,160 78,160 84" fill="none" stroke="#A98BBE"
                    stroke-width="0.9" opacity="0.4" stroke-linecap="round" />
              <g stroke="#AE90C2" fill="none" stroke-width="1" opacity=".42" stroke-linecap="round">
                <path d="M124 112 C122 126,123 138,126 150" />
                <path d="M196 112 C198 126,197 138,194 150" />
                <path d="M130 92 C127 96,125 100,124 104" />
                <path d="M190 92 C193 96,195 100,196 104" />
              </g>
              <path d="M146 88 C151 92,155 94,160 92 C165 94,169 92,174 88" fill="none"
                    stroke="#7A5E8C" stroke-width="1.2" opacity="0.5" stroke-linecap="round" />
              <path d="M128 60 C142 52,178 52,192 60" fill="none" stroke="#E9C173" stroke-width="1.8"
                    stroke-linecap="round" opacity="0.9" />
              <circle cx="160" cy="54" r="3" fill="#FFFCF4" stroke="#E0C48A" stroke-width="1" />
              <path d="M160 58 l1.3 2.6 -1.3 2.6 -1.3 -2.6 Z" fill="#F2A0BC" />
              <path d="M191 62 C197 74,196 86,190 96" fill="none" stroke="#E9C173" stroke-width="1.5"
                    stroke-linecap="round" opacity="0.8" />
              <circle cx="190" cy="98" r="3" fill="#F2719F" stroke="#E9C173" stroke-width="1" />
            </svg>
            <span class="mk-glint"></span>
          </div>
          <p class="mk-name">{{ lookName }}</p>
          <p class="mk-grade">妆成 {{ grade }}</p>
        </div>

        <!-- 妆奁控制 -->
        <div class="mk-tray">
          <div class="mk-row">
            <p>眉形 <b>{{ curBrow.n }}</b></p>
            <div class="mk-chips">
              <button v-for="(b, i) in BROWS" :key="b.n" type="button" class="gx-chip"
                      :class="{ on: bi === i }" @click="bi = i">{{ b.n }}</button>
            </div>
          </div>
          <div class="mk-row">
            <p>眼影 <b>{{ curShadow.n }}</b></p>
            <div class="mk-pans">
              <button v-for="(s, i) in SHADOW" :key="'s' + i" type="button" class="mk-pan"
                      :class="{ on: si === i }" :title="s.n" :style="{ '--pan': s.c === 'transparent' ? '#3A2A44' : s.c }"
                      @click="si = i"><i v-if="i === 0">素</i></button>
            </div>
          </div>
          <div class="mk-row">
            <p>胭脂 <b>{{ curBlush.n }}</b></p>
            <div class="mk-chips">
              <button v-for="(c, i) in BLUSH" :key="'c' + i" type="button" class="gx-chip"
                      :class="{ on: ci === i }" @click="ci = i">{{ c.n }}</button>
            </div>
          </div>
          <div class="mk-row">
            <p>唇色 <b>{{ curLip.n }}</b></p>
            <div class="mk-pans">
              <button v-for="(l, i) in LIPS" :key="l.n" type="button" class="mk-pan"
                      :class="{ on: li === i }" :title="l.n" :style="{ '--pan': l.c }" @click="li = i"></button>
            </div>
          </div>
          <div class="mk-row">
            <p>花钿 <b>{{ curHua.n }}</b></p>
            <div class="mk-chips">
              <button v-for="(h, i) in HUADIAN" :key="h.n" type="button" class="gx-chip"
                      :class="{ on: ni === i }" @click="ni = i">{{ h.n }}</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="done" class="gx-result">
        <div class="gx-result-card">
          <div class="gx-seal">妆</div>
          <h3>{{ lookName }}</h3>
          <p>{{ curBrow.n }}配{{ curLip.n }}，{{ curHua.n === '素面' ? '素面朝天亦自清' : '额间一点' + curHua.n }}，<br>
            {{ curBlush.n }}胭脂衬得面色如春。此妆评 {{ grade }}。</p>
          <div class="gx-foot">
            <button class="gx-btn primary" type="button" @click="done = false">再描两笔</button>
            <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
          </div>
        </div>
      </div>
    </div>

    <p class="gx-hint">点选眉形、眼影、胭脂、唇色与花钿，镜中人即时换妆。素净或浓艳，皆由心意。</p>
    <div class="gx-foot">
      <button class="gx-btn primary" type="button" @click="finish">定妆</button>
      <button class="gx-btn ghost" type="button" @click="emit('back')">返回推荐</button>
    </div>
  </div>
</template>

<style scoped>
.mk-wrap{ display:flex; gap:22px; align-items:center; flex-wrap:wrap; padding:18px 20px 20px; }
.mk-mirror{ flex:none; width:min(250px,60vw); margin:0 auto; text-align:center; }
.mk-frame{
  position:relative; aspect-ratio:1; border-radius:50%; padding:10px;
  background:
    conic-gradient(from 210deg,#C79A4C,#FFEFC6 12%,#C79A4C 26%,#8E6A32 42%,#FFE7B8 58%,#B98B3F 74%,#FFEFC6 88%,#C79A4C);
  box-shadow:0 18px 40px rgba(6,2,16,.55), inset 0 0 0 1px rgba(255,255,255,.25);
}
.mk-frame::after{
  content:""; position:absolute; inset:10px; border-radius:50%; pointer-events:none; z-index:3;
  box-shadow:inset 0 0 30px rgba(120,60,40,.35), inset 0 0 0 1px rgba(255,255,255,.35);
}
.mk-face{
  width:100%; height:100%; display:block; border-radius:50%;
  background:radial-gradient(circle at 42% 30%,#FFF7F1,#F6DFE6 62%,#E3C4D6 100%);
}
.mk-glint{
  position:absolute; inset:10px; border-radius:50%; z-index:2; pointer-events:none;
  background:linear-gradient(125deg,transparent 34%,rgba(255,255,255,.55) 46%,transparent 56%);
  animation:mk-sheen 5.5s ease-in-out infinite;
}
@keyframes mk-sheen{ 0%,72%{ opacity:0; transform:translateX(-16%) } 84%{ opacity:1 } 100%{ opacity:0; transform:translateX(18%) } }
.mk-name{ margin:12px 0 3px; font-family:var(--gx-serif); font-size:15px; letter-spacing:2px; color:#FFE9B8; }
.mk-grade{ margin:0; font-size:10.5px; letter-spacing:2px; color:rgba(255,232,244,.55); }
.mk-tray{ flex:1; min-width:220px; display:flex; flex-direction:column; gap:11px; }
.mk-row{ padding-bottom:9px; border-bottom:1px dashed rgba(233,193,115,.20); }
.mk-row:last-child{ border-bottom:0; padding-bottom:0; }
.mk-row p{ margin:0 0 7px; font-size:10.5px; letter-spacing:2px; color:rgba(255,232,244,.5);
  display:flex; align-items:center; gap:7px; }
.mk-row p b{ font-family:var(--gx-serif); font-size:12px; letter-spacing:1px; color:#FFE9B8; }
.mk-chips{ display:flex; gap:7px; flex-wrap:wrap; }
.mk-pans{ display:flex; gap:8px; align-items:center; }
.mk-pan{
  width:28px; height:28px; border-radius:50%; cursor:pointer; padding:0;
  border:1px solid rgba(255,255,255,.35);
  background:radial-gradient(circle at 34% 28%, rgba(255,255,255,.55), var(--pan) 58%, rgba(0,0,0,.28));
  box-shadow:inset 0 -3px 6px rgba(0,0,0,.30), 0 3px 8px rgba(0,0,0,.35);
  transition:.2s cubic-bezier(.22,1,.36,1);
  display:flex; align-items:center; justify-content:center;
}
.mk-pan i{ font-style:normal; font-size:9px; color:#C9B8D0; }
.mk-pan:hover{ transform:translateY(-2px) scale(1.07); }
.mk-pan.on{
  box-shadow:0 0 0 2px #FFF7EF, 0 0 0 4px #E9C173, 0 0 18px rgba(255,200,120,.5),
             inset 0 -3px 6px rgba(0,0,0,.30);
  transform:scale(1.05);
}
@media (max-width:640px){
  .mk-wrap{ gap:14px; padding:14px; }
  .mk-tray{ min-width:100%; }
}
</style>
