package com.Spring.Ioc.Annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component("carBySetter") //注解操作中Spring的核心4大注解之一
/*
* bean的4大注解 :按照逻辑进行分类
* @Component : 定义一个组件  可以是组件 默认值是singleton 类似于Bean的定义
* @service : 定义一个服务  特定用于业务逻辑的处理
* @controller : 定义一个控制器  特定用于处理HTTP请求
* @repository : 定义一个DAO的操作  特定用于数据的持久化操作
* 从本质上讲 这4个注解的功能是一致的
* */
public class Car {

    private String brand;
    private String model;
    private int price;
    private Wheel wheel;

    public Car() {
        System.out.println("  [Car] 无参构造器");
    }

    public Car(String brand, String model, int price) {
        this.brand = brand;
        this.model = model;
        this.price = price;
    }

    @Value("比亚迪") //相当于xml中<property>标签的value属性
    public void setBrand(String brand) {
        this.brand = brand;
    }

    @Value("秦PLUS")
    public void setModel(String model) {
        this.model = model;
    }

    @Value("128000")
    public void setPrice(int price) {
        this.price = price;
    }

    // @Autowired
    //@Qualifier("wheelSingleton")
   @Resource(name = "wheelSingleton")
    //属性对象的注入  注入的是一个对象  而不是一个值
    /*
    *  属性对象的注入 可以采用3种方式
    * 1. @Autowired + @Qualifier 注入 按照类型+名称的注入
    * 2. @Autowired 注入 按照类型注入注入 要求Spring容器中 同类型的对象只能有一个
    * 3.Resource 注入 按照名称注入注入 要求Spring容器中 同类型的对象只能有一个

    * */
    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
        System.out.println("  [Car] @Autowired + @Qualifier 注入 Wheel -> " + wheel.getType());
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getPrice() {
        return price;
    }

    public Wheel getWheel() {
        return wheel;
    }

    @Override
    public String toString() {
        return "Car{brand='" + brand + "', model='" + model + "', price=" + price
                + ", wheel=" + wheel + "}";
    }
}