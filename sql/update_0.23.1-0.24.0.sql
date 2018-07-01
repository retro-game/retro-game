alter table users
  add column bodies_sort_order integer;
alter table users
  add column bodies_sort_direction integer;

update users
set bodies_sort_order = 0, bodies_sort_direction = 0;

alter table users
  alter column bodies_sort_order set not null;
alter table users
  alter column bodies_sort_direction set not null;