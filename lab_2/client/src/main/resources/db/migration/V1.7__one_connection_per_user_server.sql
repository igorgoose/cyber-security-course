alter table server_connections add constraint uq_user_server unique (user_server_id);

alter table user_servers add column namespace_created bool not null default true;