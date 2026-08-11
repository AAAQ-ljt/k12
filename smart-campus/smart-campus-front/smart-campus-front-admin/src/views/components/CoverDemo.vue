<template>
  <section class="demo-card">
    <div class="demo-card__head">
      <h3>封面组件</h3>
      <p>适合商品、课程、专题活动等带封面内容的卡片展示。</p>
    </div>
    <div class="demo-card__body">
      <div class="cover-grid">
        <BaseCoverCard
          v-for="item in covers"
          :key="item.title"
          :image="item.image"
          :title="item.title"
          :description="item.description"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import BaseCoverCard from '@/components/BaseCoverCard.vue'

const createCover = (colorA, colorB, text) =>
  `data:image/svg+xml;utf8,${encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="640" height="360" viewBox="0 0 640 360">
      <defs>
        <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="${colorA}" />
          <stop offset="100%" stop-color="${colorB}" />
        </linearGradient>
      </defs>
      <rect width="640" height="360" rx="28" fill="url(#g)" />
      <circle cx="520" cy="82" r="56" fill="rgba(255,255,255,0.14)" />
      <circle cx="118" cy="292" r="72" fill="rgba(255,255,255,0.12)" />
      <text x="56" y="196" fill="#ffffff" font-size="42" font-family="Arial">${text}</text>
    </svg>
  `)}`

const covers = [
  {
    title: '课程专题封面',
    description: '适合内容管理后台展示课程专题。',
    image: createCover('#4a7cff', '#1dd1a1', 'Course Cover'),
  },
  {
    title: '商品推荐封面',
    description: '适合电商或资源管理中的商品视觉卡片。',
    image: createCover('#ff8f50', '#ff5e7a', 'Product Cover'),
  },
]
</script>

<style lang="scss" scoped>
.demo-card {
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(30, 49, 86, 0.05);
}

.demo-card__head {
  padding: 14px;
  border-bottom: 1px solid #eef2f8;

  h3 {
    margin: 0 0 6px;
    font-size: 15px;
    color: #23324f;
  }

  p {
    margin: 0;
    color: #677793;
    font-size: 13px;
  }
}

.demo-card__body {
  padding: 14px;
}

.cover-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 900px) {
  .cover-grid {
    grid-template-columns: 1fr;
  }
}
</style>
