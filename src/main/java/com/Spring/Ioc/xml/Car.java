package com.Spring.Ioc.xml;

public class Car {  //创建一个类 ,并将此类交由Spring容器进行管理

    //基本属性
    private String brand;
    private String model;
    private int price;
    private Wheel wheel; //对象属性  ,此属性需要Spring进行动态的属性注入

    public Car() {
        System.out.println("  [Car] 无参构造器");
    }

    public Car(String brand, String model, int price) {
        this.brand = brand;
        this.model = model;
        this.price = price;
        System.out.println("  [Car] 三参构造器 -> " + brand + " " + model);
    }

    public Car(String brand, String model, int price, Wheel wheel) {
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.wheel = wheel;
        System.out.println("  [Car] 四参构造器 -> " + brand + " " + model);
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    public void customInit() {
        System.out.println("  [Car] ★ init-method -> " + brand + " " + model);
    }

    public void customDestroy() {
        System.out.println("  [Car] ★ destroy-method -> " + brand + " " + model);
    }

    @Override
    public String toString() {
        return "Car{brand='" + brand + "', model='" + model + "', price=" + price
                + ", wheel=" + wheel + "}";
    }
}