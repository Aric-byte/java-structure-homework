-- ========== 创建 dept 和 emp 表（第4节、第5节共用） ==========
-- 用途：演示 Mybatis 多表操作（一对多、多对一）
-- 执行方式：在 MySQL 中运行此脚本
-- =====================================================================

-- 如果表已存在，先删除（谨慎！）
DROP TABLE IF EXISTS emp;
DROP TABLE IF EXISTS dept;

-- 创建部门表
CREATE TABLE dept (
    deptno INT PRIMARY KEY COMMENT '部门编号',
    dname VARCHAR(50) NOT NULL COMMENT '部门名称',
    loc VARCHAR(50) COMMENT '部门位置'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE emp (
    empno INT PRIMARY KEY COMMENT '员工编号',
    ename VARCHAR(50) NOT NULL COMMENT '员工姓名',
    job VARCHAR(50) COMMENT '职位',
    mgr INT COMMENT '上级编号',
    hiredate DATE COMMENT '入职日期',
    sal DECIMAL(10,2) COMMENT '薪水',
    comm DECIMAL(10,2) COMMENT '佣金',
    deptno INT COMMENT '部门编号',
    FOREIGN KEY (deptno) REFERENCES dept(deptno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

INSERT INTO dept (deptno, dname, loc) VALUES
(10, 'ACCOUNTING', 'NEW YORK'),
(20, 'RESEARCH', 'DALLAS'),
(30, 'SALES', 'CHICAGO'),
(40, 'OPERATIONS', 'BOSTON');

INSERT INTO emp (empno, ename, job, mgr, hiredate, sal, comm, deptno) VALUES
(7369, 'SMITH', 'CLERK', 7902, '1980-12-17', 800.00, NULL, 20),
(7499, 'ALLEN', 'SALESMAN', 7698, '1981-02-20', 1600.00, 300.00, 30),
(7521, 'WARD', 'SALESMAN', 7698, '1981-02-22', 1250.00, 500.00, 30),
(7566, 'JONES', 'MANAGER', 7839, '1981-04-02', 2975.00, NULL, 20),
(7654, 'MARTIN', 'SALESMAN', 7698, '1981-09-28', 1250.00, 1400.00, 30),
(7698, 'BLAKE', 'MANAGER', 7839, '1981-05-01', 2850.00, NULL, 30),
(7782, 'CLARK', 'MANAGER', 7839, '1981-06-09', 2450.00, NULL, 10),
(7788, 'SCOTT', 'ANALYST', 7566, '1987-04-19', 3000.00, NULL, 20),
(7839, 'KING', 'PRESIDENT', NULL, '1981-11-17', 5000.00, NULL, 10),
(7844, 'TURNER', 'SALESMAN', 7698, '1981-09-08', 1500.00, 0.00, 30),
(7876, 'ADAMS', 'CLERK', 7788, '1987-05-23', 1100.00, NULL, 20),
(7900, 'JAMES', 'CLERK', 7698, '1981-12-03', 950.00, NULL, 30),
(7902, 'FORD', 'ANALYST', 7566, '1981-12-03', 3000.00, NULL, 20),
(7934, 'MILLER', 'CLERK', 7782, '1982-01-23', 1300.00, NULL, 10);

-- ========== 验证数据 ==========
SELECT * FROM dept;
SELECT * FROM emp;

-- ========== 多对多：skill 表 和 emp_skill 中间表 ==========
-- 用途：演示 Mybatis 多对多关联查询
-- 关系：一个员工可以掌握多项技能，一项技能可以被多个员工掌握
-- =====================================================================

DROP TABLE IF EXISTS emp_skill;
DROP TABLE IF EXISTS skill;

CREATE TABLE skill (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '技能编号',
    name VARCHAR(50) NOT NULL COMMENT '技能名称',
    description VARCHAR(200) COMMENT '技能描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能表';

CREATE TABLE emp_skill (
    empno INT NOT NULL COMMENT '员工编号（外键）',
    skill_id INT NOT NULL COMMENT '技能编号（外键）',
    PRIMARY KEY (empno, skill_id),
    FOREIGN KEY (empno) REFERENCES emp(empno),
    FOREIGN KEY (skill_id) REFERENCES skill(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工-技能中间表（多对多）';

INSERT INTO skill (id, name, description) VALUES
(1, 'Java', 'Java编程语言'),
(2, 'Spring', 'Spring框架'),
(3, 'MySQL', 'MySQL数据库'),
(4, 'MyBatis', 'MyBatis持久层框架'),
(5, 'Redis', 'Redis缓存'),
(6, 'Python', 'Python编程语言'),
(7, 'HTML/CSS', '前端开发基础');

INSERT INTO emp_skill (empno, skill_id) VALUES
(7369, 1), (7369, 3), (7369, 7),
(7499, 1), (7499, 2), (7499, 3),
(7521, 3), (7521, 5),
(7566, 1), (7566, 2), (7566, 4), (7566, 5),
(7654, 1), (7654, 6),
(7698, 1), (7698, 2), (7698, 4),
(7782, 1), (7782, 3),
(7788, 1), (7788, 2), (7788, 4), (7788, 6),
(7839, 1), (7839, 2), (7839, 3), (7839, 4), (7839, 5),
(7844, 5), (7844, 7),
(7876, 3), (7876, 7),
(7900, 1), (7900, 4),
(7902, 1), (7902, 2), (7902, 4), (7902, 6),
(7934, 3), (7934, 5);

-- ========== 验证数据 ==========
SELECT * FROM dept;
SELECT * FROM emp;
SELECT * FROM skill;
SELECT * FROM emp_skill;

-- ========== 多表关联查询测试 ==========
-- 1. 多对一：查询员工及其部门
SELECT e.empno, e.ename, e.job, d.dname, d.loc
FROM emp e
LEFT JOIN dept d ON e.deptno = d.deptno;

-- 2. 多对多：查询员工及其技能
SELECT e.empno, e.ename, s.id AS skill_id, s.name AS skill_name, s.description
FROM emp e
LEFT JOIN emp_skill es ON e.empno = es.empno
LEFT JOIN skill s ON es.skill_id = s.id;