<script setup>
import { ref, computed, onMounted } from 'vue'

// 个人资料编辑页：进入即直接编辑当前夫君的资料，上传头像，修改密码。
// 鉴权沿用前台登录时已下发的 JWT（guiniang_user.token），直接调后端 /api/profile。
const props = defineProps({
  user: { type: Object, default: null }
})
const emit = defineEmits(['back', 'logout', 'tokenRefreshed', 'needLogin'])

const API_BASE = 'http://127.0.0.1:8081'

const token = computed(() => (props.user && props.user.token) || '')

// ---------- 状态 ----------
const loading = ref(false)
const notice = ref('')
const profile = ref(null)

const saving = ref(false)
const form = ref({ nickname: '', email: '', phone: '', gender: 0, bio: '' })

const uploading = ref(false)
const avatarInput = ref(null)

const showPwd = ref(false)
const pwd = ref({ old: '', new: '' })
const pwdLoading = ref(false)

const toast = ref('')
let toastTimer = null
function flash(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 2400)
}

// 头像完整地址（后端返回的是 /uploads/xxx.jpg，需拼上后端域名）
const avatarUrl = computed(() => {
  const a = profile.value && profile.value.avatar
  if (!a) return ''
  return a.startsWith('http') ? a : API_BASE + a
})

// ---------- 拉取资料 ----------
async function loadProfile() {
  if (!token.value) {
    notice.value = '还未缔结婚约（未登录），无法编辑资料。'
    return
  }
  loading.value = true
  notice.value = ''
  try {
    const res = await fetch(API_BASE + '/api/profile', {
      headers: { Authorization: 'Bearer ' + token.value }
    })
    if (res.status === 401) {
      notice.value = '登录已失效，请重新缔结婚约后再来编辑资料。'
      return
    }
    const json = await res.json()
    if (json.code === 200 && json.data) {
      profile.value = json.data
      fillForm(json.data)
    } else {
      notice.value = json.message || '获取资料失败'
    }
  } catch (e) {
    notice.value = '冥府信道未通 · 后端未启动，无法载入个人资料。'
  } finally {
    loading.value = false
  }
}

function fillForm(p) {
  form.value = {
    nickname: p.nickname || '',
    email: p.email || '',
    phone: p.phone || '',
    gender: p.gender == null ? 0 : p.gender,
    bio: p.bio || ''
  }
}

// ---------- 保存资料 ----------
async function saveProfile() {
  if (!token.value) return
  // 轻量前端校验，与后端规则呼应
  const f = form.value
  if (f.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) { flash('邮箱格式好像不对呢'); return }
  if (f.phone && !/^1[3-9]\d{9}$/.test(f.phone)) { flash('手机号需为 11 位大陆号码'); return }
  saving.value = true
  try {
    const body = {
      nickname: f.nickname,
      email: f.email,
      phone: f.phone,
      gender: f.gender,
      bio: f.bio
    }
    const res = await fetch(API_BASE + '/api/profile', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token.value },
      body: JSON.stringify(body)
    })
    const json = await res.json()
    if (json.code === 200) {
      profile.value = { ...profile.value, ...body }
      flash('资料已更新 ♥')
    } else if (res.status === 401) {
      notice.value = '登录已失效，请重新缔结婚约后再来编辑资料。'
    } else {
      flash(json.message || '保存失败')
    }
  } catch (e) {
    flash('通讯失败，请稍后再试')
  } finally {
    saving.value = false
  }
}

// ---------- 头像上传 ----------
function triggerAvatar() {
  avatarInput.value && avatarInput.value.click()
}
async function onAvatarChange(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!token.value) return
  const fd = new FormData()
  fd.append('file', file)
  uploading.value = true
  try {
    const res = await fetch(API_BASE + '/api/profile/avatar', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token.value },
      body: fd
    })
    const json = await res.json()
    if (json.code === 200 && json.data) {
      profile.value = { ...profile.value, avatar: json.data }
      flash('头像已更新 ♥')
    } else if (res.status === 401) {
      notice.value = '登录已失效，请重新缔结婚约后再来编辑资料。'
    } else {
      flash(json.message || '上传失败')
    }
  } catch (e) {
    flash('上传失败，请稍后再试')
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

// ---------- 修改密码 ----------
function togglePwd() {
  showPwd.value = !showPwd.value
}
async function changePwd() {
  if (!token.value) return
  if (!pwd.value.old || !pwd.value.new) {
    flash('请先填写原密码与新密码')
    return
  }
  if (pwd.value.new.length < 6) {
    flash('新密码至少 6 位')
    return
  }
  pwdLoading.value = true
  try {
    const res = await fetch(API_BASE + '/api/profile/password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token.value },
      body: JSON.stringify({ oldPassword: pwd.value.old, newPassword: pwd.value.new })
    })
    const json = await res.json()
    if (json.code === 200) {
      // 后端在改密后会自增令牌版本并返回新 token，需同步更新本地登录态
      if (json.data) emit('tokenRefreshed', json.data)
      pwd.value = { old: '', new: '' }
      showPwd.value = false
      flash('结契暗语已更改，已为你续期登录态 ♥')
    } else if (res.status === 401) {
      notice.value = '登录已失效，请重新缔结婚约后再来编辑资料。'
    } else {
      flash(json.message || '修改失败')
    }
  } catch (e) {
    flash('通讯失败，请稍后再试')
  } finally {
    pwdLoading.value = false
  }
}

