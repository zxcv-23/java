<script setup>
import { ref } from 'vue'

// 后台登录：优先请求后端 /api/admin/login；
// 后端未启动 / 接口未实现时，提供「演示身份进入」（不伪造登录成功，明确标注）。
const emit = defineEmits(['loggedIn', 'back'])

const API_BASE = 'http://127.0.0.1:8081'

const account = ref('')
const password = ref('')
const showPwd = ref(false)
const loading = ref(false)
const notice = ref('')
const noticeKind = ref('')   // error | info
const demoOffer = ref(false) // 接口不可用时是否给出演示入口

async function onSubmit() {
  const acc = account.value.trim()
  const pwd = password.value
  if (!acc || !pwd) {
    noticeKind.value = 'error'
    notice.value = '请填写名讳与口令'
    return
  }
  loading.value = true
  notice.value = ''
  demoOffer.value = false
  try {
    const res = await fetch(API_BASE + '/api/admin/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ account: acc, password: pwd })
    })
    if (res.ok) {
      const json = await res.json()
      if (json.code === 200 && json.data) {
        const admin = json.data
        try { window.localStorage.setItem('guiniang_admin', JSON.stringify(admin)) } catch {}
        emit('loggedIn', admin)
        return
      }
      noticeKind.value = 'error'
      notice.value = json.message || '名讳或口令有误'
    } else if (res.status === 404) {
      apiMissing()
    } else {
      noticeKind.value = 'error'
      notice.value = '冥府信道受阻（HTTP ' + res.status + '），请稍后再试'
    }
  } catch (e) {
    // 后端未启动
    noticeKind.value = 'error'
    notice.value = '冥府信道未通 · 后端未启动，无法核验名讳'
    demoOffer.value = true
  } finally {
    loading.value = false
  }
}

// 后台登录接口尚未实现时的提示（保留演示入口，便于查看后台界面排版）
function apiMissing() {
  noticeKind.value = 'info'
  notice.value = '后台登录接口（/api/admin/login）尚未实现，可暂以演示身份进入查看界面。'
  demoOffer.value = true
}

function enterDemo() {
  const admin = { account: 'admin', name: '判官（演示）', token: 'demo', demo: true }
  try { window.sessionStorage.setItem('guiniang_admin', JSON.stringify(admin)) } catch {}
  emit('loggedIn', admin)
}

function back() {
  emit('back')
}
</script>

<template>
  <div class="admin-login">
    <div class="crest" aria-hidden="true">🛡️</div>
    <h1 class="title">判官台 · 后台登录</h1>
    <p class="subtitle">生死簿  ·  姻缘簿  ·  皆在此掌管</p>

    <form class="card" @submit.prevent="onSubmit">
      <label class="field">
        <span class="field-label">名 讳</span>
        <input v-model="account" type="text" maxlength="30" autocomplete="username"
               placeholder="请输入管理员名讳" @input="notice = ''" />
      </label>

      <label class="field">
        <span class="field-label">口 令</span>
        <span class="pwd-wrap">
          <input v-model="password" :type="showPwd ? 'text' : 'password'" maxlength="40"
                 autocomplete="current-password" placeholder="请输入口令" @input="notice = ''" />
          <button class="eye" type="button" @click="showPwd = !showPwd" :aria-label="showPwd ? '隐藏口令' : '显示口令'">
            {{ showPwd ? '🙈' : '👁️' }}
          </button>
        </span>
      </label>

      <p v-if="notice" class="notice" :class="noticeKind">{{ notice }}</p>

      <button class="submit" type="submit" :disabled="loading">
        {{ loading ? '核验中…' : '入 台' }}
      </button>

      <button v-if="demoOffer" class="demo" type="button" @click="enterDemo">
        以演示身份进入（不写库，仅查看界面）
      </button>

      <button class="back" type="button" @click="back">← 返回前台首页</button>
    </form>
  </div>
</template>

