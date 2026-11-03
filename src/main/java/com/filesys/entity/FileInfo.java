package com.filesys.entity;

import java.util.Date;

/**
 * 文件元信息（file_info）—— 磁盘存文件，数据库存元数据。
 *
 * 为什么原始文件名要存在数据库里：
 *   落盘文件名由服务端用 UUID 生成（防重名覆盖、防路径穿越、防特殊字符），
 *   磁盘上已经看不出用户上传时叫什么；列表展示和下载时的文件名只能从库里取回 originName。
 */
public class FileInfo {

    public static final int STATUS_DELETED = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_FROZEN = 2;

    private Integer id;
    private String originName;
    private String storeName;
    private String filePath;
    private String ext;
    private String category;
    private Long fileSize;
    private String contentType;
    private Integer uploaderId;
    private Date createTime;
    private Integer status;
    private Integer downloadCount;

    /** 列表页展示用：1536 → "1.5 KB" */
    public String getSizeText() {
        if (fileSize == null) {
            return "";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        }
        if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        }
        return String.format("%.1f MB", fileSize / 1024.0 / 1024.0);
    }

    public boolean isFrozen() {
        return status != null && status == STATUS_FROZEN;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Integer uploaderId) {
        this.uploaderId = uploaderId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
}
