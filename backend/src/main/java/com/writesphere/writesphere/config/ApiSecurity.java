package com.writesphere.writesphere.config;

import com.writesphere.writesphere.user.application.AccountStore;
import com.writesphere.writesphere.user.application.PasswordHash;
import com.writesphere.writesphere.user.domain.Account;
import com.writesphere.writesphere.user.infrastructure.SessionAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ApiSecurity {
    @Bean
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.GET, "/api/public/**", "/api/session", "/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/accounts", "/api/session/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/moderation/**").hasRole("MODERATOR")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(
                        (request, response, failure) -> response.setStatus(401)))
                .formLogin(login -> login.loginProcessingUrl("/api/session/login")
                        .usernameParameter("email")
                        .successHandler((request, response, authentication) -> response.setStatus(204))
                        .failureHandler((request, response, failure) -> response.setStatus(401)))
                .logout(logout -> logout.logoutUrl("/api/session/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .requestCache(cache -> cache.disable())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    PasswordHash passwordHash(PasswordEncoder encoder) {
        return encoder::encode;
    }

    @Bean
    UserDetailsService userDetailsService(AccountStore accounts) {
        return email -> accounts.findByEmail(Account.normalize(email))
                .map(account -> new SessionAccount(account.id(), account.email(), account.penName(),
                        account.role(), account.passwordHash()))
                .orElseThrow(() -> new UsernameNotFoundException("Unknown account"));
    }
}
