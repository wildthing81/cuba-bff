alter table notification add constraint FK_NOTIFICATION_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID);
create index IDX_NOTIFICATION_ON_INSTITUTION on notification (INSTITUTION_ID);
alter table notification rename column type to type__u85552 ;
alter table notification alter column type__u85552 drop not null ;
alter table notification add column NOTIFICATION_TEXT varchar(255) ^
update notification set NOTIFICATION_TEXT = '' where NOTIFICATION_TEXT is null ;
alter table notification alter column NOTIFICATION_TEXT set not null ;

