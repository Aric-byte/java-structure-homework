-- ============================================================
-- 外卖点餐系统数据库脚本
-- 数据库名: takeout_db
-- 角色: 客户(Client) + 骑手(Rider) + 后台管理(Admin)
-- ============================================================

CREATE DATABASE IF NOT EXISTS takeout_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE takeout_db;

-- ============================================================
-- 1. 用户表 (t_user)
-- role: 0-管理员, 1-客户, 2-骑手
-- ============================================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '手机号/账号',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    role TINYINT DEFAULT 1 COMMENT '角色：0-管理员 1-客户 2-骑手',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

INSERT INTO t_user (username, password, nickname, avatar, phone, role) VALUES
-- 管理员
('admin', 'admin123', '系统管理员', 'https://picsum.photos/100/100?random=1', '13800000000', 0),
-- 客户
('13800000001', '123456', '美食达人', 'https://picsum.photos/100/100?random=2', '138****0001', 1),
('13800000002', '123456', '吃货小王', 'https://picsum.photos/100/100?random=3', '138****0002', 1),
('13800000003', '123456', '外卖专家', 'https://picsum.photos/100/100?random=4', '138****0003', 1),
-- 骑手
('rider001', '123456', '骑手小张', 'https://picsum.photos/100/100?random=10', '13900000001', 2),
('rider002', '123456', '骑手小李', 'https://picsum.photos/100/100?random=11', '13900000002', 2),
('rider003', '123456', '骑手小王', 'https://picsum.photos/100/100?random=12', '13900000003', 2);

-- ============================================================
-- 2. 骑手信息表 (t_rider)
-- ============================================================
DROP TABLE IF EXISTS t_rider;
CREATE TABLE t_rider (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) DEFAULT '' COMMENT '真实姓名',
    id_card VARCHAR(20) DEFAULT '' COMMENT '身份证号',
    vehicle_type VARCHAR(20) DEFAULT '' COMMENT '车辆类型：电动车/摩托车',
    vehicle_no VARCHAR(50) DEFAULT '' COMMENT '车牌号',
    latitude DECIMAL(10,6) DEFAULT NULL COMMENT '当前纬度',
    longitude DECIMAL(10,6) DEFAULT NULL COMMENT '当前经度',
    is_online TINYINT DEFAULT 0 COMMENT '是否在线：0-离线 1-在线',
    is_busy TINYINT DEFAULT 0 COMMENT '是否忙碌：0-空闲 1-配送中',
    total_orders INT DEFAULT 0 COMMENT '总接单数',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骑手信息表';

INSERT INTO t_rider (user_id, real_name, vehicle_type, vehicle_no, is_online) VALUES
(5, '张伟', '电动车', '京A12345', 1),
(6, '李强', '电动车', '京B67890', 1),
(7, '王磊', '摩托车', '京C11111', 0);

-- ============================================================
-- 3. 客户地址表 (t_address)
-- ============================================================
DROP TABLE IF EXISTS t_address;
CREATE TABLE t_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '收货人',
    phone VARCHAR(20) NOT NULL COMMENT '电话',
    address VARCHAR(200) NOT NULL COMMENT '详细地址',
    latitude DECIMAL(10,6) DEFAULT NULL,
    longitude DECIMAL(10,6) DEFAULT NULL,
    is_default TINYINT DEFAULT 0 COMMENT '是否默认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址表';

INSERT INTO t_address (user_id, name, phone, address, is_default) VALUES
(2, '张三', '13800000001', '北京市朝阳区建国路88号SOHO现代城A座1201', 1),
(2, '张三', '13800000001', '北京市海淀区中关村大街1号海龙大厦', 0),
(3, '李四', '13800000002', '上海市浦东新区陆家嘴环路1000号恒生银行大厦', 1);

-- ============================================================
-- 4. 商家表 (t_store)
-- ============================================================
DROP TABLE IF EXISTS t_store;
CREATE TABLE t_store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '店名',
    logo VARCHAR(500) DEFAULT '' COMMENT 'Logo',
    image VARCHAR(500) DEFAULT '' COMMENT '店铺图片',
    address VARCHAR(200) DEFAULT '' COMMENT '地址',
    phone VARCHAR(20) DEFAULT '' COMMENT '电话',
    description VARCHAR(500) DEFAULT '' COMMENT '描述',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    month_sales INT DEFAULT 0 COMMENT '月销量',
    delivery_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
    min_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '起送价',
    delivery_time INT DEFAULT 30 COMMENT '预计送达(分钟)',
    business_start TIME DEFAULT '09:00:00' COMMENT '营业开始时间',
    business_end TIME DEFAULT '22:00:00' COMMENT '营业结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-休息 1-营业',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

