create table workflow_step (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    workflow_id uuid not null,
    index integer,
    name varchar(255) not null,
    layout jsonb,
    --
    primary key (ID)
);