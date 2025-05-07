package com.ignaciodm.challenge.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.interfaces.SellingCostApi;
import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.service.SellingCostsService;

import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/selling-costs")
public class SellingCostController implements SellingCostApi {

	@Autowired
	private SellingCostsService sellingCostService;

	@GetMapping("/get-all-selling-points-paths")
	public Mono<Map<Integer, Map<Integer, Integer>>> getSellingPointsPaths() {
		return sellingCostService.getSellingPointsPaths();
	}

	@PostMapping("/add-selling-cost")
	public Mono<ResponseEntity<SellingCost>> addSellingCost(@RequestBody SellingCost sellingCost) {
		return sellingCostService.addSellingCost(sellingCost)
				.map(savedSellingCost -> ResponseEntity.status(HttpStatus.CREATED).body(savedSellingCost))
				.onErrorResume(IllegalArgumentException.class,
						error -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
				.onErrorResume(DuplicateKeyException.class,
						error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
	}

	@PutMapping("edit-existing-selling-cost/{startingPoint}/{endingPoint}")
	public Mono<ResponseEntity<SellingCost>> updateSellingCost(@PathVariable Integer startingPoint,
			@PathVariable Integer endingPoint, @RequestBody SellingCost sellingCost) {
		return sellingCostService.updateSellingCost(startingPoint, endingPoint, sellingCost).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("delete-selling-cost/{startingPoint}/{endingPoint}")
	public Mono<ResponseEntity<Object>> deleteSellingCost(@PathVariable Integer startingPoint,
			@PathVariable Integer endingPoint) {
		return sellingCostService.deleteSellingCost(startingPoint, endingPoint)
				.thenReturn(ResponseEntity.noContent().build()).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/direct-connections/{startingPoint}")
	public Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnections(@PathVariable Integer startingPoint) {
		return sellingCostService.getDirectConnections(startingPoint).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@Override
	@GetMapping("/full-cheapest-path")
	public Mono<ResponseEntity<Map<String, Object>>> getFullCheapestPath(@RequestParam Integer startingPoint,
			@RequestParam Integer endingPoint) {
		return sellingCostService.getFullCheapestPath(startingPoint, endingPoint).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping("/initialize-selling-costs")
	public Mono<ResponseEntity<String>> saveInitialData() {
		return sellingCostService.saveInitialData()
				.map(message -> ResponseEntity.status(HttpStatus.CREATED).body(message));
	}

}
