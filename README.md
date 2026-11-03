# Spring + SpringMVC + MyBatis 学习项目（homework11-3）

按知识点拆分的 SSM 学习项目。从 Spring IoC/AOP、MyBatis 基础与多表查询、MyBatis-Plus，到 SpringMVC 和 SSM 三层整合，每个知识点都有可运行的代码、测试和笔记。另提供一个学生管理图形界面，用于演示与接口验证。

## 技术栈

| 组件 | 版本 |
|------|------|
| JDK | 17 |
| Spring / SpringMVC | 6.2.12 |
| MyBatis / mybatis-spring | 3.5.19 / 3.0.5 |
| MyBatis-Plus | 3.5.12 |
| 数据库 | MySQL 8.0（HikariCP 连接池） |
| Web 容器 | Tomcat 11（Jakarta Servlet 6） |
| 测试 | JUnit 5 |
| 构建 | Maven（自带 `mvnw`，无需另装） |

## 模块一览

| 模块 | 包 / 目录 | 内容 | 演示方式 |
|------|-----------|------|----------|
| Spring IoC | `com.Spring.Ioc` | XML 与注解两种方式装配 Bean、生命周期 | 测试 |
| Spring AOP | `com.Spring.Aop` | 切面日志：前置 / 后置 / 返回 / 异常 / 环绕五种通知 | 测试 |
| Spring DAO | `com.Spring.dao` | Spring 整合 MyBatis、声明式事务 | 测试 |
| MyBatis 01–03 | `com.mybatis01~03` | 增删改查、参数传递、resultMap、动态 SQL、批量插入 | 测试 |
| MyBatis 04 | `com.mybatis04` | **多表查询**：部门-员工 一对多 / 多对一 | 测试 |
| MyBatis 05 | `com.mybatis05` | **多表查询**：员工-技能 多对多 | 测试 |
| MyBatis 06 | `com.mybatis06` | 一级 / 二级缓存 | 测试 |
| MyBatis-Plus | `com.mybatis07` | 通用 CRUD、分页插件、自动填充、乐观锁、ActiveRecord | 测试 |
| SpringMVC | `com.springmvc` | XML 与注解两种方式、参数绑定、页面跳转、拦截器、文件上传下载、全局异常 | 网页 |
| SSM 整合 | `com.ssm` | Controller → Service（事务）→ Mapper 三层整合 | 网页 |

大部分模块同时提供 XML 和注解两个版本，便于对比。每个模块的要点整理在 `src/main/resources/<模块>/*知识点.md` 中。

## 快速开始

### 1. 准备数据库

```sql
CREATE DATABASE IF NOT EXISTS homework11_3 DEFAULT CHARSET utf8mb4;
```

然后在 `homework11_3` 库中依次执行 `sql/` 下的脚本：

| 脚本 | 建的表 | 使用模块 |
|------|--------|----------|
| `User_db.sql` | `user` | mybatis01–03、Spring DAO |
| `student.sql` | `student` | mybatis06、SSM、图形界面 |
| `dept_emp.sql` | `dept`、`emp`、`skill`、`emp_skill` | mybatis04–05 |
| `mp_student.sql` | `mp_student` | MyBatis-Plus |

### 2. 配置数据库连接

数据库密码不进版本库。复制示例文件并填写本机的账号密码：

```bash
cp src/main/resources/common/db.properties.example src/main/resources/common/db.properties
```

```properties
jdbc.url=jdbc:mysql://localhost:3306/homework11_3?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
jdbc.username=root
jdbc.password=你的密码
```

### 3. 运行测试

```bash
./mvnw test
```

共 93 个测试，覆盖 Spring 与 MyBatis 全部模块。

### 4. 启动 Web（Tomcat 11）

**IDEA：** 运行 → 编辑配置 → 添加「Tomcat 服务器 → 本地」，应用服务器选本机 Tomcat 11，JRE 选 17；部署页添加工件 `SpringSpringMVCMybatis:war exploded`，上下文路径填 `/`。

**命令行：**

```bash
./mvnw package -DskipTests
```

把 `target/SpringSpringMVCMybatis-1.0-SNAPSHOT.war` 复制为 Tomcat 的 `webapps/ROOT.war`，再运行 `bin/startup.bat`。

