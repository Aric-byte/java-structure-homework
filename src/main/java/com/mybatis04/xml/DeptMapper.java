package com.mybatis04.xml;

import com.mybatis04.entity.Dept;

/**
 * 部门 Mapper 接口 —— XML 方式（mybatis04：多表查询 + 延迟加载）
 *
 * ==========================================
 * 【mybatis04 核心知识点】
 * ==========================================
 *
 * 1. collection（一对多）的"嵌套 select"写法：
 *    collection 的 select 属性指向另一条查询语句，
 *    column 把主查询的外键列传给子查询
 *
 * 2. 延迟加载（Lazy Loading）：
 *    mybatis-config.xml 中 lazyLoadingEnabled=true、
 *    aggressiveLazyLoading=false 时，主查询返回后【不会】立即执行子查询，
 *    直到代码访问 getEmps() 才发出第二条 SQL
 *
 * 3. 一次 JOIN 的"嵌套结果"写法见 mybatis05（三种关系对比模块）
 * 4. 动态 SQL（if/where/foreach 等）见 mybatis03，本模块不混入
 *
 * 对应 XML：resources/mybatis04/xml/mapper/DeptMapper.xml
 */
public interface DeptMapper {

    /**
     * 根据编号查询部门
     * 【知识点】分步查询的"子查询目标"——被 EmpMapper 的 association select 属性引用
     */
    Dept findById(Integer deptno);

    /**
     * 一对多：collection 嵌套 select（分步查询 + 延迟加载）
     * 【知识点】① 主查询只查 dept 表
     *           ② 访问 getEmps() 时才按 deptno 查 emp 表（观察日志确认）
     */
    Dept getDeptWithEmpsLazy(Integer deptno);
}
