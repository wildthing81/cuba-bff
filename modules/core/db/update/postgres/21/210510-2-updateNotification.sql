alter table notification rename column notification_text to notification_text__u55582 ;
alter table notification alter column notification_text__u55582 drop not null ;
alter table notification add column TEXT varchar(255) ^
update notification set TEXT = '' where TEXT is null ;
alter table notification alter column TEXT set not null ;
