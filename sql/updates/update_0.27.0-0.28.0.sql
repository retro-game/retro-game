-- @formatter:off

drop materialized view overall_ranking;
drop materialized view buildings_ranking;
drop materialized view technologies_ranking;
drop materialized view fleet_ranking;
drop materialized view defense_ranking;


create function create_statistics(kind text) returns void as $$
begin
  execute '
    create table ' || kind || '_statistics (
      user_id bigint references users not null,
      at timestamptz not null,
      points int not null,
      rank int not null,
      primary key (user_id, at)
    )';
  execute 'create index on ' || kind || '_statistics (at)';
end
$$ language plpgsql;

select create_statistics('overall');
select create_statistics('buildings');
select create_statistics('technologies');
select create_statistics('fleet');
select create_statistics('defense');

drop function create_statistics(kind text);


delete from buildings_points where extract(hour from at) not in (0, 8, 16);
delete from technologies_points where extract(hour from at) not in (0, 8, 16);
delete from fleet_points where extract(hour from at) not in (0, 8, 16);
delete from defense_points where extract(hour from at) not in (0, 8, 16);


insert into buildings_statistics
     select user_id,
            at,
            points,
            (rank() over (partition by at order by points desc))
       from buildings_points;

insert into technologies_statistics
     select user_id,
            at,
            points,
            (rank() over (partition by at order by points desc))
       from technologies_points;

insert into fleet_statistics
     select user_id,
            at,
            points,
            (rank() over (partition by at order by points desc))
       from fleet_points;

insert into defense_statistics
     select user_id,
            at,
            points,
            (rank() over (partition by at order by points desc))
       from defense_points;

with tmp as (
  select b.user_id,
         b.at,
         (b.points + t.points + f.points + d.points) as points
    from buildings_points b
    join technologies_points t
      on t.user_id = b.user_id
     and t.at = b.at
    join fleet_points f
      on f.user_id = b.user_id
     and f.at = b.at
    join defense_points d
      on d.user_id = b.user_id
     and d.at = b.at
)
insert into overall_statistics
     select user_id,
            at,
            points,
            (rank() over (partition by at order by points desc))
       from tmp;


drop table buildings_points;
drop table technologies_points;
drop table fleet_points;
drop table defense_points;
