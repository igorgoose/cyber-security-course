create table server_connections
(
    id             uuid primary key,
    user_server_id uuid         not null references user_servers (id),
    session        varchar(256) not null,
    iv             varchar(128) not null,
    expires_at     timestamp    not null
);

alter table user_servers drop column connected;
alter table user_servers drop column session;
alter table user_servers drop column iv;