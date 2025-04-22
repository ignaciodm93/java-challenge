package com.ignaciodm.challenge.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.AuthResponse;
import com.ignaciodm.challenge.models.LoginRequest;
import com.ignaciodm.challenge.models.RegisterRequest;
import com.ignaciodm.challenge.service.AuthService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private static final String INVALID_CREDENTIALS = "Invalid credentials";
	private static final String REGISTER_FAILED = "Register failed";
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest req) {
		return authService.login(req).map(ResponseEntity::ok).onErrorResume(
				err -> Mono.just(ResponseEntity.badRequest().body(new AuthResponse(INVALID_CREDENTIALS))));
	}

	@PostMapping("/register")
	public Mono<ResponseEntity<AuthResponse>> register(@RequestBody RegisterRequest req) {
		return authService.register(req).map(ResponseEntity::ok)
				.onErrorResume(err -> Mono.just(ResponseEntity.badRequest().body(new AuthResponse(REGISTER_FAILED))));
	}
}
