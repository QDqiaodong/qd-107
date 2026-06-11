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

    <div class="card goal-card" v-if="checkinStore.monthGoalProgress" @click="goGoal">
      <div class="goal-card-header">
        <div class="goal-card-title">
          <el-icon :size="20" color="#ff6b6b"><Flag /></el-icon>
          <span>月度冲线目标</span>
        </div>
        <div class="goal-card-action">
          <span>查看详情</span>
          <el-icon :size="16"><ArrowRight /></el-icon>
        </div>
      </div>
      <div class="goal-card-progress">
        <div class="goal-progress-item">
          <div class="goal-progress-top">
            <span class="goal-progress-label">打卡次数</span>
            <span class="goal-progress-num">
              {{ checkinStore.monthGoalProgress.checkinCount.current }}/{{ checkinStore.monthGoalProgress.checkinCount.target }}
            </span>
          </div>
          <div class="goal-progress-bar">
            <div class="goal-progress-fill" :style="{ 
              width: checkinStore.monthGoalProgress.checkinCount.progress + '%',
              background: 'linear-gradient(90deg, #667eea, #764ba2)'
            }"></div>
          </div>
        </div>
        <div class="goal-progress-item">
          <div class="goal-progress-top">
            <span class="goal-progress-label">运动时长</span>
            <span class="goal-progress-num">
              {{ checkinStore.monthGoalProgress.totalDuration.current }}/{{ checkinStore.monthGoalProgress.totalDuration.target }}分钟
            </span>
          </div>
          <div class="goal-progress-bar">
            <div class="goal-progress-fill" :style="{ 
              width: checkinStore.monthGoalProgress.totalDuration.progress + '%',
              background: 'linear-gradient(90deg, #f093fb, #f5576c)'
            }"></div>
          </div>
        </div>
        <div class="goal-progress-item">
          <div class="goal-progress-top">
            <span class="goal-progress-label">消耗热量</span>
            <span class="goal-progress-num">
              {{ checkinStore.monthGoalProgress.totalCalorie.current }}/{{ checkinStore.monthGoalProgress.totalCalorie.target }}千卡
            </span>
          </div>
          <div class="goal-progress-bar">
            <div class="goal-progress-fill" :style="{ 
              width: checkinStore.monthGoalProgress.totalCalorie.progress + '%',
              background: 'linear-gradient(90deg, #fa709a, #fee140)'
            }"></div>
          </div>
        </div>
      </div>
      <div class="goal-card-footer">
        <span class="goal-remaining">
          还剩 <b>{{ checkinStore.monthGoalProgress.remainingDays }}</b> 天
        </span>
        <span class="goal-daily-tip">
          每天需 {{ checkinStore.monthGoalProgress.checkinCount.dailyNeeded }} 次打卡
        </span>
      </div>
    </div>

    <div class="card goal-card goal-empty-card" v-else @click="goGoal">
      <div class="goal-empty-content">
        <el-icon :size="32" color="#c0c4cc"><Flag /></el-icon>
        <div class="goal-empty-text">
          <span class="goal-empty-title">设置月度冲线目标</span>
          <span class="goal-empty-desc">设定目标，让运动更有动力</span>
        </div>
        <el-button type="primary" size="small">
          去设置
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="card hot-types-card">
      <div class="hot-types-header">
        <el-icon :size="20" color="#f56c6c"><HotWater /></el-icon>
        <span class="section-title" style="margin-bottom: 0;">热门运动</span>
        <span class="hot-tip">实时热度 · 智能排序</span>
      </div>
      <div v-if="hotTypes.length > 0" class="hot-types-grid">
        <div
          v-for="(type, index) in hotTypes"
          :key="type.id"
          class="hot-type-item"
          @click="goCheckin(type)"
        >
          <div class="hot-rank" :class="{ 'top3': index < 3 }">{{ index + 1 }}</div>
          <div class="type-icon" :style="{ background: getSportTypeInfo(type.icon).color + '20' }">
            <el-icon :size="24" :color="getSportTypeInfo(type.icon).color">
              <component :is="getSportTypeIcon(type.icon)" />
            </el-icon>
          </div>
          <div class="type-info">
            <div class="type-name">{{ type.name }}</div>
            <div class="type-hot">热度 {{ formatHotCount(type.hotCount) }}</div>
          </div>
          <el-icon class="go-icon" :size="16" color="#909399"><ArrowRight /></el-icon>
        </div>
      </div>
      <div v-else class="hot-types-empty">
        <el-icon :size="32" color="#c0c4cc"><TrendCharts /></el-icon>
        <p>暂无热门运动数据</p>
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

          <div v-if="item.muscleTags && item.muscleTags.length > 0" class="checkin-muscle-tags">
            <span class="muscle-tags-label">肌感：</span>
            <el-tag
              v-for="tag in getMuscleTagInfos(item.muscleTags)"
              :key="tag.value"
              size="small"
              effect="light"
              class="muscle-tag-item"
              :style="{ borderColor: tag.color, color: tag.color, backgroundColor: tag.color + '10' }"
            >
              {{ tag.emoji }} {{ tag.label }}
            </el-tag>
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
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCheckinStore } from '@/stores/checkin'
import { sportTypes, getSportTypeInfo, muscleTags, timeAgo } from '@/utils/common'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import { sportTypeApi } from '@/api'
import InfiniteScrollList from '@/components/InfiniteScrollList.vue'

