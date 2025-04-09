package com.ignaciodm.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.ignaciodm.challenge.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SpringSecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SpringSecurityConfig(JwtAuthenticationFilter jwtFilter) {
		this.jwtAuthenticationFilter = jwtFilter;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.csrf(csrf -> csrf
						.disable())
				.authorizeExchange(
						exchanges -> exchanges.pathMatchers("/auth/login", "/auth/register", "/swagger-ui.html",
								"/swagger-ui/**", "/v3/api-docs/**").permitAll().anyExchange().authenticated())
				.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION).build();
		// cambiar luego cuando se agrege admin
	}

}