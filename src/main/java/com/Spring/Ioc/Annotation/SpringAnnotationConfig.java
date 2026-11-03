package com.Spring.Ioc.Annotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Configuration // 标识这是一个配置类 ,特定使用在Spring容器中
@ComponentScan(basePackages = "com.Spring.Ioc.Annotation") // 扫描com.Spring.Ioc.Annotation包下的所有组件
public class SpringAnnotationConfig {
    // 构造器的调用顺序: 配置类的构造器 -> 组件的构造器 启动了容器后调用
    public SpringAnnotationConfig() {
        System.out.println("  [SpringAnnotationConfig] 构造器被调用");
    }

    @Bean(name = "wheelPrototype") // 定义一个Bean ,Bean的名称为wheelPrototype
    @Scope("prototype")//每一次的请求 容器都会创建一个新的实例
    public Wheel wheelPrototype() {
        return new Wheel("固特异", 18);
    }

    @Bean(name = "carLifecycle")
    public Car carLifecycle() {
        return new Car("保时捷", "911", 1500000);
    }

    //对于集合类型的数据, Spring容器中是单例的, 所以需要在Bean方法中创建一个新的实例

    @Bean(name = "hobbies") // 定义一个Bean ,Bean的名称为hobbies 创建了一个List类型集合的实例
    public List<String> hobbies() {
        List<String> list = new ArrayList<>();
        list.add("游泳");
        list.add("编程");
        list.add("读书");
        return list;
    }

    @Bean(name = "skills")
    public Set<String> skills() {
        return new HashSet<>(Arrays.asList("Java", "Spring", "MyBatis"));
    }

    @Bean(name = "scores")
    public Map<String, Double> scores() {
        Map<String, Double> map = new HashMap<>();
        map.put("语文", 85.5);
        map.put("数学", 92.0);
        return map;
    }

    @Bean(name = "healthInfo")
    public Properties healthInfo() {
        Properties p = new Properties();
        p.setProperty("height", "175cm");
        p.setProperty("weight", "70kg");
        return p;
    }
}