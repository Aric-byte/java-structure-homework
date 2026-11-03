package com.ssm.service;

import com.ssm.entity.Student;
import com.ssm.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务层实现 —— SSM 整合核心之二
 *
 * 【整合要点】
 * 1. @Service —— 由 applicationContext.xml 的 component-scan 注册成 Bean
 * 2. @Autowired 注入 Mapper 代理 —— 接口无实现类，注入的是
 *    MapperScannerConfigurer 生成的动态代理对象
 * 3. @Transactional —— 注解声明式事务（需配置 <tx:annotation-driven/>）。
 *    对比 Spring/dao 模块的 XML 式 tx:advice：注解式按方法粒度，更直观。
 *    原理：事务代理在方法正常返回时提交、抛 RuntimeException 时回滚。
 */
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    /** 构造器注入（推荐）：字段可 final，依赖一目了然，便于单测 */
    @Autowired
    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public List<Student> findAll() {
        return studentMapper.selectAll();
    }

    @Override
    public Student findById(Integer id) {
        return studentMapper.selectById(id);
    }

    @Override
    @Transactional
    public void addStudent(Student student) {
        studentMapper.insert(student);
    }

    @Override
    @Transactional
    public void addThenFail(Student student) {
        studentMapper.insert(student);          // ① SQL 已执行（日志可见 INSERT）
        throw new RuntimeException("故意抛出异常 → 事务回滚，上面的 INSERT 不会提交到数据库");
        // ② 异常穿过 @Transactional 代理 → 代理捕获并 rollback
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        studentMapper.deleteById(id);
    }
}
