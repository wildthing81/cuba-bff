alter table step_transition rename column button_label to button_label__u56201 ;
alter table step_transition alter column button_label__u56201 drop not null ;
alter table step_transition add column LABEL varchar(255) ^
update step_transition set LABEL = '' where LABEL is null ;
alter table step_transition alter column LABEL set not null ;
