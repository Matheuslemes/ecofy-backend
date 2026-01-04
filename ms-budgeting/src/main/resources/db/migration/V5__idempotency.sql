SET search_path TO ecofy_budgeting;

CREATE TABLE IF NOT EXISTS idempotency_keys (
  id BIGSERIAL PRIMARY KEY,
  idem_key VARCHAR(200) NOT NULL,
  scope VARCHAR(80) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_idempotency_key ON idempotency_keys(idem_key);
CREATE INDEX IF NOT EXISTS idx_idem_expires ON idempotency_keys(expires_at);
