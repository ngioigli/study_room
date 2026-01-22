# 🎨 Skill: 前端开发 (Frontend Development)

> **技能 ID**: `frontend-dev`
> **触发关键词**: 页面、HTML、CSS、JS、样式、交互、前端、按钮、表单、列表

## 1. 技能职责

- 编写/修改 HTML 页面结构
- 编写/修改 CSS 样式
- 编写/修改 JavaScript 交互逻辑
- 调用后端 API 并处理数据

## 2. 技术栈约束

| 项目 | 说明 |
|------|------|
| **HTML** | 语义化标签，移动端优先 |
| **CSS** | 使用 `variables.css` 变量，遵循 `components.css` 组件规范 |
| **JavaScript** | 原生 ES6+，部分页面使用 Vue 3 CDN |
| **API 调用** | 使用 `fetch`，带 `credentials: 'include'` |
| **文件位置** | `src/main/resources/static/` |

## 3. 开发规范

### HTML 结构

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面标题 - 云端自习室</title>
    <link rel="stylesheet" href="/css/variables.css">
    <link rel="stylesheet" href="/css/components.css">
</head>
<body>
    <!-- 页面内容 -->
    <script src="/js/nav.js"></script>
</body>
</html>
```

### JavaScript API 调用模板

```javascript
async function fetchData() {
    try {
        const res = await fetch('/api/xxx', {
            method: 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });
        
        const data = await res.json();
        console.log('[页面名] API响应:', data);
        
        if (data.success) {
            // 成功处理
        } else {
            showToast(data.message || '操作失败');
        }
    } catch (err) {
        console.error('[页面名] 请求异常:', err);
        showToast('网络错误，请重试');
    }
}
```

## 4. 状态处理

| 状态 | 说明 | 处理方式 |
|------|------|---------|
| **加载中** | 数据请求中 | 显示 loading 动画 |
| **空数据** | 无数据返回 | 显示空状态提示 |
| **错误** | 请求失败 | 显示错误提示 + 重试按钮 |
| **成功** | 操作成功 | Toast 提示 |

## 5. 移动端适配

- **视口**：`<meta name="viewport" content="width=device-width, initial-scale=1.0">`
- **触控区**：按钮/可点击区域 ≥ 44px
- **底部安全区**：考虑 iPhone 底部横条
- **字体**：不小于 14px

## 6. 开发检查清单

- [ ] HTML 结构语义化
- [ ] CSS 使用变量系统
- [ ] JS 有完整错误处理
- [ ] API 请求带 credentials
- [ ] 处理加载/空/错误状态
- [ ] 移动端触控区 ≥ 44px
- [ ] 控制台无报错
