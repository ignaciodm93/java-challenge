package com.ignaciodm.challenge.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.service.SellingPointService;
import com.ignaciodm93.interfaces.SellingPointApi;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/selling-points")
public class SellingPointController implements SellingPointApi {

	@Autowired
	private SellingPointService sellingPointService;

	@GetMapping
	public Flux<SellingPoint> getAllSellingPoints() {
		return sellingPointService.findAll();
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<SellingPoint>> getSellingPointById(@PathVariable Integer id) {
		return sellingPointService.findById(id).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Mono<ResponseEntity<SellingPoint>> createSellingPoint(@RequestBody SellingPoint sellingPoint) {
		return sellingPointService.save(sellingPoint)
				.map(savedSellingPoint -> ResponseEntity.status(HttpStatus.CREATED).body(sellingPoint));
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<SellingPoint>> updateSellingPoint(@PathVariable Integer id,
			@RequestBody SellingPoint sellingPoint) {
		return sellingPointService.update(id, sellingPoint).map(updatedSellingPoint -> ResponseEntity.ok(sellingPoint))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteSellingPoint(@PathVariable Integer id) {
		return sellingPointService.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
	}

	@PostMapping("/initial-data")
	public Mono<ResponseEntity<Void>> saveInitialData() {
		return sellingPointService.saveInitialData().then(Mono.just(ResponseEntity.ok().build()));
	}

	@DeleteMapping("/flush-all")
	public Mono<ResponseEntity<Void>> clearCache() {
		return sellingPointService.clearCache().then(Mono.just(ResponseEntity.noContent().build()));
	}
}