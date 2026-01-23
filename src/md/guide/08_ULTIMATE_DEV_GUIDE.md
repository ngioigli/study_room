n# 终极开发指南 - 基于官方任务书的开发规范

**版本**: V2.2 (技术栈升级为 React + TypeScript)  
**最后更新**: 2026-01-13  
**状态**: 开发中

---

## 📋 项目总览

### 项目信息

| 项目 | 内容 |
|------|------|
| **题目** | 基于Spring Boot的无人自习室智能门禁协同与沉浸式助学空间设计与实现 |
| **学生** | 黎志鹏（22软件3班 / 2210412115） |
| **指导教师** | 张湛（讲师） |
| **学院** | 计算机与人工智能学院 |

### 核心价值主张

解决传统付费自习室的**双重痛点**：

1. **管理痛点**：无人化管理难（尾随、占座冲突、安全隐患）
2. **用户痛点**：备考焦虑重（心理压力、孤独感、注意力涣散）

---

## 🏗️ 系统架构

### 技术栈（任务书规定 + 升级说明）

| 层次 | 技术选型 | 说明 |
|------|----------|------|
| **后端** | Spring Boot + MyBatis-Plus | 微服务架构思想，RESTful API |
| **前端** | React 18 + TypeScript + Tailwind CSS | 前后端分离，现代化工程 |
| **构建工具** | Vite 5 | 快速开发服务器，热更新 |
| **数据库** | MySQL | 关系型数据存储 |
| **硬件模拟** | Python + PyAutoGUI | 软硬件通信模拟技术 |
| **架构模式** | B/S + C/S 混合架构 | Web端 + Python客户端 |

**技术栈升级说明** (2026-01-13)：
- **原规定**：Vue.js (CDN 模式)，禁止 Node.js/Webpack 工程化
- **升级为**：React 18 + TypeScript + Vite 5 + Tailwind CSS
- **升级理由**：
  1. 项目复杂度已超出 Vue CDN 模式承载能力（宠物 AI、音频混音、实时同步）
  2. TypeScript 类型安全提升开发效率和代码质量
  3. 前后端分离是现代 Web 开发标准实践，答辩加分项
  4. 距离答辩还有 2 个月，时间充裕
- **部署方式**：开发阶段使用 Vite 开发服务器 (5173)，生产环境构建后部署到 `static/` 目录 (9090)

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         用户端 (Mobile Web)                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │ 登录页面 │  │ 扫码大厅 │  │ 专注模式 │  │ 座位管理 │           │
│  │  login   │  │  qrcode  │  │  focus   │  │   seat   │           │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘           │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Spring Boot 后端服务                            │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │ 登录鉴权   │  │ 门禁控制   │  │ 座位电源   │  │ 专注记录   │   │
│  │ /api/auth  │  │/api/qr-*   │  │/api/seat   │  │/api/focus  │   │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │
│                                  │                                  │
│                    ┌─────────────┴─────────────┐                   │
│                    │       MyBatis-Plus        │                   │
│                    └─────────────┬─────────────┘                   │
└─────────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        ▼                         ▼                         ▼
  ┌───────────┐            ┌───────────┐            ┌───────────────┐
  │   MySQL   │            │ 文件存储  │            │ Python 网关   │
  │  数据库   │            │  (图片)   │            │ (硬件模拟)    │
  └───────────┘            └───────────┘            └───────────────┘
