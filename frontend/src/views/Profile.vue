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
import { useCheckinStore } from '@/stores/checkin'
import { sportTypes } from '@/utils/common'

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

const editForm = ref({
  nickname: '',
  height: 0,
  weight: 0,
  target: ''
})

watch(showEdit, (val) => {
  if (val) {
    editForm.value = { ...checkinStore.userInfo }
  }
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

@media (max-width: 768px) {
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
}
</style>
