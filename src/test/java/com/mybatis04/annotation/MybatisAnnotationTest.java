package com.mybatis04.annotation;

import com.mybatis04.entity.Dept;
import com.mybatis04.entity.Emp;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class MybatisAnnotationTest {

    private static SqlSessionFactory factory;

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis04/annotation/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. 基础 CRUD（注解方式）")
    void test01_basicCrud() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("========== 1. 基础 CRUD（注解方式）==========");
        empMapper.findAll().forEach(e -> System.out.println("  Emp: " + e.getEname()));

        session.close();
    }

    @Test
    @DisplayName("2. @One 多对一（N+1 查询）")
    void test02_oneAnnotation() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("\n========== 2. @One 多对一（N+1 查询）==========");
        System.out.println(">>> 观察日志：调用方法时只有主查询 SQL，");
        System.out.println(">>> 访问 getDept() 才出现子查询 SQL —— 延迟加载（lazyLoadingEnabled=true）");
        Emp emp = empMapper.getEmpWithDeptAnno(7369);
        System.out.println("员工: " + emp.getEname());
        if (emp.getDept() != null) {
            System.out.println("  部门: " + emp.getDept().getDname());
        }

        session.close();
    }

    @Test
    @DisplayName("3. @Many 一对多（N+1 查询）")
    void test03_manyAnnotation() {
        SqlSession session = factory.openSession();
        DeptMapper deptMapper = session.getMapper(DeptMapper.class);

        System.out.println("\n========== 3. @Many 一对多（N+1 查询）==========");
        System.out.println(">>> 同样：访问 getEmps() 才触发子查询；");
        System.out.println(">>> 若换成查询部门列表，每个部门都发一次子查询 → N+1 问题（详见 mybatis05）");
        Dept dept = deptMapper.getDeptWithEmpsAnno(20);
        System.out.println("部门: " + dept.getDname());
        if (dept.getEmps() != null) {
            dept.getEmps().forEach(e -> System.out.println("  员工: " + e.getEname()));
        }

        session.close();
    }
}