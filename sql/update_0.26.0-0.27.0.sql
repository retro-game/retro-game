alter table users
  add column roles int;

update users
set roles = 1; -- USER

alter table users
  alter column roles set not null;