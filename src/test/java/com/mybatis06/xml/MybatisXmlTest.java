package com.mybatis06.xml;

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

public class MybatisXmlTest {

    private static SqlSessionFactory factory; //session的连接工厂

    @BeforeAll
    static void initFactory() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis06/xml/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("一、一级缓存：同一个 SqlSession 中查询相同数据只发 1 次 SQL")
    void test01_firstLevelCache_sameSessionOnlyOneSQL() {
        System.out.println("======================================================");
        System.out.println("一、一级缓存：同一个 SqlSession 中查询相同数据只发 1 次 SQL");
        System.out.println("======================================================\n");

        SqlSession session1 = factory.openSession(); // 打开一个 SqlSession 一级缓存作用域为 SESSION 默认开启
        StudentMapper m1 = session1.getMapper(StudentMapper.class);

        System.out.println(">>> 第 1 次 selectById(1) —— 发 SQL");
        Student s1 = m1.selectById(1); //在数据库级别上执行一个sql语句，返回一个Student对象 , 并缓存到一级缓存中
        System.out.println("    结果: " + s1.getName());

        System.out.println("\n>>> 第 2 次 selectById(1) —— 命中一级缓存，不发 SQL！");
        Student s2 = m1.selectById(1); //同一个session 并且相同的sql(参数) , 会命中一级缓存，不发 SQL
        System.out.println("    s1 == s2 ? " + (s1 == s2));

        //清空一级缓存 ：任何 insert/update/delete 都会清空本 SqlSession 的一级缓存 close() clearCache() 也会清空一级缓存
        System.out.println("\n>>> 同一 SqlSession 执行 update —— 一级缓存被清空！");
        Student mod = new Student();
        mod.setId(1);
        mod.setName("CacheBuster");
        mod.setAge(20);
        mod.setEmail("cb@test.com");
        mod.setMajor("CS");
        mod.setScore(90.0);
        m1.updateById(mod); //更新数据库中的数据 , 并清空一级缓存
        session1.rollback(); //回滚事务 , 会清空一级缓存
        System.out.println(">>> update 后（已 rollback）再 selectById(1) —— 重新发 SQL");
        System.out.println("    【知识点】任何 insert/update/delete 都会清空本 SqlSession 的一级缓存");
        Student s3 = m1.selectById(1); //重新查询数据库 , 会重新发 SQL
        System.out.println("    结果: " + s3.getName());

        session1.close();
    }

    @Test
    @DisplayName("二、一级缓存：不同 SqlSession 之间不共享")
    void test02_firstLevelCache_notSharedBetweenSessions() {
        System.out.println("======================================================");
        System.out.println("二、一级缓存：不同 SqlSession 之间不共享");
        System.out.println("======================================================\n");

        SqlSession session1 = factory.openSession();
        StudentMapper m1 = session1.getMapper(StudentMapper.class);
        Student s1 = m1.selectById(1);
        System.out.println("    结果: " + s1.getName());

        SqlSession session2 = factory.openSession();
        StudentMapper m2 = session2.getMapper(StudentMapper.class);
        Student s2 = m2.selectById(1);

        System.out.println("    结果: " + s2.getName());
        System.out.println("    s1 == s2 ? " + (s1 == s2) + "  （不同 SqlSession，对象不是同一个，一级缓存相互独立）");
        session2.close();

        session1.close();
    }

    @Test
    @DisplayName("三、二级缓存：Session1.close() 后数据提交到二级缓存")
    void test03_secondLevelCache_afterSessionClose() {
        System.out.println("======================================================");
        System.out.println("三、二级缓存：Session1.close() 后数据提交到二级缓存");
        System.out.println("======================================================\n");

        SqlSession session1 = factory.openSession();
        StudentMapper m1 = session1.getMapper(StudentMapper.class);
        System.out.println(">>> Session1 第 1 次 selectById(1) —— 发 SQL，先查一级缓存（没有）再查数据库");
        m1.selectById(1); // 一级缓存没有，会查数据库
        session1.close();// 关闭后一级缓存被清空 , 二级缓存被提交

        SqlSession session2 = factory.openSession();
        StudentMapper m2 = session2.getMapper(StudentMapper.class);
        System.out.println(">>> Session2 selectById(1) —— 应该命中二级缓存，不发 SQL！");
        m2.selectById(1); // 二级缓存有，不会查数据库
        session2.close();
    }

    @Test
    @DisplayName("四、二级缓存失效：增删改操作会清空本 Mapper 的二级缓存")
    void test04_secondLevelCache_invalidationOnUpdate() {
        System.out.println("======================================================");
        System.out.println("四、二级缓存失效：增删改操作会清空本 Mapper 的二级缓存");
        System.out.println("======================================================\n");

        SqlSession session1 = factory.openSession();
        StudentMapper m1 = session1.getMapper(StudentMapper.class);
        m1.selectById(5);
        session1.close();  //id为5的二级缓存 被session1提交到二级缓存

        SqlSession session2 = factory.openSession();
        StudentMapper m2 = session2.getMapper(StudentMapper.class);
        Student update = new Student();
        update.setId(5);
        update.setName("Modified");
        update.setAge(20);
        update.setEmail("m@test.com");
        update.setMajor("CS");
        update.setScore(80.0);
        m2.updateById(update); // session2 将id为5的二级缓存被清空，后续查询会重新从数据库查询
        session2.commit();
        session2.close();

        SqlSession session3 = factory.openSession();
        StudentMapper m3 = session3.getMapper(StudentMapper.class);
        System.out.println(">>> updateById 后再 selectById(5) —— 二级缓存已清空，重新发 SQL");
        m3.selectById(5);// 二级缓存没有，会查数据库
        session3.close();
    }

    @Test
    @DisplayName("五、useCache='false' 的查询（selectAll 不走二级缓存）")
    void test05_useCacheFalse_selectAllAlwaysHitDB() {
        System.out.println("======================================================");
        System.out.println("五、useCache='false' 的查询（selectAll 不走二级缓存）");
        System.out.println("======================================================\n");

        SqlSession session1 = factory.openSession();
        StudentMapper m1 = session1.getMapper(StudentMapper.class);
        m1.selectAll();
        session1.close();

        SqlSession session2 = factory.openSession();
        StudentMapper m2 = session2.getMapper(StudentMapper.class);
        System.out.println(">>> Session2 selectAll —— 每次都查数据库（useCache=false）");
        m2.selectAll();
        session2.close();
    }
}