<template>
  <div class="metric-bar" :style="containerStyle">
    <span class="metric-icon">{{ icon }}</span>
    <div class="metric-track">
      <div class="metric-fill" :style="fillStyle"></div>
    </div>
    <span class="metric-value">{{ displayValue }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  icon: { type: String, required: true },
  value: { type: [Number, String], required: true },
  unit: { type: String, default: '' },
  color: { type: String, default: '#606266' },
  fillPercent: { type: Number, default: 0 },
  maxPercent: { type: Number, default: 100 }
})

const displayValue = computed(() => {
  if (props.unit) return `${props.value}${props.unit}`
  return props.value
})

const containerStyle = computed(() => ({
  '--metric-color': props.color
}))

const fillStyle = computed(() => {
  const pct = Math.min(Math.max(props.fillPercent, 0), props.maxPercent)
  return {
    width: pct + '%',
    background: `linear-gradient(90deg, ${props.color}33, ${props.color})`
  }
})
</script>

<style scoped>
.metric-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 22px;
  padding: 0 4px;
}

.metric-icon {
  font-size: 13px;
  flex-shrink: 0;
  width: 16px;
  text-align: center;
}

.metric-track {
  flex: 1;
  min-width: 0;
  height: 6px;
  background: #ebeef5;
  border-radius: 3px;
  overflow: hidden;
}

.metric-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.35s ease;
}

.metric-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--metric-color);
  flex-shrink: 0;
  min-width: 0;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .metric-bar {
    height: 20px;
    gap: 4px;
  }

  .metric-icon {
    font-size: 12px;
  }

  .metric-track {
    height: 5px;
  }

  .metric-value {
    font-size: 11px;
  }
}
</style>
