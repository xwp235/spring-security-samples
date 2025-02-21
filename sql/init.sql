create table users(
                      username varchar(50) primary key,
                      password varchar(500) not null,
                      enabled boolean not null
);

create table authorities (
                             username varchar(50) not null,
                             authority varchar(50) not null,
                             constraint fk_authorities_users foreign key(username) references users(username)
);
create unique index ix_auth_username on authorities (username,authority);

create table groups (
                        id bigserial primary key,
                        group_name varchar(50) not null
);

create table group_authorities (
                                   group_id bigint not null,
                                   authority varchar(50) not null,
                                   constraint fk_group_authorities_group foreign key(group_id) references groups(id)
);

create table group_members (
                               id bigserial primary key,
                               username varchar(50) not null,
                               group_id bigint not null,
                               constraint fk_group_members_group foreign key(group_id) references groups(id)
);

create table persistent_logins (
                                   series varchar(64) primary key,
                                   username varchar(64) not null,
                                   token varchar(64) not null,
                                   last_used timestamp not null
);
