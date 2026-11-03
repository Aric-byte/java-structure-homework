# SSM 整合知识点 —— ssm 模块

> Spring + SpringMVC + MyBatis 三层整合：Controller（表现）→ Service（业务）→ Mapper（持久）→ DB
> 访问入口：`/ssm/student/list`（需 MySQL 可连接，表 `student` 与 mybatis06 共用）

---

## 一、整合思路

```
浏览器
  ↓ /ssm/*
DispatcherServlet（子容器：spring-mvc.xml → 只扫 Controller）
  ↓ 调用
StudentController ──注入──> StudentService（父容器：applicationContext.xml → Service + Mapper + 事务）
  ↓ @Transactional
StudentServiceImpl ──注入──> StudentMapper（MapperScannerConfigurer 生成的代理）
  ↓
StudentMapper.xml 的 SQL ──> MySQL（HikariCP 连接池）
```

**为什么需要 mybatis-spring**：原生 MyBatis 的 `SqlSessionFactoryBuilder` 手动构建工厂；
整合后 `SqlSessionFactoryBean` 把工厂交给 Spring 管理，`SqlSessionTemplate` 保证线程安全，
Mapper 接口直接生成代理 Bean —— 全程无 `SqlSession` 手动代码。

## 二、整合四件套（applicationContext.xml）

| # | Bean | 作用 |
|---|------|------|
| ① | `HikariDataSource` | 连接池（参数来自 `common/db.properties`） |
| ② | `SqlSessionFactoryBean` | MyBatis 工厂：加载 `ssm/mapper/*.xml`、别名包 `com.ssm.entity` |
| ③ | `MapperScannerConfigurer` | 扫描 `com.ssm.mapper` → 每个接口一个代理 Bean（**无需实现类**） |
| ④ | `DataSourceTransactionManager` + `<tx:annotation-driven/>` | 支撑 `@Transactional`（本质是接管 SqlSession 的 commit/rollback） |

## 三、父子容器（SSM 经典结构）

| | 父容器 | 子容器 |
|--|--------|--------|
| 加载者 | `ContextLoaderListener`（web.xml 注册） | `DispatcherServlet`（ssm，web.xml 注册） |
| 配置文件 | `ssm/applicationContext.xml` | `ssm/spring-mvc.xml` |
| 管辖 Bean | Service / Mapper / 事务 / 数据源 | Controller / 视图解析器 |
| 生命周期 | 全局唯一，随 Web 容器 | 随 Servlet |
| 可见性 | 看不到子容器 Bean | **能**看到父容器 Bean（注入 Service） |

- 分工原则：子容器扫 `com.ssm.controller`，父容器扫 `com.ssm.service`——
  两个容器扫同一个包会导致 Bean 重复注册
- 本项目原有的 `/annotation/*`、`/xml/*` 两个 Servlet 的容器也以父容器为根，互不干扰
- web.xml 中 ContextLoaderListener 和三个 DispatcherServlet 的注册方式详见 **springmvc-advanced知识点.md 第五章「web.xml 全局配置详解」**

## 四、三层代码对照

| 层 | 文件 | 要点 |
|----|------|------|
| 表现层 | `controller/StudentController` | 只做参数 ↔ Service 中转 + 视图跳转；`@Controller` |
| 业务层 | `service/StudentServiceImpl` | `@Service` + 构造器注入 Mapper + `@Transactional` |
| 持久层 | `mapper/StudentMapper` + `ssm/mapper/StudentMapper.xml` | 接口 + XML，namespace = 接口全限定名 |
| 实体 | `entity/Student` | 各层之间穿梭的数据载体 |

## 五、事务验证（@Transactional）

| 接口 | 现象 |
|------|------|
| `POST /ssm/student/add` | INSERT 生效，列表出现新数据（Flash 提示"新增成功"） |
| `GET /ssm/student/add-fail` | 日志可见 INSERT 执行 → Service 抛 RuntimeException → **回滚**，列表无此数据 |

- 回滚原理：异常穿过事务代理 → 代理捕获 → rollback；默认只回滚 `RuntimeException`（受检异常需 `rollbackFor`）
- 对比 `Spring/dao` 模块：那边是 XML 式 `tx:advice` 声明事务，本模块是注解式 `@Transactional`

## 六、易错点

| 易错点 | 说明 |
|--------|------|
| redirect 带前缀 | Servlet 映射 `/ssm/*` 时，`redirect:` 必须写 `redirect:/ssm/student/list`（上下文相对，非 Servlet 相对） |
| namespace | XML 的 namespace 必须 = 接口全限定名，statement id = 方法名 |
| 别名冲突 | `typeAliasesPackage` 只指向 `com.ssm.entity`（教训见 springdao 模块：扫父包导致同名类冲突） |
| 双容器重复扫描 | 子容器不要扫到 Service（否则事务等 AOP 可能失效） |

## 七、文件清单

| 文件 | 内容 |
|------|------|
| `com/ssm/entity/Student` | 实体（student 表） |
| `com/ssm/mapper/StudentMapper` | 持久层接口（CRUD） |
| `resources/ssm/mapper/StudentMapper.xml` | SQL 映射（resultMap + useGeneratedKeys） |
| `com/ssm/service/StudentService` / `Impl` | 业务层（@Transactional，含回滚演示） |
| `com/ssm/controller/StudentController` | 表现层（列表/详情/增删/回滚演示） |
| `resources/ssm/applicationContext.xml` | 父容器：整合四件套 |
| `resources/ssm/spring-mvc.xml` | 子容器：MVC 配置 |
| `web.xml` | ContextLoaderListener + ssm DispatcherServlet |
| `views/ssm/studentList.jsp` / `detail.jsp` | 列表页（含新增表单）/ 详情页 |