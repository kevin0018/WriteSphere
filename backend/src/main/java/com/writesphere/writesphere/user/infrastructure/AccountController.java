package com.writesphere.writesphere.user.infrastructure;

import java.util.Map;

import com.writesphere.writesphere.user.application.RegisterAccount;
import com.writesphere.writesphere.user.domain.Account;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final RegisterAccount register;

    public AccountController(RegisterAccount register) {
        this.register = register;
    }

    record Registration(@NotBlank @Email @Size(max = 254) String email,
                        @NotBlank @Size(min = 2, max = 40) String penName,
                        @NotBlank @Size(min = 12, max = 128) String password) {}

    record Session(String penName, boolean moderator) {}

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    void register(@Valid @RequestBody Registration registration) {
        register.handle(new RegisterAccount.Command(registration.email(), registration.penName(), registration.password()));
    }

    @GetMapping("/csrf")
    Map<String, String> csrf(CsrfToken token) {
        return Map.of("header", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/session")
    Session session(@AuthenticationPrincipal SessionAccount principal) {
        return principal == null ? new Session(null, false) :
                new Session(principal.penName(), principal.role() == Account.Role.MODERATOR);
    }
}
