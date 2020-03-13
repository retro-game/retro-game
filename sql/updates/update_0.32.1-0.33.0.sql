-- @formatter:off

begin;

alter table bodies add column buildings int[] default array_fill(0, array[18]) check (array_length(buildings, 1) = 18);

update bodies
   set buildings = tmp.buildings
  from (
    select bodies.id,
           (select array(
                select coalesce(buildings.level, 0)
                  from generate_series(0, 17) as k
             left join buildings
                    on buildings.kind = k
                   and buildings.body_id = bodies.id
              order by k)) as buildings
      from bodies) tmp
 where bodies.id = tmp.id;

alter table bodies alter column buildings set not null;

drop table buildings;

commit;
