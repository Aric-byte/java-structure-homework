package com.mybatis02.annotation;

import com.mybatis02.entity.User;
import com.mybatis02.entity.Vo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

public class MybatisAnnotationTest {

    private static SqlSessionFactory factory;

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis02/annotation/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. 查询所有")
    void test01_findAll() {
        System.out.println("==============================================");
        System.out.println("   MyBatis 进阶 —— 注解方式");
        System.out.println("==============================================\n");

        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 1. 查询所有 =====");
        List<User> all = mapper.findAll();
        for (User u : all) System.out.println("  -> " + u);
        System.out.println("  共 " + all.size() + " 条");

        sqlSession.close();
    }

    @Test
    @DisplayName("2. Vo 类 + SQL AS 别名")
    void test02_voWithAlias() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 2. Vo 类 + SQL AS 别名 =====");
        List<Vo> voList = mapper.selectAllVo();
        for (Vo v : voList) System.out.println("  -> " + v);

        sqlSession.close();
    }

    @Test
    @DisplayName("3. 多参数 login")
    void test03_login() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 3. 多参数 login =====");
        User loginUser = mapper.login("zhangsan", "pass111");
        System.out.println("  登录结果: " + loginUser);

        sqlSession.close();
    }

    @Test
    @DisplayName("4. 插入 + 更新 + 按ID查询 + 删除")
    void test04_insertUpdateFindDelete() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 4. 插入 =====");
        User insert = new User();
        insert.setUsername("ann_ch02_" + System.currentTimeMillis());
        insert.setPassword("123456");
        insert.setEmail("ch02_ann@qq.com");
        int rows = mapper.addUser(insert);
        sqlSession.commit();
        System.out.println("  影响行数: " + rows + "  自增主键: " + insert.getId());

        System.out.println("\n===== 5. 更新 =====");
        insert.setUsername("ann_ch02_updated");
        rows = mapper.updateUser(insert);
        sqlSession.commit();
        System.out.println("  影响行数: " + rows);

        System.out.println("\n===== 6. 按 ID 查询 =====");
        User u = mapper.findById(insert.getId());
        System.out.println("  -> " + u);

        System.out.println("\n===== 7. 删除 =====");
        rows = mapper.deleteUser(insert.getId());
        sqlSession.commit();
        System.out.println("  影响行数: " + rows);

        sqlSession.close();
    }

    @Test
    @DisplayName("8. ${} 直接拼接（注入演示）")
    void test08_findLikeUnsafe() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 8. ${} 直接拼接（注入演示）=====");
        List<User> unsafeList = mapper.findLikeUnsafe("a' OR '1'='1");
        System.out.println("  注入后查出 " + unsafeList.size() + " 条");

        sqlSession.close();
    }

    @Test
    @DisplayName("9. ${} 动态 ORDER BY 列名")
    void test09_findByOrderColumn() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 9. ${} 动态 ORDER BY 列名 =====");
        mapper.findByOrderColumn("id DESC").forEach(user -> System.out.println("  -> " + user.getUsername()));

        sqlSession.close();

        System.out.println("\n==============================================");
        System.out.println("   注解方式演示完成");
        System.out.println("==============================================");
    }
}