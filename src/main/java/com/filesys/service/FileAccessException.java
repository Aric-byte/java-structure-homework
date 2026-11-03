package com.filesys.service;

/** 上传 / 下载 / 冻结等操作被拒绝：message 直接作为友好提示显示给用户，不打印堆栈 */
public class FileAccessException extends RuntimeException {

    public FileAccessException(String message) {
        super(message);
    }
}
