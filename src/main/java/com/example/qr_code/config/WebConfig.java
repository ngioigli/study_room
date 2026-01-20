package com.example.qr_code.config; // 你的包名可能不一样，注意修改

import com.example.qr_code.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // --- 1. 配置图片文件的访问路径 (这是你之前写过的) ---
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 / 结尾
        String resourceLocation = "file:" + uploadDir;
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }
        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation);
    }

    // --- 2. 配置拦截器 (这是新增的) ---
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**") // 默认拦截所有路径
                .excludePathPatterns(   // 下面这些路径白名单，不拦截：
                        "/login.html",       // 登录页面本身必须放行，否则死循环
                        "/api/login",        // 登录接口必须放行
                        "/upload/qrcode",    // <--- 【关键修改】添加这一行！允许上传接口匿名访问，否则无法上传二维码图片
                        "/images/**",        // 图片资源放行 (可选，看你是否想保护图片)
                        "/css/**", "/js/**", // 静态资源放行
                        "/app/**",           // React 应用静态资源放行
                        "/audio/**",         // 音频资源放行
                        "/favicon.ico",
                        "/error"             // 报错页面放行
                );
    }
}