package com.ignaciodm.challenge.controllers;

import java.util.List;

import org.redisson.api.RMapReactive;
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
import com.ignaciodm.challenge.service.CacheService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/selling-points")
public class SellingPointsController implements SellingPointsApi {

	private static final String ERROR_SELLING_POINT_DOESN_T_EXIST = "Error, selling point doesn't exist.";
	private static final String SELLING_POINT_NOT_FOUND = "Selling point not found.";
	private static final String SELLING_POINT_DELETED = "Selling point deleted.";
	private static final String NEW_SELLING_POINT_CREATED = "New selling point created";
	private static final String ERROR_IDENTIFIER_ALREADY_IN_USE = "Error, identifier already in use.";
	private final CacheService cacheService;

	public SellingPointsController(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Override
	@GetMapping("/all")
	public Mono<ResponseEntity<List<SellingPoint>>> getAll() {
		return cacheService.getSellingPointsCache().valueIterator().collectList().map(ResponseEntity::ok);
	}

	@Override
	@PostMapping("/add")
	public Mono<ResponseEntity<String>> add(@RequestBody SellingPoint sellingPoint) {
		RMapReactive<Integer, SellingPoint> cache = cacheService.getSellingPointsCache();

		return cache.containsKey(sellingPoint.getId())
				.flatMap(exists -> exists ? Mono.just(ResponseEntity.badRequest().body(ERROR_IDENTIFIER_ALREADY_IN_USE))
						: cache.put(sellingPoint.getId(), sellingPoint)
								.thenReturn(ResponseEntity.ok(NEW_SELLING_POINT_CREATED)));
	}

	@Override
	@PutMapping("/{id}")
	public Mono<ResponseEntity<String>> update(@RequestBody SellingPoint sellingPoint) {
		return cacheService.getSellingPointsCache().containsKey(sellingPoint.getId()).flatMap(exists -> {
			if (exists) {
				return cacheService.getSellingPointsCache().put(sellingPoint.getId(), sellingPoint)
						.thenReturn(new ResponseEntity<>(HttpStatus.OK));
			} else {
				return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_SELLING_POINT_DOESN_T_EXIST));
			}
		});
	}

	@Override
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<String>> delete(@PathVariable Integer id) {
		return cacheService.getSellingPointsCache().remove(id).flatMap(
				deletedValue -> Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body(SELLING_POINT_DELETED)))
				.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(SELLING_POINT_NOT_FOUND)));
	}
}