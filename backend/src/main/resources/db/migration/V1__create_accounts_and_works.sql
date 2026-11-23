CREATE TABLE accounts (
    id uuid PRIMARY KEY,
    email varchar(254) NOT NULL CONSTRAINT accounts_email_unique UNIQUE,
    pen_name varchar(40) NOT NULL,
    pen_name_key varchar(40) NOT NULL CONSTRAINT accounts_pen_name_unique UNIQUE,
    password_hash varchar(255) NOT NULL,
    role varchar(20) NOT NULL CHECK (role IN ('MEMBER', 'MODERATOR'))
);

CREATE TABLE works (
    id uuid PRIMARY KEY,
    author_id uuid NOT NULL REFERENCES accounts(id),
    genre varchar(20) NOT NULL CHECK (genre IN ('POEM', 'MICROFICTION')),
    title varchar(160) NOT NULL,
    body text NOT NULL,
    state varchar(20) NOT NULL CHECK (state IN ('DRAFT', 'PUBLISHED', 'HIDDEN')),
    version bigint NOT NULL CHECK (version >= 0),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    published_at timestamptz,
    CHECK (state <> 'PUBLISHED' OR published_at IS NOT NULL)
);

CREATE VIEW published_works AS
SELECT w.id, w.author_id, a.pen_name, w.genre, w.title, w.body, w.published_at
FROM works w
JOIN accounts a ON a.id = w.author_id
WHERE w.state = 'PUBLISHED';

CREATE INDEX works_publication_order ON works (published_at DESC, id DESC)
    WHERE state = 'PUBLISHED';
CREATE INDEX works_author_order ON works (author_id, updated_at DESC, id DESC);
