-- @formatter:off

alter table users drop column messages_seen_at;

alter table users add column private_received_messages_seen_at timestamptz;
alter table users add column alliance_messages_seen_at timestamptz;
alter table users add column broadcast_messages_seen_at timestamptz;

update users set private_received_messages_seen_at = now(),
                 alliance_messages_seen_at = now(),
                 broadcast_messages_seen_at = now();

alter table users alter column private_received_messages_seen_at set not null;
alter table users alter column alliance_messages_seen_at set not null;
alter table users alter column broadcast_messages_seen_at set not null;

drop table messages;

create table private_messages (
  id bigserial primary key,
  sender_id bigint references users on delete set null,
  recipient_id bigint references users on delete set null,
  deleted_by_sender boolean not null,
  deleted_by_recipient boolean not null,
  at timestamptz not null,
  message text not null
);

create index private_messages_sender_id_deleted_by_sender_at_idx
          on private_messages (sender_id, deleted_by_sender, at desc);
create index private_messages_recipient_id_deleted_by_recipient_at_idx
          on private_messages (recipient_id, deleted_by_recipient, at desc);

create table broadcast_messages (
  id bigserial primary key,
  sender_id bigint references users on delete set null,
  at timestamptz not null,
  message text not null
);

create index broadcast_messages_at_idx
          on broadcast_messages (at desc);

create table alliance_messages (
  id bigserial primary key,
  alliance_id bigint references alliances on delete cascade not null,
  sender_id bigint references users on delete set null,
  at timestamptz not null,
  message text not null
);

create index alliance_messages_alliance_id_at_idx
          on alliance_messages (alliance_id, at desc);