const router = useRouter()
const checkinStore = useCheckinStore()

const filterTypes = [{ value: 'all', label: '全部' }, ...sportTypes]
const activeFilter = ref('all')
const hotTypes = ref([])

const loadHotTypes = async () => {
  try {
    const res = await sportTypeApi.getHotTypes()
    if (res.code === 200) {
      hotTypes.value = res.data || []
    }
  } catch (e) {
    console.error('加载热门运动类型失败', e)
  }
}

const formatHotCount = (count) => {
  if (!count) return 0
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count
}

const goCheckin = (type) => {
  router.push({
    path: '/checkin',
    query: { type: type.icon }
  })
}

const goGoal = () => {
  router.push('/goal')
}

onMounted(() => {
  loadHotTypes()
})

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

const getMuscleTagInfos = (tagValues) => {
  if (!tagValues || tagValues.length === 0) return []
  return tagValues.map(v => muscleTags.find(t => t.value === v)).filter(Boolean)
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

.goal-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.goal-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.goal-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.goal-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.goal-card-action {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  transition: all 0.2s ease;
}

.goal-card:hover .goal-card-action {
  color: #409eff;
  transform: translateX(4px);
}

.goal-card-progress {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 16px;
}

.goal-progress-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.goal-progress-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goal-progress-label {
  font-size: 13px;
  color: #606266;
}

.goal-progress-num {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.goal-progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.goal-progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.goal-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #606266;
}

.goal-remaining b {
  color: #f59e0b;
  font-size: 15px;
  margin: 0 2px;
}

.goal-daily-tip {
  color: #909399;
}

.goal-empty-card {
  cursor: pointer;
}

.goal-empty-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.goal-empty-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.goal-empty-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.goal-empty-desc {
  font-size: 13px;
  color: #909399;
}

.hot-types-card {
  margin-bottom: 16px;
}

.hot-types-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.hot-types-header .section-title {
  margin: 0;
}

.hot-tip {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 12px;
}

.hot-types-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.hot-type-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  border: 1px solid transparent;
}

.hot-type-item:hover {
  background: #fff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.hot-rank {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: #e4e7ed;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.hot-rank.top3 {
  background: linear-gradient(135deg, #ff6b6b, #feca57);
  color: #fff;
}

.hot-type-item .type-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hot-type-item .type-info {
  flex: 1;
  min-width: 0;
}

.hot-type-item .type-info .type-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.hot-type-item .type-info .type-hot {
  font-size: 12px;
  color: #909399;
}

.hot-type-item .go-icon {
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.hot-type-item:hover .go-icon {
  transform: translateX(4px);
  color: #409eff;
}

.hot-types-empty {
  text-align: center;
  padding: 32px 0;
  color: #909399;
}

.hot-types-empty p {
  margin-top: 8px;
  font-size: 14px;
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

.checkin-muscle-tags {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 6px;
}

.muscle-tags-label {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
  margin-right: 2px;
}

.muscle-tag-item {
  border-width: 1px;
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
  
  .hot-types-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .hot-type-item {
    padding: 10px;
    gap: 8px;
  }
  
  .hot-type-item .type-icon {
    width: 40px;
    height: 40px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .goal-card {
    margin-bottom: 16px;
  }
  
  .goal-empty-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
}
</style>
