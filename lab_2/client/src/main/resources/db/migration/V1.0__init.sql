create table server_data
(
    id         serial primary key,
    client_id  varchar(256) not null,
    session    varchar(256) not null,
    updated_on timestamp    not null
);