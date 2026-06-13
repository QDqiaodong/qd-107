<template>
  <div class="profile-page">
    <h1 class="page-title">个人中心</h1>
    
    <div class="card user-card">
      <div class="user-avatar">
        <el-avatar :size="80" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <el-icon :size="40"><User /></el-icon>
        </el-avatar>
      </div>
      <div class="user-info">
        <div class="user-name">{{ checkinStore.userInfo.nickname }}</div>
        <div class="user-desc">{{ checkinStore.userInfo.target }}</div>
        <div class="user-stats">
          <div class="stat">
            <div class="num">{{ checkinStore.checkinCount }}</div>
            <div class="label">累计打卡</div>
          </div>
          <div class="stat">
            <div class="num">{{ checkinStore.totalDuration }}</div>
            <div class="label">总时长(分)</div>
          </div>
          <div class="stat">
            <div class="num">{{ checkinStore.weekCheckins.length }}</div>
            <div class="label">本周打卡</div>
          </div>
        </div>
      </div>
      <el-button type="primary" @click="showEdit = true">
        <el-icon><Edit /></el-icon>
        编辑资料
      </el-button>
    </div>

    <div class="card">
      <h3 class="section-title">打卡日历</h3>
      <CalendarHeatmap :checkins="checkinStore.checkins" />
    </div>

    <div class="card">
      <h3 class="section-title">最佳记录</h3>
      <div class="best-records-grid">
        <div 
          class="best-record-card record-duration"
          @click="openRecordDetail('duration')"
        >
          <div class="record-icon">
            <el-icon :size="28"><Timer /></el-icon>
          </div>
          <div class="record-content">
            <div class="record-value">
              <span class="record-num">{{ checkinStore.maxDurationRecord?.duration || 0 }}</span>
              <span class="record-unit">分钟</span>
            </div>
            <div class="record-label">最长单次时长</div>
            <div class="record-date" v-if="checkinStore.maxDurationRecord">
              {{ formatRecordDate(checkinStore.maxDurationRecord.createTime) }}
            </div>
          </div>
          <el-icon class="record-arrow" :size="16"><ArrowRight /></el-icon>
        </div>

        <div 
          class="best-record-card record-calorie"
          @click="openRecordDetail('calorie')"
        >
          <div class="record-icon">
            <el-icon :size="28"><HotWater /></el-icon>
          </div>
          <div class="record-content">
            <div class="record-value">
              <span class="record-num">{{ maxCalorieValue }}</span>
              <span class="record-unit">千卡</span>
            </div>
            <div class="record-label">最大单次热量</div>
            <div class="record-date" v-if="checkinStore.maxCalorieRecord">
              {{ formatRecordDate(checkinStore.maxCalorieRecord.createTime) }}
            </div>
          </div>
          <el-icon class="record-arrow" :size="16"><ArrowRight /></el-icon>
        </div>

        <div 
          class="best-record-card record-distance"
          @click="openRecordDetail('distance')"
        >
          <div class="record-icon">
            <el-icon :size="28"><Location /></el-icon>
          </div>
          <div class="record-content">
            <div class="record-value">
              <span class="record-num">{{ checkinStore.maxDistanceRecord?.amount || 0 }}</span>
              <span class="record-unit">{{ checkinStore.maxDistanceRecord?.amountUnit || '公里' }}</span>
            </div>
            <div class="record-label">最远距离</div>
            <div class="record-date" v-if="checkinStore.maxDistanceRecord">
              {{ formatRecordDate(checkinStore.maxDistanceRecord.createTime) }}
            </div>
          </div>
          <el-icon class="record-arrow" :size="16"><ArrowRight /></el-icon>
        </div>

        <div 
          class="best-record-card record-streak"
          @click="openRecordDetail('streak')"
        >
          <div class="record-icon">
            <el-icon :size="28"><Medal /></el-icon>
          </div>
          <div class="record-content">
            <div class="record-value">
              <span class="record-num">{{ checkinStore.maxStreakDays.days }}</span>
              <span class="record-unit">天</span>
            </div>
            <div class="record-label">最长连续打卡</div>
            <div class="record-date" v-if="checkinStore.maxStreakDays.days > 0">
              共 {{ checkinStore.maxStreakDays.records.length }} 次训练
            </div>
          </div>
          <el-icon class="record-arrow" :size="16"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="chart-header">
        <h3 class="section-title" style="margin-bottom: 0;">数据统计</h3>
        <el-radio-group v-model="statsType" size="small">
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
        </el-radio-group>
      </div>
      <div class="chart-container">
        <v-chart class="chart" :option="chartOption" autoresize />
      </div>
    </div>

    <div class="card">
      <h3 class="section-title">运动类型分布</h3>
      <div class="chart-container" style="height: 300px;">
        <v-chart class="chart" :option="pieOption" autoresize />
      </div>
    </div>

    <div class="card">
      <div class="compare-header">
        <h3 class="section-title" style="margin-bottom: 0;">打卡照片对照板</h3>
        <el-tag v-if="selectedRecords.length === 1" type="warning" size="small">
          请再选择一条记录进行对比
        </el-tag>
        <el-tag v-if="selectedRecords.length === 2" type="success" size="small">
          已选择2条记录
        </el-tag>
        <el-button 
          v-if="selectedRecords.length > 0" 
          size="small" 
          text 
          @click="clearSelection"
        >
          清除选择
        </el-button>
      </div>

      <div class="compare-filter">
        <span class="filter-label">选择运动类型：</span>
        <el-radio-group v-model="compareSportType" size="small">
          <el-radio-button 
            v-for="type in compareSportTypes" 
            :key="type.value" 
            :label="type.value"
          >
            {{ type.label }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="filteredCheckins.length === 0" class="empty-compare">
        <el-icon><Picture /></el-icon>
        <p>该类型暂无打卡记录</p>
      </div>

      <div v-else class="compare-thumb-list">
        <div 
          v-for="item in filteredCheckins" 
          :key="item.id"
          class="compare-thumb-item"
          :class="{ selected: isSelected(item.id), 'selectable': selectedRecords.length < 2 || isSelected(item.id) }"
          @click="toggleRecordSelection(item)"
        >
          <div class="thumb-image-wrapper">
            <img 
              v-if="item.images && item.images.length > 0" 
              :src="item.images[0]" 
              class="thumb-image" 
            />
            <div v-else class="thumb-placeholder">
              <el-icon :size="32"><Camera /></el-icon>
            </div>
            <div v-if="isSelected(item.id)" class="selected-badge">
              <el-icon><CircleCheck /></el-icon>
              <span>{{ getSelectOrder(item.id) }}</span>
            </div>
          </div>
          <div class="thumb-info">
            <div class="thumb-date">{{ formatShortDate(item.createTime) }}</div>
            <div class="thumb-stats">
              <span>{{ item.duration }}分钟</span>
              <span>{{ item.amount }}{{ item.amountUnit }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="selectedRecords.length === 2" class="compare-detail">
        <div class="compare-divider">
          <el-icon :size="24" color="#409eff"><Sort /></el-icon>
        </div>
        
        <div class="compare-columns">
          <div class="compare-column">
            <div class="compare-record-header">
              <el-tag size="small" type="primary">记录 1</el-tag>
              <span class="compare-date">{{ formatShortDate(selectedRecords[0].createTime) }}</span>
            </div>
            <div class="compare-images">
              <el-image 
                v-for="(img, idx) in selectedRecords[0].images" 
                :key="idx"
                :src="img" 
                class="compare-image"
                :preview-src-list="selectedRecords[0].images"
                :initial-index="idx"
                fit="cover"
              />
              <div v-if="!selectedRecords[0].images || selectedRecords[0].images.length === 0" class="no-images">
                <el-icon :size="48"><Picture /></el-icon>
                <span>暂无照片</span>
              </div>
            </div>
            <div class="compare-stats-grid">
              <div class="compare-stat">
                <div class="compare-stat-label">运动时长</div>
                <div class="compare-stat-value">{{ selectedRecords[0].duration }} 分钟</div>
              </div>
              <div class="compare-stat">
                <div class="compare-stat-label">运动量</div>
                <div class="compare-stat-value">{{ selectedRecords[0].amount }} {{ selectedRecords[0].amountUnit }}</div>
              </div>
              <div class="compare-stat">
                <div class="compare-stat-label">身体状态</div>
                <div class="compare-stat-value">{{ selectedRecords[0].statusText }}</div>
              </div>
              <div v-if="selectedRecords[0].note" class="compare-stat full-width">
                <div class="compare-stat-label">备注</div>
                <div class="compare-stat-value note">{{ selectedRecords[0].note }}</div>
              </div>
            </div>
          </div>

          <div class="compare-column">
            <div class="compare-record-header">
              <el-tag size="small" type="success">记录 2</el-tag>
              <span class="compare-date">{{ formatShortDate(selectedRecords[1].createTime) }}</span>
            </div>
            <div class="compare-images">
              <el-image 
                v-for="(img, idx) in selectedRecords[1].images" 
                :key="idx"
                :src="img" 
                class="compare-image"
                :preview-src-list="selectedRecords[1].images"
                :initial-index="idx"
                fit="cover"
              />
              <div v-if="!selectedRecords[1].images || selectedRecords[1].images.length === 0" class="no-images">
                <el-icon :size="48"><Picture /></el-icon>
                <span>暂无照片</span>
              </div>
            </div>
            <div class="compare-stats-grid">
              <div class="compare-stat">
                <div class="compare-stat-label">运动时长</div>
                <div class="compare-stat-value">{{ selectedRecords[1].duration }} 分钟</div>
                <div class="compare-stat-diff" :class="getDiffClass(selectedRecords[1].duration, selectedRecords[0].duration)">
                  {{ getDiffText(selectedRecords[1].duration, selectedRecords[0].duration, '分钟') }}
                </div>
              </div>
              <div class="compare-stat">
                <div class="compare-stat-label">运动量</div>
                <div class="compare-stat-value">{{ selectedRecords[1].amount }} {{ selectedRecords[1].amountUnit }}</div>
                <div class="compare-stat-diff" :class="getDiffClass(selectedRecords[1].amount, selectedRecords[0].amount)">
                  {{ getDiffText(selectedRecords[1].amount, selectedRecords[0].amount, selectedRecords[1].amountUnit) }}
                </div>
              </div>
              <div class="compare-stat">
                <div class="compare-stat-label">身体状态</div>
                <div class="compare-stat-value">{{ selectedRecords[1].statusText }}</div>
              </div>
              <div v-if="selectedRecords[1].note" class="compare-stat full-width">
                <div class="compare-stat-label">备注</div>
                <div class="compare-stat-value note">{{ selectedRecords[1].note }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showEdit" title="编辑资料" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="身高(cm)">
          <el-input-number v-model="editForm.height" :min="100" :max="250" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="editForm.weight" :min="30" :max="200" :step="0.1" />
        </el-form-item>
        <el-form-item label="目标">
          <el-input v-model="editForm.target" placeholder="例如：减肥、增肌、保持健康" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRecordDetail" :title="detailTitle" width="600px" class="record-detail-dialog">
      <div v-if="detailType === 'streak'" class="streak-detail">
        <div class="streak-summary">
          <div class="streak-badge">
            <el-icon :size="32" color="#f59e0b"><Medal /></el-icon>
          </div>
          <div class="streak-info">
            <div class="streak-days">{{ checkinStore.maxStreakDays.days }} 天</div>
            <div class="streak-label">连续打卡</div>
            <div class="streak-range">
              {{ formatStreakStart(checkinStore.maxStreakDays) }} - {{ formatStreakEnd(checkinStore.maxStreakDays) }}
            </div>
          </div>
        </div>
        <div class="streak-timeline">
          <div 
            v-for="record in checkinStore.maxStreakDays.records" 
            :key="record.id" 
            class="timeline-item"
          >
            <div class="timeline-dot" :style="{ background: getSportTypeInfo(record.type).color }"></div>
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="timeline-type" :style="{ color: getSportTypeInfo(record.type).color }">
                  {{ record.typeName }}
                </span>
                <span class="timeline-time">{{ formatRecordDate(record.createTime) }}</span>
              </div>
              <div class="timeline-stats">
                <span>{{ record.duration }} 分钟</span>
                <span>{{ record.amount }} {{ record.amountUnit }}</span>
                <el-tag :type="getStatusType(record.status)" size="small">
                  {{ record.statusText }}
                </el-tag>
              </div>
              <div v-if="record.note" class="timeline-note">{{ record.note }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="currentDetailRecord" class="single-record-detail">
        <div class="detail-header">
          <div class="detail-type-icon" :style="{ background: getSportTypeInfo(currentDetailRecord.type).color + '20' }">
            <el-icon :size="28" :color="getSportTypeInfo(currentDetailRecord.type).color">
              <component :is="getSportTypeIcon(currentDetailRecord.type)" />
            </el-icon>
          </div>
          <div class="detail-type-info">
            <div class="detail-type-name">{{ currentDetailRecord.typeName }}</div>
            <div class="detail-time">{{ formatRecordDate(currentDetailRecord.createTime) }}</div>
          </div>
        </div>

        <div class="detail-stats">
          <div class="detail-stat">
            <div class="detail-stat-value">{{ currentDetailRecord.duration }}</div>
            <div class="detail-stat-label">运动时长(分钟)</div>
          </div>
          <div class="detail-stat">
            <div class="detail-stat-value">{{ currentDetailRecord.amount }}</div>
            <div class="detail-stat-label">运动量({{ currentDetailRecord.amountUnit }})</div>
          </div>
          <div class="detail-stat">
            <div class="detail-stat-value">{{ calculateCalorie(currentDetailRecord) }}</div>
            <div class="detail-stat-label">消耗热量(千卡)</div>
          </div>
        </div>

        <div class="detail-status">
          <span class="detail-status-label">身体状态：</span>
          <el-tag :type="getStatusType(currentDetailRecord.status)" size="large">
            {{ currentDetailRecord.statusText }}
          </el-tag>
        </div>

        <div v-if="currentDetailRecord.muscleTags && currentDetailRecord.muscleTags.length > 0" class="detail-muscle-tags">
          <span class="detail-tags-label">肌感体验：</span>
          <el-tag
            v-for="tag in getMuscleTagInfos(currentDetailRecord.muscleTags)"
            :key="tag.value"
            size="small"
            effect="light"
            class="detail-tag-item"
            :style="{ borderColor: tag.color, color: tag.color, backgroundColor: tag.color + '10' }"
          >
            {{ tag.emoji }} {{ tag.label }}
          </el-tag>
        </div>

        <div v-if="currentDetailRecord.note" class="detail-note">
          <div class="detail-note-label">备注</div>
          <div class="detail-note-content">{{ currentDetailRecord.note }}</div>
        </div>

        <div v-if="currentDetailRecord.images && currentDetailRecord.images.length > 0" class="detail-images">
          <div class="detail-images-label">训练照片</div>
          <div class="detail-images-grid">
            <el-image
              v-for="(img, idx) in currentDetailRecord.images"
              :key="idx"
              :src="img"
              :preview-src-list="currentDetailRecord.images"
              :initial-index="idx"
              fit="cover"
              class="detail-image"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import {
  Timer,
  HotWater,
  Location,
  Medal,
  ArrowRight,
  User,
  Edit,
  Picture,
  Camera,
  CircleCheck,
  Sort
} from '@element-plus/icons-vue'
import { useCheckinStore } from '@/stores/checkin'
import { sportTypes, getSportTypeInfo, muscleTags, formatDate } from '@/utils/common'
import CalendarHeatmap from '@/components/CalendarHeatmap.vue'

use([
  CanvasRenderer,
  BarChart,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const checkinStore = useCheckinStore()
const statsType = ref('week')
const showEdit = ref(false)
const compareSportType = ref('running')
const selectedRecords = ref([])
const showRecordDetail = ref(false)
const detailType = ref('')
const currentDetailRecord = ref(null)

const editForm = ref({
  nickname: '',
  height: 0,
  weight: 0,
  target: ''
})

const maxCalorieValue = computed(() => {
  const record = checkinStore.maxCalorieRecord
  if (!record) return 0
  return Math.round((record.duration || 0) * (record.amount || 0) * 0.1)
})

const detailTitle = computed(() => {
  const titles = {
    duration: '最长单次时长记录',
    calorie: '最大单次热量记录',
    distance: '最远距离记录',
    streak: '最长连续打卡记录'
  }
  return titles[detailType.value] || '记录详情'
})

const compareSportTypes = computed(() => {
  const availableTypes = new Set(checkinStore.checkins.map(c => c.type))
  return sportTypes.filter(t => availableTypes.has(t.value))
})

const filteredCheckins = computed(() => {
  return checkinStore.checkins.filter(c => c.type === compareSportType.value)
})

const formatShortDate = (dateStr) => {
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

const formatRecordDate = (dateStr) => {
  return formatDate(dateStr, 'YYYY-MM-DD HH:mm')
}

const openRecordDetail = (type) => {
  detailType.value = type
  if (type === 'duration') {
    currentDetailRecord.value = checkinStore.maxDurationRecord
  } else if (type === 'calorie') {
    currentDetailRecord.value = checkinStore.maxCalorieRecord
  } else if (type === 'distance') {
    currentDetailRecord.value = checkinStore.maxDistanceRecord
  } else {
    currentDetailRecord.value = null
  }
  if (type === 'streak' || currentDetailRecord.value) {
    showRecordDetail.value = true
  }
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

const getMuscleTagInfos = (tagValues) => {
  if (!tagValues || tagValues.length === 0) return []
  return tagValues.map(v => muscleTags.find(t => t.value === v)).filter(Boolean)
}

const calculateCalorie = (record) => {
  if (!record) return 0
  return Math.round((record.duration || 0) * (record.amount || 0) * 0.1)
}

const formatStreakStart = (streakData) => {
  if (!streakData || !streakData.endDate || streakData.days === 0) return '-'
  const endDate = new Date(streakData.endDate)
  endDate.setDate(endDate.getDate() - (streakData.days - 1))
  return formatDate(endDate.toISOString(), 'YYYY-MM-DD')
}

const formatStreakEnd = (streakData) => {
  if (!streakData || !streakData.endDate) return '-'
  return formatDate(new Date(streakData.endDate).toISOString(), 'YYYY-MM-DD')
}

const isSelected = (id) => {
  return selectedRecords.value.some(r => r.id === id)
}

const getSelectOrder = (id) => {
  const index = selectedRecords.value.findIndex(r => r.id === id)
  return index > -1 ? index + 1 : ''
}

const toggleRecordSelection = (record) => {
  const index = selectedRecords.value.findIndex(r => r.id === record.id)
  if (index > -1) {
    selectedRecords.value.splice(index, 1)
  } else if (selectedRecords.value.length < 2) {
    selectedRecords.value.push(record)
  }
}

const clearSelection = () => {
  selectedRecords.value = []
}

const getDiffClass = (val1, val2) => {
  const diff = val1 - val2
  if (diff > 0) return 'diff-positive'
  if (diff < 0) return 'diff-negative'
  return 'diff-equal'
}

const getDiffText = (val1, val2, unit) => {
  const diff = val1 - val2
  if (diff > 0) return `↑ +${diff}${unit}`
  if (diff < 0) return `↓ ${diff}${unit}`
  return '持平'
}

watch(showEdit, (val) => {
  if (val) {
    editForm.value = { ...checkinStore.userInfo }
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

const saveProfile = () => {
  checkinStore.updateUserInfo(editForm.value)
  ElMessage.success('保存成功')
  showEdit.value = false
}

const generateWeekData = () => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const today = new Date()
  const currentDay = today.getDay()
  
  const data = new Array(7).fill(0)
  
  checkinStore.checkins.forEach(item => {
    const date = new Date(item.createTime)
    const dayOfWeek = date.getDay()
    const diffDays = Math.floor((today - date) / (1000 * 60 * 60 * 24))
    
    if (diffDays < 7) {
      data[dayOfWeek] += item.duration
    }
  })
  
  return {
    days,
    data
  }
}

const generateMonthData = () => {
  const daysInMonth = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate()
  const days = []
  const data = new Array(daysInMonth).fill(0)
  
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(`${i}日`)
  }
  
  checkinStore.checkins.forEach(item => {
    const date = new Date(item.createTime)
    if (date.getMonth() === new Date().getMonth() && date.getFullYear() === new Date().getFullYear()) {
      const dayOfMonth = date.getDate() - 1
      data[dayOfMonth] += item.duration
    }
  })
  
  return {
    days,
    data
  }
}

const chartOption = computed(() => {
  const statsData = statsType.value === 'week' ? generateWeekData() : generateMonthData()
  
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      textStyle: {
        color: '#303133'
      },
      formatter: '{b}<br/>运动时长: {c} 分钟'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: statsData.days,
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      },
      axisLabel: {
        color: '#606266',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '分钟',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      },
      axisLabel: {
        color: '#606266'
      }
    },
    series: [
      {
        name: '运动时长',
        type: 'bar',
        data: statsData.data,
        barWidth: statsType.value === 'week' ? '40%' : '60%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ]
          }
        }
      }
    ]
  }
})

const pieOption = computed(() => {
  const typeData = {}
  
  checkinStore.checkins.forEach(item => {
    if (!typeData[item.type]) {
      typeData[item.type] = { value: 0, name: item.typeName }
    }
    typeData[item.type].value += 1
  })
  
  const data = Object.values(typeData)
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}次 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: '#606266'
      }
    },
    series: [
      {
        name: '运动类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data.length > 0 ? data : [{ value: 1, name: '暂无数据' }],
        color: data.length > 0 
          ? data.map(item => {
              const type = sportTypes.find(t => t.label === item.name)
              return type ? type.color : '#909399'
            })
          : ['#c0c4cc']
      }
    ]
  }
})
</script>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 200px;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.user-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
}

