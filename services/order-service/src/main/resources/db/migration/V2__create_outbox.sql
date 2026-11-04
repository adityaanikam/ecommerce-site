-- The transactional outbox.
--
-- An event is written here in the same transaction as the order it describes, so
-- either both land or neither does. Publishing to Kafka then happens separately,
-- from this table. That is the whole point: a service cannot atomically write to its
-- database and to a broker, so trying to do both inline leaves a window where the
-- order exists and the event was never sent, or the event was sent and the
-- transaction then rolled back.

CREATE TABLE outbox_events (
    -- This id is the event id carried in the message, and it is what consumers
    -- deduplicate on. It is generated here, inside the transaction, so a republish
    -- of the same row always carries the same id.
    id             UUID         PRIMARY KEY,
    aggregate_type VARCHAR(64)  NOT NULL,
    aggregate_id   VARCHAR(64)  NOT NULL,
    event_type     VARCHAR(64)  NOT NULL,
    payload        TEXT         NOT NULL,
    correlation_id VARCHAR(128),

    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMPTZ,
    attempts       INTEGER      NOT NULL DEFAULT 0,
    last_error     TEXT
);

-- The publisher's query is "unpublished, oldest first". A partial index keeps that
-- scan proportional to the backlog rather than to the whole history of events.
CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at)
    WHERE published_at IS NULL;
