package com.springmvc.exception;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 全局异常处理 —— springmvc 模块
 *
 * ============================================================
 * 【知识点】
 * ============================================================
 * 1. @ControllerAdvice —— 全局增强：声明后对【所有 Controller】生效
 *    （必须被组件扫描到，见 springmvc-servlet.xml 的 component-scan）
 *
 * 2. @ExceptionHandler(异常类) —— 按异常类型精确匹配处理：
 *    - 优先匹配最具体的异常（ArithmeticException 先于 Exception）
 *    - 一个 Controller 里也可写 @ExceptionHandler（只对本类生效，范围小于 @ControllerAdvice）
 *
 * 3. 好处：业务代码只管抛异常，不用每个方法 try-catch；
 *    统一返回友好错误页 / JSON，避免用户看到 500 白页和堆栈
 */
@Controller
@ControllerAdvice
public class GlobalExceptionHandler {

    /** 算术异常演示：/extra/arith/10/0（见 AdvanceController 统一演示类） */
    @ExceptionHandler(ArithmeticException.class)
    public ModelAndView arithmeticException(ArithmeticException ex) {
        return errorPage("算术异常", ex);
    }

    /** 空指针异常演示：/extra/null */
    @ExceptionHandler(NullPointerException.class)
    public ModelAndView nullPointerException(NullPointerException ex) {
        return errorPage("空指针异常", ex);
    }

    /** 上传文件超限（见 web.xml multipart-config 的 max-file-size=10MB）→ 友好提示而不是报错页 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView maxUploadException(MaxUploadSizeExceededException ex) {
        ModelAndView mv = new ModelAndView("error/error");
        mv.addObject("exClass", "上传文件过大");
        mv.addObject("exMessage", "单个文件不能超过 10MB（web.xml multipart-config）");
        return mv;
    }

    /** 兜底：其余所有异常 */
    @ExceptionHandler(Exception.class)
    public ModelAndView allException(Exception ex) {
        return errorPage("其他异常", ex);
    }

    /** 统一的错误页视图 */
    private ModelAndView errorPage(String title, Exception ex) {
        ModelAndView mv = new ModelAndView("error/error");
        mv.addObject("exClass", title);
        mv.addObject("exMessage", ex.getMessage());
        return mv;
    }
}
