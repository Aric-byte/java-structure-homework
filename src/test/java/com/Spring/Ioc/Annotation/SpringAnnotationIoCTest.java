package com.Spring.Ioc.Annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringAnnotationIoCTest {

    //区别于 xml配置方式  可以直接获取所有Bean的名称AnnotationConfigApplicationContext   ,使用了SpringAnnotationConfig自定义的辅助类
    //SpringAnnotationConfig自定义的辅助类 :扫描哪些需要Spring容器管理的类的位置
    //ComponentScanScan(basePackages = "com.Spring.Ioc.Annotation")
    //使用 AnnotationConfigApplicationContext ctx =
    //                new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);
                //  获取并启动Spring容器, 并扫描com.Spring.Ioc.Annotation包下的所有组件, 并将其添加到容器中

    @Test
    @DisplayName("1. @Value + @Component (值注入)")
    void test01_valueComponent() {
        System.out.println("==============================================");
        System.out.println("   Spring IoC - 注解配置方式  完整示例");
        System.out.println("==============================================\n");

        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);

        System.out.println("===== 1. @Value + @Component (值注入) =====");
        System.out.println("  -> Wheel: " + ctx.getBean(Wheel.class));


        ctx.close();
    }

    @Test
    @DisplayName("2. @Autowired Setter 注入引用类型")
    void test02_autowiredSetter() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);

        System.out.println("\n===== 2. @Autowired Setter 注入引用类型 =====");
        Car carSetter = (Car) ctx.getBean("carBySetter");
        System.out.println("  -> Car: " + carSetter);

        ctx.close();
    }

    @Test
    @DisplayName("3. @Scope 作用域")
    void test03_scope() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);

        System.out.println("\n===== 3. @Scope 作用域 =====");
        Object s1 = ctx.getBean("wheelSingleton");
        Object s2 = ctx.getBean("wheelSingleton");
        System.out.println("  singleton(@Component默认): " + (s1 == s2));
        Object p1 = ctx.getBean("wheelPrototype");
        Object p2 = ctx.getBean("wheelPrototype");
        System.out.println("  prototype(@Bean+@Scope):   " + (p1 != p2));

        ctx.close();
    }

    @Test
    @DisplayName("4. @Bean 集合属性注入")
    void test04_beanCollection() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);

        System.out.println("\n===== 4. @Bean 集合属性注入 =====");
        System.out.println("  -> " + ctx.getBean("personRich"));

        ctx.close();
    }

    @Test
    @DisplayName("5. Bean 生命周期")
    void test05_lifecycle() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);

        System.out.println("\n===== 5. Bean 生命周期 =====");
        System.out.println("  -> " + ctx.getBean("carLifecycle"));

        ctx.close();
    }

    @Test
    @DisplayName("6. 容器中所有 Bean")
    void test06_allBeans() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);
        new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);


        System.out.println("\n===== 6. 容器中所有 Bean =====");
        for (String name : ctx.getBeanDefinitionNames()) {
            String scope = ctx.getBeanFactory().getBeanDefinition(name).getScope();
            System.out.println("  - " + name + "  scope=" + scope);
        }

        System.out.println("\n==============================================");
        System.out.println("  关闭容器 - 触发 @PreDestroy");
        System.out.println("==============================================\n");

        ctx.close();
    }
}