package com.filesys.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 统一登录校验：session 中没有登录用户 → 重定向到登录页。
 * 注册方式与放行路径见 FileSysMvcConfig#addInterceptors（Java 配置，不使用 XML）。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_REAL_NAME = "realName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_USERNAME) != null) {
            return true;
        }
        // 上下文路径 + Servlet 路径（/filesys）+ /login，部署到任何上下文都能跳对
        response.sendRedirect(request.getContextPath() + request.getServletPath() + "/login");
        return false;
    }
}
