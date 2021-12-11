begin;

-- Fix building queue

insert into events (at, kind, param)
select now(), 0, b.id
from bodies b
where b.building_queue != '{}'
  and not exists (select 1 from events e where e.kind = 0 and e.param = b.id);

-- Fix technology queue

insert into events (at, kind, param)
select now(), 2, u.id
from users u
where u.technology_queue != '{}'
        and not exists (select 1 from events e where e.kind = 2 and e.param = u.id);

commit;
