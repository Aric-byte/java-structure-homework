package com.Spring.Aop.Annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AopAnnotationTest {

    @Test
    @DisplayName("1. 正常返回的方法 (saveUser + findUser)")
    void test01_normalReturn() {
        System.out.println("==============================================");
        System.out.println("   Spring AOP - 注解配置方式  完整示例");
        System.out.println("==============================================\n");

        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(AopAnnotationConfig.class);

        UserService proxy = ctx.getBean(UserService.class);

        System.out.println("===== 1. 正常返回的方法 (saveUser + findUser) =====");
        proxy.saveUser("张三");
        System.out.println();
        String result = proxy.findUser("李四");
        System.out.println("  >>> 主程序拿到返回值: " + result);

        ctx.close();
    }

    @Test
    @DisplayName("2. 返回值是 int 的方法 (deleteUser)")
    void test02_intReturn() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(AopAnnotationConfig.class);

        UserService proxy = ctx.getBean(UserService.class);

        System.out.println("\n===== 2. 返回值是 int 的方法 (deleteUser) =====");
        int del = proxy.deleteUser("王五");
        System.out.println("  >>> 主程序拿到返回值: " + del);

        ctx.close();
    }

    @Test
    @DisplayName("3. 抛出异常的方法 (riskyOperation)")
    void test03_exceptionThrow() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(AopAnnotationConfig.class);

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
}