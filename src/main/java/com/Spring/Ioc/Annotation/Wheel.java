package com.Spring.Ioc.Annotation;

import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("wheelSingleton")
@Primary //此注解表示此Bean是默认的Bean  当有多个Bean可以注入时  默认注入此Bean 为了避免出现同类型的注入失败
@Scope("singleton")
public class Wheel {

    @Value("米其林")
    private String type;

    @Value("17")
    private int size;

    public Wheel() {
    }

    public Wheel(String type, int size) {
        this.type = type;
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Wheel{type='" + type + "', size=" + size + "}";
    }
}