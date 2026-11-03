package com.mybatis07.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;

/**
 * 学生实体类（mp_student 表）—— mybatis07 模块独立实体（ActiveRecord 模式）
 * <p>
 * 知识点：MyBatis-Plus ActiveRecord（AR）模式。
 * 实体继承 Model&lt;MpStudentAR&gt; 后即可直接调用实体自身的
 * insertOrUpdate() / deleteById() / selectById() 等方法，
 * 无需再通过 Mapper 中转（内部依然走 BaseMapper）。
 */
@TableName("mp_student")
public class MpStudentAR extends Model<MpStudentAR> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer age;

    private String email;

    private String major;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Double score;

    // 乐观锁版本号
    @Version
    private Integer version;

    // 逻辑删除标记
    @TableLogic
    private Integer deleted;

    // 自动填充：插入时
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 自动填充：插入和更新时
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public MpStudentAR() {
    }

    public MpStudentAR(String name, Integer age, String email, String major, Double score) {
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
        return "MpStudentAR{id=" + id + ", name='" + name + '\'' + ", age=" + age +
                ", email='" + email + '\'' + ", major='" + major + '\'' +
                ", score=" + score + ", version=" + version + ", deleted=" + deleted +
                ", createTime=" + createTime + ", updateTime=" + updateTime + '}';
    }
}
