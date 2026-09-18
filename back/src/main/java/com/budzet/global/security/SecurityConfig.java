package com.budzet.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationFilter customAuthenticationFilter) throws Exception{
        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers("/users/join", "users/login").permitAll()
                        .anyRequest().authenticated())
                .csrf((csrf) -> csrf.disable())
                .addFilterBefore(customAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }
}
