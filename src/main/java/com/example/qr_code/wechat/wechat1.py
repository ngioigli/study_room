import uiautomation as auto
import time
import pyperclip
import sys
import win32gui
import win32con

# 设置全局超时
auto.SetGlobalSearchTimeout(2)

def log(msg):
    print(f"[流程] {msg}")

def force_restore_wechat():
    """ 强制将微信从最小化/后台状态拉起到前台 """
    log("🔄 正在唤醒微信窗口...")
    
    hwnd = win32gui.FindWindow("WeChatMainWndForPC", "微信")
    if not hwnd:
        hwnd = win32gui.FindWindow(None, "微信")
        
    if not hwnd:
        log("❌ 致命错误：未找到微信进程，请先手动登录微信！")
        return None

    if win32gui.IsIconic(hwnd):
        win32gui.ShowWindow(hwnd, win32con.SW_RESTORE)
        time.sleep(0.5)
    else:
        win32gui.ShowWindow(hwnd, win32con.SW_SHOW)

    win32gui.SetForegroundWindow(hwnd)
    
    win = auto.WindowControl(Handle=hwnd)
    win.SetFocus()
    return win, hwnd


def smart_navigate_to_service_account(wx_win, keyword):
    """
    智能导航：按下箭头并检查当前选中项，
    找到"服务号"分类后的第一个匹配项
    """
    log("🔍 智能导航模式：寻找服务号下的目标...")
    
    # 策略：按下箭头，每次检查剪贴板是否有变化
    # 当经过"服务号"后遇到包含关键词的选项时停止
    
    found_service_section = False
    max_attempts = 15  # 最多按15次，防止无限循环
    
    for i in range(max_attempts):
        # 按一次下箭头
        wx_win.SendKeys('{Down}')
        time.sleep(0.25)
        
        # 尝试获取当前高亮项的文本
        # 方法：复制当前选中项到剪贴板（有些程序支持 Ctrl+C 复制选中项名称）
        # 但微信搜索框不支持这个，所以我们用另一种策略：
        # 观察选中项的变化 - 通过按 Enter 后的窗口标题变化来判断
        
        # 由于无法直接读取下拉框，我们采用"标记法"：
        # 先按到底部（超过服务号），然后回到服务号后第一项
        
        log(f"   ⬇️ 按下 Down ({i+1})")
    
    # 既然无法检测，用另一种策略：
    # 直接搜索 "服务号 i湖工" 或调整搜索词使结果更精准
    return False


def main():
    keyword = "i湖工"
    target_menu_name = "一码通"
    
    log("🚀 启动智能导航版...")

    result = force_restore_wechat()
    if not result: return
    wx_win, hwnd = result
    
    wx_win.SwitchToThisWindow()
    time.sleep(0.5)

    # ==========================================
    # 方案：直接搜索更精确的关键词
    # ==========================================
    log("⌨️  激活搜索框...")
    
    wx_win.SendKeys('{Ctrl}f')
    time.sleep(0.3)
    
    wx_win.SendKeys('{Ctrl}a')
    wx_win.SendKeys('{Delete}')
    
    # 输入更精确的关键词，减少干扰结果
    # 尝试搜索公众号的全名或添加过滤词
    pyperclip.copy(keyword)
    wx_win.SendKeys('{Ctrl}v')
    log(f"📋 输入关键词: {keyword}")
    
    log("⏳ 等待搜索结果 (3秒)...")
    time.sleep(3.0)

    # ==========================================
    # 智能定位：按下箭头直到找到"服务号"分区
    # ==========================================
    log("⬇️ 开始智能导航...")
    
    # 策略：每按一次 Down 就按 Enter 测试，如果进入了小程序就 ESC 返回继续
    # 更好的策略：观察搜索结果的分区规律
    
    # 根据截图，搜索结果的规律是：
    # - "搜索网络结果" 区域（5-6项）
    # - "服务号" 区域（1项：i湖工）
    # - "最近使用过的小程序" 区域
    # - "聊天记录" 区域
    
    # 新策略：先按到最后，然后往上找
    # 或者：通过多次尝试找到正确的次数
    
    # 最可靠的方法：用坐标点击
    # 从截图看，"服务号"下的"i湖工"大约在搜索框下方 230-260 像素处
    
    log("📍 使用坐标定位法...")
    
    # 获取搜索框位置
    search_box = wx_win.EditControl(Name="搜索")
    if not search_box.Exists(1):
        # 尝试其他方式找搜索框
        search_box = wx_win.EditControl()
    
    if search_box.Exists(1):
        rect = search_box.BoundingRectangle
        log(f"   搜索框位置: ({rect.left}, {rect.top}, {rect.right}, {rect.bottom})")
        
        # 服务号下的 i湖工 大约在搜索框下方 220-250 像素
        # 根据你的截图，大约是第7个选项的位置
        target_y = rect.bottom + 220  # 搜索框底部 + 220像素
        target_x = rect.left + 100    # 搜索框左侧 + 100像素（居中）
        
        log(f"   📍 点击坐标: ({target_x}, {target_y})")
        
        # 使用 pyautogui 点击指定坐标
        try:
            import pyautogui
            pyautogui.click(target_x, target_y)
            log("✅ 已点击目标位置")
        except ImportError:
            # 如果没有 pyautogui，用 win32api
            import win32api
            win32api.SetCursorPos((target_x, target_y))
            time.sleep(0.1)
            win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN, 0, 0)
            win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP, 0, 0)
            log("✅ 已点击目标位置 (win32api)")
    else:
        log("⚠️ 找不到搜索框，使用固定次数导航...")
        # 回退到固定次数方案
        down_count = 6  # 你说是6次
        for i in range(down_count):
            wx_win.SendKeys('{Down}')
            time.sleep(0.2)
        wx_win.SendKeys('{Enter}')
    
    log("✅ 已选择公众号，等待页面加载...")
    time.sleep(1.5)

    # ==========================================
    # 菜单操作 - 点击"一码通"
    # ==========================================
    process_menu(wx_win, target_menu_name)


