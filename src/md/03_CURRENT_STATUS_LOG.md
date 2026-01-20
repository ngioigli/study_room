# Project Status Log & Handover Note (项目现状与交接日志)

**存档日期**: 2026-01-20
**当前阶段**: Phase 5 完成（HTML 前端 + Spring Boot 后端）
**最近更新**: 
- **📦 配置模板归档**: 创建 `config-templates/` 目录，包含 MCP 配置模板和用户规则模板，方便环境迁移
- **🔗 双仓库同步**: 代码同步至 Gitee: `https://gitee.com/abcdxiaocheng/cloud-based-self-study-room`
- **🌐 项目开源**: 仓库已创建并上传至 GitHub: `https://github.com/ngioigli/study_room`
- **🔗 前后端数据对接完善**：
  - study.html：添加缺失的 `formatDuration()` 函数，正确显示今日学习时长
  - index.html：修复用户信息加载，从 `/api/user/info` 获取 nickname
  - member.html：添加用户会员状态检查，根据角色显示不同信息
  - profile.html：完善连续学习天数计算逻辑
- **🔧 管理后台座位显示修复**：修复 admin.html 座位列表显示 undefined 的问题，正确解析嵌套 API 响应结构
- **🚪 扫码入座改为扫码开门**：
  - qrcode_scan.html：标题改为"扫码开门"，说明改为"将手机对准下方二维码进入自习室"
  - index.html：按钮文案改为"扫码进入自习室"→"扫码开门"
- **⚡ 座位管理功能优化**：
  - seat.html：入座按钮改为"占座通电"，离座按钮改为"离座断电"
- **📊 个人中心数据对接**：profile.html 完整对接后端 API，包括用户信息、今日统计、周统计、宠物等级
- **🆕 新增用户信息接口**：`/api/user/info` (GET) 返回当前登录用户信息
- **🔧 专注页面弹窗修复**：focus.html 离开确认弹窗从底部移至屏幕中央
- **🎉 情绪碎纸机全面升级**：完整移植 Bad-Mood-Crusher 项目
  - 4种粉碎方式：火箭发射、强力粉碎、泡泡飘走、黑洞吸入
  - 治愈团子吉祥物：根据状态变化表情（IDLE/PROCESSING/RESULT）
  - 历史记录功能：localStorage 存储治愈旅程
  - AI 安慰话语：预留 Gemini API 配置入口（设置页面可配置 API Key）
  - 完整音效系统：Web Audio API 合成音效
  - 响应式设计：适配手机和桌面
- **✅ 座位管理页面**：seat.html 完成，支持占座通电/离座断电、座位状态查看、电源指示
- **✅ 管理员后台页面**：admin.html 完成，支持座位管理、用户管理、排行榜、数据概览
- **✅ 首页座位状态**：index.html 已对接真实座位 API
- **⚠️ 前端架构调整**：删除 Vue 3 前端项目，仅保留原版 HTML 静态页面
- **✅ 专注页面增强**：宠物快捷互动、白噪音音量控制、页面离开提醒
- **✅ 宠物页面增强**：心情衰减、经验实时更新、自主行为循环、升级提示、对话词条扩展至90+条
- **✅ 座位智能电源控制与订单模块**：后端 API 完成
- **✅ 管理员后台模块**：后端 API 完成
**开发环境**: Windows 11 + JDK 17 + Maven
**前端技术**: 纯 HTML/CSS/JavaScript（部分页面使用 Vue 3 CDN 模式）

---

## 当前项目结构

```
qr_code/
├── src/
│   ├── main/
│   │   ├── java/com/example/qr_code/   # Spring Boot 后端
│   │   │   ├── controller/             # REST API 控制器
│   │   │   ├── service/                # 业务逻辑层
│   │   │   ├── entity/                 # 数据实体
│   │   │   └── mapper/                 # MyBatis Mapper
│   │   └── resources/
│   │       ├── static/                 # 前端静态资源（HTML版本）
│   │       │   ├── *.html              # 页面文件
│   │       │   ├── css/                # 样式文件
│   │       │   ├── js/                 # JavaScript 文件
│   │       │   ├── audio/              # 白噪音音频
│   │       │   └── images/             # 图片资源
│   │       ├── sql/                    # 数据库脚本
│   │       └── application.properties  # 配置文件
│   └── md/                             # 项目文档
├── pom.xml                             # Maven 配置
└── run.bat                             # 启动脚本
```

---

## 1. 基础设施配置 (Infrastructure Config)

*   **本地开发环境**:
    *   JDK: `C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot`
    *   Maven: `C:\Program Files\Maven\apache-maven-3.9.12`
    *   启动脚本: `run.bat`
