# Project Status Log & Handover Note (项目现状与交接日志)

**存档日期**: 2026-01-22
**当前阶段**: Phase 5 完成（HTML 前端 + Spring Boot 后端）
**最近更新**: 
- **🎨 study.html 页面布局与数据加载优化（2026-01-22）**:
  - **问题**：study.html 页面显示异常，"开始专注"卡片内容溢出，今日数据显示为"-"
  - **修复内容**：
    - 修改 `.focus-entry-card` 样式：从 `display: block` 改为 `display: flex` + `flex-direction: column`，并调整 padding
    - 优化 `.today-stats` 样式：修改背景透明度、padding、width 等属性，确保内容正确自适应
    - 优化 `.today-stat-value` 和 `.today-stat-label` 的字体大小与间距
    - 增强 `loadTodayStats()` 和 `loadPetInfo()` 函数错误处理，添加详细的 console.log 日志便于调试
    - 当 API 失败时显示默认值而非"-"
  - **BUG修复**：移除重复声明的 `PET_EMOJIS` 常量（与 pet-ai.js 中的定义冲突导致 JavaScript 语法错误）
- **📅 座位预约系统时间选择优化（2026-01-22）**:
  - **需求**：预约需提前至少1小时，开始前30分钟内不可取消
  - **前端修复内容**（reservation.html）：
    - 重写 `generateTimeOptions()` 函数：开始时间只显示当前时间1小时后的最近半小时时间点
    - 新增 `updateEndTimeOptions()` 函数：结束时间至少是开始时间的1小时之后，最多4小时
    - 新增 `onStartTimeChange()` 函数：开始时间变更时自动更新结束时间选项
    - 修改 `selectDate()` 函数：切换日期时重新生成时间选项
    - 更新提示文案：⏰ 预约需提前至少1小时，时长1-4小时
  - **后端修复内容**（ReservationService.java）：
    - 新增"预约需提前至少1小时"验证
    - 新增"预约时长1-4小时"验证
    - 新增"营业时间08:00-22:00"验证
    - 新增"用户只能有一个待使用预约"验证
    - 简化预约日期限制为只允许今天和明天
- **🚀 超级团队模式 5 大改进完成（2026-01-22）**:
  - **改进点 #1：专注结算逻辑补全**
    - 前端 `focus.html` 新增 `saveFocusRecordWithRetry()` 自动重试机制（最多 3 次，指数退避 1s/2s/4s）
    - 新增 `localStorage` 离线缓存：`focus_offline_records` 保存失败记录，网络恢复自动同步
    - 添加网络状态监听（online/offline 事件）
  - **改进点 #2：后端幂等性校验**
    - `FocusRecord.java` 新增 `clientId` 字段
    - `FocusService.saveFocusRecord()` 支持 clientId 去重，重复请求返回已有记录
    - 数据库新增 `focus_records.client_id` 字段和索引
  - **改进点 #3：数据一致性强化**
    - 所有事务方法使用 `@Transactional(rollbackFor = Exception.class)` 确保原子性
  - **改进点 #4：统一响应类**
    - 新增 `com.example.qr_code.common.ApiResponse<T>` 统一响应类
    - 支持 `success` + `code` 双字段兼容模式
    - 提供 `success()`, `error()`, `unauthorized()`, `badRequest()` 等静态工厂方法
  - **改进点 #5：座位并发冲突处理**
    - `SeatMapper.java` 新增 `selectByIdForUpdate()` 行级锁查询方法
    - `SeatService.occupySeat()` 使用 `FOR UPDATE` 防止并发抢座
  - **改进点 #6：排行榜隐私保护**
    - `users` 表新增 `hide_ranking` 字段（0-显示，1-隐藏）
    - `User.java` 实体新增 `hideRanking` 字段
    - `LearningStatsMapper` 排行榜查询自动过滤 `hide_ranking=1` 的用户
    - `UserController` 新增 `/api/user/privacy` 和 `/api/user/privacy/ranking` 接口
    - `UserService` 新增 `updateHideRanking()` 方法
