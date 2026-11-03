# SpringMVC 进阶 —— 拦截器 · 文件上传下载 · 异常处理 · web.xml

> 这是 SpringMVC 的**进阶 + 框架配置**部分，聚焦"让 Controller 跑稳、扩展能力"。
> 建议先读基础部分（`springmvc知识点.md`），再读本文件和 SSM 整合（`ssm知识点.md`）。

---

## 一、拦截器 —— `com.springmvc.interceptor`

### 1.1 三个方法与执行时机

| 方法 | 时机 | 返回值/作用 |
|------|------|------------|
| `preHandle` | Handler **执行前** | `true` 放行 / `false` 中断（后续拦截器和 Handler 都不执行） |
| `postHandle` | Handler 后、**视图渲染前** | 可修改 ModelAndView（改数据最后机会） |
| `afterCompletion` | 渲染**完成后** | 无论有无异常都执行（类似 finally），适合清理/记日志 |

**多拦截器顺序（洋葱模型）**：
`pre1 → pre2 → Handler → post2 → post1 → 渲染 → after2 → after1`
（postHandle/afterCompletion 与 preHandle 顺序**相反**）

### 1.2 注册（`springmvc-servlet.xml`）

```xml
<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/student/**"/>          <!-- Ant 风格路径 -->
        <mvc:exclude-mapping path="/student/open"/><!-- 排除路径 -->
        <bean class="com.springmvc.interceptor.LoginInterceptor"/>
    </mvc:interceptor>
</mvc:interceptors>
```

- 多个拦截器**按声明顺序**执行
- `MappedInterceptor` 对 XML 方式的 `BeanNameUrlHandlerMapping` 同样生效

### 1.3 本模块的两个实现

| 类 | 演示点 |
|----|--------|
| `TimeInterceptor` | `preHandle` 存开始时间 → `afterCompletion` 算耗时（三个方法完整走一遍） |
| `LoginInterceptor` | 未登录 `preHandle` 返回 `false` + `sendRedirect` 拦截；登录后放行（与 `/page/session` 联动） |

### 1.4 拦截器 vs 过滤器

| | 拦截器 | 过滤器 |
|--|--------|--------|
| 规范 | SpringMVC 机制 | Servlet 规范 |
| 执行位置 | DispatcherServlet 之后 | 最外层，所有请求（含静态资源） |
| 能拿到 | Handler 信息、Spring Bean | 仅 ServletRequest/Response |

---

## 二、文件上传下载 —— `FileController`（`/annotation/file/...`）

### 2.1 上传四件套

```xml
<!-- ① 配置：Bean 名必须叫 multipartResolver（DispatcherServlet 按名查找） -->
<!--    Spring 6 已移除 CommonsMultipartResolver，改用容器自带解析 -->
<bean id="multipartResolver" class="org.springframework.web.multipart.support.StandardServletMultipartResolver"/>
<!--    大小限制写在 web.xml 对应 servlet 的 <multipart-config><max-file-size>10485760</max-file-size> 中；
        中文文件名由 CharacterEncodingFilter 统一 UTF-8 -->
```

```jsp
<!-- ② 表单：post + multipart/form-data 缺一不可 -->
<form method="post" enctype="multipart/form-data">
    <input type="file" name="file">
```

```java
// ③ 控制器：MultipartFile 接收（依赖 commons-fileupload）
// ④ 保存：file.transferTo(...) 落盘；时间戳前缀防同名覆盖
```

`MultipartFile` 常用 API：`getOriginalFilename()` / `getSize()` / `isEmpty()` / `transferTo()`

### 2.2 下载要点

| 要点 | 代码 |
|------|------|
| 触发"另存为" | `Content-Disposition: attachment; filename*=UTF-8''名字` |
| 中文文件名 | `URLEncoder.encode(name, UTF-8)` + 空格换 `%20` |
| 响应方式 | `ResponseEntity<byte[]>`（响应头 + 响应体一起返回） |
| **安全：防路径穿越** | `resolve(name).normalize()` 后校验 `startsWith(STORAGE)`，拒绝 `../` |

