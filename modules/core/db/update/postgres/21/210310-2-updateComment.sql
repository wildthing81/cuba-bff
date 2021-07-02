alter table comment rename column section_id to section_id__u04153 ;
alter table comment alter column section_id__u04153 drop not null ;
-- alter table comment add column SUBMISSION_ID uuid ^
-- update comment set SUBMISSION_ID = <default_value> ;
-- alter table comment alter column SUBMISSION_ID set not null ;
alter table comment add column SUBMISSION_ID uuid not null ;
alter table comment add column TEXT varchar(255) ^
update comment set TEXT = '' where TEXT is null ;
alter table comment alter column TEXT set not null ;
