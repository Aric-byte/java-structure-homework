package com.mybatis05.xml;

import com.mybatis05.entity.Emp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmpMapper {

    Emp one2oneByXml(Integer empno);

    List<Emp> many2oneByXml();

    Emp many2manyByXml(Integer empno);
}