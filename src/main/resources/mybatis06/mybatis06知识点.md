# mybatis06 知识点 —— MyBatis 缓存机制


---

## 一、缓存查询流程

```
查询请求
  ↓
查一级缓存（当前 SqlSession 内）── 命中 → 直接返回 ✅
  ↓ 未命中
查二级缓存（同一 Mapper 的所有 SqlSession 共享）── 命中 → 返回并写入一级 ✅
  ↓ 未命中
查数据库 → 结果写入一级缓存
  ↓ commit() / close()
提交到二级缓存
```

---

## 二、一级缓存（LocalCache）

### 2.1 基本特性

| 属性 | 说明 |
|------|------|
| 作用域 | **同一个 SqlSession 内** |
| 默认状态 | **开启**，无法关闭（只能 `localCacheScope=STATEMENT` 禁用） |
| 缓存内容 | 查询结果对象引用（返回的是同一个对象！`s1 == s2` 为 true） |
| 触发失效 | 增删改 / commit / rollback / close / clearCache |

### 2.2 验证：同一个 SqlSession

```java
SqlSession session = factory.openSession();
StudentMapper mapper = session.getMapper(StudentMapper.class);

Student s1 = mapper.selectById(1);  // 发 SQL → 存入一级缓存
Student s2 = mapper.selectById(1);  // 命中一级缓存！不发 SQL
// s1 == s2  → true （同一个对象引用）
```

日志观察：只看到 **1 次** `Preparing: SELECT ...`

### 2.3 验证：不同 SqlSession 不共享

```java
SqlSession s1 = factory.openSession();
SqlSession s2 = factory.openSession();
s1.getMapper(...).selectById(1);   // 发 SQL
s2.getMapper(...).selectById(1);   // 也发 SQL！一级缓存不跨 Session
```

日志观察：**2 次** `Preparing: SELECT ...`

### 2.4 失效场景

```java
SqlSession session = factory.openSession();
StudentMapper mapper = session.getMapper(StudentMapper.class);

mapper.selectById(1);          // 进入一级缓存

// 1. 增删改 → 清空一级缓存
mapper.updateById(new Student(1, "XXX", ...));  
mapper.selectById(1);          // 重新发 SQL ✅

// 2. commit/rollback → 清空一级缓存
session.commit();
mapper.selectById(1);          // 重新发 SQL ✅

// 3. 手动 clearCache
session.clearCache();
mapper.selectById(1);          // 重新发 SQL ✅
```

---

## 三、二级缓存（Mapper Cache）

### 3.1 基本特性

| 属性 | 说明 |
|------|------|
| 作用域 | **同一个 Mapper 的所有 SqlSession 共享** |
| 默认状态 | **不开启**（需要手动配置） |
| 缓存内容 | 对象的序列化拷贝（不同 SqlSession 返回的是不同对象） |
| 触发失效 | 增删改 / 手动 flushCache |

### 3.2 开启二级缓存的 4 个条件

```
① 全局 cacheEnabled=true（默认就是 true）
② Mapper XML 中加 <cache/> 或接口上加 @CacheNamespace
③ 实体类 implements Serializable（Student 已实现）
④ SqlSession 执行 commit() 或 close()（一级缓存数据才提交到二级）
```

### 3.3 XML 开启方式

```xml
<!-- 在 Mapper XML 中加一行 -->
<mapper namespace="...">
    <cache eviction="LRU" size="1024" flushInterval="60000" readOnly="false"/>
    ...
</mapper>
```

| 属性 | 含义 | 默认值 |
|------|------|--------|
| `eviction` | 回收策略：LRU/FIFO/SOFT/WEAK | LRU |
| `size` | 最多缓存对象数 | 1024 |
| `flushInterval` | 自动清空间隔（毫秒） | null（不自动） |
| `readOnly` | true=只读返回对象本身；false=返回拷贝 | false |

### 3.4 注解开启方式

```java
@CacheNamespace
public interface StudentMapper { ... }
```

### 3.5 验证：跨 SqlSession 共享

