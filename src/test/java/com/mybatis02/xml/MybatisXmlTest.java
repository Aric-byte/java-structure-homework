package com.mybatis02.xml;

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

public class MybatisXmlTest {

    private static SqlSessionFactory factory;

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis02/xml/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. resultType 自动映射")
    void test01_resultTypeAutoMapping() {
        System.out.println("==============================================");
        System.out.println("   MyBatis 进阶 —— XML 方式");
        System.out.println("   resultMap + Logback 日志 + Vo 类");
        System.out.println("==============================================\n");

        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 1. resultType 自动映射 =====");
        List<User> all = mapper.findAll();
        for (User u : all) System.out.println("  -> " + u);
        System.out.println("  共 " + all.size() + " 条");

        sqlSession.close();
    }

    @Test
    @DisplayName("2. resultType + SQL AS 别名")
    void test02_resultTypeWithAlias() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 2. resultType + SQL AS 别名 =====");
        List<User> aliasList = mapper.selectAll();
        for (User u : aliasList) System.out.println("  -> " + u);

        sqlSession.close();
    }

    @Test
    @DisplayName("3. Vo 类 + resultMap 自定义映射")
    void test03_voWithResultMap() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 3. Vo 类 + resultMap 自定义映射 =====");
        List<Vo> voList1 = mapper.selectAllByVo();
        for (Vo v : voList1) System.out.println("  -> " + v);

        sqlSession.close();
    }

    @Test
    @DisplayName("4. Vo 类 + SQL AS 别名（resultType）")
    void test04_voWithAlias() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 4. Vo 类 + SQL AS 别名（resultType）=====");
        List<Vo> voList2 = mapper.selectAllVo();
        for (Vo v : voList2) System.out.println("  -> " + v);

        sqlSession.close();
    }

    @Test
    @DisplayName("5. 多参数 login（观察日志输出）")
    void test05_loginMultiParam() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 5. 多参数 login（观察日志输出）=====");
        User loginUser = mapper.login("zhangsan", "pass111");
        System.out.println("  登录结果: " + loginUser);

        sqlSession.close();
    }

    @Test
    @DisplayName("6. 插入 + 按ID查询 + 更新 + 删除")
    void test06_insertFindUpdateDelete() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 6. 插入（观察 insert SQL 日志）=====");
        User insert = new User();
        insert.setUsername("xml_ch02_" + System.currentTimeMillis());
        insert.setPassword("123456");
        insert.setEmail("ch02@qq.com");
        int rows = mapper.addUser(insert);
        sqlSession.commit();
        System.out.println("  影响行数: " + rows + "  自增主键: " + insert.getId());

        System.out.println("\n===== 7. 按 ID 查询 =====");
        User u = mapper.findById(insert.getId());
        System.out.println("  -> " + u);

        System.out.println("\n===== 8. 更新 =====");
        u.setUsername("xml_ch02_updated");
        rows = mapper.updateUser(u);
        sqlSession.commit();
        System.out.println("  影响行数: " + rows);

        System.out.println("\n===== 9. 删除 =====");
        rows = mapper.deleteUser(insert.getId());
        sqlSession.commit();
        System.out.println("  影响行数: " + rows);

        sqlSession.close();
    }

    @Test
    @DisplayName("10. #{} + CONCAT 安全模糊查询")
    void test10_findLikeSafe() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 10. #{} + CONCAT 安全模糊查询 =====");
        List<User> safeList = mapper.findLikeSafe("zhang");
        System.out.println("  匹配 " + safeList.size() + " 条");
        safeList.forEach(user -> System.out.println("  -> " + user.getUsername()));

        sqlSession.close();
    }

    @Test
    @DisplayName("11. ${} 直接拼接（注入演示）")
    void test11_findLikeUnsafe() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 11. ${} 直接拼接（注入演示：keyword = \"a' OR '1'='1\"）=====");
        List<User> unsafeList = mapper.findLikeUnsafe("a' OR '1'='1");
        System.out.println("  注入后查出 " + unsafeList.size() + " 条（对比 #{} 版本的匹配条数）");

        sqlSession.close();
    }

    @Test
    @DisplayName("12. ${} 动态 ORDER BY 列名")
    void test12_findByOrderColumn() {
        SqlSession sqlSession = factory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== 12. ${} 动态 ORDER BY 列名（#{} 做不到）=====");
        mapper.findByOrderColumn("id DESC", 3).forEach(user -> System.out.println("  -> " + user.getUsername()));

        sqlSession.close();

        System.out.println("\n==============================================");
        System.out.println("   MyBatis 进阶演示完成");
        System.out.println("==============================================");
    }
}