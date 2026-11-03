package com.springmvc.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 学生实体 —— springmvc 模块独立实体（模块自治原则）
 *
 * 【知识点】SpringMVC 实体参数绑定规则：
 * 请求参数名 == 属性名 → 自动调用 setter 注入（要求有无参构造器 + 标准 setter）
 *   POST /param/form?name=x&age=20 → new Student() + setName("x") + setAge(20)
 *
 * 【嵌套对象绑定】
 *   student.address.province → Student.address.province
 *   student.addresses[0].province → Student.addresses[0].province
 */
public class Student implements Serializable {

    private Integer id;
    private String name;
    private Integer age;
    private String major;
    private Double score;

    private Address address;

    private List<Address> addresses;

    public Student() {
    }

    public Student(Integer id, String name, Integer age, String major, Double score) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.major = major;
        this.score = score;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', age=" + age
                + ", major='" + major + "', score=" + score
                + ", address=" + address
                + ", addresses=" + addresses + "}";
    }
}