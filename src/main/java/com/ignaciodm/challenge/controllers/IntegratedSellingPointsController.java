package com.ignaciodm.challenge.controllers;

import java.util.List;
import java.util.stream.Collectors;

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

import com.ignaciodm.challenge.models.SellingPointDocument;
import com.ignaciodm.challenge.repository.SellingPointRepository;
import com.ignaciodm.challenge.service.IntegratedCacheService;

import reactor.core.publisher.Mono;

//ok falta redis
@RestController
@RequestMapping("/integrated-selling-points")
public class IntegratedSellingPointsController implements IntegratedSellingPointsApi {

	private static final String INITIAL_SELLING_POINTS_ADDED_SUCCESSFULLY = "Initial selling points added successfully";
	private static final String ERROR_SELLING_POINT_DOESN_T_EXIST = "Error, selling point doesn't exist.";
	private static final String SELLING_POINT_NOT_FOUND = "Selling point not found.";
	private static final String SELLING_POINT_DELETED = "Selling point deleted.";
	private static final String NEW_SELLING_POINT_CREATED = "New selling point created";
	private static final String ERROR_IDENTIFIER_ALREADY_IN_USE = "Error, identifier already in use.";
	private static final String SELLING_POINT_UPDATED_SUCCESSFULLY = "Selling point updated successfully";

	private SellingPointRepository sellingPointRepository;

	public IntegratedSellingPointsController(SellingPointRepository sellingPointRepository) {
		this.sellingPointRepository = sellingPointRepository;
	}

//	@Override
//	@GetMapping("/all")
//	public Mono<ResponseEntity<List<SellingPointDocument>>> getAll() {
//		return sellingPointRepository.findAll().collectList().map(ResponseEntity::ok);
//	}

	@Autowired
	private IntegratedCacheService integartedCacheService;

	// Test redis y mongo , ok
	@Override
	@GetMapping("/all")
	public Mono<ResponseEntity<List<SellingPointDocument>>> getAll() {
		return integartedCacheService.getSellingPointsCache().valueIterator().collectList().flatMap(cachedPoints -> {
			if (!cachedPoints.isEmpty()) {
				return Mono.just(ResponseEntity.ok(cachedPoints));
			} else {
				return sellingPointRepository.findAll().collectList().flatMap(mongoPoints -> {
					return integartedCacheService.getSellingPointsCache()
							.putAll(mongoPoints.stream()
									.collect(Collectors.toMap(SellingPointDocument::getId,
											sellingPointDocument -> sellingPointDocument)))
							.then(Mono.just(ResponseEntity.ok(mongoPoints)));
				});
			}
		});
	}

	// Falta agregar redis de acà para abajo
	@Override
	@PostMapping("/add")
	public Mono<ResponseEntity<String>> add(@RequestBody SellingPointDocument sellingPointDocument) {
		return sellingPointRepository.findById(sellingPointDocument.getId()).hasElement().flatMap(exists -> {
			if (exists) {
				return Mono.just(ResponseEntity.badRequest().body(ERROR_IDENTIFIER_ALREADY_IN_USE));
			} else {
				return sellingPointRepository.save(sellingPointDocument)
						.thenReturn(ResponseEntity.ok(NEW_SELLING_POINT_CREATED));
			}
		});
	}

	@Override
	@PutMapping("/{id}")
	public Mono<ResponseEntity<String>> update(@RequestBody SellingPointDocument sellingPointDocument) {
		return sellingPointRepository.findById(sellingPointDocument.getId())
				.flatMap(existingSellingPoint -> sellingPointRepository.save(sellingPointDocument)
						.thenReturn(ResponseEntity.ok(SELLING_POINT_UPDATED_SUCCESSFULLY)))
				.switchIfEmpty(Mono.just(ResponseEntity.badRequest().body(ERROR_SELLING_POINT_DOESN_T_EXIST)));
	}

	@Override
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<String>> delete(@PathVariable int id) {
		return sellingPointRepository.findById(id)
				.flatMap(sellingPointDocument -> sellingPointRepository.delete(sellingPointDocument)
						.then(Mono.just(ResponseEntity.ok().body(SELLING_POINT_DELETED))))
				.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(SELLING_POINT_NOT_FOUND)));

	}

	@PostMapping("/initialize")
	public Mono<ResponseEntity<String>> initializeSellingPoints() {
		List<SellingPointDocument> initialSellingPoints = List.of(new SellingPointDocument(1, "CABA"),
				new SellingPointDocument(2, "GBA_1"), new SellingPointDocument(3, "GBA_2"),
				new SellingPointDocument(4, "Santa Fe"), new SellingPointDocument(5, "Córdoba"),
				new SellingPointDocument(6, "Misiones"), new SellingPointDocument(7, "Salta"),
				new SellingPointDocument(8, "Chubut"), new SellingPointDocument(9, "Santa Cruz"),
				new SellingPointDocument(10, "Catamarca"));

		return sellingPointRepository.saveAll(initialSellingPoints)
				.then(Mono.just(ResponseEntity.ok(INITIAL_SELLING_POINTS_ADDED_SUCCESSFULLY)));
	}
}
