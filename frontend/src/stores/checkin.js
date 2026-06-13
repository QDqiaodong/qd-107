import { defineStore } from 'pinia'
import {
  getCheckins,
  saveCheckins,
  getPlans,
  savePlans,
  getUserInfo,
  saveUserInfo,
  getMonthlyGoals,
  saveMonthlyGoals
} from '@/utils/storage'
import { checkinApi, sportTypeApi } from '@/api'
import { getWeekRange, getMonthRange, isInWeekRange, isInMonthRange } from '@/utils/common'

const DEFAULT_USER_ID = 1

const typeIdMapCache = {
  running: 1,
  cycling: 2,
  swimming: 3,
  yoga: 4,
  gym: 5,
  fitness: 5,
  badminton: 6,
  basketball: 7,
  hiking: 8,
  other: 5
}

const idTypeMapCache = {
  1: { type: 'running', typeName: '跑步', amountUnit: '公里' },
  2: { type: 'cycling', typeName: '骑行', amountUnit: '公里' },
  3: { type: 'swimming', typeName: '游泳', amountUnit: '米' },
  4: { type: 'yoga', typeName: '瑜伽', amountUnit: '次' },
  5: { type: 'gym', typeName: '健身', amountUnit: '组' },
  6: { type: 'badminton', typeName: '羽毛球', amountUnit: '次' },
  7: { type: 'basketball', typeName: '篮球', amountUnit: '次' },
  8: { type: 'hiking', typeName: '徒步', amountUnit: '公里' }
}

const getTypeId = (type) => {
  return typeIdMapCache[type] || 5
}

const getTypeInfo = (sportTypeId) => {
  return idTypeMapCache[sportTypeId] || { type: 'other', typeName: '其他', amountUnit: '次' }
}

const frontToBack = (checkin, userId = DEFAULT_USER_ID) => {
  const sportTypeId = getTypeId(checkin.type)
  let distance = null
  if (checkin.amount && (checkin.type === 'running' || checkin.type === 'cycling' || checkin.type === 'hiking')) {
    distance = Number(checkin.amount)
  } else if (checkin.amount && checkin.type === 'swimming') {
    if (checkin.amountUnit === '公里' || checkin.amountUnit === 'km') {
      distance = Number(checkin.amount)
    } else {
      distance = Number(checkin.amount) / 1000
    }
  }

  return {
    userId,
    sportTypeId,
    duration: checkin.duration || 0,
    distance: distance != null ? distance : undefined,
    remark: checkin.note || '',
    muscleTags: Array.isArray(checkin.muscleTags) ? checkin.muscleTags.join(',') : '',
    images: Array.isArray(checkin.images) ? checkin.images.join(',') : ''
  }
}

