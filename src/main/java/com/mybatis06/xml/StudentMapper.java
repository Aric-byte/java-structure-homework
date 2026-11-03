package com.mybatis06.xml;

import com.mybatis06.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper {

    Student selectById(@Param("id") Integer id);

    List<Student> selectByMajor(@Param("major") String major);

    List<Student> selectAll();

    int insert(Student student);

    int updateById(Student student);

    int deleteById(@Param("id") Integer id);
}