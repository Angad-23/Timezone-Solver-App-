package com.personal.esttimeconverter.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/logo.png", "/styles.css", "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * A single account for this personal app. Set APP_USERNAME / APP_PASSWORD
     * as environment variables wherever this is deployed — the defaults here
     * are only a fallback for local testing and should not be relied on.
     */
    /**
     * Accounts for this personal app. The primary account comes from
     * APP_USERNAME / APP_PASSWORD (falling back to admin/changeme locally).
     * A second, optional account can be added via APP_USERNAME2 /
     * APP_PASSWORD2 — leave those unset if you only need one login.
     */
    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.username:admin}") String username,
            @Value("${app.security.password:changeme}") String password,
            @Value("${app.security.username2:}") String username2,
            @Value("${app.security.password2:}") String password2,
            PasswordEncoder encoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        manager.createUser(User.withUsername(username)
                .password(encoder.encode(password))
                .roles("USER")
                .build());

        if (!username2.isBlank() && !password2.isBlank()) {
            manager.createUser(User.withUsername(username2)
                    .password(encoder.encode(password2))
                    .roles("USER")
                    .build());
        }

        return manager;
    }
}