*   **数据库**:
    *   类型: MySQL 8.x
    *   库名: `study_room`
    *   连接: `localhost:3306`
    *   初始化脚本: `src/main/resources/sql/study_room_init.sql`
*   **端口**:
    *   TCP 9090 (Spring Boot Web服务)

---

## 2. 数据库表结构 (Database Schema)

| 表名 | 说明 | 状态 |
|------|------|------|
| `users` | 用户表 | ✅ 已创建 |
| `seats` | 座位表 | ✅ 已创建 |
| `orders` | 订单表 | ✅ 已创建 |
| `focus_records` | 专注记录表 | ✅ 已创建 |
| `user_pets` | 用户宠物表 | ✅ 已创建 |
| `learning_stats` | 学习统计表 | ✅ 已创建 |
| `access_logs` | 门禁通行记录表 | ✅ 已创建 |

**初始数据**：2个用户（admin/test）+ 8个座位（A01-C02）

---

## 3. 已完成功能清单 (Completed Features)

### 3.1 后端 (Spring Boot)

*   **身份验证**:
    *   `LoginInterceptor` 拦截器
    *   `/api/login`：用户登录（POST）
    *   `/api/logout`：用户登出（GET/POST）
    *   `/api/user/info`：获取当前登录用户信息（GET）
    *   **登录成功后跳转**: `/index.html`（自习室首页）

*   **门禁控制**:
    *   `/upload/qrcode`: Python 客户端上传图片
    *   `/api/qr-status/lock`: 抢占式防冲突逻辑（3分钟锁定）
    *   `/api/qr-status/check`: 状态查询

*   **专注模块**:
    *   `FocusService`: 专注记录保存 + 学习统计更新 + 宠物经验同步
    *   `/api/focus/save`: 保存专注记录（POST）
    *   `/api/focus/today`: 获取今日学习统计（GET）

*   **电子宠物模块**:
    *   `PetService`: 宠物孵化、成长、进化、交互
    *   **心情值系统**: 心情值影响经验获取（满心情+30%经验）
    *   **心情值衰减**: 每5分钟自动减少1点心情值
    *   **多样化互动**: 喂食、抚摸、玩耍、聊天四种互动方式
    *   `/api/pet`: 获取当前宠物（GET）
    *   `/api/pet/create`: 创建宠物（POST）
    *   `/api/pet/interact`: 与宠物互动（POST）
    *   `/api/pet/message`: 获取随机鼓励语（GET）
    *   `/api/pet/instant-hatch`: 一键孵化（POST，测试用）

*   **学习统计模块**:
    *   `StatsService`: 周/月统计、时段分布、学习画像
    *   `/api/stats/weekly`: 获取周统计（GET）
    *   `/api/stats/monthly`: 获取月统计（GET）
    *   `/api/stats/hourly`: 获取时段分布（GET）
    *   `/api/stats/profile`: 获取学习画像（GET）

*   **座位管理模块**:
    *   `SeatService`: 座位业务逻辑
    *   `OrderService`: 订单业务逻辑
    *   `/api/seats`: 获取所有座位列表（GET）
    *   `/api/seats/available`: 获取可用座位（GET）
    *   `/api/seats/{id}`: 获取座位详情（GET）
    *   `/api/seats/{id}/power`: 控制座位电源（POST）
    *   `/api/orders`: 创建订单/入座（POST）
    *   `/api/orders/{id}/end`: 结束订单/离座（PUT）
    *   `/api/orders/current`: 获取当前订单（GET）

*   **管理员后台模块**:
    *   `AdminService`: 管理员业务逻辑
    *   `/api/admin/check`: 检查管理员权限（GET）
    *   `/api/admin/users`: 获取用户列表（GET）
    *   `/api/admin/users/{id}/status`: 更新用户状态（PUT）
    *   `/api/admin/users/{id}/role`: 更新用户角色（PUT）
    *   `/api/admin/seats`: 获取座位列表（GET）
    *   `/api/admin/seats/{id}/release`: 强制释放座位（POST）
    *   `/api/admin/seats/{id}/maintenance`: 设置维护状态（PUT）
    *   `/api/admin/stats/overview`: 今日概览（GET）
    *   `/api/admin/stats/ranking`: 用户排行榜（GET）

### 3.2 前端 (HTML/CSS/JS)

**位置**: `src/main/resources/static/`

