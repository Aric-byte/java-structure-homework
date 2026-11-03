package com.mybatis06.annotation;

import com.mybatis06.entity.Student;
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
        InputStream is = Resources.getResourceAsStream("mybatis06/annotation/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("@CacheNamespace 开启二级缓存（注解方式）")
    void test_cacheNamespaceAnnotation() {
        System.out.println("=== @CacheNamespace 开启二级缓存（注解方式）===\n");

        SqlSession session1 = factory.openSession();
        StudentMapper m1 = session1.getMapper(StudentMapper.class);

        System.out.println(">>> 第 1 次 selectById(1) —— 发 SQL");
        Student s1 = m1.selectById(1);
        System.out.println("    结果: " + s1.getName());

        System.out.println("\n>>> 第 2 次 selectById(1) —— 命中一级缓存");
        m1.selectById(1);

        session1.close();

        SqlSession session2 = factory.openSession();
        StudentMapper m2 = session2.getMapper(StudentMapper.class);
        System.out.println("\n>>> Session2 selectById(1) —— 命中二级缓存！");
        m2.selectById(1);
        session2.close();
    }
}