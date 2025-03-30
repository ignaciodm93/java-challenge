package com.ignaciodm.challenge.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.SellingCostDocument;

import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/integrated-selling-costs")
public class IntegratedSellingCostsController implements IntegratedSellingCostsApi {

	private static final String SELLING_COSTS_INITIALIZED_SUCCESSFULLY = "Selling costs initialized successfully";
	private static final String CONNECTION_NOT_FOUND_PLEASE_TRY_ANOTHER_ONE = "Connection not found, please try another one.";
	private static final String SELLING_COST_REMOVED_SUCCESFULLY = "Selling cost removed succesfully.";

	@Override
	public Mono<ResponseEntity<List<SellingCostDocument>>> getAllSellingCosts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<SellingCostDocument>> addOrUpdateSellingCost(SellingCostDocument sellingCostDocument) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<String>> removeSellingCost(Integer startingPoint, Integer endingPoint) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnection(Integer sellingPointToDiscoverPathsId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<Object>> getFullCheapestPath(Integer startingPoint, Integer endingPoint) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Autowired
//	private IntegratedPathService integratedPathService;
//
//	@Autowired
//	private SellingCostRepository sellingCostDocumentRepository;
//
//	@Override
//	@GetMapping("/all")
//	public Mono<ResponseEntity<List<SellingCostDocument>>> getAllSellingCosts() {
//		return integratedPathService.getAllSellingCosts().map(ResponseEntity::ok);
//	}
//
//	@Override
//	@PostMapping("/new-selling-cost")
//	public Mono<ResponseEntity<SellingCostDocument>> addOrUpdateSellingCost(
//			@RequestBody SellingCostDocument sellingCostDocument) {
//		return integratedPathService.saveSellingCost(sellingCostDocument).map(ResponseEntity::ok);
//	}
//
//	@Override
//	@DeleteMapping("/remove-selling-cost")
//	public Mono<ResponseEntity<String>> removeSellingCost(@RequestParam Integer startingPoint,
//			@RequestParam Integer endingPoint) {
//		return integratedPathService.deleteSellingCost(startingPoint, endingPoint)
//				.then(Mono.just(ResponseEntity.ok(SELLING_COST_REMOVED_SUCCESFULLY))).switchIfEmpty(
//						Mono.just(ResponseEntity.badRequest().body(CONNECTION_NOT_FOUND_PLEASE_TRY_ANOTHER_ONE)));
//	}
//
//	@Override
//	@GetMapping("/direct-connection")
//	public Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnection(
//			@RequestParam Integer sellingPointToDiscoverPathsId) {
//		return integratedPathService.getDirectConnection(sellingPointToDiscoverPathsId).map(directConnections -> {
//			if (directConnections.isEmpty()) {
//				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(directConnections);
//			}
//			return ResponseEntity.status(HttpStatus.OK).body(directConnections);
//		});
//	}
//
//	@Override
//	@GetMapping("/full-cheapest-path")
//	public Mono<ResponseEntity<Object>> getFullCheapestPath(@RequestParam Integer startingPoint,
//			@RequestParam Integer endingPoint) {
//		return integratedPathService.getFullCheapestPath(startingPoint, endingPoint)
//				.map(pathAndFare -> ResponseEntity.ok().body(pathAndFare));
//	}
//
//	@PostMapping("/initialize")
//	public Mono<ResponseEntity<String>> initializeSellingCosts() {
//		List<SellingCostDocument> initialSellingCosts = List.of(new SellingCostDocument(1, 2, 2, 1),
//				new SellingCostDocument(1, 3, 3, 2), new SellingCostDocument(2, 3, 5, 3),
//				new SellingCostDocument(2, 4, 10, 4), new SellingCostDocument(1, 4, 11, 5),
//				new SellingCostDocument(4, 5, 5, 6), new SellingCostDocument(2, 5, 14, 7),
//				new SellingCostDocument(6, 7, 32, 8), new SellingCostDocument(8, 9, 11, 9),
//				new SellingCostDocument(10, 7, 5, 10), new SellingCostDocument(3, 8, 10, 11),
//				new SellingCostDocument(5, 8, 30, 12), new SellingCostDocument(10, 5, 5, 13),
//				new SellingCostDocument(4, 6, 6, 14));
//
//		return sellingCostDocumentRepository.saveAll(initialSellingCosts)
//				.then(Mono.just(ResponseEntity.ok(SELLING_COSTS_INITIALIZED_SUCCESSFULLY)));
//	}

}