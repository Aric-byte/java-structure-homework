# springioc 知识点 —— Spring IoC 与依赖注入

> 模块代码：`com.Spring.Ioc.xml`（XML 方式） / `com.Spring.Ioc.Annotation`（注解方式）
> 配置文件：`src/main/resources/springioc/ioc-beans.xml`
> 可运行入口：`SpringXmlIoCTest`（XML）、`SpringAnnotationIoCTest`（注解）


---

## 一、IoC 与 DI：到底解决了什么问题

### 1.1 传统写法的问题

```java
// 传统写法：对象自己在内部 new 依赖，控制权在"被调用方"
public class Car {
    private Wheel wheel = new Wheel("米其林", 17);   // 写死
}
```

问题：换轮胎型号要改源码、重新编译；无法替换成 mock；对象的创建与回收没人统一管理。

### 1.2 IoC 写法

```java
// IoC 写法：依赖由外部（容器）送进来，控制权交给了"容器"
<bean id="carWithWheelRef" class="com.Spring.Ioc.xml.Car">
    <property name="wheel" ref="wheelSingleton"/>
</bean>
```

- **IoC（Inversion of Control 控制反转）**：对象的**创建权、依赖装配权**从程序代码转移到容器，是**目标**。
- **DI（Dependency Injection 依赖注入）**：容器把依赖"注入"给对象，是实现 IoC 的**手段**。

### 1.3 容器接口的两层结构

```
BeanFactory                ← 顶层接口，最基础的容器，默认懒加载（getBean 时才创建）
   ↑
ApplicationContext         ← 子接口，企业开发实际使用的容器
   ├── ClassPathXmlApplicationContext     从 classpath 读 XML
   └── AnnotationConfigApplicationContext 读 @Configuration 类
```

| 对比 | BeanFactory | ApplicationContext |
|------|-------------|--------------------|
| 加载时机 | 懒加载（用时才创建） | 启动即创建所有单例 |
| 国际化 / 事件 / 资源加载 | 不支持 | 支持 |
| 实际使用 | 几乎不用 | 本项目两种方式都用它 |

### 1.4 本项目怎么拿到 Bean

```java
// XML 方式
ClassPathXmlApplicationContext ctx =
        new ClassPathXmlApplicationContext("springioc/ioc-beans.xml");
Car car = (Car) ctx.getBean("carBySetter");        // 按 id 取
Wheel w  = ctx.getBean(Wheel.class);               // 按类型取

// 注解方式
AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);
Car car = (Car) ctx.getBean("carBySetter");

ctx.close();     // 关闭容器 → 触发销毁回调
```

---

## 二、两种装配方式总览

| 维度 | XML 方式 | 注解方式 |
|------|----------|----------|
| 配置位置 | `springioc/ioc-beans.xml` | Java 类 + 类上的注解 |
| 启动容器 | `ClassPathXmlApplicationContext` | `AnnotationConfigApplicationContext` |
| 定义 Bean | `<bean id class>` | `@Component` / `@Bean` |
| 值注入 | `<property name value>` | `@Value` |
| 引用注入 | `<property name ref>` | `@Autowired`(+`@Qualifier`) |
| 作用域 | `scope="prototype"` | `@Scope("prototype")` |
| 生命周期 | `init-method` / `destroy-method` | `@PostConstruct` / `@PreDestroy` |
| 是否需要扫描 | 不需要，逐个声明 | 需要 `@ComponentScan` 指定包 |
| 优点 | 集中、改配置不用改代码 | 类型安全、简洁、少写 XML |

**本项目两套代码并存**：`com.Spring.Ioc.xml` 是纯 Java 类（无注解，靠 XML 装配）；`com.Spring.Ioc.Annotation` 是带注解的类（靠注解装配）。两类都叫 `Car` / `Wheel` / `Person`，但**包名不同，互不干扰**。

---

## 三、XML 方式：七种注入写法

### 3.1 无参构造 + Setter 注入值类型

```xml
<bean id="carBySetter" class="com.Spring.Ioc.xml.Car">
    <property name="brand" value="比亚迪"/>
    <property name="model" value="秦PLUS"/>
    <property name="price" value="128000"/>
</bean>
```

```
Car{brand='比亚迪', model='秦PLUS', price=128000, wheel=null}   ← wheel 没配就是 null
```

