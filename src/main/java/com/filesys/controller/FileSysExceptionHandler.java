package com.filesys.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.List;

/**
 * 本模块的全局异常处理：把异常变成列表页上的友好提示，不显示堆栈。
 * 它只注册在 filesys 这个 DispatcherServlet 的子容器里，不会影响其他模块。
 *
 * 上传大小有两道关：
 *   ① FileService 按 upload.max-size（10MB）逐个文件校验 —— 主要校验，提示具体文件名
 *   ② web.xml 的 multipart-config 是容器硬上限（单文件 20MB / 单次 60MB），超出时在解析 multipart 阶段
 *      就抛 MaxUploadSizeExceededException，此时还没确定由哪个 Controller 处理，
 *      所以这里【不能】加 basePackages 限定，否则匹配不上，用户会看到容器的 413 错误页
 */
@ControllerAdvice
public class FileSysExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FileSysExceptionHandler.class);

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public String uploadTooLarge(HttpServletRequest request) {
        return backToList(request, "上传内容过大：单个文件不能超过 10MB，且一次提交不能超过 60MB");
    }

    /** 其他异常显示独立错误页（不重定向回列表，避免列表页本身出错时循环重定向） */
    @ExceptionHandler(Exception.class)
    public String other(Exception e, HttpServletRequest request) {
        log.error("资料上传下载系统异常", e);
        request.setAttribute("message", "系统繁忙，请稍后再试");
        return "error";
    }

    private String backToList(HttpServletRequest request, String message) {
        RequestContextUtils.getOutputFlashMap(request).put("errors", List.of(message));
        return "redirect:/filesys/files";
    }
}
