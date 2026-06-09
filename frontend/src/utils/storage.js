const CHECKINS_KEY = 'sport_checkins'
const PLANS_KEY = 'sport_plans'
const USER_KEY = 'sport_user'

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

const getMockCheckins = () => {
  const now = new Date()
  return [
    {
      id: 1,
      type: 'running',
      typeName: '跑步',
      duration: 45,
      amount: 5.2,
      amountUnit: '公里',
      status: 'good',
      statusText: '状态良好',
      note: '今天跑步感觉不错，配速稳定',
      images: [],
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 2).toISOString()
    },
    {
      id: 2,
      type: 'cycling',
      typeName: '骑行',
      duration: 60,
      amount: 15,
      amountUnit: '公里',
      status: 'normal',
      statusText: '一般',
      note: '风有点大',
      images: [],
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 24).toISOString()
    },
    {
      id: 3,
      type: 'gym',
      typeName: '健身',
      duration: 90,
      amount: 12,
      amountUnit: '组',
      status: 'good',
      statusText: '状态良好',
      note: '力量训练，胸部和三头肌',
      images: [],
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 48).toISOString()
    },
    {
      id: 4,
      type: 'yoga',
      typeName: '瑜伽',
      duration: 60,
      amount: 1,
      amountUnit: '次',
      status: 'excellent',
      statusText: '非常好',
      note: '身心放松',
      images: [],
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 72).toISOString()
    },
    {
      id: 5,
      type: 'swimming',
      typeName: '游泳',
      duration: 40,
      amount: 1000,
      amountUnit: '米',
      status: 'good',
      statusText: '状态良好',
      note: '自由泳1000米',
      images: [],
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 96).toISOString()
    }
  ]
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
      completed: false,
      createTime: new Date(now.getTime() - 1000 * 60 * 60 * 24 * 7).toISOString()
    }
  ]
}
