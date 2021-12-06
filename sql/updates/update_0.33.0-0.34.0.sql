-- @formatter:off

begin;

drop view flight_view;

create view flight_view as (
  select f.id,
         f.start_user_id,
         f.start_body_id,
         sb.galaxy as start_galaxy,
         sb.system as start_system,
         sb.position as start_position,
         sb.kind as start_kind,
         sb.name as start_body_name,
         f.target_user_id,
         f.target_body_id,
         f.target_galaxy,
         f.target_system,
         f.target_position,
         f.target_kind,
         f.party_id,
         f.departure_at,
         f.arrival_at,
         f.return_at,
         f.hold_until,
         f.mission,
         f.metal,
         f.crystal,
         f.deuterium,
         f.units
    from flights f
    join bodies sb
      on sb.id = f.start_body_id
);

alter table bodies add column building_queue int[];

update bodies
   set building_queue = tmp3.q
  from (
      select body_id,
             array_agg(i) as q
        from (
          select body_id,
                 unnest(a) as i
            from (
                select body_id,
                       array[sequence, kind, action] as a
                  from building_queue
              order by body_id, sequence
            ) tmp
        ) tmp2
    group by body_id
  ) tmp3
where bodies.id = tmp3.body_id;

update bodies
   set building_queue = '{}'::int[]
 where building_queue is null;

alter table bodies alter column building_queue set not null;

drop table building_queue;

commit;
