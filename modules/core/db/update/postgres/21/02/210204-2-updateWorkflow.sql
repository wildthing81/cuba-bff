alter table workflow rename column status to status__u04811 ;
alter table workflow alter column status__u04811 drop not null ;
alter table workflow add column INITIAL_STATUS varchar(255) ^
update workflow set INITIAL_STATUS = '' where INITIAL_STATUS is null ;
alter table workflow alter column INITIAL_STATUS set not null ;