.user-stats {
  display: flex;
  gap: 32px;
}

.stat {
  text-align: center;
}

.stat .num {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat .label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.chart-container {
  height: 350px;
}

.chart {
  width: 100%;
  height: 100%;
}

.compare-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.compare-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 14px;
  color: #606266;
}

.empty-compare {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #c0c4cc;
}

.empty-compare .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-compare p {
  font-size: 14px;
  margin: 0;
}

.compare-thumb-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}

.compare-thumb-item {
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
}

.compare-thumb-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.compare-thumb-item.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}

.compare-thumb-item:not(.selectable) {
  opacity: 0.5;
  cursor: not-allowed;
}

.thumb-image-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
}

.thumb-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
}

.selected-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 8px;
  background: #409eff;
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.selected-badge .el-icon {
  font-size: 14px;
}

.thumb-info {
  padding: 8px 10px;
  background: #fafafa;
}

.thumb-date {
  font-size: 12px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 2px;
}

.thumb-stats {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #909399;
}

.compare-detail {
  position: relative;
  padding-top: 20px;
  border-top: 2px dashed #e4e7ed;
}

.compare-divider {
  position: absolute;
  top: -16px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 0 12px;
}

.compare-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.compare-column {
  background: #fafafa;
  border-radius: 12px;
  padding: 16px;
}

