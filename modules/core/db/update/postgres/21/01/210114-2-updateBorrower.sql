-- alter table borrower add column INSTITUTION_ID uuid ^
-- update borrower set INSTITUTION_ID = <default_value> ;
-- alter table borrower alter column INSTITUTION_ID set not null ;
alter table borrower add column INSTITUTION_ID uuid not null ;
