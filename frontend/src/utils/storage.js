const CHECKINS_KEY = 'sport_checkins'
const PLANS_KEY = 'sport_plans'
const USER_KEY = 'sport_user'
const GOALS_KEY = 'sport_monthly_goals'

export const getCheckins = () => {
  const data = localStorage.getItem(CHECKINS_KEY)
  return data ? JSON.parse(data) : []
}

export const saveCheckins = (checkins) => {
  localStorage.setItem(CHECKINS_KEY, JSON.stringify(checkins))
}

export const getPlans = () => {
  const data = localStorage.getItem(PLANS_KEY)
  return data ? JSON.parse(data) : []
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
  return data ? JSON.parse(data) : {}
}

export const saveMonthlyGoals = (goals) => {
  localStorage.setItem(GOALS_KEY, JSON.stringify(goals))
}