INSERT INTO t_store (name, logo, image, address, phone, description, rating, month_sales, delivery_fee, min_price, delivery_time) VALUES
('肯德基(国贸店)', 'https://picsum.photos/100/100?random=20', 'https://picsum.photos/400/300?random=21', '北京市朝阳区建国路93号', '010-65888888', '全球知名连锁快餐', 4.8, 8562, 5.00, 20.00, 25),
('麦当劳(中关村店)', 'https://picsum.photos/100/100?random=22', 'https://picsum.photos/400/300?random=23', '北京市海淀区中关村大街27号', '010-82888888', '我就喜欢', 4.7, 6234, 4.00, 15.00, 30),
('必胜客(望京店)', 'https://picsum.photos/100/100?random=24', 'https://picsum.photos/400/300?random=25', '北京市朝阳区望京西路8号', '010-59888888', '欢乐餐厅', 4.6, 3456, 6.00, 30.00, 35),
('瑞幸咖啡(国贸店)', 'https://picsum.photos/100/100?random=26', 'https://picsum.photos/400/300?random=27', '北京市朝阳区光华路9号', '010-65999999', '专业咖啡新鲜烘焙', 4.9, 12345, 3.00, 10.00, 15),
('星巴克(三里屯店)', 'https://picsum.photos/100/100?random=28', 'https://picsum.photos/400/300?random=29', '北京市朝阳区三里屯路19号', '010-64111111', '第三空间', 4.8, 9876, 5.00, 25.00, 20);

-- ============================================================
-- 5. 食品分类表 (t_food_category)
-- ============================================================
DROP TABLE IF EXISTS t_food_category;
CREATE TABLE t_food_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品分类表';

INSERT INTO t_food_category (store_id, name, sort_order) VALUES
(1,'热门推荐',1),(1,'汉堡',2),(1,'炸鸡',3),(1,'饮品',4),(1,'小食',5),
(2,'超值套餐',1),(2,'汉堡',2),(2,'麦乐鸡',3),(2,'饮品',4),
(3,'披萨',1),(3,'意面',2),(3,'小食',3),(3,'饮品',4),
(4,'咖啡',1),(4,'茶饮',2),(4,'甜点',3),
(5,'咖啡',1),(5,'茶饮',2),(5,'甜点',3);

-- ============================================================
-- 6. 食品表 (t_food)
-- ============================================================
DROP TABLE IF EXISTS t_food;
CREATE TABLE t_food (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    image VARCHAR(500) DEFAULT '',
    description VARCHAR(500) DEFAULT '',
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2) DEFAULT NULL,
    month_sales INT DEFAULT 0,
    rating DECIMAL(2,1) DEFAULT 5.0,
    status TINYINT DEFAULT 1 COMMENT '0-下架 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_store_id (store_id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品表';

INSERT INTO t_food (store_id, category_id, name, image, description, price, original_price, month_sales, rating) VALUES
(1,1,'香辣鸡腿堡','https://picsum.photos/400/300?random=30','香辣可口',18.00,22.00,2345,4.8),
(1,1,'原味鸡','https://picsum.photos/400/300?random=31','经典原味',12.00,15.00,1876,4.7),
(1,2,'劲脆鸡腿堡','https://picsum.photos/400/300?random=32','外酥里嫩',17.00,20.00,1567,4.6),
(1,3,'香辣鸡翅(2块)','https://picsum.photos/400/300?random=33','香辣酥脆',10.00,12.00,1234,4.7),
(1,4,'可乐(中)','https://picsum.photos/400/300?random=34','冰爽可口',6.00,8.00,3456,4.5),
(1,5,'薯条(中)','https://picsum.photos/400/300?random=35','香脆可口',9.00,11.00,2345,4.7),
(2,6,'巨无霸套餐','https://picsum.photos/400/300?random=36','经典套餐',35.00,42.00,1234,4.8),
(2,7,'巨无霸','https://picsum.photos/400/300?random=37','经典美味',22.00,26.00,2345,4.8),
(2,8,'麦乐鸡(6块)','https://picsum.photos/400/300?random=38','外酥里嫩',12.00,15.00,1567,4.6),
(2,9,'可乐(中)','https://picsum.photos/400/300?random=39','冰爽可口',6.00,8.00,3456,4.5),
(3,10,'超级至尊披萨','https://picsum.photos/400/300?random=40','多种配料',68.00,88.00,876,4.8),
(3,11,'意大利肉酱面','https://picsum.photos/400/300?random=41','经典肉酱',38.00,45.00,543,4.6),
(3,12,'香辣鸡翅','https://picsum.photos/400/300?random=42','香辣酥脆',22.00,28.00,765,4.7),
(4,14,'生椰拿铁','https://picsum.photos/400/300?random=43','爆款椰香',9.90,13.00,8765,4.9),
(4,15,'多肉葡萄','https://picsum.photos/400/300?random=44','新鲜葡萄',12.90,16.00,4321,4.7),
(5,17,'拿铁','https://picsum.photos/400/300?random=45','经典拿铁',32.00,36.00,5678,4.8),
(5,18,'抹茶拿铁','https://picsum.photos/400/300?random=46','日式抹茶',35.00,40.00,3456,4.6);

