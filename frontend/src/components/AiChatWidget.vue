<template>
  <!-- 右下角悬浮按钮 -->
  <div class="ai-fab" @click="toggle">
    AI
  </div>

  <!-- 聊天窗口 -->
  <div v-if="open" class="ai-panel">
    <div class="ai-header">
      <div>
        <div class="ai-title">AI 智能客服</div>
        <div class="ai-sub">可问：有什么二手耳机？预算 300 推荐</div>
      </div>
      <button class="ai-close" @click="toggle">×</button>
    </div>

    <div class="ai-body" ref="bodyRef">
      <div v-for="(m, idx) in messages" :key="idx" class="ai-msg" :class="m.role">
        <div class="bubble">
          <div v-if="m.type==='text'">{{ m.text }}</div>

          <div v-else-if="m.type==='items'">
            <div style="font-weight:700; margin-bottom:6px;">为你找到这些商品：</div>
            <div v-for="it in m.items" :key="it.id" class="ai-item">
              <div class="ai-item-title">{{ it.title }}</div>
              <div class="ai-item-meta">¥ {{ it.price }} · {{ mapStatus(it.status) }} · 卖家 {{ it.seller?.username || '-' }}</div>
              <button class="ai-btn" @click="$emit('openDetail', it.id)">查看</button>
            </div>
            <div v-if="m.items.length===0" style="color:#666;">暂无匹配商品，换个关键词试试。</div>
          </div>
        </div>
      </div>
    </div>

    <div class="ai-footer">
      <input
          v-model="input"
          class="ai-input"
          placeholder="输入需求：二手显示器 27寸 / 预算300 耳机..."
          @keydown.enter="send"
      />
      <button class="ai-send" @click="send" :disabled="sending || !input.trim()">发送</button>
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { listItems, getRecommendations } from '../api'

const emit = defineEmits(['openDetail'])

const open = ref(false)
const input = ref('')
const sending = ref(false)
const bodyRef = ref(null)

const messages = ref([
  { role: 'assistant', type: 'text', text: '你好，我是AI客服。你可以说“推荐二手耳机 300以内”或“有什么书籍”。' }
])

function toggle() {
  open.value = !open.value
  if (open.value) scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

function mapStatus(s) {
  if (s === 'AVAILABLE') return '在售'
  if (s === 'RESERVED') return '交易中'
  if (s === 'SOLD') return '已售'
  return s || '-'
}

function normalizeQuery(text) {
  // MVP：直接用整句；也可以做更复杂的关键词抽取
  return text.replace(/[^\u4e00-\u9fa5A-Za-z0-9\s]/g, ' ').trim()
}

function looksLikeRecommend(text) {
  const t = text.trim()
  return t.includes('推荐') || t.includes('随便看看') || t.includes('你觉得') || t.includes('适合')
}

async function send() {
  const q = input.value.trim()
  if (!q) return

  messages.value.push({ role: 'user', type: 'text', text: q })
  input.value = ''
  sending.value = true
  scrollToBottom()

  try {
    if (looksLikeRecommend(q)) {
      const res = await getRecommendations(6)
      if (!res.success) throw new Error(res.message || 'recommendation failed')
      messages.value.push({ role: 'assistant', type: 'items', items: res.data })
    } else {
      const query = normalizeQuery(q)
      const res = await listItems({ q: query })
      if (!res.success) throw new Error(res.message || 'search failed')

      // 只取前 6 条
      const top = (res.data || []).slice(0, 6)
      messages.value.push({ role: 'assistant', type: 'items', items: top })
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', type: 'text', text: `请求失败：${e.message}` })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.ai-fab{
  position:fixed;
  right:18px;
  bottom:18px;
  width:54px;
  height:54px;
  border-radius:999px;
  background:#1677ff;
  color:#fff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-weight:900;
  cursor:pointer;
  box-shadow:0 10px 26px rgba(0,0,0,.18);
  z-index:9999;
}
.ai-panel{
  position:fixed;
  right:18px;
  bottom:86px;
  width:360px;
  max-height:70vh;
  background:#fff;
  border:1px solid #e5e7eb;
  border-radius:14px;
  box-shadow:0 18px 50px rgba(0,0,0,.18);
  display:flex;
  flex-direction:column;
  overflow:hidden;
  z-index:9999;
}
.ai-header{
  padding:12px 12px;
  border-bottom:1px solid #e5e7eb;
  display:flex;
  align-items:center;
  justify-content:space-between;
}
.ai-title{font-weight:900}
.ai-sub{font-size:12px;color:#6b7280;margin-top:2px}
.ai-close{border:none;background:transparent;font-size:20px;cursor:pointer;color:#6b7280}
.ai-body{
  padding:12px;
  overflow:auto;
  background:#f7f8fa;
  flex:1;
}
.ai-msg{display:flex;margin-bottom:10px}
.ai-msg.user{justify-content:flex-end}
.ai-msg.assistant{justify-content:flex-start}
.bubble{
  max-width:85%;
  background:#fff;
  border:1px solid #e5e7eb;
  border-radius:12px;
  padding:10px 10px;
  font-size:13px;
  line-height:1.35;
}
.ai-msg.user .bubble{
  background:#1677ff;
  border-color:#1677ff;
  color:#fff;
}
.ai-item{
  border:1px solid #e5e7eb;
  border-radius:10px;
  padding:8px;
  margin-top:8px;
  background:#fff;
}
.ai-item-title{font-weight:800}
.ai-item-meta{font-size:12px;color:#6b7280;margin-top:4px}
.ai-btn{
  margin-top:8px;
  height:30px;
  padding:0 10px;
  border-radius:8px;
  border:1px solid #e5e7eb;
  background:#fff;
  cursor:pointer;
}
.ai-footer{
  padding:10px;
  border-top:1px solid #e5e7eb;
  display:flex;
  gap:8px;
}
.ai-input{
  flex:1;
  height:34px;
  border:1px solid #e5e7eb;
  border-radius:10px;
  padding:0 10px;
  outline:none;
}
.ai-send{
  height:34px;
  padding:0 12px;
  border-radius:10px;
  border:1px solid #1677ff;
  background:#1677ff;
  color:#fff;
  cursor:pointer;
}
.ai-send:disabled{opacity:.6; cursor:not-allowed}
</style>