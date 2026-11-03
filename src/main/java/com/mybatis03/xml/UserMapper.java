package com.mybatis03.xml;

import com.mybatis03.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<User> findUsersByCondition(User user);

    int updateUserSelective(User user);

    List<User> findUserPriority(User user);

    List<User> findByIds(@Param("ids") List<Integer> ids);

    int batchInsert(List<User> users);

    List<User> findUsersByTrim(User user);

    List<User> findUsersWithInclude(User user);

    /**
     * <bind> 标签：OGNL 表达式绑定
     * 【知识点】<bind name="keywordLike" value="'%' + keyword + '%'"/>
     *           在 SQL 前先计算表达式并绑定到新变量，模糊查询不用在 Java 层拼 %，
     *           也比 ${} 安全（结果仍走 #{} 预编译）
     */
    List<User> findUserByBind(@Param("keyword") String keyword);
}