# mybatis03 知识点 —— 动态 SQL + ResultMap 注解方式


---

## 一、动态 SQL 是什么

动态 SQL = 根据参数值动态拼接 SQL 语句，避免写 N 个重载方法或拼字符串。

| 对比 | 不用动态 SQL | 用动态 SQL |
|------|-------------|-----------|
| 场景 | 固定条件查固定条件 | 多条件任意组合，有值就加条件 |
| 方式 | 多个方法 / 拼字符串 | `<if>` `<where>` `<set>` 等标签 |
| 安全 | 手动处理 `and`/`,` 很烦 | 框架自动处理多余的 `and`/`,` |

---

## 二、动态 SQL 6 大标签

### 2.1 `<if>` —— 条件判断（最常用）

```xml
<if test="username != null and username != ''">
    AND username LIKE CONCAT('%', #{username}, '%')
</if>
```

- `test` 里写 OGNL 表达式（Java Bean 属性/方法都能调）
- 条件成立才把 `AND username LIKE ...` 拼进去

### 2.2 `<where>` —— 智能 WHERE

```xml
SELECT * FROM user
<where>
    <if test="username != null">AND username LIKE '%'</if>
    <if test="email != null">AND email = #{email}</if>
</where>
```

- 所有条件都为空 → 自动去掉 `WHERE` 关键字（不会 `SELECT * FROM user WHERE` 报错）
- 首个条件带了 `AND`/`OR` → 自动去掉（不会 `WHERE AND ...` 报错）

### 2.3 `<set>` —— 智能更新

```xml
UPDATE user
<set>
    <if test="username != null">username = #{username},</if>
    <if test="password != null">password = #{password},</if>
</set>
WHERE id = #{id}
```

- 自动加 `SET` 关键字
- 自动去掉末尾多余的逗号
- 所有字段都为 null → `SET` 空（有 `WHERE` 就不会报错）

### 2.4 `<choose>/<when>/<otherwise>` —— 互斥分支

```xml
<choose>
    <when test="id != null">id = #{id}</when>
    <when test="username != null">username LIKE '%'</when>
    <otherwise>1 = 1</otherwise>
</choose>
```

- 相当于 Java 的 `if - else if - else`
- 只命中第一个满足条件的 `<when>`，后面全部跳过
- `<otherwise>` 兜底（相当于最后的 `else`）

### 2.5 `<foreach>` —— 集合遍历（批量操作必备）

```xml
WHERE id IN
<foreach collection="ids" item="id" open="(" separator="," close=")">
    #{id}
</foreach>
```

| 属性 | 含义 |
|------|------|
| `collection` | 对应 Java 参数的名字（@Param 值 / `list` / `array`） |
| `item` | 每次遍历元素的变量名 |
| `open` | 开头加什么（`(`） |
| `separator` | 元素之间分隔符（`,`） |
| `close` | 结尾加什么（`)`） |

**批量插入**：

```xml
INSERT INTO user(username, email) VALUES
<foreach collection="list" item="u" separator=",">
    (#{u.username}, #{u.email})
</foreach>
```

### 2.6 `<trim>` —— 自定义裁剪（万能版 where/set）

```xml
<trim prefix="WHERE" prefixOverrides="AND |OR ">
    <if test="...">AND username LIKE '%'</if>
</trim>
```

- `prefix`：有内容时才加前缀（WHERE）
- `suffix`：有内容时加后缀
- `prefixOverrides`：去掉开头的 AND/OR
- `suffixOverrides`：去掉末尾的逗号

**`<where>` 本质就是 `<trim prefix="WHERE" prefixOverrides="AND |OR ">` 的简化**

### 2.7 `<bind>` —— OGNL 表达式绑定

```xml
<select id="findUserByBind" parameterType="string" resultMap="userResultMap">
    <bind name="keywordLike" value="'%' + keyword + '%'"/>
    SELECT * FROM user WHERE username LIKE #{keywordLike}
</select>
```

