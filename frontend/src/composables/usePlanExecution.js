import { computed } from 'vue'
import { useCheckinStore } from '@/stores/checkin'
import { planApi } from '@/api'

const EXECUTION_STATUS = {
  IN_PROGRESS: { key: 'IN_PROGRESS', label: '进行中', color: '#409eff', bg: '#ecf5ff', borderColor: '#d9ecff' },
  GOAL_MET: { key: 'GOAL_MET', label: '已达标', color: '#67c23a', bg: '#f0f9eb', borderColor: '#e1f3d8' },
  SIGNIFICANTLY_BEHIND: { key: 'SIGNIFICANTLY_BEHIND', label: '明显落后', color: '#f56c6c', bg: '#fef0f0', borderColor: '#fde2e2' }
}

function getWeekRange() {
  const now = new Date()
  const dayOfWeek = now.getDay()
  const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek
  const weekStart = new Date(now)
  weekStart.setDate(now.getDate() + mondayOffset)
  weekStart.setHours(0, 0, 0, 0)
  const weekEnd = new Date(weekStart)
  weekEnd.setDate(weekStart.getDate() + 6)
  weekEnd.setHours(23, 59, 59, 999)
  return { weekStart, weekEnd }
}

function computeSnapshot(plan, checkins) {
  const { weekStart, weekEnd } = getWeekRange()

  const planCheckins = checkins.filter(item => {
    const itemDate = new Date(item.createTime)
    return itemDate >= weekStart && itemDate <= weekEnd && item.type === plan.type
  })

  const completedCount = planCheckins.length
  const completedDuration = planCheckins.reduce((sum, item) => sum + (item.duration || 0), 0)
  const completedCalorie = planCheckins.reduce((sum, item) => {
    return sum + Math.round((item.duration || 0) * (item.amount || 0) * 0.1)
  }, 0)

  const targetCount = plan.weekdays ? plan.weekdays.length : (plan.frequency ? parseInt(plan.frequency) : 0)
  const targetDuration = targetCount * (plan.duration || 0)

  const now = new Date()
  const dayOfWeek = now.getDay()
  const daysElapsed = dayOfWeek === 0 ? 7 : dayOfWeek
  const totalDays = 7
  const daysRemaining = totalDays - daysElapsed

  let completionRate = 0
  if (targetCount > 0) {
    completionRate = Math.min((completedCount / targetCount) * 100, 100)
  } else if (targetDuration > 0) {
    completionRate = Math.min((completedDuration / targetDuration) * 100, 100)
  }

  const expectedProgress = (daysElapsed / totalDays) * 100
  const deviation = completionRate - expectedProgress

  let status = 'IN_PROGRESS'
  if (completionRate >= 100) {
    status = 'GOAL_MET'
  } else if (completionRate < expectedProgress * 0.5) {
    status = 'SIGNIFICANTLY_BEHIND'
  }

  return {
    planId: plan.id,
    title: plan.title,
    type: plan.type,
    typeName: plan.typeName,
    targetCount,
    targetDuration,
    completedCount,
    completedDuration,
    completedCalorie,
    completionRate: Math.round(completionRate * 10) / 10,
    status,
    statusInfo: EXECUTION_STATUS[status],
    cycleStart: weekStart.toISOString().slice(0, 10),
    cycleEnd: weekEnd.toISOString().slice(0, 10),
    daysElapsed,
    daysRemaining,
    expectedProgress: Math.round(expectedProgress * 10) / 10,
    deviation: Math.round(deviation * 10) / 10
  }
}

export function usePlanExecution() {
  const store = useCheckinStore()

  const snapshots = computed(() => {
    const activePlans = store.plans.filter(p => !p.completed)
    return activePlans.map(plan => computeSnapshot(plan, store.checkins))
  })

  const byStatus = computed(() => ({
    IN_PROGRESS: snapshots.value.filter(s => s.status === 'IN_PROGRESS'),
    GOAL_MET: snapshots.value.filter(s => s.status === 'GOAL_MET'),
    SIGNIFICANTLY_BEHIND: snapshots.value.filter(s => s.status === 'SIGNIFICANTLY_BEHIND')
  }))

  const summary = computed(() => {
    const total = snapshots.value.length
    const goalMet = byStatus.value.GOAL_MET.length
    const behind = byStatus.value.SIGNIFICANTLY_BEHIND.length
    const inProgress = byStatus.value.IN_PROGRESS.length
    const avgCompletionRate = total > 0
      ? Math.round(snapshots.value.reduce((sum, s) => sum + s.completionRate, 0) / total * 10) / 10
      : 0
    const totalCompletedDuration = snapshots.value.reduce((sum, s) => sum + s.completedDuration, 0)
    const totalTargetDuration = snapshots.value.reduce((sum, s) => sum + s.targetDuration, 0)
    return { total, goalMet, behind, inProgress, avgCompletionRate, totalCompletedDuration, totalTargetDuration }
  })

  const getSnapshotForPlan = (planId) => {
    return snapshots.value.find(s => s.planId === planId) || null
  }

  const fetchApiSnapshots = async (userId, status) => {
    try {
      const res = await planApi.getExecutionSnapshots(userId, status)
      if (res.code === 200) {
        return res.data || []
      }
      return []
    } catch (e) {
      return []
    }
  }

  return {
    snapshots,
    byStatus,
    summary,
    getSnapshotForPlan,
    fetchApiSnapshots,
    EXECUTION_STATUS
  }
}

export { EXECUTION_STATUS }
