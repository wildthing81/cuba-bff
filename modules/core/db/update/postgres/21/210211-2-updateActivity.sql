-- alter table activity add column INSTITUTION_ID uuid ^
-- update activity set INSTITUTION_ID = <default_value> ;
-- alter table activity alter column INSTITUTION_ID set not null ;
alter table activity add column INSTITUTION_ID uuid not null ;
