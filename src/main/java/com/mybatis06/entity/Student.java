package com.mybatis06.entity;

import java.io.Serializable;

/**
 * 学生实体类（student 表）—— mybatis06 模块独立实体
 * <p>
 * 知识点：MyBatis 缓存机制演示用实体。
 * 一级缓存（SqlSession 级）与二级缓存（namespace 级）均以该表的查询为示例。
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer age;
    private String email;
    private String major;
    private Double score;

    public Student() {
    }

    public Student(String name, Integer age, String email, String major, Double score) {
        this.name = name;
        this.age = age;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + '\'' + ", age=" + age +
                ", email='" + email + '\'' + ", major='" + major + '\'' + ", score=" + score + '}';
    }
}
