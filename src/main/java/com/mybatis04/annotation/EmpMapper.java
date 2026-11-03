package com.mybatis04.annotation;

import com.mybatis04.entity.Emp;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface EmpMapper {

    @Select("SELECT * FROM emp")
    List<Emp> findAll();

    @Select("SELECT * FROM emp WHERE empno = #{empno}")
    Emp findById(Integer empno);

    @Select("INSERT INTO emp(ename, job, mgr, hiredate, sal, comm, deptno) " +
            "VALUES(#{ename}, #{job}, #{mgr}, #{hiredate}, #{sal}, #{comm}, #{deptno})")
    @Options(useGeneratedKeys = true, keyProperty = "empno")
    int addEmp(Emp emp);

    @Select("SELECT * FROM emp WHERE empno = #{empno}")
    @Results(id = "empWithDeptAnno", value = {
            @Result(property = "empno", column = "empno"),
            @Result(property = "ename", column = "ename"),
            @Result(property = "job", column = "job"),
            @Result(property = "mgr", column = "mgr"),
            @Result(property = "hiredate", column = "hiredate"),
            @Result(property = "sal", column = "sal"),
            @Result(property = "comm", column = "comm"),
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "dept", column = "deptno",
                    one = @One(select = "com.mybatis04.annotation.DeptMapper.findById"))
    })
    Emp getEmpWithDeptAnno(Integer empno);

    @Select("SELECT * FROM emp WHERE deptno = #{deptno}")
    List<Emp> findByDeptno(Integer deptno);
}