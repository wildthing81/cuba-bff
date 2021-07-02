alter table activity add constraint FK_ACTIVITY_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID);
create index IDX_ACTIVITY_ON_INSTITUTION on activity (INSTITUTION_ID);
