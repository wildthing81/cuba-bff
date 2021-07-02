create table SUBMISSION_VIEWED_USERS (
    submission_id uuid,
    user_id uuid,
    primary key (submission_id, user_id)
);
