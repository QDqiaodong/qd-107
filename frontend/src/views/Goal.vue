<template>
  <div class="goal-page">
    <h1 class="page-title">月度冲线目标</h1>

    <div class="card goal-header-card" v-if="checkinStore.monthGoalProgress">
      <div class="goal-header-top">
        <div class="goal-month">
          <el-icon :size="24" color="#ff6b6b"><Flag /></el-icon>
          <span>{{ currentMonthText }}冲线目标</span>
        </div>
        <el-button type="primary" size="small" @click="showSetGoal = true">
          <el-icon><Edit /></el-icon>
          设置目标
        </el-button>
      </div>

      <div class="goal-stats-summary">
        <div class="goal-stat-item">
          <div class="goal-stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon :size="20"><Trophy /></el-icon>
          </div>
          <div class="goal-stat-info">
            <div class="goal-stat-value">
              {{ checkinStore.monthGoalProgress.checkinCount.current }}
              <span class="goal-stat-target">/{{ checkinStore.monthGoalProgress.checkinCount.target }}次</span>
            </div>
            <div class="goal-stat-label">打卡次数</div>
          </div>
        </div>
        <div class="goal-stat-item">
          <div class="goal-stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon :size="20"><Clock /></el-icon>
          </div>
          <div class="goal-stat-info">
            <div class="goal-stat-value">
              {{ checkinStore.monthGoalProgress.totalDuration.current }}
              <span class="goal-stat-target">/{{ checkinStore.monthGoalProgress.totalDuration.target }}分钟</span>
            </div>
            <div class="goal-stat-label">运动时长</div>
          </div>
        </div>
        <div class="goal-stat-item">
          <div class="goal-stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon :size="20"><HotWater /></el-icon>
          </div>
          <div class="goal-stat-info">
            <div class="goal-stat-value">
              {{ checkinStore.monthGoalProgress.totalCalorie.current }}
              <span class="goal-stat-target">/{{ checkinStore.monthGoalProgress.totalCalorie.target }}千卡</span>
            </div>
            <div class="goal-stat-label">消耗热量</div>
          </div>
        </div>
      </div>

      <div class="goal-progress-bars">
        <div class="progress-item">
          <div class="progress-header">
            <span class="progress-label">打卡进度</span>
            <span class="progress-percent">{{ checkinStore.monthGoalProgress.checkinCount.progress.toFixed(1) }}%</span>
          </div>
          <el-progress 
            :percentage="checkinStore.monthGoalProgress.checkinCount.progress" 
            :show-text="false"
            :stroke-width="12"
            color="url(#countGradient)"
          />
          <div class="progress-tip">
            还差 {{ checkinStore.monthGoalProgress.checkinCount.remaining }} 次，
            平均每天需 {{ checkinStore.monthGoalProgress.checkinCount.dailyNeeded }} 次
          </div>
        </div>
        <div class="progress-item">
          <div class="progress-header">
            <span class="progress-label">时长进度</span>
            <span class="progress-percent">{{ checkinStore.monthGoalProgress.totalDuration.progress.toFixed(1) }}%</span>
          </div>
          <el-progress 
            :percentage="checkinStore.monthGoalProgress.totalDuration.progress" 
            :show-text="false"
            :stroke-width="12"
            color="url(#durationGradient)"
          />
          <div class="progress-tip">
            还差 {{ checkinStore.monthGoalProgress.totalDuration.remaining }} 分钟，
            平均每天需 {{ checkinStore.monthGoalProgress.totalDuration.dailyNeeded }} 分钟
          </div>
        </div>
        <div class="progress-item">
          <div class="progress-header">
            <span class="progress-label">热量进度</span>
            <span class="progress-percent">{{ checkinStore.monthGoalProgress.totalCalorie.progress.toFixed(1) }}%</span>
          </div>
          <el-progress 
            :percentage="checkinStore.monthGoalProgress.totalCalorie.progress" 
            :show-text="false"
            :stroke-width="12"
            color="url(#calorieGradient)"
          />
          <div class="progress-tip">
            还差 {{ checkinStore.monthGoalProgress.totalCalorie.remaining }} 千卡，
            平均每天需 {{ checkinStore.monthGoalProgress.totalCalorie.dailyNeeded }} 千卡
          </div>
        </div>
      </div>

      <div class="goal-remaining-info">
        <el-icon :size="16" color="#f59e0b"><Warning /></el-icon>
        <span>本月还剩 <b>{{ checkinStore.monthGoalProgress.remainingDays }}</b> 天，加油冲线！</span>
      </div>
    </div>

    <div class="card goal-empty-card" v-else>
      <div class="empty-icon">
        <el-icon :size="48" color="#c0c4cc"><Flag /></el-icon>
      </div>
      <h3>还没有设置本月目标</h3>
      <p>设定月度目标，让运动更有动力！</p>
      <el-button type="primary" @click="showSetGoal = true">
        <el-icon><Plus /></el-icon>
        设置目标
      </el-button>
    </div>

    <div class="card daily-detail-card" v-if="checkinStore.monthGoalProgress">
      <div class="card-header">
        <h3 class="section-title">每日完成明细</h3>
        <div class="view-toggle">
          <el-radio-group v-model="detailType" size="small">
            <el-radio-button label="checkin">打卡次数</el-radio-button>
            <el-radio-button label="duration">运动时长</el-radio-button>
            <el-radio-button label="calorie">消耗热量</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="daily-list">
        <div 
          v-for="item in checkinStore.dailyGoalDetail" 
          :key="item.date"
          class="daily-item"
          :class="{ 
            'is-today': item.isToday,
            'is-future': item.isFuture,
            'is-completed': isDayCompleted(item)
          }"
        >
          <div class="daily-left">
            <div class="daily-date">
              <span class="date-num">{{ item.day }}日</span>
              <span class="weekday">{{ item.weekday }}</span>
            </div>
            <div v-if="item.isToday" class="today-tag">今天</div>
            <div v-else-if="item.isFuture" class="future-tag">待完成</div>
            <div v-else-if="isDayCompleted(item)" class="completed-tag">已完成</div>
            <div v-else class="missed-tag">未完成</div>
          </div>
          <div class="daily-right">
            <div class="daily-value">
              <span class="value-num">{{ getDailyValue(item) }}</span>
              <span class="value-unit">{{ getDailyUnit() }}</span>
            </div>
            <div class="daily-bar-wrapper">
              <div class="daily-bar-bg">
                <div 
                  class="daily-bar-fill" :style="{ width: getDailyProgress(item) + '%' }"></div>
              </div>
              <span class="daily-percent">{{ getDailyProgress(item).toFixed(0) }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card plan-execution-card" v-if="planSnapshots.length > 0">
      <div class="card-header">
        <h3 class="section-title">计划执行率快照</h3>
        <div class="execution-summary-mini">
          <span class="mini-stat mini-goal-met">
            <span class="mini-dot" style="background: #67c23a;"></span>
            {{ planExecutionSummary.goalMet }}达标
          </span>
          <span class="mini-stat mini-in-progress">
            <span class="mini-dot" style="background: #409eff;"></span>
            {{ planExecutionSummary.inProgress }}进行
          </span>
          <span class="mini-stat mini-behind">
            <span class="mini-dot" style="background: #f56c6c;"></span>
            {{ planExecutionSummary.behind }}落后
          </span>
        </div>
      </div>

      <div class="execution-list">
        <div
          v-for="snap in planSnapshots"
          :key="snap.planId"
          class="execution-item"
          :class="'exec-' + snap.status.toLowerCase().replace(/_/g, '-')"
        >
          <div class="exec-top">
            <div class="exec-title">{{ snap.title }}</div>
            <span class="exec-status-badge" :style="snap.statusInfo ? { color: snap.statusInfo.color, background: snap.statusInfo.bg, borderColor: snap.statusInfo.borderColor } : {}">
              {{ snap.statusInfo?.label || '进行中' }}
            </span>
          </div>
          <div class="exec-metrics">
            <span class="exec-metric">
              <span class="exec-metric-label">目标</span>
              <span class="exec-metric-value">{{ snap.targetCount }}次 / {{ snap.targetDuration }}分钟</span>
            </span>
            <span class="exec-metric">
              <span class="exec-metric-label">完成</span>
              <span class="exec-metric-value" :style="{ color: snap.completionRate >= 100 ? '#67c23a' : '#303133' }">
                {{ snap.completedCount }}次 / {{ snap.completedDuration }}分钟
              </span>
            </span>
          </div>
          <div class="exec-progress-track">
            <div class="exec-progress-expected" :style="{ width: snap.expectedProgress + '%' }"></div>
            <div
              class="exec-progress-fill"
              :style="{
                width: Math.min(snap.completionRate, 100) + '%',
                background: snap.status === 'GOAL_MET' ? 'linear-gradient(90deg, #67c23a88, #67c23a)'
                  : snap.status === 'SIGNIFICANTLY_BEHIND' ? 'linear-gradient(90deg, #f56c6c88, #f56c6c)'
                  : 'linear-gradient(90deg, #409eff88, #409eff)'
              }"
            ></div>
          </div>
          <div class="exec-footer">
            <span class="exec-rate">完成率 {{ snap.completionRate }}%</span>
            <span class="exec-deviation" :style="{ color: snap.deviation >= 0 ? '#67c23a' : '#f56c6c' }">
              偏差 {{ snap.deviation >= 0 ? '+' : '' }}{{ snap.deviation }}%
            </span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showSetGoal" title="设置月度冲线目标" width="500px" class="set-goal-dialog">
      <el-form :model="goalForm" label-width="100px">
        <el-form-item label="打卡次数">
          <el-input-number v-model="goalForm.checkinCount" :min="1" :max="100" />
          <span class="form-tip">次 / 月</span>
        </el-form-item>
        <el-form-item label="运动时长">
          <el-input-number v-model="goalForm.totalDuration" :min="10" :max="10000" :step="30" />
          <span class="form-tip">分钟 / 月</span>
        </el-form-item>
        <el-form-item label="消耗热量">
          <el-input-number v-model="goalForm.totalCalorie" :min="100" :max="50000" :step="100" />
          <span class="form-tip">千卡 / 月</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSetGoal = false">取消</el-button>
        <el-button type="primary" @click="saveGoal">保存</el-button>
      </template>
    </el-dialog>

    <svg style="position: absolute; width: 0; height: 0;">
      <defs>
        <linearGradient id="countGradient" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" style="stop-color:#667eea" />
          <stop offset="100%" style="stop-color:#764ba2" />
        </linearGradient>
        <linearGradient id="durationGradient" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" style="stop-color:#f093fb" />
          <stop offset="100%" style="stop-color:#f5576c" />
        </linearGradient>
        <linearGradient id="calorieGradient" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" style="stop-color:#fa709a" />
          <stop offset="100%" style="stop-color:#fee140" />
        </linearGradient>
      </defs>
    </svg>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Flag,
  Edit,
  Trophy,
  Clock,
  HotWater,
  Warning,
  Plus
} from '@element-plus/icons-vue'
import { useCheckinStore } from '@/stores/checkin'
import { usePlanExecution } from '@/composables/usePlanExecution'

