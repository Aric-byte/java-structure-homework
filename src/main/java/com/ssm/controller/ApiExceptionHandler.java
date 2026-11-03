package com.ssm.controller;

import com.ssm.common.BusinessException;
import com.ssm.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletResponse;

/**
 * 只作用于 StudentApiController：把异常转成统一 Result，并同步设置 HTTP 状态码。
 * 不向前端暴露堆栈；JSP 版 StudentController 不受影响。
 */
@RestControllerAdvice(assignableTypes = StudentApiController.class)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> business(BusinessException e, HttpServletResponse response) {
        response.setStatus(e.getCode());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class})
    public Result<Void> badRequest(Exception e, HttpServletResponse response) {
        response.setStatus(400);
        return Result.fail(400, "请求参数格式不正确");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> other(Exception e, HttpServletResponse response) {
        log.error("接口异常", e);
        response.setStatus(500);
        return Result.fail(500, "系统繁忙，请稍后再试");
    }
}
