# mybatis05 知识点 —— 一对一/一对多/多对多完整对比 + N+1 vs JOIN


---

## 一、三种关联关系全览

### 1.1 模型准备

```
Dept (部门)  1 ──── N  Emp (员工)    N ──── M  Skill (技能)
                                  通过 emp_skill 中间表关联
```

- **一对一**：Emp → Dept（多对一视角）或 Emp 扩展表
- **一对多**：Dept → List\<Emp\>
- **多对多**：Emp ↔ Skill（需要中间表 emp_skill）

---

## 二、一对一 / 多对一（Emp → Dept）

### 2.1 XML 方式 —— 一次 LEFT JOIN（推荐）

```xml
<resultMap id="empWithDeptMap" type="com.mybatis05.entity.Emp">
    <id property="empno" column="empno"/>
    <result property="deptno" column="emp_deptno"/>   <!-- ⚠️ AS 别名 -->
    <association property="dept" javaType="com.mybatis05.entity.Dept">
        <id property="deptno" column="deptno"/>       <!-- Dept 的主键，列名不同 -->
        <result property="dname" column="dname"/>
    </association>
</resultMap>

<select id="one2oneByXml" resultMap="empWithDeptMap">
    SELECT e.empno, e.deptno AS emp_deptno,    <!-- Emp.deptno -->
           d.deptno, d.dname                     <!-- Dept.deptno -->
    FROM emp e LEFT JOIN dept d ON e.deptno = d.deptno
    WHERE e.empno = #{empno}
</select>
```

**关键：同名列用 AS 别名区分**

### 2.2 注解方式 —— N+1 查询

```java
@Select("SELECT e.empno, e.deptno FROM emp e WHERE e.empno = #{empno}")
@Results({
    @Result(property = "dept", column = "deptno",
            one = @One(select = "com.mybatis05.annotation.DeptMapper.findById"))
})
Emp one2oneByAnn(Integer empno);
```

**SQL 次数：1 （Emp） + 1 （Dept） = 2 次**

---

## 三、一对多（Dept → List\<Emp\>）

### 3.1 XML 方式 —— 一次 LEFT JOIN

```xml
<resultMap id="deptWithEmpsMap" type="com.mybatis05.entity.Dept">
    <id property="deptno" column="deptno"/>
    <collection property="emps" ofType="com.mybatis05.entity.Emp">
        <id property="empno" column="empno"/>
        <result property="ename" column="ename"/>
        <result property="deptno" column="emp_deptno"/>
    </collection>
</resultMap>

<select id="one2manyByXml" resultMap="deptWithEmpsMap">
    SELECT d.deptno, e.empno, e.ename, e.deptno AS emp_deptno
    FROM dept d LEFT JOIN emp e ON d.deptno = e.deptno
    WHERE d.deptno = #{deptno}
</select>
```

**框架自动聚合**：相同 deptno 的多行 Emp → 一个 `List<Emp>`

### 3.2 注解方式 —— N+1 查询

```java
@Results({
    @Result(property = "emps", column = "deptno",
            many = @Many(select = "com.mybatis05.annotation.EmpMapper.selectListByDeptno"))
})
Dept one2manyByAnn(Integer deptno);
```

**SQL 次数：1 （Dept） + N （Emp，每个部门一次） = N+1 次**

---

## 四、多对多（Emp ↔ Skill）

### 4.1 数据库设计

```
emp (员工表) ── emp_skill (中间表) ── skill (技能表)
                   empno + skill_id
```

### 4.2 XML 方式 —— 两次 LEFT JOIN + DISTINCT

```xml
<resultMap id="empWithSkillsMap" type="com.mybatis05.entity.Emp">
    <id property="empno" column="empno"/>
    <collection property="skills" ofType="com.mybatis05.entity.Skill">
        <id property="id" column="skill_id"/>
        <result property="name" column="skill_name"/>
    </collection>
</resultMap>

<select id="many2manyByXml" resultMap="empWithSkillsMap">
    SELECT DISTINCT e.empno, s.id AS skill_id, s.name AS skill_name
    FROM emp e
    LEFT JOIN emp_skill es ON e.empno = es.empno
    LEFT JOIN skill s ON es.skill_id = s.id
    WHERE e.empno = #{empno}
</select>
```

- **`DISTINCT` 去重**：避免中间表导致的多行相同 Emp 数据
- 两次 LEFT JOIN（emp → emp_skill → skill）

### 4.3 注解方式 —— @Many

```java
@Results({
    @Result(property = "skills", column = "empno",
            many = @Many(select = "com.mybatis05.annotation.EmpMapper.selectSkillsByEmpno"))
})
Emp many2manyByAnn(Integer empno);

@Select("SELECT s.id, s.name FROM skill s " +
        "INNER JOIN emp_skill es ON s.id = es.skill_id WHERE es.empno = #{empno}")
List<Skill> selectSkillsByEmpno(@Param("empno") Integer empno);
```

---

## 五、N+1 问题详解

### 5.1 什么是 N+1

```
N = 主表查询的行数
1 = 额外的关联查询

查 1 个 Dept + 它的 20 个 Emp：
  JOIN 方式：1 条 SQL
  N+1 方式：1 条 Dept + 20 条 Emp = 21 条 SQL
```

### 5.2 日志中观察

```
JOIN 方式（XML association/collection 嵌套）：
  Preparing: SELECT d.*, e.* FROM dept d LEFT JOIN emp e ON ...
  Parameters: 20(Integer)
  
N+1 方式（@One/@Many 或 select 属性）：
  Preparing: SELECT * FROM dept WHERE deptno = ?        ← 1
  Parameters: 20(Integer)
  Preparing: SELECT * FROM emp WHERE deptno = ?         ← 2
  Parameters: 20(Integer)
  ... 每次访问 .getEmps() 或 .getDept() 都要发 SQL
```

### 5.3 什么时候用哪种

| 场景 | 推荐方式 | 原因 |
|------|---------|------|
| 批量查 + 必须用关联数据 | JOIN（XML 嵌套） | 性能碾压 N+1 |
| 单条查 + 关联数据可选 | N+1（延迟加载） | 代码简单，可能不触发子查询 |
| 纯注解项目 | @One/@Many | 没有 XML 可用 |

---

## 六、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | association vs collection | association=对象，collection=集合 |
| 2 | 一对一 XML | `<association>` 嵌套 + LEFT JOIN |
| 3 | 一对多 XML | `<collection>` 嵌套 + LEFT JOIN，`ofType` |
| 4 | 多对多 XML | 两次 LEFT JOIN + DISTINCT + 中间表 |
| 5 | 同名列 AS 别名 | `e.deptno AS emp_deptno` 避免映射冲突 |
| 6 | 注解 @One / @Many | `one=@One(select="...")` / `many=@Many(select="...")` |
| 7 | N+1 问题 | 1 条主查询 + N 条子查询，性能差但代码简单 |
| 8 | 延迟加载配合 N+1 | `lazyLoadingEnabled=true` 让 N+1 按需触发 |
| 9 | Skill 实体 + 中间表 | 多对多必须有中间表 |