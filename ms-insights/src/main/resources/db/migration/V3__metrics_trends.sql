create table if not exists metric_snapshot (
  id uuid primary key,
  user_id uuid not null,
  metric_type varchar(40) not null,
  period_start date not null,
  period_end date not null,
  granularity varchar(10) not null,
  value_cents bigint not null,
  currency varchar(3) not null,
  created_at timestamptz not null
);

create index if not exists idx_metric_user_created on metric_snapshot(user_id, created_at desc);

create table if not exists trend (
  id uuid primary key,
  user_id uuid not null,
  trend_type varchar(20) not null,
  period_start date not null,
  period_end date not null,
  granularity varchar(10) not null,
  series_json text not null,
  currency varchar(3) not null,
  created_at timestamptz not null
);

create index if not exists idx_trend_user_created on trend(user_id, created_at desc);
