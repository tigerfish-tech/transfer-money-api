create table TRANSFERS
(
    id      bigint auto_increment primary key,
    created timestamp not null DEFAULT CURRENT_TIMESTAMP
);

alter table OPERATIONS
    add column created timestamp not null DEFAULT CURRENT_TIMESTAMP;

alter table ACCOUNTS
    add column created timestamp not null DEFAULT CURRENT_TIMESTAMP;

alter table USERS
    add column created timestamp not null DEFAULT CURRENT_TIMESTAMP;

create table TRANSFER_OPERATIONS
(
    transfer_id  bigint not null,
    operation_id bigint not null,
    constraint transfer_operation_unique UNIQUE (transfer_id, operation_id)
);