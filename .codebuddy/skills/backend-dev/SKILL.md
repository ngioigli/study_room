---
name: backend-dev
description: >
  Java/Spring Boot后端开发。当用户提到接口、API、Controller、Service、
  后端、Java、Mapper、Entity时激活此技能。
---

# 后端开发 (Backend Development)

编写Controller接口、Service业务逻辑、Mapper数据访问。

## 技术栈

| 项目 | 版本/说明 |
|------|----------|
| Spring Boot | 2.6.13 |
| MyBatis-Plus | 3.5.3.1 |
| Java | 编译目标 1.8 |
| 数据库 | MySQL 8.x |
| 包路径 | `com.example.qr_code` |

## 代码结构

```
src/main/java/com/example/qr_code/
├── controller/     # 控制器层
├── service/        # 服务层
├── mapper/         # 数据访问层
├── entity/         # 实体类
├── config/         # 配置类
└── interceptor/    # 拦截器
```

## Controller 模板

```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {
    @Autowired
    private XxxService xxxService;

    @GetMapping
    public Map<String, Object> getData(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
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

## API 返回格式

```json
{
    "success": true,
    "message": "操作成功",
    "data": { ... }
}
```

## 开发检查清单

- Controller 路径符合 RESTful
- 登录检查（需要的接口）
- 参数校验完整
- Service 层有事务注解（写操作）
- 返回格式与前端对齐
