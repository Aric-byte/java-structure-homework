package com.mybatis05.annotation;

import com.mybatis05.entity.Dept;
import com.mybatis05.entity.Emp;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DeptMapper {

    @Select("SELECT * FROM dept WHERE deptno = #{deptno}")
    Dept findById(Integer deptno);

    @Select("SELECT * FROM dept WHERE deptno = #{deptno}")
    @Results({
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "dname", column = "dname"),
            @Result(property = "loc", column = "loc"),
            @Result(property = "emps", column = "deptno",
                    many = @Many(select = "com.mybatis05.annotation.EmpMapper.selectListByDeptno"))
    })
    Dept one2manyByAnn(Integer deptno);
}