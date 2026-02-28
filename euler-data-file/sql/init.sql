create table t_file_storage_index
(
    id             varchar(36)  not null comment 'File ID',
    filename       varchar(255) not null comment 'Original filename',
    extension      varchar(255) null comment 'File extension',
    storage_type   varchar(8)   not null comment 'File storage type',
    storage_index  varchar(255) not null comment 'File storage index',
    user_id        varchar(36)  not null comment 'ID of the user who owns the resource',
    tenant_id      varchar(36)  not null comment 'ID of the tenant to which the resource belongs',
    resource_scope int unsigned not null comment 'Resource visibility scope',
    created_by     varchar(36)  not null comment 'ID of the user who created the resource',
    modified_by    varchar(36)  not null comment 'ID of the user who last modified the resource',
    created_date   datetime(3)  not null comment 'Timestamp when the resource was created',
    modified_date  datetime(3)  not null comment 'Timestamp when the resource was last modified',
    primary key (id)
)
    engine = innodb
    default character set utf8mb4
    default collate utf8mb4_bin
    COMMENT ='The file storage index';

create table t_file_storage_jdbc
(
    id   bigint unsigned not null auto_increment comment 'Uploaded file ID',
    size int             not null comment 'File size in bytes',
    data longblob        not null comment 'Binary file data',
    primary key (id)
)
    engine = innodb
    default character set utf8mb4
    default collate utf8mb4_bin
    COMMENT ='JDBC file storage';

create table t_file_storage_local
(
    id         varchar(36) not null comment 'Uploaded file ID',
    prefix     varchar(10) not null comment 'Path prefix for the saved file',
    saved_name varchar(36) not null comment 'Name of the saved file',
    size       int         not null comment 'File size in bytes',
    primary key (id)
)
    engine = innodb
    default character set utf8mb4
    default collate utf8mb4_bin
    COMMENT ='Local file storage';