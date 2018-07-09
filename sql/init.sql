-- ------------------------------------
-- 用户管理与安全验证模块
-- ------------------------------------

-- 用户表
CREATE TABLE sys_user
(
  id                      VARCHAR(36)  NOT NULL,
  username                VARCHAR(255) NOT NULL,
  email                   VARCHAR(255) NULL,
  mobile                  VARCHAR(255) NULL,
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
  CONSTRAINT uk_sys_user_mobile UNIQUE (mobile)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- 权限组表
CREATE TABLE sys_group
(
  id          VARCHAR(36)  NOT NULL,
  code        VARCHAR(255) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_sys_group_code UNIQUE (code),
  CONSTRAINT uk_sys_group_name UNIQUE (name)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- 权限表
CREATE TABLE sys_authority
(
  authority   VARCHAR(255) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (authority),
  CONSTRAINT uk_sys_authority UNIQUE (name)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- 用户-权限组关系表
CREATE TABLE sys_user_group
(
  user_id  VARCHAR(36) NOT NULL,
  group_id VARCHAR(36) NOT NULL,
  PRIMARY KEY (user_id, group_id),
  CONSTRAINT fk_sys_user_group_uid FOREIGN KEY (user_id) REFERENCES SYS_USER (id),
  CONSTRAINT fk_sys_user_group_gid FOREIGN KEY (group_id) REFERENCES SYS_GROUP (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- 权限组-权限关系表
CREATE TABLE sys_group_authority
(
  group_id  VARCHAR(36)  NOT NULL,
  authority VARCHAR(255) NOT NULL,
  PRIMARY KEY (group_id, authority),
  CONSTRAINT fk_sys_group_authority_gid FOREIGN KEY (group_id) REFERENCES SYS_GROUP (id),
  CONSTRAINT fk_sys_group_authority_aid FOREIGN KEY (authority) REFERENCES SYS_AUTHORITY (authority)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
  
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
  DEFAULT CHARSET = utf8;

-- 插入根用户
INSERT INTO SYS_USER (id, username, email, mobile, password, account_non_expired, account_non_locked, credentials_non_expired, enabled, root)
VALUES ('00000000-0000-0000-0000-000000000000', 'root', NULL, NULL, 'NaN', TRUE, TRUE, TRUE, TRUE, TRUE);
-- 插入默认管理员用户
INSERT INTO SYS_USER (id, username, email, mobile, password, account_non_expired, account_non_locked, credentials_non_expired, enabled, root)
VALUES (uuid(), 'admin', NULL, NULL, 'NaN', TRUE, TRUE, TRUE, TRUE, NULL);
-- 插入默认管理员权限组
INSERT INTO SYS_GROUP (id, description, name) VALUES ('00000000-0000-0000-0000-000000000000', '管理员', '管理员');
-- 插入默认管理员权限
INSERT INTO SYS_AUTHORITY (authority, description, name) VALUES ('ADMIN', '管理员', '管理员');
-- 插入默认管理员与默认管理员权限组的关系映射
INSERT INTO SYS_USER_GROUP (user_id, group_id) VALUES (
  (SELECT id
   FROM SYS_USER
   WHERE username = 'admin'), '00000000-0000-0000-0000-000000000000');
-- 插入默认管理员权限组与默认管理员权限的关系映射
INSERT INTO SYS_GROUP_AUTHORITY (group_id, authority) VALUES ('00000000-0000-0000-0000-000000000000', 'ADMIN');

-- ------------------------------------
-- 基础配置、数据字典、文件上下载模块
-- ------------------------------------

-- 配置信息表
CREATE TABLE sys_conf
(
  conf_key         VARCHAR(255) NOT NULL
  COMMENT '配置信息键值',
  conf_description VARCHAR(255) COMMENT '配置信息说明',
  enabled          BIT          NOT NULL
  COMMENT '是否生效',
  conf_value       VARCHAR(255) COMMENT '配置值',
  PRIMARY KEY (conf_key)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT ='配置信息表';

-- 上传文件信息表
CREATE TABLE basic_uploaded_file
(
  id                   VARCHAR(36)  NOT NULL
  COMMENT '上传文件ID',
  archived_filename    VARCHAR(255) NOT NULL
  COMMENT '上传后文件名',
  archived_path_suffix VARCHAR(255) COMMENT '上传保存路径前缀',
  extension            VARCHAR(255) COMMENT '文件扩展名',
  file_byte_size       BIGINT       NOT NULL
  COMMENT '文件大小',
  md5                  VARCHAR(255) NOT NULL
  COMMENT '文件MD5',
  original_filename    VARCHAR(255) NOT NULL
  COMMENT '原始文件名',
  uploaded_date        DATETIME     NOT NULL
  COMMENT '上传时刻',
  uploaded_user_id     VARCHAR(255) NOT NULL
  COMMENT '上传用户ID',
  PRIMARY KEY (id),
  CONSTRAINT UK_BASIC_UPLOADED_FILE_1 UNIQUE (archived_filename)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT ='上传文件记录表';
