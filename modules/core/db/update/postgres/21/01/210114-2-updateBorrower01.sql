alter table borrower add constraint FK_BORROWER_ON_INSTITUTION foreign key (INSTITUTION_ID) references institution(ID);
create index IDX_BORROWER_ON_INSTITUTION on borrower (INSTITUTION_ID);
