-- begin SECTION_TYPE
create table section_type (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    type varchar(255) not null,
    --
    primary key (ID)
)^
-- end SECTION_TYPE
-- begin INSTITUTION
create table institution (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    name varchar(255) not null,
    site_id varchar(255) not null,
    configuration jsonb,
    borrowerDefaults jsonb,
    --
    primary key (ID)
)^
-- end INSTITUTION
-- begin SUBMISSION
create table submission (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    institution_id uuid not null,
    due_date timestamp,
    note text,
    status varchar(255) not null,
    borrower_id uuid not null,
    workflow_id uuid,
    workflow_version integer,
    workflow_step integer,
    display_ref varchar(10),
    --
    primary key (ID)
)^
-- end SUBMISSION
-- begin TASK
create table task (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    submission_id uuid not null,
    category varchar(255) not null,
    assigned_to uuid not null,
    type varchar(255) not null,
    due_date timestamp,
    description text,
    note text,
    status varchar(255) not null,
    --
    primary key (ID)
)^
-- end TASK
-- begin SUBMISSION_SECTION
create table submission_section (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    slug varchar(255) not null,
    submission_id uuid not null,
    content text not null,
    exceptions jsonb,
    comments text,
    --
    primary key (ID)
)^
-- end SUBMISSION_SECTION
-- begin SUBMISSION_PURPOSE
create table submission_purpose (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    submission_id uuid not null,
    type text not null,
    purpose text not null,
    is_primary boolean,
    --
    primary key (ID)
)^
-- end SUBMISSION_PURPOSE
-- begin SUBMISSION_TYPE
create table submission_type (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    type varchar(255) not null,
    --
    primary key (ID)
)^
-- end SUBMISSION_TYPE
-- begin BORROWER
create table borrower (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    institution_id uuid not null,
    name varchar(255) not null,
    submission_defaults jsonb,
    customer_type varchar(255),
    market_cap bigint,
    cad_level integer,
    customer_group varchar(255),
    business_unit varchar(255),
    anzsic varchar(255),
    ccr_risk_score integer,
    security_index varchar(255),
    external_rating_and_outlook integer,
    last_full_review_ts timestamp,
    last_schedule_review_ts timestamp,
    next_schedule_review_ts timestamp,
    risk_sign_off varchar(255),
    regulatory_requirements varchar(255),
    --
    primary key (ID)
)^
-- end BORROWER
-- begin COMMENT
create table comment (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    submission_id uuid not null,
    text varchar(255) not null,
    --
    primary key (ID)
)^
-- end COMMENT
-- begin TASK_UPDATE
create table task_update (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    task_id uuid not null,
    status varchar(255),
    note varchar(255),
    --
    primary key (ID)
)^
-- end TASK_UPDATE
-- begin BORROWER_WATCHERS
create table BORROWER_WATCHERS (
    borrower_id uuid,
    user_id uuid,
    primary key (borrower_id, user_id)
)^
-- end BORROWER_WATCHERS
-- begin TASK_FLAGGED_USERS
create table TASK_FLAGGED_USERS (
    task_id uuid,
    user_id uuid,
    primary key (task_id, user_id)
)^
-- end TASK_FLAGGED_USERS
-- begin SUBMISSION_TEAM
create table SUBMISSION_TEAM (
    submission_id uuid,
    user_id uuid,
    primary key (submission_id, user_id)
)^
-- end SUBMISSION_TEAM
-- begin SEC_USER
alter table SEC_USER add column PROFILEIMAGE text ^
alter table SEC_USER add column SCOPE varchar(255) ^
alter table SEC_USER add column CAD_LEVEL integer ^
alter table SEC_USER add column EMAIL_VERIFIED boolean ^
alter table SEC_USER add column PREFERENCES jsonb ^
alter table SEC_USER add column INSTITUTION_ID uuid ^
alter table SEC_USER add column LAST_NOTIFIED_TS timestamp ^
alter table SEC_USER add column DTYPE varchar(31) ^
update SEC_USER set DTYPE = 'AppUser' where DTYPE is null ^
-- end SEC_USER
-- begin STEP_TRANSITION
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
    label varchar(255) not null,
    borrower_refresh boolean not null,
    --
    primary key (ID)
)^
-- end STEP_TRANSITION
-- begin WORKFLOW_STEP
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
)^
-- end WORKFLOW_STEP
-- begin WORKFLOW
create table workflow (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    name varchar(255) not null,
    submission_types varchar(255) not null,
    initial_status varchar(255) not null,
    institution_id uuid not null,
    --
    primary key (ID)
)^
-- end WORKFLOW
-- begin ACTIVITY
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
    institution_id uuid not null,
    id_key varchar(255) not null,
    id_value varchar(255) not null,
    details jsonb,
    --
    primary key (ID)
)^
-- end ACTIVITY
-- begin SUBMISSION_FLAGGED_USERS
create table SUBMISSION_FLAGGED_USERS (
    submission_id uuid,
    user_id uuid,
    primary key (submission_id, user_id)
)^
-- end SUBMISSION_FLAGGED_USERS
-- begin TASK_VIEWED_USERS
create table TASK_VIEWED_USERS (
    task_id uuid,
    user_id uuid,
    primary key (task_id, user_id)
)^
-- end TASK_VIEWED_USERS
-- begin SUBMISSION_VIEWED_USERS
create table SUBMISSION_VIEWED_USERS (
    submission_id uuid,
    user_id uuid,
    primary key (submission_id, user_id)
)^
-- end SUBMISSION_VIEWED_USERS
-- begin NOTIFICATION
create table notification (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    text varchar(255) not null,
    payload jsonb,
    institution_id uuid not null,
    type varchar(255) not null,
    --
    primary key (ID)
)^
-- end NOTIFICATION
-- begin SUBSCRIBER
create table subscriber (
    ID uuid,
    --
    is_read boolean,
    is_hidden boolean,
    notification_id uuid not null,
    user_id uuid not null,
    --
    primary key (ID)
)^
-- end SUBSCRIBER
-- begin BORROWER_TEAM
create table BORROWER_TEAM (
    borrower_id uuid,
    user_id uuid,
    primary key (borrower_id, user_id)
)^
-- end BORROWER_TEAM
