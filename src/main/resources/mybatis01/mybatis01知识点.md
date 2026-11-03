# MyBatis01 — 基础入门知识点

> 本模块为 MyBatis 基础入门教学内容（XML 与注解两种方式）。

---

## 一、MyBatis 是什么

MyBatis 是一个**持久层框架**，核心功能是 **SQL 与 Java 对象之间的映射**。

- **不是**全自动的 ORM（如 Hibernate），而是**半自动**映射工具
- SQL 由开发者自己写，MyBatis 负责：参数绑定、结果集映射、事务管理
- 优点：SQL 可控、学习成本低、灵活度高
- 缺点：SQL 要自己写

---

## 二、MyBatis 使用的四步流程

```
┌─────────────────────────────────────────────────────────────────┐
│  第1步：加载主配置文件                                              │
│  InputStream is = Resources.getResourceAsStream("mybatis-config.xml"); │
├─────────────────────────────────────────────────────────────────┤
│  第2步：构建 SqlSessionFactory                                     │
│  SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is); │
│  ★ 重量级、线程安全、单例                                          │
├─────────────────────────────────────────────────────────────────┤
│  第3步：打开 SqlSession                                            │
│  SqlSession sqlSession = factory.openSession();                   │
│  ★ 轻量级、非线程安全、用完必须 close()                              │
│  ★ 默认手动提交事务，增删改后必须 commit()                           │
├─────────────────────────────────────────────────────────────────┤
│  第4步：获取 Mapper 代理对象                                        │
│  UserMapper mapper = sqlSession.getMapper(UserMapper.class);      │
│  ★ 接口本身无实现类，MyBatis 用 JDK 动态代理自动生成                 │
└─────────────────────────────────────────────────────────────────┘
```

### 四大核心对象

| 对象 | 作用 | 生命周期 | 线程安全 |
|------|------|----------|----------|
| `Configuration` | 全局配置对象，加载 mybatis-config.xml 生成 | 常驻 | ✅ |
| `SqlSessionFactory` | 生产 SqlSession 的工厂 | 常驻 | ✅ |
| `SqlSession` | 数据库会话（连接 + 事务） | 请求级别 | ❌ |
| `Mapper Proxy` | 真正执行 SQL 的代理对象 | 方法级别 | ❌ |

---

## 三、mybatis-config.xml 主配置文件

```xml
<configuration>

    <!-- ① 属性文件引入（数据库连接等公共配置） -->
    <properties resource="common/db.properties"/>

    <!-- ② 全局设置 -->
    <settings>
        <setting name="logImpl" value="LOG4J"/>           <!-- 日志实现 -->
        <setting name="mapUnderscoreToCamelCase" value="true"/>  <!-- 驼峰映射 -->
    </settings>

    <!-- ③ 类型别名（简化 XML 中的全限定类名） -->
    <typeAliases>
        <typeAlias type="com.mybatis01.entity.User" alias="User"/>
    </typeAliases>

    <!-- ④ 环境配置（数据源 + 事务管理器） -->
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${jdbc.driver}"/>
                <property name="url" value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
            </dataSource>
        </environment>
    </environments>

    <!-- ⑤ Mapper 注册（告诉 MyBatis 去哪里找 SQL 定义） -->
    <mappers>
        <!-- XML 方式：指定映射文件路径 -->
        <mapper resource="mybatis01/xml/mapper/UserMapper.xml"/>
        <!-- 注解方式：指定接口类 -->
        <mapper class="com.mybatis01.annotation.UserMapper"/>
    </mappers>

</configuration>
```

### 配置节点说明

| 节点 | 作用 |
|------|------|
| `<properties>` | 引入外部 .properties 文件，用 `${key}` 占位 |
| `<settings>` | 全局开关，如日志、驼峰映射 |
| `<typeAliases>` | 给全限定类名起别名，简化 XML 书写 |
| `<environments>` | 数据库环境配置（可配多套，default 选默认） |
| `<transactionManager type="JDBC"/>` | 使用 JDBC 原生事务 |
| `<dataSource type="POOLED"/>` | 使用 MyBatis 自带连接池 |
| `<mappers>` | 注册 Mapper，XML 用 resource，注解用 class |

### `<dataSource>` 内置类型

