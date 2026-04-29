<template>
  <div class="card">
    <div class="sidebar-section">
      <div class="sidebar-title">快速筛选</div>
      <button class="board-btn" @click="$emit('filter', '')">全部商品</button>
      <button class="board-btn" @click="$emit('filter', 'AVAILABLE')">只看在售</button>
      <button class="board-btn" @click="$emit('filter', 'SOLD')">已售商品</button>
      <div class="tip-box">
        AI 客服会结合商品库进行搜索和推荐；聊天消息、商品发布都会先走内容审核。
      </div>
    </div>
  </div>
</template>

<script setup>
defineEmits(['filter'])
</script>
