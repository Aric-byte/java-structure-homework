package com.mybatis02.annotation;

import com.mybatis02.entity.User;
import com.mybatis02.entity.Vo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * MyBatis Mapper 接口 —— 注解方式（mybatis02）
 *
 * 对应 resources/mybatis02/annotation/mybatis-config.xml
 */
public interface UserMapper {

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addUser(User user);

    @Update("UPDATE user SET username = #{username}, password = #{password}, email = #{email} WHERE id = #{id}")
    int updateUser(User user);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteUser(Integer id);

    /**
     * 多参数方法
     * 注解方式下参数名可以直接写，MyBatis 会按方法参数名匹配
     * 如果编译时没有保留参数名，需要用 @Param
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    User login(@Param("username") String username, @Param("password") String password);

    /**
     * 使用 AS 别名 + resultType 映射到 Vo
     * 注解方式没有 resultMap，只能靠 SQL AS 别名
     */
    @Select("SELECT id AS p1, username AS p2, password AS p3 FROM user")
    List<Vo> selectAllVo();

    // ===== mybatis02 新增：#{} vs ${} 参数绑定 =====

    /**
     * ${} 注入风险演示（仅教学，生产禁用）
     * 【知识点】'%' || ... || '%' 这种字符串拼接把参数直接写进 SQL 文本
     */
    @Select("SELECT * FROM user WHERE username LIKE '%${keyword}%'")
    List<User> findLikeUnsafe(@Param("keyword") String keyword);

    /**
     * ${} 合法场景：动态 ORDER BY 列名
     * 【知识点】列名不能被 #{} 占位（会变成字符串字面量），只能 ${} 拼接
     */
    @Select("SELECT * FROM user ORDER BY ${orderCol}")
    List<User> findByOrderColumn(@Param("orderCol") String orderCol);
}