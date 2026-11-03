package com.mybatis04.xml;

import com.mybatis04.entity.Emp;

import java.util.List;

/**
 * 员工 Mapper 接口 —— XML 方式（mybatis04：多表查询 + 延迟加载）
 *
 * ==========================================
 * 【mybatis04 核心知识点】
 * ==========================================
 *
 * 1. association（多对一）的"嵌套 select"写法：
 *    association 的 select 属性指向 DeptMapper.findById，
 *    column="deptno" 把外键传给子查询
 *
 * 2. 延迟加载：主查询只查 emp 表，访问 getDept() 才触发子查询
 *
 * 3. 一次 JOIN 的"嵌套结果"写法见 mybatis05；动态 SQL 见 mybatis03
 *
 * 对应 XML：resources/mybatis04/xml/mapper/EmpMapper.xml
 */
public interface EmpMapper {

    /**
     * 根据部门编号查询员工列表
     * 【知识点】分步查询的"子查询目标"——被 DeptMapper 的 collection select 属性引用
     */
    List<Emp> findByDeptno(Integer deptno);

    /**
     * 多对一：association 嵌套 select（分步查询 + 延迟加载）
     * 【知识点】① 主查询只查 emp 表
     *           ② 访问 getDept() 时才按 deptno 查 dept 表（观察日志确认）
     */
    Emp getEmpWithDeptLazy(Integer empno);
}
