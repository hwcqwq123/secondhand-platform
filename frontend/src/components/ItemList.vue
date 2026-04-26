<template>
  <div class="card item-list-card">
    <div class="list-head">
      <div>
        <div class="title">{{ title }}</div>
        <div class="item-list-subtitle">为你展示当前筛选条件下的商品信息</div>
      </div>
      <div class="meta item-list-total">共 {{ total }} 条</div>
    </div>

    <div v-if="items.length > 0" class="item-list-body">
      <ItemRow
          v-for="it in items"
          :key="it.id"
          :item="it"
          @open="$emit('open', $event)"
          @buy="$emit('buy', $event)"
      />
    </div>

    <div v-else-if="!error" class="item-list-empty">
      <div class="item-list-empty-icon">📦</div>
      <div class="item-list-empty-title">暂无商品</div>
      <div class="item-list-empty-desc">当前筛选条件下还没有可展示的商品</div>
    </div>

    <div v-if="error" class="form">
      <div class="msg">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import '../styles/item-list.css'
import ItemRow from './ItemRow.vue'

defineProps({
  title: String,
  items: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  error: String
})

defineEmits(['buy', 'open'])
</script>