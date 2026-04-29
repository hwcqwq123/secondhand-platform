<template>
  <header class="topbar">
    <div class="brand" @click="$emit('goHome')">校园二手交易平台</div>
    <div class="search-box">
      <input
          :value="modelValue"
          placeholder="搜索商品，如：耳机、教材、自行车"
          @input="$emit('update:modelValue', $event.target.value)"
          @keyup.enter="$emit('search')"
      />
      <button class="btn primary" @click="$emit('search')">搜索</button>
    </div>
    <nav class="nav-actions">
      <button class="btn" @click="$emit('goHome')">首页</button>
      <button class="btn" @click="$emit('goPost')">发布商品</button>
      <button class="btn" @click="$emit('goMyItems')">我的发布</button>
      <button class="btn" @click="$emit('goOrders')">我的订单</button>
      <button class="btn" @click="$emit('goChats')">我的消息</button>
      <button class="btn" @click="$emit('goProfile')">个人中心</button>
      <button v-if="!token" class="btn primary" @click="$emit('openAuth')">登录/注册</button>
      <button v-else class="btn danger" @click="$emit('logout')">退出</button>
    </nav>
  </header>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  token: { type: String, default: '' },
  userInfo: { type: Object, default: () => ({}) }
})
defineEmits(['update:modelValue', 'search', 'openAuth', 'logout', 'goOrders', 'goProfile', 'goHome', 'goPost', 'goMyItems', 'goChats'])
</script>
