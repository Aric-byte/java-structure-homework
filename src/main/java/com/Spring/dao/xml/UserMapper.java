package com.Spring.dao.xml;


import java.util.List;

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