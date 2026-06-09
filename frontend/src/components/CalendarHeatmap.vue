<template>
  <div class="calendar-heatmap">
    <div class="calendar-header">
      <el-button type="text" @click="prevMonth" class="nav-btn">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <div class="month-title">{{ currentYear }}年{{ currentMonth }}月</div>
      <el-button type="text" @click="nextMonth" class="nav-btn">
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <div class="calendar-legend">
      <span class="legend-label">少</span>
      <div class="legend-colors">
        <span class="legend-color" style="background: #ebedf0;"></span>
        <span class="legend-color" style="background: #c6e48b;"></span>
        <span class="legend-color" style="background: #7bc96f;"></span>
        <span class="legend-color" style="background: #239a3b;"></span>
        <span class="legend-color" style="background: #196127;"></span>
      </div>
      <span class="legend-label">多</span>
    </div>

    <div class="calendar-grid">
      <div class="weekday-header">
        <span v-for="day in weekDays" :key="day" class="weekday">{{ day }}</span>
      </div>
      <div class="days-grid">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="day-cell"
          :class="{
            'other-month': !day.inMonth,
            'today': day.isToday,
            'has-checkin': day.duration > 0
          }"
          :style="{ backgroundColor: getHeatColor(day.duration) }"
          @click="day.inMonth && handleDayClick(day)"
        >
          <span class="day-number">{{ day.day }}</span>
          <div v-if="day.duration > 0" class="day-duration">{{ day.duration }}分钟</div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showDetailDialog"
      :title="selectedDateTitle"
      width="520px"
      class="detail-dialog"
    >
      <div v-if="selectedDayCheckins.length === 0" class="empty-day">
        <el-icon :size="48" style="color: #c0c4cc;"><Calendar /></el-icon>
        <p>当日暂无打卡记录</p>
      </div>
      <div v-else class="day-checkin-list">
        <div class="day-summary">
          <div class="summary-item">
            <div class="summary-num">{{ selectedDayCheckins.length }}</div>
            <div class="summary-label">打卡次数</div>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-item">
            <div class="summary-num">{{ totalSelectedDuration }}</div>
            <div class="summary-label">总时长(分钟)</div>
          </div>
        </div>
        <div
          v-for="item in selectedDayCheckins"
          :key="item.id"
          class="checkin-item"
        >
          <div class="item-header">
            <div class="item-type">
              <div class="type-icon" :style="{ background: getSportTypeInfo(item.type).color + '20' }">
                <el-icon :size="20" :color="getSportTypeInfo(item.type).color">
                  <component :is="getSportTypeIcon(item.type)" />
                </el-icon>
              </div>
              <div>
                <div class="type-name">{{ item.typeName }}</div>
                <div class="item-time">{{ formatTime(item.createTime) }}</div>
              </div>
            </div>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ item.statusText }}
            </el-tag>
          </div>
          <div class="item-stats">
            <span class="stat"><b>{{ item.duration }}</b> 分钟</span>
            <span class="stat-divider">·</span>
            <span class="stat"><b>{{ item.amount }}</b> {{ item.amountUnit }}</span>
          </div>
          <div v-if="item.note" class="item-note">
            <span class="note-label">备注：</span>{{ item.note }}
          </div>
          <div v-if="item.images && item.images.length > 0" class="item-images">
            <img v-for="(img, idx) in item.images" :key="idx" :src="img" class="item-img" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { sportTypes, getSportTypeInfo, formatDate } from '@/utils/common'

const props = defineProps({
  checkins: {
    type: Array,
    default: () => []
  }
})

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth() + 1)
const showDetailDialog = ref(false)
const selectedDayDate = ref('')

const selectedDayCheckins = computed(() => {
  if (!selectedDayDate.value) return []
  return props.checkins.filter(item => {
    const itemDate = new Date(item.createTime)
    const itemDateStr = `${itemDate.getFullYear()}-${String(itemDate.getMonth() + 1).padStart(2, '0')}-${String(itemDate.getDate()).padStart(2, '0')}`
    return itemDateStr === selectedDayDate.value
  }).sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
})

const totalSelectedDuration = computed(() => {
  return selectedDayCheckins.value.reduce((sum, item) => sum + (item.duration || 0), 0)
})

const selectedDateTitle = computed(() => {
  if (!selectedDayDate.value) return ''
  const [year, month, day] = selectedDayDate.value.split('-')
  return `${year}年${parseInt(month)}月${parseInt(day)}日 打卡详情`
})