def process_menu(window, menu_name):
    log(f"👇 寻找菜单: {menu_name}")
    
    time.sleep(1.0)
    
    # 公众号对话窗口可能是新窗口，需要重新获取
    # 尝试找标题为 "i湖工" 的窗口
    gzh_win = auto.WindowControl(Name="i湖工")
    if not gzh_win.Exists(1):
        gzh_win = window  # 用原窗口
        log("   使用原窗口查找菜单")
    else:
        log("   找到公众号窗口")
        gzh_win.SetFocus()
    
    # 策略 1: 直接找按钮
    btn = gzh_win.ButtonControl(Name=menu_name)
    if btn.Exists(1):
        btn.Click(simulateMove=False)
        log("🎉 点击菜单成功")
        return True

    # 策略 2: 找文本控件
    txt = gzh_win.TextControl(Name=menu_name)
    if txt.Exists(0.5):
        txt.Click(simulateMove=False)
        log("🎉 点击菜单文字成功")
        return True
    
    # 策略 3: 找菜单项控件
    menu_item = gzh_win.MenuItemControl(Name=menu_name)
    if menu_item.Exists(0.5):
        menu_item.Click(simulateMove=False)
        log("🎉 点击菜单项成功")
        return True

    # 策略 4: 遍历所有控件查找包含菜单名的
    log("   🔍 遍历查找菜单按钮...")
    try:
        def find_by_name(ctrl, target_name, depth=0):
            if depth > 15:
                return None
            try:
                name = ctrl.Name
                ctrl_type = ctrl.ControlTypeName
                if name and target_name in name:
                    log(f"   ✅ 找到: {ctrl_type} - {name}")
                    return ctrl
                for child in ctrl.GetChildren():
                    result = find_by_name(child, target_name, depth + 1)
                    if result:
                        return result
            except:
                pass
            return None
        
        menu_btn = find_by_name(gzh_win, menu_name)
        if menu_btn:
            menu_btn.Click(simulateMove=False)
            log("🎉 点击菜单成功")
            return True
    except Exception as e:
        log(f"   ⚠️ 遍历出错: {e}")

    # 策略 5: 用坐标点击底部菜单区域
    log("   📍 尝试用坐标点击底部菜单...")
    try:
        rect = gzh_win.BoundingRectangle
        # "一码通" 在底部菜单栏的右侧
        # 从截图看，底部有三个菜单：校园应用、我刷码、一码通
        # 一码通大约在窗口底部，水平位置约 70% 处
        target_x = rect.left + int((rect.right - rect.left) * 0.72)
        target_y = rect.bottom - 30  # 距底部30像素
        
        log(f"   点击坐标: ({target_x}, {target_y})")
        
        # 使用 win32api 点击
        import win32api
        win32api.SetCursorPos((target_x, target_y))
        time.sleep(0.1)
        win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN, 0, 0)
        time.sleep(0.05)
        win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP, 0, 0)
        log("🎉 已点击菜单位置")
        return True
    except Exception as e:
        log(f"   ⚠️ 坐标点击失败: {e}")

    log("❌ 无法找到菜单按钮，请手动点击")
    return False


if __name__ == "__main__":
    main()