要求：类必须有**无参构造器**（Spring 先 new 出来，再调 setter）。

### 3.2 构造器注入值类型

```xml
<bean id="carByConstructor" class="com.Spring.Ioc.xml.Car">
    <constructor-arg value="特斯拉"/>
    <constructor-arg value="Model 3"/>
    <constructor-arg value="259900"/>
</bean>
```

`Car` 里有三个构造器（无参 / 三参 / 四参），写 3 个 `<constructor-arg>` → Spring 选**三参构造器**。
不写 `index` 时按**声明顺序**匹配，遇到同类型参数容易错位。

### 3.3 Setter + ref 注入引用类型

```xml
<bean id="carWithWheelRef" class="com.Spring.Ioc.xml.Car">
    <property name="brand" value="宝马"/>
    <property name="model" value="3系"/>
    <property name="price" value="320000"/>
    <property name="wheel" ref="wheelSingleton"/>
</bean>
```

```
Car{brand='宝马', model='3系', price=320000, wheel=Wheel{type='米其林', size=17}}
```

- `value` 用于**值类型**（String/int/Double…）
- `ref` 用于**引用类型**，值是另一个 `<bean>` 的 id

### 3.4 内部 Bean（inner bean）

```xml
<bean id="carWithInnerWheel" class="com.Spring.Ioc.xml.Car">
    <property name="brand" value="奔驰"/>
    <property name="model" value="C级"/>
    <property name="price" value="350000"/>
    <property name="wheel">
        <bean class="com.Spring.Ioc.xml.Wheel">   <!-- 没有 id -->
            <property name="type" value="马牌"/>
            <property name="size" value="19"/>
        </bean>
    </property>
</bean>
```

```
Car{brand='奔驰', model='C级', price=350000, wheel=Wheel{type='马牌', size=19}}
```

内部 Bean **没有 id、不能被 `getBean()` 取到**，只服务于外层 Bean，不会被复用。

### 3.5 构造器 + index + ref

```xml
<bean id="carFullConstructor" class="com.Spring.Ioc.xml.Car">
    <constructor-arg index="0" value="奥迪"/>
    <constructor-arg index="1" value="A4L"/>
    <constructor-arg index="2" value="300000"/>
    <constructor-arg index="3" ref="wheelPrototype"/>
</bean>
```

```
Car{brand='奥迪', model='A4L', price=300000, wheel=Wheel{type='固特异', size=18}}
```

`index` 从 **0** 开始，显式指定参数位置 → 这里准确命中**四参构造器**。

### 3.6 集合属性注入（List / Set / Map / Properties）

```xml
<bean id="personRich" class="com.Spring.Ioc.xml.Person">
    <property name="name" value="张三"/>
    <property name="age" value="25"/>
    <property name="hobbies">
        <list><value>游泳</value><value>编程</value><value>读书</value></list>
    </property>
    <property name="skills">
        <set><value>Java</value><value>Spring</value><value>MyBatis</value></set>
    </property>
    <property name="scores">
        <map><entry key="语文" value="85.5"/><entry key="数学" value="92.0"/></map>
    </property>
    <property name="healthInfo">
        <props><prop key="height">175cm</prop><prop key="weight">70kg</prop></props>
    </property>
</bean>
```

标签与 Java 类型对应表：

| XML 标签 | Java 类型 | 特点 |
|----------|-----------|------|
| `<list>` | List / 数组 | 有序、可重复 |
| `<set>` | Set | 无序、不可重复 |
| `<map>` + `<entry>` | Map | 键值对 |
| `<props>` + `<prop>` | Properties | key/value 都是 String |

### 3.7 作用域与生命周期

```xml
<!-- 单例：容器里只有一份 -->
<bean id="wheelSingleton" class="com.Spring.Ioc.xml.Wheel"> ... </bean>

<!-- 多例：每次 getBean 都 new 一个 -->
<bean id="wheelPrototype" class="com.Spring.Ioc.xml.Wheel" scope="prototype"> ... </bean>

<!-- 生命周期回调 -->
<bean id="carLifecycle" class="com.Spring.Ioc.xml.Car"
      init-method="customInit"
      destroy-method="customDestroy">
    <constructor-arg value="保时捷"/>
    <constructor-arg value="911"/>
    <constructor-arg value="1500000"/>
</bean>
```

