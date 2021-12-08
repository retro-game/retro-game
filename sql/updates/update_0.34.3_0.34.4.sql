begin;

alter table bodies add column shipyard_start_at timestamptz;

update bodies b
   set shipyard_start_at = tmp.at
  from (
    select e.param as body_id, e.at
      from events e
     where e.kind = 1
  ) tmp
 where b.id = tmp.body_id;

delete from events where kind = 1;

commit;
