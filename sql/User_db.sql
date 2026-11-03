-- 用户表 —— mybatis01/02/03 模块使用
-- 列名与代码一致：create_time / update_time（mybatis03 的 resultMap 映射这两列）
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 演示友好数据：mybatis02/03 的示例查询 username LIKE '%zhang%'、email='admin@test.com' 都能命中
INSERT INTO `user` (`username`, `password`, `email`) VALUES
('zhangsan', '123456', 'admin@test.com'),
('lisi', '123456', 'lisi@test.com'),
('wangwu', '123456', 'wangwu@test.com'),
('zhaoliu', '123456', 'zhaoliu@test.com'),
('sunqi', '123456', 'sunqi@test.com');
