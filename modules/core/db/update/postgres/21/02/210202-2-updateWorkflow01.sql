alter table workflow add column SUBMISSION_TYPES varchar(255) ^
update workflow set SUBMISSION_TYPES = '' where SUBMISSION_TYPES is null ;
alter table workflow alter column SUBMISSION_TYPES set not null ;
