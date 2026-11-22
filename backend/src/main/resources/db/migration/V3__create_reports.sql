CREATE TABLE reports (
    id uuid PRIMARY KEY,
    work_id uuid NOT NULL REFERENCES works(id),
    reporter_id uuid NOT NULL REFERENCES accounts(id),
    reason varchar(1000) NOT NULL CHECK (length(trim(reason)) > 0),
    created_at timestamptz NOT NULL,
    decision varchar(20) CHECK (decision IN ('DISMISS', 'HIDE')),
    resolved_at timestamptz,
    moderator_id uuid REFERENCES accounts(id),
    CHECK ((decision IS NULL AND resolved_at IS NULL AND moderator_id IS NULL)
        OR (decision IS NOT NULL AND resolved_at IS NOT NULL AND moderator_id IS NOT NULL))
);

CREATE UNIQUE INDEX reports_open_per_reader ON reports (work_id, reporter_id) WHERE decision IS NULL;
CREATE INDEX reports_pending_order ON reports (created_at, id) WHERE decision IS NULL;
