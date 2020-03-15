-- @formatter:off

begin;

alter table bodies add column buildings int[]  check (array_length(buildings, 1) = 18);
alter table bodies add column units int[] check (array_length(units, 1) = 23);
alter table flights add column units int[] check (array_length(units, 1) = 23);

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

update bodies
   set units = tmp.units
  from (
    select bodies.id,
           (select array(
                select coalesce(body_units.count, 0)
                  from generate_series(0, 22) as k
             left join body_units
                    on body_units.kind = k
                   and body_units.body_id = bodies.id
              order by k)) as units
      from bodies) tmp
 where bodies.id = tmp.id;

update flights
   set units = tmp.units
  from (
    select flights.id,
           (select array(
                select coalesce(flight_units.count, 0)
                  from generate_series(0, 22) as k
             left join flight_units
                    on flight_units.kind = k
                   and flight_units.flight_id = flights.id
              order by k)) as units
    from flights) tmp
where flights.id = tmp.id;

alter table bodies alter column buildings set not null;
alter table bodies alter column units set not null;
alter table flights alter column units set not null;

drop table buildings;
drop table body_units;
drop table flight_units;

drop view flight_view;

create view flight_view as (
     select f.id,
            f.start_user_id,
            su.name as start_user_name,
            f.start_body_id,
            sb.galaxy as start_galaxy,
            sb.system as start_system,
            sb.position as start_position,
            sb.kind as start_kind,
            sb.name as start_body_name,
            f.target_user_id,
            tu.name as target_user_name,
            f.target_body_id,
            tb.name as target_body_name,
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
       join users su
         on su.id = f.start_user_id
       join bodies sb
         on sb.id = f.start_body_id
  left join users tu
         on tu.id = f.target_user_id
  left join bodies tb
         on tb.id = f.target_body_id
);

commit;
