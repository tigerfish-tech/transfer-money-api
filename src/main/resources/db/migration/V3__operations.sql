create table OPERATIONS
(
    id      bigint auto_increment primary key,
    account varchar not null,
    debit   double,
    credit  double,
);