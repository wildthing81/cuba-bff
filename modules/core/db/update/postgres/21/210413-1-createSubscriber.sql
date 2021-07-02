create table subscriber (
    ID uuid,
    --
    type varchar(255),
    is_read varchar(255),
    is_hidden varchar(255),
    notification_id uuid not null,
    user_id uuid not null,
    --
    primary key (ID)
);