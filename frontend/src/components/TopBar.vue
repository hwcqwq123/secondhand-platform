<template>
  <div class="topbar">
    <div class="container topbar-inner">
      <div class="logo" @click="$emit('goHome')">校园二手论坛</div>

      <div class="search">
        <input
            v-model="q"
            placeholder="搜索：耳机 / 书籍 / 显示器..."
            @keydown.enter="emitSearch"
        />
        <button class="btn" @click="emitSearch">搜索</button>
      </div>

      <div class="user-actions">
        <template v-if="!token">
          <button class="btn primary" @click="$emit('openAuth')">登录 / 注册</button>
        </template>

        <template v-else>
          <button class="btn primary" @click="$emit('goPost')">发帖</button>

          <div class="user-mini" @click="$emit('goProfile')">
            <img
                v-if="userInfo?.avatarUrl"
                :src="userInfo.avatarUrl"
                alt="avatar"
                class="avatar"
            />
            <div v-else class="avatar avatar-fallback">
              {{ displayName.slice(0, 1) || '用' }}
            </div>

            <span class="user-name">{{ displayName }}</span>
          </div>
          <button class="btn" @click="$emit('goMyItems')">我的发布</button>
          <button class="btn" @click="$emit('goOrders')">我的订单</button>
          <button class="btn" @click="$emit('logout')">退出</button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import '../styles/topbar.css'

const props = defineProps({
  modelValue: String,
  token: String,
  userInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits([
  'update:modelValue',
  'search',
  'openAuth',
  'logout',
  'goOrders',
  'goProfile',
  'goHome',
  'goPost',
  'goMyItems'
])

const q = ref(props.modelValue || '')

watch(() => props.modelValue, (v) => {
  q.value = v || ''
})

const displayName = computed(() => {
  return props.userInfo?.nickname || props.userInfo?.username || '用户'
})

function emitSearch() {
  emit('update:modelValue', q.value)
  emit('search', q.value)
}
</script>