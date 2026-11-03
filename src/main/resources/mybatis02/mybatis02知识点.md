# MyBatis02 — 进阶知识点

> 本模块为 MyBatis 进阶教学内容：resultType/resultMap + 日志进阶 + Vo 类 + #{} ${} 参数绑定。

---

## 一、resultType 自动映射

### 基本规则

MyBatis 默认使用 `resultType` 把查询结果映射为 Java 对象。

```xml
<select id="findAll" resultType="com.mybatis02.entity.User">
    SELECT * FROM user
</select>
```

**匹配规则**：SQL 查询的**列名**和 Java 对象的**属性名**（getter/setter 对应的字段名）一致就自动映射。

| 数据库列 | Java 属性 | 能否匹配 |
|----------|-----------|----------|
| `id` | `id` | ✅ 直接匹配 |
| `username` | `username` | ✅ 直接匹配 |
| `create_time` | `createTime` | ✅ 开启驼峰映射后自动转换 |
| `uid` | `id` | ❌ 不一致，需要 AS 别名 |

### 开启驼峰映射

```xml
<!-- mybatis-config.xml -->
<settings>
    <setting name="mapUnderscoreToCamelCase" value="true"/>
</settings>
```

开启后：数据库 `create_time` → Java `createTime`，自动下划线转驼峰。

### resultType 的局限性

当数据库列名和 Java 属性名**不一致且无法通过驼峰转换解决**时（比如想把列名 `id` 映射到 `Vo.p1`），需要用 SQL AS 别名：

```xml
<select id="selectAllVo" resultType="Vo">
    SELECT id AS p1, username AS p2, password AS p3 FROM user
</select>
```

### resultType 的缺点

- SQL 中必须写 AS 别名来适配字段名不一致
- 每写一个不同的 VO 类，就要重写一次带 AS 的 SQL
- 无法处理嵌套对象（多表 JOIN 后结果拆到多个 Java 对象）
- **单表简单场景够用，复杂场景吃力**

---

## 二、resultMap 自定义映射（mybatis02 重点）

### 定义 resultMap

```xml
<resultMap id="userResultMap" type="com.mybatis02.entity.Vo">
    <!-- type: 映射到哪个 Java 类 -->
    <!-- id/result: 数据库列 与 Java 属性 的对应关系 -->
    <id     property="p1"    column="id"/>
    <result property="p2"    column="username"/>
    <result property="p3"    column="password"/>
</resultMap>
```

### 两种子元素

| 元素 | 作用 | 出现次数 |
|------|------|----------|
| `<id property="p1" column="id"/>` | 主键映射（告诉 MyBatis 这是主键） | **只能 1 次** |
| `<result property="p2" column="username"/>` | 普通字段映射 | **可多次** |

### 使用 resultMap

```xml
<!-- 使用 resultMap 的 id 引用，resultType 和 resultMap 互斥！ -->
<select id="selectAllByVo" resultMap="userResultMap">
    SELECT * FROM user
</select>
```

### resultMap vs resultType 对比

| 维度 | resultType | resultMap |
|------|-----------|-----------|
| 映射方式 | 自动匹配（列名==属性名） | 手工指定 column → property |
| 字段名不一致 | 用 SQL AS 别名适配 | 不需要，直接配 property/column |
| 复用性 | 同一个 SQL 换 VO 就要重写 AS | resultMap 可被多个 select 引用 |
| 多表嵌套 | ❌ 不支持 | ✅ 支持 `<association>` `<collection>` |
| 配置复杂度 | 零配置（自动） | 需要写 resultMap 块 |
| 适用场景 | 单表、字段名一致 | 字段名差异大、多表 JOIN、复杂 VO |
| 互斥性 | **不能**和 resultMap 同时写 | **不能**和 resultType 同时写 |

### 重点记忆

> **resultMap 和 resultType 互斥！** 同一个 `<select>` 上只能写其中一个。

```xml
<!-- ✅ 正确 -->
<select id="s1" resultType="User">...</select>
<select id="s2" resultMap="userResultMap">...</select>

<!-- ❌ 错误！两个同时写 -->
<select id="s3" resultType="User" resultMap="userResultMap">...</select>
```

---

## 三、Vo 类封装

Vo（Value Object）是**自定义的查询结果封装类**，可以和数据库表结构完全不同。

### 为什么需要 Vo

数据库表和 Java 实体类 `User` 通常是一一对应的，但有时候：

- 不同的业务场景需要不同的字段组合
- 多表 JOIN 后需要把结果放到一个新类里
- 不想让实体类暴露太多字段

### Vo 示例

```java
// com.mybatis02.entity.Vo —— 只有三个字段，p1/p2/p3 和 user 表字段名完全不同
public class Vo {
    private Integer p1;    // 映射 user.id
    private String p2;     // 映射 user.username
    private String p3;     // 映射 user.password
    // getter/setter/toString...
}
```

