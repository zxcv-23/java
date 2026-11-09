// 后台管理统一请求层
// - 自动注入 Authorization: Bearer <后台 JWT>
// - 统一解析后端 Result<T>，失败抛带可读 message 的 Error
// - 401 时清除本地管理员会话并广播事件，App.vue 据此退回后台登录页
const API_BASE = 'http://192.168.68.22:8081'
const ADMIN_KEY = 'guiniang_admin'

export const ADMIN_UNAUTHORIZED_EVENT = 'admin:unauthorized'

/** 读取本地管理员会话（localStorage 优先，兼容旧的 sessionStorage 演示态） */
export function readAdmin() {
  try {
    const raw =
      window.localStorage.getItem(ADMIN_KEY) ||
      window.sessionStorage.getItem(ADMIN_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

/** 登录成功后写入持久化会话 */
export function saveAdmin(admin) {
  try {
    window.localStorage.removeItem(ADMIN_KEY)
    window.sessionStorage.removeItem(ADMIN_KEY)
    window.localStorage.setItem(ADMIN_KEY, JSON.stringify(admin))
  } catch {
    /* 隐私模式下存储不可用，忽略 */
  }
}

/** 沿用当前存储位置更新会话（改密后替换 token 用） */
export function persistAdmin(admin) {
  try {
    const inLocal = window.localStorage.getItem(ADMIN_KEY) !== null
    window.localStorage.removeItem(ADMIN_KEY)
    window.sessionStorage.removeItem(ADMIN_KEY)
    const target = inLocal ? window.localStorage : window.sessionStorage
    target.setItem(ADMIN_KEY, JSON.stringify(admin))
  } catch {
    /* 忽略持久化异常 */
  }
}

export function clearAdmin() {
  try {
    window.localStorage.removeItem(ADMIN_KEY)
    window.sessionStorage.removeItem(ADMIN_KEY)
  } catch {
    /* 忽略 */
  }
}

function notifyUnauthorized() {
  clearAdmin()
  try {
    window.dispatchEvent(new Event(ADMIN_UNAUTHORIZED_EVENT))
  } catch {
    /* 老浏览器可用 CustomEvent 兜底 */
    try {
      window.dispatchEvent(new CustomEvent(ADMIN_UNAUTHORIZED_EVENT))
    } catch { /* 忽略 */ }
  }
}

async function parseJson(res) {
  try {
    return await res.json()
  } catch {
    throw new Error('接口返回格式异常（HTTP ' + res.status + '）')
  }
}

/** 后台登录（公开接口，不带 token） */
export async function adminLogin(account, password) {
  let res
  try {
    res = await fetch(API_BASE + '/api/admin/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ account, password })
    })
  } catch {
    throw new Error('冥府信道未通 · 后端未启动，无法核验名讳')
  }

  if (res.status === 404) {
    throw new Error('后台登录接口（/api/admin/login）尚未就绪，请确认后端已启动')
  }
  const json = await parseJson(res)
  if (!res.ok || !json || json.code !== 200 || !json.data) {
    throw new Error((json && json.message) || '名讳或口令有误')
  }
  return json.data
}

/**
 * 受保护的后台请求
 * @param {string} path 以 /api 开头的路径
 * @param {object} options 原生 fetch 选项（body 为对象时会自动 JSON 序列化）
 * @returns {Promise<any>} Result.data
 */
export async function adminFetch(path, options = {}) {
  const admin = readAdmin()
  const token = admin && admin.token

  const opts = Object.assign({}, options)
  const headers = Object.assign({}, options.headers || {})
  let body = options.body
  if (body !== undefined && body !== null && typeof body === 'object' && !(body instanceof FormData)) {
    body = JSON.stringify(body)
    headers['Content-Type'] = headers['Content-Type'] || 'application/json'
  }
  if (body !== undefined) {
    headers['Content-Type'] = headers['Content-Type'] || 'application/json'
  }
  if (token) headers['Authorization'] = 'Bearer ' + token
  opts.headers = headers
  if (body !== undefined) opts.body = body

  let res
  try {
    res = await fetch(API_BASE + path, opts)
  } catch {
    throw new Error('冥府信道未通 · 后端未启动或网络异常')
  }

  if (res.status === 401) {
    notifyUnauthorized()
    let message = '登录已过期，请重新登录'
    try {
      const json = await res.json()
      if (json && json.message) message = json.message
    } catch { /* 保留默认提示 */ }
    throw new Error(message)
  }

  const json = await parseJson(res)
  if (!res.ok || !json || (json.code !== undefined && json.code !== 200)) {
    throw new Error((json && json.message) || ('请求失败（HTTP ' + res.status + '）'))
  }
  return json.data
}
