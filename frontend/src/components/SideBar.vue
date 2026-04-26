<template>
  <div class="sidebar-stack">
    <div class="card sidebar-card">
      <div class="sidebar-section">
        <div class="sidebar-title">公告</div>

        <div class="notice-list">
          <div class="notice-item">
            <span class="notice-label">交易提示</span>
            <span class="notice-value">面交优先</span>
          </div>
          <div class="notice-item">
            <span class="notice-label">防骗提醒</span>
            <span class="notice-value">不先转账</span>
          </div>
          <div class="notice-item">
            <span class="notice-label">客服支持</span>
            <span class="notice-value">AI 辅助</span>
          </div>
        </div>
      </div>
    </div>

    <div class="card sidebar-card">
      <div class="sidebar-section">
        <div class="sidebar-title">快捷筛选</div>

        <div class="sidebar-filter-list">
          <button class="btn sidebar-btn" @click="$emit('filter', 'AVAILABLE')">
            只看在售
          </button>
          <button class="btn sidebar-btn" @click="$emit('filter', 'SOLD')">
            只看已售
          </button>
          <button class="btn sidebar-btn" @click="$emit('filter', '')">
            查看全部
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import '../styles/sidebar.css'

defineEmits(['filter'])
</script>