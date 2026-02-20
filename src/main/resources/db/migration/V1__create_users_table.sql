CREATE TABLE users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    plan           VARCHAR(20) NOT NULL DEFAULT 'FREE',
    plan_expires_at TIMESTAMPTZ,
    analyses_today INTEGER NOT NULL DEFAULT 0,
    analyses_date  DATE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);
