-- @formatter:off

begin;

-- Flight view

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

-- Fix event is not present

delete from building_queue
      where body_id in (
        select distinct bq.body_id
                   from building_queue bq
                  where not exists (select 1 from events e where e.param = bq.body_id and e.kind = 0)
      );

delete from shipyard_queue
      where body_id in (
        select distinct sq.body_id
                   from shipyard_queue sq
                  where not exists (select 1 from events e where e.param = sq.body_id and e.kind = 1)
      );

delete from technology_queue
      where user_id in (
        select distinct tq.user_id
                   from technology_queue tq
                  where not exists (select 1 from events e where e.param = tq.user_id and e.kind = 2)
      );

-- Building queue

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

-- Shipyard queue

alter table bodies add column shipyard_queue int[];

update bodies
   set shipyard_queue = tmp3.q
  from (
      select body_id,
             array_agg(i) as q
        from (
          select body_id,
                 unnest(a) as i
            from (
                select body_id,
                       array[kind, count] as a
                  from shipyard_queue
              order by body_id, sequence
            ) tmp
        ) tmp2
    group by body_id
  ) tmp3
where bodies.id = tmp3.body_id;

update bodies
   set shipyard_queue = '{}'::int[]
 where shipyard_queue is null;

alter table bodies alter column shipyard_queue set not null;

drop table shipyard_queue;

-- Technology queue

alter table users add column technology_queue bigint[];

update users
   set technology_queue = tmp3.q
  from (
      select user_id,
             array_agg(i) as q
        from (
          select user_id,
                 unnest(a) as i
            from (
                select user_id,
                       array[sequence, kind, body_id] as a
                  from technology_queue
              order by user_id, sequence
            ) tmp
        ) tmp2
    group by user_id
  ) tmp3
where users.id = tmp3.user_id;

update users
   set technology_queue = '{}'::bigint[]
 where technology_queue is null;

alter table users alter column technology_queue set not null;

drop table technology_queue;

commit;
