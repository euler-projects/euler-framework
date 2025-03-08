CREATE DATABASE db_name
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_bin;

USE db_name;

# ------------------------------------
# 用户管理与安全验证模块
# ------------------------------------

# 用户表
CREATE TABLE sys_user
(
  id                      VARCHAR(36)  NOT NULL,
  username                VARCHAR(100) NOT NULL,
  email                   VARCHAR(100) NULL,
  phone                  VARCHAR(100) NULL,
  password                VARCHAR(255) NOT NULL,
  account_non_expired     BIT          NOT NULL,
  account_non_locked      BIT          NOT NULL,
  credentials_non_expired BIT          NOT NULL,
  enabled                 BIT          NOT NULL,
  root                    BIT          NULL,
  regist_time             DATETIME     NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_sys_user_username UNIQUE (username),
  CONSTRAINT uk_sys_user_email UNIQUE (email),
  CONSTRAINT uk_sys_user_phone UNIQUE (phone)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin;

# 权限组表
CREATE TABLE sys_group
(
  id          VARCHAR(36)  NOT NULL,
  code        VARCHAR(20) NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_sys_group_code UNIQUE (code),
  CONSTRAINT uk_sys_group_name UNIQUE (name)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin;

# 权限表
CREATE TABLE sys_authority
(
  authority   VARCHAR(100) NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (authority),
  CONSTRAINT uk_sys_authority UNIQUE (name)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin;

# 用户-权限组关系表
CREATE TABLE sys_user_group
(
  user_id  VARCHAR(36) NOT NULL,
  group_id VARCHAR(36) NOT NULL,
  PRIMARY KEY (user_id, group_id),
  CONSTRAINT fk_sys_user_group_uid FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_sys_user_group_gid FOREIGN KEY (group_id) REFERENCES sys_group (id)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin;

# 权限组-权限关系表
CREATE TABLE sys_group_authority
(
  group_id  VARCHAR(36)  NOT NULL,
  authority VARCHAR(100) NOT NULL,
  PRIMARY KEY (group_id, authority),
  CONSTRAINT fk_sys_group_authority_gid FOREIGN KEY (group_id) REFERENCES sys_group (id),
  CONSTRAINT fk_sys_group_authority_aid FOREIGN KEY (authority) REFERENCES sys_authority (authority)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin;
  
CREATE TABLE sys_basic_user_profile
(
  user_id           VARCHAR(36)   NOT NULL
    PRIMARY KEY,
  address           VARCHAR(2000) NULL,
  city              VARCHAR(255)  NULL,
  country_or_region VARCHAR(255)  NULL,
  date_of_birth     DATETIME      NULL,
  ethnicity         VARCHAR(255)  NULL,
  family_name       VARCHAR(255)  NULL,
  gender            VARCHAR(6)    NULL,
  given_name        VARCHAR(255)  NULL,
  name_order        VARCHAR(17)   NULL,
  nationality       VARCHAR(255)  NULL,
  preferred_lang    VARCHAR(20)   NULL,
  province          VARCHAR(255)  NULL
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin;

# 插入根用户
INSERT INTO sys_user (id, username, email, phone, password, account_non_expired, account_non_locked, credentials_non_expired, enabled, root, regist_time)
VALUES ('00000000-0000-0000-0000-000000000000', 'root', NULL, NULL, 'NaN', TRUE, TRUE, TRUE, TRUE, TRUE, now());
# 插入默认管理员用户
INSERT INTO sys_user (id, username, email, phone, password, account_non_expired, account_non_locked, credentials_non_expired, enabled, root, regist_time)
VALUES (uuid(), 'admin', NULL, NULL, 'NaN', TRUE, TRUE, TRUE, TRUE, NULL, now());
# 插入默认管理员权限组
INSERT INTO sys_group (id, code, description, name) VALUES ('00000000-0000-0000-0000-000000000000', 'g_admin', 'Default system management group', 'Administrators');
# 插入默认管理员权限
INSERT INTO sys_authority (authority, description, name) VALUES ('ADMIN', 'Default system management permissions', 'Admin');
# 插入默认管理员与默认管理员权限组的关系映射
INSERT INTO sys_user_group (user_id, group_id) VALUES (
  (SELECT id
   FROM sys_user
   WHERE username = 'admin'), '00000000-0000-0000-0000-000000000000');
# 插入默认管理员权限组与默认管理员权限的关系映射
INSERT INTO sys_group_authority (group_id, authority) VALUES ('00000000-0000-0000-0000-000000000000', 'ADMIN');

GRANT ALL PRIVILEGES ON db_name.* TO 'user'@'host' WITH GRANT OPTION;
FLUSH PRIVILEGES;

# ------------------------------------
# 基础配置、数据字典、文件上下载模块
# ------------------------------------

# 配置信息表
CREATE TABLE sys_conf
(
  conf_key         VARCHAR(100) NOT NULL
  COMMENT '配置信息键值',
  conf_description VARCHAR(255) COMMENT '配置信息说明',
  enabled          BIT          NOT NULL
  COMMENT '是否生效',
  conf_value       VARCHAR(255) COMMENT '配置值',
  PRIMARY KEY (conf_key)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin
  COMMENT ='配置信息表';

# 上传文件信息表
CREATE TABLE basic_uploaded_file
(
  id                   VARCHAR(36)  NOT NULL
  COMMENT '上传文件ID',
  archived_filename    VARCHAR(100) NOT NULL
  COMMENT '上传后文件名',
  archived_path_suffix VARCHAR(255) COMMENT '上传保存路径前缀',
  extension            VARCHAR(255) COMMENT '文件扩展名',
  file_byte_size       BIGINT       NOT NULL
  COMMENT '文件大小',
  md5                  VARCHAR(255) NOT NULL
  COMMENT '文件MD5',
  original_filename    VARCHAR(2000) NOT NULL
  COMMENT '原始文件名',
  uploaded_date        DATETIME     NOT NULL
  COMMENT '上传时刻',
  uploaded_user_id     VARCHAR(255) NOT NULL
  COMMENT '上传用户ID',
  PRIMARY KEY (id),
  CONSTRAINT UK_BASIC_UPLOADED_FILE_AF UNIQUE (archived_filename)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin
  COMMENT ='上传文件记录表';
