package com.filesys.service;

import com.filesys.entity.FileInfo;
import com.filesys.mapper.FileInfoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件业务：所有安全校验都在这里（服务端）完成，前端校验只是体验优化。
 *
 * 上传三要点：大小校验、类型（扩展名白名单）校验、安全重命名（UUID）。
 * 下载三要点：按数据库 ID 取路径（不拼接用户输入）、归属校验、路径必须落在上传根目录内。
 */
@Service
public class FileService {

    private final FileInfoMapper fileInfoMapper;
    private final Path uploadRoot;
    private final long maxSize;
    private final Set<String> allowedExt;

    public FileService(FileInfoMapper fileInfoMapper,
                       @Value("${upload.root}") String uploadRoot,
                       @Value("${upload.max-size}") long maxSize,
                       @Value("${upload.allowed-ext}") String allowedExt) {
        this.fileInfoMapper = fileInfoMapper;
        this.uploadRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
        this.maxSize = maxSize;
        this.allowedExt = Arrays.stream(allowedExt.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public List<FileInfo> listMine(Integer userId, String category) {
        return fileInfoMapper.findByUploader(userId, category);
    }

    public Set<String> getAllowedExt() {
        return allowedExt;
    }

    public long getMaxSize() {
        return maxSize;
    }

    /**
     * 保存一个上传文件：校验 → 落盘 → 元信息入库。
     * 入库失败时删除已落盘的文件，避免磁盘上留下没有记录的"孤儿文件"。
     */
    @Transactional
    public FileInfo upload(MultipartFile file, Integer userId, String username) {
        if (file == null || file.isEmpty()) {
            throw new FileAccessException("请选择要上传的文件（不能为空文件）");
        }
        String originName = baseName(file.getOriginalFilename());
        String ext = extension(originName);
        if (!allowedExt.contains(ext)) {
            throw new FileAccessException("「" + originName + "」类型不允许，仅支持：" + String.join("、", sortedExt()));
        }
        if (file.getSize() > maxSize) {
            throw new FileAccessException("「" + originName + "」超过大小上限 " + (maxSize / 1024 / 1024) + "MB");
        }

        // 按 用户名/年/月 分目录，避免单目录文件堆积；落盘名由服务端生成，与用户输入无关
        LocalDate today = LocalDate.now();
        String relativeDir = username + "/" + today.getYear() + "/" + String.format("%02d", today.getMonthValue());
        String storeName = UUID.randomUUID() + "." + ext;
        Path target = resolveInsideRoot(relativeDir + "/" + storeName);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new FileAccessException("「" + originName + "」保存失败，请稍后重试");
        }

        FileInfo info = new FileInfo();
        info.setOriginName(originName);
        info.setStoreName(storeName);
        info.setFilePath(relativeDir + "/" + storeName);
        info.setExt(ext);
        info.setCategory(categoryOf(ext));
        info.setFileSize(file.getSize());
        info.setContentType(file.getContentType());
        info.setUploaderId(userId);
        try {
            fileInfoMapper.insert(info);
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }
        return info;
    }

    /**
     * 下载前校验，返回可读的文件位置。下载日志由 DownloadLogAspect 在本方法正常返回后写入。
     */
    @Transactional
    public DownloadTarget prepareDownload(Integer fileId, Integer userId) {
        FileInfo info = requireOwned(fileId, userId);
        if (info.isFrozen()) {
            throw new FileAccessException("「" + info.getOriginName() + "」已被冻结，解冻后才能下载");
        }
        Path path = resolveInsideRoot(info.getFilePath());
        if (!Files.isRegularFile(path)) {
            throw new FileAccessException("「" + info.getOriginName() + "」在服务器上已不存在，可能已被清理");
        }
        fileInfoMapper.incrementDownloadCount(fileId);
        return new DownloadTarget(info, path);
    }

    /** 上传者冻结 / 解冻：冻结后禁止下载 */
    @Transactional
    public FileInfo setFrozen(Integer fileId, Integer userId, boolean frozen) {
        FileInfo info = requireOwned(fileId, userId);
        fileInfoMapper.updateStatus(fileId, frozen ? FileInfo.STATUS_FROZEN : FileInfo.STATUS_NORMAL);
        return info;
    }

    /** 逻辑删除：status 置 0，列表不再显示，磁盘文件保留 */
    @Transactional
    public FileInfo delete(Integer fileId, Integer userId) {
        FileInfo info = requireOwned(fileId, userId);
        fileInfoMapper.updateStatus(fileId, FileInfo.STATUS_DELETED);
        return info;
    }

    /** 归属校验：不存在、已删除、不属于当前用户都拒绝（他人文件统一提示"无权访问"） */
    private FileInfo requireOwned(Integer fileId, Integer userId) {
        FileInfo info = fileId == null ? null : fileInfoMapper.findById(fileId);
        if (info == null || info.getStatus() == FileInfo.STATUS_DELETED) {
            throw new FileAccessException("文件不存在或已被删除");
        }
        if (!info.getUploaderId().equals(userId)) {
            throw new FileAccessException("无权访问：该文件不属于当前账号");
        }
        return info;
    }

    /** 拼出的路径必须仍在上传根目录内，防止 ../ 之类的路径穿越 */
    private Path resolveInsideRoot(String relativePath) {
        Path path = uploadRoot.resolve(relativePath).normalize();
        if (!path.startsWith(uploadRoot)) {
            throw new FileAccessException("非法的文件路径");
        }
        return path;
    }

    /** 浏览器可能带上客户端完整路径（如 C:\fakepath\a.pdf），只保留文件名部分 */
    private static String baseName(String name) {
        if (name == null || name.isBlank()) {
            return "未命名";
        }
        String n = name.replace('\\', '/');
        n = n.substring(n.lastIndexOf('/') + 1).trim();
        return n.length() > 200 ? n.substring(n.length() - 200) : n;
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 文件分类：按扩展名自动判定，列表页可按分类筛选 */
    public static String categoryOf(String ext) {
        switch (ext) {
            case "jpg":
            case "png":
                return "图片";
            case "zip":
                return "压缩包";
            default:
                return "文档";
        }
    }

    private List<String> sortedExt() {
        return allowedExt.stream().sorted().map(e -> "." + e).collect(Collectors.toList());
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 清理失败不影响主流程，异常已由调用方抛出
        }
    }

    /** prepareDownload 的返回值：元信息 + 磁盘上的绝对路径 */
    public static class DownloadTarget {
        private final FileInfo info;
        private final Path path;

        public DownloadTarget(FileInfo info, Path path) {
            this.info = info;
            this.path = path;
        }

        public FileInfo getInfo() {
            return info;
        }

        public Path getPath() {
            return path;
        }
    }
}
