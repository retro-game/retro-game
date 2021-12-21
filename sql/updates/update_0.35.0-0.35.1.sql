begin;

alter table overall_statistics rename to overall_statistics_old;
alter table buildings_statistics rename to buildings_statistics_old;
alter table technologies_statistics rename to technologies_statistics_old;
alter table fleet_statistics rename to fleet_statistics_old;
alter table defense_statistics rename to defense_statistics_old;

create function create_statistics(kind text) returns void as $$
begin
  execute '
    create table ' || kind || '_statistics (
      user_id bigint references users not null,
      at timestamptz not null,
      points bigint not null,
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

insert into overall_statistics select * from overall_statistics_old;
insert into buildings_statistics select * from buildings_statistics_old;
insert into technologies_statistics select * from technologies_statistics_old;
insert into fleet_statistics select * from fleet_statistics_old;
insert into defense_statistics select * from defense_statistics_old;

drop table overall_statistics_old;
drop table buildings_statistics_old;
drop table technologies_statistics_old;
drop table fleet_statistics_old;
drop table defense_statistics_old;

commit;
