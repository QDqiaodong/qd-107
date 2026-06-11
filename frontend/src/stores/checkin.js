import { defineStore } from 'pinia'
import { getCheckins, saveCheckins, getPlans, savePlans, getUserInfo, saveUserInfo, getMonthlyGoals, saveMonthlyGoals } from '@/utils/storage'

export const useCheckinStore = defineStore('checkin', {
  state: () => ({
    checkins: getCheckins(),
    plans: getPlans(),
    userInfo: getUserInfo(),
    monthlyGoals: getMonthlyGoals()
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
    },
    currentMonthKey: () => {
      const now = new Date()
      return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    },
    currentMonthGoal: (state) => {
      const now = new Date()
      const key = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      return state.monthlyGoals[key] || null
    },
    monthCheckinCount: (state) => {
      const now = new Date()
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      return state.checkins.filter(item => new Date(item.createTime) >= monthStart).length
    },
    monthTotalDuration: (state) => {
      const now = new Date()
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      const monthCheckins = state.checkins.filter(item => new Date(item.createTime) >= monthStart)
      return monthCheckins.reduce((sum, item) => sum + (item.duration || 0), 0)
    },
    monthTotalCalorie: (state) => {
      const now = new Date()
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      const monthCheckins = state.checkins.filter(item => new Date(item.createTime) >= monthStart)
      return monthCheckins.reduce((sum, item) => {
        return sum + Math.round((item.duration || 0) * (item.amount || 0) * 0.1)
      }, 0)
    },
    monthGoalProgress: (state) => {
      const now = new Date()
      const key = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      const goal = state.monthlyGoals[key]
      if (!goal) return null
      
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      const monthCheckins = state.checkins.filter(item => new Date(item.createTime) >= monthStart)
      
      const checkinCount = monthCheckins.length
      const totalDuration = monthCheckins.reduce((sum, item) => sum + (item.duration || 0), 0)
      const totalCalorie = monthCheckins.reduce((sum, item) => {
        return sum + Math.round((item.duration || 0) * (item.amount || 0) * 0.1)
      }, 0)
      
      const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
      const currentDay = now.getDate()
      const remainingDays = daysInMonth - currentDay
      
      return {
        checkinCount: {
          target: goal.checkinCount,
          current: checkinCount,
          progress: goal.checkinCount > 0 ? Math.min(100, (checkinCount / goal.checkinCount) * 100) : 0,
          remaining: Math.max(0, goal.checkinCount - checkinCount),
          dailyNeeded: remainingDays > 0 ? Math.ceil(Math.max(0, goal.checkinCount - checkinCount) / remainingDays) : 0
        },
        totalDuration: {
          target: goal.totalDuration,
          current: totalDuration,
          progress: goal.totalDuration > 0 ? Math.min(100, (totalDuration / goal.totalDuration) * 100) : 0,
          remaining: Math.max(0, goal.totalDuration - totalDuration),
          dailyNeeded: remainingDays > 0 ? Math.ceil(Math.max(0, goal.totalDuration - totalDuration) / remainingDays) : 0
        },
        totalCalorie: {
          target: goal.totalCalorie,
          current: totalCalorie,
          progress: goal.totalCalorie > 0 ? Math.min(100, (totalCalorie / goal.totalCalorie) * 100) : 0,
          remaining: Math.max(0, goal.totalCalorie - totalCalorie),
          dailyNeeded: remainingDays > 0 ? Math.ceil(Math.max(0, goal.totalCalorie - totalCalorie) / remainingDays) : 0
        },
        remainingDays,
        daysInMonth,
        currentDay
      }
    },
    dailyGoalDetail: (state) => {
      const now = new Date()
      const key = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      const goal = state.monthlyGoals[key]
      if (!goal) return []
      
      const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
      const dailyData = []
      
      for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(now.getFullYear(), now.getMonth(), day)
        const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
        
        const dayCheckins = state.checkins.filter(item => {
          const itemDate = new Date(item.createTime)
          const itemDateStr = `${itemDate.getFullYear()}-${String(itemDate.getMonth() + 1).padStart(2, '0')}-${String(itemDate.getDate()).padStart(2, '0')}`
          return itemDateStr === dateStr
        })
        
        const dayDuration = dayCheckins.reduce((sum, item) => sum + (item.duration || 0), 0)
        const dayCalorie = dayCheckins.reduce((sum, item) => {
          return sum + Math.round((item.duration || 0) * (item.amount || 0) * 0.1)
        }, 0)
        
        const isPast = day < now.getDate()
        const isToday = day === now.getDate()
        const isFuture = day > now.getDate()
        
        dailyData.push({
          day,
          date: dateStr,
          weekday: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()],
          checkinCount: dayCheckins.length,
          duration: dayDuration,
          calorie: dayCalorie,
          isPast,
          isToday,
          isFuture,
          hasCheckin: dayCheckins.length > 0
        })
      }
      
      return dailyData
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
    },
    setMonthlyGoal(goalData) {
      const now = new Date()
      const key = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      this.monthlyGoals[key] = { ...goalData }
      saveMonthlyGoals(this.monthlyGoals)
    }
  }
})
