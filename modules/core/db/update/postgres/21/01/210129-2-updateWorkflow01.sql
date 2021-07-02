alter table workflow add constraint FK_WORKFLOW_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID);
create index IDX_WORKFLOW_ON_INSTITUTION on workflow (INSTITUTION_ID);
