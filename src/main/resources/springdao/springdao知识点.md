# springdao 知识点 —— Spring 整合 MyBatis 与声明式事务

> 模块代码：`com.Spring.dao.xml`（XML 方式） / `com.Spring.dao.annotation`（注解方式）
> 配置文件：`src/main/resources/springdao/spring-dao.xml`、`springdao/mybatis-config.xml`、
> 　　　　　`springdao/annotation/mybatis-config.xml`、`common/db.properties`
> 可运行入口：`DaoXmlTest`（XML）、`DaoAnnotationTest`（注解）
> 依赖版本：Spring 4.3.30 / mybatis-spring 1.3.3 / MyBatis 3.5.15 / HikariCP 4.0.3 / mysql-connector-j 8.0.33


---

## 一、为什么要整合

### 1.1 原生 MyBatis 的模板代码

```java
InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
SqlSession session = factory.openSession();
try {
    UserMapper mapper = session.getMapper(UserMapper.class);
    mapper.insert(user);
    session.commit();          // 手动提交
} catch (Exception e) {
    session.rollback();        // 手动回滚
} finally {
    session.close();           // 手动关闭，忘了就连接泄漏
}
```

痛点：每个方法都要写这一套；`SqlSession` 要手动管；事务要手动 commit/rollback；DAO 无法直接 `@Autowired`。

### 1.2 整合后

```java
@Service("annUserService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;      // 直接用接口，SqlSession 由 Spring 管

    @Transactional(rollbackFor = Exception.class)
    public void registerUser(User user) {
        userMapper.insertAndGetId(user);
    }
}
```

**目标**：把 `SqlSessionFactory`、`SqlSession`、`Mapper` 代理、事务管理**全部交给 Spring 容器**，业务代码只剩一行调用。

---

## 二、整合"四件套"

| 职责 | Spring/MyBatis-Spring 提供的类 | XML 写法 | 注解写法 |
|------|-------------------------------|----------|----------|
| ① 数据源 | `HikariDataSource`（连接池） | `<bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">` | `@Bean public DataSource dataSource(Environment env)` |
| ② 会话工厂 | `org.mybatis.spring.SqlSessionFactoryBean` | `<bean id="sqlSessionFactory" class="...SqlSessionFactoryBean">` | `@Bean public SqlSessionFactoryBean sqlSessionFactory(DataSource)` |
| ③ Mapper 扫描 | `org.mybatis.spring.mapper.MapperScannerConfigurer` | `<bean class="...MapperScannerConfigurer">` | `@Bean public static MapperScannerConfigurer mapperScannerConfigurer()` |
| ④ 事务管理器 | `org.springframework.jdbc.datasource.DataSourceTransactionManager` | `<bean id="transactionManager">` + `<tx:advice>` + `<aop:config>` | `@Bean transactionManager` + `@EnableTransactionManagement` |

一句话记法：**数据源 → 会话工厂 → Mapper 扫描 → 事务管理器**，顺序不能乱（后面依赖前面）。

---

## 三、XML 整合配置逐项讲

```xml
<!-- ① 读取数据库配置 -->
<context:property-placeholder location="classpath:common/db.properties"/>

<!-- ② 数据源：HikariCP -->
<bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
    <property name="driverClassName" value="${jdbc.driver}"/>
    <property name="jdbcUrl"       value="${jdbc.url}"/>
    <property name="username"      value="${jdbc.username}"/>
    <property name="password"      value="${jdbc.password}"/>
    <property name="maximumPoolSize"   value="10"/>
    <property name="minimumIdle"       value="5"/>
    <property name="connectionTimeout" value="30000"/>
    <property name="idleTimeout"       value="600000"/>
    <property name="maxLifetime"       value="1800000"/>
</bean>

<!-- ③ 会话工厂：把 MyBatis 的配置接进来 -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configLocation" value="classpath:springdao/mybatis-config.xml"/>
    <property name="mapperLocations" value="classpath:springdao/**/*Mapper.xml"/>
    <property name="typeAliasesPackage" value="com.Spring.dao.xml"/>
</bean>

<!-- ④ Mapper 扫描：接口自动生成代理 Bean -->
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="com.Spring.dao.xml"/>
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
</bean>

<!-- ⑤ Service（手工装配 Mapper） -->
<bean id="userService" class="com.Spring.dao.xml.UserServiceImpl">
    <property name="userMapper" ref="userMapper"/>
</bean>

<!-- ⑥ 事务管理器 + 事务通知 + 织入 -->
<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource"/>
</bean>

<tx:advice id="txAdvice" transaction-manager="transactionManager">
    <tx:attributes>
        <tx:method name="find*"         read-only="true"/>
        <tx:method name="registerUser"  propagation="REQUIRED" rollback-for="Exception"/>
        <tx:method name="transferPoints" rollback-for="Exception" isolation="READ_COMMITTED"/>
        <tx:method name="batchInsertWithTransaction" rollback-for="Exception"/>
        <tx:method name="*"             propagation="REQUIRED"/>
    </tx:attributes>
</tx:advice>

<aop:config>
    <aop:pointcut id="servicePointcut"
                  expression="execution(* com.Spring.dao.xml.*Service*.*(..))"/>
    <aop:advisor advice-ref="txAdvice" pointcut-ref="servicePointcut"/>
</aop:config>
```

