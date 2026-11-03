package com.ssm.entity;

import java.io.Serializable;

/**
 * 学生实体 —— ssm 模块独立实体（对应 student 表：id, name, age, email, major, score）
 *
 * 【SSM 三层中的角色】
 *   Controller(表现层) → Service(业务层) → Mapper(持久层) → DB
 *   实体在各层之间穿梭：Controller 收表单 → 传 Service → 传 Mapper → 落库
 */
public class Student implements Serializable {

    private Integer id;
    private String name;
    private Integer age;
    private String email;
    private String major;
    private Double score;

    public Student() {
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
        return "Student{id=" + id + ", name='" + name + "', age=" + age
                + ", email='" + email + "', major='" + major + "', score=" + score + "}";
    }
}
