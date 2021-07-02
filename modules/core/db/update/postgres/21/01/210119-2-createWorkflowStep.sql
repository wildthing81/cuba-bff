alter table workflow_step add constraint FK_WORKFLOW_STEP_ON_WORKFLOW foreign key (WORKFLOW_ID) references workflow(ID);
create index IDX_WORKFLOW_STEP_ON_WORKFLOW on workflow_step (WORKFLOW_ID);
