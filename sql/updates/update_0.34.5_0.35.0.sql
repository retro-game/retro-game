-- @formatter:off

begin;

-- Records

create table records (
    key text primary key,
    value bigint not null,
    at timestamptz not null,
    holders bigint[] not null
);

commit;