---

## 三、中文乱码与静态资源

### 3.1 POST 中文乱码 —— `CharacterEncodingFilter`（web.xml）

```xml
<filter>
    <filter-name>encodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name><param-value>UTF-8</param-value>
    </init-param>
    <init-param>
        <param-name>forceEncoding</param-name><param-value>true</param-value>
    </init-param>
</filter>
```

| 乱码来源 | 解决 |
|---------|------|
| GET 参数 | Tomcat `server.xml` 的 `URIEncoding="UTF-8"` |
| POST 请求体 | 本过滤器（`forceEncoding=true` 连响应也强制） |
| 上传文件名 | `CharacterEncodingFilter`（web.xml）统一 UTF-8 |
| JSP 页面 | `<%@ page pageEncoding="UTF-8" %>` |

### 3.2 静态资源 —— `<mvc:resources>`

```xml
<mvc:resources mapping="/static/**" location="/static/"/>
```

- DispatcherServlet 映射 `/` 时会拦截 css/js/图片请求 → 配此标签放行
- 本模块映射的是 `/annotation/*`、`/xml/*`（不挡静态资源），但原理必须掌握

---

## 四、全局异常处理 —— `com.springmvc.exception.GlobalExceptionHandler`

| 注解 | 作用 |
|------|------|
| `@ControllerAdvice` | 全局增强，对**所有 Controller** 生效（需被组件扫描到） |
| `@ExceptionHandler(XxxException.class)` | 按异常类型精确匹配，**越具体的异常优先**；Controller 内也可写（仅本类生效） |

本模块实现（演示接口统一在 `AdvanceController`，见 `/annotation/extra/...`）：

| Handler | 演示 |
|---------|------|
| `ArithmeticException` | 访问 `/annotation/extra/arith/10/0` → 友好错误页（不再是 500 白页） |
| `NullPointerException` | 访问 `/annotation/extra/null` |
| `MaxUploadSizeExceededException` | 上传超 10MB → 友好提示 |
| `Exception` 兜底 | 其余所有异常 |

```java
@Controller
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ArithmeticException.class)
    public ModelAndView arithmeticException(ArithmeticException ex) {
        return errorPage("算术异常", ex);   // → /WEB-INF/views/error/error.jsp
    }
}
```

---

## 五、web.xml 全局配置详解

> web.xml 是传统 WAR 项目的**启动总线**（本项目已移除 Cargo 内嵌 Tomcat，web.xml 是部署到外部 Tomcat 时的唯一启动入口）。
> 本项目三大核心组件全部在这里注册，是理解 SSM 父子容器、多模块路由的**总开关**。

### 5.1 启动全景流程

```
Tomcat 启动，加载 web.xml（按声明顺序）
  ├─ ① context-param + listener → ContextLoaderListener 启动父容器
  │   加载 classpath:ssm/applicationContext.xml（Service/Mapper/事务）
  ├─ ② filter + filter-mapping → CharacterEncodingFilter 注册
  │   拦截 /annotation/*、/ssm/*，强制 UTF-8 编解码
  └─ ③ servlet + servlet-mapping → 三个 DispatcherServlet 依次初始化（load-on-startup=1）
      ├─ springmvc-annotation → 子容器：classpath:springmvc/annotation/springmvc-servlet.xml
      │   扫 com.springmvc.controller、/annotation/* 路由
      ├─ springmvc-xml → 子容器：classpath:springmvc/xml/springmvc-servlet.xml
      │   XML 方式手动注册、/xml/* 路由
      └─ ssm → 子容器：classpath:ssm/spring-mvc.xml
          扫 com.ssm.controller、/ssm/* 路由（父容器已就绪，能注入 Service）
```

### 5.2 核心组件逐个解析