启动后访问 <http://localhost:8080/>。

> 本项目基于 `jakarta.servlet`，必须使用 **Tomcat 10.1 或 11**；Tomcat 9 及以下版本无法启动。

## 学生管理图形界面

首页 `/` 是一个单页应用（原生 HTML + JavaScript），调用 `/ssm/api/students` 下的 RESTful 接口。

**左侧：学生管理**

- 按姓名、专业模糊查询，支持分页
- 新增、编辑、单条删除、勾选批量删除
- 表单校验失败时，在表单下方显示后端返回的提示

**右侧：功能验证**

每个按钮发送一个非法或边界请求，日志区原样显示请求和响应：

| 场景 | 期望结果 |
|------|----------|
| 空姓名 / 超长姓名 / 成绩为负 | 400 + 具体提示 |
| 查询或删除不存在的 ID | 404 |
| 批量删除空数组 | 400 |
| 分页 page=0、size=0 | 400 |
| 请求体不是合法 JSON | 400 |
| 事务回滚演示 | 调用前后记录总数不变 |

### 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ssm/api/students?name=&major=&page=&size=` | 多条件模糊 + 分页 |
| GET | `/ssm/api/students/{id}` | 单条查询 |
| POST | `/ssm/api/students` | 新增 |
| PUT | `/ssm/api/students` | 修改 |
| DELETE | `/ssm/api/students/{id}` | 单条删除 |
| DELETE | `/ssm/api/students`（请求体 `[1,2,3]`） | 批量删除（单个事务） |
| POST | `/ssm/api/students/tx-demo` | 事务回滚演示 |

统一返回格式：

```json
{ "code": 200, "message": "success", "data": { } }
```

约定：`200` 成功、`400` 参数错误、`404` 资源不存在、`500` 系统错误（不向前端暴露堆栈）。

## 其他网页演示

首页右上角「其他模块演示导航」（`/demos.jsp`）列出了 SpringMVC 与 SSM 模块的全部页面：

| 功能 | 路径 |
|------|------|
| SpringMVC XML 方式 | `/xml/student/list` |
| 拦截器登录演示 | `/annotation/page/session`（先访问它完成「登录」） |
| REST 接口（受拦截器保护） | `/annotation/student/hello` |
| 页面跳转 + 携带数据 | `/annotation/page/modelAndView` |
| 参数绑定综合演示 | `/annotation/param/bind` |
| 文件上传下载（单文件 ≤ 10MB） | `/annotation/file/list` |
| 全局异常演示 | `/annotation/extra/arith/1/0` |
| SSM 学生管理（JSP 版） | `/ssm/student/list` |

## 目录结构

```
homework11-3
├── sql/                         数据库初始化脚本
├── src/main/java/com
│   ├── Spring/                  IoC、AOP、DAO
│   ├── mybatis01 ~ mybatis07/   MyBatis 与 MyBatis-Plus
│   ├── springmvc/               SpringMVC 各知识点
│   └── ssm/                     SSM 整合 + 图形界面接口
├── src/main/resources
│   ├── common/db.properties     数据库连接（本地文件，不提交）
│   └── <模块>/                  各模块配置、Mapper XML、知识点笔记
├── src/main/webapp
│   ├── index.html               学生管理图形界面
│   ├── demos.jsp                其他模块演示导航
│   └── WEB-INF/                 web.xml 与 JSP 视图
├── src/test/java                各模块测试
└── 运行环境.md                  环境搭建与常见问题
```

## 常见问题

| 现象 | 原因 / 解决 |
|------|-------------|
| 启动时报 `jakarta.servlet` 或 `javax.servlet` 相关类找不到 | Tomcat 版本不对，需要 10.1 或 11 |
| 接口报 `Name for argument ... not specified` | 未带 `-parameters` 编译。pom 已配置，在 IDEA 中重新加载 Maven 项目后重新构建 |
| 页面 500 / 表不存在 | 未执行 `sql/` 脚本，或库名与 `db.properties` 不一致 |
| 测试全部连不上数据库 | 没有创建 `db.properties`，见「配置数据库连接」 |
| `/annotation/student/*` 被重定向 | 拦截器的预期行为，先访问 `/annotation/page/session` |

更多细节见 [运行环境.md](运行环境.md)。