const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value - 1
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const firstDayOfWeek = firstDay.getDay()
  const daysInMonth = lastDay.getDate()
  
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  
  const days = []
  
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const dayNum = prevMonthLastDay - i
    days.push({
      day: dayNum,
      inMonth: false,
      date: new Date(year, month - 1, dayNum),
      duration: 0,
      isToday: false
    })
  }
  
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(year, month, i)
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    const dayDuration = getDayDuration(dateStr)
    const isToday = date.toDateString() === today.toDateString()
    
    days.push({
      day: i,
      inMonth: true,
      date: date,
      dateStr: dateStr,
      duration: dayDuration,
      isToday: isToday
    })
  }
  
  const remainingDays = 42 - days.length
  for (let i = 1; i <= remainingDays; i++) {
    days.push({
      day: i,
      inMonth: false,
      date: new Date(year, month + 1, i),
      duration: 0,
      isToday: false
    })
  }
  
  return days
})

const getDayDuration = (dateStr) => {
  return props.checkins.reduce((total, item) => {
    const itemDate = new Date(item.createTime)
    const itemDateStr = `${itemDate.getFullYear()}-${String(itemDate.getMonth() + 1).padStart(2, '0')}-${String(itemDate.getDate()).padStart(2, '0')}`
    if (itemDateStr === dateStr) {
      return total + (item.duration || 0)
    }
    return total
  }, 0)
}

const getHeatColor = (duration) => {
  if (duration === 0) return '#ebedf0'
  if (duration < 30) return '#c6e48b'
  if (duration < 60) return '#7bc96f'
  if (duration < 120) return '#239a3b'
  return '#196127'
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const handleDayClick = (day) => {
  selectedDayDate.value = day.dateStr
  showDetailDialog.value = true
}

const getSportTypeIcon = (type) => {
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

const getStatusType = (status) => {
  const map = {
    excellent: 'success',
    good: 'success',
    normal: 'warning',
    tired: 'danger'
  }
  return map[status] || 'info'
}

const formatTime = (dateStr) => {
  return formatDate(dateStr, 'HH:mm')
}
</script>

<style scoped>
.calendar-heatmap {
  width: 100%;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
}

.month-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  min-width: 140px;
  text-align: center;
}

.nav-btn {
  font-size: 18px;
  color: #606266;
  padding: 8px;
}

.nav-btn:hover {
  color: #409eff;
}

.calendar-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 16px;
}

.legend-label {
  font-size: 12px;
  color: #909399;
}

.legend-colors {
  display: flex;
  gap: 3px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.calendar-grid {
  width: 100%;
}

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: #909399;
  padding: 8px 0;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  min-height: 50px;
}

.day-cell:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.day-cell.other-month {
  opacity: 0.3;
  cursor: default;
}

.day-cell.other-month:hover {
  transform: none;
  box-shadow: none;
}

.day-cell.today {
  border: 2px solid #409eff;
}

.day-number {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.day-cell.has-checkin .day-number {
  color: #fff;
  font-weight: 600;
}

.day-duration {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 2px;
}

.empty-day {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-day p {
  margin-top: 12px;
  font-size: 14px;
}

.day-checkin-list {
  max-height: 500px;
  overflow-y: auto;
}

.day-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 16px;
  color: #fff;
}

.summary-item {
  text-align: center;
  flex: 1;
}

.summary-num {
  font-size: 28px;
  font-weight: bold;
}

.summary-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}

.summary-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

.checkin-item {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}

.checkin-item:last-child {
  margin-bottom: 0;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.item-type {
  display: flex;
  align-items: center;
  gap: 10px;
}

.type-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.item-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.item-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
}

.item-stats .stat {
  font-size: 13px;
  color: #606266;
}

.item-stats .stat b {
  color: #409eff;
  font-size: 16px;
  margin-right: 4px;
}

.stat-divider {
  color: #dcdfe6;
}

.item-note {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.note-label {
  color: #909399;
}

.item-images {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.item-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

@media (max-width: 768px) {
  .day-cell {
    min-height: 40px;
  }
  
  .day-number {
    font-size: 12px;
  }
  
  .day-duration {
    font-size: 9px;
  }
  
  .calendar-legend {
    justify-content: center;
  }
}
</style>