```

---

## 📦 四大核心模块

### 模块一：智能门禁与动态鉴权模块

**任务书要求**：
- 设计一套安全可靠的通行鉴权机制
- 生成具备时效性和防伪能力的动态入场凭证
- 结合软硬件通信模拟技术，实现用户身份的远程核验与门禁指令下发
- 设计有效的防冲突逻辑，确保多人同时请求通行时系统能有序处理

**开发清单**：

| 功能点 | 状态 | 说明 |
|--------|------|------|
| 动态二维码同步 | ✅ 已完成 | Python截图上传 + 前端轮询展示 |
| 抢占式防冲突锁定 | ✅ 已完成 | 3分钟原子化通行窗口 |
| Token 校验 | ✅ 已完成 | 上传接口 Token 验证 |
| 动态凭证时效性 | ⏳ 待开发 | 凭证过期自动失效机制 |
| 防伪能力 | ⏳ 待开发 | 加密签名/动态水印 |
| 门禁指令下发 | ⏳ 待开发 | 与硬件模拟层通信 |

**核心算法 - 原子化通行窗口**：

```java
// QrStatusController.java - 抢占式锁定逻辑
@PostMapping("/lock")
public Result lock(HttpSession session) {
    long now = System.currentTimeMillis();
    
    // 检查是否在冷却期
    if (cooldownEndTime > now) {
        return Result.fail("通道占用中，请等待");
    }
    
    // 原子锁定（3分钟窗口）
    cooldownEndTime = now + 3 * 60 * 1000;
    lockedUserId = session.getAttribute("userId");
    
    return Result.success("锁定成功", cooldownEndTime);
}
```

---

### 模块二：座位智能电源控制与订单模块

**任务书要求**：
- 参考高校图书馆座位管理模式，建立座位电源与订单状态的联动机制
- 根据用户订单状态（入座、离座、超时）自动控制座位电源通断
- 实现资源智能化管理，防止恶意占座与能源浪费
- 管理员可通过后台实时监控座位使用情况并进行人工干预

**开发清单**：

| 功能点 | 状态 | 说明 |
|--------|------|------|
| 座位表设计 | ✅ 已完成 | seats 表（id, number, status, power_on） |
| 订单表设计 | ✅ 已完成 | orders 表（id, user_id, seat_id, start_time, end_time） |
| 扫码入座API | ✅ 已完成 | /api/orders - 入座即通电 |
| 离座断电API | ✅ 已完成 | /api/orders/{id}/end - 离座即断电 |
| 超时自动断电 | ⏳ 待开发 | 定时任务检测超时订单 |
| 管理员后台 | ✅ 已完成 | admin.html - 座位状态面板 + 远程控制 |
| 座位管理前端 | ✅ 已完成 | seat.html - 用户入座/离座页面 |

**数据库设计**：

```sql
-- 座位表
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(10) NOT NULL UNIQUE COMMENT '座位编号',
    status ENUM('available', 'occupied', 'reserved') DEFAULT 'available' COMMENT '状态',
    power_on BOOLEAN DEFAULT FALSE COMMENT '电源状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status ENUM('active', 'completed', 'cancelled', 'timeout') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 模块三：沉浸式助学与专注管理模块

**任务书要求**：
- 针对备考生的学习焦虑，设计情绪安抚功能
- 提供心理暗示与压力宣泄出口，构建支持性的在线学习环境
- 提供白噪音混音与正向计时器，帮助用户屏蔽干扰，集中注意力

**开发清单**：

| 功能点 | 状态 | 说明 |
|--------|------|------|
| 专注计时器 | ✅ 已完成 | 正向计时，极简UI |
| 白噪音Dock | ✅ 已完成 | 4音轨混音（雨/火/林/海） |
| 呼吸感背景 | ✅ 已完成 | CSS动画暗化效果 |
| 专注数据入库 | ✅ 已完成 | 停止时发送时长到后端 |
| 情绪安抚功能 | ✅ 已完成 | 心理暗示文案/动画 |
| 压力宣泄出口 | ✅ 已完成 | 情绪碎纸机（输入 → 碎纸动画 → 不保存） |

**数据库设计**：

```sql
-- 专注记录表
CREATE TABLE focus_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    duration INT NOT NULL COMMENT '专注时长(秒)',
    focus_date DATE NOT NULL COMMENT '专注日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_date (user_id, focus_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 模块四：用户行为激励与数据分析模块

**任务书要求**：
- 实现电子宠物伴读功能，将用户专注时长转化为宠物成长经验值
- 通过游戏化机制提升用户粘性
- 建立用户学习画像，统计并展示用户学习时长分布与习惯
- 辅助用户进行自我管理

**开发清单**：

| 功能点 | 状态 | 说明 |
|--------|------|------|
| 宠物表设计 | ✅ 已完成 | user_pets 表 |
| 宠物孵化机制 | ✅ 已完成 | 新用户获得"蛋" |
| 经验值成长 | ✅ 已完成 | 专注时长 = EXP |
| 进化系统 | ✅ 已完成 | 蛋 → 幼年 → 成年 → 职业形态 |
| 宠物交互 | ✅ 已完成 | 点击弹出鼓励气泡 |
| 学习时长统计 | ✅ 已完成 | 每日/每周/每月统计 |
| 学习画像展示 | ✅ 已完成 | 图表可视化 |
| 学习报表生成 | ⏳ 待开发 | 导出学习报告 |

**数据库设计**：

```sql
-- 用户宠物表
CREATE TABLE user_pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    pet_name VARCHAR(50) DEFAULT '小蛋' COMMENT '宠物名称',
    exp INT DEFAULT 0 COMMENT '经验值',
    level INT DEFAULT 0 COMMENT '等级',
    stage ENUM('egg', 'baby', 'adult', 'professional') DEFAULT 'egg' COMMENT '进化阶段',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学习统计表（按日汇总）