- **🧩 新增项目定制超级团队提示词手册（2026-01-22）**:
  - 新增 `src/md/12_AI_SUPER_TEAM_PROMPT_PACK.md`，将“超级团队 AI 提示词”按当前仓库现状与 `.codebuddy` 规则进行定制（技术栈边界、接口/页面/表结构、文档同步、MCP 模板使用说明等）。
- **🛑 专注停止按钮保存修复（2026-01-22）**:
  - **问题**：点击"停止"按钮后没有触发保存专注记录
  - **修复内容**：
    - 将 stopTimer 函数改为 async，使用 await 等待 saveFocusRecord 完成
    - 添加 isStopping 标志防止 visibilitychange 事件干扰保存流程
    - 增强日志输出便于调试
    - 修复自由模式下调用 saveFocusRecord 时缺少 type 参数的问题
- **🎯 专注页面经验结算修复（2026-01-22）**:
  - **问题**：focus.html 专注结束后没有经验结算，今日学习/专注次数/今日经验显示为 0
  - **修复内容**：
    - 修复 FocusController.saveFocusRecord 中 duration 参数类型转换问题（Integer 改为 Number 兼容处理）
    - 增强 saveFocusRecord 前端函数的错误处理和日志输出
    - 改进 stopTimer 函数，明确专注不足1分钟的提示
    - 为 loadTodayStats 和 loadPetInfo 添加控制台日志便于调试
    - 增加宠物升级检测和提示
    - **数据库修复**：为 `focus_records` 表添加 `type` 字段（VARCHAR(20) DEFAULT 'free'），区分 free/pomodoro 模式
- **📊 学习统计页面修复（2026-01-22）**:
  - **问题**：stats.html 学习时段分布不显示，学习画像一直显示"加载中"
  - **修复内容**：
    - 修复 updateHourlyChart 函数中 hourlyData 数据访问逻辑（从按索引访问改为按 hour 字段映射）
    - 增强错误处理，API 失败时显示友好的空状态
    - 添加 showEmptyChart 和 showProfileError 辅助函数
- **📱 扫码页面图片加载修复（2026-01-22）**:
  - **问题**：qrcode_scan.html 页面尝试加载不存在的 dynamic_qrcode.jpg 导致控制台报错
  - **修复内容**：
    - 为 qrcodeImg 添加 onerror 事件处理，图片加载失败时显示友好占位符
    - 更新 WebConfig 静态资源映射，添加 classpath 回退路径
  - **说明**：动态二维码由外部微信系统通过 /upload/qrcode API 上传
- **🗄️ 数据库字段修复（2026-01-22）**:
  - **问题**：learning_stats 表缺少 tomato_count 字段导致管理后台报错
  - **修复内容**：
    - 通过 ALTER TABLE 为 learning_stats 表添加 tomato_count 字段
    - 更新 study_room_init.sql 初始化脚本，添加 tomato_count 字段定义
  - **影响**：管理后台概览数据加载恢复正常
- **🔒 安全性与代码质量全面修复（2026-01-22）**:
  - **前后端接口对齐修复**：
    - 修复index.html中订单API路径不一致问题（`/api/order/` → `/api/orders/`）
    - 修复离座功能API调用错误（`POST /api/order/leave` → `PUT /api/orders/{id}/end`）
  - **XSS安全漏洞修复**：
    - 在encouragement.html、admin.html中添加HTML转义函数防止XSS攻击
    - 对所有用户输入数据进行HTML转义处理
  - **权限控制增强**：
    - OrderController的getAllActiveOrders接口添加管理员权限验证
    - 防止普通用户访问管理员功能
  - **单元测试更新**：
    - 新增testGetAllActiveOrders_NotAdmin测试用例
    - 更新testGetAllActiveOrders_Success确保管理员权限验证
    - 所有193个测试用例全部通过
  - **代码质量提升**：
    - 移除TODO标记，完善待办功能
    - 统一前后端接口规范
    - 增强用户数据安全性
