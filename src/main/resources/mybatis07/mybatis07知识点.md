# mybatis07 知识点 —— MyBatis-Plus 快速开发


---

## 一、MyBatis-Plus 是什么

MyBatis-Plus = MyBatis 的增强工具包，**只做增强不做改变**，简化开发、提高效率。

| 原生 MyBatis | MyBatis-Plus |
|-------------|-------------|
| 手写每个 CRUD 方法 | BaseMapper 自动带 17 个 CRUD 方法 |
| 手写分页 SQL | PaginationInnerInterceptor 自动分页 |
| 手写 createTime/updateTime | MetaObjectHandler 自动填充 |
| 手写乐观锁 SQL | @Version 自动处理 |
| 逻辑删除手写 WHERE deleted=0 | @TableLogic 自动处理 |

---

## 二、BaseMapper 自动 CRUD（17 个方法）

```java
public interface StudentMapper extends BaseMapper<Student> {
    // 继承后自动拥有：insert / selectById / selectList / updateById / deleteById ...
    // 无需写任何方法！
}
```

### 完整方法列表

```
—— 插入 ——
  insert(T entity)                          → INSERT INTO ...

—— 删除 ——
  deleteById(Serializable id)                → DELETE WHERE id = ?
  delete(T entity)                          → 根据 entity 非 null 字段删除
  deleteBatchIds(Collection<?>)             → DELETE WHERE id IN (...)
  deleteByMap(Map<String, Object>)          → DELETE WHERE col=? AND col=?

—— 更新 ——
  updateById(T entity)                      → UPDATE SET ... WHERE id = ?
  update(T entity, Wrapper<T> wrapper)      → 根据条件构造器更新

—— 查询 ——
  selectById(Serializable id)               → SELECT * WHERE id = ?
  selectBatchIds(Collection<?>)              → SELECT * WHERE id IN (...)
  selectOne(Wrapper<T>)                     → 查询 1 条（多条会报错）
  selectCount(Wrapper<T>)                   → SELECT COUNT(*)
  selectList(Wrapper<T>)                   → 查询列表
  selectObjs(Wrapper<T>)                   → 只返回第一列值
  selectPage(IPage<T>, Wrapper<T>)         → 分页查询 ⭐
  selectMapsPage(IPage<T>, Wrapper<T>)     → 分页查询（返回 Map）
  selectMaps(Wrapper<T>)                   → 查询列表（返回 Map）
```

---

## 三、核心注解

### 3.1 @TableName —— 指定表名

```java
@TableName("mp_student")  // 类名 Student → 表名 mp_student
public class Student { ... }
```

不写的话默认：类名驼峰转下划线 → `student`

### 3.2 @TableId —— 指定主键

```java
@TableId(value = "id", type = IdType.AUTO)
private Long id;
```

| IdType | 说明 | 适用场景 |
|--------|------|---------|
| AUTO | 数据库自增（最常用） | MySQL 等 |
| ASSIGN_ID | 雪花算法（默认） | 分布式 ID |
| ASSIGN_UUID | UUID | 字符串主键 |
| INPUT | 手动赋值 | 代码生成主键 |
| NONE | 跟随全局配置 | —— |

### 3.3 @TableField —— 指定字段

```java
@TableField("name")                    // 列名不同时用 value 指定
private String name;

@TableField(updateStrategy = FieldStrategy.NOT_NULL)  // update 时忽略 null 字段
private Double score;

@TableField(exist = false)             // 该字段不在表中（计算字段）
private String fullName;
```

**FieldStrategy 更新策略**：

| 策略 | 说明 |
|------|------|
| NOT_NULL（推荐） | 字段为 null 时不参与更新（默认 IGNORED 会把 null 写回！） |
| NOT_EMPTY | 字段为空字符串时不参与 |
| NEVER | 永远不参与更新 |
| IGNORED | 不管什么值都参与（默认） |

### 3.4 @Version —— 乐观锁

```java
@Version
private Integer version;
```

**工作原理**：
```
读取时：SELECT * FROM student WHERE id = 1
       拿到 version = 1

更新时：UPDATE student SET score=99, version=2
       WHERE id = 1 AND version = 1   ← MP 自动加
       
如果别人已经改过了，version 变成 2，WHERE 不匹配 → rows=0 → 冲突
```

**必加插件**：`OptimisticLockerInnerInterceptor`

### 3.5 @TableLogic —— 逻辑删除

```java
@TableLogic
private Integer deleted;   // 0=未删除, 1=已删除
```

**效果**：
- `deleteById(1)` → 实际执行 `UPDATE SET deleted=1 WHERE id=1`
- `selectById(1)` → 自动加 `WHERE deleted=0`

**优点**：数据不真删，可恢复

---

## 四、自动填充 MetaObjectHandler

