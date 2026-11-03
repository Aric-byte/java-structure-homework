package com.mybatis03.annotation;

import com.mybatis03.entity.User;
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
        InputStream is = Resources.getResourceAsStream("mybatis03/annotation/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. 注解方式匿名 @Results")
    void test01_anonymousResults() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 1. 注解方式匿名 @Results ==========");
        User u1 = mapper.findByIdAnno(1);
        System.out.println("findByIdAnno(1) -> " + u1);

        session.close();
    }

    @Test
    @DisplayName("2. 注解方式命名 @Results(id='annoUserMap')")
    void test02_namedResults() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("\n========== 2. 注解方式命名 @Results(id='annoUserMap') ==========");
        User u2 = mapper.findByIdWithAnnoMap(1);
        System.out.println("findByIdWithAnnoMap(1) -> " + u2);

        session.close();
    }

    @Test
    @DisplayName("3. @ResultMap 引用注解命名 ResultMap")
    void test03_resultMapReference() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("\n========== 3. @ResultMap 引用注解命名 ResultMap ==========");
        mapper.findAllByAnnoMap().forEach(u -> System.out.println("  -> " + u));

        session.close();
    }

    @Test
    @DisplayName("4. 注解方式 <script> 动态 SQL")
    void test04_scriptDynamicSql() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("\n========== 4. 注解方式 <script> 动态 SQL ==========");
        User q = new User();
        q.setUsername("zhang");
        mapper.findUsersAnnoDynamic(q).forEach(u -> System.out.println("  -> " + u));

        session.close();
    }
}