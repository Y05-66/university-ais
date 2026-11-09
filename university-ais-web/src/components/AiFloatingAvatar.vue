<template>
  <div class="ai-float-wrapper" ref="wrapperRef">
    <!-- 卡通人物 -->
    <div
      class="ai-avatar"
      :style="{ left: position.x + 'px', top: position.y + 'px' }"
      @mousedown="startDrag"
      @click.stop="toggleChat"
    >
      <div class="robot-emoji">🤖</div>
      <div class="pulse-ring" v-if="!isChatOpen"></div>
    </div>

    <!-- 对话窗口 -->
    <transition name="slide-fade">
      <div v-if="isChatOpen" class="chat-window" :style="chatStyle">
        <div class="chat-header">
          <div class="header-avatar">🤖</div>
          <span>AI 智能助手</span>
          <el-icon class="close-btn" @click="toggleChat"><Close /></el-icon>
        </div>

        <div class="chat-messages" ref="messageContainer">
          <div v-for="(msg, index) in messages" :key="index"
               class="message" :class="{ 'user': msg.role === 'user', 'ai': msg.role === 'ai' }">
            <div class="msg-avatar">
              <span v-if="msg.role === 'user'">👤</span>
              <span v-else>🤖</span>
            </div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="message ai">
            <div class="msg-avatar">🤖</div>
            <div class="msg-content typing">思考中...</div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="4"
            placeholder="输入问题..."
            @keydown.enter.exact.prevent="sendMessage"
          />
          <el-button type="primary" @click="sendMessage" :loading="loading" size="small">
            发送
          </el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { aiChat, aiChatForStudent, aiChatForTeacher, aiChatForAdmin } from '@/api'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

const userStore = useUserStore()
const messageContainer = ref(null)

// 卡通人物位置
const position = reactive({ x: window.innerWidth - 80, y: window.innerHeight - 120 })
const isDragging = ref(false)
const dragOffset = reactive({ x: 0, y: 0 })

// 聊天窗口
const isChatOpen = ref(false)
const chatPosition = reactive({ x: 0, y: 0 })

// 聊天消息
const messages = ref([
  { role: 'ai', content: '你好！我是 AI 智能助手，有什么可以帮助你的吗？' }
])
const inputMessage = ref('')
const loading = ref(false)

// 计算聊天窗口样式
const chatStyle = computed(() => ({
  left: Math.max(10, Math.min(chatPosition.x, window.innerWidth - 440)) + 'px',
  top: Math.max(10, Math.min(chatPosition.y, window.innerHeight - 540)) + 'px'
}))

// 开始拖拽
const startDrag = (e) => {
  if (isChatOpen.value) return
  isDragging.value = true
  dragOffset.x = e.clientX - position.x
  dragOffset.y = e.clientY - position.y

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

// 拖拽过程
const onDrag = (e) => {
  if (!isDragging.value) return
  position.x = Math.max(0, Math.min(e.clientX - dragOffset.x, window.innerWidth - 70))
  position.y = Math.max(0, Math.min(e.clientY - dragOffset.y, window.innerHeight - 70))

  chatPosition.x = position.x - 420
  chatPosition.y = position.y - 60
}

// 停止拖拽
const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 切换聊天窗口
const toggleChat = () => {
  if (isDragging.value) return
  isChatOpen.value = !isChatOpen.value
  if (isChatOpen.value) {
    chatPosition.x = Math.max(10, position.x - 300)
    chatPosition.y = Math.max(10, position.y - 50)
  }
}

// 发送消息
const sendMessage = async () => {
  const question = inputMessage.value.trim()
  if (!question || loading.value) return

  messages.value.push({ role: 'user', content: question })
  inputMessage.value = ''
  loading.value = true

  try {
    await scrollToBottom()
    const askFunction = getUserAskFunction()
    const res = await askFunction(question, '')
    messages.value.push({ role: 'ai', content: res.data?.answer || res.data || '抱歉，暂时无法回答' })
    await scrollToBottom()
  } catch (error) {
    ElMessage.error(error.message || '请求失败')
  } finally {
    loading.value = false
  }
}

// 获取对应用户类型的AI函数
const getUserAskFunction = () => {
  if (userStore.isStudent()) return aiChatForStudent
  if (userStore.isTeacher()) return aiChatForTeacher
  if (userStore.isAdmin()) return aiChatForAdmin
  return aiChat
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<style scoped lang="scss">
.ai-float-wrapper {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1000;
}

/* 卡通人物 */
.ai-avatar {
  position: absolute;
  width: 56px;
  height: 56px;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1001;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    transform: scale(1.15) rotate(-5deg);
    filter: drop-shadow(0 6px 16px rgba(102, 126, 234, 0.5));
  }

  .robot-emoji {
    font-size: 42px;
    line-height: 1;
    filter: drop-shadow(0 4px 10px rgba(102, 126, 234, 0.35));
    animation: float 3s ease-in-out infinite;
  }

  @keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-6px); }
  }

  .pulse-ring {
    position: absolute;
    width: 56px;
    height: 56px;
    border: 2px solid #667eea;
    border-radius: 50%;
    animation: pulse 2s infinite;
  }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.7); opacity: 0; }
}

/* 对话窗口 */
.chat-window {
  position: fixed;
  width: 420px;
  height: 520px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  pointer-events: auto;
  z-index: 1000;
  border: 1px solid rgba(102, 126, 234, 0.15);

  .chat-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-weight: bold;
    font-size: 14px;

    .header-avatar {
      font-size: 20px;
      animation: bounce 2s ease-in-out infinite;
    }

    @keyframes bounce {
      0%, 100% { transform: translateY(0); }
      50% { transform: translateY(-3px); }
    }

    .close-btn {
      margin-left: auto;
      cursor: pointer;
      font-size: 16px;
      opacity: 0.8;
      transition: all 0.2s;

      &:hover {
        opacity: 1;
        transform: rotate(90deg);
      }
    }
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
    background: linear-gradient(180deg, #f8f9ff 0%, #f0f2ff 100%);

    .message {
      display: flex;
      margin-bottom: 12px;

      &.user {
        flex-direction: row-reverse;

        .msg-avatar {
          background: linear-gradient(135deg, #667eea, #764ba2);
          margin-left: 8px;
        }

        .msg-content {
          background: linear-gradient(135deg, #667eea, #764ba2);
          color: #fff;
          border-radius: 14px 14px 2px 14px;
        }
      }

      &.ai {
        .msg-avatar {
          background: linear-gradient(135deg, #67c23a, #5daf34);
          margin-right: 8px;
        }

        .msg-content {
          background: #fff;
          border-radius: 14px 14px 14px 2px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        }
      }

      .msg-avatar {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        flex-shrink: 0;
      }

      .msg-content {
        max-width: 200px;
        padding: 8px 12px;
        font-size: 13px;
        line-height: 1.5;
        word-break: break-word;
      }

      .typing {
        &::after {
          content: '...';
          animation: dots 1.5s infinite;
        }
      }
    }
  }

  @keyframes dots {
    0%, 20% { content: '.'; }
    40% { content: '..'; }
    60%, 100% { content: '...'; }
  }

  .chat-input {
    padding: 10px;
    border-top: 1px solid #eee;
    display: flex;
    gap: 8px;
    background: #fff;

    :deep(.el-textarea__inner) {
      font-size: 12px;
      border-radius: 10px;
    }

    .el-button {
      flex-shrink: 0;
      border-radius: 10px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border: none;

      &:hover {
        opacity: 0.9;
      }
    }
  }
}

/* 动画 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(15px);
}
</style>
