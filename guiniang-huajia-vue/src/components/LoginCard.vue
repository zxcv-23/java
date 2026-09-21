<script setup>
import { ref, nextTick } from 'vue'
import GhostBride from './GhostBride.vue'

const emit = defineEmits(['celebrate'])

// ---------- 表单状态 ----------
const cardEl = ref(null)
const userInput = ref(null)
const passInput = ref(null)

const username = ref('')
const password = ref('')
const showPass = ref(false)
const remember = ref(false)

const loading = ref(false)
const success = ref(false)
const hintMsg = ref('')
const hintShow = ref(false)
const shaking = ref(false)

// 卡片抖动：先移除 class，等 DOM 更新后强制 reflow 再加回，让动画能重复触发
async function retriggerShake() {
  shaking.value = false
  await nextTick()
  if (cardEl.value) void cardEl.value.offsetWidth
  shaking.value = true
}

function showHint(msg) {
  hintMsg.value = msg
  hintShow.value = true
  retriggerShake()
}

function togglePass() {
  showPass.value = !showPass.value
  passInput.value && passInput.value.focus()
}

function onSubmit() {
  if (loading.value || success.value) return
  const u = username.value.trim()
  const pw = password.value.trim()
  if (!u) { showHint('夫君还不肯报名吗？'); userInput.value.focus(); return }
  if (!pw) { showHint('没有暗语，如何与我结契～'); passInput.value.focus(); return }

  hintShow.value = false
  loading.value = true
  setTimeout(() => {
    loading.value = false
    success.value = true
    // 通知父组件：花嫁害羞 + 全屏爱心雨（带上卡片位置供粒子定位）
    emit('celebrate', cardEl.value.getBoundingClientRect())
  }, 950)
}
</script>

<template>
  <main ref="cardEl" class="card" :class="{ shake: shaking }">
    <GhostBride :happy="success" />

    <h1>鬼娘花嫁</h1>
    <p class="sub">月 夜 婚 礼 · 请 君 入 瓮</p>

    <form novalidate @submit.prevent="onSubmit">
      <div class="field">
        <label for="user">夫君名讳</label>
        <div class="input-wrap">
          <span class="ico">💍</span>
          <input
            id="user" ref="userInput" v-model="username" type="text"
            placeholder="留下你的名字，与我结契" autocomplete="username"
            @input="hintShow = false"
          />
        </div>
      </div>

      <div class="field">
        <label for="pass">结契暗语</label>
        <div class="input-wrap">
          <span class="ico">🕯️</span>
          <input
            id="pass" ref="passInput" v-model="password"
            :type="showPass ? 'text' : 'password'"
            placeholder="嘘…别让阎王听见" autocomplete="current-password"
            @input="hintShow = false"
          />
          <button class="toggle" type="button" :aria-label="showPass ? '隐藏暗语' : '显示暗语'" @click="togglePass">
            {{ showPass ? '🙈' : '👀' }}
          </button>
        </div>
      </div>

      <div class="row">
        <label class="remember">
          <input v-model="remember" type="checkbox" />
          <span class="box"></span>
          <span>来世也要记得我</span>
        </label>
        <a class="link" href="#">暗语飘散在风里了</a>
      </div>

      <button class="btn" type="submit" :class="{ ok: success }" :disabled="loading || success">
        {{ success ? '契约成立 ♥ 你的灵魂，归我了' : (loading ? '正在结契…' : '缔 结 婚 约') }}
      </button>
      <p class="hint" :class="{ show: hintShow }">{{ hintMsg }}</p>
    </form>

    <div class="divider">或借冥婚之媒</div>
    <div class="socials">
      <button class="social" type="button" title="彼岸">💀</button>
      <button class="social" type="button" title="红烛">🕯️</button>
      <button class="social" type="button" title="玫瑰">🌹</button>
    </div>

    <p class="foot">还未被选中？<a class="link" href="#">那便再等一等吧</a></p>
  </main>
</template>

