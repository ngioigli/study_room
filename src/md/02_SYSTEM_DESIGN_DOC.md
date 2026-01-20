# 系统设计文档 (System Design Document)

**更新日期**: 2026-01-13
**当前版本**: v2.0 (Vue 3 重构版)

---

## 1. 系统架构概述

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        用户层                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   浏览器     │  │   手机端    │  │   Python 客户端     │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
└─────────┼────────────────┼────────────────────┼─────────────┘
          │                │                    │
          ▼                ▼                    ▼
┌─────────────────────────────────────────────────────────────┐
│                       前端层                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Vue 3 + Vite + JavaScript (vue-frontend/)          │   │
│  │  - 开发端口: 5173                                    │   │
│  │  - 生产部署: /app/                                   │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  原版 HTML (static/) - 备份/参考                     │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       后端层                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Spring Boot 2.6.13 (端口: 9090)                     │   │
│  │  - LoginController: 登录/登出                        │   │
│  │  - FocusController: 专注记录                         │   │
│  │  - PetController: 宠物系统                           │   │
│  │  - StatsController: 学习统计                         │   │
│  │  - QrStatusController: 二维码状态                    │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       数据层                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  MySQL 8.x (数据库: study_room)                      │   │
│  │  - users: 用户表                                     │   │
│  │  - user_pets: 宠物表                                 │   │
│  │  - focus_records: 专注记录表                         │   │
│  │  - learning_stats: 学习统计表                        │   │
│  │  - seats: 座位表                                     │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 前端 | Vue 3 | 3.x | UI 框架 |
| 前端 | Vite | 5.x | 构建工具 |
| 前端 | Vue Router | 4.x | 路由管理 |
| 前端 | Pinia | 2.x | 状态管理 |
| 后端 | Spring Boot | 2.6.13 | Web 框架 |
| 后端 | MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| 数据库 | MySQL | 8.x | 关系型数据库 |
| 运行时 | JDK | 17 | Java 运行环境 |
| 运行时 | Node.js | 18+ | 前端开发环境 |

---

## 2. 前端架构设计

### 2.1 Vue 3 项目结构

```
vue-frontend/
├── index.html              # 入口 HTML
├── vite.config.js          # Vite 配置
├── package.json            # 依赖配置
└── src/
    ├── main.js             # 应用入口
    ├── App.vue             # 根组件
    ├── router/             # 路由配置
    │   └── index.js
    ├── stores/             # Pinia 状态管理
    │   ├── user.js         # 用户状态
    │   ├── pet.js          # 宠物状态
    │   └── focus.js        # 专注状态
    ├── views/              # 页面组件
    │   ├── Login.vue       # 登录页
    │   ├── StudyRoom.vue   # 自习室首页
    │   ├── StudyCabin.vue  # 学习小屋
    │   ├── Focus.vue       # 深度专注
    │   ├── Pet.vue         # 宠物详情
    │   ├── Stats.vue       # 学习统计
    │   ├── Profile.vue     # 个人中心
    │   ├── Member.vue      # 会员中心
    │   └── Shredder.vue    # 情绪碎纸机
    ├── components/         # 通用组件
    │   ├── common/
    │   │   ├── BottomNav.vue       # 底部导航
    │   │   ├── QRCodeModal.vue     # 二维码放大弹窗
    │   │   └── ConfirmDialog.vue   # 确认弹窗
    │   ├── focus/
    │   │   ├── Timer.vue           # 计时器
    │   │   ├── WhiteNoise.vue      # 白噪音
    │   │   └── FloatingPet.vue     # 浮动宠物
    │   └── pet/
    │       ├── PetAvatar.vue       # 宠物头像
    │       └── InteractionPanel.vue # 互动面板
    ├── api/                # API 封装
    │   ├── client.js       # HTTP 客户端
    │   ├── auth.js         # 登录 API
    │   ├── focus.js        # 专注 API
    │   ├── pet.js          # 宠物 API
    │   └── stats.js        # 统计 API
    ├── assets/             # 静态资源
    │   ├── css/            # 样式文件
    │   └── audio/          # 音频文件
    └── utils/              # 工具函数
        └── helpers.js
```

### 2.2 路由设计

| 路径 | 组件 | 说明 |
|------|------|------|
| `/login` | Login.vue | 登录页 |
| `/` | StudyRoom.vue | 自习室首页 |
| `/study` | StudyCabin.vue | 学习小屋 |
| `/focus` | Focus.vue | 深度专注 |
| `/pet` | Pet.vue | 宠物详情 |
| `/stats` | Stats.vue | 学习统计 |
| `/profile` | Profile.vue | 个人中心 |
| `/member` | Member.vue | 会员中心 |
| `/shredder` | Shredder.vue | 情绪碎纸机 |

