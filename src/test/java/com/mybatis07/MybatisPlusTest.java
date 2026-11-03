package com.mybatis07;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mybatis07.config.MybatisPlusConfig;
import com.mybatis07.config.MyMetaObjectHandler;
import com.mybatis07.entity.MpStudent;
import com.mybatis07.mapper.StudentMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

public class MybatisPlusTest {

    private static SqlSessionFactory factory;

    private static SqlSessionFactory buildSqlSessionFactory() throws Exception {
        Properties props = new Properties();
        props.load(MybatisPlusTest.class.getClassLoader().getResourceAsStream("common/db.properties"));

        String driver = props.getProperty("jdbc.driver");
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");

        DataSource ds = new com.mybatis07.config.SimpleDataSource(driver, url, username, password);

        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(ds);
        bean.setTypeAliasesPackage("com.mybatis07.entity");

        MybatisPlusInterceptor interceptor = MybatisPlusConfig.buildInterceptor();
        bean.setPlugins(interceptor);

        com.baomidou.mybatisplus.core.config.GlobalConfig globalConfig = new com.baomidou.mybatisplus.core.config.GlobalConfig();
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        bean.setGlobalConfig(globalConfig);

        SqlSessionFactory sqlSessionFactory = bean.getObject();
        sqlSessionFactory.getConfiguration().addMapper(StudentMapper.class);
        return sqlSessionFactory;
    }

    @BeforeAll
    static void initFactory() throws Exception {
        factory = buildSqlSessionFactory();
    }

    @AfterAll
    static void destroyFactory() {
        factory = null;
    }

    @Test
    @DisplayName("一、BaseMapper 内置方法：selectById / selectList")
    void test01_baseMapperMethods() {
        SqlSession session = factory.openSession();
        StudentMapper mapper = session.getMapper(StudentMapper.class);

        System.out.println("======================================================");
        System.out.println("一、BaseMapper 内置方法：selectById / selectList");
        System.out.println("======================================================\n");

        MpStudent s1 = mapper.selectById(1L);
        System.out.println("selectById(1) -> " + s1.getName());

        List<MpStudent> all = mapper.selectList(null);
        System.out.println("selectList(null) -> 共 " + all.size() + " 条");

        session.close();
    }

    @Test
    @DisplayName("二、QueryWrapper 条件构造器")
    void test02_queryWrapper() {
        SqlSession session = factory.openSession();
        StudentMapper mapper = session.getMapper(StudentMapper.class);

        System.out.println("\n======================================================");
        System.out.println("二、QueryWrapper 条件构造器");
        System.out.println("======================================================\n");

        QueryWrapper<MpStudent> wrapper = new QueryWrapper<>();
        wrapper.like("name", "Zhang").gt("score", 60);
        List<MpStudent> result = mapper.selectList(wrapper);
        System.out.println("WHERE name LIKE '%Zhang%' AND score > 60 -> " + result.size() + " 条");

        System.out.println("\n>>> LambdaQueryWrapper（类型安全：字段名编译期检查，重构改名不怕拼错）");
        LambdaQueryWrapper<MpStudent> lw = new LambdaQueryWrapper<>();
        lw.like(MpStudent::getName, "Zhang").gt(MpStudent::getScore, 60);
        List<MpStudent> lambdaResult = mapper.selectList(lw);
        System.out.println("等价 Lambda 写法 -> " + lambdaResult.size() + " 条");

        session.close();
    }

    @Test
    @DisplayName("三、分页查询 selectPage（需 PaginationInnerInterceptor）")
    void test03_selectPage() {
        SqlSession session = factory.openSession();
        StudentMapper mapper = session.getMapper(StudentMapper.class);

        System.out.println("\n======================================================");
        System.out.println("三、分页查询 selectPage（需 PaginationInnerInterceptor）");
        System.out.println("======================================================\n");

        Page<MpStudent> page = new Page<>(1, 2);
        IPage<MpStudent> pageResult = mapper.selectPage(page, null);
        System.out.println("第 1 页，每页 2 条");
        System.out.println("  总记录数: " + pageResult.getTotal());
        System.out.println("  总页数: " + pageResult.getPages());
        pageResult.getRecords().forEach(s -> System.out.println("    - " + s.getName()));

        session.close();
    }

    @Test
    @DisplayName("四~六、insert（自动填充）+ updateById（乐观锁）+ deleteById（逻辑删除）")
    void test04_insertUpdateDelete() {
        SqlSession session = factory.openSession();
        StudentMapper mapper = session.getMapper(StudentMapper.class);

        System.out.println("\n======================================================");
        System.out.println("四、insert（自动填充 createTime + updateTime）");
        System.out.println("======================================================\n");

        MpStudent newStu = new MpStudent("MP_Test", 20, "mp@test.com", "Computer", 95.0);
        int rows = mapper.insert(newStu);
        session.commit();
        System.out.println("insert 行数: " + rows);
        System.out.println("自增主键: " + newStu.getId());
        System.out.println("自动填充 createTime: " + newStu.getCreateTime());
        System.out.println("自动填充 updateTime: " + newStu.getUpdateTime());

        System.out.println("\n======================================================");
        System.out.println("五、updateById（乐观锁 @Version）");
        System.out.println("======================================================\n");

        MpStudent update = new MpStudent();
        update.setId(newStu.getId());
        update.setScore(99.0);
        int updateRows = mapper.updateById(update);
        session.commit();
        System.out.println("updateById 行数: " + updateRows);
        MpStudent afterUpdate = mapper.selectById(newStu.getId());
        System.out.println("  version 变化: " + newStu.getVersion() + " -> " + afterUpdate.getVersion());
        System.out.println("  updateTime 变化: " + afterUpdate.getUpdateTime());

        System.out.println("\n======================================================");
        System.out.println("六、deleteById（逻辑删除 @TableLogic）");
        System.out.println("======================================================\n");

        int delRows = mapper.deleteById(newStu.getId());
        session.commit();
        System.out.println("deleteById 行数: " + delRows + "（实际是 UPDATE SET deleted=1）");

        MpStudent deleted = mapper.selectById(newStu.getId());
        System.out.println("selectById 结果: " + (deleted == null ? "null ✅ 逻辑删除生效" : "还能查到？"));

        session.close();
    }
}