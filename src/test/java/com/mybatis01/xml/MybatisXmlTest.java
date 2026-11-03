package com.mybatis01.xml;

import com.mybatis01.entity.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisXmlTest {

    private static SqlSessionFactory factory;

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis01/xml/mybatis-config.xml");
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
        System.out.println("   MyBatis 基础 CRUD - XML 配置 + XML 映射");
        System.out.println("==============================================\n");

        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 1. 查询所有 =====");
        List<User> list = mapper.findAll();
        for (User u : list) System.out.println("  -> " + u);
        System.out.println("  共 " + list.size() + " 条");

        sqlSession.close();
    }

    @Test
    @DisplayName("2. 根据 ID 查询")
    void test02_findById() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 2. 根据 ID 查询 =====");
        User u1 = mapper.findById(1);
        System.out.println("  -> " + u1);

        sqlSession.close();
    }

    @Test
    @DisplayName("3. 用户名模糊查询")
    void test03_findByUsername() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 3. 用户名模糊查询 =====");
        List<User> likeList = mapper.findByUsername("zhang");
        for (User u : likeList) System.out.println("  -> " + u);

        sqlSession.close();
    }

    @Test
    @DisplayName("4. Map 多条件查询")
    void test04_findByMap() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 4. Map 多条件查询 =====");
        Map<String, Object> map = new HashMap<>();
        map.put("username", "zhangsan");
        map.put("password", "pass111");
        List<User> mapList = mapper.findByMap(map);
        for (User u : mapList) System.out.println("  -> " + u);

        sqlSession.close();
    }

    @Test
    @DisplayName("5. 聚合函数 COUNT(*)")
    void test05_findCount() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 5. 聚合函数 COUNT(*) =====");
        int count = mapper.findCount();
        System.out.println("  -> user 表共 " + count + " 条记录");

        sqlSession.close();
    }

    @Test
    @DisplayName("6. 插入 + 更新 + 删除（含主键回填）")
    void test06_insertUpdateDelete() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 6. 插入（含主键回填） =====");
        User insert = new User();
        insert.setUsername("xml_test_" + System.currentTimeMillis());
        insert.setPassword("123456");
        insert.setEmail("xml_test@qq.com");
        int rows = mapper.insert(insert);
        sqlSession.commit();
        System.out.println("  影响行数: " + rows + "  自增主键: " + insert.getId());

        System.out.println("\n===== 7. 更新 =====");
        User update = new User();
        update.setId(insert.getId());
        update.setUsername("xml_updated");
        update.setPassword("newpass");
        update.setEmail("updated@qq.com");
        rows = mapper.updateById(update);
        sqlSession.commit();
        System.out.println("  影响行数: " + rows);

        System.out.println("\n===== 8. 删除 =====");
        rows = mapper.deleteById(insert.getId());
        sqlSession.commit();
        System.out.println("  影响行数: " + rows);

        sqlSession.close();

        System.out.println("\n==============================================");
        System.out.println("   全部 XML 方式 CRUD 执行完毕");
        System.out.println("==============================================");
    }
}