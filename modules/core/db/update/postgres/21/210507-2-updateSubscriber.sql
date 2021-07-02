alter table subscriber rename column is_hidden to is_hidden__u19262 ;
alter table subscriber rename column is_read to is_read__u83454 ;
alter table subscriber add column IS_READ boolean ;
alter table subscriber add column IS_HIDDEN boolean ;
