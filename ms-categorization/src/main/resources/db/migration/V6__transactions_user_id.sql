ALTER TABLE cat_transactions ADD COLUMN IF NOT EXISTS user_id UUID;

-- Índice parcial para a consulta principal: transações não categorizadas de um usuário.
CREATE INDEX IF NOT EXISTS idx_cat_tx_user_uncategorized
    ON cat_transactions(user_id, tx_date)
    WHERE category_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_cat_tx_user ON cat_transactions(user_id);
