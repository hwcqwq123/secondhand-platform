<template>
  <div class="ai-widget">
    <button v-if="!open" class="ai-float" @click="open = true">AI 客服</button>
    <div v-else class="ai-panel">
      <div class="ai-head">
        <div>
          <b>AI 客服</b>
          <small>大模型问答 + 商品推荐</small>
        </div>
        <button class="btn" @click="open = false">×</button>
      </div>

      <div class="ai-body">
        <div v-for="(m, idx) in messages" :key="idx" class="bubble" :class="m.role">
          {{ m.content }}
        </div>
        <div v-if="suggestions.length" class="suggest-box">
          <div class="suggest-title">推荐商品</div>
          <div v-for="item in suggestions" :key="item.id" class="suggest-item" @click="$emit('openDetail', item.id)">
            <img v-if="item.coverImageUrl" :src="item.coverImageUrl" alt="商品" />
            <div>
              <b>{{ item.title }}</b>
              <div class="price">¥ {{ item.price }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="ai-input">
        <input v-model="input" placeholder="问我：推荐耳机/怎么发布商品" @keyup.enter="send" />
        <button class="btn primary" :disabled="loading" @click="send">发送</button>
      </div>
      <div v-if="error" class="msg mini-msg">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { aiChat } from '../api'

defineEmits(['openDetail'])

const open = ref(false)
const input = ref('')
const loading = ref(false)
const error = ref('')
const suggestions = ref([])
const messages = ref([
  { role: 'assistant', content: '你好，我是 AI 客服。你可以问我商品推荐、发布流程、如何联系卖家等问题。' }
])

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  error.value = ''
  loading.value = true

  try {
    const history = messages.value.slice(-8).map(m => ({ role: m.role, content: m.content }))
    const res = await aiChat(text, history)
    if (!res.success) throw new Error(res.message || 'AI 客服调用失败')
    const data = res.data
    messages.value.push({ role: 'assistant', content: data.answer || '我暂时没有理解你的问题。' })
    suggestions.value = data.suggestions || []
  } catch (e) {
    error.value = e.message || 'AI 客服调用失败'
    messages.value.push({ role: 'assistant', content: 'AI 客服暂时不可用，你可以直接搜索商品关键词。' })
  } finally {
    loading.value = false
  }
}
</script>
