package com.pp.economia_circular.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource).and()
                .csrf().disable()
                .authorizeRequests(auth -> auth
                        // Permitir OPTIONS para CORS preflight
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger UI y OpenAPI
                        .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", 
                                     "/swagger-resources/**", "/webjars/**").permitAll()
                        // Endpoints públicos
                        .antMatchers("/api/auth/**", "/api/registro/**", "/ping").permitAll()
                        .antMatchers("/api/events", "/api/events/upcoming", "/api/events/type/**", "/api/events/nearby").permitAll()
                        .antMatchers("/api/articles", "/api/articles/**").permitAll()
                        .antMatchers("/api/recycling-centers", "/api/recycling-centers/**").permitAll()
                        // Resto requiere autenticación
                        .anyRequest().authenticated()
                )
                .exceptionHandling()
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        return http.build();
    }
}
