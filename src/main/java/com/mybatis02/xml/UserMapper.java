package com.mybatis02.xml;

import com.mybatis02.entity.User;
import com.mybatis02.entity.Vo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper 接口 —— XML 方式（mybatis02：resultMap + 日志进阶）
 *
 * ==========================================
 * 【mybatis02 核心知识点】
 * ==========================================
 *
 * 1. resultType vs resultMap
 *    - resultType：自动映射，字段名和属性名一致才匹配；不一致要用 SQL AS 别名
 *    - resultMap：自定义映射，<id property="" column=""/> + <result property="" column=""/>
 *    - 两者互斥，不能同时用
 *
 * 2. 多参数传递
 *    - 多个参数必须用 @Param("别名") 标注
 *    - XML 中用 #{别名} 取值
 *    - login(username, password) 演示两参数的日志输出
 *
 * 3. Vo 类封装
 *    - 可以把查询结果映射到任意 Java 类（不要求和数据库表结构一致）
 *    - SQL AS 别名适配字段名不一致
 *    - resultMap 的 property 可以映射到 Vo 的任意属性
 *
 * 4. 日志进阶
 *    - Logback 是 SLF4J 的原生实现，现代推荐
 *    - mybatis02 重点演示日志对多参数 SQL 的输出
 */
public interface UserMapper {

    // ===== 基础 CRUD =====

    /** 查询所有 */
    List<User> findAll();

    /** 根据 ID 查询 */
    User findById(@Param("id") Integer id);

    /** 插入（主键回填） */
    int addUser(User user);

    /** 更新 */
    int updateUser(User user);

    /** 删除 */
    int deleteUser(@Param("id") Integer id);

    // ===== mybatis02 新增：多参数方法 =====

    /**
     * 登录验证（多参数）
     * 【知识点】两个参数都用 @Param 标注 → XML 中 #{username} #{password}
     *           日志会同时打印两个参数的值
     */
    User login(@Param("username") String username, @Param("password") String password);

    // ===== mybatis02 新增：resultMap + Vo =====

    /**
     * 使用 resultMap 映射到 Vo 类
     * 【知识点】resultMap 自定义映射规则：column → property
     *           可以把任意列映射到任意 Java 属性
     */
    List<Vo> selectAllByVo();

    /**
     * 使用 resultType + SQL AS 别名 映射到 Vo 类
     * 【知识点】select id as p1, username as p2, password as p3 → Vo(p1, p2, p3)
     *           纯靠 SQL 的 AS 来适配字段名不一致
     */
    List<Vo> selectAllVo();

    /**
     * 查询所有（用 AS 别名适配）
     * 【知识点】select id as uid from user → User.uid 能匹配上
     *           AS 别名是 resultType 处理字段不一致的唯一办法
     */
    List<User> selectAll();

    // ===== mybatis02 新增：#{} vs ${} 参数绑定 =====

    /**
     * 模糊查询 —— #{} 安全写法（推荐）
     * 【知识点】#{} 是预编译占位符（PreparedStatement 的 ?），
     *           值作为参数安全绑定，彻底防止 SQL 注入
     */
    List<User> findLikeSafe(@Param("keyword") String keyword);

    /**
     * 模糊查询 —— ${} 直接拼接（仅演示注入风险，生产环境禁用！）
     * 【知识点】${} 把参数值原样拼进 SQL 文本：
     *           keyword 传入 "a' OR '1'='1" 会拼出恒真条件 → 查出全表！
     */
    List<User> findLikeUnsafe(@Param("keyword") String keyword);

    /**
     * 动态排序列 —— ${} 的唯一合法场景
     * 【知识点】表名/列名不能用 #{}（值会被加引号变成字符串字面量 'id DESC'），
     *           动态 ORDER BY 列名只能用 ${}，且列名必须在代码层做白名单校验
     */
    List<User> findByOrderColumn(@Param("orderCol") String orderCol, @Param("limit") Integer limit);
}