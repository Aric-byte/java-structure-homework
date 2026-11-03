package com.mybatis05.xml;

import com.mybatis05.entity.Dept;
import com.mybatis05.entity.Emp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DeptMapper {

    Dept one2manyByXml(Integer deptno);
}