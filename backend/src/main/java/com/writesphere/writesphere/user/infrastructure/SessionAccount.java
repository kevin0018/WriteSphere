package com.writesphere.writesphere.user.infrastructure;

import java.util.List;
import java.util.UUID;

import com.writesphere.writesphere.user.domain.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class SessionAccount extends User {
    private final UUID id;
    private final String penName;
    private final Account.Role role;

    public SessionAccount(Account account) {
        super(account.email(), account.passwordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.role().name())));
        id = account.id();
        penName = account.penName();
        role = account.role();
    }

    public UUID id() {
        return id;
    }

    public String penName() {
        return penName;
    }

    public Account.Role role() {
        return role;
    }
}