- **✅ Controller 单元测试全覆盖完成（2026-01-22）**:
  - **已完成13个Controller的单元测试，共计193个测试用例**
  - 新增测试：
    - RankingController 单元测试：涵盖今日/本周/本月/年度/总排行榜及用户排名查询等 10 个测试用例
    - EncouragementController 单元测试：涵盖鼓励卡片获取、创建、点赞、隐藏等 12 个测试用例
    - OrderController 单元测试：涵盖订单创建、结束、取消、查询、历史记录等 19 个测试用例（新增权限测试）
    - QrStatusController 单元测试：涵盖二维码状态检查、锁定、并发控制等 7 个测试用例
  - **测试覆盖率**：
    - Controller 层：100% 接口覆盖
    - 核心业务逻辑：80%+ 覆盖
  - 使用 JUnit 5 + Mockito 4.11.0 测试框架，所有测试用例均符合规范
- **🧪 Controller 单元测试继续扩展（2026-01-22）**:
  - MessageController 单元测试：涵盖文本留言管理、回复功能等 27 个测试用例（不包含图片上传功能）
  - SeatController 单元测试：涵盖座位查询、电源控制、状态管理等 17 个测试用例
  - 累计完成 9 个 Controller 的单元测试，共计 131 个测试用例
- **🧪 Controller 单元测试扩展（2026-01-21）**:
  - PetController 单元测试：涵盖宠物管理、互动、孵化等 16 个测试用例
  - FocusController 单元测试：涵盖专注记录保存、统计查询等 15 个测试用例  
  - AdminController 单元测试：涵盖管理员权限、用户管理、座位管理、数据统计等 31 个测试用例
- **🧪 Controller 单元测试创建（2026-01-21）**:
  - ReservationController 单元测试：涵盖预约管理、座位查询、签到取消等 17 个测试用例
  - UserController 单元测试：涵盖用户资料管理、昵称头像更新等 12 个测试用例
  - StatsController 单元测试：涵盖学习统计、时段分布、学习画像等 8 个测试用例
  - LoginController 单元测试：涵盖登录登出、权限验证、用户信息获取等 8 个测试用例
- **🔧 多处Bug修复（2026-01-21）**:
  - stats.html：返回按钮跳转从 `/study.html` 改为 `/index.html`
  - ranking.html：前20名显示排名序号，第21名及以后隐藏排名序号
  - reservation.html：新增预约时间限制（需提前至少1小时）、取消时间限制（开始前30分钟内不可取消）
  - encouragement.html：页面改名为"温暖传递"，移除分享按钮
  - study.html/index.html：更新"匿名鼓励"入口名称为"温暖传递"
- **🔧 专注模式UI优化**: 
  - focus.html：将番茄钟/自由计时模式切换移入专注设置面板
  - 修复番茄钟信息与经验条重叠问题（调整 .pomodoro-info 的 top 值从 70px 到 140px）
  - 在番茄钟信息栏添加"+20%经验"标注
  - 设置面板内的模式切换按钮显示经验奖励提示
- **🔧 座位预约系统重构**: 
  - reservation.html：完全重构预约页面
  - 预约日期限定为今天和明天
  - 每位用户最多只能有1个待使用的预约
  - 支持自选时间段（开始-结束时间选择器，时长1-4小时）
  - ReservationController.java：新增 `/api/reservation/pending` 接口
  - ReservationService.java：新增 `getUserPendingReservation()` 方法
- **🔧 学习统计页面修复**: 
  - stats.html：返回按钮跳转从 `/study.html` 改为 `/index.html`
