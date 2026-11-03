-- 学生表 —— mybatis06（缓存演示）与 ssm 模块（SSM 三层整合）使用
-- mybatis06 的演示依赖 id=1 与 id=5 存在；ssm 的列表/增删/事务回滚演示都基于此表
CREATE TABLE `student` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `age` int(11) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `major` varchar(50) DEFAULT NULL,
  `score` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `student` (`name`, `age`, `email`, `major`, `score`) VALUES
('zhangsan', 20, 'zhangsan@test.com', 'CS', 90.0),
('lisi', 21, 'lisi@test.com', 'Math', 85.5),
('wangwu', 22, 'wangwu@test.com', 'CS', 78.0),
('zhaoliu', 23, 'zhaoliu@test.com', 'English', 66.5),
('sunqi', 24, 'sunqi@test.com', 'Physics', 59.0);
