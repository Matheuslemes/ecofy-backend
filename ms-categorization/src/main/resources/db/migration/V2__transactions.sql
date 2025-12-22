CREATE TABLE IF NOT EXISTS cat_transactions (
  id            UUID PRIMARY KEY,
  import_job_id UUID NOT NULL,
  external_id   VARCHAR(120) NOT NULL,
  description   VARCHAR(512) NOT NULL,
  merchant_norm VARCHAR(512) NOT NULL,
  tx_date       DATE NOT NULL,
  amount        NUMERIC(19,2) NOT NULL,
  currency      VARCHAR(10) NOT NULL,
  source_type   VARCHAR(30) NOT NULL,
  category_id   UUID,
  created_at    TIMESTAMPTZ NOT NULL,
  updated_at    TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_cat_tx_job_external ON cat_transactions(import_job_id, external_id);
CREATE INDEX IF NOT EXISTS idx_cat_tx_category ON cat_transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_cat_tx_date ON cat_transactions(tx_date);