### 4.1 实体类标注

```java
@TableField(fill = FieldFill.INSERT)        // INSERT 时填充
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE) // INSERT + UPDATE 时都填充
private LocalDateTime updateTime;
```

### 4.2 实现 Handler

```java
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 4.3 注册到 GlobalConfig

```java
GlobalConfig globalConfig = new GlobalConfig();
globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
factoryBean.setGlobalConfig(globalConfig);
```

---

## 五、分页 PaginationInnerInterceptor

### 5.1 注册插件

```java
MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
factoryBean.setPlugins(interceptor);
```

### 5.2 使用

```java
Page<Student> page = new Page<>(1, 10);   // 第 1 页，每页 10 条
IPage<Student> result = mapper.selectPage(page, null);

System.out.println("总记录: " + result.getTotal());
System.out.println("总页数: " + result.getPages());
result.getRecords().forEach(s -> System.out.println(s));
```

**MP 自动改写 SQL**：
```
你写的：SELECT * FROM student WHERE ...
MP 改的：SELECT * FROM student WHERE ... LIMIT 0, 10
       同时发：SELECT COUNT(*) FROM student WHERE ...
```

---

## 六、Wrapper 条件构造器

```java
// QueryWrapper：查询条件
QueryWrapper<Student> wrapper = new QueryWrapper<>();
wrapper.like("name", "Zhang")       // WHERE name LIKE '%Zhang%'
       .gt("score", 60)             // AND score > 60
       .eq("deleted", 0)            // AND deleted = 0
       .orderByDesc("score");       // ORDER BY score DESC

List<Student> list = mapper.selectList(wrapper);

// LambdaQueryWrapper：类型安全（推荐）
LambdaQueryWrapper<Student> lw = new LambdaQueryWrapper<>();
lw.like(Student::getName, "Zhang")
   .gt(Student::getScore, 60);
List<Student> list2 = mapper.selectList(lw);
```

| 方法 | SQL |
|------|-----|
| `eq(col, val)` | col = val |
| `ne(col, val)` | col != val |
| `gt(col, val)` | col > val |
| `lt(col, val)` | col < val |
| `like(col, val)` | col LIKE '%val%' |
| `between(col, v1, v2)` | col BETWEEN v1 AND v2 |
| `in(col, list)` | col IN (...) |
| `isNull(col)` | col IS NULL |
| `orderByAsc(col)` | ORDER BY col ASC |

---

## 七、ActiveRecord 模式

```java
// 实体继承 Model<T>
public class StudentAR extends Model<StudentAR> {
    @TableId
    private Long id;
    private String name;
    // ...
}

// 直接用实体做 CRUD！
StudentAR s = new StudentAR("Test", 20, ...);
s.insert();              // INSERT
s.selectById(1L);        // SELECT
s.setName("NewName");
s.updateById();          // UPDATE
s.deleteById();          // DELETE
```

**两个条件**：
1. 实体 `extends Model<T>`
2. 对应 Mapper `extends BaseMapper<T>`

---

## 八、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | BaseMapper | 继承自动带 17 个 CRUD 方法 |
| 2 | @TableName / @TableId / @TableField | 表名、主键、字段映射 |
| 3 | @Version 乐观锁 | UPDATE 自动加 WHERE version=旧值 |
| 4 | @TableLogic 逻辑删除 | deleteById 变 UPDATE SET deleted=1 |
| 5 | MetaObjectHandler 自动填充 | createTime / updateTime 自动赋值 |
| 6 | PaginationInnerInterceptor | MP 自动改写 LIMIT SQL |
| 7 | QueryWrapper / LambdaQueryWrapper | 类型安全条件构造 |
| 8 | SimpleDataSource | MP 纯 Java 配置不需要 Spring |
| 9 | MybatisSqlSessionFactoryBean | MP 专用的 SqlSessionFactory 创建器 |

---

## 九、对比：原生 MyBatis vs MyBatis-Plus

| 对比项 | 原生 MyBatis | MyBatis-Plus |
|--------|-------------|-------------|
| CRUD 代码量 | 每个方法手写 | BaseMapper 自动提供 |
| 分页 | 手写 LIMIT SQL | PaginationInnerInterceptor |
| 审计字段 | 手动赋值 | MetaObjectHandler 自动填充 |
| 乐观锁 | 手写 version WHERE | @Version + OptimisticLockerInnerInterceptor |
| 逻辑删除 | 手写 WHERE deleted=0 | @TableLogic 自动处理 |
| 条件构造 | XML 动态 SQL 或硬拼字符串 | Wrapper / LambdaQueryWrapper |
| XML 写法 | 可选 | 可选，复杂 SQL 仍然写 XML |
| 学习成本 | 低 | 低（继承原生 API） |