.compare-record-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.compare-date {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.compare-images {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.compare-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.compare-image:hover {
  transform: scale(1.02);
}

.no-images {
  grid-column: span 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #c0c4cc;
  background: #f5f7fa;
  border-radius: 8px;
}

.no-images .el-icon {
  margin-bottom: 8px;
}

.no-images span {
  font-size: 13px;
}

.compare-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.compare-stat {
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.compare-stat.full-width {
  grid-column: span 2;
}

.compare-stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.compare-stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.compare-stat-value.note {
  font-size: 14px;
  font-weight: normal;
  color: #606266;
  line-height: 1.5;
}

.compare-stat-diff {
  font-size: 12px;
  font-weight: 500;
  margin-top: 4px;
}

.diff-positive {
  color: #67c23a;
}

.diff-negative {
  color: #f56c6c;
}

.diff-equal {
  color: #909399;
}

.best-records-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.best-record-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.best-record-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.08;
  z-index: 0;
}

.best-record-card > * {
  position: relative;
  z-index: 1;
}

.best-record-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.best-record-card:hover .record-arrow {
  transform: translateX(4px);
}

.record-duration {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.record-duration::before {
  background: #fff;
}

.record-calorie {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.record-calorie::before {
  background: #fff;
}

.record-distance {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.record-distance::before {
  background: #fff;
}

.record-streak {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
}

.record-streak::before {
  background: #fff;
}

.record-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.record-content {
  flex: 1;
  min-width: 0;
}

.record-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  line-height: 1.2;
}

.record-num {
  font-size: 28px;
  font-weight: bold;
}

.record-unit {
  font-size: 14px;
  opacity: 0.9;
}

.record-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}

.record-date {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 2px;
}

.record-arrow {
  flex-shrink: 0;
  opacity: 0.8;
  transition: transform 0.3s ease;
}

.record-detail-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.single-record-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-type-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-type-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.detail-time {
  font-size: 14px;
  color: #909399;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.detail-stat {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 10px;
}

.detail-stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  line-height: 1.2;
}

.detail-stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.detail-status {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.detail-status-label {
  color: #606266;
}

.detail-muscle-tags {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 14px;
}

.detail-tags-label {
  color: #606266;
  flex-shrink: 0;
  margin-right: 2px;
}

.detail-tag-item {
  border-width: 1px;
}

.detail-note {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 16px;
}

.detail-note-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.detail-note-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.detail-images-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.detail-images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.detail-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  cursor: pointer;
}

.streak-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.streak-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 14px;
}

.streak-badge {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.2);
}

