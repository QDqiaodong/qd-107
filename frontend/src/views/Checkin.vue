<template>
  <div class="checkin-page">
    <h1 class="page-title">运动打卡</h1>
    
    <div class="card">
      <el-steps :active="activeStep" finish-status="success" align-center class="checkin-steps">
        <el-step title="选择运动类型" />
        <el-step title="填写运动数据" />
        <el-step title="身体状态" />
        <el-step title="图片上传" />
      </el-steps>

      <div class="step-content">
        <div v-show="activeStep === 0" class="step-panel">
          <h3 class="step-title">请选择运动类型</h3>
          <div class="type-grid">
            <div
              v-for="type in sportTypes"
              :key="type.value"
              class="type-item"
              :class="{ active: formData.type === type.value }"
              @click="selectType(type)"
            >
              <div class="type-icon" :style="{ background: type.color + '20' }">
                <el-icon :size="32" :color="type.color">
                  <component :is="getTypeIcon(type.value)" />
                </el-icon>
              </div>
              <div class="type-name">{{ type.label }}</div>
            </div>
          </div>
        </div>

        <div v-show="activeStep === 1" class="step-panel">
          <h3 class="step-title">填写运动数据</h3>
          <el-form :model="formData" label-width="100px" class="data-form">
            <el-form-item label="运动时长" required>
              <el-input-number
                v-model="formData.duration"
                :min="1"
                :max="600"
                :step="5"
                style="width: 200px;"
              />
              <span class="unit">分钟</span>
            </el-form-item>
            <el-form-item label="运动量" required>
              <el-input-number
                v-model="formData.amount"
                :min="0"
                :max="10000"
                :step="0.1"
                style="width: 200px;"
              />
              <span class="unit">{{ formData.amountUnit }}</span>
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                v-model="formData.note"
                type="textarea"
                :rows="3"
                placeholder="记录一下这次运动的感受..."
              />
              <div v-if="commonPhrases.length > 0" class="phrase-tags">
                <span class="phrase-tags-label">常用短语：</span>
                <el-tag
                  v-for="(phrase, index) in commonPhrases"
                  :key="index"
                  size="small"
                  effect="plain"
                  class="phrase-tag"
                  @click="insertPhrase(phrase)"
                >
                  {{ phrase }}
                </el-tag>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <div v-show="activeStep === 2" class="step-panel">
          <h3 class="step-title">身体状态如何？</h3>
          <div class="status-grid">
            <div
              v-for="status in bodyStatus"
              :key="status.value"
              class="status-item"
              :class="{ active: formData.status === status.value }"
              @click="selectStatus(status)"
            >
              <div class="status-emoji">
                {{ getStatusEmoji(status.value) }}
              </div>
              <div class="status-label" :style="{ color: status.color }">
                {{ status.label }}
              </div>
            </div>
          </div>
        </div>

        <div v-show="activeStep === 3" class="step-panel">
          <h3 class="step-title">上传运动照片（可选）</h3>
          <el-upload
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            :limit="4"
            accept="image/*"
            class="image-upload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传4张照片，支持 JPG、PNG 格式</div>
        </div>
      </div>

      <div class="step-actions">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 3" type="primary" @click="nextStep" :disabled="!canNext">
          下一步
        </el-button>
        <el-button v-if="activeStep === 3" type="success" @click="openMuscleTagDialog">
          完成打卡
        </el-button>
      </div>
    </div>

    <el-dialog
      v-model="muscleTagDialogVisible"
      title="记录一下你的肌感体验"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      class="muscle-tag-dialog"
    >
      <div class="muscle-tag-intro">
        <el-icon :size="20" color="#409eff"><ChatDotRound /></el-icon>
        <span>快速勾选体感词，让记录更生动</span>
      </div>
      
      <div class="muscle-tag-section">
        <div class="section-label">整体感受</div>
        <div class="tag-group">
          <div
            v-for="tag in overallTags"
            :key="tag.value"
            class="muscle-tag"
            :class="{ active: selectedTags.includes(tag.value) }"
            :style="{ '--tag-color': tag.color }"
            @click="toggleTag(tag.value)"
          >
            <span class="tag-emoji">{{ tag.emoji }}</span>
            <span class="tag-label">{{ tag.label }}</span>
          </div>
        </div>
      </div>

      <div class="muscle-tag-section">
        <div class="section-label">部位感受</div>
        <div class="tag-group">
          <div
            v-for="tag in bodyPartTags"
            :key="tag.value"
            class="muscle-tag"
            :class="{ active: selectedTags.includes(tag.value) }"
            :style="{ '--tag-color': tag.color }"
            @click="toggleTag(tag.value)"
          >
            <span class="tag-emoji">{{ tag.emoji }}</span>
            <span class="tag-label">{{ tag.label }}</span>
          </div>
        </div>
      </div>

      <div class="muscle-tag-summary">
        <div class="summary-label">已选标签：</div>
        <div class="selected-tags" v-if="selectedTagInfos.length > 0">
          <el-tag
            v-for="tag in selectedTagInfos"
            :key="tag.value"
            size="small"
            effect="light"
            :style="{ borderColor: tag.color, color: tag.color, backgroundColor: tag.color + '10' }"
          >
            {{ tag.emoji }} {{ tag.label }}
          </el-tag>
        </div>
        <div v-else class="no-tags-tip">暂未选择，可跳过</div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="skipMuscleTags">跳过</el-button>
          <el-button type="primary" @click="confirmMuscleTags">
            {{ selectedTags.length > 0 ? '保存并完成' : '完成打卡' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { useCheckinStore } from '@/stores/checkin'
import { sportTypes, bodyStatus, muscleTags, extractCommonPhrases } from '@/utils/common'

const router = useRouter()
const checkinStore = useCheckinStore()

const activeStep = ref(0)
const fileList = ref([])
const muscleTagDialogVisible = ref(false)
const selectedTags = ref([])

const defaultForm = {
  type: '',
  typeName: '',
  duration: 30,
  amount: 0,
  amountUnit: '',
  status: '',
  statusText: '',
  note: '',
  images: []
}

const formData = reactive({ ...defaultForm })

const overallTags = computed(() => {
  const overallValues = ['easy', 'sweaty', 'energetic', 'tired', 'refreshed', 'normal', 'breathless', 'pumped']
  return muscleTags.filter(tag => overallValues.includes(tag.value))
})

const bodyPartTags = computed(() => {
  const bodyPartValues = ['soreLegs', 'soreArms', 'soreAbs', 'pain']
  return muscleTags.filter(tag => bodyPartValues.includes(tag.value))
})

const selectedTagInfos = computed(() => {
  return selectedTags.value.map(v => muscleTags.find(t => t.value === v)).filter(Boolean)
})

const unitMap = {
  running: '公里',
  cycling: '公里',
  swimming: '米',
  yoga: '次',
  gym: '组',
  other: '次'
}

const canNext = computed(() => {
  switch (activeStep.value) {
    case 0:
      return !!formData.type
    case 1:
      return formData.duration > 0 && formData.amount > 0
    case 2:
      return !!formData.status
    default:
      return true
  }
})

const commonPhrases = computed(() => {
  return extractCommonPhrases(checkinStore.checkins, formData.type, 6)
})

const insertPhrase = (phrase) => {
  if (formData.note) {
    formData.note += '，' + phrase
  } else {
    formData.note = phrase
  }
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

const getStatusEmoji = (status) => {
  const emojiMap = {
    excellent: '😄',
    good: '😊',
    normal: '😐',
    tired: '😫'
  }
  return emojiMap[status] || '😊'
}

const selectType = (type) => {
  formData.type = type.value
  formData.typeName = type.label
  formData.amountUnit = unitMap[type.value] || '次'
}

const selectStatus = (status) => {
  formData.status = status.value
  formData.statusText = status.label
}

const handleFileChange = (file) => {
  if (file.raw) {
    const reader = new FileReader()
    reader.onload = (e) => {
      formData.images.push(e.target.result)
    }
    reader.readAsDataURL(file.raw)
  }
}

const handleFileRemove = (file, files) => {
  const index = fileList.value.indexOf(file)
  if (index > -1) {
    formData.images.splice(index, 1)
  }
  fileList.value = files
}

const nextStep = () => {
  if (activeStep.value < 3) {
    activeStep.value++
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const openMuscleTagDialog = () => {
  selectedTags.value = []
  muscleTagDialogVisible.value = true
}

const toggleTag = (tagValue) => {
  const index = selectedTags.value.indexOf(tagValue)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagValue)
  }
}

const buildSummaryNote = () => {
  const tagLabels = selectedTagInfos.value.map(t => t.label)
  if (tagLabels.length === 0) return formData.note
  
  const tagText = tagLabels.join('、')
  if (formData.note) {
    return `【${tagText}】${formData.note}`
  }
  return tagText
}

const submitCheckin = () => {
  const summaryNote = buildSummaryNote()
  checkinStore.addCheckin({
    type: formData.type,
    typeName: formData.typeName,
    duration: formData.duration,
    amount: formData.amount,
    amountUnit: formData.amountUnit,
    status: formData.status,
    statusText: formData.statusText,
    note: summaryNote,
    muscleTags: [...selectedTags.value],
    images: [...formData.images]
  })
  
  ElMessage.success('打卡成功！继续保持哦 💪')
  
  setTimeout(() => {
    router.push('/')
  }, 1000)
}

const skipMuscleTags = () => {
  muscleTagDialogVisible.value = false
  submitCheckin()
}

const confirmMuscleTags = () => {
  muscleTagDialogVisible.value = false
  submitCheckin()
}
</script>

<style scoped>
.checkin-steps {
  padding: 20px 0 40px;
}

.muscle-tag-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.muscle-tag-intro {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #ecf5ff;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #409eff;
}

.muscle-tag-section {
  margin-bottom: 20px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.muscle-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1.5px solid #e4e7ed;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
  user-select: none;
}

.muscle-tag:hover {
  border-color: var(--tag-color, #409eff);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.muscle-tag.active {
  border-color: var(--tag-color, #409eff);
  background: color-mix(in srgb, var(--tag-color, #409eff) 10%, white);
}

.tag-emoji {
  font-size: 16px;
}

.tag-label {
  font-size: 13px;
  color: #606266;
}

.muscle-tag.active .tag-label {
  color: var(--tag-color, #409eff);
  font-weight: 500;
}

.muscle-tag-summary {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.no-tags-tip {
  font-size: 13px;
  color: #c0c4cc;
  font-style: italic;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.step-content {
  min-height: 350px;
  padding: 20px 0;
}

.step-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
  text-align: center;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  max-width: 600px;
  margin: 0 auto;
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.type-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
}

.type-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.type-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.type-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.data-form {
  max-width: 500px;
  margin: 0 auto;
}

.unit {
  margin-left: 12px;
  color: #909399;
}

.phrase-tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.phrase-tags-label {
  font-size: 13px;
  color: #909399;
  margin-right: 4px;
}

.phrase-tag {
  cursor: pointer;
  transition: all 0.2s ease;
}

.phrase-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  max-width: 600px;
  margin: 0 auto;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-item:hover {
  transform: translateY(-2px);
}

.status-item.active {
  border-color: #67c23a;
  background: #f0f9eb;
}

.status-emoji {
  font-size: 48px;
  margin-bottom: 8px;
}

.status-label {
  font-size: 14px;
  font-weight: 500;
}

.image-upload {
  max-width: 500px;
  margin: 0 auto;
}

.upload-tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin-top: 12px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 30px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .type-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .status-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .checkin-steps {
    padding: 10px 0 30px;
  }
  
  .step-content {
    min-height: 400px;
  }
}
</style>
