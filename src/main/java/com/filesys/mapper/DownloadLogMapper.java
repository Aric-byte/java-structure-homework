package com.filesys.mapper;

import org.apache.ibatis.annotations.Param;

/** SQL 见 resources/filesys/mapper/DownloadLogMapper.xml */
public interface DownloadLogMapper {

    int insert(@Param("fileId") Integer fileId, @Param("userId") Integer userId);
}
