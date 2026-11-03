-- ============================================================
-- MyBatis-Plus 演示用 Student 表
-- 数据库：与 common/db.properties 一致的 test 库
-- ============================================================

DROP TABLE IF EXISTS mp_student;

CREATE TABLE mp_student (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  NOT NULL                 COMMENT '姓名',
    age         INT          DEFAULT NULL             COMMENT '年龄',
    email       VARCHAR(100) DEFAULT NULL             COMMENT '邮箱',
    major       VARCHAR(50)  DEFAULT NULL             COMMENT '专业',
    score       DOUBLE       DEFAULT NULL             COMMENT '分数',
    version     INT          NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
    deleted     INT          NOT NULL DEFAULT 0       COMMENT '逻辑删除标记 0-未删除 1-已删除',
    create_time DATETIME     DEFAULT NULL             COMMENT '创建时间（自动填充）',
    update_time DATETIME     DEFAULT NULL             COMMENT '更新时间（自动填充）',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MyBatis-Plus 学生表示例';

-- 测试数据
INSERT INTO mp_student (name, age, email, major, score) VALUES
('张三', 20, 'zhangsan@test.com', '计算机', 88.5),
('李四', 21, 'lisi@test.com', '软件工程', 92.0),
('王五', 19, 'wangwu@test.com', '人工智能', 78.0),
('赵六', 22, 'zhaoliu@test.com', '计算机', 95.5),
('钱七', 20, 'qianqi@test.com', '大数据', 85.0),
('孙八', 21, 'sunba@test.com', '软件工程', 73.5),
('周九', 23, 'zhoujiu@test.com', '人工智能', 90.0),
('吴十', 20, 'wushi@test.com', '计算机', 66.5);