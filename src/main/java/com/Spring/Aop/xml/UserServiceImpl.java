package com.Spring.Aop.xml;

public class UserServiceImpl implements UserService { //编写一个具体的业务接口的实现

    @Override
    public void saveUser(String name) { //实现保存用户的方法
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
        throw new RuntimeException("模拟数据库连接失败!"); //模拟一个危险的操作，模拟抛出一个运行时异常
    }
}