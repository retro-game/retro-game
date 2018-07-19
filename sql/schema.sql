-- @formatter:off

create domain user_name as text check (
  char_length(value) between 3 and 16 and value ~ '^[[:alnum:]]+( ?[[:alnum:]])*$'
);

create domain body_name as text check (
  char_length(value) between 1 and 16 and value ~ '^[0-9A-Za-z\-._]+( ?[0-9A-Za-z\-._])*$'
);

-- Users

create table users (
  id bigserial primary key,
  name user_name not null,
  email text not null,
  password text not null,
  messages_seen_at timestamptz not null,
  combat_reports_seen_at timestamptz not null,
  espionage_reports_seen_at timestamptz not null,
  harvest_reports_seen_at timestamptz not null,
  transport_reports_seen_at timestamptz not null,
  other_reports_seen_at timestamptz not null,
  language text not null,
  skin text not null,
  num_probes integer not null,
  bodies_sort_order integer not null,
  bodies_sort_direction integer not null,
  flags integer not null
);

create unique index users_upper_name_idx on users (upper(name) text_pattern_ops);
create unique index users_upper_email_idx on users (upper(email) text_pattern_ops);

-- Bodies

create table bodies (
  id bigserial primary key,
  user_id bigint references users,
  galaxy int not null check (galaxy between 1 and 5),
  system int not null check (system between 1 and 500),
  position int not null check (position between 1 and 15),
  kind int not null check (kind between 0 and 1),
  name body_name,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  diameter int not null check (diameter > 0),
  temperature int not null check (temperature between -60 and 120),
  type int not null,
  image int not null,
  metal double precision not null check (metal >= 0),
  crystal double precision not null check (crystal >= 0),
  deuterium double precision not null check (deuterium >= 0),
  metal_mine_factor int not null check (metal_mine_factor between 0 and 10),
  crystal_mine_factor int not null check (crystal_mine_factor between 0 and 10),
  deuterium_synthesizer_factor int not null check (deuterium_synthesizer_factor between 0 and 10),
  solar_plant_factor int not null check (solar_plant_factor between 0 and 10),
  fusion_reactor_factor int not null check (fusion_reactor_factor between 0 and 10),
  solar_satellites_factor int not null check (solar_satellites_factor between 0 and 10),
  last_jump_at timestamptz,
  unique (galaxy, system, position, kind)
);

create index bodies_user_id_idx on bodies (user_id);
create index bodies_upper_name_idx on bodies (upper(name) text_pattern_ops);

-- Buildings

create table buildings (
  body_id bigint references bodies not null,
  kind int not null,
  level int not null check (level >= 1),
  primary key (body_id, kind)
);

-- Building queue

create table building_queue (
  body_id bigint references bodies not null,
  sequence int not null,
  kind int not null,
  action int not null,
  primary key (body_id, sequence)
);

-- Technologies

create table technologies (
  user_id bigint references users not null,
  kind int not null,
  level int not null check (level >= 1),
  primary key (user_id, kind)
);

-- Technology queue

create table technology_queue (
  user_id bigint references users not null,
  sequence int not null,
  body_id bigint references bodies not null,
  kind int not null,
  primary key (user_id, sequence)
);

-- Body units (fleet and defense)

create table body_units (
  body_id bigint references bodies not null,
  kind int not null,
  count int not null check (count >= 1),
  primary key (body_id, kind)
);

-- Shipyard queue

create table shipyard_queue (
  body_id bigint references bodies not null,
  sequence int not null,
  kind int not null,
  count int not null,
  primary key (body_id, sequence)
);

-- Parties

create table parties (
  id bigserial primary key,
  owner_id bigint references users not null,
  target_user_id bigint references users not null,
  target_body_id bigint references bodies not null,
  target_galaxy int not null check (target_galaxy between 1 and 5),
  target_system int not null check (target_system between 1 and 500),
  target_position int not null check (target_position between 1 and 15),
  target_kind int not null,
  mission int not null
);

create table party_users (
  party_id bigint references parties not null,
  user_id bigint references users not null,
  primary key (party_id, user_id)
);

-- Flights

create table flights (
  id bigserial primary key,
  start_user_id bigint references users not null,
  start_body_id bigint references bodies not null,
  target_user_id bigint references users,
  target_body_id bigint references bodies,
  target_galaxy int not null check (target_galaxy between 1 and 5),
  target_system int not null check (target_system between 1 and 500),
  target_position int not null check (target_position between 1 and 15),
  target_kind int,
  party_id bigint references parties,
  departure_at timestamptz not null,
  arrival_at timestamptz,
  return_at timestamptz not null,
  hold_until timestamptz,
  mission int not null,
  metal double precision not null check (metal >= 0),
  crystal double precision not null check (crystal >= 0),
  deuterium double precision not null check (deuterium >= 0)
);

