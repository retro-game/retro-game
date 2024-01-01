begin;

-- Bodies
alter table bodies rename to bodies_old;

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
  shipyard_start_at timestamptz,
  buildings int[] not null check (array_length(buildings, 1) = 18),
  units int[] not null check (array_length(units, 1) = 24),
  building_queue int[] not null,
  shipyard_queue int[] not null,
  unique (galaxy, system, position, kind)
);

create index bodies_user_id_idx on bodies (user_id);
create index bodies_upper_name_idx on bodies (upper(name) text_pattern_ops);

insert into bodies select * from bodies_old;

drop table bodies_old;


-- Flights

alter table flights rename to flights_old;

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
     deuterium double precision not null check (deuterium >= 0),
     units int[] not null check (array_length(units, 1) = 24),
     main_target int
);

create index flights_start_user_id_idx on flights (start_user_id);
create index flights_start_body_id_idx on flights (start_body_id);
create index flights_target_user_id_idx on flights (target_user_id);
create index flights_target_body_id_idx on flights (target_body_id);
create index flights_target_coordinates_idx on flights (target_galaxy, target_system, target_position, target_kind);
create index flights_party_id_idx on flights (party_id);

insert into flights select * from flights_old;

drop table flights_old;


commit;