### 两种把查询结果映射到 Vo 的方式

#### 方式1：resultMap（推荐，灵活）

```xml
<resultMap id="userResultMap" type="Vo">
    <id     property="p1" column="id"/>
    <result property="p2" column="username"/>
    <result property="p3" column="password"/>
</resultMap>

<select id="selectAllByVo" resultMap="userResultMap">
    SELECT * FROM user
</select>
```

#### 方式2：resultType + SQL AS 别名（简单但死板）

```xml
<select id="selectAllVo" resultType="Vo">
    SELECT id AS p1, username AS p2, password AS p3 FROM user
</select>
```

两种方式都能把 `id` 列的值赋给 `Vo.p1`，`username` 列赋给 `Vo.p2`，但 resultMap 不需要改 SQL。

---

## 四、多参数传递

### 必须用 @Param 标注

当 Mapper 接口方法有**多个参数**时，必须用 `@Param("别名")` 标注每个参数。

```java
// 正确写法 ✅
User login(@Param("username") String username,
           @Param("password") String password);
```

```xml
<!-- XML 中用别名取值 -->
<select id="login" resultType="User">
    SELECT * FROM user WHERE username = #{username} AND password = #{password}
</select>
```

### 不写 @Param 的后果

如果不写 `@Param`，MyBatis 默认用参数位置命名：

```java
User login(String username, String password);
```

```xml
<!-- XML 只能用 param1、param2 或 arg0、arg1 -->
SELECT * FROM user WHERE username = #{param1} AND password = #{param2}
```

这种写法不直观、容易错，**强烈建议始终用 @Param**。

### 日志对多参数的输出

Logback 日志会清晰打印每个参数的值和类型：

```
==>  Preparing: SELECT * FROM user WHERE username = ? AND password = ?
==> Parameters: zhangsan(String), pass111(String)
<==      Total: 1
```

mybatis01 的单参数方法日志只打印一个值，mybatis02 的多参数 login 方法日志会打印全部参数。

---

## 五、日志进阶

### Log4j 1.x vs Logback

| 对比项 | Log4j 1.x（mybatis01） | Logback（mybatis02 推荐） |
|--------|----------------------|--------------------------|
| 维护状态 | **已停止维护** ⚠️ | 活跃维护 |
| 与 SLF4J 关系 | SLF4J → Log4j 有桥接层 | **Logback 本身就是 SLF4J 的实现** |
| 配置格式 | `.properties` 键值对 | `.xml` 标签，更灵活 |
| 性能 | 较低 | 更高 |
| 依赖 | `log4j:log4j:1.2.17` | `ch.qos.logback:logback-classic`（Spring Boot 已包含） |

### SLF4J —— 日志门面

SLF4J（Simple Logging Facade for Java）不是具体的日志实现，而是一个**统一的日志 API**。

```
业务代码
  │
  ▼
SLF4J API（统一门面，代码里只依赖它）
  │
  ├── Logback      （SLF4J 原生实现，推荐 ✅）
  ├── Log4j 1.x    （通过桥接层连接）
  └── Log4j 2.x    （通过桥接层连接）
```

**好处**：写代码时只调用 SLF4J API，换日志实现（Logback 换 Log4j2）时业务代码不用改，只改依赖和配置文件。

### Logback 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- ① Appender：输出到控制台 -->
    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- ② Logger：控制特定包的日志级别 -->
    <logger name="org.apache.ibatis" level="DEBUG"/>
    <logger name="java.sql" level="DEBUG"/>

    <!-- ③ Root：全局最低日志级别 -->
    <root level="INFO">
        <appender-ref ref="stdout"/>
    </root>

</configuration>
```

### MyBatis 中开启 Logback

```xml
<!-- mybatis-config.xml 的 settings 中 -->
<settings>
    <setting name="logImpl" value="SLF4J"/>
</settings>
```

`logImpl` 可选值：

| 值 | 对应框架 |
|----|----------|
| `SLF4J` | Logback / Log4j2（推荐 ✅） |
| `LOG4J` | Log4j 1.x |
| `LOG4J2` | Log4j 2.x |
| `STDOUT_LOGGING` | MyBatis 内置控制台输出（不用加依赖） |
| `NO_LOGGING` | 关闭日志 |

### DEBUG 级别日志输出详解

```
==>  Preparing: SELECT * FROM user WHERE username = ? AND password = ?
    ↑ SQL 预编译语句（? 占位符，MyBatis 实际传给 PreparedStatement）

==> Parameters: zhangsan(String), pass111(String)
    ↑ 每个参数的值 + Java 类型，多参数依次打印

<==      Total: 1
    ↑ 查询返回的记录数
```

增删改操作的日志：

```
==>  Preparing: INSERT INTO user(...) VALUES(...)
==> Parameters: xxx(String), xxx(String), xxx(String)
<==    Updates: 1
    ↑ 受影响行数