---

## 四、注解方式：等价写法与组件扫描

### 4.1 配置类

```java
@Configuration
@ComponentScan(basePackages = "com.Spring.Ioc.Annotation")
public class SpringAnnotationConfig {

    @Bean(name = "wheelPrototype")
    @Scope("prototype")
    public Wheel wheelPrototype() {
        return new Wheel("固特异", 18);
    }

    @Bean(name = "carLifecycle")
    public Car carLifecycle() {
        return new Car("保时捷", "911", 1500000);
    }

    @Bean(name = "hobbies")
    public List<String> hobbies() { ... }        // 集合类型的 Bean

    @Bean(name = "skills")  public Set<String> skills() { ... }
    @Bean(name = "scores")  public Map<String, Double> scores() { ... }
    @Bean(name = "healthInfo") public Properties healthInfo() { ... }
}
```

| 注解 | 作用 | 等价 XML |
|------|------|----------|
| `@Configuration` | 声明这是配置类（本身就是个 Bean） | `<beans>` 根标签 |
| `@ComponentScan(basePackages="…")` | 扫描指定包下的 `@Component` 家族 | `<context:component-scan base-package="…"/>` |
| `@Bean(name="x")` | 方法返回值注册为 Bean，名字 = name 或方法名 | `<bean id="x" class="…">` |
| `@Scope("prototype")` | 作用域 | `scope="prototype"` |

### 4.2 组件与值注入

```java
@Component("carBySetter")          // bean 名 = carBySetter
public class Car {

    @Value("比亚迪")
    public void setBrand(String brand) { this.brand = brand; }

    @Value("秦PLUS")
    public void setModel(String model) { this.model = model; }

    @Value("128000")
    public void setPrice(int price) { this.price = price; }

    @Autowired
    @Qualifier("wheelSingleton")
    public void setWheel(Wheel wheel) { ... }
}
```

```java
@Component("wheelSingleton")
@Primary                            // 同类型有多个时优先选它
@Scope("singleton")
public class Wheel {
    @Value("米其林") private String type;
    @Value("17")     private int size;
}
```

- `@Component` 家族：`@Controller`（Web 层）、`@Service`（业务层）、`@Repository`（持久层）都是它的派生注解。
- **默认 Bean 名** = 类名首字母小写（`Wheel` → `wheel`）；写了 `@Component("xxx")` 就用 `xxx`。
- `@Value` 只能注入**值类型**，支持 `@Value("${jdbc.url}")` 读配置文件、`@Value("#{...}")` 写 SpEL。
- `@Autowired` **默认按类型**注入；同类型有多个时，用 `@Qualifier("id")` **按名字**指定，或在某个 Bean 上加 `@Primary` 指定默认首选。

### 4.3 集合类型的注入

注解方式下集合属性不能靠 `@Value`，要写成 `@Bean` 再按名字注进来：

```java
@Autowired
public void setHobbies(@Qualifier("hobbies") List<String> hobbies) { ... }

@Autowired
public void setSkills(@Qualifier("skills") Set<String> skills) { ... }

@Autowired
public void setScores(@Qualifier("scores") Map<String, Double> scores) { ... }

@Autowired
public void setHealthInfo(@Qualifier("healthInfo") Properties healthInfo) { ... }
```

`Person` 上还有两个字段直接用 `@Value`：

```java
@Value("张三") private String name;
@Value("25")   private int age;
```

### 4.4 生命周期回调与容器里的内置 Bean

```java
@PostConstruct                     // 等价 XML 的 init-method
public void init() { System.out.println("★ @PostConstruct -> " + name); }

@PreDestroy                        // 等价 XML 的 destroy-method
public void destroy() { System.out.println("★ @PreDestroy -> " + name); }
```

实测打印容器里所有 Bean：

```
- org.springframework.context.annotation.internalConfigurationAnnotationProcessor  scope=
- org.springframework.context.annotation.internalAutowiredAnnotationProcessor      scope=
- org.springframework.context.annotation.internalRequiredAnnotationProcessor       scope=
- org.springframework.context.annotation.internalCommonAnnotationProcessor         scope=
- org.springframework.context.event.internalEventListenerProcessor                 scope=
- org.springframework.context.event.internalEventListenerFactory                   scope=
- springAnnotationConfig  scope=singleton
- carBySetter  scope=singleton
- personRich   scope=singleton
- wheelSingleton scope=singleton
- wheelPrototype scope=prototype
- carLifecycle scope=            ← @Bean 未指定 scope，值为空串（即 singleton）
- hobbies / skills / scores / healthInfo  scope=
```

