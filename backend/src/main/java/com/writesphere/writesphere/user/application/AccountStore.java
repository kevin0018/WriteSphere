package com.writesphere.writesphere.user.application;

import java.util.Optional;

import com.writesphere.writesphere.user.domain.Account;

public interface AccountStore {
    boolean insert(Account account);

    Optional<Account> findByEmail(String normalizedEmail);
}
