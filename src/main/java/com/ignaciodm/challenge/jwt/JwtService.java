package com.ignaciodm.challenge.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secret_key;

	private static final long EXPIRATION = 120000; // ver si despues lo cambio por 3600000

	private final ReactiveUserDetailsService userDetailsService;

	public JwtService(ReactiveUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public String generateToken(UserDetails userDetails) {
		String token = Jwts.builder().setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
		// agregar luego el claim para admin
		expirationTest();

		return token;
	}

	private void expirationTest() {
		new Thread(() -> {
			System.out.println("Esperando 2 minutos para que el token expire...");
			try {
				Thread.sleep(120000);
				System.out.println("Pasaron 2 mins...");
			} catch (InterruptedException e) {
			}
		}).start();
	}

	private String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret_key);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public Mono<Authentication> getAuthentication(String token) {
		String username = extractUsername(token);
		return userDetailsService.findByUsername(username)
				.map(user -> new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
	}
}