const checkinStore = useCheckinStore()
const { snapshots: planSnapshots, summary: planExecutionSummary } = usePlanExecution()
const showSetGoal = ref(false)
const detailType = ref('checkin')

const goalForm = ref({
  checkinCount: 20,
  totalDuration: 1500,
  totalCalorie: 5000
})

const currentMonthText = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月`
})

watch(showSetGoal, (val) => {
  if (val && checkinStore.currentMonthGoal) {
    goalForm.value = { ...checkinStore.currentMonthGoal }
  }
})

const loadBackendCheckins = async () => {
  try {
    await checkinStore.fetchCheckins()
  } catch (e) {
    console.warn('从后端加载打卡数据失败，使用本地缓存', e)
  }
}

onMounted(() => {
  loadBackendCheckins()
})

const saveGoal = () => {
  checkinStore.setMonthlyGoal(goalForm.value)
  ElMessage.success('目标设置成功')
  showSetGoal.value = false
}

const getDailyValue = (item) => {
  if (detailType.value === 'checkin') {
    return item.checkinCount
  } else if (detailType.value === 'duration') {
    return item.duration
  } else {
    return item.calorie
  }
}

const getDailyUnit = () => {
  if (detailType.value === 'checkin') {
    return '次'
  } else if (detailType.value === 'duration') {
    return '分钟'
  } else {
    return '千卡'
  }
}

const getDailyProgress = (item) => {
  if (!checkinStore.monthGoalProgress) return 0
  
  let target = 0
  let current = 0
  
  if (detailType.value === 'checkin') {
    const daysInMonth = checkinStore.monthGoalProgress.daysInMonth
    target = checkinStore.monthGoalProgress.checkinCount.target / daysInMonth
    current = item.checkinCount
  } else if (detailType.value === 'duration') {
    const daysInMonth = checkinStore.monthGoalProgress.daysInMonth
    target = checkinStore.monthGoalProgress.totalDuration.target / daysInMonth
    current = item.duration
  } else {
    const daysInMonth = checkinStore.monthGoalProgress.daysInMonth
    target = checkinStore.monthGoalProgress.totalCalorie.target / daysInMonth
    current = item.calorie
  }
  
  return target > 0 ? Math.min(100, (current / target) * 100) : 0
}

const isDayCompleted = (item) => {
  if (item.isFuture) return false
  return getDailyProgress(item) >= 100
}
</script>

<style scoped>
.goal-page {
  position: relative;
}

.goal-header-card {
  margin-bottom: 20px;
}

.goal-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.goal-month {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.goal-stats-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.goal-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 10px;
}

.goal-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.goal-stat-info {
  flex: 1;
  min-width: 0;
}

.goal-stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.goal-stat-target {
  font-size: 13px;
  font-weight: normal;
  color: #909399;
}

.goal-stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.goal-progress-bars {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.progress-item {
  position: relative;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.progress-percent {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.progress-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.goal-remaining-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 30%);
  border-radius: 8px;
  font-size: 14px;
  color: #92400e;
}

.goal-remaining-info b {
  color: #92400e;
  font-size: 16px;
  margin: 0 2px;
}

.goal-empty-card {
  text-align: center;
  padding: 60px 20px;
  margin-bottom: 20px;
}

.empty-icon {
  margin-bottom: 16px;
}

.goal-empty-card h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 8px 0;
}

.goal-empty-card p {
  font-size: 14px;
  color: #909399;
  margin: 0 0 20px 0;
}

.daily-detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.card-header .section-title {
  margin: 0;
}

.daily-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.daily-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 10px;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.daily-item.is-today {
  border-color: #409eff;
  background: #ecf5ff;
}

.daily-item.is-completed {
  border-color: #67c23a;
}

.daily-item.is-future {
  opacity: 0.6;
}

.daily-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 70px;
}

.daily-date {
  text-align: center;
}

.date-num {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  display: block;
}

.weekday {
  font-size: 12px;
  color: #909399;
}

.today-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #409eff;
  color: #fff;
  border-radius: 10px;
}

.completed-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #67c23a;
  color: #fff;
  border-radius: 10px;
}

.missed-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #f56c6c;
  color: #fff;
  border-radius: 10px;
}

.future-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #909399;
  color: #fff;
  border-radius: 10px;
}

.daily-right {
  flex: 1;
  min-width: 0;
}

.daily-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.value-num {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.value-unit {
  font-size: 13px;
  color: #909399;
}

.daily-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.daily-bar-bg {
  flex: 1;
  height: 8px;
  background: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.daily-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.daily-percent {
  font-size: 12px;
  color: #909399;
  min-width: 40px;
  text-align: right;
}

.form-tip {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
}

.set-goal-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.plan-execution-card {
  margin-bottom: 20px;
}

.execution-summary-mini {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mini-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.mini-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.execution-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.execution-item {
  padding: 14px;
  background: #fafafa;
  border-radius: 10px;
  border-left: 3px solid #409eff;
  transition: all 0.2s ease;
}

.execution-item.exec-goal-met {
  border-left-color: #67c23a;
}

.execution-item.exec-significantly-behind {
  border-left-color: #f56c6c;
}

.exec-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.exec-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.exec-status-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
  border: 1px solid transparent;
}

.exec-metrics {
  display: flex;
  gap: 24px;
  margin-bottom: 10px;
}

.exec-metric {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.exec-metric-label {
  font-size: 12px;
  color: #909399;
}

.exec-metric-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.exec-progress-track {
  position: relative;
  height: 8px;
  background: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.exec-progress-expected {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: #d9ecff;
  border-radius: 4px;
  z-index: 1;
}

.exec-progress-fill {
  position: relative;
  height: 100%;
  border-radius: 4px;
  z-index: 2;
  transition: width 0.4s ease;
}

.exec-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.exec-rate {
  color: #606266;
  font-weight: 500;
}

.exec-deviation {
  font-weight: 600;
}

@media (max-width: 768px) {
  .execution-summary-mini {
    flex-wrap: wrap;
    gap: 8px;
  }

  .exec-metrics {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .goal-stats-summary {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .goal-stat-item {
    padding: 12px;
  }

  .goal-stat-value {
    font-size: 18px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .daily-item {
    padding: 12px;
    gap: 12px;
  }

  .daily-left {
    min-width: 60px;
  }

  .date-num {
    font-size: 18px;
  }

  .value-num {
    font-size: 20px;
  }
}
</style>
