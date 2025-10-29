package com.pp.economia_circular.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@TestConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests(auth -> auth
                        // Endpoints públicos
                        .antMatchers("/api/auth/**", "/api/registro/**", "/api/registrar/**", "/ping").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/events", "/api/events/upcoming", "/api/events/type/**", "/api/events/nearby").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/articles", "/api/articles/search", "/api/articles/category/**", 
                                     "/api/articles/most-viewed", "/api/articles/user/**").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/articles/{id}").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/recycling-centers", "/api/recycling-centers/type/**", "/api/recycling-centers/nearby").permitAll()
                        // Crear/Actualizar/Eliminar eventos y centros requiere ADMIN
                        .antMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.POST, "/api/recycling-centers/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PUT, "/api/recycling-centers/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/api/recycling-centers/**").hasRole("ADMIN")
                        // Resto requiere autenticación
                        .anyRequest().authenticated()
                )
                .exceptionHandling()
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        return http.build();
    }
}