前 6 个是 Spring 为了支持 `@Autowired`、`@PostConstruct` 等注解**自动注册的后置处理器**，不是业务 Bean。

> ⚠️ `@PostConstruct` / `@PreDestroy` 属于 **JSR-250** 规范，Spring 4.3 下需要 `javax.annotation-api` 依赖（pom.xml 里已加，否则注解会被静默忽略）。

---

## 五、Bean 作用域：singleton 与 prototype

| 对比项 | singleton（默认） | prototype |
|--------|------------------|-----------|
| 实例数量 | 容器内**唯一** | 每次 `getBean()` 都新建 |
| 创建时机 | 容器启动时就创建 | 用到时才创建 |
| 容器是否管理销毁 | ✅ close() 时调用销毁方法 | ❌ 只负责创建，不管销毁 |
| 适用场景 | 无状态的服务/DAO/工具类 | 有状态的对象 |

实测输出（两种方式一致）：

```
singleton(默认): true      ← 两次 getBean 拿到同一个对象
prototype:       true      ← 两次 getBean 拿到不同对象
```

Web 环境下还有 `request` / `session` / `application` 作用域（属于 `spring-web` 模块）。

---

## 六、Bean 生命周期

### 6.1 完整流程（11 步）

```
① 实例化          构造器被调用（反射 new）
② 属性注入        Setter 注入 / @Value / @Autowired
③ BeanNameAware   setBeanName()
④ BeanFactoryAware setBeanFactory()
⑤ ApplicationContextAware setApplicationContext()
⑥ BeanPostProcessor.postProcessBeforeInitialization()
⑦ 初始化          @PostConstruct → InitializingBean.afterPropertiesSet() → init-method
⑧ BeanPostProcessor.postProcessAfterInitialization()   ← AOP 代理在这一步生成
⑨ Bean 就绪，被业务代码使用
⑩ 销毁            @PreDestroy → DisposableBean.destroy() → destroy-method
⑪ 容器关闭        ctx.close()
```

三者执行顺序（第 ⑦ 步内部）：**`@PostConstruct` → `afterPropertiesSet()` → `init-method`**。

### 6.2 XML 方式实测

```xml
<bean id="carLifecycle" class="com.Spring.Ioc.xml.Car"
      init-method="customInit" destroy-method="customDestroy">
```

```
  [Car] 三参构造器 -> 保时捷 911        ← ① 实例化
  [Car] ★ init-method -> 保时捷 911     ← ⑦ 初始化
  [Car] ★ destroy-method -> 保时捷 911  ← ⑩ 销毁（ctx.close() 时）
```

### 6.3 注解方式实测

```
  [SpringAnnotationConfig] 构造器被调用
  [Car] 无参构造器
  [Car] @Autowired + @Qualifier 注入 Wheel -> 米其林
  [Person] ★ @PostConstruct 初始化 -> 张三
  ...
  [Person] ★ @PreDestroy 销毁 -> 张三     ← ctx.close() 时
```

---

## 七、注解速查表

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Configuration` | 类 | 声明配置类 |
| `@ComponentScan` | 类 | 指定扫描包（`basePackages` / `basePackageClasses`） |
| `@Component` / `@Controller` / `@Service` / `@Repository` | 类 | 注册为 Bean |
| `@Bean(name=)` | 方法 | 方法返回值注册为 Bean |
| `@Scope("prototype")` | 类/方法 | 指定作用域 |
| `@Value("…")` | 字段/Setter | 注入值类型 / `${}` / SpEL |
| `@Autowired(required=false)` | 字段/Setter/构造器 | 按类型注入 |
| `@Qualifier("id")` | 字段/Setter 参数 | 按名字注入 |
| `@Primary` | 类 | 同类型多个时的首选 |
| `@PostConstruct` / `@PreDestroy` | 方法 | 初始化 / 销毁回调 |
| `@Lazy` | 类/方法 | 延迟初始化 |
| `@PropertySource("classpath:x.properties")` | 类 | 加载 properties 文件 |

---

## 八、注意事项与踩坑

### 8.1 注解方式下 `@Value` 会覆盖 `@Bean` 方法里构造时传入的值

`SpringAnnotationConfig` 里：

```java
@Bean(name = "carLifecycle")
public Car carLifecycle() {
    return new Car("保时捷", "911", 1500000);
}
```

但 `Car` 类自己带注解：

```java
@Component("carBySetter")
public class Car {
    @Value("比亚迪") public void setBrand(String brand) { ... }
    @Value("秦PLUS") public void setModel(String model) { ... }
    @Value("128000") public void setPrice(int price) { ... }
    @Autowired @Qualifier("wheelSingleton") public void setWheel(Wheel wheel) { ... }
}
```

实测 `ctx.getBean("carLifecycle")` 打印的不是"保时捷 911"，而是：

```
  -> Car{brand='比亚迪', model='秦PLUS', price=128000, wheel=Wheel{type='米其林', size=17}}
