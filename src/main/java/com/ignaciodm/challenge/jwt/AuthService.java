package com.ignaciodm.challenge.jwt;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.AuthResponse;
import com.ignaciodm.challenge.models.LoginRequest;
import com.ignaciodm.challenge.models.RegisterRequest;
import com.ignaciodm.challenge.models.User;
import com.ignaciodm.challenge.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
	}

	public Mono<AuthResponse> login(LoginRequest req) {
		return userRepository.findByUsername(req.getUsername())
				.filter(user -> passwordEncoder.matches(req.getPassword(), user.getPassword()))
				.map(user -> new AuthResponse(jwtService.generateToken(user)))
				.switchIfEmpty(Mono.error(new RuntimeException("invalid credentials")));
	}

	public Mono<AuthResponse> register(RegisterRequest req) {
		User user = new User(req.getUsername(), req.getLastname(), req.getLastname(), req.getCountry(),
				passwordEncoder.encode(req.getPassword()), req.getRole());

		return userRepository.save(user).map(savedUser -> new AuthResponse(jwtService.generateToken(savedUser)));
	}

}
