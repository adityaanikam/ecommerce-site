-- Idempotency keys for order creation.
--
-- A client that retries POST /orders after a timeout must not end up with two
-- orders. It sends the same Idempotency-Key on the retry, and this table is what
-- makes the second request return the first request's order instead of creating
-- another one.
--
-- The key is scoped to the user, so two different people choosing the same key
-- cannot collide, and one user cannot see another's order by guessing a key.

CREATE TABLE idempotency_keys (
    idempotency_key UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    -- A fingerprint of the request body. A retry carrying the same key but a
    -- different basket is a client bug, and answering it with the first order
    -- silently would hide that, so it is rejected instead.
    request_hash    VARCHAR(64)  NOT NULL,
    order_id        UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, idempotency_key)
);

CREATE INDEX idx_idempotency_keys_created_at ON idempotency_keys (created_at);