### 关键属性说明

| 配置项 | 作用 |
|--------|------|
| `SqlSessionFactoryBean.setDataSource` | 必需，数据源 |
| `setConfigLocation` | 加载 `mybatis-config.xml`（settings / typeAliases） |
| `setMapperLocations` | MyBatis 的 `*Mapper.xml` 位置（**注意通配范围**） |
| `setTypeAliasesPackage` | 扫描包注册别名（XML 里其实未用别名，仍建议只指向**具体子包**） |
| `MapperScannerConfigurer.basePackage` | 扫描哪个包下的接口生成 Mapper 代理 |
| `sqlSessionFactoryBeanName` | 指定用哪个 `SqlSessionFactory`（避免按类型注入时提前初始化） |
| `<tx:method name="find*" read-only="true">` | 查询方法标记只读（可让数据库/驱动做优化） |
| `rollback-for="Exception"` | **受检异常也要回滚**（默认只回滚 `RuntimeException`/`Error`） |
| `isolation="READ_COMMITTED"` | 隔离级别 |

### db.properties

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
jdbc.username=root
jdbc.password=
```

### mybatis-config.xml（整合后只剩可以保留的部分）

```xml
<configuration>
    <settings>
        <setting name="logImpl" value="LOG4J"/>
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>
    <typeAliases>
        <typeAlias alias="User" type="com.Spring.dao.xml.User"/>
    </typeAliases>
</configuration>
```

> 整合后 **environments / dataSource 必须删掉** —— 数据源统一由 Spring 提供，写上会冲突且无效。

---

## 四、全注解整合

```java
@Configuration
@ComponentScan(basePackages = "com.Spring.dao.annotation")
@PropertySource("classpath:common/db.properties")
@EnableTransactionManagement                        // 替代 <tx:advice> + <aop:config>
public class DaoAnnotationConfig {

