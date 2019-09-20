create table ACCOUNTS
(
    number   varchar primary key,
    user_id  varchar not null,
    currency varchar not null,
    foreign key (user_id) references USERS (id),
    constraint user_account_unique UNIQUE (number, user_id)
);