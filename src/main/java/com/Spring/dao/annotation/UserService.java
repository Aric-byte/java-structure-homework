package com.Spring.dao.annotation;


import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserService {

    User findById(Integer id);

    List<User> findAll();

    void registerUser(User user);

    void transferPoints(Integer fromUserId, Integer toUserId, int points);

    void batchInsertWithTransaction(User... users);
}