-- alter table notification add column INSTITUTION_ID uuid ^
-- update notification set INSTITUTION_ID = <default_value> ;
-- alter table notification alter column INSTITUTION_ID set not null ;
alter table notification add column INSTITUTION_ID uuid not null ;
