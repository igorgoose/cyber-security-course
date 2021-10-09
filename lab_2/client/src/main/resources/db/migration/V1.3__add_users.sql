create table users
(
    id       uuid primary key,
    username varchar(64) not null unique,
    password text not null
);