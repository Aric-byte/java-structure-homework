package com.Spring.Aop.Annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect  //注解的书写 切面对象  一定书写成切面
@Component //定义成为组件对象 形成代理对象
public class LogAspect {
    /*
    *  在切面中一定要声明切点
    *       切点的声明一定是作用在方法上    默认的情况下: 方法名和切点的名是一致的
    *
    *   1. 全域的切点 使用通配符 不涉及方法的执行体 仅是一个声明
    *   2. 指定方法的切点
    * */

    @Pointcut("execution(* com.Spring.Aop.Annotation.UserService.*(..))")
    //定义切点 ,并且进行织入操作 没有方法体的方法名 默认成为切点名
    public void servicePointcut() { //没有方法体的切点, 用于定义切点的规则 定义全域的切点
    }

    @Pointcut("execution(* com.Spring.Aop.Annotation.UserService.save*(..))")
    //定义切点 ,并且进行织入操作
    public void savePointcut() { //没有方法体的切点, 用于定义切点的规则 定义全域的切点
    }

    //进行通知的书写 通知和切点的绑定

    @Before("savePointcut()")
    public void beforeLog(JoinPoint jp) {
        String method = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        System.out.println("  ⏰ @Before — [" + method + "] 参数: " + Arrays.toString(args));
    }

    @AfterReturning(pointcut = "servicePointcut()", returning = "result")
    public void afterReturningLog(JoinPoint jp, Object result) {
        String method = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        System.out.println("  ✅ @AfterReturning — [" + method + "] 参数: " + Arrays.toString(args) + " → 返回: " + result);
    }

    @AfterThrowing(pointcut = "servicePointcut()", throwing = "ex")
    public void afterThrowingLog(JoinPoint jp, Throwable ex) {
        String method = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        System.out.println("  ❌ @AfterThrowing — [" + method + "] 参数: " + Arrays.toString(args) + " 异常: " + ex.getMessage());
    }

    @After("servicePointcut()")
    public void afterLog(JoinPoint jp) {
        String method = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        System.out.println("  🔔 @After — [" + method + "] 参数: " + Arrays.toString(args));
    }

    @Around("servicePointcut()")
    public Object aroundLog(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();
        System.out.println("  🌯 @Around — 进入 [" + method + "] 参数: " + Arrays.toString(args));

        Object[] newArgs = args.clone();
        if (newArgs.length > 0 && newArgs[0] instanceof String) {
            newArgs[0] = "[已加密]" + newArgs[0];
            System.out.println("  🌯 @Around — 修改参数后: " + Arrays.toString(newArgs));
        }

        long start = System.currentTimeMillis();
        try {
            Object ret = pjp.proceed(newArgs);
            long cost = System.currentTimeMillis() - start;
            System.out.println("  🌯 @Around — 退出 [" + method + "] 耗时: " + cost + "ms 返回: " + ret);
            return ret;
        } catch (Throwable t) {
            long cost = System.currentTimeMillis() - start;
            System.out.println("  🌯 @Around — 异常 [" + method + "] 耗时: " + cost + "ms");
            throw t;
        }
    }
}