| 页面文件 | 功能 | 技术 |
|----------|------|------|
| `login.html` | 云朵背景登录页 | HTML/CSS |
| `index.html` | 自习室首页（扫码开门 + 座位状态） | HTML/CSS/JS |
| `study.html` | 学习小屋（专注入口 + 工具网格） | HTML/CSS/JS |
| `focus.html` | 沉浸式专注（计时器 + 浮动宠物 + 白噪音） | Vue 3 CDN |
| `pet.html` | 宠物详情（互动 + 成长日志） | Vue 3 CDN |
| `profile.html` | 个人中心（用户信息 + 统计 + 设置） | HTML/CSS/JS |
| `member.html` | 会员中心（套餐选择 + 权益展示） | HTML/CSS/JS |
| `shredder.html` | 情绪碎纸机（4种粉碎动画 + 治愈团子 + AI安慰 + 历史记录） | HTML/CSS/JS |
| `stats.html` | 学习统计（ECharts图表） | HTML/CSS/JS |
| `qrcode_scan.html` | 扫码开门页面（二维码扫描进入自习室） | HTML/CSS/JS |
| `seat.html` | 座位管理（占座通电/离座断电 + 状态查看） | HTML/CSS/JS |
| `admin.html` | 管理员后台（座位/用户/排行榜） | HTML/CSS/JS |

**公共资源**：
*   `css/variables.css`: CSS 设计系统变量
*   `css/components.css`: 公共组件样式（底部导航、卡片、按钮）
*   `css/pet-sprites.css`: 宠物动画样式
*   `js/nav.js`: 导航逻辑
*   `js/pet-ai.js`: 宠物行为 AI
*   `audio/`: 白噪音音频文件（rain.mp3, forest.mp3, ocean.mp3, fire.mp3）

### 3.3 客户端 (Python)
*   截图上传脚本（死循环 + 压缩 + 重试）

### 3.4 微信自动化脚本 (Python)

**位置**: `src/main/java/com/example/qr_code/wechat/`

| 文件 | 功能 | 依赖 |
|------|------|------|
| `wechat1.py` | 微信UI自动化：搜索"i湖工"公众号并点击"一码通"菜单 | uiautomation, pyperclip, pywin32 |
| `wechat2.py` | Token直接请求校园码二维码（需配置Authorization和Cookie） | requests, qrcode, Pillow |
| `install_deps.bat` | 一键安装所有Python依赖 | - |
| `run_wechat1.bat` | 运行微信UI自动化脚本 | - |
| `run_wechat2.bat` | 运行Token请求脚本 | - |

**使用方法**：
1. 双击 `install_deps.bat` 安装依赖（首次使用）
2. 双击 `run_wechat1.bat` 或 `run_wechat2.bat` 运行脚本
3. `wechat2.py` 需要先在代码中配置有效的 `MY_AUTH_TOKEN` 和 `MY_COOKIE`（从 Fiddler 抓包获取）

---

## 4. 待开发功能 (Pending Tasks)

### 4.1 高优先级 (P0)
- [x] Java 实体类创建
- [x] Mapper 接口创建
- [x] 专注数据入库
- [x] 座位管理前端页面（HTML版本）
- [x] 管理员后台前端页面（HTML版本）

### 4.2 中优先级 (P1)
- [x] 电子宠物基础功能
- [x] 学习数据统计
- [x] 管理员后台 HTML 页面

### 4.3 低优先级 (P2)
- [x] 情绪碎纸机
- [x] 学习画像可视化

---

## 5. 项目迁移指南

### 5.1 数据库初始化
```bash
# 1. 导入数据库（安全模式：不会覆盖已有数据）
mysql -u root -p < src/main/resources/sql/study_room_init.sql

# 2. 修改 application.properties 中的数据库密码
```

### 5.2 环境要求
- JDK 17+
- Maven 3.9+
- MySQL 8.x

### 5.3 启动项目
```bash
# Windows
run.bat

# 或
mvn spring-boot:run
```

### 5.4 访问地址
- 前端页面: `http://localhost:9090/login.html`
- API 接口: `http://localhost:9090/api/*`

---

## 6. 已知问题与注意事项

1. **Python 脚本 IP**: 服务器 IP 变更时需修改 `UPLOAD_URL`
2. **锁定状态**: 存于内存，重启服务会丢失
3. **文件上传路径**: `application.properties` 中的 `file.upload-dir` 需根据环境调整

---

## 7. 版本历史

### 2026-01-13 架构调整
- **删除 Vue 3 前端项目**：`vue-frontend/` 目录已删除
- **删除 React 前端项目**：`cloud-study-room-ui/` 目录已删除
- **保留 HTML 版本**：`src/main/resources/static/` 下的纯 HTML 页面
- **原因**：Vue/React 版本开发进度慢，Bug 多，决定回归简单的 HTML 方案

---
*End of Status Log*
