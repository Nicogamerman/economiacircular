package com.pp.economia_circular.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests(auth -> auth
                        .antMatchers("/api/auth/**", "/api/registro/**", "/ping").permitAll()
                        .antMatchers("/api/events", "/api/events/upcoming", "/api/events/type/**", "/api/events/nearby").permitAll()
                        .antMatchers("/api/articles", "/api/articles/**").permitAll()
                        .antMatchers("/api/recycling-centers", "/api/recycling-centers/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling()
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        return http.build();
    }
}
