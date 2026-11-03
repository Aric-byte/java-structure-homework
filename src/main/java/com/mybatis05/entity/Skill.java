package com.mybatis05.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 技能实体类（skill 表）—— mybatis05 模块独立实体
 * <p>
 * 知识点：多对多关系。
 * 一个员工可以拥有多项技能，一项技能也可以被多个员工拥有，
 * 通过中间表 emp_skill（empno + skill_id）关联。
 */
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;              // 技能编号（主键）
    private String name;             // 技能名称
    private String description;      // 技能描述

    // 多对多关系的另一侧：一项技能被多个员工拥有
    private List<Emp> emps;

    public Skill() {
    }

    public Skill(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Emp> getEmps() {
        return emps;
    }

    public void setEmps(List<Emp> emps) {
        this.emps = emps;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
