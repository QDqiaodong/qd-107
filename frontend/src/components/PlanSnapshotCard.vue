<template>
  <div class="card plan-snapshot-card" :class="statusClass">
    <div class="snapshot-header">
      <div class="plan-type" :style="{ background: typeColor + '20' }">
        <el-icon :size="20" :color="typeColor">
          <component :is="sportTypeIcon" />
        </el-icon>
      </div>
      <div class="plan-info">
        <div class="plan-title">{{ plan.title }}</div>
        <div class="plan-meta">
          <el-tag size="small" :type="tagType">{{ plan.typeName }}</el-tag>
          <span class="plan-frequency">
            <el-icon><Clock /></el-icon>
            {{ plan.frequency }}
          </span>
        </div>
      </div>
      <span class="status-badge" :style="statusBadgeStyle">
        {{ statusLabel }}
      </span>
    </div>

    <div class="snapshot-metrics" v-if="snapshot">
      <div class="metric-row">
        <div class="metric-item">
          <span class="metric-label">目标次数</span>
          <span class="metric-value">{{ snapshot.targetCount }}次</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">已完成</span>
          <span class="metric-value" :class="{ 'value-done': snapshot.completedCount >= snapshot.targetCount }">
            {{ snapshot.completedCount }}次
          </span>
        </div>
        <div class="metric-item">
          <span class="metric-label">累计时长</span>
          <span class="metric-value">{{ snapshot.completedDuration }}分钟</span>
        </div>
      </div>

      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-title">执行率</span>
          <span class="progress-percent" :style="{ color: progressColor }">
            {{ snapshot.completionRate }}%
          </span>
        </div>
        <div class="progress-track">
          <div class="progress-expected" :style="{ width: snapshot.expectedProgress + '%' }"></div>
          <div class="progress-fill" :style="progressFillStyle"></div>
        </div>
        <div class="progress-legend">
          <span class="legend-item">
            <span class="legend-dot" style="background: #d9ecff;"></span>
            期望进度 {{ snapshot.expectedProgress }}%
          </span>
          <span class="legend-item">
            <span class="legend-dot" :style="{ background: progressColor }"></span>
            实际进度 {{ snapshot.completionRate }}%
          </span>
          <span class="legend-deviation" :style="{ color: snapshot.deviation >= 0 ? '#67c23a' : '#f56c6c' }">
            {{ snapshot.deviation >= 0 ? '+' : '' }}{{ snapshot.deviation }}%
          </span>
        </div>
      </div>

      <div class="snapshot-footer">
        <span class="cycle-info">
          本周周期：{{ snapshot.cycleStart }} ~ {{ snapshot.cycleEnd }}
        </span>
        <span class="days-info">
          已过{{ snapshot.daysElapsed }}天 / 剩余{{ snapshot.daysRemaining }}天
        </span>
      </div>
    </div>

    <div class="plan-target" v-if="plan.target">
      <span class="target-label">目标：</span>{{ plan.target }}
    </div>

    <div class="plan-actions-row">
      <el-button type="success" text size="small" @click="$emit('toggle', plan.id)">
        <el-icon><Check /></el-icon>
        完成
      </el-button>
      <el-button type="danger" text size="small" @click="$emit('delete', plan.id)">
        <el-icon><Delete /></el-icon>
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getSportTypeInfo } from '@/utils/common'
import { EXECUTION_STATUS } from '@/composables/usePlanExecution'

const props = defineProps({
  plan: { type: Object, required: true },
  snapshot: { type: Object, default: null }
})

defineEmits(['toggle', 'delete'])

const typeColor = computed(() => getSportTypeInfo(props.plan.type).color)

const sportTypeIcon = computed(() => {
  const map = {
    running: 'Running',
    cycling: 'Bicycle',
    swimming: 'Watermelon',
    yoga: 'Moon',
    gym: 'Sugar',
    other: 'MoreFilled'
  }
  return map[props.plan.type] || 'MoreFilled'
})

const tagType = computed(() => {
  const map = {
    running: 'success',
    cycling: 'primary',
    swimming: 'danger',
    yoga: 'warning',
    gym: 'success',
    other: 'info'
  }
  return map[props.plan.type] || 'info'
})

const statusInfo = computed(() => {
  if (!props.snapshot) return EXECUTION_STATUS.IN_PROGRESS
  return EXECUTION_STATUS[props.snapshot.status] || EXECUTION_STATUS.IN_PROGRESS
})

const statusLabel = computed(() => statusInfo.value.label)

const statusClass = computed(() => `status-${(props.snapshot?.status || 'IN_PROGRESS').toLowerCase().replace(/_/g, '-')}`)

const statusBadgeStyle = computed(() => ({
  color: statusInfo.value.color,
  background: statusInfo.value.bg,
  borderColor: statusInfo.value.borderColor
}))

const progressColor = computed(() => {
  if (!props.snapshot) return '#409eff'
  if (props.snapshot.completionRate >= 100) return '#67c23a'
  if (props.snapshot.deviation < -20) return '#f56c6c'
  if (props.snapshot.deviation < 0) return '#e6a23c'
  return '#409eff'
})

const progressFillStyle = computed(() => {
  if (!props.snapshot) return { width: '0%', background: '#409eff' }
  return {
    width: Math.min(props.snapshot.completionRate, 100) + '%',
    background: `linear-gradient(90deg, ${progressColor.value}88, ${progressColor.value})`
  }
})
</script>

<style scoped>
.plan-snapshot-card {
  margin-bottom: 16px;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.plan-snapshot-card.status-goal-met {
  border-left-color: #67c23a;
}

.plan-snapshot-card.status-in-progress {
  border-left-color: #409eff;
}

.plan-snapshot-card.status-significantly-behind {
  border-left-color: #f56c6c;
}

.snapshot-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.plan-type {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.plan-info {
  flex: 1;
  min-width: 0;
}

.plan-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.plan-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.plan-frequency {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
  white-space: nowrap;
  border: 1px solid transparent;
  flex-shrink: 0;
}

.snapshot-metrics {
  margin-bottom: 12px;
}

.metric-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.metric-label {
  font-size: 12px;
  color: #909399;
}

.metric-value {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.metric-value.value-done {
  color: #67c23a;
}

.progress-section {
  margin-bottom: 12px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-title {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.progress-percent {
  font-size: 16px;
  font-weight: 700;
}

.progress-track {
  position: relative;
  height: 10px;
  background: #ebeef5;
  border-radius: 5px;
  overflow: hidden;
}

.progress-expected {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: #d9ecff;
  border-radius: 5px;
  z-index: 1;
}

.progress-fill {
  position: relative;
  height: 100%;
  border-radius: 5px;
  z-index: 2;
  transition: width 0.4s ease;
}

.progress-legend {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  flex-shrink: 0;
}

.legend-deviation {
  margin-left: auto;
  font-weight: 600;
}

.snapshot-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
}

.plan-target {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.target-label {
  color: #909399;
}

.plan-actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  padding-top: 8px;
}

@media (max-width: 768px) {
  .snapshot-header {
    flex-wrap: wrap;
  }

  .status-badge {
    font-size: 11px;
    padding: 3px 8px;
  }

  .metric-row {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .metric-item {
    padding: 8px;
  }

  .metric-value {
    font-size: 15px;
  }

  .snapshot-footer {
    flex-direction: column;
    gap: 4px;
    align-items: flex-start;
  }
}
</style>
