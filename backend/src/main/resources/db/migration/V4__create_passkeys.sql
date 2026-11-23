CREATE TABLE user_entities (
    id varchar(86) PRIMARY KEY,
    name varchar(254) NOT NULL UNIQUE REFERENCES accounts(email) ON DELETE CASCADE,
    display_name varchar(254) NOT NULL
);

CREATE TABLE user_credentials (
    credential_id varchar(1364) PRIMARY KEY,
    user_entity_user_id varchar(86) NOT NULL REFERENCES user_entities(id) ON DELETE CASCADE,
    public_key bytea NOT NULL,
    signature_count bigint NOT NULL,
    uv_initialized boolean NOT NULL,
    backup_eligible boolean NOT NULL,
    authenticator_transports text NOT NULL,
    public_key_credential_type varchar(20) NOT NULL,
    backup_state boolean NOT NULL,
    attestation_object bytea,
    attestation_client_data_json bytea,
    created timestamptz NOT NULL,
    last_used timestamptz,
    label varchar(80) NOT NULL
);

CREATE INDEX user_credentials_owner ON user_credentials (user_entity_user_id);
