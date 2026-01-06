create table if not exists insight (
  id uuid primary key,
  user_id uuid not null,
  type varchar(40) not null,
  period_start date not null,
  period_end date not null,
  granularity varchar(10) not null,
  score int not null,
  title varchar(120) not null,
  summary varchar(400) not null,
  payload_json text not null,
  created_at timestamptz not null
);

create index if not exists idx_insight_user_created on insight(user_id, created_at desc);
