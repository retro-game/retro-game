-- @formatter:off

create domain alliance_tag as text check (
  char_length(value) between 3 and 8 and value ~ '^[[:alnum:]]+( ?[[:alnum:]])*$'
);

create domain alliance_name as text check (
  char_length(value) between 3 and 16 and value ~ '^[0-9A-Za-z\-._]+( ?[0-9A-Za-z\-._])*$'
);

create domain alliance_rank as text check (
  char_length(value) between 3 and 8 and value ~ '^[[:alnum:]]+( ?[[:alnum:]])*$'
);

-- Alliances

create table alliances (
  id bigserial primary key,
  owner_id bigint references users on delete set null,
  tag alliance_tag not null,
  name alliance_name not null,
  is_recruitment_open boolean not null,
  logo text check (char_length(logo) <= 128),
  external_text text not null check (char_length(external_text) <= 10000),
  internal_text text not null check (char_length(internal_text) <= 10000),
  application_text text not null check (char_length(application_text) <= 10000)
);

create unique index alliances_upper_tag_idx on alliances (upper(tag) text_pattern_ops);
create unique index alliances_upper_name_idx on alliances (upper(name) text_pattern_ops);

-- Alliance ranks

create table alliance_ranks (
  id bigserial primary key,
  alliance_id bigint references alliances on delete cascade not null,
  name alliance_rank not null,
  privileges int not null,
  unique (alliance_id, name)
);

-- Alliance members

create table alliance_members (
  alliance_id bigint references alliances on delete cascade not null,
  user_id bigint references users on delete cascade unique not null,
  rank_id bigint references alliance_ranks on delete set null,
  joined_at timestamptz not null,
  primary key (alliance_id, user_id)
);

-- Alliance applications

create table alliance_applications (
  id bigserial primary key,
  alliance_id bigint references alliances on delete cascade not null,
  user_id bigint references users on delete cascade unique not null,
  at timestamptz not null,
  application_text text not null check (char_length(application_text) <= 10000)
);

create index alliance_applications_alliance_id_idx on alliance_applications (alliance_id);
