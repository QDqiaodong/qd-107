<template>
  <div class="weekly-schedule-board card">
    <div class="board-header">
      <div class="board-title">
        <el-icon class="title-icon"><Calendar /></el-icon>
        <span>本周训练排班</span>
      </div>
      <div class="board-summary">
        <div class="summary-item">
          <span class="summary-label">本周总时长</span>
          <span class="summary-value">{{ weeklyTotalDuration }} 分钟</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">训练天数</span>
          <span class="summary-value">{{ activeDays }} 天</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">计划数</span>
          <span class="summary-value">{{ totalPlanCount }} 个</span>
        </div>
      </div>
    </div>

    <div class="week-grid">
      <div
        v-for="day in weeklySchedule"
        :key="day.day"
        class="day-column"
        :class="{ 'is-today': day.isToday }"
      >
        <div class="day-header">
          <div class="day-name">{{ day.dayName }}</div>
          <div class="day-date" v-if="day.isToday">今天</div>
        </div>

        <div class="day-body">
          <div v-if="day.plans.length === 0" class="day-empty">
            <el-icon><Moon /></el-icon>
            <span>休息日</span>
          </div>
          <div v-else class="day-plans">
            <div
              v-for="plan in day.plans"
              :key="plan.id"
              class="plan-tag"
              :style="{ borderLeftColor: getTypeColor(plan.type) }"
            >
              <div class="plan-tag-title">{{ plan.title }}</div>
              <div class="plan-tag-meta">
                <el-icon :size="12" :color="getTypeColor(plan.type)">
                  <component :is="getTypeIcon(plan.type)" />
                </el-icon>
                <span>{{ plan.duration }} 分钟</span>
              </div>
            </div>
          </div>
        </div>

        <div class="day-footer">
          <div class="duration-bar">
            <div
              class="duration-fill"
              :style="{ width: getDurationPercent(day.totalDuration) + '%', background: getDurationColor(day.totalDuration) }"
            ></div>
          </div>
          <div class="duration-info">
            <span class="duration-used" :style="{ color: getDurationColor(day.totalDuration) }">
              {{ day.totalDuration }} min
            </span>
            <span class="duration-free">
              余 {{ dailyTarget - day.totalDuration > 0 ? dailyTarget - day.totalDuration : 0 }} min
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="board-legend">
      <div class="legend-item">
        <span class="legend-dot" style="background: #67c23a;"></span>
        <span>轻松 (≤{{ dailyTarget * 0.5 }}分钟)</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background: #e6a23c;"></span>
        <span>适中 ({{ dailyTarget * 0.5 }}~{{ dailyTarget }}分钟)</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background: #f56c6c;"></span>
        <span>饱满 (&gt;{{ dailyTarget }}分钟)</span>
      </div>
      <div class="legend-target">每日目标：{{ dailyTarget }} 分钟</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCheckinStore } from '@/stores/checkin'
import { getSportTypeInfo } from '@/utils/common'
import { Calendar, Moon } from '@element-plus/icons-vue'

const props = defineProps({
  dailyTarget: {
    type: Number,
    default: 60
  }
})

const checkinStore = useCheckinStore()

const weeklySchedule = computed(() => checkinStore.weeklySchedule)
const weeklyTotalDuration = computed(() => checkinStore.weeklyTotalDuration)

const activeDays = computed(() => {
  return weeklySchedule.value.filter(day => day.plans.length > 0).length
})

const totalPlanCount = computed(() => {
  return weeklySchedule.value.reduce((sum, day) => sum + day.plans.length, 0)
})

const getTypeColor = (type) => {
  return getSportTypeInfo(type).color
}

const getTypeIcon = (type) => {
  const iconMap = {
    running: 'Running',
    cycling: 'Bicycle',
    swimming: 'Watermelon',
    yoga: 'Moon',
    gym: 'Sugar',
    other: 'MoreFilled'
  }
  return iconMap[type] || 'MoreFilled'
}

const getDurationPercent = (duration) => {
  const percent = (duration / props.dailyTarget) * 100
  return Math.min(percent, 100)
}

const getDurationColor = (duration) => {
  if (duration === 0) return '#dcdfe6'
  if (duration <= props.dailyTarget * 0.5) return '#67c23a'
  if (duration <= props.dailyTarget) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.weekly-schedule-board {
  padding: 24px;
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.board-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.title-icon {
  color: #409eff;
  font-size: 22px;
}

.board-summary {
  display: flex;
  gap: 24px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.summary-label {
  font-size: 12px;
  color: #909399;
}

.summary-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.day-column {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  min-height: 220px;
}

.day-column.is-today {
  background: #ecf5ff;
  border-color: #409eff;
}

.day-header {
  text-align: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.day-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.day-date {
  font-size: 11px;
  color: #409eff;
  font-weight: 500;
  margin-top: 2px;
  background: #409eff;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
}

.day-body {
  flex: 1;
  min-height: 100px;
}

.day-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
  font-size: 12px;
  gap: 6px;
}

.day-empty .el-icon {
  font-size: 24px;
}

.day-plans {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.plan-tag {
  background: #fff;
  border-radius: 6px;
  padding: 8px 10px;
  border-left: 3px solid #67c23a;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.plan-tag-title {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plan-tag-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #909399;
}

.day-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.duration-bar {
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 6px;
}

.duration-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.duration-info {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
}

.duration-used {
  font-weight: 600;
}

.duration-free {
  color: #c0c4cc;
}

.board-legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-target {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

@media (max-width: 900px) {
  .week-grid {
    grid-template-columns: repeat(7, 1fr);
    gap: 6px;
  }

  .day-column {
    padding: 8px 6px;
    min-height: 180px;
  }

  .plan-tag {
    padding: 6px 8px;
  }

  .plan-tag-title {
    font-size: 11px;
  }

  .board-summary {
    gap: 16px;
  }
}

@media (max-width: 640px) {
  .week-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .day-column {
    min-height: auto;
  }

  .board-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .board-summary {
    width: 100%;
    justify-content: space-between;
  }

  .summary-item {
    align-items: flex-start;
  }

  .legend-target {
    margin-left: 0;
    width: 100%;
    text-align: center;
  }
}
</style>
