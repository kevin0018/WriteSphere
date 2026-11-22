package com.writesphere.writesphere.config;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.security.web.webauthn.authentication.HttpSessionPublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsFilter;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationProvider;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationRequestToken;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

final class PasskeyLogin {
    static final String OPTIONS = "/api/session/passkeys/login/options";
    static final String VERIFY = "/api/session/passkeys/login";

    private PasskeyLogin() {}

    static void configure(HttpSecurity http, UserDetailsService accounts, WebAuthnRelyingPartyOperations operations,
                          CsrfTokenRepository csrfTokens, JdbcClient jdbc, PlatformTransactionManager transactions) {
        var challenges = new LoginChallenges();
        var options = new PublicKeyCredentialRequestOptionsFilter(operations);
        options.setRequestMatcher(pathPattern(HttpMethod.POST, OPTIONS));
        options.setRequestOptionsRepository(challenges);

        var provider = new ProviderManager(new WebAuthnAuthenticationProvider(operations, accounts));
        var transaction = new TransactionTemplate(transactions);
        var login = new WebAuthnAuthenticationFilter();
        login.setRequiresAuthenticationRequestMatcher(pathPattern(HttpMethod.POST, VERIFY));
        login.setRequestOptionsRepository(challenges);
        login.setAuthenticationManager(request -> transaction.execute(status -> {
            var assertion = ((WebAuthnAuthenticationRequestToken) request).getWebAuthnRequest().getPublicKey();
            var credentialId = assertion == null ? null : assertion.getRawId();
            // Keep credential updates and revocation ordered in PostgreSQL.
            if (credentialId == null || jdbc.sql("SELECT credential_id FROM user_credentials WHERE credential_id = ? FOR UPDATE")
                    .param(credentialId.toBase64UrlString()).query(String.class).optional().isEmpty()) {
                throw new BadCredentialsException("Unknown passkey");
            }
            var authenticated = provider.authenticate(request);
            var principal = accounts.loadUserByUsername(authenticated.getName());
            if (principal instanceof CredentialsContainer credentials) credentials.eraseCredentials();
            return UsernamePasswordAuthenticationToken.authenticated(principal, null, authenticated.getAuthorities());
        }));
        login.setSessionAuthenticationStrategy(new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(csrfTokens))));
        login.setAuthenticationSuccessHandler((request, response, authentication) ->
                response.setStatus(HttpServletResponse.SC_NO_CONTENT));

        http.addFilterAfter(options, AuthorizationFilter.class);
        http.addFilterAfter(login, AuthorizationFilter.class);
    }

    private static final class LoginChallenges extends HttpSessionPublicKeyCredentialRequestOptionsRepository {
        private static final String EXPIRES = LoginChallenges.class.getName() + ".expires";

        @Override
        public void save(HttpServletRequest request, HttpServletResponse response, PublicKeyCredentialRequestOptions options) {
            if (options == null) return; // load consumes the challenge.
            var session = request.getSession();
            synchronized (session) {
                super.save(request, response, options);
                session.setAttribute(EXPIRES, Instant.now().plus(PasskeyConfiguration.CEREMONY_TIMEOUT));
            }
        }

        @Override
        public PublicKeyCredentialRequestOptions load(HttpServletRequest request) {
            var session = request.getSession(false);
            if (session == null) return null;
            synchronized (session) {
                var expiry = (Instant) session.getAttribute(EXPIRES);
                var options = super.load(request);
                super.save(request, null, null);
                session.removeAttribute(EXPIRES);
                return expiry != null && expiry.isAfter(Instant.now()) ? options : null;
            }
        }
    }
}
