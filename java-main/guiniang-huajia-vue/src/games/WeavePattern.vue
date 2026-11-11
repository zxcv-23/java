<script setup>
defineProps({ type: { type: String, default: 'tuan' } })

/* 传统纹样库：300 × 300 画布，全部为描金线稿 + 半透明填色 */
const petals = [0, 45, 90, 135, 180, 225, 270, 315]
const quarters = [22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5]

/* 小团花：先在脚本里算好五瓣坐标，避免模板里直接调用 Math */
const FLOWER_AT = [[64, 116], [168, 122], [232, 60], [116, 196], [214, 214], [66, 268], [268, 210]]
const flowers = FLOWER_AT.map(([x, y]) => ({
  x, y,
  pts: [0, 1, 2, 3, 4].map(k => ({
    x: +(Math.cos(k * 72 * Math.PI / 180) * 7.5).toFixed(2),
    y: +(Math.sin(k * 72 * Math.PI / 180) * 7.5).toFixed(2)
  }))
}))
</script>

<template>
  <g v-if="type === 'tuan'">
    <g stroke="#F3DBA8" fill="none" stroke-width="2.2" opacity=".92">
      <circle cx="150" cy="150" r="54" />
      <circle cx="150" cy="150" r="74" stroke-dasharray="5 9" opacity=".7" />
      <circle cx="150" cy="150" r="30" opacity=".8" />
      <circle cx="150" cy="150" r="13" fill="#F3DBA8" fill-opacity=".8" stroke="none" />
    </g>
    <g v-for="a in petals" :key="'p' + a" :transform="`rotate(${a} 150 150)`">
      <path d="M150 96 C171 116,171 140,150 152 C129 140,129 116,150 96Z"
            fill="#F3DBA8" fill-opacity=".26" stroke="#F3DBA8" stroke-width="1.8" opacity=".9" />
      <circle cx="150" cy="118" r="2.6" fill="#FFE9B8" opacity=".9" />
    </g>
    <g v-for="a in quarters" :key="'q' + a" :transform="`rotate(${a} 150 150)`">
      <path d="M150 62 q9 12 0 22 q-9 -10 0 -22Z" fill="#E9C173" fill-opacity=".55" />
    </g>
    <g stroke="#E9C173" fill="none" stroke-width="1.6" opacity=".65">
      <path d="M22 22 q26 6 34 34 q-26 -6 -34 -34Z" fill="#E9C173" fill-opacity=".22" />
      <path d="M278 22 q-26 6 -34 34 q26 -6 34 -34Z" fill="#E9C173" fill-opacity=".22" />
      <path d="M22 278 q26 -6 34 -34 q-26 6 -34 34Z" fill="#E9C173" fill-opacity=".22" />
      <path d="M278 278 q-26 -6 -34 -34 q26 6 34 34Z" fill="#E9C173" fill-opacity=".22" />
    </g>
  </g>

  <g v-else-if="type === 'chanzhi'">
    <g stroke="#F3DBA8" fill="none" stroke-width="2.4" opacity=".9" stroke-linecap="round">
      <path d="M-10 74 C 54 40, 84 110, 138 84 C 186 60, 196 132, 250 106 C 286 88, 300 110, 316 98" />
      <path d="M-10 176 C 46 206, 92 142, 144 168 C 196 194, 214 132, 264 154 C 292 166, 306 152, 316 158" />
      <path d="M-10 258 C 50 236, 96 292, 150 268 C 204 244, 232 292, 284 274" />
      <path d="M60 300 C 56 232, 110 214, 116 158 C 122 108, 176 96, 186 46" opacity=".55" />
      <path d="M212 300 C 216 246, 168 220, 176 174 C 184 128, 246 120, 250 62" opacity=".55" />
    </g>
    <g fill="#F3DBA8" fill-opacity=".3" stroke="#F3DBA8" stroke-width="1.6">
      <ellipse cx="74" cy="52" rx="15" ry="7" transform="rotate(-32 74 52)" />
      <ellipse cx="94" cy="92" rx="14" ry="6.5" transform="rotate(38 94 92)" />
      <ellipse cx="150" cy="72" rx="15" ry="7" transform="rotate(-26 150 72)" />
      <ellipse cx="212" cy="98" rx="14" ry="6.5" transform="rotate(30 212 98)" />
      <ellipse cx="52" cy="196" rx="15" ry="7" transform="rotate(-24 52 196)" />
      <ellipse cx="126" cy="150" rx="14" ry="6.5" transform="rotate(34 126 150)" />
      <ellipse cx="196" cy="176" rx="15" ry="7" transform="rotate(-30 196 176)" />
      <ellipse cx="262" cy="152" rx="13" ry="6" transform="rotate(26 262 152)" />
      <ellipse cx="96" cy="272" rx="14" ry="6.5" transform="rotate(-20 96 272)" />
      <ellipse cx="238" cy="266" rx="13" ry="6" transform="rotate(24 238 266)" />
    </g>
    <g v-for="(f, i) in flowers" :key="'f' + i" :transform="`translate(${f.x} ${f.y})`">
      <circle v-for="(p, k) in f.pts" :key="k" :cx="p.x" :cy="p.y" r="5.4"
              fill="#F3DBA8" fill-opacity=".5" stroke="#FFE9B8" stroke-width="1" />
      <circle cx="0" cy="0" r="2.6" fill="#FFE9B8" />
    </g>
  </g>

  <g v-else>
    <g stroke="#F3DBA8" fill="#E9C173" fill-opacity=".18" stroke-width="2" opacity=".92"
       stroke-linejoin="round">
      <path d="M60 108 q0 -30 28 -30 q22 0 26 20 q4 -20 26 -20 q28 0 28 30 q0 26 -28 26 h-52 q-28 0 -28 -26Z" />
      <path d="M182 108 q0 -30 28 -30 q22 0 26 20 q4 -20 26 -20 q28 0 28 30 q0 26 -28 26 h-52 q-28 0 -28 -26Z" />
      <path d="M20 210 q0 -30 28 -30 q22 0 26 20 q4 -20 26 -20 q28 0 28 30 q0 26 -28 26 h-52 q-28 0 -28 -26Z" />
      <path d="M142 210 q0 -30 28 -30 q22 0 26 20 q4 -20 26 -20 q28 0 28 30 q0 26 -28 26 h-52 q-28 0 -28 -26Z" />
      <path d="M84 268 q0 -26 24 -26 q18 0 22 17 q4 -17 22 -17 q24 0 24 26 q0 22 -24 22 h-44 q-24 0 -24 -22Z" opacity=".8" />
    </g>
    <g stroke="#F3DBA8" fill="none" stroke-width="1.6" opacity=".72" stroke-linecap="round">
      <path d="M74 96 q14 -14 28 0 M104 96 q14 -14 28 0" />
      <path d="M196 96 q14 -14 28 0 M226 96 q14 -14 28 0" />
      <path d="M34 198 q14 -14 28 0 M64 198 q14 -14 28 0" />
      <path d="M156 198 q14 -14 28 0 M186 198 q14 -14 28 0" />
      <path d="M-6 158 C 40 148, 60 176, 104 164 C 148 152, 170 180, 214 168 C 254 158, 280 176, 310 166"
            opacity=".6" />
      <path d="M-6 236 C 40 226, 62 252, 106 240 C 150 228, 172 254, 216 242 C 256 232, 282 250, 310 240"
            opacity=".5" />
    </g>
    <g fill="#FFE9B8" opacity=".85">
      <circle cx="150" cy="52" r="3.4" /><circle cx="256" cy="152" r="3" />
      <circle cx="46" cy="152" r="3" /><circle cx="150" cy="296" r="3" />
    </g>
  </g>
</template>
