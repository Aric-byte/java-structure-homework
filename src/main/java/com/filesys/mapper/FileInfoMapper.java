package com.filesys.mapper;

import com.filesys.entity.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** SQL 见 resources/filesys/mapper/FileInfoMapper.xml */
public interface FileInfoMapper {

    int insert(FileInfo fileInfo);

    List<FileInfo> findByUploader(@Param("uploaderId") Integer uploaderId, @Param("category") String category);

    List<FileInfo> findDeletedByUploader(@Param("uploaderId") Integer uploaderId);

    FileInfo findById(@Param("id") Integer id);

    int updateStatus(@Param("id") Integer id, @Param("status") int status);

    int incrementDownloadCount(@Param("id") Integer id);

    int deleteById(@Param("id") Integer id);
}