// 未登录 / 登录失效 → 回登录页，登录成功后会自动回到本页（App.vue 处理）
function goLogin() {
  emit('needLogin')
}

onMounted(loadProfile)
</script>

<template>
  <div class="pc">
    <!-- ============ 顶部操作 ============ -->
    <div class="top-row">
      <button class="back-btn" type="button" @click="emit('back')">← 回到首页</button>
      <button class="back-btn danger" type="button" @click="emit('logout')">退 出</button>
    </div>

    <p v-if="toast" class="toast">{{ toast }}</p>

    <!-- ============ 未登录 / 失效提示卡 ============ -->
    <main v-if="!token || notice" class="card empty-card">
      <div class="empty-ico">🕯️</div>
      <p class="empty-msg">{{ notice || '还未缔结婚约（未登录），无法编辑资料。' }}</p>
      <button v-if="!token || notice.includes('失效') || notice.includes('未登录') || notice.includes('缔结婚约')" class="btn" type="button" @click="goLogin">
        去缔结婚约（登录）
      </button>
    </main>

    <!-- ============ 个人资料编辑卡 ============ -->
    <main v-else class="card">
      <div class="crest">💞</div>
      <h1>个人资料编辑</h1>
      <p class="sub">月 夜 命 簿 · 请 君 落 笔</p>

      <div v-if="loading" class="loading">正在翻开你的命簿…</div>

      <template v-if="profile && !loading">
        <!-- 头像 -->
        <div class="avatar-box">
          <div class="avatar" :class="{ uploading }" @click="triggerAvatar" title="点击更换头像">
            <img v-if="avatarUrl" :src="avatarUrl" alt="头像" />
            <span v-else class="avatar-ph">🌹</span>
            <div v-if="uploading" class="avatar-mask">上传中…</div>
          </div>
          <button class="avatar-btn" type="button" @click="triggerAvatar" :disabled="uploading">
            {{ uploading ? '上传中…' : '更换头像' }}
          </button>
          <input ref="avatarInput" type="file" accept="image/*" hidden @change="onAvatarChange" />
        </div>

        <!-- 身份行 -->
        <p class="identity">
          <b>{{ profile.nickname || profile.username || '夫君' }}</b>
          <span class="uid">登录名 {{ profile.username }} · ID {{ profile.id }}</span>
        </p>

        <!-- 编辑表单（进入即编辑） -->
        <form @submit.prevent="saveProfile">
          <div class="field">
            <label for="f-nick">昵称</label>
            <div class="input-wrap">
              <span class="ico">🌸</span>
              <input id="f-nick" v-model="form.nickname" type="text" maxlength="30" placeholder="想被怎样称呼？" />
            </div>
          </div>

          <div class="grid2">
            <div class="field">
              <label for="f-email">邮箱</label>
              <div class="input-wrap">
                <span class="ico">📧</span>
                <input id="f-email" v-model="form.email" type="email" maxlength="100" placeholder="name@example.com" />
              </div>
            </div>
            <div class="field">
              <label for="f-phone">手机</label>
              <div class="input-wrap">
                <span class="ico">📱</span>
                <input id="f-phone" v-model="form.phone" type="tel" maxlength="11" placeholder="11 位手机号" />
              </div>
            </div>
          </div>

          <div class="field">
            <label for="f-gender">性别</label>
            <div class="input-wrap">
              <span class="ico">⚖️</span>
              <select id="f-gender" v-model.number="form.gender" class="sel">
                <option :value="0">未知</option>
                <option :value="1">男</option>
                <option :value="2">女</option>
              </select>
            </div>
          </div>

          <div class="field">
            <label for="f-bio">个性签名</label>
            <textarea id="f-bio" v-model="form.bio" maxlength="200" rows="3" placeholder="写下你与花娘的誓言（≤200字）"></textarea>
          </div>

          <button class="btn" type="submit" :disabled="saving">
            {{ saving ? '正在落笔…' : '保 存 资 料' }}
          </button>
        </form>

        <!-- 修改密码（折叠区） -->
        <div class="divider">🔑 结 契 暗 语</div>
        <button v-if="!showPwd" class="avatar-btn" type="button" @click="togglePwd">修改密码</button>
        <form v-else class="pwd-form" @submit.prevent="changePwd">
          <div class="grid2">
            <div class="field">
              <label for="p-old">原密码</label>
              <input id="p-old" v-model="pwd.old" type="password" autocomplete="current-password" placeholder="原来的暗语" />
            </div>
            <div class="field">
              <label for="p-new">新密码</label>
              <input id="p-new" v-model="pwd.new" type="password" autocomplete="new-password" placeholder="至少 6 位" />
            </div>
          </div>
          <div class="pwd-actions">
            <button class="avatar-btn" type="button" @click="togglePwd">取 消</button>
            <button class="btn small" type="submit" :disabled="pwdLoading">
              {{ pwdLoading ? '修改中…' : '确认修改' }}
            </button>
          </div>
        </form>
      </template>
    </main>
  </div>
