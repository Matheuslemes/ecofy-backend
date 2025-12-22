CREATE TABLE IF NOT EXISTS cat_categories (
  id          UUID PRIMARY KEY,
  name        VARCHAR(160) NOT NULL UNIQUE,
  color       VARCHAR(24),
  active      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL,
  updated_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS cat_rules (
  id              UUID PRIMARY KEY,
  category_id     UUID NOT NULL,
  name            VARCHAR(180) NOT NULL,
  status          VARCHAR(30) NOT NULL,
  priority        INT NOT NULL,
  conditions_json TEXT NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL,
  updated_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cat_rules_status_priority ON cat_rules(status, priority);