#### ① 父容器监听器（必须在最前面）

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:ssm/applicationContext.xml</param-value>
</context-param>
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```

| 要点 | 说明 |
|---|---|
| 作用 | **提前启动父容器**（全局唯一），后续三个 DispatcherServlet 的子容器都以它为父容器 |
| 父子关系 | 子容器能看到父容器 Bean（注入 Service），父容器看不到子容器 Bean（Service 不能注入 Controller） |
| 为什么放最前 | Tomcat 按 `context-param → listener → filter → servlet` 顺序加载，父容器必须先启动 |
| 缺了会怎样 | ssm 模块的 Controller 注入不到 StudentService，启动时抛 `NoSuchBeanDefinitionException` |

#### ② 编码过滤器（CharacterEncodingFilter）

```xml
<filter>
    <filter-name>encodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
    <init-param>
        <param-name>forceEncoding</param-name>
        <param-value>true</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>encodingFilter</filter-name>
    <url-pattern>/annotation/*</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>encodingFilter</filter-name>
    <url-pattern>/ssm/*</url-pattern>
</filter-mapping>
```

| 参数 | 作用 |
|---|---|
| `encoding` | 强制请求体用 UTF-8 解码（解决 POST 中文参数乱码） |
| `forceEncoding=true` | 连响应也强制 UTF-8（双保险：即使 StringHttpMessageConverter 没配 UTF-8 也能正常返回中文） |

**编码分工边界**：

| 场景 | 谁管 | 位置 |
|---|---|---|
| GET 查询参数 | Tomcat | `server.xml` 的 `Connector URIEncoding="UTF-8"` |
| POST 请求体 | 本过滤器 | web.xml |
| @ResponseBody 返回中文 | StringHttpMessageConverter + 本过滤器双保险 | `springmvc-servlet.xml` + web.xml |
| 上传文件中文文件名 | CommonsMultipartResolver | `springmvc-servlet.xml` 的 `defaultEncoding=UTF-8` |
| JSP 页面编码 | page 指令 | JSP 头部 `<%@ page pageEncoding="UTF-8" %>` |

#### ③ 三个 DispatcherServlet（核心路由）

| servlet-name | url-pattern | 配置文件 | 扫描方式 | 对应模块 | 父子容器 |
|---|---|---|---|---|---|
| `springmvc-annotation` | `/annotation/*` | `classpath:springmvc/annotation/springmvc-servlet.xml` | `<context:component-scan base-package="com.springmvc.controller"/>` | 注解方式（RESTful） | 父容器 applicationContext.xml |
| `springmvc-xml` | `/xml/*` | `classpath:springmvc/xml/springmvc-servlet.xml` | 无扫描（XML 手动注册 `<bean name="/xxx"/>`） | XML 方式（Controller 接口） | 父容器 applicationContext.xml |
| `ssm` | `/ssm/*` | `classpath:ssm/spring-mvc.xml` | `<context:component-scan base-package="com.ssm.controller"/>` | SSM 三层整合 | 父容器 applicationContext.xml |

以 ssm Servlet 为例（最能体现父子容器关联）：

```xml
<servlet>
    <servlet-name>ssm</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:ssm/spring-mvc.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>ssm</servlet-name>
    <url-pattern>/ssm/*</url-pattern>
</servlet-mapping>
```

| 要点 | 说明 |
|---|---|
| `DispatcherServlet` | SpringMVC 前端控制器，所有请求的统一入口 |
| `contextConfigLocation` | **Servlet 专属**的 Spring 配置文件（不是全局），每个 Servlet 有自己的子容器 |
| `load-on-startup=1` | 启动时就初始化（而非第一个请求来才初始化），数字越小优先级越高 |
| 父子容器关联 | DispatcherServlet 自动查找 ContextLoaderListener 启动的父容器，子容器能注入父容器 Bean |

### 5.3 Tomcat 加载 web.xml 的顺序规则

```
① <context-param> + <listener>     ← 全局配置 + 父容器（最先启动）
② <filter> + <filter-mapping>       ← 过滤器链
③ <servlet> + <servlet-mapping>     ← Servlet 容器（按 load-on-startup 初始化）
```

**为什么顺序不能乱？**
- 父容器必须在 Servlet 之前启动 → 子容器才能注入 Service
- Filter 必须在 Servlet 之前注册 → Tomcat 不会出现"Servlet 已初始化但过滤器未就绪"的中间状态

### 5.4 本项目「多 Servlet 设计」原因

| 原因 | 说明 |
|---|---|
| 教学隔离 | 分别演示注解方式、XML 方式、SSM 整合三种 SpringMVC 用法，互不干扰 |
| 配置隔离 | 每个 Servlet 有独立的 HandlerMapping、ViewResolver、拦截器配置 |
| 路由清晰 | 不同模块用不同 url-pattern，不会混淆路由匹配 |

### 5.5 常见坑汇总

| 现象 | 原因 | 解决 |
|---|---|---|
| 启动报 `BeanNotOfRequiredTypeException` 或注入不到 Service | 没配 ContextLoaderListener，父容器没启动 | 补上 listener + context-param |
| POST 请求中文乱码 | CharacterEncodingFilter 的 filter-mapping 没覆盖到路径 | 补 filter-mapping，或直接映射 `/*`（但注意静态资源） |
| 访问 `/xml/student/list` 404 | servlet-mapping 的 url-pattern 写错或配置文件找不到 | 检查 web.xml 和 `springmvc/xml/springmvc-servlet.xml` |
| 启动慢 | load-on-startup 没配，第一次请求才初始化 | 所有 Servlet 加 `load-on-startup=1` |
| SSM 页面 500 / 表不存在 | 未执行 `sql/` 脚本或库名与 db.properties 不一致 | 执行脚本、检查库名 |
| `/annotation/student/*` 被重定向 | 拦截器设计如此：先访问 `/annotation/page/session` 登录 | 登录后放行 |

---

## 六、模块文件清单

| 文件 | 覆盖知识点 |
|------|-----------|
| `web.xml` | 三个 DispatcherServlet + 父容器监听器 + 编码过滤器 |
| `controller/StudentController` | 注解方式入门（@RestController） |
| `xml/StudentXmlController` + `springmvc/xml/springmvc-servlet.xml` | XML 方式三要素 |
| `controller/ParameterController` | 接收参数 10 种方式 |
| `controller/PageController` + `views/page/show.jsp` | 跳转 + 携带数据 7 种方式 |
| `interceptor/TimeInterceptor` / `LoginInterceptor` | 拦截器全流程 + 拦截中断 |
| `controller/FileController` + `views/file/upload.jsp` | 上传下载 + MultipartResolver |
| `controller/AdvanceController` | 四大补充知识点统一演示（乱码/异常/静态资源/日期绑定） |
| `exception/GlobalExceptionHandler` + `views/error/error.jsp` | 全局异常处理 |
| `annotation/springmvc-servlet.xml` | annotation-driven / 组件扫描 / 视图解析器 / 拦截器 / MultipartResolver / mvc:resources |

---

## 七、章节要点总结

| # | 知识点 | 关键词 |
|---|--------|--------|
| 1 | 拦截器 | preHandle 三返回值 / 洋葱模型 / `<mvc:interceptors>` |
| 2 | 上传下载 | multipartResolver / MultipartFile / attachment / 防路径穿越 |
| 3 | 中文乱码 | CharacterEncodingFilter / defaultEncoding / pageEncoding |
| 4 | 静态资源 | `<mvc:resources>` / DispatcherServlet 映射 / 的连带问题 |
| 5 | 全局异常 | @ControllerAdvice / @ExceptionHandler 按类型精确匹配 |
| 6 | web.xml 启动总线 | ContextLoaderListener / 父子容器 / 多 Servlet 路由 / 加载顺序 |

> **下一章**：SSM 三层整合 —— Controller → Service → Mapper → DB
> 见 `ssm知识点.md`