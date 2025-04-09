package com.ignaciodm.challenge.jwt;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

	private static final String EXPIRED_TOKEN = "Token expirado :(";
	private static final String INVALID_TOKEN = "Token inválido";
	private static final String BEARER_PREFIX = "Bearer ";
	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
			String jwt = authHeader.substring(7);

			try {
				return jwtService.getAuthentication(jwt).flatMap(authentication -> {
					if (!jwtService.isTokenValid(jwt, (UserDetails) authentication.getPrincipal())) {
						return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)).cast(Void.class);
					}

					SecurityContext context = new SecurityContextImpl(authentication);
					return chain.filter(exchange)
							.contextWrite(org.springframework.security.core.context.ReactiveSecurityContextHolder
									.withSecurityContext(Mono.just(context)));
				});
			} catch (ExpiredJwtException e) {
				System.out.println("ExpiredJwtException: " + EXPIRED_TOKEN);
				return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, EXPIRED_TOKEN)).cast(Void.class);
			} catch (Exception e) {
				System.out.println("ExpiredJwtException: " + INVALID_TOKEN);
				return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN)).cast(Void.class);
			}
		}

		return chain.filter(exchange);
	}

}
