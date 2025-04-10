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

	private static final String ADMIN = "ADMIN";
	private static final String ACCREDITATIONS = "/accreditations/**";
	private static final String WEBJARS = "/webjars/**";
	private static final String SWAGGER_RESOURCES = "/swagger-resources/**";
	private static final String V3_API_DOCS = "/v3/api-docs/**";
	private static final String SWAGGER_UI = "/swagger-ui/**";
	private static final String SWAGGER_UI_HTML = "/swagger-ui.html";
	private static final String AUTH_REGISTER = "/auth/register";
	private static final String AUTH_LOGIN = "/auth/login";
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SpringSecurityConfig(JwtAuthenticationFilter jwtFilter) {
		this.jwtAuthenticationFilter = jwtFilter;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(csrf -> csrf.disable())
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers(AUTH_LOGIN, AUTH_REGISTER, SWAGGER_UI_HTML, SWAGGER_UI,
								V3_API_DOCS, SWAGGER_RESOURCES, WEBJARS)
						.permitAll().pathMatchers(ACCREDITATIONS).hasRole(ADMIN).anyExchange().authenticated())
				.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION).build();
	}

}