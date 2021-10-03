create table client(
    id uuid primary key,
    session varchar(256) not null,
    session_expires_at timestamp not null
);