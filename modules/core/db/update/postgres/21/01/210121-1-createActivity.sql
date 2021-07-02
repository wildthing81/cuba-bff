create table activity (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    id_key varchar(255) not null,
    id_value varchar(255) not null,
    details jsonb,
    --
    primary key (ID)
);