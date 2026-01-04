SET search_path TO ecofy_budgeting;

CREATE TABLE IF NOT EXISTS budget_alerts (
  id UUID PRIMARY KEY,
  budget_id UUID NOT NULL,
  consumption_id UUID NOT NULL,
  severity VARCHAR(20) NOT NULL,
  message VARCHAR(500) NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alert_budget ON budget_alerts(budget_id);