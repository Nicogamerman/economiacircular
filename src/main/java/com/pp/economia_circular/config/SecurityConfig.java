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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource).and()
                .csrf().disable()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests(auth -> auth
                        // Permitir OPTIONS para CORS preflight
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger UI y OpenAPI
                        .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", 
                                     "/swagger-resources/**", "/webjars/**").permitAll()
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
