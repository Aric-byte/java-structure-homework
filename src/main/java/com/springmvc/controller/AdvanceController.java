package com.springmvc.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SpringMVC 补充知识点统一演示类 —— 一个控制类覆盖 4 大知识点
 *
 * ================================================================
 * | 知识点          | 演示接口                                    | 配置/机制位置            |
 * |-----------------|---------------------------------------------|--------------------------|
 * | 一 POST 中文乱码 | POST /extra/encoding?name=张三              | web.xml 编码过滤器        |
 * | 二 全局异常处理  | GET  /extra/arith/10/0    /extra/null       | GlobalExceptionHandler  |
 * | 三 静态资源访问  | GET  /extra/static-info（另直接开 /static/…）| mvc:resources 配置        |
 * | 四 日期参数绑定  | GET  /extra/date?birthday=2000-08-15        | @DateTimeFormat 注解      |
 * ================================================================
 */
@RestController
@RequestMapping("/extra")
public class AdvanceController {

    /* ==================== 一、POST 中文乱码 ==================== */

    /**
     * 【原理】
     * - GET 参数乱码：由 Tomcat 的 URIEncoding 解决
     * - POST 请求体乱码：必须靠 web.xml 的 CharacterEncodingFilter 统一 UTF-8
     *   （forceEncoding=true 连响应也一并强制），缺了它中文变成 ???
     *
     * 【实验】
     * 正常访问 → 中文正常；把 web.xml 里 encodingFilter 注释掉重新部署 → 再 POST 中文
     * 观察乱码，体会过滤器的作用
     */
    @PostMapping("/encoding")
    public String encoding(@RequestParam String name) {
        return "一) POST 中文参数: [" + name + "] —— 显示正常说明 CharacterEncodingFilter 生效";
    }

    /* ==================== 二、全局异常处理 ==================== */

    /**
     * 【原理】Controller 抛出的异常由 GlobalExceptionHandler（@ControllerAdvice）
     * 按异常类型精确捕获 → 统一跳转友好错误页 error/error.jsp，而不是 500 白页。
     * 越具体的异常优先匹配：ArithmeticException 先于兜底的 Exception。
     *
     * 【演示】b = 0 时抛出除零异常 → 看到错误页
     */
    @GetMapping("/arith/{a}/{b}")
    public String arith(@PathVariable Integer a, @PathVariable Integer b) {
        return "二) " + a + " / " + b + " = " + (a / b);
    }

    /** 空指针异常 → 被 @ExceptionHandler(NullPointerException.class) 精确捕获 */
    @GetMapping("/null")
    public String nullPointer() {
        String s = null;
        return "二) " + s.length();
    }

    /* ==================== 三、静态资源访问 ==================== */

    /**
     * 【原理】生产项目 DispatcherServlet 通常映射 "/"，会拦截所有请求
     * （包括 css/js/图片）→ 必须配：
     *   <mvc:resources mapping="/static/**" location="/static/"/>
     * 告诉 SpringMVC 这些路径直接放行，不进 Controller。
     *
     * 【演示】本接口只返回说明；真正的效果是浏览器直接访问
     *   /static/css/demo.css 能看到文件内容（不被 404）
     */
    @GetMapping("/static-info")
    public String staticInfo() {
        return "三) 静态资源: <mvc:resources mapping='/static/**' location='/static/'/>"
                + " —— 直接访问 /static/css/demo.css 即可";
    }

    /* ==================== 四、日期参数绑定 ==================== */

    /**
     * 【原理】@DateTimeFormat(pattern) 让 SpringMVC 把 "2000-08-15" 自动转成 Date；
     * 缺了该注解会抛出类型转换异常（String 不能直接绑到 Date 上）。
     * 同族注解：@NumberFormat（数字格式化），都由 mvc:annotation-driven 注册的转换服务支持
     */
    @GetMapping("/date")
    public String date(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        return "四) 日期绑定: birthday="
                + new SimpleDateFormat("yyyy年MM月dd日").format(birthday);
    }
}
