package com.ssm.common;

/** 业务异常：携带返回给前端的 code，由 ApiExceptionHandler 转成 Result */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
