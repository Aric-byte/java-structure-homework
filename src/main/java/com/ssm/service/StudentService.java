package com.ssm.service;

import com.ssm.entity.Student;

import java.util.List;

/**
 * 业务层接口 —— SSM 三层的中间层
 *
 * 【分层意义】Controller 只管"收参数/返视图"，Mapper 只管"SQL"，
 * 业务规则（校验、组合多张表、事务边界）都归 Service。
 */
public interface StudentService {

    List<Student> findAll();

    Student findById(Integer id);

    /** 正常新增（事务方法） */
    void addStudent(Student student);

    /**
     * 事务回滚演示：插入成功后故意抛异常 → 观察数据库【没有】这条数据
     * —— @Transactional 的 rollbackFor 默认对 RuntimeException 生效
     */
    void addThenFail(Student student);

    void deleteById(Integer id);
}
