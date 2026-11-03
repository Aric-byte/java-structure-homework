package com.mybatis01.xml;

import com.mybatis01.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * MyBatis Mapper 接口 —— XML 方式
 *
 * ==========================================
 * 【核心知识点】
 * ==========================================
 *
 * 1. Mapper 接口本质
 *    - Mapper 接口没有实现类，MyBatis 在运行时通过 JDK 动态代理自动生成实现
 *    - 接口方法签名 必须 与 XML 中的 SQL 定义一一对应
 *    - 对应规则：XML 的 namespace = 接口全限定名，XML 的 id = 方法名
 *
 * 2. 参数传递的三种方式
 *    - 单个参数（Java 基本类型/String）：直接写，XML 中用 #{任意名} 或 #{_parameter}
 *    - 多个参数：必须用 @Param("别名") 标注，XML 中用 #{别名}
 *    - 复杂参数（POJO / Map）：XML 中直接用 #{属性名} 或 #{map的key}
 *
 * 3. 返回值处理
 *    - 查询单条：返回对象（User）或基本类型（int/String）
 *    - 查询多条：返回 List<T>，MyBatis 自动将每条记录转为对象再装入集合
 *    - 增删改：返回 int（受影响行数）
 *
 * 4. XML 映射文件位置：resources/mybatis01/xml/mapper/UserMapper.xml
 */
public interface UserMapper {

    /**
     * 查询所有用户
     * 【知识点】无参方法 → XML 中 SQL 不需要 parameterType
     */
    List<User> findAll();

    /**
     * 根据 ID 查询单个用户
     * 【知识点】@Param("id") 显式指定参数名 → XML 中必须写 #{id}
     *           如果不写 @Param，多参数时 MyBatis 默认名是 #{param1}、#{param2}
     */
    User findById(@Param("id") Integer id);

    /**
     * 根据用户名模糊查询（LIKE）
     * 【知识点】模糊查询三种写法：
     *           ① Java 层拼接：username 传入 "%zhang%"，XML 写 WHERE username LIKE #{username}
     *           ② XML 中 CONCAT：WHERE username LIKE CONCAT('%', #{username}, '%')
     *           ③ XML 中字符串拼接：WHERE username LIKE '%${username}%'（有 SQL 注入风险！）
     */
    List<User> findByUsername(@Param("username") String username);

    /**
     * 多条件查询（使用 Map 传参）
     * 【知识点】Map 作为参数 → XML 中 #{key} 取对应值
     *           适合条件不固定、参数数量多的场景
     */
    List<User> findByMap(Map<String, Object> map);

    /**
     * 聚合函数查询
     * 【知识点】resultType 写 java.lang.Integer / int 即可
     *           聚合函数返回的是单行单列，MyBatis 自动转换
     */
    int findCount();

    /**
     * 插入用户（含主键回填）
     * 【知识点】主键回填必须同时配置：
     *           ① XML：useGeneratedKeys="true" keyProperty="id"
     *           ② 数据库表 id 列自增（AUTO_INCREMENT）
     *           插入成功后，入参 user.getId() 就能拿到数据库生成的主键值
     */
    int insert(User user);

    /**
     * 根据 ID 更新用户
     * 【知识点】POJO 传参 → XML 中 #{属性名} 直接取字段值
     */
    int updateById(User user);

    /**
     * 根据 ID 删除用户
     * 【知识点】@Param("id") 指定参数名
     *           XML 中 parameterType 写 java.lang.Integer 或不写（可推断）
     */
    int deleteById(@Param("id") Integer id);
}