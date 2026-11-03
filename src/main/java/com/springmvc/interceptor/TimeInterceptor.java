package com.springmvc.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义拦截器① —— 耗时统计（演示放行全流程）
 *
 * ============================================================
 * 【拦截器核心知识点】
 * ============================================================
 * 1. 实现 HandlerInterceptor 接口（Spring 5 起三个方法都有默认实现，按需覆盖）
 *
 * 2. 三个方法的执行时机（一次请求）：
 *
 *    preHandle          → Handler(Controller) 执行【前】
 *      返回 true  → 放行，继续下一个拦截器/Handler
 *      返回 false → 中断，后续拦截器和 Handler 都不执行
 *    postHandle         → Handler 执行【后】、视图渲染【前】（可修改 ModelAndView）
 *    afterCompletion    → 视图渲染【完成后】（无论是否异常都执行，适合清理资源）
 *
 *    完整顺序：pre1 → pre2 → Handler → post2 → post1 → 渲染 → after2 → after1
 *    【注意】postHandle/afterCompletion 的顺序与 preHandle 相反（洋葱模型）
 *
 * 3. 拦截器 vs 过滤器（Servlet Filter）：
 *    - 拦截器是 SpringMVC 机制，可以拿到 Handler 信息、注入 Spring Bean
 *    - 过滤器是 Servlet 规范，在 DispatcherServlet 之前执行，作用范围更大
 *
 * 4. 注册方式见 springmvc-servlet.xml 的 <mvc:interceptors>（按声明顺序执行）
 */
public class TimeInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "TimeInterceptor.startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        long start = System.currentTimeMillis();
        request.setAttribute(START_TIME, start);
        System.out.println("[TimeInterceptor] 1.preHandle  → Handler 执行前: "
                + request.getRequestURI());
        // 返回 false 的效果见 LoginInterceptor；这里返回 true 放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        System.out.println("[TimeInterceptor] 2.postHandle → Handler 后、渲染前，"
                + "逻辑视图名=" + (modelAndView != null ? modelAndView.getViewName() : "无(REST)"));
        // 此处可以修改/补充模型数据，页面渲染前最后一次机会
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long cost = System.currentTimeMillis() - (Long) request.getAttribute(START_TIME);
        System.out.println("[TimeInterceptor] 3.afterCompletion → 渲染完成，本次请求耗时 "
                + cost + " ms" + (ex != null ? "，发生异常: " + ex.getMessage() : "，无异常"));
        // ex 非空时可记录日志；本方法无论如何都会执行（类似 finally）
    }
}