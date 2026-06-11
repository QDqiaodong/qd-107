const CHECKINS_KEY = 'sport_checkins'
const PLANS_KEY = 'sport_plans'
const USER_KEY = 'sport_user'
const GOALS_KEY = 'sport_monthly_goals'

export const getCheckins = () => {
  const data = localStorage.getItem(CHECKINS_KEY)
  return data ? JSON.parse(data) : getMockCheckins()
}

export const saveCheckins = (checkins) => {
  localStorage.setItem(CHECKINS_KEY, JSON.stringify(checkins))
}

export const getPlans = () => {
  const data = localStorage.getItem(PLANS_KEY)
  return data ? JSON.parse(data) : getMockPlans()
}

export const savePlans = (plans) => {
  localStorage.setItem(PLANS_KEY, JSON.stringify(plans))
}

export const getUserInfo = () => {
  const data = localStorage.getItem(USER_KEY)
  return data ? JSON.parse(data) : {
    nickname: '运动达人',
    avatar: '',
    height: 170,
    weight: 65,
    target: '保持健康'
  }
}

export const saveUserInfo = (info) => {
  localStorage.setItem(USER_KEY, JSON.stringify(info))
}

export const getMonthlyGoals = () => {
  const data = localStorage.getItem(GOALS_KEY)
  return data ? JSON.parse(data) : getMockMonthlyGoals()
}

export const saveMonthlyGoals = (goals) => {
  localStorage.setItem(GOALS_KEY, JSON.stringify(goals))
}

const getMockMonthlyGoals = () => {
  const now = new Date()
  const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  return {
    [currentMonth]: {
      checkinCount: 20,
      totalDuration: 1500,
      totalCalorie: 5000
    }
  }
}

