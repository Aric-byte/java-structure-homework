package com.Spring.dao.annotation;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper //使用了Mybatis的注解
public interface UserMapper {


    User selectById(Integer id);

    User selectByUsername(String username);

    List<User> selectAll();

    List<User> selectByCondition(User user);

    int insert(User user);

    int insertAndGetId(User user);

    int updateById(User user);

    int deleteById(Integer id);

    int deleteByUsername(String username);
}