package com.springmvc.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 自定义拦截器② —— 登录检查（演示 preHandle 返回 false 中断请求）
 *
 * 【业务规则】/student/** 下的接口需要 session 中有 loginUser 才能访问
 *
 * 【演示流程】
 *   ① 直接访问 /annotation/student/hello → 未登录 → 被拦截，重定向到登录演示页
 *   ② 先访问   /annotation/page/session  → PageController 把 loginUser 写入 session
 *   ③ 再访问   /annotation/student/hello  → 放行，控制台看到正常执行
 *
 * 【知识点】preHandle 返回 false 后：
 *   - 后续拦截器与 Handler 不再执行
 *   - 但已执行过 preHandle 的拦截器，其 afterCompletion 仍会执行
 *   - 需要自己对 response 做出响应（这里用 sendRedirect 重定向）
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object loginUser = session.getAttribute("loginUser");

        if (loginUser != null) {
            System.out.println("[LoginInterceptor] 已登录(user=" + loginUser + ")，放行");
            return true;
        }

        System.out.println("[LoginInterceptor] 未登录，拦截请求: "
                + request.getRequestURI() + " → 重定向到登录演示页");
        // contextPath = 部署上下文（如 /spring_springmvc_mybatis）
        response.sendRedirect(request.getContextPath() + "/annotation/page/session");
        return false; // 中断：Handler 不会执行
    }
}