- **🔧 学习小屋工具简化**: 
  - study.html：删除番茄钟和白噪音独立入口（已集成到专注模式）
  - 保留学习日历、匿名鼓励、情绪碎纸机、学习统计4个工具入口
- **🔧 留言板功能简化**: 
  - message.html：暂时移除图片上传功能
  - 移除图片选择按钮和预览区域
  - 发布接口改为JSON格式（不再使用FormData）
- **🆕 测试数据创建**: 
  - 新增5个测试用户（test_xiaoming等）
  - 插入最近7天的专注记录数据
  - 插入座位预约测试数据
  - 插入学习统计测试数据
- **🔧 留言板图片上传功能**: 
  - MessageController.java：支持 MultipartFile 图片上传，最多9张图片，单张最大2MB
  - MessageService.java：新增带图片参数的 createMessage 重载方法
  - MessageBoard.java：新增 images 字段存储图片URL（逗号分隔）
  - schema.sql：message_board 表新增 images 字段
  - message.html：完整图片上传/预览/显示功能
  - WebConfig.java：配置 /uploads/** 静态资源映射和拦截器白名单
- **🔧 番茄钟合并到专注模式**: 
  - focus.html：新增模式切换器（自由计时/番茄钟模式）
  - 番茄钟模式支持25分钟工作/5分钟短休息/15分钟长休息循环
  - 番茄钟完成有额外20%经验奖励
  - 完成番茄时有专属提示音和宠物反馈
- **🔧 学习日历改为今日学习总结**: 
  - calendar.html：完全重构为"今日学习总结"页面
  - 显示今日收获番茄数（大卡片）
  - 显示学习时长、专注次数统计
  - 主要学习时段分布（只统计>10分钟的专注记录）
  - 今日专注记录列表（带类型标识：🍅番茄钟/⏱️自由专注）
  - 根据表现生成个性化鼓励语
- **🆕 新增API**: 
  - GET /api/focus/today/details：获取今日学习详情（含所有专注记录）
- **🔧 数据库更新**: 
  - focus_records表新增type字段（free/pomodoro）
  - learning_stats表新增tomato_count字段
- **🔧 前后端开发强制规范**: 
  - .codebuddy/rules/assistant-identity.mdc：新增"前后端开发强制规范"
  - 禁止只写前端不写后端，禁止只写后端不写前端
  - 接口必须对齐，单元测试覆盖率要求
  - 开发检查清单
- **🔧 数据库schema修复**: 
  - schema.sql：修复所有缺失的表（seat_reservations, message_board, message_replies, encouragement_cards, system_config）
  - 修复实体类@TableName注解与数据库表名一致（使用复数形式）
- **🆕 扩展功能完成**: 
  - pomodoro.html：番茄钟页面，支持专注/短休息/长休息模式，可配置时长，进度环可视化，本地统计
  - calendar.html：学习日历页面，月历视图展示学习记录，连续学习天数统计，详情查看
  - encouragement.html：匿名鼓励卡片页面，随机展示鼓励语，支持发送、点赞、分享
  - EncouragementCard.java：鼓励卡片实体类
  - EncouragementCardMapper.java：鼓励卡片数据访问层
  - EncouragementService.java：鼓励卡片业务逻辑
  - EncouragementController.java：鼓励卡片API控制器
  - encouragement_card 数据库表：存储用户发送的鼓励卡片
- **🔧 首页优化**: 
  - index.html：新增"常用功能"快捷入口区，包含番茄钟、学习日历、匿名鼓励、排行榜、留言板、座位预约
- **🔧 学习小屋优化**: 
  - study.html：工具网格扩展至6个入口，新增番茄钟、学习日历、匿名鼓励
- **🧭 强化AI身份规则**: 
  - .codebuddy/rules/assistant-identity.mdc：新增UI质量硬性门槛（组件层级/交互状态/移动端适配/视觉一致性）
- **🧭 强化AI身份规则**: 
  - .codebuddy/rules/assistant-identity.mdc：新增"每次对话开头明确身份"与标准需求输出模板
- **🧭 新增AI身份规则**: 
  - .codebuddy/rules/assistant-identity.mdc：明确AI身份、技术栈边界、产品调性与交付准则
- **🔧 登录页面禁用用户提示优化**: 
  - login.html：当用户被禁用时，显示服务器返回的具体错误消息"您的账号已被禁用，请联系管理员"
  - 区分403禁用状态和401认证失败状态的错误提示
- **💺 座位管理页面优化**:
  - 添加返回按钮（左上角圆形按钮）
  - 增加面板底部间距，确保"占座通电"和"离座断电"按钮不被底部导航遮挡
- **🔒 用户禁用功能彻底修复**: 
  - LoginInterceptor.java：改为 `@Component` 注入，从数据库实时查询用户状态
  - 每次请求都会检查数据库中的用户status字段，确保被禁用的用户立即失效
  - WebConfig.java：通过 `@Autowired` 注入 LoginInterceptor
- **🔒 用户禁用功能修复**: 
  - LoginController.java：登录时检查用户status字段，禁用用户无法登录
  - LoginInterceptor.java：拦截器实时检查用户禁用状态，被禁用的用户会话会被强制注销
  - 登录页面支持显示禁用提示（?error=disabled）
- **👥 管理后台用户模块增强**:
  - 用户列表显示更多统计数据：今日专注次数、今日学习时长、本月专注次数、总学习时长、扫码进门总次数
  - 新增用户详情弹窗：点击用户可查看完整统计（今日/本周/本月/累计数据）
  - 新增7天学习趋势图表
  - 新增连续学习天数统计
  - 禁用用户显示特殊标识
- **📊 统计数据修复**: 
  - 今日专注次数从 learning_stats 表获取（而非 focus_records 表），确保数据准确
  - 排行榜同时显示专注次数
- **🆕 新增后端API**:
  - `GET /api/admin/users?withStats=true`：获取用户列表（含统计数据）
  - `GET /api/admin/users/{id}/detail`：获取单个用户详细统计
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
| `focus_records` | 专注记录表（含type字段：free/pomodoro） | ✅ 已创建 |
| `user_pets` | 用户宠物表 | ✅ 已创建 |
| `learning_stats` | 学习统计表（含tomato_count字段） | ✅ 已创建 |
| `access_logs` | 门禁通行记录表 | ✅ 已创建 |
| `seat_reservations` | 座位预约表 | ✅ 已创建 |
| `message_board` | 留言板表 | ✅ 已创建 |
| `message_replies` | 留言回复表 | ✅ 已创建 |
| `encouragement_cards` | 鼓励卡片表 | ✅ 已创建 |
| `system_config` | 系统配置表 | ✅ 已创建 |

**初始数据**：2个用户（admin/test）+ 8个座位（A01-C02）+ 8条鼓励卡片

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
    *   `/api/focus/save`: 保存专注记录（POST，支持type参数：free/pomodoro）
    *   `/api/focus/today`: 获取今日学习统计（GET，含tomatoCount）
    *   `/api/focus/today/details`: 获取今日学习详情（GET，含所有专注记录）

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
    *   `/api/admin/users`: 获取用户列表（GET，支持 ?withStats=true 获取统计数据）
    *   `/api/admin/users/{id}/detail`: 获取用户详细统计（GET）
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
| `focus.html` | 沉浸式专注（计时器 + 番茄钟模式 + 浮动宠物 + 白噪音） | Vue 3 CDN |
| `pet.html` | 宠物详情（互动 + 成长日志） | Vue 3 CDN |
| `profile.html` | 个人中心（用户信息 + 统计 + 设置） | HTML/CSS/JS |
| `calendar.html` | 今日学习总结（番茄数 + 时长 + 时段分布） | HTML/CSS/JS |
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
