# Spring + SpringMVC + MyBatis 学习项目（homework11-3）

按知识点拆分的 SSM 学习项目。从 Spring IoC/AOP、MyBatis 基础与多表查询、MyBatis-Plus，到 SpringMVC 和 SSM 三层整合，每个知识点都有可运行的代码、测试和笔记。另提供一个学生管理图形界面，用于演示与接口验证。

**作业二「资料上传下载系统」**（SSM 实训任务书 V2.0）在 `com.filesys` 模块中实现，见 [作业二](#作业二资料上传下载系统)。

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
| **作业二** | `com.filesys` | 资料上传下载系统：登录 + 拦截器、文件上传下载、元信息入库 | 网页 |

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
| `filesys.sql` | `sys_user`、`file_info`、`download_log` + 测试账号 | 作业二 |

### 2. 配置数据库连接

数据库密码不进版本库。复制示例文件并填写本机的账号密码：

```bash
cp src/main/resources/common/db.properties.example src/main/resources/common/db.properties
```

```properties
jdbc.url=jdbc:mysql://localhost:3306/homework11_3?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8
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

地址 `/students.html`（「其他模块演示导航」中有入口），是一个单页应用（原生 HTML + JavaScript），调用 `/ssm/api/students` 下的 RESTful 接口。

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

## 作业二：资料上传下载系统

按《SSM 实训任务书 V2.0》实现的「学员在线提交结业作业材料」系统：磁盘存文件、数据库存元信息，登录拦截 + 归属校验保证每个人只能看到和下载自己的文件。

**入口**：<http://localhost:8080/>（首页即作业二，未登录时自动转到登录页；直接访问 `/filesys/` 也可以）

**测试账号**（`sql/filesys.sql` 初始化，密码以 SHA-256 加盐存储）：

| 账号 | 密码 |
|------|------|
| `zhangsan` | `123456` |
| `lisi` | `123456` |

### 功能

| 编号 | 功能 | 实现要点 |
|------|------|----------|
| F1 | 登录与拦截器 | 查 `sys_user` 校验 SHA-256(盐 + 密码)；登录后用户名、ID 存 session；`LoginInterceptor` 用 `WebMvcConfigurer#addInterceptors` 注册，放行 `/login` 与 `/static/**` |
| F2 | 作业提交（上传） | 支持一次多个文件；服务端校验大小 ≤ 10MB 和扩展名白名单（pdf/jpg/png/doc/docx/zip）；落盘名 UUID + 原扩展名；按 `用户名/年/月` 分目录；元信息写入 `file_info` |
| F3 | 提交列表 | 只显示当前用户的记录；按 文档 / 图片 / 压缩包 分类筛选；无数据时友好提示 |
| F4 | 文件下载 | 只传 fileId，路径从数据库取；校验归属，他人文件提示「无权访问」；中文名用 `filename*=UTF-8''`；文件丢失时友好提示 |
| F5 | 下载日志（AOP） | `DownloadLogAspect` 在下载校验通过后写入 `download_log` |
| 追加 | 冻结、下载次数 | 上传者可冻结 / 解冻（冻结期间禁止下载）；每次成功下载 `download_count + 1` |
| 追加 | 删除（两级） | **逻辑删除**：`status = 0` 移入回收站，记录和磁盘文件保留，可恢复；**彻底删除**：只能在回收站操作，同时删除 `file_info` 记录和磁盘文件，不可恢复 |

### 设计说明

- **为什么原始文件名要存在数据库里**：落盘名由服务端用 UUID 生成（防重名覆盖、防路径穿越和特殊字符），磁盘上已看不出原名；列表展示和下载时的文件名只能从库里取回。
- **`file_info(uploader_id)` 索引**：列表按 `uploader_id` 查当前用户的文件，归属校验也依赖它，有索引时不必全表扫描。
- **所有安全校验都在服务端**：前端 `accept` 只是选择文件时的便利，类型、大小、归属都由 `FileService` 判断。
- **两级删除**：任务书要求逻辑删除（便于误删恢复、保留提交凭证）；彻底删除用于真正释放磁盘空间，限定在回收站内操作防止误删。彻底删除时先删记录再删文件，二者在同一事务中，磁盘文件删除失败会回滚，记录留在回收站可重试；`download_log` 历史保留作为审计。
- **上传路径来自配置文件**：`src/main/resources/filesys/filesys.properties` 的 `upload.root`，默认 `${user.home}/ssm-filesys-upload`。

### 代码位置

| 层 | 文件 |
|----|------|
| 配置 | `com.filesys.config.FileSysMvcConfig`（MVC + 拦截器注册）、`resources/filesys/spring-datasource.xml`（数据源 / MyBatis / 事务）、`filesys.properties` |
| 表现层 | `controller/LoginController`、`FileController`、`FileSysExceptionHandler` |
| 业务层 | `service/UserService`、`FileService`（上传、下载、冻结、删除的全部校验） |
| 持久层 | `mapper/*Mapper.java` + `resources/filesys/mapper/*.xml` |
| 其他 | `interceptor/LoginInterceptor`、`aspect/DownloadLogAspect`、`util/PasswordUtil` |
| 视图 | `webapp/WEB-INF/views/filesys/*.jsp`（JSP + JSTL）、`webapp/static/filesys/filesys.css` |

### 验收四条

1. 登录后上传 PDF：磁盘 `upload.root/zhangsan/年/月/` 下出现 UUID 命名的文件，`file_info` 新增一条记录，列表显示该文件。
2. 未登录访问 `/filesys/files` 被重定向到登录页。
3. 点击下载，文件正常下载，中文文件名不乱码。
4. 换 `lisi` 登录，看不到也下载不了 `zhangsan` 的文件。

## 其他网页演示

作业二页面右上角「其他模块演示」（`/demos.jsp`）列出了学生管理图形界面、SpringMVC 与 SSM 模块的全部页面：

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
│   ├── index.jsp                首页：跳转到作业二 /filesys/
│   ├── students.html            学生管理图形界面
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
