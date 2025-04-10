package com.ignaciodm.challenge.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Service
public class JwtService {

	private static final String ROLE_ = "ROLE_";

	private static final String ROLE = "role";

	@Value("${jwt.secret}")
	private String secret_key;

	private static final long EXPIRATION = 1800000; // 120000 2 mins para pruebas, 1800000 30min

	public String generateToken(User userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(ROLE, userDetails.getRole().name());
		String token = Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
		expirationTest();

		return token;
	}

	private void expirationTest() {
		new Thread(() -> {
			System.out.println("Esperando 30 minutos para que el token expire...");
			try {
				Thread.sleep(1800000);
				System.out.println("Pasaron 30 mins...");
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

	public boolean isTokenValid(String token, String username) {
		final String extractedUsername = extractUsername(token);
		return extractedUsername.equals(username) && !isTokenExpired(token);
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
		Claims claims = extractAllClaims(token);
		String username = claims.getSubject();
		String role = claims.get(ROLE, String.class);
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(ROLE_ + role);
		return Mono.just(new UsernamePasswordAuthenticationToken(username, null, List.of(authority)));
	}

}
