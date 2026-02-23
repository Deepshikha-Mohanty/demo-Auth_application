package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Local DB Login
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {

            User user = userService.findByUsername(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(user.getRole())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Keep CSRF enabled
                .csrf(csrf -> csrf.ignoringRequestMatchers("/login"))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**").permitAll()
                        .requestMatchers("/admin-dashboard").hasAuthority("Admin")
                        .requestMatchers("/user-dashboard").authenticated() // allow both local + gluu
                        .anyRequest().authenticated()
                )

                // LOCAL LOGIN
                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {

                            String role = authentication.getAuthorities()
                                    .iterator().next().getAuthority();

                            if (role.equals("Admin")) {
                                response.sendRedirect("/admin-dashboard");
                            } else {
                                response.sendRedirect("/user-dashboard");
                            }
                        })
                        .permitAll()
                )

                // GLUU OIDC LOGIN
                .oauth2Login(oauth -> oauth
                        .loginPage("/")
                        .defaultSuccessUrl("/user-dashboard", true)
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

}