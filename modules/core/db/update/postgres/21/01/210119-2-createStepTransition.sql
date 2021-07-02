alter table step_transition add constraint FK_STEP_TRANSITION_ON_STEP foreign key (STEP_ID) references workflow_step(ID);
create index IDX_STEP_TRANSITION_ON_STEP on step_transition (STEP_ID);
