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

app.mount('#app')
