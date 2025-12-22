CREATE TABLE IF NOT EXISTS cat_suggestions (
  id             UUID PRIMARY KEY,
  transaction_id UUID NOT NULL,
  category_id    UUID,
  rule_id        UUID,
  status         VARCHAR(30) NOT NULL,
  score          INT NOT NULL,
  rationale      TEXT,
  created_at     TIMESTAMPTZ NOT NULL,
  updated_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cat_sug_tx ON cat_suggestions(transaction_id);