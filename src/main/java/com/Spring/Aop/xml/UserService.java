package com.Spring.Aop.xml;

public interface UserService { //模拟一个用户服务接口

    //模拟了操作的接口
    void saveUser(String name);

    int deleteUser(String name);

    String findUser(String name);

    void riskyOperation();
}