-- begin INSTITUTION
create unique index IDX_institution_UK_site_id on institution (site_id) where DELETE_TS is null ^
-- end INSTITUTION
-- begin SUBMISSION
alter table submission add constraint FK_SUBMISSION_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID)^
alter table submission add constraint FK_SUBMISSION_ON_BORROWER foreign key (BORROWER_ID) references borrower(ID)^
alter table submission add constraint FK_SUBMISSION_ON_WORKFLOW foreign key (WORKFLOW_ID) references workflow(ID)^
create unique index IDX_submission_UK_display_ref on submission (display_ref) where DELETE_TS is null ^
create index IDX_SUBMISSION_ON_INSTITUTION on submission (INSTITUTION_ID)^
create index IDX_SUBMISSION_ON_BORROWER on submission (BORROWER_ID)^
create index IDX_SUBMISSION_ON_WORKFLOW on submission (WORKFLOW_ID)^
-- end SUBMISSION
-- begin TASK
alter table task add constraint FK_TASK_ON_SUBMISSION foreign key (SUBMISSION_ID) references submission(ID)^
alter table task add constraint FK_TASK_ON_ASSIGNED_TO foreign key (ASSIGNED_TO) references SEC_USER(ID)^
create index IDX_TASK_ON_SUBMISSION on task (SUBMISSION_ID)^
create index IDX_TASK_ON_ASSIGNED_TO on task (ASSIGNED_TO)^
-- end TASK
-- begin SUBMISSION_SECTION
alter table submission_section add constraint FK_SUBMISSION_SECTION_ON_SUBMISSION foreign key (SUBMISSION_ID) references submission(ID)^
create index IDX_SUBMISSION_SECTION_ON_SUBMISSION on submission_section (SUBMISSION_ID)^
-- end SUBMISSION_SECTION
-- begin SUBMISSION_PURPOSE
alter table submission_purpose add constraint FK_SUBMISSION_PURPOSE_ON_SUBMISSION foreign key (SUBMISSION_ID) references submission(ID)^
create index IDX_SUBMISSION_PURPOSE_ON_SUBMISSION on submission_purpose (SUBMISSION_ID)^
-- end SUBMISSION_PURPOSE
-- begin BORROWER
alter table borrower add constraint FK_BORROWER_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID)^
create unique index IDX_borrower_UK_name on borrower (name) where DELETE_TS is null ^
create index IDX_BORROWER_ON_INSTITUTION on borrower (INSTITUTION_ID)^
-- end BORROWER
-- begin TASK_UPDATE
alter table task_update add constraint FK_TASK_UPDATE_ON_TASK foreign key (TASK_ID) references task(ID)^
create index IDX_TASK_UPDATE_ON_TASK on task_update (TASK_ID)^
-- end TASK_UPDATE
-- begin BORROWER_WATCHERS
alter table BORROWER_WATCHERS add constraint FK_BORWAT_ON_BORROWER foreign key (borrower_id) references borrower(ID)^
alter table BORROWER_WATCHERS add constraint FK_BORWAT_ON_APP_USER foreign key (user_id) references SEC_USER(ID)^
-- end BORROWER_WATCHERS
-- begin TASK_FLAGGED_USERS
alter table TASK_FLAGGED_USERS add constraint FK_TASFLAUSE_ON_TASK foreign key (task_id) references task(ID)^
alter table TASK_FLAGGED_USERS add constraint FK_TASFLAUSE_ON_USER foreign key (user_id) references SEC_USER(ID)^
-- end TASK_FLAGGED_USERS
-- begin SUBMISSION_TEAM
alter table SUBMISSION_TEAM add constraint FK_SUBTEA_ON_SUBMISSION foreign key (submission_id) references submission(ID)^
alter table SUBMISSION_TEAM add constraint FK_SUBTEA_ON_APP_USER foreign key (user_id) references SEC_USER(ID)^
-- end SUBMISSION_TEAM
-- begin SEC_USER
alter table SEC_USER add constraint FK_SEC_USER_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID)^
create index IDX_SEC_USER_ON_INSTITUTION on SEC_USER (INSTITUTION_ID)^
-- end SEC_USER
-- begin STEP_TRANSITION
alter table step_transition add constraint FK_STEP_TRANSITION_ON_STEP foreign key (STEP_ID) references workflow_step(ID)^
create index IDX_STEP_TRANSITION_ON_STEP on step_transition (STEP_ID)^
-- end STEP_TRANSITION
-- begin WORKFLOW_STEP
alter table workflow_step add constraint FK_WORKFLOW_STEP_ON_WORKFLOW foreign key (WORKFLOW_ID) references workflow(ID)^
create index IDX_WORKFLOW_STEP_ON_WORKFLOW on workflow_step (WORKFLOW_ID)^
-- end WORKFLOW_STEP
-- begin WORKFLOW
alter table workflow add constraint FK_WORKFLOW_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID)^
create unique index IDX_workflow_UK_name on workflow (name) where DELETE_TS is null ^
create index IDX_WORKFLOW_ON_INSTITUTION on workflow (INSTITUTION_ID)^
-- end WORKFLOW
-- begin SUBMISSION_FLAGGED_USERS
alter table SUBMISSION_FLAGGED_USERS add constraint FK_SUBFLAUSE_ON_SUBMISSION foreign key (submission_id) references submission(ID)^
alter table SUBMISSION_FLAGGED_USERS add constraint FK_SUBFLAUSE_ON_USER foreign key (user_id) references SEC_USER(ID)^
-- end SUBMISSION_FLAGGED_USERS
-- begin ACTIVITY
alter table activity add constraint FK_ACTIVITY_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID)^
create index IDX_ACTIVITY_ON_INSTITUTION on activity (INSTITUTION_ID)^
-- end ACTIVITY
-- begin COMMENT
alter table comment add constraint FK_COMMENT_ON_SUBMISSION foreign key (SUBMISSION_ID) references submission(ID)^
create index IDX_COMMENT_ON_SUBMISSION on comment (SUBMISSION_ID)^
-- end COMMENT
-- begin TASK_VIEWED_USERS
alter table TASK_VIEWED_USERS add constraint FK_TASVIEUSE_ON_TASK foreign key (task_id) references task(ID)^
alter table TASK_VIEWED_USERS add constraint FK_TASVIEUSE_ON_USER foreign key (user_id) references SEC_USER(ID)^
-- end TASK_VIEWED_USERS
-- begin SUBMISSION_VIEWED_USERS
alter table SUBMISSION_VIEWED_USERS add constraint FK_SUBVIEUSE_ON_SUBMISSION foreign key (submission_id) references submission(ID)^
alter table SUBMISSION_VIEWED_USERS add constraint FK_SUBVIEUSE_ON_USER foreign key (user_id) references SEC_USER(ID)^
-- end SUBMISSION_VIEWED_USERS
-- begin SUBSCRIBER
alter table subscriber add constraint FK_SUBSCRIBER_ON_NOTIFICATION foreign key (NOTIFICATION_ID) references notification(ID)^
alter table subscriber add constraint FK_SUBSCRIBER_ON_USER foreign key (USER_ID) references SEC_USER(ID)^
create index IDX_SUBSCRIBER_ON_NOTIFICATION on subscriber (NOTIFICATION_ID)^
create index IDX_SUBSCRIBER_ON_USER on subscriber (USER_ID)^
-- end SUBSCRIBER
-- begin NOTIFICATION
alter table notification add constraint FK_NOTIFICATION_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID)^
create index IDX_NOTIFICATION_ON_INSTITUTION on notification (INSTITUTION_ID)^
-- end NOTIFICATION
-- begin BORROWER_TEAM
alter table BORROWER_TEAM add constraint FK_BORTEA_ON_BORROWER foreign key (borrower_id) references borrower(ID)^
alter table BORROWER_TEAM add constraint FK_BORTEA_ON_APP_USER foreign key (user_id) references SEC_USER(ID)^
-- end BORROWER_TEAM
