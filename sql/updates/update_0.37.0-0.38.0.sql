-- @formatter:off

-- User password reset tokens

create table user_password_reset_tokens (
  user_id bigint references users on delete cascade unique not null,
  encrypted_token text not null,
  expire_at timestamptz not null,
  primary key (user_id)
);
