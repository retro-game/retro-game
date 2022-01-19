-- @formatter:off

begin;

drop table simplified_combat_reports;
drop table combat_reports;

create table combat_reports (
  id uuid primary key,
  at timestamptz not null,
  attackers bigint[] not null,
  defenders bigint[] not null,
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

create index combat_reports_loss_idx
          on combat_reports ((attackers_loss + defenders_loss) desc);
create index combat_reports_plunder_idx
          on combat_reports ((plunder_metal + plunder_crystal + plunder_deuterium) desc);
create index combat_reports_debris_idx
          on combat_reports ((debris_metal + debris_crystal) desc);

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
  combat_report_id uuid
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

commit;
