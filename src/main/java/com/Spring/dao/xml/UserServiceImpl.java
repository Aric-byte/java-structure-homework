package com.Spring.dao.xml;


import java.util.List;

public class UserServiceImpl implements UserService {

    private UserMapper userMapper;

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    //注入 UserMapper 接口的实现类  XML   文件的注入操作

    @Override
    public User findById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> findAll() {
        return userMapper.selectAll();
    }

    @Override
    public void registerUser(User user) {
        userMapper.insertAndGetId(user);
    }

    @Override
    public void transferPoints(Integer fromUserId, Integer toUserId, int points) {
        User from = userMapper.selectById(fromUserId);
        if (from == null) {
            throw new RuntimeException("转出账户不存在: " + fromUserId);
        }
        User to = userMapper.selectById(toUserId);
        if (to == null) {
            throw new RuntimeException("转入账户不存在: " + toUserId);
        }

        User fromUpdate = new User();
        fromUpdate.setId(fromUserId);
        fromUpdate.setPassword(from.getPassword() + "_sub");
        userMapper.updateById(fromUpdate);

        if (points > 1000) {
            throw new RuntimeException("转账金额过大触发异常 → 事务回滚!");
        }

        User toUpdate = new User();
        toUpdate.setId(toUserId);
        toUpdate.setPassword(to.getPassword() + "_add");
        userMapper.updateById(toUpdate);
    }

    @Override
    public void batchInsertWithTransaction(User... users) {
        for (int i = 0; i < users.length; i++) {
            userMapper.insertAndGetId(users[i]);
            if (i == users.length - 1 && users[i].getUsername().contains("bad")) {
                throw new RuntimeException("第 " + (i + 1) + " 条包含 'bad' → 全部回滚!");
            }
        }
    }
}