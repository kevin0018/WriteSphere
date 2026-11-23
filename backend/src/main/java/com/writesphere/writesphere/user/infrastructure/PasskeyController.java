package com.writesphere.writesphere.user.infrastructure;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import org.springframework.security.web.webauthn.management.ImmutablePublicKeyCredentialCreationOptionsRequest;
import org.springframework.security.web.webauthn.management.ImmutableRelyingPartyRegistrationRequest;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.RelyingPartyPublicKey;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/session/passkeys")
public class PasskeyController {
    private static final String ENROLLMENT = PasskeyController.class.getName() + ".enrollment";
    private static final TypeReference<PublicKeyCredential<AuthenticatorAttestationResponse>> CREDENTIAL =
            new TypeReference<>() {};

    private final JsonMapper webauthn = JsonMapper.builder().addModule(new WebauthnJacksonModule()).build();
    private final WebAuthnRelyingPartyOperations operations;
    private final PublicKeyCredentialUserEntityRepository users;
    private final UserCredentialRepository credentials;
    private final UserDetailsService accounts;
    private final PasswordEncoder passwords;

    public PasskeyController(WebAuthnRelyingPartyOperations operations, PublicKeyCredentialUserEntityRepository users,
                             UserCredentialRepository credentials, UserDetailsService accounts, PasswordEncoder passwords) {
        this.operations = operations;
        this.users = users;
        this.credentials = credentials;
        this.accounts = accounts;
        this.passwords = passwords;
    }

    record VerifyPassword(@NotBlank @Size(max = 128) String password) {}
    record Registration(@NotNull JsonNode credential, @NotBlank @Size(max = 80) String label) {}
    record Passkey(String id, String label) {}
    private record Enrollment(PublicKeyCredentialCreationOptions options, Instant expires) {}

    @PostMapping("/register/options")
    JsonNode options(Authentication authentication, HttpSession session, @Valid @RequestBody VerifyPassword form) {
        // Enrolling an additional authenticator requires proof beyond possession of a session cookie.
        var account = accounts.loadUserByUsername(authentication.getName());
        if (!passwords.matches(form.password(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        var options = operations.createPublicKeyCredentialCreationOptions(
                new ImmutablePublicKeyCredentialCreationOptionsRequest(authentication));
        Duration timeout = options.getTimeout();
        session.setAttribute(ENROLLMENT, new Enrollment(options, Instant.now().plus(timeout)));
        return webauthn.valueToTree(options);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void register(Authentication authentication, HttpSession session, @Valid @RequestBody Registration form) {
        Enrollment enrollment;
        synchronized (session) {
            enrollment = (Enrollment) session.getAttribute(ENROLLMENT);
            session.removeAttribute(ENROLLMENT);
        }
        if (enrollment == null || !enrollment.expires().isAfter(Instant.now())
                || !enrollment.options().getUser().getName().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            var credential = webauthn.treeToValue(form.credential(), CREDENTIAL);
            operations.registerCredential(new ImmutableRelyingPartyRegistrationRequest(enrollment.options(),
                    new RelyingPartyPublicKey(credential, form.label().strip())));
        } catch (IllegalArgumentException | com.webauthn4j.verifier.exception.VerificationException
                 | tools.jackson.core.JacksonException failure) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid passkey registration");
        }
    }

    @GetMapping
    List<Passkey> list(@AuthenticationPrincipal SessionAccount account) {
        var user = users.findByUsername(account.getUsername());
        if (user == null) return List.of();
        return credentials.findByUserId(user.getId()).stream()
                .sorted(Comparator.comparing(CredentialRecord::getCreated))
                .map(record -> new Passkey(record.getCredentialId().toBase64UrlString(), record.getLabel()))
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal SessionAccount account, @PathVariable String id) {
        var user = users.findByUsername(account.getUsername());
        if (user == null || id.length() > 1364 || id.length() % 4 == 1 || !id.matches("[A-Za-z0-9_-]+")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Bytes identifier = Bytes.fromBase64(id);
        var record = credentials.findByCredentialId(identifier);
        if (record == null || !user.getId().equals(record.getUserEntityUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        credentials.delete(identifier);
    }
}
