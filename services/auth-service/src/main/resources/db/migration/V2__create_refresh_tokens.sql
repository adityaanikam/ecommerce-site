-- Refresh tokens.
--
-- Only a SHA-256 hash of the token is stored, never the token itself, so a dump of
-- this table does not hand anyone a working session. The same reasoning as storing
-- a password hash rather than a password.

CREATE TABLE refresh_tokens (
    id          UUID         PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL,
    issued_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked_at  TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

-- Supports the cleanup of tokens that are past their expiry.
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
