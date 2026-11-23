package com.writesphere.writesphere.user.application;

import java.util.UUID;

import com.writesphere.writesphere.shared.application.ApplicationProblem;
import com.writesphere.writesphere.user.domain.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAccount {
    private final AccountStore accounts;
    private final PasswordHash passwords;

    public RegisterAccount(AccountStore accounts, PasswordHash passwords) {
        this.accounts = accounts;
        this.passwords = passwords;
    }

    public record Command(String email, String penName, String password) {}

    @Transactional
    public void handle(Command command) {
        String email = Account.normalize(command.email());
        String penName = command.penName().strip();
        String penNameKey = Account.normalize(penName);
        if (email.length() > 254 || penNameKey.length() > 40 || penNameKey.isBlank()) {
            throw new ApplicationProblem(ApplicationProblem.Kind.INVALID, "invalid_input");
        }
        var account = new Account(UUID.randomUUID(), email, penName, penNameKey,
                passwords.encode(command.password()), Account.Role.MEMBER);
        if (!accounts.insertMember(account)) {
            throw new ApplicationProblem(ApplicationProblem.Kind.CONFLICT, "account_exists");
        }
    }
}
