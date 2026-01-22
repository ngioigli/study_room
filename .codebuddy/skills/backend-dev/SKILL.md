# ⚙️ Skill: 后端开发 (Backend Development)

> **技能 ID**: `backend-dev`
> **触发关键词**: 接口、API、Controller、Service、后端、Java、Mapper

## 1. 技能职责

- 编写/修改 Controller 接口
- 编写/修改 Service 业务逻辑
- 编写/修改 Mapper 数据访问
- 编写/修改 Entity 实体类

## 2. 技术栈约束

| 项目 | 版本/说明 |
|------|----------|
| **Spring Boot** | 2.6.13 |
| **MyBatis-Plus** | 3.5.3.1 |
| **Java** | 编译目标 1.8 |
| **数据库** | MySQL 8.x |
| **包路径** | `com.example.qr_code` |

## 3. 代码结构

```
src/main/java/com/example/qr_code/
├── controller/     # 控制器层
├── service/        # 服务层
├── mapper/         # 数据访问层
├── entity/         # 实体类
├── config/         # 配置类
├── interceptor/    # 拦截器
└── common/         # 公共类
```

## 4. 开发规范

### Controller 规范

```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    @Autowired
    private XxxService xxxService;

    @GetMapping
    public Map<String, Object> getData(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 检查登录
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        // 2. 调用 Service
        try {
            Object data = xxxService.getData(user.getId());
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }
}
```

### Service 规范

```java
@Service
public class XxxService {

    @Autowired
    private XxxMapper xxxMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveData(Long userId, String name) {
        // 带事务的写操作
    }
}
```

## 5. API 返回格式

```json
{
    "success": true,
    "message": "操作成功",
    "data": { ... }
}
```

## 6. 开发检查清单

- [ ] Controller 路径符合 RESTful
- [ ] 登录检查（需要的接口）
- [ ] 参数校验完整
- [ ] Service 层有事务注解（写操作）
- [ ] Mapper 方法命名规范
- [ ] Entity 字段与数据库一致
- [ ] 返回格式与前端对齐
- [ ] 异常处理完整
