package com.Spring.Ioc.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/*
* Spring的IOC/DI的操作 :依赖的反转注入
*   解决的主要问题: 对象的创建和管理
*   1.Spring作为一个管理者 , 用于创建和管理对象
*           A类 中 包含一个B类的对象   A类(new B()) 嵌套的模式
*           Spring进行B类的创建和管理 , 而不是A类的创建和管理   实质上 B对象的创建 由A类 转移给Spring
*
* Spring类似是一个 容器 ,其中管理若干的对象的实例化和生命周期  当A需要使用时 Spring提供B对象给A
* IOC : Inversion of Control  控制反转  依赖反转反转注入
* DI : Dependency Injection  依赖注入
*
* */

/*
*  当实例化对象时  如果类中 包含 构造方法  setter方法   init方法
*   创建对象的顺序 一是调用构造方法  二是调用setter方法  三是调用init方法
*
* */


public class SpringXmlIoCTest {

    //最简单的car的实例化
    @Test
    void test00_simple() {
        System.out.println("==============================================");
        System.out.println("   Spring IoC - XML 配置方式  简单示例");
        System.out.println("==============================================\n");
        //获取car的实例
        //获取Spring容器的实例,Spring容器创建所有的定义的对象
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");
//       //启动容器  加载所有的BeanDefinition
        String[] names = ctx.getBeanDefinitionNames();
        for (String name : names) {
          //  BeanDefinition def = ctx.getBeanFactory().getBeanDefinition(name);
            System.out.println(name);
        }

       Car car = ctx.getBean("car1", Car.class);
        System.out.println("  -> " + car);

    }

    @Test
    @DisplayName("1. 无参构造 + Setter 注入值类型")
    void test01_setterValue() {
        System.out.println("==============================================");
        System.out.println("   Spring IoC - XML 配置方式  完整示例");
        System.out.println("==============================================\n");

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");

        System.out.println("===== 1. 无参构造 + Setter 注入值类型 =====");
        System.out.println("  -> " + ctx.getBean("carBySetter"));

        ctx.close(); //关闭容器,释放资源
    }

    @Test
    @DisplayName("2. 构造器注入值类型")
    void test02_constructorValue() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");

        System.out.println("\n===== 2. 构造器注入值类型 =====");
        System.out.println("  -> " + ctx.getBean("carByConstructor"));

        ctx.close();
    }

    @Test
    @DisplayName("3. Setter + ref 注入引用类型") //最长使用的注入方式
    void test03_setterRef() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");

        System.out.println("\n===== 3. Setter + ref 注入引用类型 =====");
        System.out.println("  -> " + ctx.getBean("carWithWheelRef"));

        ctx.close();
    }

    @Test
    @DisplayName("4. 内部 Bean (inner bean)")
    void test04_innerBean() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");

        System.out.println("\n===== 4. 内部 Bean (inner bean) =====");
        System.out.println("  -> " + ctx.getBean("carWithInnerWheel"));

        ctx.close();
    }

    @Test
    @DisplayName("5. 构造器 + index + ref")
    void test05_constructorIndexRef() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");

        System.out.println("\n===== 5. 构造器 + index + ref =====");
        System.out.println("  -> " + ctx.getBean("carFullConstructor"));

        ctx.close();
    }

    @Test
    @DisplayName("6. 集合属性注入 (List / Set / Map / Properties)")
    void test06_collectionInjection() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");

        System.out.println("\n===== 6. 集合属性注入 (List / Set / Map / Properties) =====");
        System.out.println("  -> " + ctx.getBean("personRich"));

        ctx.close();
    }

    @Test
    @DisplayName("7. Bean 作用域 scope")


    void test07_beanScope() {
        /*作用域的使用 scope 定义的是对象的生命周期范围
         * singleton : 单例模式  默认  每次只产生一个对象 可以重复的使用
         * prototype : 原型模式  每次获取对象时都创建一个新的实例
         * 在网络中 scope 还有其他的取值
         *  request : 每次请求都创建一个新的实例  用于Web应用
         *  session : 每次会话都创建一个新的实例  用于Web应用
         *  global-session : 全局会话  用于Web应用

         */

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");
        System.out.println("\n===== 7. Bean 作用域 scope =====");
        Object s1 = ctx.getBean("wheelSingleton");
        Object s2 = ctx.getBean("wheelSingleton");
        System.out.println("  singleton(默认): " + (s1 == s2));

        Object p1 = ctx.getBean("wheelPrototype");
        Object p2 = ctx.getBean("wheelPrototype");
        System.out.println("  prototype:       " + (p1 != p2));


        //可以通过打印实例对象的hashcode来判断是否是同一个对象
        System.out.println("  singleton(默认): " + s1.hashCode());
        System.out.println("  singleton(默认): " + s2.hashCode());
        System.out.println("  prototype:       " + p1.hashCode());
        System.out.println("  prototype:       " + p2.hashCode());
        ctx.close();
    }

    @Test
    @DisplayName("8. Bean 生命周期 (init-method / destroy-method)")
    void test08_beanLifecycle() {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");
        System.out.println("\n===== 8. Bean 生命周期 (init-method / destroy-method) =====");
        System.out.println("  -> " + ctx.getBean("carLifecycle"));

        ctx.close();
    }

    @Test
    @DisplayName("9. 容器中所有 Bean")
    void test09_allBeans() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");

        System.out.println("\n===== 9. 容器中所有 Bean =====");
        for (String name : ctx.getBeanDefinitionNames()) {
            BeanDefinition def = ctx.getBeanFactory().getBeanDefinition(name);
            System.out.println("  - " + name + "  scope=" + def.getScope());
        }

        System.out.println("\n==============================================");
        System.out.println("  关闭容器 - 触发 destroy-method");
        System.out.println("==============================================\n");

        ctx.close();
    }

    /*
    * Spring 管理的 Bean  一般情况下都包含  初始化方法 构造方法 settter
    *   这三个方法的运行的顺序是  构造方法 -> settter -> 初始化方法
    *
    * */
}