-- ============================================================
-- 7. 购物车表 (t_cart)
-- ============================================================
DROP TABLE IF EXISTS t_cart;
CREATE TABLE t_cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ============================================================
-- 8. 订单表 (t_order)
-- status: 0-待支付 1-待接单 2-待取餐 3-配送中 4-已完成 5-已取消
-- ============================================================
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '客户ID',
    store_id BIGINT NOT NULL COMMENT '商家ID',
    rider_id BIGINT DEFAULT NULL COMMENT '骑手ID',
    address_id BIGINT NOT NULL COMMENT '地址ID',
    total_price DECIMAL(10,2) NOT NULL COMMENT '总价',
    pay_price DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    delivery_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
    status TINYINT DEFAULT 0 COMMENT '状态',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    accept_time DATETIME DEFAULT NULL COMMENT '接单时间',
    pickup_time DATETIME DEFAULT NULL COMMENT '取餐时间',
    delivery_time DATETIME DEFAULT NULL COMMENT '送达时间',
    finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    cancel_reason VARCHAR(200) DEFAULT '' COMMENT '取消原因',
    remark VARCHAR(200) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_store_id (store_id),
    KEY idx_rider_id (rider_id),
    KEY idx_order_no (order_no),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 9. 订单项表 (t_order_item)
-- ============================================================
DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    food_image VARCHAR(500) DEFAULT '',
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 插入测试订单
INSERT INTO t_order (order_no, user_id, store_id, rider_id, address_id, total_price, pay_price, delivery_fee, status, remark) VALUES
('ORD20260707001', 2, 1, 5, 1, 43.00, 43.00, 5.00, 4, '不要辣'),
('ORD20260707002', 3, 2, 6, 3, 41.00, 41.00, 4.00, 3, '多放番茄酱'),
('ORD20260707003', 2, 4, NULL, 1, 22.80, 22.80, 3.00, 1, '少糖');

INSERT INTO t_order_item (order_id, food_id, food_name, food_image, price, quantity) VALUES
(1, 1, '香辣鸡腿堡', 'https://picsum.photos/400/300?random=30', 18.00, 1),
(1, 3, '劲脆鸡腿堡', 'https://picsum.photos/400/300?random=32', 17.00, 1),
(1, 5, '可乐(中)', 'https://picsum.photos/400/300?random=34', 6.00, 1),
(2, 7, '巨无霸套餐', 'https://picsum.photos/400/300?random=36', 35.00, 1),
(3, 14, '生椰拿铁', 'https://picsum.photos/400/300?random=43', 9.90, 2);

-- ============================================================
-- 10. 评价表 (t_comment)
-- ============================================================
DROP TABLE IF EXISTS t_comment;
CREATE TABLE t_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    rating INT DEFAULT 5 COMMENT '评分1-5',
    content VARCHAR(500) DEFAULT '' COMMENT '评价内容',
    images VARCHAR(1000) DEFAULT '' COMMENT '图片URL逗号分隔',
    reply VARCHAR(500) DEFAULT '' COMMENT '商家回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_store_id (store_id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

INSERT INTO t_comment (user_id, order_id, store_id, rating, content) VALUES
(2, 1, 1, 5, '配送很快，味道不错！'),
(3, 2, 2, 4, '薯条有点凉了，其他都好');
