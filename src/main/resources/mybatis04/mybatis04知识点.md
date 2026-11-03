# mybatis04 知识点 —— 多表查询 + association/collection + 延迟加载


---

## 一、多表查询分类

```
一对一     ——   一个员工属于一个部门     Emp → Dept
多对一     ——   多个员工属于同一个部门   Emp → Dept（同上，不同视角）
一对多     ——   一个部门有多个员工       Dept → List<Emp>
多对多     ——   一个员工有多个技能       Emp → List<Skill>（需要中间表，详见 mybatis05）
```

---

## 二、XML 方式：association（多对一 / 一对一）

### 2.1 一次 JOIN 查询（推荐）

```xml
<resultMap id="empWithDeptMap" type="com.mybatis04.entity.Emp">
    <id property="empno" column="empno"/>
    <result property="ename" column="ename"/>
    <!-- 多对一：association 嵌套对象映射 -->
    <association property="dept" javaType="com.mybatis04.entity.Dept">
        <id property="deptno" column="deptno"/>
        <result property="dname" column="dname"/>
        <result property="loc" column="loc"/>
    </association>
</resultMap>

<select id="getEmpWithDept" resultMap="empWithDeptMap">
    SELECT e.*, d.dname, d.loc, d.deptno
    FROM emp e LEFT JOIN dept d ON e.deptno = d.deptno
    WHERE e.empno = #{empno}
</select>
```

- **一次 LEFT JOIN** 查出所有数据，性能好
- `association` 里的列名要和外层 `Emp` 的列名区分（同名列用 AS 别名）
- 【分工】一次 JOIN 写法的三种关系完整实现见 **mybatis05**，本模块代码以 2.2 分步查询为主

### 2.2 嵌套 select（延迟加载）

```xml
<resultMap id="empWithDeptMap2" type="com.mybatis04.entity.Emp">
    <id property="empno" column="empno"/>
    <!-- 多对一：association 的 select 属性延迟加载 -->
    <association property="dept" javaType="com.mybatis04.entity.Dept"
                 select="com.mybatis04.xml.DeptMapper.findById"
                 column="deptno"/>
</resultMap>
```

- 先查 Emp，访问 `.getDept()` 时才查 Dept（**两次查询 = N+1 问题**）
- 优点：单表查询简单；缺点：每次访问关联对象都要发 SQL

---

## 三、XML 方式：collection（一对多）

### 3.1 一次 JOIN 查询

```xml
<resultMap id="deptWithEmpsMap" type="com.mybatis04.entity.Dept">
    <id property="deptno" column="deptno"/>
    <result property="dname" column="dname"/>
    <!-- 一对多：collection 嵌套集合映射 -->
    <collection property="emps" ofType="com.mybatis04.entity.Emp">
        <id property="empno" column="empno"/>
        <result property="ename" column="ename"/>
    </collection>
</resultMap>

<select id="getDeptWithEmps" resultMap="deptWithEmpsMap">
    SELECT d.deptno, d.dname, e.empno, e.ename
    FROM dept d LEFT JOIN emp e ON d.deptno = e.deptno
    WHERE d.deptno = #{deptno}
</select>
```

- `ofType` 指定集合中元素的类型（不是 `javaType`，因为是集合！）
- 框架自动把相同 deptno 的多行 Emp 聚合到一个 Dept.emps 里

### 3.2 嵌套 select（延迟加载）

```xml
<collection property="emps" ofType="com.mybatis04.entity.Emp"
            select="com.mybatis04.xml.EmpMapper.findByDeptno"
            column="deptno"/>
```

---

## 四、注解方式：@One 和 @Many（N+1 查询）

### 4.1 多对一 @One

```java
@Select("SELECT * FROM emp WHERE empno = #{empno}")
@Results(id = "empWithDeptAnno", value = {
    @Result(property = "empno", column = "empno"),
    @Result(property = "ename", column = "ename"),
    @Result(property = "dept", column = "deptno",
            one = @One(select = "com.mybatis04.annotation.DeptMapper.findById"))
})
Emp getEmpWithDeptAnno(Integer empno);
```

