create table step_transition (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    step_id uuid not null,
    to_step_index integer not null,
    trigger varchar(255) not null,
    submission_status varchar(255) not null,
    button_label varchar(255) not null,
    borrower_refresh boolean not null,
    --
    primary key (ID)
);
