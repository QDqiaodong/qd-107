<template>
  <div class="home-page">
    <h1 class="page-title">运动动态</h1>
    
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <el-icon :size="28"><Trophy /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ checkinStore.checkinCount }}</div>
          <div class="stat-label">累计打卡</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ checkinStore.totalDuration }}</div>
          <div class="stat-label">总时长(分钟)</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <el-icon :size="28"><List /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ checkinStore.planCount }}</div>
          <div class="stat-label">进行中计划</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
          <el-icon :size="28"><Sunny /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ checkinStore.weekCheckins.length }}</div>
          <div class="stat-label">本周打卡</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <span class="section-title" style="margin-bottom: 0;">运动类型筛选</span>
        <div class="filter-tags">
          <el-tag
            v-for="type in filterTypes"
            :key="type.value"
            :type="activeFilter === type.value ? 'primary' : 'info'"
            effect="plain"
            style="cursor: pointer; margin-right: 8px;"
            @click="activeFilter = type.value"
          >
            {{ type.label }}
          </el-tag>
        </div>
      </div>
    </div>

    <InfiniteScrollList
      :list="list"
      :loading="loading"
      :error="error"
      :finished="finished"
      class="checkin-scroll"
      @load="loadMore"
    >
      <template #default="{ list }">
        <div v-for="item in list" :key="item.id" class="card checkin-card">
          <div class="checkin-header">
            <div class="checkin-type">
              <div class="type-icon" :style="{ background: getSportTypeInfo(item.type).color + '20' }">
                <el-icon :size="24" :color="getSportTypeInfo(item.type).color">
                  <component :is="getSportTypeIcon(item.type)" />
                </el-icon>
              </div>
              <div>
                <div class="type-name">{{ item.typeName }}</div>
                <div class="checkin-time">{{ timeAgo(item.createTime) }}</div>
              </div>
            </div>
            <el-button type="danger" text @click="handleDelete(item.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          
          <div class="checkin-stats">
            <div class="stat-item">
              <div class="stat-num">{{ item.duration }}</div>
              <div class="stat-text">分钟</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-num">{{ item.amount }}</div>
              <div class="stat-text">{{ item.amountUnit }}</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ item.statusText }}
              </el-tag>
            </div>
          </div>
          
          <div v-if="item.note" class="checkin-note">
            <span class="note-label">备注：</span>{{ item.note }}
          </div>
          
          <div v-if="item.images && item.images.length > 0" class="checkin-images">
            <img v-for="(img, idx) in item.images" :key="idx" :src="img" class="checkin-img" />
          </div>
        </div>
      </template>

      <template #empty>
        <div class="card empty-state">
          <el-icon><Document /></el-icon>
          <p>暂无运动记录</p>
          <el-button type="primary" @click="$router.push('/checkin')">
            立即打卡
          </el-button>
        </div>
      </template>
    </InfiniteScrollList>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCheckinStore } from '@/stores/checkin'
import { sportTypes, getSportTypeInfo, timeAgo } from '@/utils/common'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import InfiniteScrollList from '@/components/InfiniteScrollList.vue'

const router = useRouter()
const checkinStore = useCheckinStore()

const filterTypes = [{ value: 'all', label: '全部' }, ...sportTypes]
const activeFilter = ref('all')

const fetchCheckins = (page, pageSize) => {
  return checkinStore.getCheckinsByPage(page, pageSize, activeFilter.value)
}

const {
  list,
  loading,
  error,
  finished,
  total,
  loadMore,
  refresh
} = useInfiniteScroll(fetchCheckins, {
  pageSize: 10,
  immediate: false
})

watch(activeFilter, () => {
  refresh()
}, { immediate: true })

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

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条打卡记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    checkinStore.deleteCheckin(id)
    refresh()
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
}

.checkin-scroll {
  height: calc(100vh - 380px);
  min-height: 400px;
  margin-top: 16px;
}

.checkin-card {
  margin-bottom: 16px;
}

.checkin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.checkin-type {
  display: flex;
  align-items: center;
  gap: 12px;
}

.type-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.checkin-time {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.checkin-stats {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-num {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-text {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e4e7ed;
}

.checkin-note {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.note-label {
  color: #909399;
}

.checkin-images {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.checkin-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
  }
  
  .stat-value {
    font-size: 22px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
