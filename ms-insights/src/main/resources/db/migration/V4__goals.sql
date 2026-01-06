create table if not exists goal (
  id uuid primary key,
  user_id uuid not null,
  name varchar(120) not null,
  target_cents bigint not null,
  currency varchar(3) not null,
  status varchar(20) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create index if not exists idx_goal_user_updated on goal(user_id, updated_at desc);
