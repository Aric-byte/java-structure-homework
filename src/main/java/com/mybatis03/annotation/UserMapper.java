package com.mybatis03.annotation;

import com.mybatis03.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    @Results({
            @Result(property = "id",       column = "id",           id = true),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email",    column = "email"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    User findByIdAnno(Integer id);

    @Select("SELECT * FROM user WHERE id = #{id}")
    @Results(id = "annoUserMap", value = {
            @Result(property = "id",       column = "id",           id = true),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email",    column = "email"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    User findByIdWithAnnoMap(Integer id);

    @Select("SELECT * FROM user")
    @ResultMap("annoUserMap")
    List<User> findAllByAnnoMap();

    @Select("<script>" +
            "SELECT * FROM user " +
            "<where>" +
            "  <if test='username != null and username != \"\"'>" +
            "    AND username LIKE CONCAT('%', #{username}, '%')" +
            "  </if>" +
            "  <if test='email != null and email != \"\"'>" +
            "    AND email = #{email}" +
            "  </if>" +
            "  <if test='id != null'>" +
            "    AND id = #{id}" +
            "  </if>" +
            "</where>" +
            "</script>")
    @ResultMap("annoUserMap")
    List<User> findUsersAnnoDynamic(User user);
}