<style scoped>
.admin-login{
  position:relative; z-index:5;
  width:min(400px, 92vw);
  text-align:center;
  padding:24px 0 40px;
}
.crest{ font-size:44px; filter:drop-shadow(0 4px 18px rgba(155,107,214,.6)); }
.title{
  margin:10px 0 6px; font-size:26px; letter-spacing:4px;
  font-family:var(--font-serif);
  background:linear-gradient(100deg,#FFE6F0,#D9B8FF 45%,#F4D58B 80%,#FFB3C8);
  background-size:200% auto; -webkit-background-clip:text; background-clip:text; color:transparent;
  animation:shimmer 8s linear infinite;
  filter:drop-shadow(0 2px 16px rgba(155,107,214,.4));
}
.subtitle{ margin:0 0 22px; font-size:12px; letter-spacing:4px; color:var(--gold); opacity:.85; }

.card{
  padding:26px 26px 22px; text-align:left;
  background:linear-gradient(180deg, rgba(255,247,251,.14), rgba(255,247,251,.07));
  border:1px solid rgba(155,107,214,.5);
  border-radius:var(--radius-xl);
  backdrop-filter:blur(14px) saturate(140%); -webkit-backdrop-filter:blur(14px) saturate(140%);
  box-shadow:var(--shadow-2), 0 0 30px rgba(155,107,214,.22), inset 0 1px 0 rgba(255,255,255,.16);
}
.field{ display:block; margin-bottom:16px; }
.field-label{
  display:block; margin-bottom:7px;
  font-size:12px; letter-spacing:3px; color:var(--gold);
}
.field input{
  width:100%; padding:12px 14px; border-radius:var(--radius-md);
  border:1px solid rgba(155,107,214,.4); outline:none;
  background:rgba(23,12,43,.45); color:var(--ivory);
  font-size:14px; font-family:inherit; transition:.2s;
}
.field input::placeholder{ color:rgba(255,233,243,.4); }
.field input:focus{ border-color:#B98CF0; box-shadow:var(--focus-ring-violet); }
.pwd-wrap{ position:relative; display:block; }
.pwd-wrap input{ padding-right:44px; }
.eye{
  position:absolute; right:6px; top:50%; transform:translateY(-50%);
  border:none; background:transparent; cursor:pointer; font-size:15px;
  padding:6px 8px; border-radius:8px; opacity:.75; transition:.2s;
}
.eye:hover{ opacity:1; background:rgba(255,255,255,.1); }

.notice{
  margin:0 0 14px; padding:9px 12px; border-radius:var(--radius-sm);
  font-size:12.5px; line-height:1.6; letter-spacing:.5px;
}
.notice.error{ color:#FFC9D9; background:rgba(232,74,120,.16); border:1px dashed rgba(255,111,156,.5); }
.notice.info{ color:var(--gold); background:rgba(244,213,139,.1); border:1px dashed rgba(244,213,139,.45); }

.submit{
  width:100%; border:none; cursor:pointer;
  padding:13px 0; border-radius:999px; margin-top:4px;
  font-family:var(--font-serif); font-size:15px; font-weight:700; letter-spacing:4px; color:#fff;
  background:linear-gradient(180deg,#9B6BD6,#6A3AA0);
  box-shadow:0 6px 0 #4A2680, 0 12px 22px rgba(106,58,160,.5), inset 0 1px 0 rgba(255,255,255,.3);
  transition:transform .14s, filter .2s, box-shadow .14s;
}
.submit:hover{ filter:brightness(1.07); transform:translateY(-2px); }
.submit:active{ transform:translateY(3px); box-shadow:0 2px 0 #4A2680, 0 6px 12px rgba(106,58,160,.4); }
.submit:disabled{ cursor:default; filter:grayscale(.25); }

.demo{
  width:100%; margin-top:10px; cursor:pointer;
  padding:10px 0; border-radius:999px;
  border:1px dashed rgba(244,213,139,.55); background:transparent;
  color:var(--gold); font-size:12.5px; letter-spacing:1px; font-family:inherit; transition:.2s;
}
.demo:hover{ color:#fff; background:rgba(244,213,139,.12); }

.back{
  display:block; margin:16px auto 0; border:none; background:transparent; cursor:pointer;
  color:var(--rose); font-size:12.5px; letter-spacing:2px; font-family:inherit;
  padding:6px 10px; border-radius:999px; transition:.2s; opacity:.85;
}
.back:hover{ color:#fff; opacity:1; background:rgba(255,179,200,.12); }
</style>
