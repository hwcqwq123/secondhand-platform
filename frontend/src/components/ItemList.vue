<template>
  <div class="card">
    <div class="list-head">
      <div class="title">{{ title }}</div>
      <span class="muted">共 {{ total }} 件</span>
    </div>
    <div v-if="error" class="msg">{{ error }}</div>
    <div v-if="items.length === 0" class="empty">暂无商品</div>
    <div class="item-grid">
      <div v-for="item in items" :key="item.id" class="item-card">
        <div class="thumb" @click="$emit('open', item.id)">
          <img v-if="item.coverImageUrl" :src="item.coverImageUrl" alt="商品封面" />
          <span v-else>图片</span>
        </div>
        <div class="item-title" @click="$emit('open', item.id)">{{ item.title }}</div>
        <div class="price">¥ {{ item.price }}</div>
        <div class="muted">{{ boardName(item.board) }} ｜ {{ item.status }}</div>
        <div class="muted">卖家：{{ item.seller?.nickname || item.seller?.username || '-' }}</div>
        <div class="panel-actions compact">
          <button class="btn" @click="$emit('open', item.id)">详情</button>
          <button class="btn primary" :disabled="item.status !== 'AVAILABLE'" @click="$emit('buy', item.id)">下单</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] },
  title: { type: String, default: '商品列表' },
  total: { type: Number, default: 0 },
  error: { type: String, default: '' }
})
defineEmits(['open', 'buy'])
function boardName(board) {
  return { digital: '数码', books: '书籍', daily: '日用', sports: '运动' }[board] || '其他'
}
</script>
