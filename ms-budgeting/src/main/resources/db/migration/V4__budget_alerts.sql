SET search_path TO ecofy_budgeting;

CREATE TABLE IF NOT EXISTS budget_alert (
  id            UUID PRIMARY KEY,
  budget_id     UUID NOT NULL,
  consumption_id UUID NOT NULL,
  severity      VARCHAR(20) NOT NULL,
  message       VARCHAR(500) NOT NULL,
  period_start  DATE NOT NULL,
  period_end    DATE NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT ck_budget_alert_period CHECK (period_start <= period_end)
);

CREATE INDEX IF NOT EXISTS idx_budget_alert_budget_id ON budget_alert(budget_id);
CREATE INDEX IF NOT EXISTS idx_budget_alert_consumption_id ON budget_alert(consumption_id);
