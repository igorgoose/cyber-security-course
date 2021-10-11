create table client_connections
(
    id         uuid primary key,
    client_id  uuid         not null references client (id),
    session    varchar(256) not null,
    iv         varchar(128) not null,
    expires_at timestamp    not null
);

