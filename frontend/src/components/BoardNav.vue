<template>
  <div class="card board-nav-card">
    <div class="sidebar-section">
      <div class="sidebar-title">板块分类</div>

      <div class="board-nav-list">
        <button
            v-for="b in boards"
            :key="b.key"
            class="board-nav-item"
            :class="{ active: b.key === active }"
            @click="$emit('select', b.key)"
        >
          <span class="board-nav-dot"></span>
          <span>{{ b.name }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import '../styles/board-nav.css'


defineProps({
  boards: { type: Array, required: true },
  active: { type: String, required: true }
})

defineEmits(['select'])
</script>