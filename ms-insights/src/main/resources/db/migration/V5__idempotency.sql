create table if not exists idempotency_key (
  key varchar(300) primary key,
  expires_at timestamptz not null,
  created_at timestamptz not null
);

create index if not exists idx_idem_expires on idempotency_key(expires_at);