CREATE TABLE learning_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_duration INT DEFAULT 0 COMMENT '当日总时长(秒)',
    focus_count INT DEFAULT 0 COMMENT '专注次数',
    avg_duration INT DEFAULT 0 COMMENT '平均时长(秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_date (user_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📊 开发进度对照表

### 按任务书进度安排

| 阶段 | 时间 | 任务 | 当前状态 |
|------|------|------|----------|
| 前期准备与开题 | 2025.12.20 - 2026.01.15 | 文献查阅、需求分析、数据库设计、开题报告 | 🔄 进行中 |
| 后端核心开发 | 2026.01.16 - 2026.02.28 | Spring Boot环境、登录注册、门禁鉴权、电源控制 | ⏳ 待开始 |
| 前端开发与联调 | 2026.03.01 - 2026.03.20 | 移动端界面、专注模式、前后端联调 | ⏳ 待开始 |
| 拓展功能与中期检查 | 2026.03.21 - 2026.04.10 | 电子宠物、情绪宣泄、学习数据统计、中期检查 | ⏳ 待开始 |
| 系统整合与优化 | 2026.04.11 - 2026.04.30 | 模块集成、全链路测试、性能优化 | ⏳ 待开始 |
| 论文定稿与答辩 | 2026.05.01 - 2026.05.15 | 论文撰写、查重降重、答辩PPT | ⏳ 待开始 |

### 按功能模块进度

| 模块 | 完成度 | 核心功能 |
|------|--------|----------|
| 智能门禁模块 | 80% | 动态二维码✅ 抢占锁定✅ 防伪凭证⏳ |
| 座位电源模块 | 90% | 座位管理✅ 订单联动✅ 超时断电⏳ |
| 沉浸助学模块 | 100% | 计时器✅ 白噪音✅ 情绪碎纸机✅ |
| 激励分析模块 | 95% | 宠物系统✅ 学习统计✅ 学习画像✅ 报表导出⏳ |

---

## 🎯 设计目标检查清单

### 任务书目标对照

- [x] **目标1**：门禁鉴权与防冲突功能（基本完成）
  - [x] 动态凭证生成
  - [x] 防冲突逻辑
  - [x] 安全准入完整流程

- [x] **目标2**：座位电源与订单联动（基本完成）
  - [x] 扫码入座即通电
  - [x] 订单结束即断电
  - [x] 管理员后台监控

- [x] **目标3**：专注模式与情绪疏导（已完成）
  - [x] 专注计时器
  - [x] 白噪音播放
  - [x] 情绪碎纸机

- [x] **目标4**：虚拟伴读与学习数据（基本完成）
  - [x] 宠物成长逻辑
  - [x] 学习数据统计
  - [x] 学习报表/画像

- [ ] **目标5**：系统测试与优化
  - [ ] 功能测试
  - [ ] 性能优化
  - [ ] 系统演示

- [ ] **目标6**：文档撰写
  - [ ] 5000字英文文献翻译
  - [ ] 1.5万字毕业论文

---

## 📁 项目文件结构

