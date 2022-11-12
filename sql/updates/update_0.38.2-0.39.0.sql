begin;

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

insert into bodies select * from bodies_old;

drop table bodies_old;

commit;