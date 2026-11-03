package com.mybatis05.annotation;

import com.mybatis05.entity.Emp;
import com.mybatis05.entity.Skill;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface EmpMapper {

    @Select("SELECT e.empno, e.ename, e.job, e.sal, e.deptno, " +
            "d.deptno AS d_deptno, d.dname, d.loc " +
            "FROM emp e LEFT JOIN dept d ON e.deptno = d.deptno " +
            "WHERE e.empno = #{empno}")
    @Results({
            @Result(property = "empno", column = "empno"),
            @Result(property = "ename", column = "ename"),
            @Result(property = "job", column = "job"),
            @Result(property = "sal", column = "sal"),
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "dept", column = "deptno",
                    one = @One(select = "com.mybatis05.annotation.DeptMapper.findById"))
    })
    Emp one2oneByAnn(Integer empno);

    @Select("SELECT e.empno, e.ename, e.job, e.sal, e.deptno, " +
            "d.deptno AS d_deptno, d.dname, d.loc " +
            "FROM emp e LEFT JOIN dept d ON e.deptno = d.deptno")
    @Results({
            @Result(property = "empno", column = "empno"),
            @Result(property = "ename", column = "ename"),
            @Result(property = "job", column = "job"),
            @Result(property = "sal", column = "sal"),
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "dept", column = "deptno",
                    one = @One(select = "com.mybatis05.annotation.DeptMapper.findById"))
    })
    List<Emp> many2oneByAnn();

    @Select("SELECT * FROM emp WHERE empno = #{empno}")
    @Results({
            @Result(property = "empno", column = "empno"),
            @Result(property = "ename", column = "ename"),
            @Result(property = "job", column = "job"),
            @Result(property = "sal", column = "sal"),
            @Result(property = "deptno", column = "deptno"),
            @Result(property = "skills", column = "empno",
                    many = @Many(select = "com.mybatis05.annotation.EmpMapper.selectSkillsByEmpno"))
    })
    Emp many2manyByAnn(Integer empno);

    @Select("SELECT s.id, s.name, s.description " +
            "FROM skill s " +
            "INNER JOIN emp_skill es ON s.id = es.skill_id " +
            "WHERE es.empno = #{empno}")
    List<Skill> selectSkillsByEmpno(@Param("empno") Integer empno);

    @Select("SELECT * FROM emp WHERE deptno = #{deptno}")
    List<Emp> selectListByDeptno(@Param("deptno") Integer deptno);
}