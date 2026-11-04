-- Orders and their lines.
--
-- Money is NUMERIC(12,2), never a floating point type. The monolith stored prices
-- as a Java double, which cannot represent 0.1 exactly and so accumulates error
-- across a basket. An order total that is a cent out is a real support ticket.

CREATE TABLE orders (
    id                  UUID           PRIMARY KEY,
    user_id             UUID           NOT NULL,
    status              VARCHAR(32)    NOT NULL,
    -- VARCHAR, not CHAR: CHAR comes back as bpchar and blank-pads, which both
    -- trips Hibernate's schema validation and silently changes stored values.
    currency            VARCHAR(3)     NOT NULL DEFAULT 'USD',

    subtotal            NUMERIC(12,2)  NOT NULL,
    shipping_cost       NUMERIC(12,2)  NOT NULL DEFAULT 0,
    total               NUMERIC(12,2)  NOT NULL,

    shipping_first_name VARCHAR(100)   NOT NULL,
    shipping_last_name  VARCHAR(100)   NOT NULL,
    shipping_address    VARCHAR(255)   NOT NULL,
    shipping_city       VARCHAR(100)   NOT NULL,
    shipping_state      VARCHAR(100),
    shipping_zip_code   VARCHAR(32)    NOT NULL,
    shipping_country    VARCHAR(100)   NOT NULL,
    shipping_phone      VARCHAR(32),

    payment_method      VARCHAR(32)    NOT NULL,
    shipping_method     VARCHAR(64),
    notes               TEXT,

    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_orders_status CHECK (status IN
        ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT ck_orders_totals_non_negative CHECK (subtotal >= 0 AND shipping_cost >= 0 AND total >= 0)
);

CREATE INDEX idx_orders_user_id_created_at ON orders (user_id, created_at DESC);

CREATE TABLE order_items (
    id            UUID           PRIMARY KEY,
    order_id      UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id    VARCHAR(64)    NOT NULL,
    -- The product name is copied in rather than looked up later, on purpose. An
    -- order is a record of what was bought at the time; renaming a product in the
    -- catalog must not rewrite history on an order already placed.
    product_name  VARCHAR(255)   NOT NULL,
    unit_price    NUMERIC(12,2)  NOT NULL,
    quantity      INTEGER        NOT NULL,
    line_total    NUMERIC(12,2)  NOT NULL,

    CONSTRAINT ck_order_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_order_items_price_non_negative CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
