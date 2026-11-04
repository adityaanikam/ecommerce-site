-- Users and their roles.
--
-- The schema is owned by auth-service and by nothing else. No other service has a
-- connection string to this database; anything that needs to know who a user is gets
-- it from the JWT, or from this service over HTTP.

CREATE TABLE users (
    id              UUID         PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone_number    VARCHAR(32),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Email is the login identifier and is treated case insensitively. Storing it
-- lowercased and putting the unique constraint on the raw column would still let
-- "A@b.c" and "a@b.c" both register, so the constraint is on the lowercased value
-- and the application lowercases on the way in as well.
CREATE UNIQUE INDEX uq_users_email_lower ON users (LOWER(email));

CREATE TABLE user_roles (
    user_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role    VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT ck_user_roles_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
