SET search_path TO ecofy_budgeting;

CREATE TABLE IF NOT EXISTS budgets (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  category_id UUID NOT NULL,
  period_type VARCHAR(20) NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  limit_amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  status VARCHAR(20) NOT NULL,
  natural_key VARCHAR(200) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_budget_natural ON budgets(natural_key);
CREATE INDEX IF NOT EXISTS idx_budget_user ON budgets(user_id);
