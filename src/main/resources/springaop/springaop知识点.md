# springaop 知识点 —— Spring AOP 面向切面编程

> 模块代码：`com.Spring.Aop.xml`（XML 方式） / `com.Spring.Aop.Annotation`（注解方式）
> 配置文件：`src/main/resources/springaop/aop-xml.xml`
> 可运行入口：`AopXmlTest`（XML）、`AopAnnotationTest`（注解）


---

## 一、为什么需要 AOP

### 1.1 横切关注点

日志、事务、权限校验、缓存、性能统计 —— 这些逻辑**不属于任何一个业务方法**，却要横插到很多方法里，叫**横切关注点（cross-cutting concern）**。

不用 AOP 时只能这么写：

```java
public void saveUser(String name) {
    System.out.println("日志：调用 saveUser，参数 " + name);   // 与业务无关
    long start = System.currentTimeMillis();
    System.out.println("正在保存用户: " + name);                // 真正的业务，只有 1 行
    System.out.println("耗时：" + (System.currentTimeMillis() - start));
}
```

问题：模板代码侵入业务、改一次要改 N 个方法、业务与技术实现耦合。

### 1.2 AOP 的做法

业务类**只留业务**，日志逻辑集中在切面里，由容器在运行期把切面"织入"到方法调用前后：

```java
// 业务类：干净
@Component("userService")
public class UserServiceImpl implements UserService {
    @Override
    public void saveUser(String name) {
        System.out.println("    [saveUser] 正在保存用户: " + name);
    }
}

// 切面：所有日志逻辑集中在这里
@Aspect
@Component
public class LogAspect {
    @Around("execution(* com.Spring.Aop.Annotation.UserService.*(..))")
    public Object aroundLog(ProceedingJoinPoint pjp) throws Throwable { ... }
}
```

**AOP 的前提正是 IoC**：只有对象由 Spring 创建，Spring 才有机会把它替换成"代理对象"。

---

## 二、核心概念与术语

| 术语 | 英文 | 在本项目里对应什么 |
|------|------|--------------------|
| 切面 | Aspect | `LogAspect` 类（`@Aspect`） |
| 连接点 | JoinPoint | 程序执行过程中可被拦截的点，Spring AOP **只支持方法级** |
| 切点 | Pointcut | `execution(* com.Spring.Aop.xml.UserService.*(..))` |
| 通知 | Advice | `@Before` / `@AfterReturning` / `@AfterThrowing` / `@After` / `@Around` |
| 目标对象 | Target | `UserServiceImpl` 实例 |
| 代理对象 | Proxy | Spring 运行时生成，业务代码拿到的其实是它 |
| 织入 | Weaving | Spring AOP 在**运行期**通过动态代理织入（编译期织入是 AspectJ 的能力） |

调用关系：

```
主程序 proxy.saveUser("张三")
      ↓
   代理对象 ──► 通知链（Before → Around → …）
                    ↓
                目标对象 UserServiceImpl.saveUser()
```

---

## 三、五种通知与执行顺序

### 3.1 通知类型

| 通知 | 注解 | XML 标签 | 特点 |
|------|------|----------|------|
| 前置通知 | `@Before` | `<aop:before>` | 目标方法前执行，**拦不住**（不能阻止执行） |
| 后置返回通知 | `@AfterReturning` | `<aop:after-returning>` | 正常返回后执行，**拿得到返回值** |
| 后置异常通知 | `@AfterThrowing` | `<aop:after-throwing>` | 抛异常后执行，**拿得到异常对象** |
| 最终通知 | `@After` | `<aop:after>` | **无论正常还是异常都执行**（类似 finally） |
| 环绕通知 | `@Around` | `<aop:around>` | 最强，可改参数、改返回值、捕获异常、控制是否执行目标方法 |

### 3.2 实测执行顺序（XML 版 `com.Spring.Aop.xml.LogAspect`）

正常返回：

