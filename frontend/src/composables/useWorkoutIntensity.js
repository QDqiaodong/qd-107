import { computed } from 'vue'
import { calculateCalorie as commonCalculateCalorie } from '@/utils/common'

const INTENSITY_LEVELS = {
  LIGHT: { label: '轻量', color: '#67c23a', bg: '#f0f9eb' },
  MODERATE: { label: '中等', color: '#e6a23c', bg: '#fdf6ec' },
  HIGH: { label: '高强度', color: '#f56c6c', bg: '#fef0f0' }
}

const LOCAL_DURATION_THRESHOLD_LOW = 30
const LOCAL_DURATION_THRESHOLD_HIGH = 60
const LOCAL_CALORIE_THRESHOLD_MODERATE = 150
const LOCAL_CALORIE_THRESHOLD_HIGH = 400

const classifyIntensityLocal = (duration, calorie) => {
  const d = duration || 0
  const c = calorie || 0
  if (c >= LOCAL_CALORIE_THRESHOLD_HIGH || d > LOCAL_DURATION_THRESHOLD_HIGH) return 'HIGH'
  if (d >= LOCAL_DURATION_THRESHOLD_LOW || c >= LOCAL_CALORIE_THRESHOLD_MODERATE) return 'MODERATE'
  return 'LIGHT'
}

const calcCalorie = (duration, amount, type) => {
  if (type) {
    return commonCalculateCalorie({ duration, type })
  }
  return commonCalculateCalorie({ duration, type: 'other' })
}

export function useWorkoutIntensity(checkin) {
  const calorie = computed(() => {
    if (checkin.calorie != null) return checkin.calorie
    return calcCalorie(checkin.duration, checkin.amount, checkin.type)
  })

  const distance = computed(() => checkin.distance != null ? checkin.distance : null)

  const intensityKey = computed(() => {
    if (checkin.intensity && INTENSITY_LEVELS[checkin.intensity]) {
      return checkin.intensity
    }
    return classifyIntensityLocal(checkin.duration, calorie.value)
  })

  const intensity = computed(() => INTENSITY_LEVELS[intensityKey.value] || INTENSITY_LEVELS.MODERATE)

  return { calorie, distance, intensityKey, intensity }
}

export { calcCalorie, classifyIntensityLocal, INTENSITY_LEVELS }
