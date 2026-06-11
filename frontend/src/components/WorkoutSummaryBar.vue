<template>
  <div class="workout-summary-bar">
    <div class="summary-header">
      <span class="type-pill" :style="typePillStyle">
        <span class="type-dot"></span>
        {{ item.typeName }}
      </span>
      <span class="intensity-badge" :style="badgeStyle">
        {{ intensity.label }}
      </span>
    </div>

    <div class="metrics-grid">
      <MetricBar
        icon="⏱"
        :value="item.duration"
        unit="分钟"
        color="#409eff"
        :fill-percent="durationPercent"
      />
      <MetricBar
        icon="🔥"
        :value="calorie"
        unit="千卡"
        color="#f56c6c"
        :fill-percent="caloriePercent"
      />
      <MetricBar
        v-if="distance"
        icon="📍"
        :value="distance"
        color="#67c23a"
        :fill-percent="distancePercent"
      />
      <div v-else class="metric-spacer"></div>
      <div class="intensity-bar-cell">
        <div class="intensity-bar-track">
          <div class="intensity-bar-fill" :style="barFillStyle"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useWorkoutIntensity, INTENSITY_LEVELS } from '@/composables/useWorkoutIntensity'
import { getSportTypeInfo } from '@/utils/common'
import MetricBar from '@/components/MetricBar.vue'

const props = defineProps({
  item: { type: Object, required: true }
})

const { calorie, distance, intensityKey, intensity } = useWorkoutIntensity(props.item)

const typeColor = computed(() => getSportTypeInfo(props.item.type).color)

const typePillStyle = computed(() => ({
  background: typeColor.value + '15',
  color: typeColor.value,
  borderColor: typeColor.value + '40'
}))

const DURATION_MAX = 90
const CALORIE_MAX = 500
const DISTANCE_KM_MAX = 15

const durationPercent = computed(() => {
  return Math.min((props.item.duration || 0) / DURATION_MAX * 100, 100)
})

const caloriePercent = computed(() => {
  return Math.min((calorie.value || 0) / CALORIE_MAX * 100, 100)
})

const distancePercent = computed(() => {
  if (!distance.value) return 0
  const num = parseFloat(distance.value) || 0
  const unit = distance.value.includes('km') ? 1 : 0.001
  return Math.min((num * unit) / DISTANCE_KM_MAX * 100, 100)
})

const barPercentMap = { LIGHT: 20, MODERATE: 55, HIGH: 90 }
const barColorMap = { LIGHT: '#67c23a', MODERATE: '#e6a23c', HIGH: '#f56c6c' }

const barFillStyle = computed(() => ({
  width: barPercentMap[intensityKey.value] + '%',
  background: `linear-gradient(90deg, ${barColorMap[intensityKey.value]}88, ${barColorMap[intensityKey.value]})`
}))

const badgeStyle = computed(() => ({
  color: intensity.value.color,
  background: intensity.value.bg,
  borderColor: intensity.value.color + '40'
}))
</script>

<style scoped>
.workout-summary-bar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 12px;
  background: linear-gradient(180deg, #f8f9fb 0%, #f5f7fa 100%);
  border-radius: 10px;
  border: 1px solid #ebeef5;
}

.summary-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.type-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid transparent;
}

.type-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.intensity-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 10px;
  white-space: nowrap;
  border: 1px solid transparent;
  letter-spacing: 0.3px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 14px;
}

.metric-spacer {
  height: 22px;
}

.intensity-bar-cell {
  display: flex;
  align-items: center;
  height: 22px;
  padding: 0 4px;
}

.intensity-bar-track {
  flex: 1;
  height: 6px;
  background: #ebeef5;
  border-radius: 3px;
  overflow: hidden;
}

.intensity-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.4s ease;
}

@media (max-width: 768px) {
  .workout-summary-bar {
    padding: 8px 10px;
    gap: 8px;
  }

  .metrics-grid {
    gap: 2px 10px;
  }

  .type-pill {
    font-size: 11px;
    padding: 2px 8px;
  }

  .intensity-badge {
    font-size: 10px;
    padding: 2px 8px;
  }
}
</style>
