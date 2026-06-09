<template>
  <div ref="containerRef" class="infinite-scroll-container" @scroll.passive="handleScroll">
    <div class="infinite-scroll-content">
      <slot name="default" :list="list"></slot>
    </div>

    <div class="infinite-scroll-footer">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span class="loading-text">加载中...</span>
      </div>

      <div v-else-if="error" class="error-state">
        <span class="error-text">{{ error }}</span>
        <el-button type="primary" size="small" @click="handleRetry">
          重试
        </el-button>
      </div>

      <div v-else-if="finished && list.length > 0" class="finished-state">
        <span>没有更多了</span>
      </div>

      <div v-else-if="!loading && list.length === 0" class="empty-state">
        <slot name="empty">
          <p>暂无数据</p>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  list: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  },
  finished: {
    type: Boolean,
    default: false
  },
  threshold: {
    type: Number,
    default: 100
  }
})

const emit = defineEmits(['load'])

const containerRef = ref(null)
let isChecking = false

const handleRetry = () => {
  emit('load')
}

const checkShouldLoad = () => {
  if (props.loading || props.finished) return

  const container = containerRef.value
  if (!container) return

  const distanceToBottom = container.scrollHeight - container.scrollTop - container.clientHeight

  if (distanceToBottom <= props.threshold) {
    emit('load')
  }
}

const handleScroll = () => {
  if (isChecking) return
  isChecking = true
  requestAnimationFrame(() => {
    checkShouldLoad()
    isChecking = false
  })
}

const checkAfterLoad = async () => {
  await nextTick()
  checkShouldLoad()
}

onMounted(() => {
  checkShouldLoad()
})

watch(
  () => props.loading,
  (loading, prevLoading) => {
    if (prevLoading && !loading && !props.finished) {
      checkAfterLoad()
    }
  }
)
</script>

<style scoped>
.infinite-scroll-container {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

.infinite-scroll-content {
  padding-bottom: 8px;
}

.infinite-scroll-footer {
  padding: 20px 0;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.loading-text {
  margin-left: 4px;
}

.error-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.error-text {
  color: #f56c6c;
  font-size: 14px;
}

.finished-state {
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.finished-state span {
  position: relative;
  padding: 0 16px;
}

.finished-state span::before,
.finished-state span::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40px;
  height: 1px;
  background: #e4e7ed;
}

.finished-state span::before {
  left: -40px;
}

.finished-state span::after {
  right: -40px;
}

.empty-state {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}
</style>
