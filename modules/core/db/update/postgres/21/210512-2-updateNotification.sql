alter table notification add column TYPE varchar(255) ^
update notification set TYPE = '' where TYPE is null ;
alter table notification alter column TYPE set not null ;
