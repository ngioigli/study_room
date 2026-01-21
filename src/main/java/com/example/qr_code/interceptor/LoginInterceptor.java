package com.example.qr_code.interceptor;

import com.example.qr_code.entity.User;
import com.example.qr_code.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器：用来检查用户是否已登录且未被禁用
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 Session
        HttpSession session = request.getSession();

        // 2. 检查 Session 中是否有用户信息
        Object userObj = session.getAttribute("user");

        // 3. 判断
        if (userObj != null) {
            // 检查用户是否被禁用 - 从数据库实时查询
            if (userObj instanceof User) {
                User sessionUser = (User) userObj;
                
                // 从数据库重新查询用户状态
                User dbUser = userMapper.selectById(sessionUser.getId());
                if (dbUser == null) {
                    // 用户不存在，清除Session
                    session.invalidate();
                    response.sendRedirect("/login.html?error=notfound");
                    return false;
                }
                
                if (dbUser.getStatus() != null && dbUser.getStatus() == 0) {
                    // 用户已被禁用，清除Session并重定向
                    session.invalidate();
                    response.sendRedirect("/login.html?error=disabled");
                    return false;
                }
                
                // 更新Session中的用户信息
                session.setAttribute("user", dbUser);
            }
            // 已登录且未被禁用，放行
            return true;
        } else {
            // 未登录，重定向到登录页面
            response.sendRedirect("/login.html");
            // 拦截请求，不再向下执行
            return false;
        }
    }
}