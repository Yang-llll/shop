<template>
  <div class="messages-page">
    <div class="container">
      <h2 class="page-title">消息中心</h2>
      <div class="msg-tabs">
        <el-radio-group v-model="activeTab" @change="onTabChange">
          <el-radio-button value="notifications">系统通知</el-radio-button>
          <el-radio-button value="chat">联系客服</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 系统通知列表 -->
      <div v-if="activeTab === 'notifications'">
        <div class="message-list" v-loading="loading">
          <div class="message-item clickable" v-for="msg in messageList" :key="msg.id" @click="openNotifDetail(msg)">
            <div class="msg-icon">&#x1F514;</div>
            <div class="msg-content">
              <h4>{{ msg.title }}</h4>
              <p>{{ msg.content }}</p>
              <span class="msg-time">{{ formatTime(msg.time) }}</span>
            </div>
            <el-icon class="msg-arrow"><ArrowRight /></el-icon>
          </div>
          <el-empty v-if="!loading && messageList.length === 0" description="暂无消息" />
        </div>
      </div>

      <!-- 聊天界面 -->
      <div v-if="activeTab === 'chat'" class="chat-area">
        <div class="chat-header">
          <el-icon :size="20"><Service /></el-icon>
          <span>腊制品商铺客服</span>
          <span class="online-dot"></span>
        </div>
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-for="msg in chatMessages" :key="msg.id" class="chat-msg" :class="{ mine: msg.mine }">
            <div class="chat-bubble">{{ msg.content }}</div>
            <div class="chat-meta">
              <span class="chat-time">{{ formatTime(msg.createdAt) }}</span>
              <span v-if="msg.mine" class="chat-read-status" :class="{ read: msg.isRead }">
                {{ msg.isRead ? '已读' : '未读' }}
              </span>
            </div>
          </div>
          <div v-if="chatMessages.length === 0" class="chat-empty">开始与客服聊天吧</div>
        </div>
        <div class="chat-input">
          <el-input v-model="chatInput" placeholder="输入消息..." @keyup.enter="sendChat" :disabled="chatSending" />
          <el-button type="primary" @click="sendChat" :loading="chatSending" :disabled="!chatInput.trim()">发送</el-button>
        </div>
      </div>
    </div>

    <!-- 通知详情弹窗 -->
    <el-dialog v-model="notifDialogVisible" :title="selectedNotif?.title || '通知详情'" width="500px" center>
      <div class="notif-detail">
        <p class="notif-content">{{ selectedNotif?.content }}</p>
        <p class="notif-time">{{ formatTime(selectedNotif?.time) }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="notifDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
const userStore = useUserStore()
import axios from 'axios'

const activeTab = ref('notifications')
const loading = ref(false)
const messageList = ref([])
const notifDialogVisible = ref(false)
const selectedNotif = ref(null)

const chatMessages = ref([])
const chatInput = ref('')
const chatSending = ref(false)
const chatMessagesRef = ref(null)
const adminId = ref(null)

function getConfig() {
  const token = sessionStorage.getItem('token')
  return { headers: { Authorization: 'Bearer ' + token } }
}

function formatTime(t) {
  if (!t) return ''
  try {
    const d = new Date(t)
    return d.toLocaleString('zh-CN')
  } catch { return t }
}

async function fetchNotifications() {
  loading.value = true
  try {
    const res = await axios.get('/api/notifications')
    const data = res.data || res
    const items = data.data?.content || data.content || data.data || data || []
    const seen = new Set()
    messageList.value = Array.isArray(items) ? items
      .map(n => ({
        id: n.id,
        title: n.title === '欢迎光临阿珠腊肉坊' ? '欢迎光临腊制品商铺' : n.title,
        content: n.content,
        time: n.createdAt || ''
      }))
      .filter(n => {
        const key = `${n.title || ''}\u0000${n.content || ''}`
        if (seen.has(key)) return false
        seen.add(key)
        return true
      }) : []
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

function openNotifDetail(msg) {
  selectedNotif.value = msg
  notifDialogVisible.value = true
}

function onTabChange() {
  if (activeTab.value === 'chat') fetchChat()
}

async function findAdmin() {
  try {
    const res = await axios.get('/api/chat/partners', getConfig())
    const partners = res.data?.data || res.data || []
    const admin = Array.isArray(partners) ? partners.find(p => p.role === 'ADMIN') : null
    if (admin) { adminId.value = admin.id; return }
    adminId.value = 1
  } catch { adminId.value = 1 }
}

async function fetchChat() {
  await findAdmin()
  try {
    const res = await axios.get('/api/chat/messages', { ...getConfig(), params: { partnerId: adminId.value } })
    const msgs = res.data?.data || res.data || []
    chatMessages.value = Array.isArray(msgs) ? msgs.map(m => ({
      ...m, mine: m.senderId === userStore.userInfo?.userId
    })) : []
    await nextTick()
    scrollChat()
    await axios.post('/api/chat/read/' + adminId.value, {}, getConfig())
  } catch(e) { console.error(e) }
}

async function sendChat() {
  if (!chatInput.value.trim() || !adminId.value) return
  chatSending.value = true
  try {
    const res = await axios.post('/api/chat/send', {
      receiverId: adminId.value, content: chatInput.value
    }, getConfig())
    const msg = res.data?.data || res.data
    if (msg) { chatMessages.value.push({ ...msg, mine: true }) }
    chatInput.value = ''
    await nextTick()
    scrollChat()
  } catch(e) { console.error(e) }
  finally { chatSending.value = false }
}

function scrollChat() {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

onMounted(() => { fetchNotifications() })
</script>

<style scoped>
.messages-page { padding: 32px 20px 60px; }
.container { max-width: 800px; margin: 0 auto; }
.page-title { font-size: 22px; font-weight: 700; color: #333; margin-bottom: 16px; }
.msg-tabs { margin-bottom: 20px; }
.message-list { display: flex; flex-direction: column; gap: 12px; }
.message-item { display: flex; gap: 14px; padding: 16px; background: #fff; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.05); transition: all 0.2s; align-items: flex-start; }
.message-item.clickable { cursor: pointer; }
.message-item.clickable:hover { background: #fafafa; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.msg-icon { font-size: 28px; flex-shrink: 0; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: #FFF0E8; }
.msg-content { flex: 1; min-width: 0; }
.msg-content h4 { font-size: 15px; color: #333; margin-bottom: 6px; }
.msg-content p { font-size: 14px; color: #666; line-height: 1.6; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.msg-time { font-size: 12px; color: #aaa; margin-top: 8px; display: block; }
.msg-arrow { color: #ccc; font-size: 16px; flex-shrink: 0; margin-top: 14px; }
.notif-detail .notif-content { font-size: 15px; color: #333; line-height: 1.8; white-space: pre-wrap; }
.notif-detail .notif-time { font-size: 12px; color: #aaa; margin-top: 16px; }

.chat-area { background: #fff; border-radius: 12px; box-shadow: 0 1px 6px rgba(0,0,0,0.06); overflow: hidden; }
.chat-header { display: flex; align-items: center; gap: 8px; padding: 14px 20px; border-bottom: 1px solid #f0f0f0; font-weight: 600; color: #333; }
.online-dot { width: 8px; height: 8px; border-radius: 50%; background: #67C23A; }
.chat-messages { height: 400px; overflow-y: auto; padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; background: #f9f9f9; }
.chat-msg { display: flex; flex-direction: column; max-width: 70%; }
.chat-msg.mine { align-self: flex-end; align-items: flex-end; }
.chat-msg:not(.mine) { align-self: flex-start; align-items: flex-start; }
.chat-bubble { padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.chat-msg:not(.mine) .chat-bubble { background: #fff; color: #333; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.chat-msg.mine .chat-bubble { background: #E64A19; color: #fff; }
.chat-meta {
  display: flex; align-items: center; gap: 8px; margin-top: 4px;
  justify-content: flex-end;
}
.chat-msg:not(.mine) .chat-meta { justify-content: flex-start; }
.chat-time { font-size: 11px; color: #bbb; }
.chat-read-status { font-size: 11px; color: #bbb; }
.chat-read-status.read { color: #67C23A; }
.chat-empty { text-align: center; color: #ccc; padding: 80px 0; font-size: 14px; }
.chat-input { display: flex; gap: 10px; padding: 12px 20px; border-top: 1px solid #f0f0f0; }
.chat-input .el-input { flex: 1; }
</style>
