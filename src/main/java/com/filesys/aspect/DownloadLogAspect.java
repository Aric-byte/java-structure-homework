package com.filesys.aspect;

import com.filesys.mapper.DownloadLogMapper;
import com.filesys.service.FileService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * F5 下载日志（AOP）：FileService.prepareDownload 正常返回（校验全部通过）后，
 * 记录 文件 ID、下载人、下载时间 到 download_log。
 * 被拒绝的下载（无权访问、已冻结、文件不存在）抛异常，@AfterReturning 不会执行，因此不记日志。
 */
@Aspect
@Component
public class DownloadLogAspect {

    private final DownloadLogMapper downloadLogMapper;

    public DownloadLogAspect(DownloadLogMapper downloadLogMapper) {
        this.downloadLogMapper = downloadLogMapper;
    }

    @AfterReturning(
            pointcut = "execution(* com.filesys.service.FileService.prepareDownload(..)) && args(fileId, userId)",
            returning = "target",
            argNames = "fileId,userId,target")
    public void logDownload(Integer fileId, Integer userId, FileService.DownloadTarget target) {
        downloadLogMapper.insert(fileId, userId);
    }
}