```
⏰ @Before 前置通知 — [saveUser] 参数: [张三]
🌯 @Around 环绕 — 进入 [saveUser] 参数: [张三]
🌯 @Around 环绕 — 修改参数后: [[已加密]张三]
  [saveUser] 正在保存用户: [已加密]张三          ← 目标方法拿到的是改后的参数
🌯 @Around 环绕 — 退出 [saveUser] 耗时: 0ms 返回: null
🔔 @After 最终通知 — [saveUser] 参数: [张三]
✅ @AfterReturning — [saveUser] 参数: [张三] → 返回: null
```

抛出异常：

```
⏰ @Before 前置通知 — [riskyOperation] 参数: []
🌯 @Around 环绕 — 进入 [riskyOperation] 参数: []
  [riskyOperation] 执行危险操作...
🌯 @Around 环绕 — 异常 [riskyOperation] 耗时: 0ms
🔔 @After 最终通知 — [riskyOperation] 参数: []
❌ @AfterThrowing — [riskyOperation] 参数: [] 异常: 模拟数据库连接失败!
```

要点：
- 异常时 **没有 `@AfterReturning`**，取而代之的是 `@AfterThrowing`；`@After` 依然执行。
- `@Around` 用 `try/catch` 包住 `pjp.proceed()`，所以能同时拿到"耗时"和"异常"。

### 3.3 返回值与参数

| 细节 | 说明 |
|------|------|
| `@Around` 的返回值 | 必须 `return pjp.proceed(...)`，否则调用方拿到 `null` |
| `@AfterReturning` 拿返回值 | `returning = "result"`，方法签名多加一个 `Object result` |
| `@AfterThrowing` 拿异常 | `throwing = "ex"`，方法签名多加一个 `Throwable ex` |
| `@Before` / `@After` | 只有 `JoinPoint jp`，能拿 `jp.getSignature().getName()`、`jp.getArgs()` |
| 改参数 | 只能通过 `pjp.proceed(newArgs)`，**只有目标方法（以及位于 Around 内层的通知）能看到新参数** |

实测中 `findUser` 的返回值会"带上被改过的参数"：

```
🌯 @Around 环绕 — 退出 [findUser] 耗时: 1ms 返回: 用户[[已加密]李四]
✅ @AfterReturning — [findUser] 参数: [李四] → 返回: 用户[[已加密]李四]
  >>> 主程序拿到返回值: 用户[[已加密]李四]
```

因为 `UserServiceImpl.findUser` 的返回值就是 `"用户[" + name + "]"`，而 `name` 已被 Around 改成 `[已加密]李四`。

---

## 四、切点表达式 execution 语法

```
execution( 修饰符? 返回值类型 包名.类名.方法名(参数类型) throws? )
```

常用通配符：

| 符号 | 含义 |
|------|------|
| `*` | 任意字符序列（但**不跨包**） |
| `..` | 在包路径中表示**任意层包**；在参数中表示**任意个任意类型参数** |
| `+` | 该类及其子类 |

本项目用到的两种写法：

```java
// 模块内：接口 UserService 的所有方法
@Pointcut("execution(* com.Spring.Aop.Annotation.UserService.*(..))")

// Spring 整合 MyBatis 模块：给所有 Service 加事务（类名含 Service）
execution(* com.Spring.dao.xml.*Service*.*(..))
```

更多示例：

| 表达式 | 含义 |
|--------|------|
| `execution(* *(..))` | 任意类的任意方法（最宽的切点，调试时常用） |
| `execution(public * com.a.service.*.*(..))` | `service` 包下所有 public 方法 |
| `execution(* com.a..*Service.*(..))` | `com.a` 及其子包下所有 `*Service` 的方法 |
| `execution(* save*(..))` | 任意以 `save` 开头的方法 |
| `execution(* *(String, ..))` | 第一个参数是 String 的方法 |
| `within(com.a.service..*)` | 按类型匹配（不加方法名） |
| `this(com.a.service.UserService)` | 代理对象实现了该接口 |
| `target(com.a.service.UserService)` | 目标对象是该类型 |
| `args(java.lang.String)` | 运行时参数是 String |
| `@annotation(com.a.Log)` | 标注了 `@Log` 的方法 |
| `@within(org.springframework.stereotype.Service)` | 类上有 `@Service` |

