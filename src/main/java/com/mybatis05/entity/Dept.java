package com.mybatis05.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 部门实体类（dept 表）—— mybatis05 模块独立实体
 * <p>
 * 知识点：一对多关系（一个部门有多个员工），
 * XML 用 collection、注解用 @Many 分步查询或 JOIN 一次查询映射到 emps。
 */
public class Dept implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer deptno;          // 部门编号（主键）
    private String dname;            // 部门名称
    private String loc;              // 部门位置

    // 一对多关系：一个部门拥有多个员工
    private List<Emp> emps;

    public Dept() {
    }

    public Dept(Integer deptno, String dname, String loc) {
        this.deptno = deptno;
        this.dname = dname;
        this.loc = loc;
    }

    public Integer getDeptno() {
        return deptno;
    }

    public void setDeptno(Integer deptno) {
        this.deptno = deptno;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public List<Emp> getEmps() {
        return emps;
    }

    public void setEmps(List<Emp> emps) {
        this.emps = emps;
    }

    @Override
    public String toString() {
        return "Dept{" +
                "deptno=" + deptno +
                ", dname='" + dname + '\'' +
                ", loc='" + loc + '\'' +
                '}';
    }
}
