<template>
  <div class="card board-card">
    <div class="sidebar-title">商品板块</div>
    <button
        v-for="board in boards"
        :key="board.key"
        class="board-btn"
        :class="{ active: board.key === active }"
        @click="$emit('select', board.key)"
    >
      {{ board.name }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  boards: { type: Array, default: () => [] },
  active: { type: String, default: 'all' }
})
defineEmits(['select'])
</script>
