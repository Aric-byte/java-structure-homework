package com.mybatis05.annotation;

import com.mybatis05.entity.Dept;
import com.mybatis05.entity.Emp;
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
        InputStream is = Resources.getResourceAsStream("mybatis05/annotation/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. 一对一 one2oneByAnn（@One + N+1）")
    void test01_one2one() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("========== 1. 一对一 one2oneByAnn（@One + N+1）==========");
        Emp emp = empMapper.one2oneByAnn(7369);
        System.out.println("员工: " + emp.getEname());
        if (emp.getDept() != null) {
            System.out.println("  部门: " + emp.getDept().getDname());
        }

        session.close();
    }

    @Test
    @DisplayName("2. 多对一 many2oneByAnn（@One + N+1）")
    void test02_many2one() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("\n========== 2. 多对一 many2oneByAnn（@One + N+1）==========");
        empMapper.many2oneByAnn().forEach(e -> {
            String dname = e.getDept() != null ? e.getDept().getDname() : "无";
            System.out.println("  " + e.getEname() + " -> " + dname);
        });

        session.close();
    }

    @Test
    @DisplayName("3. 一对多 one2manyByAnn（@Many + N+1）")
    void test03_one2many() {
        SqlSession session = factory.openSession();
        DeptMapper deptMapper = session.getMapper(DeptMapper.class);

        System.out.println("\n========== 3. 一对多 one2manyByAnn（@Many + N+1）==========");
        Dept dept = deptMapper.one2manyByAnn(20);
        System.out.println("部门: " + dept.getDname());
        if (dept.getEmps() != null) {
            dept.getEmps().forEach(e -> System.out.println("  员工: " + e.getEname()));
        }

        session.close();
    }

    @Test
    @DisplayName("4. 多对多 many2manyByAnn（@Many + N+1）")
    void test04_many2many() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("\n========== 4. 多对多 many2manyByAnn（@Many + N+1）==========");
        Emp empSkills = empMapper.many2manyByAnn(7369);
        System.out.println("员工: " + empSkills.getEname());
        if (empSkills.getSkills() != null) {
            empSkills.getSkills().forEach(s -> System.out.println("  技能: " + s.getName()));
        }

        session.close();
    }
}