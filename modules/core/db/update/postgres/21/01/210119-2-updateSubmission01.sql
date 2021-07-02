alter table submission add constraint FK_SUBMISSION_ON_WORKFLOW foreign key (WORKFLOW_ID) references workflow(ID);
create index IDX_SUBMISSION_ON_WORKFLOW on submission (WORKFLOW_ID);
