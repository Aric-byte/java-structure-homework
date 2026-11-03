package com.Spring.Aop.Annotation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan(basePackages = "com.Spring.Aop.Annotation")
@EnableAspectJAutoProxy(proxyTargetClass = false) //开启自动代理 Aop   // 基于接口的代理 可以识别@Aspect注解的类
//proxyTargetClass: 是否开启基于类的代理, false 表示基于接口的代理, true 表示基于类的代理
public class AopAnnotationConfig {//进行环境的初始化操作
}