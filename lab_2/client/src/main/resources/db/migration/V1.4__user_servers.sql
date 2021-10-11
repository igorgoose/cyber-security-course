create extension if not exists "uuid-ossp";
alter table server_data
    drop column status;
alter table server_data
    drop column session;
alter table server_data
    drop column iv;
alter table server_data
    add column ip varchar(15) not null default 'localhost';
alter table server_data
    add column port varchar(5) not null default '8084';
alter table server_data
    add constraint uniq_ip_port unique (ip, port);
alter table server_data
    drop column id;
alter table server_data
    add column id uuid primary key default uuid_generate_v4();

create table user_servers
(
    id             uuid primary key,
    user_id        uuid         not null references users (id),
    server_data_id uuid         not null references server_data (id),
    name           varchar(50)  not null,
    connected      boolean      not null,
    session        varchar(256) not null,
    iv             varchar(128) not null,
    unique (user_id, server_data_id)
);