-- @formatter:off

-- Bodies

alter table bodies drop constraint bodies_units_check;
alter table bodies add constraint bodies_units_check check(array_length(units, 1) = 24);

-- Flights

alter table flights drop constraint flights_units_check;
alter table flights add constraint flights_units_check check(array_length(units, 1) = 24);

