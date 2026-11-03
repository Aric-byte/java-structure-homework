package com.mybatis03.xml;

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
import java.util.Arrays;
import java.util.List;

public class MybatisXmlTest {

    private static SqlSessionFactory factory;

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis03/xml/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("1. <if> + <where> 多条件组合查询")
    void test01_ifAndWhere() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 1. <if> + <where> 多条件组合查询 ==========");
        User q1 = new User();
        q1.setUsername("zhang");
        q1.setEmail("admin@test.com");
        System.out.println("条件: username like '%zhang%' AND email = 'admin@test.com'");
        mapper.findUsersByCondition(q1).forEach(u -> System.out.println("  -> " + u));

        System.out.println("\n条件: 全部 null（<where> 自动去掉 WHERE）");
        mapper.findUsersByCondition(new User()).forEach(u -> System.out.println("  -> " + u));

        session.close();
    }

    @Test
    @DisplayName("2. <set> 动态更新（只更新非空字段）")
    void test02_setDynamicUpdate() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 2. <set> 动态更新（只更新非空字段）==========");
        User update = new User();
        update.setId(1);
        update.setUsername("NewName");
        update.setEmail("new@email.com");
        int rows = mapper.updateUserSelective(update);
        System.out.println("更新行数: " + rows + "（password 为 null，不会被 SET）");
        session.rollback();

        session.close();
    }

    @Test
    @DisplayName("3. <choose>/<when>/<otherwise> 互斥分支")
    void test03_chooseWhenOtherwise() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 3. <choose>/<when>/<otherwise> 互斥分支 ==========");
        User q3 = new User();
        q3.setId(1);
        q3.setUsername("zhang");
        System.out.println("id=1 和 username='zhang' 同时有值 → 只取 id（优先级最高）");
        mapper.findUserPriority(q3).forEach(u -> System.out.println("  -> " + u));

        session.close();
    }

    @Test
    @DisplayName("4. <foreach> IN 查询")
    void test04_foreachIn() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 4. <foreach> IN 查询 ==========");
        List<Integer> ids = Arrays.asList(1, 2, 3);
        System.out.println("SELECT * FROM user WHERE id IN (1, 2, 3)");
        mapper.findByIds(ids).forEach(u -> System.out.println("  -> " + u));

        session.close();
    }

    @Test
    @DisplayName("5. <foreach> 批量插入")
    void test05_foreachBatchInsert() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 5. <foreach> 批量插入 ==========");
        String ts = String.valueOf(System.currentTimeMillis());
        User u1 = new User(0, "BatchA_" + ts, "123456", "a_" + ts + "@test.com", null, null);
        User u2 = new User(0, "BatchB_" + ts, "123456", "b_" + ts + "@test.com", null, null);
        int batchRows = mapper.batchInsert(Arrays.asList(u1, u2));
        System.out.println("批量插入行数: " + batchRows);
        session.rollback();

        session.close();
    }

    @Test
    @DisplayName("6. <trim> 自定义裁剪")
    void test06_trim() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 6. <trim> 自定义裁剪 ==========");
        User q6 = new User();
        q6.setUsername("zhang");
        System.out.println("trim prefix='WHERE' prefixOverrides='AND |OR '");
        mapper.findUsersByTrim(q6).forEach(u -> System.out.println("  -> " + u));

        session.close();
    }

    @Test
    @DisplayName("7. <sql> + <include> 复用片段")
    void test07_sqlInclude() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 7. <sql> + <include> 复用片段 ==========");
        User q7 = new User();
        q7.setEmail("admin@test.com");
        mapper.findUsersWithInclude(q7).forEach(u -> System.out.println("  -> " + u));

        session.close();
    }

    @Test
    @DisplayName("8. <bind> OGNL 表达式绑定（模糊查询拼接 %）")
    void test08_bind() {
        SqlSession session = factory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        System.out.println("========== 8. <bind> OGNL 表达式绑定（模糊查询拼接 %）==========");
        System.out.println("<bind name=\"keywordLike\" value=\"'%' + keyword + '%'\"/>");
        System.out.println("→ SQL 变为: WHERE username LIKE #{keywordLike}，% 拼接不再依赖 Java 层");
        mapper.findUserByBind("zhang").forEach(u -> System.out.println("  -> " + u));

        session.close();
    }
}