### 2.3 状态管理

使用 Pinia 管理全局状态：

```javascript
// stores/user.js
export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isLoggedIn: false
  }),
  actions: {
    async login(username, password) { ... },
    async logout() { ... }
  }
})

// stores/pet.js
export const usePetStore = defineStore('pet', {
  state: () => ({
    pet: null,
    mood: 100
  }),
  actions: {
    async fetchPet() { ... },
    async interact(type) { ... }
  }
})

// stores/focus.js
export const useFocusStore = defineStore('focus', {
  state: () => ({
    isRunning: false,
    duration: 0,
    todayStats: null
  }),
  actions: {
    start() { ... },
    pause() { ... },
    async save() { ... }
  }
})
```

---

## 3. 后端 API 设计

### 3.1 API 端点列表

#### 认证相关
| 方法 | 端点 | 功能 |
|------|------|------|
| POST | `/api/login` | 用户登录 |
| GET | `/api/logout` | 用户登出 |

#### 专注记录
| 方法 | 端点 | 功能 |
|------|------|------|
| POST | `/api/focus/save` | 保存专注记录 |
| GET | `/api/focus/today` | 获取今日统计 |

#### 宠物系统
| 方法 | 端点 | 功能 |
|------|------|------|
| GET | `/api/pet` | 获取宠物信息 |
| POST | `/api/pet/create` | 创建宠物 |
| POST | `/api/pet/interact` | 宠物互动 |
| GET | `/api/pet/message` | 获取随机鼓励语 |
| POST | `/api/pet/instant-hatch` | 一键孵化（测试） |

#### 学习统计
| 方法 | 端点 | 功能 |
|------|------|------|
| GET | `/api/stats/weekly` | 获取周统计 |
| GET | `/api/stats/monthly` | 获取月统计 |
| GET | `/api/stats/hourly` | 获取时段分布 |
| GET | `/api/stats/profile` | 获取学习画像 |

#### 二维码状态
| 方法 | 端点 | 功能 |
|------|------|------|
| GET | `/api/qr-status/check` | 检查锁定状态 |
| POST | `/api/qr-status/lock` | 锁定二维码 |

---

## 4. 数据库设计

### 4.1 ER 图

```
┌─────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   users     │       │  focus_records  │       │  learning_stats │
├─────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)     │◄──────│ user_id (FK)    │       │ id (PK)         │
│ username    │       │ id (PK)         │       │ user_id (FK)    │
│ password    │       │ duration        │       │ total_duration  │
│ created_at  │       │ exp_gained      │       │ total_sessions  │
└─────────────┘       │ created_at      │       │ streak_days     │
      │               └─────────────────┘       └─────────────────┘
      │
      ▼
┌─────────────┐       ┌─────────────────┐
│  user_pets  │       │     seats       │
├─────────────┤       ├─────────────────┤
│ id (PK)     │       │ id (PK)         │
│ user_id (FK)│       │ seat_code       │
│ name        │       │ status          │
│ type        │       │ user_id (FK)    │
│ level       │       └─────────────────┘
│ exp         │
│ mood        │
│ stage       │
└─────────────┘
```

### 4.2 表结构

详见 `src/main/resources/sql/study_room_init.sql`

---

## 5. 部署架构

### 5.1 开发环境

```
┌──────────────────┐     ┌──────────────────┐
│  Vue Dev Server  │────▶│  Spring Boot     │
│  localhost:5173  │     │  localhost:9090  │
└──────────────────┘     └──────────────────┘
        │                        │
        │  Vite Proxy            │
        │  /api/* ──────────────▶│
        │                        │
                                 ▼
                        ┌──────────────────┐
                        │     MySQL        │
                        │  localhost:3306  │
                        └──────────────────┘
```

### 5.2 生产环境

```
┌──────────────────────────────────────────┐
│           Spring Boot (9090)              │
│  ┌────────────────────────────────────┐  │
│  │  /app/*  →  Vue 3 构建产物         │  │
│  │  /api/*  →  后端 API               │  │
│  │  /*      →  原版 HTML (备份)       │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
                    │
                    ▼
           ┌──────────────────┐
           │     MySQL        │
           └──────────────────┘
```

---

*End of System Design Document*
