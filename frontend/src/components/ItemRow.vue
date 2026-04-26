<template>
  <div class="item-row">
    <div class="item-thumb">
      <div class="item-thumb-inner">
        <img
            v-if="item.coverImageUrl"
            :src="item.coverImageUrl"
            alt="商品图片"
            class="item-thumb-img"
        />
        <span v-else>图片</span>
      </div>
    </div>

    <div class="item-main">
      <div class="item-top">
        <div class="item-title" @click="$emit('open', item.id)">
          {{ item.title }}
        </div>

        <div class="item-price">¥ {{ item.price }}</div>
      </div>

      <div class="item-desc">
        {{ item.description || '（无描述）' }}
      </div>

      <div class="item-meta">
        <span
            class="badge"
            :class="item.status === 'SOLD'
            ? 'sold'
            : (item.status === 'RESERVED' ? 'reserved' : 'available')"
        >
          {{ item.status === 'SOLD' ? '已出' : (item.status === 'RESERVED' ? '交易中' : '在售') }}
        </span>

        <span class="meta-text">卖家：{{ item.seller?.nickname || item.seller?.username || '-' }}</span>
        <span class="meta-text">发布：{{ formatTime(item.createdAt) }}</span>
      </div>
    </div>

    <div class="item-actions">
      <button
          class="btn primary"
          @click="$emit('buy', item.id)"
          :disabled="item.status !== 'AVAILABLE'"
      >
        下单
      </button>

      <button class="btn" @click="$emit('open', item.id)">
        查看详情
      </button>
    </div>
  </div>
</template>

<script setup>
import '../styles/item-row.css'

defineProps({
  item: Object
})

defineEmits(['buy', 'open'])

function formatTime(v) {
  if (!v) return '-'
  try {
    return new Date(v).toLocaleString()
  } catch {
    return '-'
  }
}
</script>