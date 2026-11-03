package com.Spring.Aop.Annotation;

public interface UserService { //注解中使用的接口的声明与配置文件的书写一致

    void saveUser(String name);

    int deleteUser(String name);

    String findUser(String name);

    void riskyOperation();
}