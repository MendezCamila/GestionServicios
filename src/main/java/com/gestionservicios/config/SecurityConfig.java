package com.gestionservicios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // permite TODO sin login
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable()); // desactiva la pantalla de login

        return http.build();
    }
}
