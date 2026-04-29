<template>
  <div>
    <div class="page-title">我的消息</div>
    <div class="chat-layout card">
      <aside class="conversation-list">
        <div class="list-head slim">
          <div class="title">会话列表</div>
          <button class="btn" @click="loadConversations">刷新</button>
        </div>
        <div v-if="convError" class="msg">{{ convError }}</div>
        <div v-if="conversations.length === 0" class="empty">暂无会话，可以在商品详情页点击“联系卖家”。</div>
        <div
            v-for="c in conversations"
            :key="c.id"
            class="conversation-item"
            :class="{ active: activeConversationId === c.id }"
            @click="selectConversation(c.id)"
        >
          <img v-if="c.itemCoverImageUrl" :src="c.itemCoverImageUrl" alt="商品" />
          <div class="conversation-info">
            <b>{{ c.otherNickname || c.otherUsername }}</b>
            <span>{{ c.itemTitle }}</span>
            <small>{{ c.lastMessage || '暂无消息' }}</small>
          </div>
          <em v-if="c.unreadCount">{{ c.unreadCount }}</em>
        </div>
      </aside>

      <main class="message-panel">
        <div v-if="!activeConversation" class="empty big-empty">请选择一个会话</div>
        <template v-else>
          <div class="message-head">
            <div>
              <b>{{ activeConversation.otherNickname || activeConversation.otherUsername }}</b>
              <span>关于：{{ activeConversation.itemTitle }}</span>
            </div>
            <button class="btn" @click="$emit('openDetail', activeConversation.itemId)">查看商品</button>
          </div>

          <div class="message-list">
            <div v-if="msgError" class="msg">{{ msgError }}</div>
            <div
                v-for="m in messages"
                :key="m.id"
                class="message-row"
                :class="{ mine: m.mine }"
            >
              <div class="message-bubble">
                <div>{{ m.content }}</div>
                <small>{{ formatTime(m.createdAt) }}</small>
              </div>
            </div>
          </div>

          <div class="message-input">
            <input v-model="content" placeholder="输入消息，发送前会经过内容审核" @keyup.enter="send" />
            <button class="btn primary" :disabled="sending" @click="send">发送</button>
          </div>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { listChatConversations, listChatMessages, sendChatMessage, markChatRead } from '../api'

const props = defineProps({ initialConversationId: { type: Number, default: null } })
defineEmits(['openDetail'])

const conversations = ref([])
const messages = ref([])
const activeConversationId = ref(null)
const content = ref('')
const convError = ref('')
const msgError = ref('')
const sending = ref(false)
let timer = null

const activeConversation = computed(() => conversations.value.find(c => c.id === activeConversationId.value))

watch(() => props.initialConversationId, async (id) => {
  if (id) {
    activeConversationId.value = id
    await loadConversations()
    await loadMessages()
  }
}, { immediate: true })

async function loadConversations() {
  convError.value = ''
  try {
    const res = await listChatConversations()
    if (!res.success) throw new Error(res.message || '加载会话失败')
    conversations.value = res.data || []
    if (!activeConversationId.value && conversations.value.length) {
      activeConversationId.value = conversations.value[0].id
      await loadMessages()
    }
  } catch (e) {
    convError.value = e.message || '加载会话失败'
  }
}

async function selectConversation(id) {
  activeConversationId.value = id
  await loadMessages()
}

async function loadMessages() {
  if (!activeConversationId.value) return
  msgError.value = ''
  try {
    const res = await listChatMessages(activeConversationId.value)
    if (!res.success) throw new Error(res.message || '加载消息失败')
    messages.value = res.data || []
    await markChatRead(activeConversationId.value)
    await loadConversations()
  } catch (e) {
    msgError.value = e.message || '加载消息失败'
  }
}

async function send() {
  const text = content.value.trim()
  if (!text || !activeConversationId.value || sending.value) return

  sending.value = true
  msgError.value = ''
  try {
    const res = await sendChatMessage(activeConversationId.value, text)
    if (!res.success) throw new Error(res.message || '发送失败')
    content.value = ''
    await loadMessages()
  } catch (e) {
    msgError.value = e.message || '发送失败'
  } finally {
    sending.value = false
  }
}

function formatTime(v) {
  if (!v) return ''
  return new Date(v).toLocaleString()
}

onMounted(async () => {
  await loadConversations()
  timer = setInterval(loadMessages, 3000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>