```

---

## 六、参数绑定：#{} vs ${}

### #{} —— 预编译参数占位符 ✅

```xml
<select id="findLikeSafe" resultType="User">
    SELECT * FROM user WHERE username LIKE CONCAT('%', #{keyword}, '%')
</select>
```

- **本质**：JDBC `PreparedStatement` 的 `?` 占位符
- MyBatis 实际执行：`... LIKE CONCAT('%', ?, '%')`，参数值安全绑定
- 自动做类型转换，**彻底防止 SQL 注入**
- **推荐**：绝大多数场景都用 `#{}`

### ${} —— 字符串直接拼接 ⚠️

```xml
<!-- 仅教学演示，生产环境禁用！ -->
<select id="findLikeUnsafe" resultType="User">
    SELECT * FROM user WHERE username LIKE '%${keyword}%'
</select>
```

- **本质**：把参数值**原样拼进 SQL 文本**（等价 JDBC `Statement`）
- **SQL 注入风险**：keyword 传入 `a' OR '1'='1` 时拼出
  `... LIKE '%a' OR '1'='1%'` → 条件恒真，**查出全表**（运行 findLikeUnsafe 可实测）
- 观察日志：`Preparing` 打印的是拼好的完整 SQL，**没有 `?` 占位符**

### ${} 的唯一合法场景：动态表名 / 列名

```xml
<!-- 列名位置不能用 #{}：ORDER BY 'id DESC' 会变成字符串字面量，排序失效 -->
<select id="findByOrderColumn" resultType="User">
    SELECT * FROM user ORDER BY ${orderCol} LIMIT #{limit}
</select>
```

- `?` 占位符只能出现在**值的位置**，表名/列名只能用 `${}` 拼接
- **必须**在代码层对列名做白名单校验，否则同样有注入风险

### 对比总结

| 维度 | #{} | ${} |
|------|-----|-----|
| 本质 | PreparedStatement 的 `?` | 字符串直接拼接 |
| SQL 注入 | ✅ 防止 | ❌ 有风险 |
| 预编译缓存 | ✅ 可复用执行计划 | ❌ 每次都是新 SQL |
| 动态表名/列名 | ❌ 不支持 | ✅ 唯一手段 |
| 使用建议 | **默认全用它** | 仅动态表名/列名 + 白名单校验 |

---

## 七、XML 方式 vs 注解方式在 mybatis02 的差异

| 功能 | XML 方式 | 注解方式 |
|------|----------|----------|
| resultType | ✅ | ✅ |
| SQL AS 别名 | ✅ | ✅ |
| **resultMap** | ✅ 支持 | ❌ **不支持** |
| **多表 JOIN 嵌套映射** | ✅ 支持 | ❌ **不支持** |
| 复杂动态 SQL | ✅ `<if> <where> <set> <foreach>` | ❌ 只有字符串拼接 |
| 单表简单 CRUD | ✅ | ✅ 更方便 |

**结论**：
- **简单单表 CRUD**：注解方式更简洁，一个接口文件搞定
- **复杂查询（resultMap、多表、动态 SQL）**：必须用 XML 方式

---

## 八、本目录文件说明

```
src/main/java/com/mybatis02/
├── xml/
│   ├── UserMapper.java              ← XML 方式接口（包含多参数 login、Vo 查询）
│   └── MybatisXmlDemo.java          ← 主入口 main()
└── annotation/
    ├── UserMapper.java              ← 注解方式接口（没有 resultMap 方法）
    └── MybatisAnnotationDemo.java   ← 主入口 main()

src/main/resources/mybatis02/
├── xml/
│   ├── mybatis-config.xml           ← logImpl=SLF4J
│   ├── logback.xml                  ← Logback 日志配置
│   └── mapper/
│       └── UserMapper.xml           ← 含 resultMap + 多参数 SQL
└── annotation/
    ├── mybatis-config.xml
    └── logback.xml
```

### 运行方式

```
com.mybatis02.xml.MybatisXmlDemo.main()          → XML 方式（含 resultMap 演示）
com.mybatis02.annotation.MybatisAnnotationDemo.main()  → 注解方式（无 resultMap）
```

### 演示步骤

XML 方式演示 12 个场景：
1. resultType 自动映射（findAll）
2. resultType + SQL AS 别名（selectAll）
3. resultMap 自定义映射到 Vo（selectAllByVo）
4. resultType + AS 别名映射到 Vo（selectAllVo）
5. 多参数 login（重点看日志输出）
6. 插入 + 主键回填
7. 按 ID 查询
8. 更新
9. 删除 + 清理
10. #{} + CONCAT 安全模糊查询（findLikeSafe）
11. ${} 注入演示（findLikeUnsafe，观察查出全表）
12. ${} 动态排序列（findByOrderColumn）