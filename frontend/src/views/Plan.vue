<template>
  <div class="plan-page">
    <div class="page-header">
      <h1 class="page-title">运动计划</h1>
      <el-button type="primary" @click="showDialog = true">
        <el-icon><Plus /></el-icon>
        新建计划
      </el-button>
    </div>

    <div class="plan-tabs">
      <el-tabs v-model="activeTab" class="plan-tabs-content">
        <el-tab-pane label="进行中" name="active">
          <div v-if="activePlans.length === 0" class="card empty-state">
            <el-icon><Document /></el-icon>
            <p>暂无进行中的计划</p>
            <el-button type="primary" @click="showDialog = true">
              创建第一个计划
            </el-button>
          </div>
          
          <div v-for="plan in activePlans" :key="plan.id" class="card plan-card">
            <div class="plan-header">
              <div class="plan-type" :style="{ background: getSportTypeInfo(plan.type).color + '20' }">
                <el-icon :size="20" :color="getSportTypeInfo(plan.type).color">
                  <component :is="getSportTypeIcon(plan.type)" />
                </el-icon>
              </div>
              <div class="plan-info">
                <div class="plan-title">{{ plan.title }}</div>
                <div class="plan-meta">
                  <el-tag size="small" :type="getTagType(plan.type)">
                    {{ plan.typeName }}
                  </el-tag>
                  <span class="plan-frequency">
                    <el-icon><Clock /></el-icon>
                    {{ plan.frequency }}
                  </span>
                </div>
              </div>
              <div class="plan-actions">
                <el-button type="success" text @click="handleToggle(plan.id)">
                  <el-icon><Check /></el-icon>
                  完成
                </el-button>
                <el-button type="danger" text @click="handleDelete(plan.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="plan-target">
              <span class="target-label">目标：</span>{{ plan.target }}
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="已完成" name="completed">
          <div v-if="completedPlans.length === 0" class="card empty-state">
            <el-icon><CircleCheck /></el-icon>
            <p>暂无已完成的计划</p>
          </div>
          
          <div v-for="plan in completedPlans" :key="plan.id" class="card plan-card completed">
            <div class="plan-header">
              <div class="plan-type" style="background: #e1f3d8;">
                <el-icon :size="20" color="#67c23a">
                  <Check />
                </el-icon>
              </div>
              <div class="plan-info">
                <div class="plan-title" style="text-decoration: line-through; color: #909399;">
                  {{ plan.title }}
                </div>
                <div class="plan-meta">
                  <el-tag size="small" type="success">
                    {{ plan.typeName }}
                  </el-tag>
                  <span class="plan-frequency">
                    <el-icon><Clock /></el-icon>
                    {{ plan.frequency }}
                  </span>
                </div>
              </div>
              <div class="plan-actions">
                <el-button type="primary" text @click="handleToggle(plan.id)">
                  恢复
                </el-button>
                <el-button type="danger" text @click="handleDelete(plan.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="plan-target" style="color: #c0c4cc;">
              <span class="target-label">目标：</span>{{ plan.target }}
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog
      v-model="showDialog"
      title="新建运动计划"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="planForm" label-width="80px" ref="planFormRef">
        <el-form-item label="计划名称" prop="title" :rules="[{ required: true, message: '请输入计划名称', trigger: 'blur' }]">
          <el-input v-model="planForm.title" placeholder="例如：每周跑步3次" />
        </el-form-item>
        <el-form-item label="运动类型" prop="type" :rules="[{ required: true, message: '请选择运动类型', trigger: 'change' }]">
          <el-select v-model="planForm.type" placeholder="请选择" style="width: 100%;" @change="handleTypeChange">
            <el-option
              v-for="type in sportTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="运动目标" prop="target" :rules="[{ required: true, message: '请输入运动目标', trigger: 'blur' }]">
          <el-input v-model="planForm.target" type="textarea" :rows="2" placeholder="描述你的运动目标" />
        </el-form-item>
        <el-form-item label="频率" prop="frequency" :rules="[{ required: true, message: '请输入执行频率', trigger: 'blur' }]">
          <el-input v-model="planForm.frequency" placeholder="例如：每周一、三、五" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox, ElForm } from 'element-plus'
import { useCheckinStore } from '@/stores/checkin'
import { sportTypes, getSportTypeInfo } from '@/utils/common'

const checkinStore = useCheckinStore()
const activeTab = ref('active')
const showDialog = ref(false)
const planFormRef = ref()

const planForm = ref({
  title: '',
  type: '',
  typeName: '',
  target: '',
  frequency: ''
})

const activePlans = computed(() => checkinStore.plans.filter(p => !p.completed))
const completedPlans = computed(() => checkinStore.plans.filter(p => p.completed))

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

const getTagType = (type) => {
  const map = {
    running: 'success',
    cycling: 'primary',
    swimming: 'danger',
    yoga: 'warning',
    gym: 'success',
    other: 'info'
  }
  return map[type] || 'info'
}

const handleTypeChange = (value) => {
  const typeInfo = sportTypes.find(t => t.value === value)
  if (typeInfo) {
    planForm.value.typeName = typeInfo.label
  }
}

const handleSubmit = async () => {
  if (!planFormRef.value) return
  
  await planFormRef.value.validate((valid) => {
    if (valid) {
      checkinStore.addPlan({ ...planForm.value })
      ElMessage.success('创建成功')
      showDialog.value = false
      planForm.value = {
        title: '',
        type: '',
        typeName: '',
        target: '',
        frequency: ''
      }
    }
  })
}

const handleToggle = (id) => {
  checkinStore.togglePlan(id)
  ElMessage.success('状态已更新')
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个计划吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    checkinStore.deletePlan(id)
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.plan-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.plan-tabs-content {
  --el-tabs-header-height: 50px;
}

.plan-card {
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.plan-card.completed {
  opacity: 0.8;
}

.plan-header {
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

.plan-actions {
  display: flex;
  gap: 8px;
}

.plan-target {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.target-label {
  color: #909399;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .plan-header {
    flex-wrap: wrap;
  }
  
  .plan-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
