CREATE TABLE IF NOT EXISTS cat_idempotency_keys (
  idempotency_key VARCHAR(200) PRIMARY KEY,
  created_at      TIMESTAMPTZ NOT NULL,
  expires_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cat_idem_expires ON cat_idempotency_keys(expires_at);
