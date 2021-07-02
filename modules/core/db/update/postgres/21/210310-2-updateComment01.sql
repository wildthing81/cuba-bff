alter table comment add constraint FK_COMMENT_ON_SUBMISSION foreign key (SUBMISSION_ID) references submission(ID);
create index IDX_COMMENT_ON_SUBMISSION on comment (SUBMISSION_ID);
