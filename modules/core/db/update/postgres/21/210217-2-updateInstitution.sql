alter table institution add column BORROWERDEFAULTS jsonb ;
update institution set BORROWERDEFAULTS = '{}' where BORROWERDEFAULTS is null;
alter table institution alter column BORROWERDEFAULTS set not null ;
