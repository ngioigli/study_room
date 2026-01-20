package com.example.qr_code.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器：用来检查用户是否已登录
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 Session
        HttpSession session = request.getSession();

        // 2. 检查 Session 中是否有用户信息
        Object user = session.getAttribute("user");

        // 3. 判断
        if (user != null) {
            // 已登录，放行
            return true;
        } else {
            // 未登录，重定向到登录页面
            response.sendRedirect("/login.html");
            // 拦截请求，不再向下执行
            return false;
        }
    }
}