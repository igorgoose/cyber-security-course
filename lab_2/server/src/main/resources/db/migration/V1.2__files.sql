create table file
(
    id         uuid primary key,
    name       varchar(100) not null,
    client_id  uuid         not null references client (id),
    updated_on timestamp    not null default current_timestamp
);