```

原因：`@Value` / `@Autowired` 由 **BeanPostProcessor** 处理，它对容器里**所有** Bean 生效，包括 `@Bean` 方法返回的对象 —— 于是构造时传的"保时捷/911/1500000"在属性注入阶段被 `@Value` 覆盖了。
而 XML 方式下 `com.Spring.Ioc.xml.Car` 没有任何注解，同样写法就保留"保时捷 911"。

**结论**：不要让同一个类既被 `@Component` 扫描、又在 `@Bean` 方法里 `new` 出来还指望保留构造值；要么用 XML 的纯 POJO，要么把配置值统一到 `@Value`/`@Bean` 一处。

### 8.2 单例 Bean 在容器启动时就被创建

实测启动日志里 9 个 Bean 的构造器在容器创建阶段就全部打印了（`carBySetter`、`carByConstructor`、`carWithWheelRef`、`carWithInnerWheel`、`carFullConstructor`、`personRich`、`carLifecycle`…），只有 `wheelPrototype` 是 `getBean` 时才创建。

### 8.3 `<constructor-arg>` 容易选错构造器

不写 `index` 时按声明顺序匹配；参数类型相同（都是 String）时尤其危险。**多构造器的类建议一律写 `index`**。

### 8.4 内部 Bean 取不到

`carWithInnerWheel` 里的 `Wheel` 没有 id，`ctx.getBean(Wheel.class)` 只会拿到 `wheelSingleton`，不可能拿到内部那个。

### 8.5 prototype Bean 的销毁方法不会被调用

容器只负责创建、不缓存引用，所以 `@PreDestroy` / `destroy-method` 对 prototype 无效，需要自己清理。

### 8.6 集合类型注入必须按名字

`List<String>`、`Map<String,Double>` 这类 Bean 按类型注入有歧义（容器里可能有多个集合 Bean），所以注解方式必须配 `@Qualifier("hobbies")` 这样的名字；XML 方式则直接写在 `<property>` 里，不经过容器查找。

### 8.7 `@Autowired` 找不到 Bean 会直接启动失败

默认 `required=true`，同类型找不到就抛 `NoSuchBeanDefinitionException`；同类型有多个则抛 `NoUniqueBeanDefinitionException`（这时用 `@Qualifier` 或 `@Primary` 解决）。

---

## 九、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | IoC / DI | IoC 是目标，DI 是手段；控制权交给容器 |
| 2 | 容器体系 | BeanFactory（懒加载）← ApplicationContext（启动即建单例） |
| 3 | XML 七种注入 | setter 值 / 构造器值 / setter+ref / 内部 Bean / index+ref / 集合 / 生命周期 |
| 4 | 注解等价写法 | `@Component`、`@Value`、`@Autowired`、`@Configuration`、`@Bean` |
| 5 | 组件扫描 | `@ComponentScan` + 默认 Bean 名 = 类名首字母小写 |
| 6 | 限定注入 | `@Qualifier` 按名字、`@Primary` 定首选 |
| 7 | 作用域 | singleton（默认、容器管理）/ prototype（每次新建、不管销毁） |
| 8 | 生命周期 | 构造 → 注入 → `@PostConstruct`/init-method → 使用 → `@PreDestroy`/destroy-method |
| 9 | 最大坑 | `@Value`/`@Autowired` 会覆盖 `@Bean` 方法里构造时传入的值 |
