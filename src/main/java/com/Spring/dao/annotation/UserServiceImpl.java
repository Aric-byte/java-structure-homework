package com.Spring.dao.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("annUserService") //此处最好使用@Sevice
@Transactional(propagation = Propagation.REQUIRED) //开启事务
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public User findById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void registerUser(User user) {
        userMapper.insertAndGetId(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
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
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertWithTransaction(User... users) {
        for (int i = 0; i < users.length; i++) {
            userMapper.insertAndGetId(users[i]);
            if (i == users.length - 1 && users[i].getUsername().contains("bad")) {
                throw new RuntimeException("第 " + (i + 1) + " 条包含 'bad' → 全部回滚!");
            }
        }
    }
}