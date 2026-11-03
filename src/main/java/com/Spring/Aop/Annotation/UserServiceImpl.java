package com.Spring.Aop.Annotation;

import org.springframework.stereotype.Component;

@Component("userService")  //注解的书写 目标对象 使用的是标准的@Component注解 定义一个组件 讲给Spring的容器进行管理
public class UserServiceImpl implements UserService {

    @Override
    public void saveUser(String name) {
        System.out.println("    [saveUser] 正在保存用户: " + name);
    }

    @Override
    public int deleteUser(String name) {
        System.out.println("    [deleteUser] 正在删除用户: " + name);
        return 1;
    }

    @Override
    public String findUser(String name) {
        System.out.println("    [findUser] 正在查询用户: " + name);
        return "用户[" + name + "]";
    }

    @Override
    public void riskyOperation() {
        System.out.println("    [riskyOperation] 执行危险操作...");
        throw new RuntimeException("模拟数据库连接失败!");
    }
}