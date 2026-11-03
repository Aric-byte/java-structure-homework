package com.mybatis06.annotation;

import com.mybatis06.entity.Student;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@CacheNamespace
public interface StudentMapper {

    @Select("SELECT * FROM student WHERE id = #{id}")
    Student selectById(@Param("id") Integer id);

    @Select("SELECT * FROM student")
    List<Student> selectAll();
}