| type | 说明 |
|------|------|
| `UNPOOLED` | 不使用连接池，每次新建连接 |
| `POOLED` | MyBatis 自带连接池（mybatis01 使用） |

---

## 四、XML 方式实现 CRUD

### 4.1 定义 Mapper 接口

```java
public interface UserMapper {
    List<User> findAll();
    User findById(Integer id);
    List<User> findByUsernameLike(String value);
    int insert(User user);
}
```

### 4.2 编写 UserMapper.xml

```xml
<!-- namespace 必须等于接口的全限定类名 -->
<mapper namespace="com.mybatis01.xml.UserMapper">

    <!-- 查询所有 -->
    <!-- id 必须等于接口方法名 -->
    <!-- resultType 自动把查询结果映射为 User 对象 -->
    <select id="findAll" resultType="User">
        SELECT * FROM user
    </select>

    <!-- 根据 ID 查询 -->
    <select id="findById" resultType="User">
        SELECT * FROM user WHERE id = #{id}
    </select>

    <!-- 模糊查询 -->
    <select id="findByUsernameLike" resultType="User">
        SELECT * FROM user WHERE username LIKE '%${value}%'
    </select>

    <!-- 插入数据 -->
    <!-- parameterType 指定入参类型，可省略（MyBatis 能推断） -->
    <insert id="insert" parameterType="User">
        INSERT INTO user(username, password, email)
        VALUES(#{username}, #{password}, #{email})
    </insert>

</mapper>
```

### 4.3 XML 映射文件规则

| 规则 | 说明 |
|------|------|
| `namespace` | **必须**等于 Mapper 接口的全限定类名 |
| `id` | **必须**等于接口中对应的方法名 |
| `resultType` | 返回值类型，写别名或全限定类名 |
| `parameterType` | 入参类型，可省略 |
| `#{}` | 参数占位符，MyBatis 自动做预编译 |

---

## 五、注解方式实现 CRUD

### 5.1 定义带注解的 Mapper 接口

```java
public interface UserMapperAnnotation {

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    // 模糊查询
    @Select("SELECT * FROM user WHERE username LIKE '%${value}%'")
    List<User> findByUsernameLike(String value);

    // 插入 + 主键回填
    @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
```

### 5.2 MyBatis 配置中注册注解 Mapper

```xml
<mappers>
    <!-- 方式1：指定单个接口类 -->
    <mapper class="com.mybatis01.annotation.UserMapper"/>
    <!-- 方式2：包扫描（推荐，自动扫描包下所有带 @Select/@Insert 等注解的接口） -->
    <package name="com.mybatis01"/>
</mappers>
```

### 5.3 常用注解

| 注解 | 作用 | 等价于 XML |
|------|------|------------|
| `@Select("SQL")` | 查询 | `<select>` |
| `@Insert("SQL")` | 插入 | `<insert>` |
| `@Update("SQL")` | 更新 | `<update>` |
| `@Delete("SQL")` | 删除 | `<delete>` |
| `@Options(...)` | 附加选项 | XML 标签上的属性 |

### 5.4 XML 方式 vs 注解方式

| 维度 | XML 方式 | 注解方式 |
|------|----------|----------|
| SQL 位置 | 独立 XML 文件 | Java 接口注解里 |
| 修改 SQL | 改 XML，不用重新编译 | 改 Java，需要重新编译 |
| 简单程度 | 需要额外维护 XML 文件 | 一个接口文件搞定 |

---

## 六、参数绑定：#{} 占位符

- `#{}` 是 MyBatis 的**预编译参数占位符**（JDBC `PreparedStatement` 的 `?`），前文所有 CRUD 已在使用
- 值作为参数安全绑定，自动类型转换，**防止 SQL 注入**，绝大多数场景都用它
- `#{}` vs `${}` 的完整对比、SQL 注入演示、动态列名等进阶内容 → **见 mybatis02 知识点「参数绑定：#{} vs ${}」**

---

## 七、参数传递

### 单个参数

```java
User findById(Integer id);
```
```xml
<select id="findById" resultType="User">
    SELECT * FROM user WHERE id = #{id}
    <!-- #{id} 或 #{_parameter} 都可以 -->
</select>
```
> 单参数时，#{} 里写什么都能匹配上，MyBatis 不会去校验名字

### POJO 对象参数