create index flights_start_user_id_idx on flights (start_user_id);
create index flights_start_body_id_idx on flights (start_body_id);
create index flights_target_user_id_idx on flights (target_user_id);
create index flights_target_body_id_idx on flights (target_body_id);
create index flights_target_coordinates_idx on flights (target_galaxy, target_system, target_position, target_kind);
create index flights_party_id_idx on flights (party_id);

-- Flight units

create table flight_units (
  flight_id bigint references flights not null,
  kind int not null,
  count int not null check (count >= 1),
  primary key (flight_id, kind)
);

-- Debris fields

create table debris_fields (
  galaxy int not null check (galaxy between 1 and 5),
  system int not null check (system between 1 and 500),
  position int not null check (position between 1 and 15),
  created_at timestamptz not null,
  updated_at timestamptz not null,
  metal bigint not null check (metal >= 0),
  crystal bigint not null check (crystal >= 0),
  primary key (galaxy, system, position)
);

-- Events

create table events (
  id bigserial primary key,
  at timestamptz not null,
  kind int not null,
  param bigint
);

create index events_at_id_idx
          on events (at, id);
create index events_kind_param_idx
          on events (kind, param);

-- Combat reports

create table combat_reports (
  id bigserial primary key,
  token bytea not null,
  at timestamptz not null,
  result int not null check (result between 0 and 2),
  attackers_loss bigint not null check (attackers_loss >= 0),
  defenders_loss bigint not null check (defenders_loss >= 0),
  plunder_metal double precision not null check (plunder_metal >= 0),
  plunder_crystal double precision not null check (plunder_crystal >= 0),
  plunder_deuterium double precision not null check (plunder_deuterium >= 0),
  debris_metal bigint not null check (debris_metal >= 0),
  debris_crystal bigint not null check (debris_crystal >= 0),
  moon_chance real not null,
  moon_given boolean not null,
  seed int not null,
  execution_time bigint not null,
  data bytea not null
);

-- Simplified combat reports

create table simplified_combat_reports (
  id bigserial primary key,
  user_id bigint references users on delete cascade not null,
  deleted boolean not null,
  at timestamptz not null,
  enemy_id bigint references users on delete set null,
  enemy_name user_name not null,
  galaxy int not null check (galaxy between 1 and 5),
  system int not null check (system between 1 and 500),
  position int not null check (position between 1 and 15),
  kind int not null check (kind between 0 and 1),
  result int not null check (result between 0 and 2),
  attackers_loss bigint not null check (attackers_loss >= 0),
  defenders_loss bigint not null check (defenders_loss >= 0),
  plunder_metal double precision not null check (plunder_metal >= 0),
  plunder_crystal double precision not null check (plunder_crystal >= 0),
  plunder_deuterium double precision not null check (plunder_deuterium >= 0),
  debris_metal bigint not null check (debris_metal >= 0),
  debris_crystal bigint not null check (debris_crystal >= 0),
  moon_chance real not null,
  moon_given boolean not null,
  combat_report_id bigint references combat_reports on delete cascade,
  token bytea
);

create index simplified_combat_reports_user_id_deleted_at_idx
          on simplified_combat_reports (user_id, deleted, at desc);
create index simplified_combat_reports_user_id_deleted_enemy_name_idx
          on simplified_combat_reports (user_id, deleted, enemy_name);
create index simplified_combat_reports_user_id_deleted_coordinates_idx
          on simplified_combat_reports (user_id, deleted, galaxy, system, position, kind);
create index simplified_combat_reports_user_id_deleted_result_idx
          on simplified_combat_reports (user_id, deleted, result);
create index simplified_combat_reports_user_id_deleted_attackers_loss_idx
          on simplified_combat_reports (user_id, deleted, attackers_loss desc);
create index simplified_combat_reports_user_id_deleted_defenders_loss_idx
          on simplified_combat_reports (user_id, deleted, defenders_loss desc);
create index simplified_combat_reports_user_id_deleted_plunder_idx
          on simplified_combat_reports (user_id, deleted, (plunder_metal + plunder_crystal + plunder_deuterium) desc);
create index simplified_combat_reports_user_id_deleted_debris_idx
          on simplified_combat_reports (user_id, deleted, (debris_metal + debris_crystal) desc);
create index simplified_combat_reports_user_id_deleted_moon_chance_idx
          on simplified_combat_reports (user_id, deleted, moon_chance desc);
create index simplified_combat_reports_enemy_id_idx
          on simplified_combat_reports (enemy_id);

-- Espionage reports

