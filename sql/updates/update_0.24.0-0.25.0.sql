alter domain body_name drop constraint body_name_check;

alter domain body_name add constraint body_name_check check (
  char_length(value) between 1 and 16 and value ~ '^[0-9A-Za-z\-._]+( ?[0-9A-Za-z\-._])*$'
);