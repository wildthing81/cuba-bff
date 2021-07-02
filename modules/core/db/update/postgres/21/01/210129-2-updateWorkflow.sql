alter table workflow rename column type to type__u23793 ;
alter table workflow alter column type__u23793 drop not null ;
-- alter table workflow add column INSTITUTION_ID uuid ^
-- update workflow set INSTITUTION_ID = <default_value> ;
-- alter table workflow alter column INSTITUTION_ID set not null ;
alter table workflow add column INSTITUTION_ID uuid not null ;

