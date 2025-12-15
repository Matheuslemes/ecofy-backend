-- ENUMS (PostgreSQL)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'auth_user_status') THEN
        CREATE TYPE auth_user_status AS ENUM (
            'PENDING_EMAIL_CONFIRMATION',
            'ACTIVE',
            'LOCKED',
            'BLOCKED',
            'DELETED'
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'client_type') THEN
        CREATE TYPE client_type AS ENUM (
            'CONFIDENTIAL',
            'PUBLIC',
            'SPA',
            'MACHINE_TO_MACHINE'
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'grant_type') THEN
        CREATE TYPE grant_type AS ENUM (
            'AUTHORIZATION_CODE',
            'PASSWORD',
            'CLIENT_CREDENTIALS',
            'REFRESH_TOKEN'
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'token_type') THEN
        CREATE TYPE token_type AS ENUM (
            'ACCESS',
            'REFRESH',
            'VERIFICATION',
            'PASSWORD_RESET'
        );
    END IF;
END$$;

-- TABELA: auth_users  (AuthUserEntity)
CREATE TABLE IF NOT EXISTS auth_users (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(180) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    status                auth_user_status NOT NULL,
    email_verified        BOOLEAN NOT NULL DEFAULT FALSE,
    first_name            VARCHAR(80),
    last_name             VARCHAR(80),
    locale                VARCHAR(10) NOT NULL DEFAULT 'pt-BR',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at         TIMESTAMPTZ,
    failed_login_attempts INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_auth_users_email
    ON auth_users (LOWER(email));

CREATE INDEX IF NOT EXISTS idx_auth_users_status
    ON auth_users (status);

-- TABELA: auth_roles (RoleEntity)
CREATE TABLE IF NOT EXISTS auth_roles (
    name        VARCHAR(64) PRIMARY KEY,
    description VARCHAR(255)
);

-- TABELA: auth_permissions (PermissionEntity)
CREATE TABLE IF NOT EXISTS auth_permissions (
    name        VARCHAR(128) PRIMARY KEY,
    description VARCHAR(255),
    domain      VARCHAR(64) DEFAULT '*'
);

-- RELACIONAMENTOS
-- auth_users ↔ auth_roles (ManyToMany)
CREATE TABLE IF NOT EXISTS auth_users_roles (
    user_id   UUID        NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_auth_users_roles_user
        FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_auth_users_roles_role
        FOREIGN KEY (role_name) REFERENCES auth_roles (name) ON DELETE CASCADE
);

-- auth_users ↔ auth_permissions (ManyToMany)
CREATE TABLE IF NOT EXISTS auth_users_permissions (
    user_id         UUID         NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    PRIMARY KEY (user_id, permission_name),
    CONSTRAINT fk_auth_users_permissions_user
        FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_auth_users_permissions_perm
        FOREIGN KEY (permission_name) REFERENCES auth_permissions (name) ON DELETE CASCADE
);

-- auth_roles ↔ auth_permissions (ManyToMany)
CREATE TABLE IF NOT EXISTS auth_roles_permissions (
    role_name       VARCHAR(64)  NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    PRIMARY KEY (role_name, permission_name),
    CONSTRAINT fk_auth_roles_permissions_role
        FOREIGN KEY (role_name) REFERENCES auth_roles (name) ON DELETE CASCADE,
    CONSTRAINT fk_auth_roles_permissions_perm
        FOREIGN KEY (permission_name) REFERENCES auth_permissions (name) ON DELETE CASCADE
);

-- TABELA: auth_client_applications (ClientApplicationEntity)
CREATE TABLE IF NOT EXISTS auth_client_applications (
    id                 VARCHAR(36) PRIMARY KEY,
    client_id          VARCHAR(100) NOT NULL UNIQUE,
    client_secret_hash VARCHAR(255),
    name               VARCHAR(120) NOT NULL,
    client_type        client_type  NOT NULL,
    first_party        BOOLEAN      NOT NULL DEFAULT FALSE,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_auth_client_applications_active
    ON auth_client_applications (active);

-- ElementCollection: grantTypes (auth_client_grants)
CREATE TABLE IF NOT EXISTS auth_client_grants (
    client_id  VARCHAR(100) NOT NULL,
    grant_type grant_type   NOT NULL,
    PRIMARY KEY (client_id, grant_type),
    CONSTRAINT fk_auth_client_grants_client
        FOREIGN KEY (client_id) REFERENCES auth_client_applications (client_id) ON DELETE CASCADE
);

-- ElementCollection: redirectUris (auth_client_redirect_uris)
CREATE TABLE IF NOT EXISTS auth_client_redirect_uris (
    client_id    VARCHAR(100) NOT NULL,
    redirect_uri VARCHAR(512) NOT NULL,
    PRIMARY KEY (client_id, redirect_uri),
    CONSTRAINT fk_auth_client_redirect_uris_client
        FOREIGN KEY (client_id) REFERENCES auth_client_applications (client_id) ON DELETE CASCADE
);

-- ElementCollection: scopes (auth_client_scopes)
CREATE TABLE IF NOT EXISTS auth_client_scopes (
    client_id VARCHAR(100) NOT NULL,
    scope     VARCHAR(64)  NOT NULL,
    PRIMARY KEY (client_id, scope),
    CONSTRAINT fk_auth_client_scopes_client
        FOREIGN KEY (client_id) REFERENCES auth_client_applications (client_id) ON DELETE CASCADE
);

-- TABELA: auth_refresh_tokens (RefreshTokenEntity)
CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    id          UUID PRIMARY KEY,
    token_value VARCHAR(2048) NOT NULL UNIQUE,
    user_id     UUID         NOT NULL,
    client_id   VARCHAR(100) NOT NULL,
    issued_at   TIMESTAMPTZ  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    type        token_type   NOT NULL,

    CONSTRAINT fk_auth_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE CASCADE,

    CONSTRAINT fk_auth_refresh_tokens_client
        FOREIGN KEY (client_id) REFERENCES auth_client_applications (client_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_user
    ON auth_refresh_tokens (user_id);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_expires_at
    ON auth_refresh_tokens (expires_at);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_active
    ON auth_refresh_tokens (revoked, expires_at);

-- TABELA: auth_jwk_keys (JwkKeyEntity)
CREATE TABLE IF NOT EXISTS auth_jwk_keys (
    key_id         VARCHAR(64) PRIMARY KEY,
    public_key_pem TEXT        NOT NULL,
    algorithm      VARCHAR(16) NOT NULL,
    "use"          VARCHAR(8)  NOT NULL,
    active         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_auth_jwk_keys_active
    ON auth_jwk_keys (active);
