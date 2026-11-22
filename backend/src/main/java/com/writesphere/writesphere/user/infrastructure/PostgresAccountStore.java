package com.writesphere.writesphere.user.infrastructure;

import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.user.application.AccountStore;
import com.writesphere.writesphere.user.domain.Account;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresAccountStore implements AccountStore {
    private final JdbcClient jdbc;

    public PostgresAccountStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean insertMember(Account account) {
        return jdbc.sql("""
                INSERT INTO accounts (id, email, pen_name, pen_name_key, password_hash)
                VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING
                """).params(account.id(), account.email(), account.penName(), account.penNameKey(),
                account.passwordHash()).update() == 1;
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return jdbc.sql("SELECT * FROM accounts WHERE email = ?").param(email).query((row, index) ->
                new Account(row.getObject("id", UUID.class), row.getString("email"),
                        row.getString("pen_name"), row.getString("pen_name_key"),
                        row.getString("password_hash"), Account.Role.valueOf(row.getString("role"))))
                .optional();
    }
}
