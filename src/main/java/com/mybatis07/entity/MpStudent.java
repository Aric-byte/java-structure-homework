package com.mybatis07.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生实体类（mp_student 表）—— mybatis07 模块独立实体
 * <p>
 * MyBatis-Plus 注解知识点：
 *   @TableName        —— 实体映射的表名
 *   @TableId          —— 主键，IdType.AUTO 使用数据库自增
 *   @TableField       —— 字段映射、填充策略、更新策略
 *   @Version          —— 乐观锁版本号
 *   @TableLogic       —— 逻辑删除标记（0=未删除，1=已删除）
 */
@TableName("mp_student")
public class MpStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    private Integer age;

    private String email;

    private String major;

    // 更新时 score 为 null 则不参与 SET（NOT_NULL 策略）
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Double score;

    // 乐观锁：updateById 时自动追加 WHERE version = #{version} 并 SET version = version + 1
    @Version
    private Integer version;

    // 逻辑删除：deleteById 实际执行 UPDATE mp_student SET deleted = 1
    @TableLogic
    private Integer deleted;

    // 自动填充：插入时由 MyMetaObjectHandler 填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 自动填充：插入和更新时填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public MpStudent() {
    }

    public MpStudent(String name, Integer age, String email, String major, Double score) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.major = major;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "MpStudent{id=" + id + ", name='" + name + '\'' + ", age=" + age +
                ", email='" + email + '\'' + ", major='" + major + '\'' +
                ", score=" + score + ", version=" + version + ", deleted=" + deleted +
                ", createTime=" + createTime + ", updateTime=" + updateTime + '}';
    }
}
