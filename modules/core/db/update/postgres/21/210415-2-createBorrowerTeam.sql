alter table BORROWER_TEAM add constraint FK_BORTEA_ON_BORROWER foreign key (borrower_id) references borrower(ID);
alter table BORROWER_TEAM add constraint FK_BORTEA_ON_APP_USER foreign key (user_id) references SEC_USER(ID);
