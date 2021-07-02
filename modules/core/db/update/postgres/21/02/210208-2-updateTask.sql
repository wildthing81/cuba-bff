alter table task rename column note to note__u83607 ;
alter table task add column DESCRIPTION text ;
alter table task alter column CATEGORY set data type varchar(255) ;
