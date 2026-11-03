-- 资料上传下载系统（作业二 · SSM 实训任务书 V2.0）
-- 在 homework11_3 库中执行；可重复执行（先删后建，测试账号随之重置）

DROP TABLE IF EXISTS download_log;
DROP TABLE IF EXISTS file_info;
DROP TABLE IF EXISTS sys_user;

-- 用户表：密码 = SHA-256(盐 + 明文)，盐单独存储，禁止明文
CREATE TABLE sys_user (
  id          INT          NOT NULL AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL,
  password    CHAR(64)     NOT NULL COMMENT 'SHA-256(salt + 明文)，64 位十六进制',
  salt        CHAR(32)     NOT NULL COMMENT '每个用户独立的随机盐',
  real_name   VARCHAR(50)  DEFAULT NULL,
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 文件元信息表：文件本体存磁盘，这里只存元数据
CREATE TABLE file_info (
  id             INT           NOT NULL AUTO_INCREMENT,
  origin_name    VARCHAR(255)  NOT NULL COMMENT '用户上传时的原始文件名（只存库，不用于拼路径）',
  store_name     VARCHAR(50)   NOT NULL COMMENT '服务端生成的落盘文件名：UUID + 扩展名',
  file_path      VARCHAR(255)  NOT NULL COMMENT '相对上传根目录的路径，如 zhangsan/2026/11/xxx.pdf',
  ext            VARCHAR(10)   NOT NULL COMMENT '小写扩展名，不含点',
  category       VARCHAR(20)   NOT NULL COMMENT '文件分类：文档 / 图片 / 压缩包（按扩展名自动判定）',
  file_size      BIGINT        NOT NULL COMMENT '字节',
  content_type   VARCHAR(100)  DEFAULT NULL,
  uploader_id    INT           NOT NULL,
  create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status         TINYINT       NOT NULL DEFAULT 1 COMMENT '1 正常 / 2 已冻结（禁止下载）/ 0 已删除（逻辑删除）',
  download_count INT           NOT NULL DEFAULT 0 COMMENT '下载次数',
  PRIMARY KEY (id),
  KEY idx_uploader_id (uploader_id),
  CONSTRAINT fk_file_uploader FOREIGN KEY (uploader_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件元信息';

-- 下载日志（F5 选做，由 AOP 切面写入）
CREATE TABLE download_log (
  id            INT       NOT NULL AUTO_INCREMENT,
  file_id       INT       NOT NULL,
  user_id       INT       NOT NULL,
  download_time DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载日志';

-- 测试账号（密码均为 123456），哈希算法与 com.filesys.util.PasswordUtil 一致
INSERT INTO sys_user (username, password, salt, real_name) VALUES
('zhangsan', SHA2(CONCAT('3f1c9a7e5b2d4c8e9a1b6d0f2e4c7a95', '123456'), 256), '3f1c9a7e5b2d4c8e9a1b6d0f2e4c7a95', '张三'),
('lisi',     SHA2(CONCAT('8b4e2d6a1c9f7e3b5d0a2c4e6f8b1d37', '123456'), 256), '8b4e2d6a1c9f7e3b5d0a2c4e6f8b1d37', '李四');