create table espionage_reports (
  id bigserial primary key,
  user_id bigint references users on delete cascade not null,
  deleted boolean not null,
  at timestamptz not null,
  enemy_id bigint references users on delete set null,
  enemy_name user_name not null,
  galaxy int not null check (galaxy between 1 and 5),
  system int not null check (system between 1 and 500),
  position int not null check (position between 1 and 15),
  kind int not null check (kind between 0 and 1),
  activity int not null check (activity between 0 and 60),
  metal double precision not null check (metal >= 0),
  crystal double precision not null check (crystal >= 0),
  deuterium double precision not null check (deuterium >= 0),
  fleet bigint check (fleet >= 0),
  defense bigint check (defense >= 0),
  diameter int not null check (diameter > 0),
  counter_chance real not null check (counter_chance between 0 and 1),
  token bytea not null,
  data bytea not null
);

create index espionage_reports_user_id_deleted_at_idx
          on espionage_reports (user_id, deleted, at desc);
create index espionage_reports_user_id_deleted_enemy_name_idx
          on espionage_reports (user_id, deleted, enemy_name);
create index espionage_reports_user_id_deleted_coordinates_idx
          on espionage_reports (user_id, deleted, galaxy, system, position, kind);
create index espionage_reports_user_id_deleted_activity_idx
          on espionage_reports (user_id, deleted, activity);
create index espionage_reports_user_id_deleted_resources_idx
          on espionage_reports (user_id, deleted, (metal + crystal + deuterium) desc);
create index espionage_reports_user_id_deleted_fleet_idx
          on espionage_reports (user_id, deleted, fleet desc);
create index espionage_reports_user_id_deleted_defense_idx
          on espionage_reports (user_id, deleted, defense desc);
create index espionage_reports_enemy_id_idx
          on espionage_reports (enemy_id);

-- Harvest reports

create table harvest_reports (
  id bigserial primary key,
  user_id bigint references users on delete cascade not null,
  deleted boolean not null,
  at timestamptz not null,
  galaxy int not null check (galaxy between 1 and 5),
  system int not null check (system between 1 and 500),
  position int not null check (position between 1 and 15),
  kind int not null check (kind = 2),
  num_recyclers int not null check (num_recyclers >= 1),
  capacity bigint not null check (capacity >= 0),
  harvested_metal bigint not null check (harvested_metal >= 0),
  harvested_crystal bigint not null check (harvested_crystal >= 0),
  remaining_metal bigint not null check (remaining_metal >= 0),
  remaining_crystal bigint not null check (remaining_crystal >= 0)
);

create index harvest_reports_user_id_deleted_at_idx
          on harvest_reports (user_id, deleted, at desc);
create index harvest_reports_user_id_deleted_coordinates_idx
          on harvest_reports (user_id, deleted, galaxy, system, position, kind);
create index harvest_reports_user_id_deleted_num_recyclers_idx
          on harvest_reports (user_id, deleted, num_recyclers desc);
create index harvest_reports_user_id_deleted_capacity_idx
          on harvest_reports (user_id, deleted, capacity desc);
create index harvest_reports_user_id_deleted_harvested_resources_idx
          on harvest_reports (user_id, deleted, (harvested_metal + harvested_crystal) desc);
create index harvest_reports_user_id_deleted_remaining_resources_idx
          on harvest_reports (user_id, deleted, (remaining_metal + remaining_crystal) desc);

-- Transport reports

create table transport_reports (
  id bigserial primary key,
  user_id bigint references users on delete cascade not null,
  deleted boolean not null,
  at timestamptz not null,
  kind int not null check (kind between 0 and 2),
  partner_id bigint references users on delete set null,
  partner_name text not null,
  start_galaxy int not null check (start_galaxy between 1 and 5),
  start_system int not null check (start_system between 1 and 500),
  start_position int not null check (start_position between 1 and 15),
  start_kind int not null check (start_kind between 0 and 1),
  target_galaxy int not null check (target_galaxy between 1 and 5),
  target_system int not null check (target_system between 1 and 500),
  target_position int not null check (target_position between 1 and 15),
  target_kind int not null check (target_kind between 0 and 1),
  metal double precision not null check (metal >= 0),
  crystal double precision not null check (crystal >= 0),
  deuterium double precision not null check (deuterium >= 0)
);

create index transport_reports_user_id_deleted_at_idx
          on transport_reports (user_id, deleted, at desc);
create index transport_reports_user_id_deleted_partner_name_idx
          on transport_reports (user_id, deleted, partner_name);
create index transport_reports_user_id_deleted_kind_idx
          on transport_reports (user_id, deleted, kind);
create index transport_reports_user_id_deleted_start_coordinates_idx
          on transport_reports (user_id, deleted, start_galaxy, start_system, start_position, start_kind);
create index transport_reports_user_id_deleted_target_coordinates_idx
          on transport_reports (user_id, deleted, target_galaxy, target_system, target_position, target_kind);
