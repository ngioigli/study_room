---
name: frontend-dev
description: >
  HTML/CSS/JavaScript前端页面开发。当用户提到页面、HTML、CSS、JS、样式、
  交互、前端、按钮、表单、列表时激活此技能。
---

# 前端开发 (Frontend Development)

编写HTML页面、CSS样式、JavaScript交互逻辑。

## 技术栈

| 项目 | 说明 |
|------|------|
| HTML | 语义化标签，移动端优先 |
| CSS | 使用 `variables.css` 变量 |
| JavaScript | 原生 ES6+，部分页面使用 Vue 3 CDN |
| 文件位置 | `src/main/resources/static/` |

## HTML 模板

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

## API 调用模板

```javascript
async function fetchData() {
    try {
        const res = await fetch('/api/xxx', {
            method: 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });
        const data = await res.json();
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

## 状态处理

| 状态 | 处理方式 |
|------|---------|
| 加载中 | 显示 loading 动画 |
| 空数据 | 显示空状态提示 |
| 错误 | 显示错误提示 + 重试按钮 |
| 成功 | Toast 提示 |

## 移动端适配

- 触控区 ≥ 44px
- 字体 ≥ 14px
- 底部安全区：`padding-bottom: env(safe-area-inset-bottom)`
