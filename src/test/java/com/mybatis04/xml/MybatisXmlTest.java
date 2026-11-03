package com.mybatis04.xml;

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

public class MybatisXmlTest {

    private static SqlSessionFactory factory;

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis04/xml/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. 多对一：association 嵌套 select + 延迟加载")
    void test01_associationLazyLoading() {
        SqlSession session = factory.openSession();
        EmpMapper empMapper = session.getMapper(EmpMapper.class);

        System.out.println("========== 1. 多对一：association 嵌套 select ==========");
        System.out.println(">>> 调用 getEmpWithDeptLazy(7369)，观察日志：只有主查询（查 emp 表）的 SQL");
        Emp emp = empMapper.getEmpWithDeptLazy(7369);
        System.out.println(">>> 此时【只打印员工名，不访问 dept 属性】—— 日志中还没有 dept 的 SQL");
        System.out.println("  员工: " + emp.getEname() + "  (deptno=" + emp.getDeptno() + ")");

        System.out.println("\n>>> 现在访问 emp.getDept()，观察日志：此时才发出子查询（查 dept 表）的 SQL —— 延迟加载生效！");
        System.out.println("  所属部门: " + emp.getDept().getDname() + " / " + emp.getDept().getLoc());

        session.close();
    }

    @Test
    @DisplayName("2. 一对多：collection 嵌套 select + 延迟加载")
    void test02_collectionLazyLoading() {
        SqlSession session = factory.openSession();
        DeptMapper deptMapper = session.getMapper(DeptMapper.class);

        System.out.println("\n========== 2. 一对多：collection 嵌套 select ==========");
        System.out.println(">>> 调用 getDeptWithEmpsLazy(20)，观察日志：只有主查询（查 dept 表）的 SQL");
        Dept dept = deptMapper.getDeptWithEmpsLazy(20);
        System.out.println(">>> 此时【只打印部门名，不访问 emps 属性】—— 日志中还没有 emp 的 SQL");
        System.out.println("  部门: " + dept.getDname());

        System.out.println("\n>>> 现在访问 dept.getEmps()，观察日志：此时才发出子查询（查 emp 表）的 SQL —— 延迟加载生效！");
        System.out.println("  员工数量: " + dept.getEmps().size());
        dept.getEmps().forEach(e -> System.out.println("    - " + e.getEname() + " (" + e.getJob() + ")"));

        session.close();
    }

    @Test
    @DisplayName("3. 延迟加载配置回顾")
    void test03_lazyLoadingConfig() {
        System.out.println("\n========== 3. 延迟加载配置回顾 ==========");
        System.out.println("  mybatis-config.xml 中：");
        System.out.println("    <setting name=\"lazyLoadingEnabled\" value=\"true\"/>      <!-- 开启延迟加载 -->");
        System.out.println("    <setting name=\"aggressiveLazyLoading\" value=\"false\"/>  <!-- 按需加载，不激进 -->");
        System.out.println("  aggressiveLazyLoading=false：访问任一属性只加载该属性对应的子查询；");
        System.out.println("  若为 true（或旧版默认），访问任意方法都会触发全部子查询。");
    }
}