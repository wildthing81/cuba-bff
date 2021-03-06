alter table borrower add column ANZSIC varchar(255) ;
alter table borrower add column CAD_LEVEL integer ;
alter table borrower add column REGULATORY_REQUIREMENTS varchar(255) ;
alter table borrower add column LAST_SCHEDULE_REVIEW_TS timestamp ;
alter table borrower add column CUSTOMER_TYPE varchar(255) ;
alter table borrower add column NEXT_SCHEDULE_REVIEW_TS timestamp ;
alter table borrower add column BUSINESS_UNIT varchar(255) ;
alter table borrower add column RISK_SIGN_OFF varchar(255) ;
alter table borrower add column EXTERNAL_RATING_AND_OUTLOOK integer ;
alter table borrower add column MARKET_CAP bigint ;
alter table borrower add column SECURITY_INDEX varchar(255) ;
alter table borrower add column CCR_RISK_SCORE integer ;
alter table borrower add column CUSTOMER_GROUP varchar(255) ;
alter table borrower add column LAST_FULL_REVIEW_TS timestamp ;
