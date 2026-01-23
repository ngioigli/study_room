---
name: security
description: >
  安全审查与合规检查。当用户提到安全、XSS、注入、权限、越权、漏洞时激活此技能。
---

# 安全与合规 (Security)

安全漏洞审查、权限控制检查、内容安全治理。

## 安全底线

### 禁止事项

- ❌ 私聊与强社交聚合
- ❌ 密码明文存储
- ❌ SQL 拼接（防注入）
- ❌ 用户输入直接渲染（防 XSS）
- ❌ 越权访问数据

## XSS 防护

### 前端

```javascript
// 用户输入必须转义
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ❌ 错误
element.innerHTML = userInput;

// ✅ 正确
element.textContent = userInput;
```

### 后端

```java
// 使用参数化查询
// ❌ 错误
String sql = "SELECT * FROM users WHERE name = '" + name + "'";

// ✅ 正确
@Select("SELECT * FROM users WHERE name = #{name}")
```

## 权限控制

### 登录检查

```java
User user = (User) session.getAttribute("user");
if (user == null) {
    result.put("success", false);
    result.put("message", "请先登录");
    return result;
}
```

### 管理员检查

```java
if (user == null || !"admin".equals(user.getRole())) {
    result.put("success", false);
    result.put("message", "无权限访问");
    return result;
}
```

## 内容治理

留言/互动功能必须包含：
- ✅ 文明提示语
- ✅ 内容过滤（敏感词）
- ✅ 隐藏/封禁机制
- ✅ 定期清理策略

## 日志规范

**应该记录**：登录/登出、管理员操作、异常信息

**禁止记录**：用户密码、敏感 Token、个人隐私
