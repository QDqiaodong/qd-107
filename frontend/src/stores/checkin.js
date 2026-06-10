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
    weeklyTotalDuration: (state) => {
      const days = [0, 1, 2, 3, 4, 5, 6]
      return days.reduce((total, day) => {
        const dayPlans = state.plans.filter(p => !p.completed && p.weekdays && p.weekdays.includes(day))
        const dayDuration = dayPlans.reduce((sum, p) => sum + (p.duration || 0), 0)
        return total + dayDuration
      }, 0)
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
