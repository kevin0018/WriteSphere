CREATE TABLE bookmarks (
    account_id uuid NOT NULL REFERENCES accounts(id),
    work_id uuid NOT NULL REFERENCES works(id),
    saved_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (account_id, work_id)
);

CREATE INDEX bookmarks_recent ON bookmarks (account_id, saved_at DESC, work_id DESC);
