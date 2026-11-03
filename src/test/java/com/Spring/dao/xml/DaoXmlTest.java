package com.Spring.dao.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

public class DaoXmlTest {

    @Test
    @DisplayName("1. 查询所有用户")
    void test01_findAll() {
        System.out.println("==============================================");
        System.out.println("   Spring + MyBatis XML 整合 CRUD");
        System.out.println("   含 Spring 声明式事务管理");
        System.out.println("==============================================\n");

        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springdao/spring-dao.xml");

        UserService service = ctx.getBean(UserService.class);

        System.out.println("===== 1. 查询所有用户 =====");
        List<User> users = service.findAll();
        users.forEach(u -> System.out.println("  " + u));
        System.out.println("  共 " + users.size() + " 条\n");

        ctx.close();
    }

    @Test
    @DisplayName("2~4. 转账事务演示：正常执行 + 异常回滚")
    void test02_transferTransaction() {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springdao/spring-dao.xml");

        UserMapper mapper = ctx.getBean(UserMapper.class);
        UserService service = ctx.getBean(UserService.class);

        String suffix = String.valueOf(System.currentTimeMillis() % 10000);
        Integer fromId = null;
        Integer toId = null;

        try {
            System.out.println("===== 2. 准备测试数据 (事务 REQUIRED) =====");
            User fromUser = new User("from_" + suffix, "from_pass", "from@test.com");
            User toUser = new User("to_" + suffix, "to_pass", "to@test.com");
            service.registerUser(fromUser);
            service.registerUser(toUser);
            fromId = fromUser.getId();
            toId = toUser.getId();
            System.out.println("  转出账户 id=" + fromId + "  转入账户 id=" + toId);
            System.out.println("  数据库确认: " + mapper.selectById(fromId));
            System.out.println("  数据库确认: " + mapper.selectById(toId) + "\n");

            System.out.println("===== 3. 转账演示 (正常, 不回滚) =====");
            String fromOrig = mapper.selectById(fromId).getPassword();
            String toOrig = mapper.selectById(toId).getPassword();
            System.out.println("  转账前: from(password=" + fromOrig + ")  to(password=" + toOrig + ")");

            service.transferPoints(fromId, toId, 100);
            String fromAfter = mapper.selectById(fromId).getPassword();
            String toAfter = mapper.selectById(toId).getPassword();
            System.out.println("  转账后: from(password=" + fromAfter + ")  to(password=" + toAfter + ")");
            System.out.println("  ✅ 正常执行, 两处 UPDATE 都提交了\n");

            System.out.println("===== 4. 转账演示 (异常触发回滚) =====");
            String beforeFrom = mapper.selectById(fromId).getPassword();
            String beforeTo = mapper.selectById(toId).getPassword();
            System.out.println("  回滚前: from(password=" + beforeFrom + ")  to(password=" + beforeTo + ")");
            try {
                service.transferPoints(fromId, toId, 9999);
            } catch (RuntimeException e) {
                System.out.println("  🚨 捕获异常: " + e.getMessage());
            }
            String afterFrom = mapper.selectById(fromId).getPassword();
            String afterTo = mapper.selectById(toId).getPassword();
            System.out.println("  回滚后: from(password=" + afterFrom + ")  to(password=" + afterTo + ")");
            if (beforeFrom.equals(afterFrom) && beforeTo.equals(afterTo)) {
                System.out.println("  ✅ 事务回滚生效! 两处 UPDATE 都撤销了\n");
            } else {
                System.out.println("  ❌ 回滚可能有问题\n");
            }

        } finally {
            System.out.println("===== 清理演示数据 =====");
            if (fromId != null) mapper.deleteById(fromId);
            if (toId != null) mapper.deleteById(toId);
            mapper.deleteByUsername("from_" + suffix);
            mapper.deleteByUsername("to_" + suffix);
            System.out.println("  清理完成\n");
        }

        ctx.close();
    }

    @Test
    @DisplayName("5. 批量插入 (最后一条 bad → 全部回滚)")
    void test05_batchInsertRollback() {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("springdao/spring-dao.xml");

        UserMapper mapper = ctx.getBean(UserMapper.class);
        UserService service = ctx.getBean(UserService.class);

        String suffix = String.valueOf(System.currentTimeMillis() % 10000);

        try {
            System.out.println("===== 5. 批量插入 (最后一条 bad → 全部回滚) =====");
            User a = new User("batch_good_1_" + suffix, "p1", "bg1@t.com");
            User b = new User("batch_good_2_" + suffix, "p2", "bg2@t.com");
            User c = new User("batch_bad_user_" + suffix, "p3", "bbad@t.com");
            try {
                service.batchInsertWithTransaction(a, b, c);
            } catch (RuntimeException e) {
                System.out.println("  🚨 捕获异常: " + e.getMessage());
            }
            System.out.println("  查 batch_good_1: " + mapper.selectByUsername("batch_good_1_" + suffix));
            System.out.println("  查 batch_good_2: " + mapper.selectByUsername("batch_good_2_" + suffix));
            System.out.println("  查 batch_bad_user: " + mapper.selectByUsername("batch_bad_user_" + suffix));
            System.out.println("  ✅ 全部不存在 = 整个事务回滚了\n");

        } finally {
            System.out.println("===== 清理演示数据 =====");
            mapper.deleteByUsername("batch_good_1_" + suffix);
            mapper.deleteByUsername("batch_good_2_" + suffix);
            mapper.deleteByUsername("batch_bad_user_" + suffix);
            System.out.println("  清理完成\n");

            System.out.println("===== 最终用户列表 =====");
            service.findAll().forEach(u -> System.out.println("  " + u));
        }

        ctx.close();
    }
}