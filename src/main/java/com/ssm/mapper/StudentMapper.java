package com.ssm.mapper;

import com.ssm.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 持久层接口 —— SSM 整合核心之一
 *
 * 【整合要点】
 * 1. 这只是一个【普通接口】，不需要写实现类：
 *    applicationContext.xml 的 MapperScannerConfigurer 扫描 com.ssm.mapper，
 *    为每个接口生成代理对象注册进 Spring 容器（Bean 名 = 首字母小写接口名）
 * 2. SQL 写在 resources/ssm/mapper/StudentMapper.xml（namespace = 本接口全限定名）
 * 3. Service 层直接 @Autowired 注入使用，方法调用即 SQL 执行
 */
public interface StudentMapper {

    List<Student> selectAll();

    Student selectById(@Param("id") Integer id);

    int insert(Student student);

    int updateById(Student student);

    int deleteById(@Param("id") Integer id);
}
