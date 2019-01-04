alter table users
  add column roles int,
  add column forced_vacation boolean;

update users
set roles = 1, forced_vacation = false;

alter table users
  alter column roles set not null,
  alter column forced_vacation set not null;

create table pranger (
  id bigserial primary key,
  user_id bigint references users not null,
  at timestamptz not null,
  until timestamptz not null,
  reason text not null,
  admin_id bigint references users not null,
  unique (user_id, until)
);

create index pranger_at_idx
          on pranger (at desc);

drop view galaxy;

create view galaxy as (
     select p.galaxy,
            p.system,
            p.position,
            u.id as user_id,
            u.name as user_name,
            u.vacation_until,
            u.forced_vacation,
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
