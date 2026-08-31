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
    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.username:admin}") String username,
            @Value("${app.security.password:changeme}") String password,
            PasswordEncoder encoder) {
        UserDetails user = User.withUsername(username)
                .password(encoder.encode(password))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