`@Pointcut` 定义命名切点后，其他通知可以直接引用方法名，便于复用：

```java
@Pointcut("execution(* com.Spring.Aop.Annotation.UserService.*(..))")
public void servicePointcut() { }          // 空方法，只作名字用
```

---

## 五、注解方式实现

### 5.1 三个必备要素

```java
@Configuration
@ComponentScan(basePackages = "com.Spring.Aop.Annotation")
@EnableAspectJAutoProxy(proxyTargetClass = false)   // 开启自动代理
public class AopAnnotationConfig { }
```

```java
@Aspect          // 声明这是切面
@Component       // 同时必须是 Bean！只写 @Aspect 是一个"哑切面"
public class LogAspect {
    @Pointcut("execution(* com.Spring.Aop.Annotation.UserService.*(..))")
    public void servicePointcut() { }

    @Before("servicePointcut()")
    public void beforeLog(JoinPoint jp) { ... }

    @AfterReturning(pointcut = "servicePointcut()", returning = "result")
    public void afterReturningLog(JoinPoint jp, Object result) { ... }

    @AfterThrowing(pointcut = "servicePointcut()", throwing = "ex")
    public void afterThrowingLog(JoinPoint jp, Throwable ex) { ... }

    @After("servicePointcut()")
    public void afterLog(JoinPoint jp) { ... }

    @Around("servicePointcut()")
    public Object aroundLog(ProceedingJoinPoint pjp) throws Throwable { ... }
}
```

| 要素 | 作用 |
|------|------|
| `@Aspect` + `@Component` | 切面 + 注册为 Bean |
| `@EnableAspectJAutoProxy` | 注册 `AnnotationAwareAspectJAutoProxyCreator`，它是个 **BeanPostProcessor**，在初始化阶段把目标对象替换为代理 |
| `proxyTargetClass` | `false`=有接口用 JDK 代理；`true`=一律用 CGLIB |

### 5.2 环绕通知模板

```java
@Around("servicePointcut()")
public Object aroundLog(ProceedingJoinPoint pjp) throws Throwable {
    String method = pjp.getSignature().getName();
    Object[] args = pjp.getArgs();

    Object[] newArgs = args.clone();
    if (newArgs.length > 0 && newArgs[0] instanceof String) {
        newArgs[0] = "[已加密]" + newArgs[0];      // 改参数
    }

    long start = System.currentTimeMillis();
    try {
        Object ret = pjp.proceed(newArgs);         // 放行（必须传参并 return）
        System.out.println("耗时: " + (System.currentTimeMillis() - start));
        return ret;
    } catch (Throwable t) {
        System.out.println("异常，耗时: " + (System.currentTimeMillis() - start));
        throw t;                                    // 异常必须往外抛，否则事务/上层感知不到
    }
}
```

---

## 六、XML 方式实现

```xml
<bean id="userService" class="com.Spring.Aop.xml.UserServiceImpl"/>
<bean id="logAspect"   class="com.Spring.Aop.xml.LogAspect"/>

<aop:config>
    <aop:aspect ref="logAspect">
        <aop:pointcut id="servicePointcut"
                      expression="execution(* com.Spring.Aop.xml.UserService.*(..))"/>

        <aop:before        method="beforeLog"         pointcut-ref="servicePointcut"/>
        <aop:after-returning method="afterReturningLog" pointcut-ref="servicePointcut" returning="result"/>
        <aop:after-throwing  method="afterThrowingLog"  pointcut-ref="servicePointcut" throwing="ex"/>
        <aop:after         method="afterLog"          pointcut-ref="servicePointcut"/>
        <aop:around        method="aroundLog"         pointcut-ref="servicePointcut"/>
    </aop:aspect>
</aop:config>
```

