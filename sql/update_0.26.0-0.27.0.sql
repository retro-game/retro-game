alter table users
  add column roles int,
  add column forced_vacation boolean;

update users
set roles = 1, forced_vacation = false;

alter table users
  alter column roles set not null,
  alter column forced_vacation set not null;

create table pranger (
  id bigserial primary key,
  user_id bigint references users not null,
  at timestamptz not null,
  until timestamptz not null,
  reason text not null,
  admin_id bigint references users not null,
  unique (user_id, until)
);

create index pranger_at_idx
          on pranger (at desc);
