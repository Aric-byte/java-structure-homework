package com.springmvc.entity;

import java.io.Serializable;

/**
 * 地址实体 —— 用于嵌套对象参数绑定演示
 *
 * 【知识点】嵌套对象绑定规则：
 * 表单字段名使用 "对象属性.嵌套属性" 格式 → SpringMVC 自动注入
 * 例如：student.address.province → Student.address.province
 */
public class Address implements Serializable {

    private String province;
    private String city;
    private String street;

    public Address() {
    }

    public Address(String province, String city, String street) {
        this.province = province;
        this.city = city;
        this.street = street;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        return "Address{province='" + province + "', city='" + city
                + "', street='" + street + "'}";
    }
}