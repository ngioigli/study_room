package com.example.qr_code.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qr-status")
public class QrStatusController {

    private static volatile long cooldownEndTime = 0L;
    private static final long COOLDOWN_DURATION = 180 * 1000; // 3分钟

    @GetMapping("/check")
    public Map<String, Object> checkStatus() {
        long now = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        if (now < cooldownEndTime) {
            result.put("status", "LOCKED");
            // 【关键修改】直接返回结束的时间戳，让前端自己去算
            result.put("endTime", cooldownEndTime); 
        } else {
            result.put("status", "FREE");
            result.put("endTime", 0);
        }
        return result;
    }

    @PostMapping("/lock")
    public Map<String, Object> lockQrCode() {
        long now = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        if (now < cooldownEndTime) {
            result.put("success", false);
            result.put("message", "哎呀，慢了一步！已经被别人锁定了。");
            return result;
        }

        cooldownEndTime = now + COOLDOWN_DURATION;
        
        result.put("success", true);
        result.put("endTime", cooldownEndTime); // 锁定成功也返回结束时间
        return result;
    }
}