- `@One(select = "...")` 指定关联查询哪个 Mapper 的哪个方法
- **两次查询 = N+1**

### 4.2 一对多 @Many

```java
@Select("SELECT * FROM dept WHERE deptno = #{deptno}")
@Results({
    @Result(property = "deptno", column = "deptno"),
    @Result(property = "dname", column = "dname"),
    @Result(property = "emps", column = "deptno",
            many = @Many(select = "com.mybatis04.annotation.EmpMapper.findByDeptno"))
})
Dept getDeptWithEmpsAnno(Integer deptno);
```

---

## 五、延迟加载（Lazy Loading）

### 5.1 配置

```xml
<settings>
    <setting name="lazyLoadingEnabled" value="true"/>       <!-- 开启延迟加载 -->
    <setting name="aggressiveLazyLoading" value="false"/>   <!-- 不要激进加载 -->
</settings>
```

- `lazyLoadingEnabled=true`：访问关联属性时才发 SQL
- `aggressiveLazyLoading=false`：只加载被访问的属性（不是加载全部）

### 5.2 适用场景

| 场景 | 建议 |
|------|------|
| 知道一定会用到关联数据 | **一次 JOIN 查询**（association/collection 嵌套） |
| 不确定会不会用到关联数据 | **延迟加载**（select 属性） |

### 5.3 日志中观察

- 一次 JOIN：只看到 **1 条** SQL
- 延迟加载：先看到 Emp 的 SQL → 访问 `.getDept()` 时再看到 Dept 的 SQL

### 5.4 fetchType —— 语句级局部开关

全局的 `lazyLoadingEnabled` 也可以在**单个映射**上被覆盖：

```xml
<!-- 全局开启延迟加载，但这条关联强制立即加载 -->
<association property="dept" column="deptno" javaType="Dept"
             select="com.mybatis04.xml.DeptMapper.findById"
             fetchType="eager"/>

<!-- 反之：全局关闭延迟加载，这条关联单独开启 -->
<collection property="emps" ... fetchType="lazy"/>
```

| 取值 | 含义 | 适用 |
|------|------|------|
| `lazy`（默认跟随全局） | 访问属性时才发子查询 | 不确定会不会用到关联数据 |
| `eager` | 主查询后立即发子查询 | 知道一定会用的热点关联，省去"访问才触发"的额外往返 |

`@One`/`@Many` 注解同样支持：`@One(select = "...", fetchType = FetchType.EAGER)`。

---

## 六、多表查询对比总结

| 方式 | SQL 次数 | 性能 | 复杂度 | 适用场景 |
|------|---------|------|--------|---------|
| XML association（嵌套） | 1 次 JOIN | 高 | 中 | 关联数据一定会用 |
| XML association（select） | 2 次 | 低（N+1） | 低 | 可能不用关联数据 |
| 注解 @One/@Many | 2 次 | 低（N+1） | 低 | 简单场景、纯注解项目 |
| XML collection（嵌套） | 1 次 JOIN | 高 | 中 | 一对多必用 |
| XML collection（select） | 2 次 | 低（N+1） | 低 | 可能不用子集合 |

---

## 七、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | association | 嵌套对象（一对一/多对一），一次 JOIN vs 延迟加载 |
| 2 | collection | 嵌套集合（一对多），`ofType` 不是 `javaType` |
| 3 | 同名列 AS 别名 | `e.deptno AS emp_deptno` 避免歧义 |
| 4 | 注解 @One/@Many | N+1 查询，`column` 传外键给子查询 |
| 5 | 延迟加载 | `lazyLoadingEnabled=true` + `aggressiveLazyLoading=false` |
| 6 | 分步查询 vs JOIN | 本模块 XML 用嵌套 select + 延迟加载；一次 JOIN 的三种关系对比见 mybatis05 |
| 7 | 动态 SQL | 多表 SQL 中的动态条件属于 mybatis03 知识点，本模块不涉及 |