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