create index transport_reports_user_id_deleted_resources_idx
          on transport_reports (user_id, deleted, (metal + crystal + deuterium) desc);
create index transport_reports_partner_id_idx
          on transport_reports (partner_id);

-- Other reports

create table other_reports (
  id bigserial primary key,
  user_id bigint references users on delete cascade not null,
  deleted boolean not null,
  at timestamptz not null,
  kind int not null,
  start_galaxy int not null check (start_galaxy between 1 and 5),
  start_system int not null check (start_system between 1 and 500),
  start_position int not null check (start_position between 1 and 15),
  start_kind int not null check (start_kind between 0 and 1),
  target_galaxy int not null check (target_galaxy between 1 and 5),
  target_system int not null check (target_system between 1 and 500),
  target_position int not null check (target_position between 1 and 15),
  target_kind int not null check (target_kind between 0 and 2),
  metal double precision check (metal >= 0),
  crystal double precision check (crystal >= 0),
  deuterium double precision check (deuterium >= 0),
  param double precision
);

create index other_reports_user_id_deleted_at_idx
          on other_reports (user_id, deleted, at desc);

-- Reports trigger on user name updates

create function reports_update_target_user_name() returns trigger as $$
begin
  update simplified_combat_reports
     set enemy_name = NEW.name
   where enemy_id = NEW.id;

  update espionage_reports
     set enemy_name = NEW.name
   where enemy_id = NEW.id;

  update transport_reports
     set partner_name = NEW.name
   where partner_id = NEW.id;

  return null;
end;
$$ language plpgsql;

create trigger reports_update_target_user_name_trigger
  after update on users
  for each row
  when (OLD.name != NEW.name)
  execute procedure reports_update_target_user_name();

-- Messages

create table messages (
  id bigserial primary key,
  recipient_id bigint references users on delete cascade,
  deleted boolean not null,
  at timestamptz not null,
  sender_id bigint references users on delete set null,
  message text not null
);

create index messages_recipient_id_deleted_at_idx
          on messages (recipient_id, deleted, at desc);

-- Points

create function create_points(kind text) returns void as $$
begin
  execute '
    create table ' || kind || '_points (
      user_id int references users not null,
      at timestamptz not null,
      points int not null,
      primary key (user_id, at)
    )';
end
$$ language plpgsql;

select create_points('buildings');
select create_points('technologies');
select create_points('fleet');
select create_points('defense');

drop function create_points(kind text);

-- Rankings

create function create_ranking(kind text) returns void as $$
begin
  execute '
    create materialized view ' || kind || '_ranking as (
      select u.id as user_id,
             u.name,
             (rank() over (order by p.points desc)) as rank,
             p.points
        from users u
        join ' || kind || '_points p
          on p.user_id = u.id
       where p.at = (select max(at) from ' || kind || '_points)
    )';
  execute 'create index on ' || kind || '_ranking (rank)';
end
$$ language plpgsql;

select create_ranking('buildings');
select create_ranking('technologies');
select create_ranking('fleet');
select create_ranking('defense');

drop function create_ranking(kind text);

create materialized view overall_ranking as (
  select u.id as user_id,
         u.name,
         (rank() over (order by br.points + tr.points + fr.points + dr.points desc)) as rank,
         br.points + tr.points + fr.points + dr.points as points
    from users u
    join buildings_ranking br
      on br.user_id = u.id
    join technologies_ranking tr
      on tr.user_id = u.id
    join fleet_ranking fr
      on fr.user_id = u.id
    join defense_ranking dr
      on dr.user_id = u.id
);

create index on overall_ranking (rank);

-- Flight view

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
            f.deuterium
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

-- Galaxy

create view galaxy as (
     select p.galaxy,
            p.system,
            p.position,
            u.id as user_id,
            u.name as user_name,
            p.id as planet_id,
            p.name as planet_name,
            p.type as planet_type,
            p.image as planet_image,
            m.id as moon_id,
            m.name as moon_name,
            m.image as moon_image,
            df.metal as debris_metal,
            df.crystal as debris_crystal
       from bodies p
       join users u
         on u.id = p.user_id
  left join bodies m
         on m.galaxy = p.galaxy
        and m.system = p.system
        and m.position = p.position
        and m.kind = 1 -- MOON
  left join debris_fields df
         on df.galaxy = p.galaxy
        and df.system = p.system
        and df.position = p.position
      where p.kind = 0 -- PLANET
);

-- Free systems

create view free_systems as (
    select g as galaxy,
           s as system
      from generate_series(1, 5) as g,
           generate_series(1, 500) as s
     where (
       select count(1)
         from bodies b
        where b.galaxy = g
          and b.system = s
          and b.position between 4 and 12
          and b.kind = 0 -- PLANET
     ) != 12 - 4 + 1
);