<style scoped>
/* ---------- 卡片 ---------- */
.card{
  position:relative; z-index:5;
  width:min(440px,94vw);
  padding:22px 30px 30px;
  background:linear-gradient(180deg,#FFFDFE 0%, #FDF3F8 100%);
  border:2px solid var(--gold);
  border-radius:26px;
  box-shadow:0 30px 70px rgba(20,8,40,.55), 0 0 0 6px rgba(255,179,200,.18), 0 0 40px rgba(232,74,120,.25) inset;
  text-align:center;
  animation:cardIn .8s cubic-bezier(.22,1,.36,1) both;
}
@keyframes cardIn{ from{opacity:0; transform:translateY(28px) scale(.96);} to{opacity:1; transform:none;} }
.card.shake{ animation:shake .45s cubic-bezier(.36,.07,.19,.97); }
@keyframes shake{
  10%,90%{ transform:translateX(-6px);} 20%,80%{ transform:translateX(7px);}
  30%,50%,70%{ transform:translateX(-9px);} 40%,60%{ transform:translateX(9px);}
}
.card::before{
  content:"💕"; position:absolute; top:-14px; left:50%; transform:translateX(-50%);
  font-size:24px; filter:drop-shadow(0 2px 4px rgba(232,74,120,.4));
}
.card::after{
  content:""; position:absolute; inset:9px; border:1px dashed rgba(232,74,120,.35);
  border-radius:18px; pointer-events:none;
}

h1{
  margin:12px 0 4px; font-size:26px; letter-spacing:3px;
  font-family:Georgia,"Songti SC","STSong","SimSun",serif; color:var(--ink);
  text-shadow:0 0 18px rgba(255,179,200,.5);
}
.sub{ margin:0 0 22px; font-size:12px; letter-spacing:4px; color:var(--pink-deep); font-weight:600; }

form{ text-align:left; }

.field{ margin-bottom:15px; }
.field label{
  display:block; margin:0 0 7px 4px; font-size:12.5px; font-weight:600;
  color:var(--ink-soft); letter-spacing:1px;
}
.input-wrap{ position:relative; }
.input-wrap .ico{ position:absolute; left:15px; top:50%; transform:translateY(-50%); font-size:15px; opacity:.55; pointer-events:none; }
input[type="text"], input[type="password"]{
  width:100%; padding:14px 46px 14px 42px; font-size:15px; color:var(--ink);
  background:#FFFDFE; border:2px solid var(--line); border-radius:15px; outline:none;
  transition:border-color .2s, box-shadow .2s; font-family:inherit;
}
input::placeholder{ color:#C9B3C2; }
input:hover{ border-color:#F3C9DC; }
input:focus{ border-color:var(--pink); box-shadow:0 0 0 4px rgba(255,111,156,.22); }

.toggle{
  position:absolute; right:8px; top:50%; transform:translateY(-50%);
  width:34px; height:34px; display:grid; place-items:center; border:none; background:transparent;
  border-radius:10px; font-size:15px; cursor:pointer; opacity:.6; transition:.18s;
}
.toggle:hover{ opacity:1; background:#FCE6EF; }

.row{ display:flex; align-items:center; justify-content:space-between; margin:4px 2px 20px; font-size:12.5px; color:var(--ink-soft); }
.remember{ display:flex; align-items:center; gap:7px; cursor:pointer; user-select:none; }
.remember input{ position:absolute; opacity:0; pointer-events:none; }
.box{ width:17px; height:17px; border-radius:6px; border:2px solid var(--line); background:#FFFDFE;
      display:grid; place-items:center; transition:.18s; font-size:10px; color:#fff; line-height:1; }
.remember input:checked + .box{ background:var(--pink); border-color:var(--pink); }
.remember input:checked + .box::after{ content:"♥"; font-size:11px; }
a.link{ color:var(--pink-deep); text-decoration:none; border-bottom:1px dashed rgba(232,74,120,.55); }
a.link:hover{ color:#C93A66; }

.btn{
  width:100%; padding:15px; border:none; border-radius:17px;
  font-family:Georgia,"Songti SC",serif; font-size:16px; font-weight:700; letter-spacing:4px; color:#fff;
  background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 6px 0 #C93363, 0 14px 24px rgba(232,74,120,.45);
  cursor:pointer; transition:transform .14s, box-shadow .14s, filter .2s;
}
.btn:hover{ filter:brightness(1.05); transform:translateY(-2px); box-shadow:0 8px 0 #C93363, 0 18px 28px rgba(232,74,120,.5); }
.btn:active{ transform:translateY(4px); box-shadow:0 2px 0 #C93363, 0 6px 12px rgba(232,74,120,.35); }
.btn:disabled{ cursor:default; }
.btn.ok{ background:linear-gradient(180deg,#9B6BD6,#6A3AA0); box-shadow:0 6px 0 #4F2A7E, 0 12px 20px rgba(106,58,160,.45); }

.hint{ margin:12px 0 0; min-height:18px; font-size:12.5px; color:var(--pink-deep); opacity:0; transform:translateY(-4px); transition:.22s; }
.hint.show{ opacity:1; transform:none; }

.divider{ display:flex; align-items:center; gap:12px; margin:22px 0 16px; color:#D8BBD0; font-size:11px; letter-spacing:1px; }
.divider::before, .divider::after{ content:""; flex:1; height:1px; background:var(--line); }

.socials{ display:flex; gap:12px; justify-content:center; }
.social{ width:52px; height:44px; border-radius:14px; border:2px solid var(--line); background:#FFFDFE;
         display:grid; place-items:center; font-size:18px; cursor:pointer; transition:.18s; }
.social:hover{ border-color:var(--pink); transform:translateY(-3px); box-shadow:0 8px 14px rgba(255,111,156,.3); }

.foot{ margin:20px 0 0; font-size:12.5px; color:var(--ink-soft); }

@media (max-width:420px){ .card{ padding:18px 20px 24px; border-radius:22px;} .mascot{ width:152px;} h1{ font-size:22px;} }
</style>
