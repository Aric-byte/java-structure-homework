package com.Spring.dao.xml;


public interface UserService {

    User findById(Integer id);

    java.util.List<User> findAll();

    void registerUser(User user);

    void transferPoints(Integer fromUserId, Integer toUserId, int points);

    void batchInsertWithTransaction(User... users);
}