```java
int insert(User user);
```
```xml
<insert id="insert" parameterType="User">
    INSERT INTO user(username, password)
    VALUES(#{username}, #{password})
    <!-- #{属性名}，自动取 User 对象的 getter -->
</insert>
```

---

## 八、主键回填

**插入数据后，拿到数据库自动生成的自增主键值。**

### XML 方式

```xml
<insert id="insert" parameterType="User"
        useGeneratedKeys="true"        <!-- 开启主键回填 -->
        keyProperty="id">              <!-- 回填到 Java 对象的 id 属性 -->
    INSERT INTO user(username, password, email)
    VALUES(#{username}, #{password}, #{email})
</insert>
```

### 注解方式

```java
@Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
@Options(useGeneratedKeys = true, keyProperty = "id")
int insert(User user);
```

### 使用效果

```java
User user = new User();
user.setUsername("zhangsan");
mapper.insert(user);
sqlSession.commit();
System.out.println(user.getId());   // 插入成功后，这里能拿到数据库生成的自增主键
```

### 使用条件

1. 数据库表 id 列必须是 `AUTO_INCREMENT`
2. 必须同时配置 `useGeneratedKeys="true"` 和 `keyProperty="id"`

---

## 九、事务管理

MyBatis 默认使用**手动提交事务**。

```java
SqlSession sqlSession = factory.openSession();   // 默认手动事务
try {
    mapper.insert(user);
    sqlSession.commit();    // ✅ 提交
} catch (Exception e) {
    sqlSession.rollback();  // ❌ 回滚
} finally {
    sqlSession.close();     // 🔒 必须关闭，释放数据库连接
}
```

| 方法 | 作用 |
|------|------|
| `factory.openSession()` | 打开手动事务会话（默认） |
| `factory.openSession(true)` | 打开自动提交会话（不推荐，无法回滚） |
| `sqlSession.commit()` | 提交事务 |
| `sqlSession.rollback()` | 回滚事务 |
| `sqlSession.close()` | 关闭会话、释放连接 |

> 💡 实际开发中事务交给 Spring 管理，自己写 commit/rollback 只出现在原生 MyBatis 学习阶段。

---

## 十、Log4j 日志配置

mybatis01 使用 Log4j 查看 MyBatis 执行的 SQL。

### 引入依赖（pom.xml）

```xml
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```

### 编写 log4j.properties（resources 下）

```properties
# 日志级别 DEBUG 可以看到 MyBatis 的 SQL 细节
log4j.rootLogger=DEBUG, stdout

# 控制台输出
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# MyBatis 相关的日志单独控制级别
log4j.logger.org.apache.ibatis=DEBUG
log4j.logger.java.sql=DEBUG
```

### mybatis-config.xml 中开启 Log4j

```xml
<settings>
    <setting name="logImpl" value="LOG4J"/>
</settings>
```

### 控制台输出效果（DEBUG 级别）

```
==>  Preparing: SELECT * FROM user WHERE id = ?           ← SQL 预编译语句（? 占位符）
==> Parameters: 1(Integer)                               ← 参数值 + 类型
<==      Total: 1                                        ← 返回条数
```

---

## 十一、完整使用示例

### XML 方式

```java
// 1. 加载主配置
InputStream is = Resources.getResourceAsStream("mybatis01/xml/mybatis-config.xml");

// 2. 构建工厂
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);

// 3. 打开会话
SqlSession sqlSession = factory.openSession();

// 4. 获取 Mapper 代理
UserMapper mapper = sqlSession.getMapper(UserMapper.class);

// 5. 执行查询
List<User> users = mapper.findAll();
for (User u : users) {
    System.out.println(u);
}

// 6. 执行插入
User user = new User();
user.setUsername("lisi");
user.setPassword("123456");
mapper.insert(user);
sqlSession.commit();   // 增删改必须手动提交！

// 7. 关闭会话
sqlSession.close();
```

### 注解方式

```java
// 和 XML 方式完全一样，区别只是 getMapper 拿到的是带注解的接口
InputStream is = Resources.getResourceAsStream("mybatis01/annotation/mybatis-config.xml");
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
SqlSession sqlSession = factory.openSession();

UserMapperAnnotation mapper = sqlSession.getMapper(UserMapperAnnotation.class);
List<User> users = mapper.findAll();

sqlSession.close();
```