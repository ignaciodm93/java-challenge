package com.teamcubation.springsecurity.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        // Usuario en memoria (para simplificar el ejemplo)
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        // admin en memoria (para simplificar el ejemplo)
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()  // Permite el acceso al login sin autenticación
                        .anyRequest().authenticated()          // Cualquier otra URL requiere autenticación
                )
                .formLogin(form -> form
                        .loginPage("/login")                   // Página de inicio de sesión personalizada
                        .successHandler(customAuthenticationSuccessHandler())  // Redirección personalizada según rol
                        .failureUrl("/login?error=true")  // 🚩 Redirige con un parámetro de error si falla el login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")                  // URL para cerrar sesión
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.sendRedirect("/login?logout");  // Redirige manualmente después de cerrar sesión
                        })
                        .invalidateHttpSession(true)           // Invalida la sesión al cerrar sesión
                        .deleteCookies("JSESSIONID")           // Borra la cookie de sesión
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?invalid")   // Redirige si la sesión es inválida
                        .maximumSessions(1)                    // Permite solo una sesión por usuario
                ).headers(headers -> headers
                        .cacheControl(cache -> cache.disable())  // Desactiva la caché
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                // Verifica si el usuario tiene el rol de ADMIN
                boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    response.sendRedirect("/inicio"); // Si es ADMIN, redirige a "/inicio"
                } else {
                    response.sendRedirect("/home"); // Si no, redirige a "/home"
                }
            }
        };
    }
}

