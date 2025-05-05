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

import com.ignaciodm.challenge.interfaces.SellingPointApi;
import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.service.SellingPointService;

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
		return sellingPointService.findById(sellingPoint.getId())
				.flatMap(existingSellingPoint -> Mono
						.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SellingPoint())))
				.switchIfEmpty(sellingPointService.save(sellingPoint)
						.map(savedSellingPoint -> ResponseEntity.status(HttpStatus.CREATED).body(savedSellingPoint)))
				.map(responseEntity -> {
					if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
					}
					return responseEntity;
				});
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<SellingPoint>> updateOrCreateSellingPoint(@PathVariable Integer id,
			@RequestBody SellingPoint sellingPoint) {
		return sellingPointService.update(id, sellingPoint)
				.map(updatedSellingPoint -> ResponseEntity.ok(updatedSellingPoint))
				.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)));
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteSellingPoint(@PathVariable Integer id) {
		return sellingPointService.deleteById(id).map(result -> {
			if (result == 1L) {
				return ResponseEntity.status(HttpStatus.OK).build();
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
		});
	}

	@DeleteMapping("/flush-all")
	public Mono<ResponseEntity<Void>> clearCache() {
		return sellingPointService.clearCache().then(Mono.just(ResponseEntity.noContent().build()));
	}
}