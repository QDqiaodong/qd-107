import { computed } from 'vue'

const LIGHT_DURATION_THRESHOLD = 30
const LONG_DURATION_THRESHOLD = 60
const HIGH_CALORIE_THRESHOLD = 300
const MODERATE_CALORIE_THRESHOLD = 150

const INTENSITY_LEVELS = {
  light: { label: '轻练', color: '#67c23a', bg: '#f0f9eb' },
  moderate: { label: '中等', color: '#e6a23c', bg: '#fdf6ec' },
  long: { label: '长练', color: '#409eff', bg: '#ecf5ff' },
  highBurn: { label: '高消耗', color: '#f56c6c', bg: '#fef0f0' }
}

const calcCalorie = (duration, amount) => Math.round((duration || 0) * (amount || 0) * 0.1)

const extractDistance = (amount, amountUnit) => {
  if (!amount || !amountUnit) return null
  if (amountUnit === '公里') return `${amount}km`
  if (amountUnit === '米') return `${amount}m`
  return null
}

const classifyIntensity = (duration, calorie) => {
  if (calorie >= HIGH_CALORIE_THRESHOLD) return 'highBurn'
  if (duration >= LONG_DURATION_THRESHOLD) return 'long'
  if (duration < LIGHT_DURATION_THRESHOLD && calorie < MODERATE_CALORIE_THRESHOLD) return 'light'
  return 'moderate'
}

export function useWorkoutIntensity(checkin) {
  const calorie = computed(() => calcCalorie(checkin.duration, checkin.amount))
  const distance = computed(() => extractDistance(checkin.amount, checkin.amountUnit))
  const intensityKey = computed(() => classifyIntensity(checkin.duration, calorie.value))
  const intensity = computed(() => INTENSITY_LEVELS[intensityKey.value])

  return { calorie, distance, intensityKey, intensity }
}

export { calcCalorie, extractDistance, classifyIntensity, INTENSITY_LEVELS }
