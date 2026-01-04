SET search_path TO ecofy_budgeting;

CREATE TABLE IF NOT EXISTS budget_consumption (
  id UUID PRIMARY KEY,
  budget_id UUID NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  consumed_amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  source VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_consumption_budget_period ON budget_consumption(budget_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_consumption_budget ON budget_consumption(budget_id);
