export const sportTypes = [
  { value: 'running', label: '跑步', icon: 'Running', color: '#67c23a' },
  { value: 'cycling', label: '骑行', icon: 'Bicycle', color: '#409eff' },
  { value: 'swimming', label: '游泳', icon: 'Watermelon', color: '#f56c6c' },
  { value: 'yoga', label: '瑜伽', icon: 'Moon', color: '#e6a23c' },
  { value: 'gym', label: '健身', icon: 'Sugar', color: '#85ce61' },
  { value: 'other', label: '其他', icon: 'MoreFilled', color: '#909399' }
]

export const bodyStatus = [
  { value: 'excellent', label: '非常好', color: '#67c23a' },
  { value: 'good', label: '状态良好', color: '#85ce61' },
  { value: 'normal', label: '一般', color: '#e6a23c' },
  { value: 'tired', label: '比较累', color: '#f56c6c' }
]

export const muscleTags = [
  { value: 'easy', label: '轻松', emoji: '😊', color: '#67c23a' },
  { value: 'sweaty', label: '爆汗', emoji: '💦', color: '#409eff' },
  { value: 'soreLegs', label: '腿酸', emoji: '🦵', color: '#e6a23c' },
  { value: 'soreArms', label: '胳膊酸', emoji: '💪', color: '#e6a23c' },
  { value: 'soreAbs', label: '腹部酸', emoji: '🔥', color: '#f56c6c' },
  { value: 'energetic', label: '精力充沛', emoji: '⚡', color: '#85ce61' },
  { value: 'tired', label: '疲惫', emoji: '😫', color: '#909399' },
  { value: 'refreshed', label: '神清气爽', emoji: '🌿', color: '#67c23a' },
  { value: 'normal', label: '状态一般', emoji: '😐', color: '#909399' },
  { value: 'pain', label: '有点疼', emoji: '😣', color: '#f56c6c' },
  { value: 'breathless', label: '喘得厉害', emoji: '😤', color: '#409eff' },
  { value: 'pumped', label: '泵感十足', emoji: '💯', color: '#67c23a' }
]

export const getSportTypeInfo = (type) => {
  return sportTypes.find(t => t.value === type) || sportTypes[sportTypes.length - 1]
}

export const formatDate = (dateStr, format = 'YYYY-MM-DD HH:mm') => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
}

export const timeAgo = (dateStr) => {
  const now = new Date()
  const date = new Date(dateStr)
  const diff = now - date
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  
  return formatDate(dateStr, 'YYYY-MM-DD')
}

export const getWeekRange = (date = new Date()) => {
  const d = new Date(date)
  const dayOfWeek = d.getDay()
  const offset = (dayOfWeek + 6) % 7
  const weekStart = new Date(d)
  weekStart.setDate(d.getDate() - offset)
  weekStart.setHours(0, 0, 0, 0)
  const weekEnd = new Date(weekStart)
  weekEnd.setDate(weekStart.getDate() + 6)
  weekEnd.setHours(23, 59, 59, 999)
  return { weekStart, weekEnd }
}

export const getMonthRange = (date = new Date()) => {
  const d = new Date(date)
  const monthStart = new Date(d.getFullYear(), d.getMonth(), 1)
  monthStart.setHours(0, 0, 0, 0)
  const monthEnd = new Date(d.getFullYear(), d.getMonth() + 1, 0)
  monthEnd.setHours(23, 59, 59, 999)
  return { monthStart, monthEnd }
}

export const isInWeekRange = (dateStr, weekStart, weekEnd) => {
  const t = new Date(dateStr)
  return t >= weekStart && t <= weekEnd
}

export const isInMonthRange = (dateStr, monthStart, monthEnd) => {
  const t = new Date(dateStr)
  return t >= monthStart && t <= monthEnd
}

export const getWeekdayIndexFromMonday = (date) => {
  const d = new Date(date)
  return (d.getDay() + 6) % 7
}

export const caloriePerMinuteMap = {
  running: 10.00,
  cycling: 8.00,
  swimming: 12.00,
  yoga: 4.00,
  gym: 7.00,
  fitness: 7.00,
  badminton: 6.50,
  basketball: 9.00,
  hiking: 5.50,
  other: 5.00
}

export const getCaloriePerMinute = (type) => {
  return caloriePerMinuteMap[type] || caloriePerMinuteMap.other
}

export const calculateCalorie = (record) => {
  if (!record) return 0
  if (record.calorie != null) return Math.round(record.calorie)
  const duration = record.duration || 0
  const type = record.type || 'other'
  const caloriePerMinute = getCaloriePerMinute(type)
  return Math.round(duration * caloriePerMinute)
}

export const extractCommonPhrases = (checkins, sportType = '', limit = 8) => {
  const phraseCount = {}
  const punctuationRegex = /[，。！？、；：""''（）\[\]【】\n\r,.!?;:'"()\[\]]/g

  const filteredCheckins = sportType
    ? checkins.filter(c => c.type === sportType && c.note && c.note.trim())
    : checkins.filter(c => c.note && c.note.trim())

  filteredCheckins.forEach(checkin => {
    const note = checkin.note.trim()
    if (!note) return

    const phrases = note.split(punctuationRegex).filter(p => p.trim().length >= 2)

    phrases.forEach(phrase => {
      const trimmed = phrase.trim()
      if (trimmed.length >= 2 && trimmed.length <= 20) {
        phraseCount[trimmed] = (phraseCount[trimmed] || 0) + 1
      }
    })

    if (note.length >= 2 && note.length <= 20) {
      phraseCount[note] = (phraseCount[note] || 0) + 2
    }
  })

  const sortedPhrases = Object.entries(phraseCount)
    .sort((a, b) => b[1] - a[1])
    .slice(0, limit)
    .map(([phrase]) => phrase)

  return sortedPhrases
}
