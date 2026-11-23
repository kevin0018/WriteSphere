#!/usr/bin/env bash
set -euo pipefail

export WRITESPHERE_MIGRATOR_PASSWORD
export WRITESPHERE_API_PASSWORD
WRITESPHERE_MIGRATOR_PASSWORD="$(cat /run/secrets/db_migrator_password)"
WRITESPHERE_API_PASSWORD="$(cat /run/secrets/db_api_password)"

if [[ -z "$WRITESPHERE_MIGRATOR_PASSWORD" || -z "$WRITESPHERE_API_PASSWORD" ||
      "$WRITESPHERE_MIGRATOR_PASSWORD" == "$WRITESPHERE_API_PASSWORD" ||
      "$POSTGRES_PASSWORD" == "$WRITESPHERE_MIGRATOR_PASSWORD" ||
      "$POSTGRES_PASSWORD" == "$WRITESPHERE_API_PASSWORD" ]]; then
    printf '%s\n' 'Configure three distinct PostgreSQL passwords.' >&2
    exit 1
fi

psql --no-psqlrc --set ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\getenv migrator_password WRITESPHERE_MIGRATOR_PASSWORD
\getenv api_password WRITESPHERE_API_PASSWORD

BEGIN;
SET LOCAL password_encryption = 'scram-sha-256';
CREATE ROLE writesphere_migrator LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS
    PASSWORD :'migrator_password';
CREATE ROLE writesphere_api LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS
    PASSWORD :'api_password';

REVOKE CONNECT, TEMPORARY ON DATABASE postgres, template1, writesphere FROM PUBLIC;
GRANT CONNECT ON DATABASE writesphere TO writesphere_migrator, writesphere_api;

REVOKE ALL ON SCHEMA public FROM PUBLIC;
ALTER SCHEMA public OWNER TO writesphere_migrator;
GRANT USAGE ON SCHEMA public TO writesphere_api;
COMMIT;
SQL
