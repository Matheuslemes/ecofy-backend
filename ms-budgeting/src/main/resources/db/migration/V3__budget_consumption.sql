SET search_path TO ecofy_budgeting;

CREATE TABLE IF NOT EXISTS budget_consumption (
  id            UUID PRIMARY KEY,
  budget_id     UUID NOT NULL,
  period_start  DATE NOT NULL,
  period_end    DATE NOT NULL,
  consumed_cents BIGINT NOT NULL,
  currency      VARCHAR(3) NOT NULL,
  source        VARCHAR(40) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT ck_budget_consumption_period CHECK (period_start <= period_end),
  CONSTRAINT ck_budget_consumption_consumed_nonneg CHECK (consumed_cents >= 0),
  CONSTRAINT ck_budget_consumption_currency_len CHECK (length(currency) = 3)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_budget_consumption_budget_period
  ON budget_consumption(budget_id, period_start, period_end);

CREATE INDEX IF NOT EXISTS idx_budget_consumption_budget_id
  ON budget_consumption(budget_id);

SET search_path TO ecofy_budgeting;

ALTER TABLE budget_consumption
  ADD COLUMN IF NOT EXISTS reference_date DATE;

-- Backfill simples: usa period_end como referência (padrão para limpeza)
UPDATE budget_consumption
SET reference_date = period_end
WHERE reference_date IS NULL;

ALTER TABLE budget_consumption
  ALTER COLUMN reference_date SET NOT NULL;

-- Índice/unique para evitar duplicidade por budget + reference_date
CREATE UNIQUE INDEX IF NOT EXISTS uk_budget_consumption_budget_refdate
  ON budget_consumption(budget_id, reference_date);

