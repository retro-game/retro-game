-- @formatter:off

begin;

-- Records

create table records (
    key text primary key,
    value bigint not null,
    at timestamptz not null,
    holders bigint[] not null
);

-- Main target

alter table flights add column main_target int;

commit;