const backToFront = (record) => {
  const { type, typeName, amountUnit } = getTypeInfo(record.sportTypeId)
  let amount = 0
  if (record.distance != null) {
    if (type === 'swimming' && amountUnit === '米') {
      amount = Number((Number(record.distance) * 1000).toFixed(1))
    } else {
      amount = Number(record.distance)
    }
  }
  return {
    id: record.id,
    type,
    typeName,
    duration: record.duration || 0,
    amount,
    amountUnit,
    status: 'good',
    statusText: '状态良好',
    note: record.remark || '',
    muscleTags: record.muscleTags ? record.muscleTags.split(',').filter(Boolean) : [],
    images: record.images ? record.images.split(',').filter(Boolean) : [],
    createTime: record.checkinTime ? new Date(record.checkinTime).toISOString() : (record.createTime ? new Date(record.createTime).toISOString() : new Date().toISOString())
  }
}

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
      const { weekStart, weekEnd } = getWeekRange()
      return state.checkins.filter(item => isInWeekRange(item.createTime, weekStart, weekEnd))
    },
    monthCheckins: (state) => {
      const { monthStart, monthEnd } = getMonthRange()
      return state.checkins.filter(item => isInMonthRange(item.createTime, monthStart, monthEnd))
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
      const { weekStart, weekEnd } = getWeekRange()

      const weekCheckins = state.checkins.filter(item => {
        return isInWeekRange(item.createTime, weekStart, weekEnd) && item.type === plan.type
      })

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
      const { monthStart, monthEnd } = getMonthRange()
      return state.checkins.filter(item => isInMonthRange(item.createTime, monthStart, monthEnd)).length
    },
    monthTotalDuration: (state) => {
      const { monthStart, monthEnd } = getMonthRange()
      const monthCheckins = state.checkins.filter(item => isInMonthRange(item.createTime, monthStart, monthEnd))
      return monthCheckins.reduce((sum, item) => sum + (item.duration || 0), 0)
    },
    monthTotalCalorie: (state) => {
      const { monthStart, monthEnd } = getMonthRange()
      const monthCheckins = state.checkins.filter(item => isInMonthRange(item.createTime, monthStart, monthEnd))
      return monthCheckins.reduce((sum, item) => {
        return sum + Math.round((item.duration || 0) * (item.amount || 0) * 0.1)
      }, 0)
    },
    monthGoalProgress: (state) => {
      const now = new Date()
      const key = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      const goal = state.monthlyGoals[key]
      if (!goal) return null

      const { monthStart, monthEnd } = getMonthRange()
      const monthCheckins = state.checkins.filter(item => isInMonthRange(item.createTime, monthStart, monthEnd))

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
    getCurrentUserId() {
      return this.userInfo?.id || DEFAULT_USER_ID
    },

    async ensureSportTypeMap() {
      try {
        const res = await sportTypeApi.getList()
        if (res.code === 200 && Array.isArray(res.data)) {
          res.data.forEach(item => {
            if (item.icon) typeIdMapCache[item.icon] = item.id
            const unit = amountUnitForType(item.icon)
            idTypeMapCache[item.id] = {
              type: item.icon === 'fitness' ? 'gym' : item.icon,
              typeName: item.name,
              amountUnit: unit
            }
          })
        }
      } catch (e) {
        console.warn('加载运动类型映射失败，使用默认映射', e)
      }
    },

    async fetchCheckins() {
      try {
        await this.ensureSportTypeMap()
        const userId = this.getCurrentUserId()
        const res = await checkinApi.getList(userId)
        if (res.code === 200 && Array.isArray(res.data)) {
          this.checkins = res.data.map(r => backToFront(r)).sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
          saveCheckins(this.checkins)
          return this.checkins
        }
        throw new Error(res.message || '加载打卡数据失败')
      } catch (e) {
        console.error('fetchCheckins error:', e)
        throw e
      }
    },

    getCheckinsByPage(page, pageSize, filter = {}) {
      return new Promise((resolve, reject) => {
        this.fetchCheckins().then(() => {
          setTimeout(() => {
            let filtered = this.checkins

            const { type = 'all', types = [], minDuration, maxDuration, hasImage } = filter

            if (type !== 'all') {
              filtered = filtered.filter(item => item.type === type)
            }

            if (types && types.length > 0) {
              filtered = filtered.filter(item => types.includes(item.type))
            }

            if (minDuration !== undefined && minDuration !== null) {
              filtered = filtered.filter(item => (item.duration || 0) >= minDuration)
            }

            if (maxDuration !== undefined && maxDuration !== null) {
              filtered = filtered.filter(item => (item.duration || 0) <= maxDuration)
            }

            if (hasImage !== undefined && hasImage !== null) {
              if (hasImage) {
                filtered = filtered.filter(item => item.images && item.images.length > 0)
              } else {
                filtered = filtered.filter(item => !item.images || item.images.length === 0)
              }
            }

            const start = (page - 1) * pageSize
            const end = start + pageSize
            const list = filtered.slice(start, end)
            resolve({
              list,
              total: filtered.length
            })
          }, 100)
        }).catch(reject)
      })
    },

    async addCheckin(checkin) {
      try {
        await this.ensureSportTypeMap()
        const userId = this.getCurrentUserId()
        const payload = frontToBack(checkin, userId)
        const res = await checkinApi.create(payload)
        if (res.code !== 200 || !res.data || !res.data.record) {
          throw new Error(res.message || '打卡失败')
        }
        const newRecord = backToFront(res.data.record)
        if (checkin.status) newRecord.status = checkin.status
        if (checkin.statusText) newRecord.statusText = checkin.statusText

        this.checkins = [newRecord, ...this.checkins.filter(c => c.id !== newRecord.id)].sort(
          (a, b) => new Date(b.createTime) - new Date(a.createTime)
        )
        saveCheckins(this.checkins)

        return {
          merged: !!res.data.merged,
          mergeTip: res.data.mergeTip || res.message,
          record: newRecord
        }
      } catch (e) {
        console.error('addCheckin error:', e)
        const fallback = {
          ...checkin,
          id: Date.now(),
          createTime: new Date().toISOString()
        }
        this.checkins.unshift(fallback)
        saveCheckins(this.checkins)
        return { merged: false, mergeTip: '', record: fallback, offline: true, error: e }
      }
    },

    async deleteCheckin(id) {
      try {
        const userId = this.getCurrentUserId()
        const res = await checkinApi.delete(id, userId)
        if (res.code !== 200) {
          throw new Error(res.message || '删除失败')
        }
      } catch (e) {
        console.warn('后端删除失败，继续从本地移除', e)
      }
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
      this.userInfo = { ...this.userInfo, ...info, id: this.userInfo?.id || DEFAULT_USER_ID }
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

const amountUnitForType = (type) => {
  const m = {
    running: '公里',
    cycling: '公里',
    swimming: '米',
    yoga: '次',
    fitness: '组',
    gym: '组',
    badminton: '次',
    basketball: '次',
    hiking: '公里'
  }
  return m[type] || '次'
}
