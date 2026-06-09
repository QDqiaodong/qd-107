# 运动打卡与健身记录空间

面向健身、跑步、球类等运动爱好者，主打个人运动计划制定、日常打卡与运动数据记录，支持分享运动心得、互相观摩参考的纯内容互动平台。

## 📋 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [端口配置](#端口配置)
- [快速启动](#快速启动)
- [项目结构](#项目结构)
- [开发指南](#开发指南)
- [Docker 部署](#docker-部署)
- [API 接口](#api-接口)

## ✨ 功能特性

### 核心功能模块

| 模块 | 功能说明 |
|------|----------|
| **运动计划制定** | 自定义运动类型、训练时长、执行周期，创建专属个人运动计划并设置执行提醒 |
| **日常运动打卡** | 完成训练后填写运动时长、运动量、身体状态，上传现场图片完成打卡记录 |
| **运动动态浏览** | 浏览其他用户公开打卡内容，按运动类型筛选动态列表 |
| **个人数据统计** | 按周、月汇总打卡次数、运动时长，直观展示个人运动趋势 |

### 技术亮点

- **前端**: Vue3 + Vite，运动表单分步拆分、历史打卡数据本地持久化缓存
- **后端**: SpringBoot3.3 + JDK17 + MyBatis-Plus，Redis集合结构缓存热门数据
- **部署**: Docker容器化，原生Layer分层缓存，国内镜像加速依赖下载

## 🛠️ 技术栈

### 前端
- Vue 3.4.0
- Vite 5.0.8
- Vue Router 4.2.5
- Pinia 2.1.7
- Axios 1.6.2
- Element Plus 2.4.4
- ECharts 5.4.3

### 后端
- Spring Boot 3.3.0
- JDK 17
- MyBatis-Plus 3.5.7
- Spring Data Redis
- MySQL 8.0

## 🔌 端口配置

所有端口统一在 `.env` 文件中配置，避免端口冲突：

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 (Nginx) | **3008** | 固定端口，绑定 127.0.0.1 |
| 后端 (SpringBoot) | **8088** | 固定端口，绑定 127.0.0.1 |
| MySQL | **3309** | 固定端口，绑定 127.0.0.1 |
| Redis | **6399** | 固定端口，绑定 127.0.0.1 |

> ⚠️ **重要**: 所有服务仅绑定 IPv4 回环地址 `127.0.0.1`，不对外网暴露。

## 🚀 快速启动

### 方式一：一键启动脚本（推荐）

```bash
chmod +x start.sh
./start.sh
```

脚本会自动：
- 检查端口占用情况
- 构建并启动所有 Docker 服务
- 等待后端服务就绪
- 打印前端访问地址

### 方式二：Docker Compose 手动启动

```bash
# 加载环境变量并启动
docker compose --env-file .env up --build -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 访问地址

启动成功后访问：

| 地址 | 说明 |
|------|------|
| **http://localhost:3008** | 前端应用（推荐） |
| **http://127.0.0.1:3008** | 前端应用（IPv4直连） |
| http://localhost:8088 | 后端API |
| http://127.0.0.1:8088 | 后端API（IPv4直连） |

## 📁 项目结构

```
qd-107/
├── .env                          # 全局环境变量配置（端口、镜像、数据库）
├── .env.example                  # 环境变量示例
├── docker-compose.yml            # Docker Compose 编排配置
├── start.sh                      # 一键启动脚本
├── README.md                     # 项目说明文档
├── frontend/                     # 前端 Vue3 项目
│   ├── Dockerfile                # 前端 Dockerfile（分层缓存）
│   ├── nginx.conf                # Nginx 配置
│   ├── .npmrc                    # npm 国内镜像源配置
│   ├── package.json              # 前端依赖
│   ├── vite.config.js            # Vite 配置（严格端口绑定）
│   └── src/
│       ├── api/                  # API 接口封装
│       ├── router/               # 路由配置
│       ├── stores/               # Pinia 状态管理
│       ├── utils/                # 工具函数（本地持久化）
│       └── views/                # 页面组件
│           ├── Home.vue          # 首页 - 运动动态浏览
│           ├── Plan.vue          # 运动计划制定
│           ├── Checkin.vue       # 运动打卡（分步表单）
│           └── Profile.vue       # 个人中心 - 数据统计
└── backend/                      # 后端 SpringBoot 项目
    ├── Dockerfile                # 后端 Dockerfile（分层缓存）
    ├── pom.xml                   # Maven 配置（腾讯云镜像）
    ├── settings.xml              # Maven 镜像配置
    └── src/main/
        ├── resources/
        │   ├── application.yml   # 应用配置
        │   └── sql/init.sql      # 数据库初始化脚本
        └── java/com/sport/checkin/
            ├── common/           # 统一响应格式
            ├── config/           # 跨域、MyBatis-Plus、Redis配置
            ├── controller/       # 控制层（6个模块）
            ├── service/          # 业务逻辑层
            ├── mapper/           # 数据访问层
            ├── entity/           # 实体类
            └── dto/              # 数据传输对象
```

## 💻 开发指南

### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 开发模式（自动热更新，端口 3008）
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

### 后端开发

```bash
cd backend

# 本地运行（端口 9030）
mvn spring-boot:run

# 打包
mvn clean package -DskipTests
```

### 本地数据库配置

如需本地开发，修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3309/fitness_db
    username: fitness
    password: fitness123
  data:
    redis:
      host: 127.0.0.1
      port: 6399
```

## 🐳 Docker 部署

### 分层缓存机制

**前端 Dockerfile 分层**:
1. `ARG DOCKER_REGISTRY` / `ARG NODE_IMAGE` - 镜像参数（缓存层）
2. `COPY package*.json ./` - 依赖描述文件（缓存层）
3. `COPY .npmrc ./` - 镜像源配置（缓存层）
4. `RUN npm install` - 安装依赖（缓存层，仅依赖文件变更才重新执行）
5. `COPY . .` - 业务源代码（仅源码修改时触发）
6. `RUN npm run build` - 编译构建（仅源码修改时触发）

**后端 Dockerfile 分层**:
1. `ARG DOCKER_REGISTRY` / `ARG MAVEN_IMAGE` - 镜像参数（缓存层）
2. `COPY pom.xml ./` - Maven 配置（缓存层）
3. `COPY settings.xml` - 镜像源配置（缓存层）
4. `RUN mvn dependency:go-offline` - 下载依赖（缓存层，仅pom.xml变更才重新执行）
5. `COPY src ./src` - 业务源代码（仅源码修改时触发）
6. `RUN mvn clean package` - 编译打包（仅源码修改时触发）

### 镜像源配置

- **Docker 镜像**: DaoCloud 国内镜像 `docker.m.daocloud.io`
- **npm 镜像**: 网易镜像 `https://mirrors.163.com/npm/`
- **Maven 镜像**: 腾讯云镜像 `https://mirrors.cloud.tencent.com/`

### 常用 Docker 命令

```bash
# 启动服务
docker compose up -d

# 重新构建并启动（依赖文件无变更时复用缓存）
docker compose up --build -d

# 停止并删除容器
docker compose down

# 停止并删除容器及数据卷（清空数据库）
docker compose down -v

# 查看服务状态
docker compose ps

# 查看所有服务日志
docker compose logs -f

# 查看单个服务日志
docker compose logs -f frontend
docker compose logs -f backend
docker compose logs -f mysql
docker compose logs -f redis
```

## 📡 API 接口

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/user/login` | 用户登录 |
| GET | `/api/user/{id}` | 获取用户信息 |
| PUT | `/api/user` | 更新用户信息 |

### 运动类型

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/sport-type/hot` | 热门运动类型（Redis缓存） |
| GET | `/api/sport-type/list` | 所有运动类型 |

### 运动计划

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/plan/list` | 计划列表 |
| GET | `/api/plan/{id}` | 计划详情 |
| POST | `/api/plan` | 创建计划 |
| PUT | `/api/plan` | 更新计划 |
| DELETE | `/api/plan/{id}` | 删除计划 |
| POST | `/api/plan/reminder` | 设置提醒 |

### 运动打卡

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/checkin/list` | 打卡列表（分页） |
| GET | `/api/checkin/{id}` | 打卡详情 |
| POST | `/api/checkin` | 创建打卡 |

### 运动动态

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/dynamic/public` | 公开动态列表（Redis缓存） |
| GET | `/api/dynamic/type/{id}` | 按类型筛选 |
| GET | `/api/dynamic/my` | 我的动态 |
| GET | `/api/dynamic/{id}` | 动态详情 |
| POST | `/api/dynamic` | 发布动态 |
| DELETE | `/api/dynamic/{id}` | 删除动态 |
| POST | `/api/dynamic/like` | 点赞动态 |

### 数据统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/statistics/week` | 周统计（打卡次数、时长、卡路里） |
| GET | `/api/statistics/month` | 月统计（打卡次数、时长、卡路里） |

## 🔧 配置说明

### .env 环境变量

```env
# 项目名称
COMPOSE_PROJECT_NAME=fitness-checkin
PROJECT_NAME=fitness-checkin

# Docker 镜像仓库（DaoCloud 国内镜像）
DOCKER_REGISTRY=docker.m.daocloud.io/

# 端口配置
FRONTEND_PORT=3018
BACKEND_PORT=8098
MYSQL_PORT=3319
REDIS_PORT=6399

# MySQL 配置
MYSQL_ROOT_PASSWORD=fitness123
MYSQL_DATABASE=fitness_db
MYSQL_USER=fitness
MYSQL_PASSWORD=fitness123
```

### 修改端口

如需修改端口，直接编辑 `.env` 文件中的对应端口号即可，无需修改其他配置。

## ⚠️ 注意事项

1. **端口冲突**: 启动前脚本会自动检测端口占用，如端口被占用请先关闭占用进程
2. **数据持久化**: MySQL 和 Redis 数据通过 Docker Volume 持久化，`docker compose down` 不会删除数据
3. **国内镜像**: 所有依赖源已配置国内镜像，无需 VPN 即可正常构建
4. **IPv4 绑定**: 所有服务仅绑定 `127.0.0.1`，确保本地开发安全
5. **严格端口**: Vite 配置 `strictPort: true`，端口被占用时直接报错不自动换端口

## 📝 数据库表结构

- `sys_user` - 用户表
- `sport_type` - 运动类型表（预置8种运动类型）
- `sport_plan` - 运动计划表
- `checkin_record` - 打卡记录表
- `sport_dynamic` - 运动动态表
- `dynamic_like` - 动态点赞表

预置运动类型：跑步、骑行、游泳、瑜伽、健身、羽毛球、篮球、徒步

## 🤝 开发约定

- 代码风格遵循各语言官方规范
- 提交信息使用中文语义化描述
- API 接口统一返回格式 `{ code, message, data }`
- 前端使用 localStorage 缓存用户操作数据

## 📄 License

MIT
