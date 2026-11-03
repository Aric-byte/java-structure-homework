package com.Spring.Aop.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AopXmlTest {

    @Test
    //测试前置前置通知
    void test01_normalbefore(){
        System.out.println("===== 0. 前置前置通知 =====");
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springaop/aop-xml.xml");
        UserService proxy = ctx.getBean(UserService.class);
        proxy.saveUser("前置方法执行的参数张三三");
        ctx.close();
    }

    @Test
    @DisplayName("1. 正常返回的方法 (saveUser + findUser)")
    void test01_normalReturn() {
        System.out.println("==============================================");
        System.out.println("   Spring AOP - XML 配置方式  完整示例");
        System.out.println("==============================================\n");

        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springaop/aop-xml.xml");

        UserService proxy = ctx.getBean(UserService.class);

        System.out.println("===== 1. 正常返回的方法 (saveUser + findUser) =====");
        proxy.saveUser("张三");
        System.out.println();
        String result = proxy.findUser("李四"); //产生返回值
        System.out.println("  >>> 主程序拿到返回值: " + result);

        ctx.close();
    }

    @Test
    @DisplayName("2. 返回值是 int 的方法 (deleteUser)")
    void test02_intReturn() {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springaop/aop-xml.xml");

        UserService proxy = ctx.getBean(UserService.class);

        System.out.println("\n===== 2. 返回值是 int 的方法 (deleteUser) =====");
        int del = proxy.deleteUser("王五");
        System.out.println("  >>> 主程序拿到返回值: " + del);

        ctx.close();
    }

    @Test
    @DisplayName("3. 抛出异常的方法 (riskyOperation)")
    void test03_exceptionThrow() {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springaop/aop-xml.xml");

        UserService proxy = ctx.getBean(UserService.class);

        System.out.println("\n===== 3. 抛出异常的方法 (riskyOperation) =====");
        try {
            proxy.riskyOperation();
        } catch (Exception ignored) {
        }

        System.out.println("\n==============================================");
        System.out.println("  关闭容器");
        System.out.println("==============================================");

        ctx.close();
    }
    //添加一个aound通知的测试
    @Test
    @DisplayName("4. aound通知 (saveUser)")
    void test04_around() {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springaop/aop-xml.xml");

        UserService proxy = ctx.getBean(UserService.class);

        System.out.println("\n===== 4. aound通知 (saveUser) =====");
        proxy.saveUser("aound通知的参数张三");

        ctx.close();
    }
}