- `name`：绑定的新变量名，SQL 中用 `#{name}` 取值
- `value`：OGNL 表达式（这里是字符串拼接 `'%' + keyword + '%'`）
- **好处**：模糊查询的 `%` 拼接从 Java 层移到 XML；拼接结果仍走 `#{}` 预编译，**没有 SQL 注入风险**（对比 `${}`）
- 典型用途：不同数据库的模糊拼接语法不统一（MySQL `CONCAT` / Oracle `||`），`<bind>` 让 SQL 可移植

---

## 三、`<sql>` + `<include>` —— SQL 片段复用

```xml
<sql id="userColumns">
    id, username, password, email, create_time, update_time
</sql>

<sql id="commonWhere">
    <if test="username != null">AND username LIKE '%'</if>
    <if test="email != null">AND email = #{email}</if>
</sql>

<select id="findUsers" resultMap="...">
    SELECT <include refid="userColumns"/> FROM user
    <where>
        <include refid="commonWhere"/>
    </where>
</select>
```

- `<sql>` 定义可复用片段，`<include refid="...">` 引用
- 片段中可以包含动态标签（`<if>`、`<foreach>` 等），插入后会继续解析

---

## 四、ResultMap 注解方式（mybatis03 新增重点）

### 4.1 注解方式 1：匿名 @Results

```java
@Select("SELECT * FROM user WHERE id = #{id}")
@Results({
    @Result(property = "id",       column = "id",       id = true),
    @Result(property = "username", column = "username"),
    @Result(property = "createTime", column = "create_time")
})
User findByIdAnno(Integer id);
```

- 直接在方法上写，只给当前方法用
- `id = true` 表示主键字段

### 4.2 注解方式 2：命名 @Results（可复用）

```java
@Results(id = "annoUserMap", value = {
    @Result(property = "id",       column = "id",       id = true),
    @Result(property = "username", column = "username")
})
User findByIdWithAnnoMap(Integer id);

@ResultMap("annoUserMap")
List<User> findAllByAnnoMap();
```

- `id = "xxx"` 给这个 ResultMap 起个名字
- 其他方法用 `@ResultMap("xxx")` 引用

### 4.3 注解方式 3：@ResultMap 跨方式引用 XML ResultMap

```java
@Select("SELECT * FROM user WHERE id = #{id}")
@ResultMap("userResultMap")    // 引用 XML 中定义的 id
User findByIdWithXmlMap(Integer id);
```

- **注解和 XML ResultMap 可以互相引用**（namespace.id 方式）

---

## 五、注解写动态 SQL —— `<script>` 包裹

注解中写动态 SQL 很丑，但 MyBatis 支持：

```java
@Select("<script>" +
        "SELECT * FROM user " +
        "<where>" +
        "  <if test='username != null and username != \"\"'>" +
        "    AND username LIKE CONCAT('%', #{username}, '%')" +
        "  </if>" +
        "</where>" +
        "</script>")
List<User> findUsersAnnoDynamic(User user);
```

- 整个 SQL 字符串用 `<script>...</script>` 包裹
- 内部可以写任意 MyBatis 动态标签
- **实际项目中复杂动态 SQL 99% 写 XML**，注解适合简单 SQL

---

## 六、章节要点总结

| # | 知识点 | 核心 |
|---|--------|------|
| 1 | 动态 SQL 6 大标签 | `<if>` `<where>` `<set>` `<choose>` `<foreach>` `<trim>` |
| 2 | `<where>` 自动去首个 AND | 不用自己判断第一个条件带不带 AND |
| 3 | `<set>` 自动去末尾逗号 | UPDATE 只更新非空字段 |
| 4 | `<foreach>` IN 查询 / 批量插入 | `collection` / `item` / `open` / `separator` / `close` |
| 5 | `<sql>` + `<include>` | 复用 SQL 片段 |
| 6 | `<bind>` OGNL 绑定 | 模糊查询拼 `%`，可移植且无注入风险 |
| 7 | @Results 注解 ResultMap | 匿名 vs 命名 vs @ResultMap 引用 XML |
| 8 | 注解 <script> 动态 SQL | 能用但丑，推荐 XML |