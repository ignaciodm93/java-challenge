package com.ignaciodm.challenge.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.SellingPointDocument;
import com.ignaciodm.challenge.repository.SellingPointRepository;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mongo-test")
public class MongoTestController {

	@Autowired
	private SellingPointRepository sellingPointRepository;

	@PostMapping
	public Mono<ResponseEntity<SellingPointDocument>> testCreate(@RequestBody SellingPointDocument sellingPoint) {
		return sellingPointRepository.save(sellingPoint)
				.map(savedSellingPoint -> ResponseEntity.status(HttpStatus.CREATED).body(savedSellingPoint));
	}

	@GetMapping("/all")
	public Mono<ResponseEntity<java.util.List<SellingPointDocument>>> getAllSellingPoints() {
		return sellingPointRepository.findAll().collectList().map(ResponseEntity::ok);
	}

}
