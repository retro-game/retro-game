-- @formatter:off

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
