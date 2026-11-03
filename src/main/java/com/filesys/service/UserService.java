package com.filesys.service;

import com.filesys.entity.SysUser;
import com.filesys.mapper.SysUserMapper;
import com.filesys.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;

    public UserService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 登录校验：按用户名取出盐与哈希，用相同算法计算后比较。
     * 用户不存在和密码错误返回同一个结果，不向前端区分，避免被用来探测账号。
     */
    public SysUser login(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }
        SysUser user = sysUserMapper.findByUsername(username.trim());
        if (user == null || !PasswordUtil.matches(rawPassword, user.getSalt(), user.getPassword())) {
            return null;
        }
        return user;
    }
}