```
src/
├── main/
│   ├── java/com/example/qr_code/
│   │   ├── QrCodeApplication.java          # 启动类
│   │   ├── config/
│   │   │   └── WebConfig.java              # 拦截器+静态资源配置
│   │   ├── controller/
│   │   │   ├── LoginController.java        # 登录/登出
│   │   │   ├── FileUploadController.java   # 图片上传
│   │   │   ├── QrStatusController.java     # 门禁状态
│   │   │   ├── SeatController.java         # [待开发] 座位管理
│   │   │   ├── FocusController.java        # [待开发] 专注记录
│   │   │   └── PetController.java          # [待开发] 宠物系统
│   │   ├── entity/
│   │   │   ├── User.java                   # 用户实体
│   │   │   ├── Seat.java                   # [待开发] 座位实体
│   │   │   ├── Order.java                  # [待开发] 订单实体
│   │   │   ├── FocusRecord.java            # [待开发] 专注记录
│   │   │   ├── UserPet.java                # [待开发] 宠物实体
│   │   │   └── LearningStats.java          # [待开发] 学习统计
│   │   ├── mapper/
│   │   │   └── UserMapper.java             # 用户Mapper
│   │   ├── service/                        # [待开发] 业务逻辑层
│   │   └── interceptor/
│   │       └── LoginInterceptor.java       # 登录拦截器
│   └── resources/
│       ├── application.properties          # 配置文件
│       └── static/
│           ├── login.html                  # 登录页
│           ├── qrcode.html                 # 扫码大厅
│           ├── focus.html                  # 专注模式
│           ├── seat.html                   # [待开发] 座位管理
│           ├── pet.html                    # [待开发] 宠物页面
│           ├── stats.html                  # [待开发] 学习统计
│           ├── admin.html                  # [待开发] 管理员后台
│           ├── audio/                      # 白噪音音频
│           └── images/                     # 图片资源
└── md/
    ├── 00_AI_DEVELOPER_RULES.md            # AI开发准则
    ├── 01_PRODUCT_REQUIREMENTS_DOC.md      # 产品需求文档
    ├── 02_SYSTEM_DESIGN_DOC.md             # 系统设计文档
    ├── 03_CURRENT_STATUS_LOG.md            # 当前状态日志
    ├── 04_THESIS_OUTLINE.md                # 论文大纲
    ├── 05_AI_HANDOVER_PROTOCOL.md          # AI交接协议
    ├── 07_FINAL_TASK_BOOK.md               # 官方任务书定稿
    └── 08_ULTIMATE_DEV_GUIDE.md            # 本文档
```

---

## 🔧 开发规范

### 后端规范

1. **API 命名**：`/api/{模块}/{动作}`，如 `/api/seat/checkin`
2. **返回格式**：统一使用 `Result<T>` 封装
3. **异常处理**：全局异常处理器 + 业务异常码
4. **注释要求**：类注释 + 核心方法注释

### 前端规范

1. **技术栈**：React 18 + TypeScript + Tailwind CSS
2. **Mobile-first**：优先适配手机端
3. **响应式布局**：Flex/Grid + Tailwind 响应式类
4. **交互反馈**：按钮点击状态、加载动画、错误提示
5. **状态管理**：React Context + useReducer
6. **API 调用**：统一封装 fetch/axios，处理错误和 loading 状态

### 数据库规范

1. **命名**：表名小写下划线，如 `focus_records`
2. **主键**：统一使用 `BIGINT AUTO_INCREMENT`
3. **时间字段**：`created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
4. **索引**：高频查询字段建立索引

---

## 📚 参考文献（任务书指定）

[1] 屈子淳,何嘉盛,陆美辰,等.共享经济背景下付费自习室现状及推行难点破解研究[J].中国集体经济,2022,(11):66-68.

[2] 兰旭辉,熊家军,邓刚.基于MySQL的应用程序设计[J].计算机工程与设计,2004,(03):442-443+468.

[3] 朱启方,黄彩霞,范旭,等.基于SpringBoot和Vue的多功能时间管理系统的设计与实现[J].电脑知识与技术,2022,18(18):31-32+41.

[4] 吴超,张建兵,李红霞,等.基于STM32单片机的二维码门禁控制系统[J].科技创新与应用,2024,14(25):49-52.

[5] 丁枝秀,王国栋.基于Web的图书馆览阅座位管理系统[J].江苏科技信息,2020,37(20):7-11.

[6] 蔡其君.在线陪伴学习系统的设计与实现[D].北京交通大学硕士学位论文,2022.

---

*本文档基于官方任务书定稿，作为后续开发的终极参考指南。*