```java
SqlSession s1 = factory.openSession();
s1.getMapper(...).selectById(1);
s1.close();   // 关键！close 后一级缓存数据才提交到二级缓存

SqlSession s2 = factory.openSession();
s2.getMapper(...).selectById(1);  // 命中二级缓存！不发 SQL
s2.close();
```

日志观察：Session1 看到 SQL，Session2 **没看到 SQL** ✅

### 3.6 不 close/commit = 不提交

```java
SqlSession s1 = factory.openSession();
s1.getMapper(...).selectById(1);
// 故意不 close/commit

SqlSession s2 = factory.openSession();
s2.getMapper(...).selectById(1);  // 不命中！s1 的数据还在一级缓存里
s2.close();
```

---

## 四、缓存失效场景汇总

| 场景 | 一级缓存 | 二级缓存 |
|------|---------|---------|
| 增删改（insert/update/delete） | ✅ 清空当前 Session | ✅ 清空本 Mapper |
| commit() / rollback() | ✅ 清空 | ❌ 不清空 |
| close() | ✅ 清空（并提交到二级） | ❌ 不清空 |
| clearCache() | ✅ 清空当前 Session | ❌ 不清空 |
| 手动 flushCache="true" | ✅ 清空 | ✅ 清空 |

---

## 五、select 标签的缓存属性

```xml
<select id="selectById" resultMap="StudentMap" useCache="true">
    ...
</select>

<select id="selectAll" resultMap="StudentMap" useCache="false">
    ...
</select>

<insert id="insert" flushCache="true">...</insert>
```

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `useCache="true"` | 该查询结果存入二级缓存 | select=true, 其他=false |
| `flushCache="true"` | 执行前清空本 Mapper 的二级缓存 | select=false, 增删改=true |

### 应用场景

- 实时性要求高的查询（SELECT * FROM ...）→ `useCache="false"`
- 批量更新后手动清缓存 → `flushCache="true"`

---

## 六、缓存 Key 结构

```
缓存 Key = Mapper ID + SQL 语句 + 参数值 + RowBounds + BoundSql

同一个 Mapper，SQL 完全一样，参数完全一样 → 同一个 Key → 命中缓存
```

**不同查询绝对不会共享**：哪怕同一个 Mapper，不同 SQL / 不同参数 → 不同 Key

---

## 七、一级 vs 二级 完整对比

| 对比项 | 一级缓存 | 二级缓存 |
|--------|---------|---------|
| 作用域 | 当前 SqlSession | 同一 Mapper 所有 Session |
| 默认状态 | 开启 | 关闭 |
| 缓存数据 | 对象引用 | 对象序列化拷贝 |
| 跨 Session | ❌ 不共享 | ✅ 共享 |
| 提交时机 | 自动 | close()/commit() 后 |
| 失效触发 | 增删改/commit/close/clear | 增删改/flushCache |
| 配置难度 | 无需配置 | 需 `<cache/>` + Serializable |
| 适用场景 | 当前 Session 反复查 | 多 Session 反复查相同数据 |

---

## 八、如何判断是否走了缓存

**看日志中有没有 SQL 日志！**

```
走缓存：Preparing / Parameters 都没有，直接到 Result 或下一段
没走缓存：看到 Preparing: SELECT ... Parameters: ...
```

---

## 九、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | 一级缓存 | SqlSession 级别，默认开启，跨 Session 不共享 |
| 2 | 二级缓存 | Mapper 级别，需手动开启，跨 Session 共享 |
| 3 | 开启二级缓存 4 条件 | cacheEnabled + `<cache/>` + Serializable + close/commit |
| 4 | 缓存回收策略 | LRU(默认)/FIFO/SOFT/WEAK |
| 5 | useCache / flushCache | 控制某条 SQL 是否走缓存 |
| 6 | 实体类必须 Serializable | 二级缓存序列化存储 |
| 7 | 缓存 Key 组成 | Mapper ID + SQL + 参数 + RowBounds |
| 8 | 增删改清空缓存 | 一级+二级同时清空 |