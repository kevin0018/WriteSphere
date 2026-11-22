package com.writesphere.writesphere.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.web.webauthn.api.AuthenticatorSelectionCriteria;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.api.ResidentKeyRequirement;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;

@Configuration(proxyBeanMethods = false)
class PasskeyConfiguration {
    static final Duration CEREMONY_TIMEOUT = Duration.ofMinutes(5);

    @Bean
    JdbcPublicKeyCredentialUserEntityRepository passkeyUsers(JdbcOperations jdbc) {
        return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
    }

    @Bean
    JdbcUserCredentialRepository passkeyCredentials(JdbcOperations jdbc) {
        return new JdbcUserCredentialRepository(jdbc);
    }

    @Bean
    Webauthn4JRelyingPartyOperations passkeyOperations(
            PublicKeyCredentialUserEntityRepository users, UserCredentialRepository credentials,
            @Value("${writesphere.passkeys.rp-id}") String rpId,
            @Value("${writesphere.passkeys.origins}") String origins) {
        var rp = PublicKeyCredentialRpEntity.builder().id(rpId).name("WriteSphere").build();
        var allowedOrigins = Arrays.stream(origins.split(",")).map(String::strip).collect(Collectors.toUnmodifiableSet());
        var operations = new Webauthn4JRelyingPartyOperations(users, credentials, rp, allowedOrigins);
        operations.setCustomizeCreationOptions(options -> options.authenticatorSelection(
                AuthenticatorSelectionCriteria.builder().residentKey(ResidentKeyRequirement.REQUIRED)
                        .userVerification(UserVerificationRequirement.REQUIRED).build())
                .timeout(CEREMONY_TIMEOUT));
        operations.setCustomizeRequestOptions(options -> options
                .userVerification(UserVerificationRequirement.REQUIRED).timeout(CEREMONY_TIMEOUT));
        return operations;
    }
}