</template>

<style scoped>
/* ---------- 页面容器（垂直水平居中，登录卡同款布局） ---------- */
.pc{
  position:relative; z-index:5;
  width:100%; min-height:100vh;
  display:flex; flex-direction:column; align-items:center; justify-content:center;
  gap:14px; padding:24px 0 40px;
}
.top-row{
  position:absolute; top:18px; left:0; right:0;
  display:flex; justify-content:space-between;
  padding:0 clamp(14px,4vw,40px);
}
.back-btn{
  border:1px solid rgba(232,74,120,.45); background:rgba(255,253,254,.8); color:var(--pink-deep);
  font-size:12.5px; letter-spacing:1px; padding:7px 16px; border-radius:999px; cursor:pointer; transition:.2s;
}
.back-btn:hover{ color:#fff; background:var(--pink); border-color:var(--pink); }
.back-btn.danger:hover{ background:#C93363; border-color:#C93363; }

/* ---------- 卡片（与登录卡同风格：浅粉描金） ---------- */
.card{
  position:relative; z-index:5;
  width:min(520px,94vw);
  padding:22px 30px 30px;
  background:linear-gradient(180deg,#FFFDFE 0%, #FDF3F8 100%);
  border:2px solid var(--gold);
  border-radius:26px;
  box-shadow:var(--shadow-3), 0 0 0 6px rgba(255,179,200,.18), inset 0 0 0 1px rgba(244,213,139,.28), inset 0 0 40px rgba(232,74,120,.22);
  text-align:center;
  animation:cardIn .8s cubic-bezier(.22,1,.36,1) both;
  max-height:calc(100vh - 120px); overflow-y:auto;
}
@keyframes cardIn{ from{opacity:0; transform:translateY(28px) scale(.96);} to{opacity:1; transform:none;} }
.card::before{
  content:"💕"; position:absolute; top:-14px; left:50%; transform:translateX(-50%);
  font-size:24px; filter:drop-shadow(0 2px 4px rgba(232,74,120,.4));
}
.card::after{
  content:""; position:absolute; inset:9px; border:1px dashed rgba(244,213,139,.45);
  border-radius:18px; pointer-events:none;
}

.crest{ font-size:40px; filter:drop-shadow(0 6px 18px rgba(232,74,120,.4)); }
h1{
  margin:8px 0 4px; font-size:24px; letter-spacing:3px;
  font-family:Georgia,"Songti SC","STSong","SimSun",serif; color:var(--ink);
  text-shadow:0 0 18px rgba(255,179,200,.5);
}
.sub{ margin:0 0 18px; font-size:12px; letter-spacing:4px; color:var(--pink-deep); font-weight:600; }
.loading{ color:var(--pink-deep); font-size:13px; letter-spacing:2px; padding:26px 0; }

/* ---------- 未登录 / 失效提示卡 ---------- */
.empty-card{ width:min(400px,94vw); }
.empty-ico{ font-size:44px; }
.empty-msg{ margin:10px 0 18px; font-size:13.5px; color:var(--ink-soft); line-height:1.7; }

/* ---------- 头像 ---------- */
.avatar-box{ display:flex; flex-direction:column; align-items:center; gap:10px; margin-bottom:12px; }
.avatar{
  position:relative; width:110px; height:110px; border-radius:50%; overflow:hidden; cursor:pointer;
  border:2px solid var(--gold);
  box-shadow:0 0 0 5px rgba(255,179,200,.2), 0 10px 24px rgba(232,74,120,.3);
  background:linear-gradient(180deg,#FFFDFE,#FCE6EF);
  display:grid; place-items:center; transition:.25s var(--ease-out);
}
.avatar:hover{ transform:translateY(-2px); box-shadow:0 0 0 5px rgba(255,179,200,.32), 0 14px 28px rgba(232,74,120,.45); }
.avatar.uploading{ cursor:wait; }
.avatar img{ width:100%; height:100%; object-fit:cover; }
.avatar-ph{ font-size:42px; }
.avatar-mask{
  position:absolute; inset:0; display:grid; place-items:center; font-size:12px; color:#fff;
  background:rgba(201,51,99,.6);
}
.avatar-btn{
  border:1px solid rgba(232,74,120,.45); background:#FFFDFE; color:var(--pink-deep);
  font-size:12px; letter-spacing:1px; padding:7px 18px; border-radius:999px; cursor:pointer; transition:.2s;
}
.avatar-btn:hover:not(:disabled){ color:#fff; background:var(--pink); border-color:var(--pink); }
.avatar-btn:disabled{ opacity:.6; cursor:default; }

/* ---------- 身份行 ---------- */
.identity{ margin:2px 0 16px; display:flex; flex-direction:column; gap:3px; }
.identity b{ font-size:17px; color:var(--ink); }
.uid{ font-size:11.5px; color:var(--ink-soft); opacity:.8; }

/* ---------- 表单（登录卡同款输入框） ---------- */
form{ text-align:left; }
.field{ margin-bottom:14px; }
.field label{
  display:block; margin:0 0 6px 4px; font-size:12.5px; font-weight:600;
  color:var(--ink-soft); letter-spacing:1px;
}
.input-wrap{ position:relative; }
.input-wrap .ico{ position:absolute; left:14px; top:50%; transform:translateY(-50%); font-size:14px; opacity:.55; pointer-events:none; }
.grid2{ display:grid; grid-template-columns:1fr 1fr; gap:12px; }
input, select, textarea{
  width:100%; padding:12px 14px; font-size:14px; color:var(--ink);
  background:#FFFDFE; border:2px solid var(--line); border-radius:14px; outline:none;
  transition:border-color .2s, box-shadow .2s; font-family:inherit;
}
.input-wrap input, .input-wrap select{ padding-left:40px; }
input::placeholder, textarea::placeholder{ color:#C9B3C2; }
input:hover, select:hover, textarea:hover{ border-color:#F3C9DC; }
input:focus, select:focus, textarea:focus{ border-color:var(--pink); box-shadow:var(--focus-ring); }
select.sel{ appearance:auto; cursor:pointer; }
textarea{ resize:vertical; line-height:1.6; }

/* ---------- 主按钮（登录卡同款） ---------- */
.btn{
  position:relative; overflow:hidden; width:100%; padding:14px; margin-top:4px; border:none; border-radius:16px;
  font-family:Georgia,"Songti SC",serif; font-size:15px; font-weight:700; letter-spacing:4px; color:#fff;
  background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 6px 0 #C93363, 0 14px 24px rgba(232,74,120,.45), inset 0 1px 0 rgba(255,255,255,.3);
  cursor:pointer; transition:transform .14s, box-shadow .14s, filter .2s;
}
.btn::after{
  content:""; position:absolute; top:0; left:-60%; width:50%; height:100%;
  background:var(--sheen); transform:skewX(-20deg); opacity:0; transition:left .6s ease, opacity .3s; pointer-events:none;
}
.btn:hover:not(:disabled){ filter:brightness(1.05); transform:translateY(-2px); }
.btn:hover:not(:disabled)::after{ left:120%; opacity:.55; }
.btn:active:not(:disabled){ transform:translateY(4px); box-shadow:0 2px 0 #C93363, 0 6px 12px rgba(232,74,120,.35); }
.btn:disabled{ cursor:default; filter:grayscale(.15); }
.btn.small{ width:auto; padding:11px 26px; font-size:13.5px; letter-spacing:2px; }

/* ---------- 分隔线与密码区 ---------- */
.divider{ display:flex; align-items:center; gap:12px; margin:22px 0 14px; color:#D8BBD0; font-size:11px; letter-spacing:2px; }
.divider::before, .divider::after{ content:""; flex:1; height:1px; background:var(--line); }
.pwd-form .btn{ margin-top:2px; }
.pwd-actions{ display:flex; justify-content:flex-end; gap:10px; align-items:center; }

/* ---------- 轻提示 ---------- */
.toast{
  position:fixed; top:64px; left:50%; transform:translateX(-50%); z-index:50;
  padding:10px 20px; border-radius:999px; font-size:13px; letter-spacing:1px;
  color:#fff; background:linear-gradient(180deg,#FF8FB4,#E84A78);
  box-shadow:0 6px 16px rgba(232,74,120,.45); animation:toastIn .3s var(--ease-out);
}
@keyframes toastIn{ from{ opacity:0; transform:translate(-50%,-6px); } to{ opacity:1; transform:translateX(-50%); } }

@media (max-width:460px){
  .card{ padding:18px 18px 24px; border-radius:22px; }
  .grid2{ grid-template-columns:1fr; }
}
</style>
