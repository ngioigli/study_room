/**
 * 云端自习室 - 导航逻辑
 */

// 底部导航配置
const NAV_CONFIG = [
    { id: 'index', icon: '🏠', label: '自习室', href: '/index.html' },
    { id: 'study', icon: '📚', label: '学习小屋', href: '/study.html' },
    { id: 'member', icon: '👑', label: '会员', href: '/member.html' },
    { id: 'profile', icon: '👤', label: '我的', href: '/profile.html' }
];

/**
 * 创建底部导航栏
 * @param {string} activeId - 当前激活的导航项 ID
 */
function createBottomNav(activeId) {
    const nav = document.createElement('nav');
    nav.className = 'bottom-nav';
    
    NAV_CONFIG.forEach(item => {
        const link = document.createElement('a');
        link.href = item.href;
        link.className = 'nav-item' + (item.id === activeId ? ' active' : '');
        link.innerHTML = `
            <span class="nav-icon">${item.icon}</span>
            <span class="nav-label">${item.label}</span>
        `;
        nav.appendChild(link);
    });
    
    document.body.appendChild(nav);
}

/**
 * 初始化导航
 * 自动检测当前页面并高亮对应导航项
 */
function initNav() {
    const path = window.location.pathname;
    let activeId = 'index';
    
    if (path.includes('study')) {
        activeId = 'study';
    } else if (path.includes('member')) {
        activeId = 'member';
    } else if (path.includes('profile')) {
        activeId = 'profile';
    } else if (path.includes('index') || path === '/' || path === '/index.html') {
        activeId = 'index';
    }
    
    createBottomNav(activeId);
}

/**
 * Toast 提示
 * @param {string} message - 提示消息
 * @param {number} duration - 显示时长（毫秒）
 */
function showToast(message, duration = 3000) {
    // 移除已有的 toast
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }
    
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, duration);
}

/**
 * 显示加载状态
 * @param {string} text - 加载文字
 */
function showLoading(text = '加载中...') {
    const overlay = document.createElement('div');
    overlay.className = 'loading-overlay';
    overlay.id = 'loading-overlay';
    overlay.innerHTML = `
        <div class="loading-spinner"></div>
        <div class="loading-text">${text}</div>
    `;
    document.body.appendChild(overlay);
}

/**
 * 隐藏加载状态
 */
function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.remove();
    }
}

/**
 * 格式化时长（秒 -> 可读字符串）
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的字符串
 */
function formatDuration(seconds) {
    if (seconds < 60) return `${seconds}秒`;
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    if (hours > 0) {
        return `${hours}h${minutes}m`;
    }
    return `${minutes}分钟`;
}

/**
 * 格式化时间（秒 -> MM:SS）
 * @param {number} totalSeconds - 总秒数
 * @returns {string} 格式化后的时间
 */
function formatTime(totalSeconds) {
    const m = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
    const s = (totalSeconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
}

/**
 * 格式化时间（秒 -> HH:MM:SS）
 * @param {number} totalSeconds - 总秒数
 * @returns {string} 格式化后的时间
 */
function formatTimeHMS(totalSeconds) {
    const h = Math.floor(totalSeconds / 3600).toString().padStart(2, '0');
    const m = Math.floor((totalSeconds % 3600) / 60).toString().padStart(2, '0');
    const s = (totalSeconds % 60).toString().padStart(2, '0');
    return `${h}:${m}:${s}`;
}

/**
 * 计算经验值（与后端逻辑一致）
 * @param {number} durationSeconds - 专注时长（秒）
 * @returns {number} 经验值
 */
function calculateExp(durationSeconds) {
    const minutes = Math.floor(durationSeconds / 60);
    if (minutes >= 30) {
        return Math.floor(minutes * 1.5);
    }
    return minutes;
}

/**
 * API 请求封装
 * @param {string} url - 请求地址
 * @param {object} options - 请求选项
 * @returns {Promise} 请求结果
 */
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json'
        }
    };
    
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };
    
    try {
        const response = await fetch(url, mergedOptions);
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API 请求失败:', error);
        throw error;
    }
}

/**
 * 获取今日统计
 * @returns {Promise<object>} 今日统计数据
 */
async function getTodayStats() {
    return apiRequest('/api/focus/today');
}

/**
 * 保存专注记录
 * @param {number} duration - 专注时长（秒）
 * @returns {Promise<object>} 保存结果
 */
async function saveFocusRecord(duration) {
    return apiRequest('/api/focus/save', {
        method: 'POST',
        body: JSON.stringify({ duration })
    });
}

/**
 * 获取宠物信息
 * @returns {Promise<object>} 宠物信息
 */
async function getPetInfo() {
    return apiRequest('/api/pet');
}

/**
 * 与宠物互动
 * @returns {Promise<object>} 互动结果
 */
async function interactWithPet() {
    return apiRequest('/api/pet/interact', { method: 'POST' });
}

/**
 * 获取宠物消息
 * @returns {Promise<object>} 消息内容
 */
async function getPetMessage() {
    return apiRequest('/api/pet/message');
}

// 导出函数（如果使用模块化）
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        createBottomNav,
        initNav,
        showToast,
        showLoading,
        hideLoading,
        formatDuration,
        formatTime,
        formatTimeHMS,
        calculateExp,
        apiRequest,
        getTodayStats,
        saveFocusRecord,
        getPetInfo,
        interactWithPet,
        getPetMessage
    };
}
