<!-- AI 智能助手页面。根据用户角色调用不同的 AI 接口，支持流式对话与自动滚动。 -->
<template>
  <div class="ai-assistant">
    <div class="chat-header">
      <el-icon size="24"><ChatDotRound /></el-icon>
      <span>AI 智能助手</span>
    </div>
    
    <div class="chat-messages" ref="messageContainer">
      <div v-for="(msg, index) in messages" :key="index" 
           class="message" :class="{ 'user': msg.role === 'user', 'ai': msg.role === 'ai' }">
        <div class="avatar">
          <el-icon v-if="msg.role === 'user'" size="20"><User /></el-icon>
          <el-icon v-else size="20"><Monitor /></el-icon>
        </div>
        <div class="content">{{ msg.content }}</div>
      </div>
      <div v-if="loading" class="message ai">
        <div class="avatar"><el-icon size="20"><Loading /></el-icon></div>
        <div class="content thinking">思考中...</div>
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="输入您的问题，按 Enter 发送，Shift+Enter 换行"
        @keydown.enter.exact.prevent="sendMessage"
      />
      <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputMessage.trim()">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { aiChat, aiChatForStudent, aiChatForTeacher, aiChatForAdmin } from '@/api'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Monitor, Loading } from '@element-plus/icons-vue'

const userStore = useUserStore()
const messages = ref([
  { role: 'ai', content: '你好！我是 AI 智能助手，有什么可以帮助你的吗？' }
])
const inputMessage = ref('')
const loading = ref(false)
const messageContainer = ref(null)

const askFunction = computed(() => {
  if (userStore.isStudent()) return aiChatForStudent
  if (userStore.isTeacher()) return aiChatForTeacher
  if (userStore.isAdmin()) return aiChatForAdmin
  return aiChat
})

const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  const question = inputMessage.value.trim()
  if (!question || loading.value) return
  
  messages.value.push({ role: 'user', content: question })
  inputMessage.value = ''
  loading.value = true
  
  try {
    await scrollToBottom()
    const res = await askFunction.value(question)
    messages.value.push({ role: 'ai', content: res.data.answer })
    await scrollToBottom()
  } catch (error) {
    ElMessage.error(error.message || '请求失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.ai-assistant {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #f5f7fa;
  border-radius: 12px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  
  &.user {
    flex-direction: row-reverse;
    .content {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
  }
  
  &.ai {
    .content {
      background: white;
      color: #303133;
    }
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-break: break-word;
}

.thinking {
  font-style: italic;
  color: #909399;
}

.chat-input {
  padding: 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  
  :deep(.el-textarea__inner) {
    flex: 1;
    border-radius: 12px;
  }
}
</style>