const getMockCheckins = () => {
  const now = new Date()
  const types = [
    { type: 'running', typeName: '跑步', unit: '公里', amountRange: [3, 10], durationRange: [30, 90] },
    { type: 'cycling', typeName: '骑行', unit: '公里', amountRange: [10, 30], durationRange: [45, 120] },
    { type: 'swimming', typeName: '游泳', unit: '米', amountRange: [500, 2000], durationRange: [30, 90] },
    { type: 'yoga', typeName: '瑜伽', unit: '次', amountRange: [1, 2], durationRange: [30, 90] },
    { type: 'gym', typeName: '健身', unit: '组', amountRange: [8, 20], durationRange: [45, 120] }
  ]
  const statuses = [
    { value: 'excellent', text: '非常好' },
    { value: 'good', text: '状态良好' },
    { value: 'normal', text: '一般' },
    { value: 'tired', text: '比较累' }
  ]
  const notes = [
    '今天状态不错，继续加油！',
    '配速稳定，感觉很好',
    '力量训练，感觉肌肉在增长',
    '身心放松，非常舒服',
    '风有点大，有点累',
    '突破了个人记录！',
    '热身充分，状态在线',
    '',
    '',
    ''
  ]
  
  const muscleTagOptions = [
    { value: 'easy', label: '轻松' },
    { value: 'sweaty', label: '爆汗' },
    { value: 'soreLegs', label: '腿酸' },
    { value: 'soreArms', label: '胳膊酸' },
    { value: 'soreAbs', label: '腹部酸' },
    { value: 'energetic', label: '精力充沛' },
    { value: 'tired', label: '疲惫' },
    { value: 'refreshed', label: '神清气爽' },
    { value: 'normal', label: '状态一般' },
    { value: 'pain', label: '有点疼' },
    { value: 'breathless', label: '喘得厉害' },
    { value: 'pumped', label: '泵感十足' }
  ]
  
  const typeTagMap = {
    running: ['sweaty', 'soreLegs', 'breathless', 'energetic', 'tired', 'easy'],
    cycling: ['soreLegs', 'sweaty', 'easy', 'energetic', 'tired'],
    swimming: ['sweaty', 'tired', 'refreshed', 'breathless', 'easy'],
    yoga: ['refreshed', 'easy', 'energetic', 'soreAbs'],
    gym: ['pumped', 'soreArms', 'soreAbs', 'soreLegs', 'pain', 'energetic']
  }
  
  const checkins = []
  let id = 1
  
  for (let dayOffset = 0; dayOffset < 60; dayOffset++) {
    const hasCheckin = Math.random() > 0.25
    if (!hasCheckin) continue
    
    const numCheckins = Math.random() > 0.7 ? 2 : 1
    
    for (let i = 0; i < numCheckins; i++) {
      const typeInfo = types[Math.floor(Math.random() * types.length)]
      const statusInfo = statuses[Math.floor(Math.random() * statuses.length)]
      const duration = Math.floor(
        typeInfo.durationRange[0] + 
        Math.random() * (typeInfo.durationRange[1] - typeInfo.durationRange[0])
      )
      const amount = +(
        typeInfo.amountRange[0] + 
        Math.random() * (typeInfo.amountRange[1] - typeInfo.amountRange[0])
      ).toFixed(1)
      
      const hour = 6 + Math.floor(Math.random() * 14)
      const minute = Math.floor(Math.random() * 60)
      
      const date = new Date(now)
      date.setDate(date.getDate() - dayOffset)
      date.setHours(hour, minute, 0, 0)
      
      const hasTags = Math.random() > 0.3
      let muscleTags = []
      if (hasTags) {
        const availableTags = typeTagMap[typeInfo.type] || muscleTagOptions.map(t => t.value)
        const numTags = Math.floor(Math.random() * 3) + 1
        const shuffled = [...availableTags].sort(() => Math.random() - 0.5)
        muscleTags = shuffled.slice(0, Math.min(numTags, availableTags.length))
      }
      
      let note = notes[Math.floor(Math.random() * notes.length)]
      if (muscleTags.length > 0) {
        const tagLabels = muscleTags.map(v => muscleTagOptions.find(t => t.value === v)?.label).filter(Boolean)
        const tagText = tagLabels.join('、')
        if (note) {
          note = `【${tagText}】${note}`
        } else {
          note = tagText
        }
      }
      
      checkins.push({
        id: id++,
        type: typeInfo.type,
        typeName: typeInfo.typeName,
        duration: duration,
        amount: amount,
        amountUnit: typeInfo.unit,
        status: statusInfo.value,
        statusText: statusInfo.text,
        note: note,
        muscleTags: muscleTags,
        images: [],
        createTime: date.toISOString()
      })
    }
  }
  
  return checkins.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
}

const getMockPlans = () => {
  const now = new Date()
  return [
    {
      id: 1,
      title: '每周跑步3次',
      type: 'running',
      typeName: '跑步',
      target: '每次5公里，配速6分以内',
      frequency: '每周一、三、五',
      duration: 45,
      weekdays: [1, 3, 5],
      completed: false,
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 24 * 3).toISOString()
    },
    {
      id: 2,
      title: '每天早起做瑜伽',
      type: 'yoga',
      typeName: '瑜伽',
      target: '每天早上6:30开始，30分钟',
      frequency: '每天',
      duration: 30,
      weekdays: [1, 2, 3, 4, 5, 6, 0],
      completed: true,
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 24 * 5).toISOString()
    },
    {
      id: 3,
      title: '每周骑行一次',
      type: 'cycling',
      typeName: '骑行',
      target: '周末骑行20公里',
      frequency: '每周日',
      duration: 90,
      weekdays: [0],
      completed: false,
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 24 * 7).toISOString()
    },
    {
      id: 4,
      title: '力量训练',
      type: 'gym',
      typeName: '健身',
      target: '每周2次力量训练，增肌塑形',
      frequency: '每周二、六',
      duration: 60,
      weekdays: [2, 6],
      completed: false,
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 24 * 2).toISOString()
    }
  ]
}
