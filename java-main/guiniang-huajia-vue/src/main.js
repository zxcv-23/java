import { createApp } from 'vue'
import './styles/base.css'
import App from './App.vue'

const app = createApp(App)

// 滚动揭示指令：元素进入视口时添加 .is-visible（配合 base.css 的 .reveal 动画）
app.directive('reveal', {
  mounted(el) {
    el.classList.add('reveal')
    if (!('IntersectionObserver' in window)) {
      el.classList.add('is-visible')
      return
    }
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) {
          el.classList.add('is-visible')
          io.unobserve(el)
        }
      })
    }, { threshold: 0.12 })
    io.observe(el)
    el.__revealIO = io
  },
  unmounted(el) {
    if (el.__revealIO) el.__revealIO.disconnect()
  }
})

// 3D 倾斜指令：鼠标跟随产生透视旋转 + 光泽追随（v-tilt 或 v-tilt="{ strength: 8 }"）
// 用法：加在外层包裹元素上（内部子元素自身的 transform 动画不受影响）
app.directive('tilt', {
  mounted(el, binding) {
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    const strength = (binding.value && binding.value.strength) || 6
    el.classList.add('tilt-host')

    // 光泽追随层（独立子元素，避免与宿主伪元素冲突）
    const sheen = document.createElement('span')
    sheen.className = 'tilt-sheen'
    const first = el.firstElementChild
    if (first) {
      const r = getComputedStyle(first).borderRadius
      if (r && r !== '0px') sheen.style.borderRadius = r
    }
    el.appendChild(sheen)

    let raf = 0
    const onMove = (e) => {
      const rect = el.getBoundingClientRect()
      if (!rect.width || !rect.height) return
      const px = (e.clientX - rect.left) / rect.width - 0.5
      const py = (e.clientY - rect.top) / rect.height - 0.5
      sheen.style.setProperty('--sx', ((px + 0.5) * 100).toFixed(1) + '%')
      sheen.style.setProperty('--sy', ((py + 0.5) * 100).toFixed(1) + '%')
      cancelAnimationFrame(raf)
      raf = requestAnimationFrame(() => {
        el.style.setProperty('--rx', (-py * strength).toFixed(2) + 'deg')
        el.style.setProperty('--ry', (px * strength).toFixed(2) + 'deg')
      })
    }
    const onLeave = () => {
      cancelAnimationFrame(raf)
      el.style.setProperty('--rx', '0deg')
      el.style.setProperty('--ry', '0deg')
    }
    el.__tiltCleanup = () => {
      cancelAnimationFrame(raf)
      el.removeEventListener('pointermove', onMove)
      el.removeEventListener('pointerleave', onLeave)
      sheen.remove()
    }
    el.addEventListener('pointermove', onMove)
    el.addEventListener('pointerleave', onLeave)
  },
  unmounted(el) {
    if (el.__tiltCleanup) el.__tiltCleanup()
  }
})

app.mount('#app')
