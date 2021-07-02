create table SUBMISSION_FLAGGED_USERS (
    submission_id uuid,
    user_id uuid,
    primary key (submission_id, user_id)
);
