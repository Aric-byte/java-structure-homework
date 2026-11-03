package com.mybatis05.xml;

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

public class MybatisXmlTest {

    private static SqlSessionFactory factory;

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis05/xml/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. 一对一 one2oneByXml（一次 LEFT JOIN）")
    void test01_one2one() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("========== 1. 一对一 one2oneByXml（一次 LEFT JOIN）==========");
        Emp emp = empMapper.one2oneByXml(7369);
        System.out.println("员工: " + emp.getEname());
        if (emp.getDept() != null) {
            System.out.println("  部门: " + emp.getDept().getDname() + " / " + emp.getDept().getLoc());
        }

        session.close();
    }

    @Test
    @DisplayName("2. 多对一 many2oneByXml（一次 LEFT JOIN）")
    void test02_many2one() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("\n========== 2. 多对一 many2oneByXml（一次 LEFT JOIN）==========");
        empMapper.many2oneByXml().forEach(e -> {
            String dname = e.getDept() != null ? e.getDept().getDname() : "无部门";
            System.out.println("  " + e.getEname() + " -> " + dname);
        });

        session.close();
    }

    @Test
    @DisplayName("3. 一对多 one2manyByXml（一次 LEFT JOIN + collection）")
    void test03_one2many() {
        SqlSession session = factory.openSession();
        DeptMapper deptMapper = session.getMapper(DeptMapper.class);

        System.out.println("\n========== 3. 一对多 one2manyByXml（一次 LEFT JOIN + collection）==========");
        Dept dept = deptMapper.one2manyByXml(20);
        System.out.println("部门: " + dept.getDname());
        if (dept.getEmps() != null) {
            System.out.println("  员工数: " + dept.getEmps().size());
            dept.getEmps().forEach(e -> System.out.println("    - " + e.getEname()));
        }

        session.close();
    }

    @Test
    @DisplayName("4. 多对多 many2manyByXml（两次 LEFT JOIN + DISTINCT）")
    void test04_many2many() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("\n========== 4. 多对多 many2manyByXml（两次 LEFT JOIN + DISTINCT）==========");
        Emp empSkills = empMapper.many2manyByXml(7369);
        System.out.println("员工: " + empSkills.getEname());
        if (empSkills.getSkills() != null) {
            System.out.println("  技能数: " + empSkills.getSkills().size());
            empSkills.getSkills().forEach(s -> System.out.println("    - " + s.getName() + ": " + s.getDescription()));
        }

        session.close();
    }
}