import { defineStore } from 'pinia'
import { getCheckins, saveCheckins, getPlans, savePlans, getUserInfo, saveUserInfo } from '@/utils/storage'

export const useCheckinStore = defineStore('checkin', {
  state: () => ({
    checkins: getCheckins(),
    plans: getPlans(),
    userInfo: getUserInfo()
  }),
  getters: {
    checkinCount: (state) => state.checkins.length,
    planCount: (state) => state.plans.filter(p => !p.completed).length,
    totalDuration: (state) => {
      return state.checkins.reduce((sum, item) => sum + (item.duration || 0), 0)
    },
    weekCheckins: (state) => {
      const now = new Date()
      const weekStart = new Date(now.setDate(now.getDate() - now.getDay()))
      weekStart.setHours(0, 0, 0, 0)
      return state.checkins.filter(item => new Date(item.createTime) >= weekStart)
    },
    monthCheckins: (state) => {
      const now = new Date()
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      return state.checkins.filter(item => new Date(item.createTime) >= monthStart)
    },
    activePlans: (state) => state.plans.filter(p => !p.completed),
    weeklySchedule: (state) => {
      const days = [0, 1, 2, 3, 4, 5, 6]
      return days.map(day => {
        const dayPlans = state.plans.filter(p => !p.completed && p.weekdays && p.weekdays.includes(day))
        const totalDuration = dayPlans.reduce((sum, p) => sum + (p.duration || 0), 0)
        return {
          day,
          dayName: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][day],
          plans: dayPlans,
          totalDuration,
          isToday: new Date().getDay() === day
        }
      })
    },
    getWeeklyRemaining: (state) => (plan) => {
      const now = new Date()
      const dayOfWeek = now.getDay()
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - dayOfWeek)
      weekStart.setHours(0, 0, 0, 0)

      const weekCheckins = state.checkins.filter(item =>
        new Date(item.createTime) >= weekStart && item.type === plan.type
      )

      const completedCount = weekCheckins.length
      const completedDuration = weekCheckins.reduce((sum, item) => sum + (item.duration || 0), 0)

      const targetCount = plan.weekdays ? plan.weekdays.length : 0
      const targetDuration = targetCount * (plan.duration || 0)

      return {
        remainingCount: Math.max(0, targetCount - completedCount),
        remainingMinutes: Math.max(0, targetDuration - completedDuration)
      }
    },
    weeklyTotalDuration: (state) => {
      const days = [0, 1, 2, 3, 4, 5, 6]
      return days.reduce((total, day) => {
        const dayPlans = state.plans.filter(p => !p.completed && p.weekdays && p.weekdays.includes(day))
        const dayDuration = dayPlans.reduce((sum, p) => sum + (p.duration || 0), 0)
        return total + dayDuration
      }, 0)
    },
    maxDurationRecord: (state) => {
      if (state.checkins.length === 0) return null
      return state.checkins.reduce((max, curr) => 
        (curr.duration || 0) > (max.duration || 0) ? curr : max
      )
    },
    maxCalorieRecord: (state) => {
      if (state.checkins.length === 0) return null
      return state.checkins.reduce((max, curr) => {
        const currCalorie = (curr.duration || 0) * (curr.amount || 0) * 0.1
        const maxCalorie = (max.duration || 0) * (max.amount || 0) * 0.1
        return currCalorie > maxCalorie ? curr : max
      })
    },
    maxDistanceRecord: (state) => {
      const distanceTypes = ['running', 'cycling', 'swimming']
      const distanceRecords = state.checkins.filter(c => 
        distanceTypes.includes(c.type) && c.amountUnit === '公里'
      )
      if (distanceRecords.length === 0) {
        const meterRecords = state.checkins.filter(c => c.amountUnit === '米')
        if (meterRecords.length === 0) return null
        return meterRecords.reduce((max, curr) => (curr.amount || 0) > (max.amount || 0) ? curr : max)
      }
      return distanceRecords.reduce((max, curr) => (curr.amount || 0) > (max.amount || 0) ? curr : max)
    },
    maxStreakDays: (state) => {
      if (state.checkins.length === 0) return { days: 0, records: [] }
      const dateSet = new Set()
      state.checkins.forEach(c => {
        const date = new Date(c.createTime)
        const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
        dateSet.add(dateStr)
      })
      const sortedDates = Array.from(dateSet).sort()
      let maxStreak = 1
      let currentStreak = 1
      let streakEndDate = sortedDates[0]
      let currentEndDate = sortedDates[0]
      for (let i = 1; i < sortedDates.length; i++) {
        const prev = new Date(sortedDates[i - 1])
        const curr = new Date(sortedDates[i])
        const diffDays = Math.round((curr - prev) / (1000 * 60 * 60 * 24))
        if (diffDays === 1) {
          currentStreak++
          currentEndDate = sortedDates[i]
          if (currentStreak > maxStreak) {
            maxStreak = currentStreak
            streakEndDate = currentEndDate
          }
        } else if (diffDays > 1) {
          currentStreak = 1
          currentEndDate = sortedDates[i]
        }
      }
      const streakRecords = []
      const endDate = new Date(streakEndDate)
      for (let i = maxStreak - 1; i >= 0; i--) {
        const d = new Date(endDate)
        d.setDate(d.getDate() - i)
        const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
        const dayRecords = state.checkins.filter(c => {
          const cd = new Date(c.createTime)
          const cStr = `${cd.getFullYear()}-${String(cd.getMonth() + 1).padStart(2, '0')}-${String(cd.getDate()).padStart(2, '0')}`
          return cStr === dateStr
        })
        if (dayRecords.length > 0) {
          streakRecords.push(...dayRecords)
        }
      }
      return { days: maxStreak, records: streakRecords, endDate: streakEndDate }
    }
  },
  actions: {
    getCheckinsByPage(page, pageSize, type = 'all') {
      return new Promise((resolve) => {
        setTimeout(() => {
          let filtered = this.checkins
          if (type !== 'all') {
            filtered = this.checkins.filter(item => item.type === type)
          }
          const start = (page - 1) * pageSize
          const end = start + pageSize
          const list = filtered.slice(start, end)
          resolve({
            list,
            total: filtered.length
          })
        }, 300)
      })
    },
    addCheckin(checkin) {
      const newCheckin = {
        ...checkin,
        id: Date.now(),
        createTime: new Date().toISOString()
      }
      this.checkins.unshift(newCheckin)
      saveCheckins(this.checkins)
    },
    deleteCheckin(id) {
      this.checkins = this.checkins.filter(item => item.id !== id)
      saveCheckins(this.checkins)
    },
    addPlan(plan) {
      const newPlan = {
        ...plan,
        id: Date.now(),
        completed: false,
        createTime: new Date().toISOString()
      }
      this.plans.unshift(newPlan)
      savePlans(this.plans)
    },
    togglePlan(id) {
      const plan = this.plans.find(p => p.id === id)
      if (plan) {
        plan.completed = !plan.completed
        savePlans(this.plans)
      }
    },
    deletePlan(id) {
      this.plans = this.plans.filter(p => p.id !== id)
      savePlans(this.plans)
    },
    updatePlan(id, data) {
      const plan = this.plans.find(p => p.id === id)
      if (plan) {
        Object.assign(plan, data)
        savePlans(this.plans)
      }
    },
    togglePlanWeekday(planId, weekday) {
      const plan = this.plans.find(p => p.id === planId)
      if (plan) {
        if (!plan.weekdays) {
          plan.weekdays = []
        }
        const index = plan.weekdays.indexOf(weekday)
        if (index > -1) {
          plan.weekdays.splice(index, 1)
        } else {
          plan.weekdays.push(weekday)
        }
        savePlans(this.plans)
      }
    },
    updateUserInfo(info) {
      this.userInfo = { ...this.userInfo, ...info }
      saveUserInfo(this.userInfo)
    }
  }
})
