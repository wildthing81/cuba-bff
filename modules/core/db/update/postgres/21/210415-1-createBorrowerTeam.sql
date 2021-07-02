create table BORROWER_TEAM (
    borrower_id uuid,
    user_id uuid,
    primary key (borrower_id, user_id)
);