.streak-days {
  font-size: 36px;
  font-weight: bold;
  color: #92400e;
  line-height: 1.1;
}

.streak-label {
  font-size: 15px;
  color: #b45309;
  font-weight: 500;
  margin-top: 4px;
}

.streak-range {
  font-size: 13px;
  color: #92400e;
  margin-top: 6px;
  opacity: 0.8;
}

.streak-timeline {
  position: relative;
  padding-left: 28px;
}

.streak-timeline::before {
  content: '';
  position: absolute;
  left: 9px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: #e4e7ed;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -23px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #e4e7ed;
}

.timeline-content {
  background: #fafafa;
  border-radius: 10px;
  padding: 14px 16px;
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.timeline-type {
  font-size: 15px;
  font-weight: 600;
}

.timeline-time {
  font-size: 12px;
  color: #909399;
}

.timeline-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.timeline-note {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .best-records-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .best-record-card {
    padding: 16px;
  }

  .record-num {
    font-size: 22px;
  }

  .detail-stats {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .detail-stat {
    padding: 12px 8px;
  }

  .detail-stat-value {
    font-size: 20px;
  }

  .detail-images-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .streak-summary {
    padding: 16px;
    gap: 14px;
  }

  .streak-badge {
    width: 56px;
    height: 56px;
  }

  .streak-days {
    font-size: 28px;
  }
  .user-card {
    flex-direction: column;
    text-align: center;
  }
  
  .user-stats {
    justify-content: center;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .chart-container {
    height: 300px;
  }
  
  .compare-columns {
    grid-template-columns: 1fr;
  }
  
  .compare-thumb-list {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .compare-stats-grid {
    grid-template-columns: 1fr;
  }
  
  .compare-stat.full-width {
    grid-column: span 1;
  }
}
</style>