    @Bean
    public DataSource dataSource(Environment env) {          // 用 Environment 读属性
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(env.getProperty("jdbc.driver"));
        ds.setJdbcUrl(env.getProperty("jdbc.url"));
        ds.setUsername(env.getProperty("jdbc.username"));
        ds.setPassword(env.getProperty("jdbc.password"));
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(5);
        return ds;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws IOException {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfigLocation(new ClassPathResource("springdao/annotation/mybatis-config.xml"));
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:springdao/annotation/**/*Mapper.xml"));
        factory.setTypeAliasesPackage("com.Spring.dao.annotation");
        return factory;
    }

    @Bean
    public static MapperScannerConfigurer mapperScannerConfigurer() {   // 必须 static！
        MapperScannerConfigurer scanner = new MapperScannerConfigurer();
        scanner.setBasePackage("com.Spring.dao.annotation");
        scanner.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return scanner;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```

### 与 XML 的对应关系

| XML | 注解 |
|-----|------|
| `<context:property-placeholder>` | `@PropertySource("classpath:common/db.properties")` |
| `<bean id="dataSource">` | `@Bean public DataSource dataSource(Environment env)` |
| `<bean id="sqlSessionFactory">` | `@Bean public SqlSessionFactoryBean sqlSessionFactory(DataSource)` |
| `<bean class="MapperScannerConfigurer">` | `@Bean public static MapperScannerConfigurer mapperScannerConfigurer()` |
| `<bean id="transactionManager">` | `@Bean public DataSourceTransactionManager transactionManager(DataSource)` |
| `<tx:advice>` + `<aop:config>` | `@EnableTransactionManagement` + 业务方法上的 `@Transactional` |

Service 侧：

```java
@Service("annUserService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public User findById(Integer id) { return userMapper.selectById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void registerUser(User user) { userMapper.insertAndGetId(user); }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void transferPoints(Integer fromUserId, Integer toUserId, int points) { ... }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertWithTransaction(User... users) { ... }
}
```

对比表：

| 场景 | XML 事务 | 注解事务 |
|------|----------|----------|
| 切哪些方法 | `execution(* com.Spring.dao.xml.*Service*.*(..))` | 标了 `@Transactional` 的方法 |
| 只读查询 | `<tx:method name="find*" read-only="true"/>` | `@Transactional(readOnly = true)` |
| 遇受检异常回滚 | `rollback-for="Exception"` | `rollbackFor = Exception.class` |
| 隔离级别 | `isolation="READ_COMMITTED"` | `isolation = Isolation.READ_COMMITTED` |
| 新增方法要不要配 | **不用**（按名字模式匹配，自动覆盖） | **要**（必须加注解，否则无事务） |

---

## 五、Mapper 接口代理是怎么来的

```
MapperScannerConfigurer（BeanDefinitionRegistryPostProcessor）
      ↓ 扫描 basePackage 下的所有接口
为每个接口注册一个 MapperFactoryBean（bean 名 = 接口简单名首字母小写）
      ↓
MapperFactoryBean.getObject() → sqlSession.getMapper(接口.class)
      ↓
得到 JDK 动态代理对象，方法调用最终落到 Mapper.xml / 注解里的 SQL
```

- `UserMapper` 接口 → Bean 名 **`userMapper`**（首字母小写）。
- 所以 XML 里 `ref="userMapper"` 能直接引用到它，注解里 `@Autowired private UserMapper userMapper;` 也能注入。
- **SqlSession 不再需要手动管理**：`SqlSessionTemplate` + `SpringManagedTransaction` 把 SqlSession 与 Spring 事务绑定，实测日志可见全过程：

```
DEBUG SqlSessionUtils:97  - Creating a new SqlSession
DEBUG SqlSessionUtils:128 - Registering transaction synchronization for SqlSession [...]
DEBUG SpringManagedTransaction:87 - JDBC Connection [HikariProxyConnection@... ] will be managed by Spring
DEBUG selectAll:135 - ==>  Preparing: SELECT * FROM user ORDER BY id DESC
DEBUG selectAll:135 - <==      Total: 25
DEBUG SqlSessionUtils:186 - Releasing transactional SqlSession [...]
DEBUG SqlSessionUtils:284 - Transaction synchronization committing SqlSession [...]
DEBUG SqlSessionUtils:310 - Transaction synchronization deregistering SqlSession [...]
DEBUG SqlSessionUtils:315 - Transaction synchronization closing SqlSession [...]
```

**`Creating a new SqlSession` → `Releasing` → `committing` → `closing` 全自动**，这就是整合后最大的收益。

---

## 六、声明式事务

### 6.1 原理

```
Service 方法调用
   ↓ 事务拦截器（AOP 代理）在方法外层切入
开启事务（connection.setAutoCommit(false)，连接绑定到 ThreadLocal）
   ↓
业务方法内的所有 Mapper 调用使用同一个连接
   ↓
正常返回 → commit()     抛出需要回滚的异常 → rollback()
   ↓
释放连接回池
```

**事务 = AOP 的应用**：`@EnableTransactionManagement` 注册 `InfrastructureAdvisorAutoProxyCreator`，它是 `BeanPostProcessor`，在 Bean 初始化阶段生成代理。所以事务会不会生效，遵守的正是 AOP 的规则（见 springaop 模块）。

### 6.2 `@Transactional` 常用属性

| 属性 | 取值 | 说明 |
|------|------|------|
| `propagation` | 7 种 | 传播行为，默认 `REQUIRED` |
| `isolation` | 5 种 | 隔离级别，默认 `DEFAULT`（跟数据库） |
| `readOnly` | true/false | 只读优化，默认 false |
| `rollbackFor` | `Exception.class` 等 | 指定哪些异常回滚，默认只 `RuntimeException`/`Error` |
| `noRollbackFor` | 异常类 | 指定哪些异常**不**回滚 |
| `timeout` | 秒 | 超时自动回滚，默认 -1（不限） |

### 6.3 七种传播行为

| 传播行为 | 含义 | 典型场景 |
|----------|------|----------|
| `REQUIRED`（默认） | 有事务就加入，没有就新建 | 绝大多数业务方法 |
| `SUPPORTS` | 有就加入，没有就以非事务方式执行 | 查询 |
| `MANDATORY` | 必须在事务中，否则抛异常 | 强制要求上层开事务 |
| `REQUIRES_NEW` | **总是新建**，外层事务挂起 | 记日志、独立流水 |
| `NOT_SUPPORTED` | 挂起外层事务，自己以非事务执行 | 非关键操作 |
| `NEVER` | 不能在事务中，否则抛异常 | 限制性场景 |
| `NESTED` | 嵌套事务（保存点），可单独回滚 | 部分失败不影响整体 |

### 6.4 五种隔离级别

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
|----------|------|-----------|------|
| `READ_UNCOMMITTED` | 会 | 会 | 会 |
| `READ_COMMITTED` | 不会 | 会 | 会 |
| `REPEATABLE_READ`（MySQL 默认） | 不会 | 不会 | 会（InnoDB 实际基本解决） |
| `SERIALIZABLE` | 不会 | 不会 | 不会 |
| `DEFAULT` | 跟数据库默认 | | |

---

## 七、事务回滚实测结论

`transferPoints(fromId, toId, points)` 内部先 UPDATE 转出账户，若 `points > 1000` 抛异常，再 UPDATE 转入账户。

### 7.1 正常执行（`points = 100`）

```
  转账前: from(password=from_pass)      to(password=to_pass)
==>  Preparing: UPDATE user SET password = ? WHERE id = ?
==> Parameters: from_pass_sub(String), 46(Integer)
<==    Updates: 1
==>  Preparing: UPDATE user SET password = ? WHERE id = ?
==> Parameters: to_pass_add(String), 47(Integer)
<==    Updates: 1
  转账后: from(password=from_pass_sub)  to(password=to_pass_add)
  ✅ 正常执行, 两处 UPDATE 都提交了
```

### 7.2 异常回滚（`points = 9999`）

```
  回滚前: from(password=from_pass_sub)  to(password=to_pass_add)
==>  Preparing: UPDATE user SET password = ? WHERE id = ?
==> Parameters: from_pass_sub_sub(String), 46(Integer)     ← 第 1 处 UPDATE 已经发出去了
<==    Updates: 1
  🚨 捕获异常: 转账金额过大触发异常 → 事务回滚!
  回滚后: from(password=from_pass_sub)  to(password=to_pass_add)   ← 第 1 处 UPDATE 被撤销
  ✅ 事务回滚生效! 两处 UPDATE 都撤销了
```

关键结论：
1. **第 1 条 UPDATE 确实执行成功了**（`Updates: 1`），但因为和后面的逻辑在**同一个事务**里，异常抛出后被整体回滚 —— 这正是"原子性"的直接证据。
2. 回滚后读到的仍是 `from_pass_sub`（上一次成功事务的结果），而不是 `from_pass_sub_sub`。

### 7.3 批量插入回滚

3 条 insert，最后一条用户名含 `bad` → 抛异常：

```
==>  Preparing: INSERT INTO user (...) VALUES (?, ?, ?)
==> Parameters: batch_good_1_6601(String), p1(String), bg1@t.com(String)
<==    Updates: 1
==>  Parameters: batch_good_2_6601(String), p2(String), bg2@t.com(String)
<==    Updates: 1
==>  Parameters: batch_bad_user_6601(String), p3(String), bbad@t.com(String)
<==    Updates: 1
  🚨 捕获异常: 第 3 条包含 'bad' → 全部回滚!
  查 batch_good_1: null
  查 batch_good_2: null
  查 batch_bad_user: null
  ✅ 全部不存在 = 整个事务回滚了
```

三条 insert 全部生效后又被整体撤销，再次印证"一个方法 = 一个事务"。

---

## 八、注意事项与踩坑

### 8.1 `typeAliasesPackage` 只能指向具体子包

`com.Spring.dao` 下同时存在 `xml` 和 `annotation` 两套**同名类**（`User`、`UserMapper`、`UserServiceImpl`…），如果扫父包：

```xml
<property name="typeAliasesPackage" value="com.Spring.dao"/>   <!-- ❌ -->
```

MyBatis 按**简单类名**注册别名（`User` → `com.Spring.dao.xml.User` 或 `annotation.User`），两个同名类会冲突并报错。所以源码里注释写得很明确：

```xml
<!-- 只能指向具体子包：com.Spring.dao 下 xml/annotation 两套同名类（User、UserServiceImpl 等），
     扫父包会导致 MyBatis 按简单类名注册别名时冲突 -->
<property name="typeAliasesPackage" value="com.Spring.dao.xml"/>
```

### 8.2 `MapperScannerConfigurer` 的 `@Bean` 必须是 `static`

```java
@Bean
public static MapperScannerConfigurer mapperScannerConfigurer() { ... }   // ✅ static
```

它是 `BeanDefinitionRegistryPostProcessor`，**必须比普通 Bean 更早实例化**。若做成实例方法，配置类会被提前实例化，其中的 `@Value`/`Environment`/占位符还没就绪，容易出"配置不生效"的诡异问题。

### 8.3 扫描器会把 Service 接口也当成 Mapper —— 实测告警

`basePackage = com.Spring.dao.xml` 时，该包下的 `UserService` 接口也被扫描，于是启动日志出现：

```
WARN ClassPathMapperScanner:239 - Skipping MapperFactoryBean with name 'userService' and
     'com.Spring.dao.xml.UserService' mapperInterface. Bean already defined with the same name!
```

Spring 发现 `userService` 这个名字已被 `<bean id="userService">` 占用，就跳过了 —— **没有报错，但说明扫描范围过宽**。规范做法是让 Mapper 接口单独放一个包（如 `…dao.mapper`），只扫那个包。

### 8.4 `mapperLocations` 通配过宽会加载到别的模块的 mapper

```xml
<property name="mapperLocations" value="classpath:springdao/**/*Mapper.xml"/>
```

实测启动日志：

```
DEBUG SqlSessionFactoryBean:529 - Parsed mapper file: '.../springdao/annotation/UserMapper.xml'
DEBUG SqlSessionFactoryBean:529 - Parsed mapper file: '.../springdao/xml/UserMapper.xml'
```

`**` 把 `annotation` 子包的 mapper 也加载进来了。虽然两个 XML 各自 namespace 不同、不会直接冲突，但**加载了不需要的东西**，性能与维护性都变差。XML 版应改为：

```xml
<property name="mapperLocations" value="classpath:springdao/xml/*Mapper.xml"/>
```

### 8.5 整合后 `mybatis-config.xml` 必须删掉数据源配置

保留 `<environments>` 是无意义的（Spring 的 `SqlSessionFactoryBean` 用 `dataSource` 属性），而且容易让人误以为改那里能生效。

### 8.6 HikariCP 4.x 的属性名是 `jdbcUrl`

不是 `url`；`driverClassName`、`username`、`password`、`maximumPoolSize` 才是标准名。启动时 Hikari 会 dump 一整屏配置（实测日志里 40 多行），属于正常现象。

### 8.7 受检异常默认不回滚

```java
@Transactional                                                    // ❌ 抛 IOException 不回滚
@Transactional(rollbackFor = Exception.class)                      // ✅ 全异常回滚
```

本项目所有写操作方法都显式写了 `rollbackFor = Exception.class`（XML 版对应 `rollback-for="Exception"`）。

### 8.8 事务不生效的排查清单

| 现象 | 原因 |
|------|------|
| 完全没事务 | 没加 `@EnableTransactionManagement` / 没配 `transactionManager` |
| 加了注解也没事务 | 方法不是 `public`；或类内部 `this.xxx()` 自调用（不走代理） |
| 抛异常没回滚 | 抛的是**受检异常**且没写 `rollbackFor`；或异常被自己 `catch` 掉了 |
| 注解方式新增方法必须先加注解 | XML 的 `tx:advice` 按名字模式匹配，新方法名不匹配就没事务 |

### 8.9 `LocalDateTime` 映射

需要 `mysql-connector-j 8.x`（本项目是 8.0.33）并配 `serverTimezone=UTC`；`mybatis-config.xml` 里的 `mapUnderscoreToCamelCase=true` 负责 `create_time → createTime` 的自动映射（本项目又用 `resultMap` 显式映射了一遍，双保险）。

### 8.10 两套同名类的取舍

`xml` 与 `annotation` 两套代码解决同一个问题，方便对照学习，但**不要在一个 Spring 容器里同时加载两套配置**（Mapper 接口、TypeAlias 会重名），本项目也是分开跑两个测试类。

---

## 九、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | 整合目标 | 消灭 SqlSession 模板代码，交给 Spring 管理 |
| 2 | 四件套 | DataSource → SqlSessionFactoryBean → MapperScannerConfigurer → DataSourceTransactionManager |
| 3 | Mapper 代理 | 扫描接口 → MapperFactoryBean → JDK 代理，Bean 名首字母小写 |
| 4 | SqlSession 托管 | `Creating → Releasing → committing → closing` 全自动 |
| 5 | 声明式事务 | `@Transactional` / `<tx:advice>`，本质是 AOP |
| 6 | 传播行为 | REQUIRED（默认）/ REQUIRES_NEW / NESTED 最常用 |
| 7 | 回滚规则 | 默认只回滚运行时异常，受检异常要 `rollbackFor` |
| 8 | 实测结论 | 同一方法内多次 DML 同事务；异常后**已执行的 UPDATE 也被撤销** |
| 9 | 高频坑 | typeAliasesPackage 只能到子包、MapperScannerConfigurer 必须 static、扫描范围过宽 |
