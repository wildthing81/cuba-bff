alter table subscriber add constraint FK_SUBSCRIBER_ON_NOTIFICATION foreign key (NOTIFICATION_ID) references notification(ID);
alter table subscriber add constraint FK_SUBSCRIBER_ON_USER foreign key (USER_ID) references SEC_USER(ID);
create index IDX_SUBSCRIBER_ON_NOTIFICATION on subscriber (NOTIFICATION_ID);
create index IDX_SUBSCRIBER_ON_USER on subscriber (USER_ID);
