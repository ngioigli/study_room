# 🔒 Skill: 安全与合规 (Security)

> **技能 ID**: `security`
> **触发关键词**: 安全、XSS、注入、权限、越权

## 1. 技能职责

- 安全漏洞审查
- 权限控制检查
- 内容安全治理
- 安全加固建议

## 2. 安全底线

### 禁止事项

- ❌ 私聊与强社交聚合
- ❌ 密码明文存储
- ❌ SQL 拼接（防注入）
- ❌ 用户输入直接渲染（防 XSS）
- ❌ 越权访问数据

## 3. XSS 防护

### 前端防护

```javascript
// 用户输入必须转义后再渲染
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 错误示例
element.innerHTML = userInput;  // ❌

// 正确示例
element.innerHTML = escapeHtml(userInput);  // ✅
element.textContent = userInput;  // ✅
```

### 后端防护

```java
// 使用参数化查询，不要拼接 SQL
// 错误示例
String sql = "SELECT * FROM users WHERE name = '" + name + "'";  // ❌

// 正确示例
@Select("SELECT * FROM users WHERE name = #{name}")  // ✅
```

## 4. 权限控制

### 登录检查

```java
@GetMapping("/api/xxx")
public Map<String, Object> getData(HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        result.put("success", false);
        result.put("message", "请先登录");
        return result;
    }
    // ...
}
```

### 管理员检查

```java
@GetMapping("/api/admin/xxx")
public Map<String, Object> adminData(HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) {
        result.put("success", false);
        result.put("message", "无权限访问");
        return result;
    }
    // ...
}
```

## 5. 内容治理

### 留言/互动功能必须

- ✅ 文明提示语
- ✅ 内容过滤（敏感词）
- ✅ 隐藏/封禁机制
- ✅ 定期清理策略

### 示例

```javascript
// 留言前提示
const tips = [
    '💡 请文明发言，友善交流',
    '🌸 传递温暖，共建美好社区'
];
```

## 6. 日志规范

### 应该记录

- ✅ 用户登录/登出
- ✅ 关键操作（如管理员操作）
- ✅ 异常信息

### 禁止记录

- ❌ 用户密码
- ❌ 敏感 Token
- ❌ 个人隐私信息

## 7. 安全检查清单

- [ ] 用户输入已转义（防 XSS）
- [ ] SQL 使用参数化查询
- [ ] 敏感接口有登录检查
- [ ] 管理接口有权限检查
- [ ] 日志不含敏感信息
- [ ] 留言功能有内容治理
