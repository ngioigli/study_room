import requests
import json
import qrcode
from PIL import Image
import sys

# ================= 配置区域 (请从 Fiddler 复制填入) =================

# 1. 填入 Fiddler 抓到的 Authorization (长字符串)
# 类似于: "C2CDB09706EDA2A034EFB89EF73A095B..."
MY_AUTH_TOKEN = "C2CDB09706EDA2A034EFB89EF73A095B.6E1521B2892B544507C4080F2BFB7FA462F9FE8FF38A9F48BB1103DF07C490667C2722BBBB23D9042F05B1B9D0F5F00EA8D50ECEB548A11790DBE077DC945F70C1907619258C752F0E83BDF3543F64EC4EB6D055642D79FFFB0119EBAB7F42E0EF7E6551B1876663DB74582C6FB9AAA7.ulXF2-7rvoUozbHsKdPIu9yXWQfwrijdQcaFv-gmOo0"


# 2. 填入 Fiddler 抓到的 Cookie
# 类似于: "JSESSIONID=D4E71E69703F1FF34329D6EA7E5D6FD6"
MY_COOKIE = "JSESSIONID=FED7E4B241A1CFBC8AFFCE9235EEF21C"


# ===================================================================

HOST = "code.hbut.edu.cn"

def log(msg):
    print(f"[日志] {msg}")
    sys.stdout.flush()

def fetch_qrcode_with_token():
    print("\n" + "="*50)
    print("🚀 启动：直接使用 Token 请求二维码")
    print("👉 跳过 TID 交换步骤，直接伪装成已登录用户")
    print("="*50 + "\n")

    # 检查配置是否填写
    if "在这里粘贴" in MY_AUTH_TOKEN or "在这里粘贴" in MY_COOKIE:
        log("❌ 错误：请先在代码顶部填入 Fiddler 抓到的 Token 和 Cookie！")
        return

    session = requests.Session()
    
    # 构造完全伪装的请求头
    headers = {
        "Host": HOST,
        "Connection": "keep-alive",
        # 【核心 1】直接带上有效的 Token
        "Authorization": MY_AUTH_TOKEN,
        "Cookie": MY_COOKIE,
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF WindowsWechat(0x6309080f) XWEB/8351",
        "Content-Type": "application/json",
        "Accept": "*/*",
        "Origin": f"https://{HOST}",
        "Referer": f"https://{HOST}/", # Referer 可以简化，甚至不带 tid
        "Accept-Encoding": "gzip, deflate, br",
        "Accept-Language": "zh-CN,zh;q=0.9"
    }
    
    # Body 数据 (根据之前的 Fiddler 截图，这里是空的 qrcodeType)
    payload = {"qrcodeType": ""}

    try:
        log("正在发送请求...")
        api_url = f"https://{HOST}/server/virtualCard/qrcodeOnline"
        
        # 直接发起 POST 请求
        response = session.post(api_url, headers=headers, json=payload)
        
        log(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            try:
                res_json = response.json()
            except:
                log(f"❌ 解析 JSON 失败，返回内容: {response.text[:100]}")
                return

            if res_json.get("success"):
                result = res_json.get("resultData", {})
                
                qr_str = result.get("qrcode")
                name = result.get("userName")
                balance = result.get("balance")
                
                print("\n" + "🔥"*50)
                print(f"🎉【获取成功】")
                print(f"👤 姓名: {name}")
                print(f"💰 余额: {balance}")
                print(f"🔗 二维码数据: {qr_str[:50]}...") # 只打印前50字符
                print("🔥"*50 + "\n")
                
                # 生成并显示二维码
                show_qrcode_image(qr_str)
            else:
                log(f"❌ 接口返回失败: {res_json.get('message')}")
                log(f"完整返回: {res_json}")
                if "无效" in str(res_json) or "登录" in str(res_json):
                    print("\n💡 提示：你的 Token 或 Cookie 可能已过期。")
                    print("请在电脑微信上刷新一下校园码页面，然后从 Fiddler 复制最新的值填入代码。")
        else:
            log(f"❌ HTTP 请求失败: {response.text}")

    except Exception as e:
        log(f"❌ 发生异常: {e}")

def show_qrcode_image(content):
    if not content: return
    try:
        qr = qrcode.QRCode(version=1, box_size=10, border=4)
        qr.add_data(content)
        qr.make(fit=True)
        img = qr.make_image(fill_color="black", back_color="white")
        img.show()
        print("✅ 二维码图片已显示")
    except Exception as e:
        log(f"⚠️ 生成图片失败: {e}")

if __name__ == "__main__":
    fetch_qrcode_with_token()  # 这个是根据token拿二维码的