XML 方式下切面类**不需要任何注解**，就是个普通 POJO：

```java
public class LogAspect {
    public void beforeLog(JoinPoint jp) { ... }                  // JoinPoint 由 Spring 传
    public void afterReturningLog(JoinPoint jp, Object result) { ... }
    public void afterThrowingLog(JoinPoint jp, Throwable ex) { ... }
    public Object aroundLog(ProceedingJoinPoint pjp) throws Throwable { ... }
}
```

- 通知方法的**参数表**：`JoinPoint` / `ProceedingJoinPoint` 由 Spring 自动注入；`returning` / `throwing` 指定的参数名对应方法里的形参名。
- 返回值/异常通过 `returning="result"`、`throwing="ex"` 声明，**名字必须与方法形参名一致**。

### 两种方式对比

| 维度 | 注解方式 | XML 方式 |
|------|----------|----------|
| 切面类 | `@Aspect` + `@Component` | 纯 POJO |
| 开关 | `@EnableAspectJAutoProxy` | `<aop:config>`（或 `<aop:aspectj-autoproxy/>`） |
| 切点定义 | `@Pointcut` 方法 | `<aop:pointcut id expression>` |
| 通知声明 | `@Before` / `@Around` … | `<aop:before>` / `<aop:around>` … |
| 通知方法参数 | 注解属性 `returning` / `throwing` | 标签属性 `returning` / `throwing` |
| 可读性 | 与代码在一起，改切点方便 | 集中，切点与类解耦 |
| 本项目对应类 | `com.Spring.Aop.Annotation.LogAspect` | `com.Spring.Aop.xml.LogAspect` |

---

## 七、代理方式：JDK 动态代理 vs CGLIB

| 对比 | JDK 动态代理 | CGLIB |
|------|--------------|-------|
| 前提 | 目标类**必须实现接口** | 无接口也可，生成**子类** |
| 生成方式 | `Proxy.newProxyInstance` | 字节码生成子类，覆写方法 |
| 生效范围 | 接口中声明的方法 | public / protected 非 final 方法 |
| 切换 | `proxyTargetClass=false`（默认） | `proxyTargetClass=true` |
| 限制 | 必须用**接口类型**接收 Bean | final 类/方法无法代理 |

本模块 `UserServiceImpl` 实现了 `UserService` 接口，所以走 JDK 动态代理：

```java
UserService proxy = ctx.getBean(UserService.class);   // ✅ 用接口接收
// UserServiceImpl impl = ctx.getBean(UserServiceImpl.class);  // ❌ JDK 代理下会失败
```

> Spring 4.3 的 CGLIB 是内置的，在 Java 9+ 模块系统下需要 `--add-opens java.base/java.lang=ALL-UNNAMED`（本项目 `maven-surefire-plugin` 的 `argLine` 里已加，否则 `@Configuration` 的 CGLIB 代理创建会失败）。

---

## 八、注意事项与踩坑

### 8.1 `@Aspect` 不等于 Bean

只写 `@Aspect` 而不加 `@Component`（或在 XML 里注册），切面**不会被容器管理**，通知永远不触发，且没有任何报错。

### 8.2 同类内部调用不走代理 —— 最经典的坑

```java
public void a() { this.b(); }   // this.b() 不经过代理，b 上的通知失效
```

只有**通过代理对象**发起的调用才会被拦截。要和 AOP 一起用，得注入自己（`@Autowired private UserService self;`）再 `self.b()`。

### 8.3 `@Around` 忘记 `return pjp.proceed()`

目标方法照常执行，但**调用方拿到 `null`**。对于 `int`/`boolean` 返回值的方法，直接抛 `NullPointerException`。

### 8.4 同一连接点上多个通知的顺序不可依赖

