create table TASK_VIEWED_USERS (
    task_id uuid,
    user_id uuid,
    primary key (task_id, user_id)
);
