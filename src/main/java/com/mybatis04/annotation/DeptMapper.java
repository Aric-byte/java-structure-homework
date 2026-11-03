package com.mybatis04.annotation;

import com.mybatis04.entity.Dept;
import com.mybatis04.entity.Emp;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DeptMapper {

    @Select("SELECT * FROM dept")
    List<Dept> findAll();

    @Select("SELECT * FROM dept WHERE deptno = #{deptno}")
    Dept findById(Integer deptno);

    @Select("SELECT * FROM dept WHERE deptno = #{deptno}")
    @Results(id = "deptWithEmpsAnno", value = {
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "dname", column = "dname"),
            @Result(property = "loc", column = "loc"),
            @Result(property = "emps", column = "deptno",
                    many = @Many(select = "com.mybatis04.annotation.EmpMapper.findByDeptno"))
    })
    Dept getDeptWithEmpsAnno(Integer deptno);
}