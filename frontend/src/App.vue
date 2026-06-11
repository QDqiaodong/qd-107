<template>
  <div class="app-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <el-icon :size="28" color="#409EFF"><Timer /></el-icon>
            <span class="logo-text">运动打卡</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="nav-menu"
            mode="horizontal"
            router
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/plan">
              <el-icon><List /></el-icon>
              <span>运动计划</span>
            </el-menu-item>
            <el-menu-item index="/goal">
              <el-icon><Flag /></el-icon>
              <span>月度目标</span>
            </el-menu-item>
            <el-menu-item index="/checkin">
              <el-icon><Edit /></el-icon>
              <span>运动打卡</span>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 0;
  height: 70px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 22px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  border-bottom: none;
  background: transparent;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 30px 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .header {
    height: auto;
    padding: 10px 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 10px;
  }
  
  .nav-menu {
    width: 100%;
    justify-content: center;
  }
  
  .main-content {
    padding: 20px 15px;
  }
}
</style>
