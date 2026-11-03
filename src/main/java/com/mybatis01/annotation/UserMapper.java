package com.mybatis01.annotation;

import com.mybatis01.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * MyBatis Mapper 接口 —— 注解方式
 *
 * ==========================================
 * 【核心知识点：5 种常用注解】
 * ==========================================
 *
 *   @Select("SQL语句")       → 查询
 *   @Insert("SQL语句")       → 插入
 *   @Update("SQL语句")       → 更新
 *   @Delete("SQL语句")       → 删除
 *   @Results + @Result       → 复杂结果映射（等价于 XML 中的 resultMap）
 *
 *   @Options                 → 附加选项（主键回填、超时、批量操作等）
 *   @Param("别名")            → 指定参数在 SQL 中的名字
 *
 * ==========================================
 * 【注解方式 vs XML 方式 对比】
 * ==========================================
 *
 *   注解方式：
 *     + 简单直观，一个文件搞定
 *     + SQL 与 Java 代码紧耦合，方便查看
 *     - SQL 写死在 Java 中，修改 SQL 需要重新编译
 *     - 复杂 SQL（动态 SQL、多表关联）用注解非常别扭
 *     - 适合简单的单表 CRUD
 *
 *   XML 方式：
 *     + SQL 与 Java 代码解耦，修改 SQL 不用重新编译
 *     + 支持动态 SQL（<if> <where> <set> <foreach> 等标签）
 *     + 复杂查询、多表关联、resultMap 映射功能强大
 *     - 两个文件维护，略有分散
 *     - 适合中大型项目、复杂 SQL 场景
 *
 * ==========================================
 * 【参数绑定】
 * ==========================================
 *
 *   #{} → 预编译参数占位符（PreparedStatement 的 ?），
 *         自动类型转换、防止 SQL 注入 ✅ 本模块所有方法统一使用
 *   #{} vs ${} 的完整对比与注入演示 → 见 mybatis02
 */
public interface UserMapper {

    /**
     * 查询所有
     * @Select 等价于 XML 中的 <select> 标签
     * resultType 由方法返回类型自动推断 → List<User>
     */
    @Select("SELECT * FROM user")
    List<User> findAll();

    /**
     * 根据 ID 查询单个
     * @Param("id") 让 #{id} 可以正确绑定到参数
     * 如果没有 @Param，多参数方法 MyBatis 默认用 param1、param2
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Integer id);

    /**
     * 用户名模糊查询
     * CONCAT('%', #{username}, '%') 是 MySQL 原生字符串拼接函数
     * 也可以用注解方式的 SQL 字符串拼接：'%' || #{username} || '%'（部分数据库）
     */
    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{username}, '%')")
    List<User> findByUsername(@Param("username") String username);

    /**
     * Map 多条件查询
     * Map 的 key 直接对应 #{xxx} 中的名字
     * 不需要 @Param 注解，Map 本身就是一个命名空间
     */
    @Select("SELECT * FROM user WHERE password = #{password} OR username = #{username}")
    List<User> findByMap(Map<String, Object> map);

    /**
     * 聚合函数
     * 返回值 int 由 @Select 自动映射
     */
    @Select("SELECT COUNT(*) FROM user")
    int findCount();

    /**
     * 插入 + 主键回填
     * @Insert 的 SQL 写在注解里
     * @Options(useGeneratedKeys = true, keyProperty = "id")
     *   → 告诉 MyBatis 使用 JDBC getGeneratedKeys() 取回自增主键
     *   → 取回后赋值给 User 对象的 id 属性
     * 两者结合等价于 XML 中的：
     *   <insert id="insert" useGeneratedKeys="true" keyProperty="id">
     */
    @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新
     * POJO 传参 → 直接 #{属性名}
     */
    @Update("UPDATE user SET username = #{username}, password = #{password}, email = #{email} WHERE id = #{id}")
    int updateById(User user);

    /**
     * 删除
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}