-- Habilita o fluxo de login utilizado pelo frontend local em API Mode.
INSERT INTO auth_client_grants (client_id, grant_type)
VALUES ('eco_dashboard_local', 'PASSWORD')
ON CONFLICT (client_id, grant_type) DO NOTHING;

-- Mantém o callback local coerente com a porta padrão do Vite.
INSERT INTO auth_client_redirect_uris (client_id, redirect_uri)
VALUES ('eco_dashboard_local', 'http://localhost:5173/auth/callback')
ON CONFLICT (client_id, redirect_uri) DO NOTHING;
