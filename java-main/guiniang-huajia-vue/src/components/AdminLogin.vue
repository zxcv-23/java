<script setup>
import { ref } from 'vue'
import { adminLogin, saveAdmin } from '../utils/adminApi'

// 后台登录：真实调用 /api/admin/login，成功后保存后台 JWT。
// 已移除「演示身份进入」——鉴权加固后不再允许无凭证进入并写入真实数据。
const emit = defineEmits(['loggedIn', 'back'])

const account = ref('')
const password = ref('')
const showPwd = ref(false)
const loading = ref(false)
const notice = ref('')
const noticeKind = ref('error') // error | info

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
  try {
    const admin = await adminLogin(acc, pwd)
    saveAdmin(admin)
    emit('loggedIn', admin)
  } catch (e) {
    const msg = e.message || '名讳或口令有误'
    // 后端未启动 / 接口缺失属于环境提示（金色），口令错误属于校验失败（红色）
    noticeKind.value = /冥府信道|尚未就绪|格式异常/.test(msg) ? 'info' : 'error'
    notice.value = msg
  } finally {
    loading.value = false
  }
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

      <p class="hint">首次使用默认账号 <code>admin / admin123</code>，登录后请到「账号安全」更换口令。</p>

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

.hint{
  margin:14px 0 0; font-size:11.5px; line-height:1.7; letter-spacing:.5px;
  color:var(--rose); opacity:.72; text-align:center;
}
.hint code{ color:var(--gold); font-family:inherit; }

.back{
  display:block; margin:12px auto 0; border:none; background:transparent; cursor:pointer;
  color:var(--rose); font-size:12.5px; letter-spacing:2px; font-family:inherit;
  padding:6px 10px; border-radius:999px; transition:.2s; opacity:.85;
}
.back:hover{ color:#fff; opacity:1; background:rgba(255,179,200,.12); }
</style>