实测同一份业务代码、两套等价的切面，**输出顺序和参数可见性并不一致**：

| 观测项 | XML 版（`aop-xml.xml`） | 注解版（`@Aspect`） |
|--------|------------------------|---------------------|
| 第 1 个输出 | `@Before` | `@Around`（进入） |
| 第 2 个输出 | `@Around`（进入） | `@Around`（修改参数后） |
| 第 3 个输出 | `@Around`（修改参数后） | `@Before` |
| `@Before` 看到的参数 | `[张三]`（**原始**） | `[[已加密]张三]`（**已改**） |
| `@After` / `@AfterReturning` 看到的参数 | `[张三]`（**原始**） | `[[已加密]张三]`（**已改**） |
| 目标方法收到的参数 | `[已加密]张三` | `[已加密]张三` |

原因：两种方式**生成通知链的顺序不同**，`@Around` 的"位置"也不同 —— XML 下 `@Around` 位于链路最内层（最贴近目标方法），注解下 `@Around` 位于最外层，于是内层/外层通知取到的参数快照不一样。

- XML 的通知链顺序与 `<aop:aspect>` 子标签的**书写顺序**一致（本文件里写成 before → after-returning → after-throwing → after → around，所以 `@Around` 落在最内层）；
- 注解方式由 AspectJ 的固定优先级决定：**Around → Before → After → AfterReturning → AfterThrowing**（`@Around` 落在最外层）。

这里只陈述**实测现象**，不代表 Spring 承诺了这套顺序。

**结论（重要）**：
1. **不要依赖同一连接点上多个通知的先后顺序**，需要确定顺序时把通知拆到不同切面并用 `@Order`（或实现 `Ordered`）显式指定；
2. **不要指望别的通知能看到 `@Around` 改过的参数**；
3. 真正需要改参数时，用 `@Around` + `pjp.proceed(newArgs)`，改后的参数只对目标方法可靠。

### 8.5 `JoinPoint.getArgs()` 拿到的是数组引用

`getArgs()` 返回的是本次调用的参数数组，直接改它的元素会影响后续（这也是为什么 Around 里要 `args.clone()` 再改）。

### 8.6 切点写错不会报错，只是静默失效

拼错包名、方法名匹配不上，容器正常启动、业务正常执行，日志里一句提示都没有。排查办法：先把切点放宽成 `execution(* *(..))`，确认切面本身生效，再逐步收窄。

### 8.7 `@AfterReturning` 的返回值参数

`returning = "result"` 里的名字必须与方法形参名**完全一致**；只在异常时 `@AfterReturning` 不执行（改由 `@AfterThrowing` 顶上）。

### 8.8 事务也是 AOP

Spring 的 `@Transactional` 就是基于 AOP 实现的：`DataSourceTransactionManager` + `aop:advisor` 或 `@EnableTransactionManagement`。它同样受"方法非 public 不生效""内部调用不生效"这些 AOP 规则约束（详见 springdao 模块）。

---

## 九、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | AOP 作用 | 把日志/事务等横切逻辑从业务代码中抽出来 |
| 2 | 七个术语 | 切面、连接点、切点、通知、目标、代理、织入 |
| 3 | 五种通知 | Before / AfterReturning / AfterThrowing / After / Around |
| 4 | 执行顺序 | 正常：Before→Around(入)→目标→Around(出)→After→AfterReturning；异常：无 AfterReturning，改 AfterThrowing |
| 5 | 切点表达式 | `execution(返回值 包.类.方法(参数))`，`*` 与 `..` 是核心通配符 |
| 6 | 两种实现 | 注解（`@Aspect`+`@EnableAspectJAutoProxy`）/ XML（`<aop:config>`） |
| 7 | 代理方式 | 有接口走 JDK 动态代理，无接口/`proxyTargetClass=true` 走 CGLIB |
| 8 | 最大坑 | 内部调用不走代理；`@Around` 忘 return；通知顺序不可依赖 |
