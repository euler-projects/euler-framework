create table t_file_storage_index
(
    id            varchar(36)  not null comment '文件ID',
    filename      varchar(255) not null comment '原始文件名',
    extension     varchar(255) null comment '文件扩展名',
    storage_type  varchar(8)   not null comment '文件存储类型',
    storage_index varchar(255) not null comment '文件存储索引',
    tenant_id     varchar(255) not null comment '资源所属租户ID',
    created_by    varchar(255) not null comment '资源创建人',
    created_date  datetime(3)  not null comment '资源创建时间',
    modified_by   varchar(255) not null comment '资源最后修改人',
    modified_date datetime(3)  not null comment '资源最后修改时间',
    primary key (id)
)
    engine = innodb
    default character set utf8mb4
    default collate utf8mb4_bin
    COMMENT ='The file storage index';


create table t_file_storage_jdbc
(
    id   bigint unsigned not null auto_increment comment '上传文件id',
    data longblob        not null comment '文件数据',
    primary key (id)
)
    engine = innodb
    default character set utf8mb4
    default collate utf8mb4_bin
    COMMENT ='JDBC file storage';