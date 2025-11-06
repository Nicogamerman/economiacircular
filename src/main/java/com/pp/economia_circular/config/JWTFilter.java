package com.pp.economia_circular.config;

import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JWTFilter implements Filter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String authHeader = req.getHeader("Authorization");

        // 🔒 Evitar parsear JWT en rutas públicas o sin header
        if (path.startsWith("/api/auth") || path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs") || path.startsWith("/ping") ||
                authHeader == null || !authHeader.startsWith("Bearer ")) {

            chain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtService.extraerEmail(token);

            if (email != null && jwtService.validarToken(token)) {
                Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

                if (usuario != null && usuario.isActivo()) {
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + usuario.getRol())
                    );

                    User userDetails = new User(usuario.getEmail(), usuario.getContrasena(), authorities);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al validar token: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
