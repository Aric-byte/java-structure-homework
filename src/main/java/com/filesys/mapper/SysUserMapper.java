package com.filesys.mapper;

import com.filesys.entity.SysUser;
import org.apache.ibatis.annotations.Param;

/** SQL 见 resources/filesys/mapper/SysUserMapper.xml */
public interface SysUserMapper {

    SysUser findByUsername(@Param("username") String username);
}
