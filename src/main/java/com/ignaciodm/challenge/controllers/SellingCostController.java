package com.ignaciodm.challenge.controllers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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

import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.repository.SellingCostRepository;
import com.ignaciodm.challenge.service.PathsService;
import com.ignaciodm.interfaces.SellingCostApi;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/selling-costs")
public class SellingCostController implements SellingCostApi {

	private static final int REDIS_TTL = 300;

	@Autowired
	private PathsService integratedPathService;

	@Autowired
	private SellingCostRepository sellingCostDocumentRepository;

	@Autowired
	private ReactiveRedisTemplate<String, Map<Integer, Integer>> redisTemplateSellingCost;

	@Autowired
	private ReactiveRedisTemplate<String, Map<String, Object>> redisTemplateCheapestPath;

	@Autowired
	private ReactiveRedisConnectionFactory redisConnectionFactory;

	private static final Logger log = LoggerFactory.getLogger(SellingCostApi.class);

	@GetMapping("/get-all-selling-points-paths")
	private Mono<Map<Integer, Map<Integer, Integer>>> getSellingPointsPaths() {
		return sellingCostDocumentRepository.findAll().collectList().map(sellingCosts -> {
			Map<Integer, Map<Integer, Integer>> sellingPointsPaths = new HashMap<>();
			sellingCosts.forEach(sellingCost -> {
				Map<Integer, Integer> startPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getStartingPoint(), path -> new HashMap<>());
				startPointConnections.put(sellingCost.getEndingPoint(), sellingCost.getCost());
				Map<Integer, Integer> endPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getEndingPoint(), path -> new HashMap<>());
				endPointConnections.put(sellingCost.getStartingPoint(), sellingCost.getCost());
			});
			return sellingPointsPaths;
		});
	}

	@PostMapping("/add-selling-cost")
	public Mono<ResponseEntity<SellingCost>> addSellingCost(@RequestBody SellingCost sellingCost) {
		return sellingCostDocumentRepository.save(sellingCost)
				.map(savedSellingCost -> ResponseEntity.status(HttpStatus.CREATED).body(savedSellingCost));
	}

	@PutMapping("edit-existing-selling-cost/{startingPoint}/{endingPoint}")
	public Mono<ResponseEntity<SellingCost>> updateSellingCost(@PathVariable Integer startingPoint,
			@PathVariable Integer endingPoint, @RequestBody SellingCost sellingCost) {

		String redisKey = "directConnections:" + startingPoint;

		return sellingCostDocumentRepository.findByStartingPointAndEndingPoint(startingPoint, endingPoint)
				.flatMap(existingSellingCost -> {
					existingSellingCost.setCost(sellingCost.getCost());
					return sellingCostDocumentRepository.save(existingSellingCost).flatMap(updatedSellingCost -> {
						return redisTemplateSellingCost.opsForValue().get(redisKey).flatMap(directConnections -> {
							if (directConnections == null) {
								directConnections = new HashMap<>();
							}
							directConnections.put(endingPoint, updatedSellingCost.getCost());
							return redisTemplateSellingCost.opsForValue()
									.set(redisKey, directConnections, Duration.ofSeconds(REDIS_TTL))
									.thenReturn(ResponseEntity.ok(updatedSellingCost));
						}).switchIfEmpty(Mono.defer(() -> {
							Map<Integer, Integer> newMap = new HashMap<>();
							newMap.put(endingPoint, updatedSellingCost.getCost());
							return redisTemplateSellingCost.opsForValue()
									.set(redisKey, newMap, Duration.ofSeconds(REDIS_TTL))
									.thenReturn(ResponseEntity.ok(updatedSellingCost));
						}));
					});
				}).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@DeleteMapping("delete-selling-cost/{startingPoint}/{endingPoint}")
	public Mono<ResponseEntity<Object>> deleteSellingCost(@PathVariable Integer startingPoint,
			@PathVariable Integer endingPoint) {

		String redisKey = "directConnections:" + startingPoint;

		return sellingCostDocumentRepository.findByStartingPointAndEndingPoint(startingPoint, endingPoint)
				.flatMap(existingSellingCost -> {
					return sellingCostDocumentRepository.delete(existingSellingCost)
							.then(redisTemplateSellingCost.opsForValue().get(redisKey).flatMap(directConnections -> {
								if (directConnections != null) {
									directConnections.remove(endingPoint);
									return redisTemplateSellingCost.delete(redisKey)
											.thenReturn(ResponseEntity.noContent().build());
								} else {
									return Mono.just(ResponseEntity.noContent().build());
								}
							}));
				}).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	// ok para una direccion
	@GetMapping("/direct-connections/{startingPoint}")
	public Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnections(@PathVariable Integer startingPoint) {

		String redisKey = "directConnections:" + startingPoint;

		return redisTemplateSellingCost.opsForValue().get(redisKey)
				.switchIfEmpty(sellingCostDocumentRepository.findByStartingPoint(startingPoint)
						.collectMap(SellingCost::getEndingPoint, SellingCost::getCost).flatMap(connections -> {
							if (connections.isEmpty()) {
								return Mono.empty();
							}
							return redisTemplateSellingCost.opsForValue()
									.set(redisKey, connections, Duration.ofSeconds(REDIS_TTL)).thenReturn(connections);
						}))
				.map(connections -> ResponseEntity.ok(connections)).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@Override
	@GetMapping("/full-cheapest-path")
	public Mono<ResponseEntity<Map<String, Object>>> getFullCheapestPath(@RequestParam Integer startingPoint,
			@RequestParam Integer endingPoint) {
		String key = "cheapestPath:" + startingPoint + "-" + endingPoint;
		Mono<ResponseEntity<Map<String, Object>>> redisMono = redisTemplateCheapestPath.opsForValue().get(key)
				.map(ResponseEntity::ok);
		Mono<ResponseEntity<Map<String, Object>>> mongoMono = getSellingPointsPaths().flatMap(sellingPointsPaths -> {
			log.debug("Fetching full cheapest path from {} to {}", startingPoint, endingPoint);
			return integratedPathService.getFullCheapestPath(sellingPointsPaths, startingPoint, endingPoint)
					.doOnNext(pathAndFare -> {
						log.debug("Cheapest path found: {}", pathAndFare);
					}).flatMap(pathAndFare -> {
						return redisTemplateCheapestPath.opsForValue()
								.set(key, pathAndFare, Duration.ofSeconds(REDIS_TTL))
								.thenReturn(ResponseEntity.ok(pathAndFare));
					});
		});
		return redisMono.switchIfEmpty(mongoMono).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping("/initialize-selling-costs")
	public Mono<ResponseEntity<String>> saveInitialData() {
		List<SellingCost> initialSellingCosts = getInitialSellingCosts();

		return Flux.fromIterable(initialSellingCosts)
				.flatMap(sellingCost -> sellingCostDocumentRepository.save(sellingCost)).then(Mono.defer(() -> {

					Map<String, Object> sellingCostMap = new HashMap<>();
					initialSellingCosts.forEach(sellingCost -> {
						sellingCostMap.put(sellingCost.getIdentifierKeyRoute(), sellingCost.getCost());
					});

					String redisKey = "sellingCostsData";
					return redisTemplateCheapestPath.opsForValue()
							.set(redisKey, sellingCostMap, Duration.ofSeconds(300))
							.thenReturn(ResponseEntity.status(HttpStatus.CREATED)
									.body("Initial selling costs saved successfully and cached in Redis."));
				}));
	}

	private List<SellingCost> getInitialSellingCosts() {
		return List.of(new SellingCost(1, 2, 2), new SellingCost(1, 3, 3), new SellingCost(2, 3, 5),
				new SellingCost(2, 4, 10), new SellingCost(1, 4, 11), new SellingCost(4, 5, 5),
				new SellingCost(2, 5, 14), new SellingCost(6, 7, 32), new SellingCost(8, 9, 11),
				new SellingCost(10, 7, 5), new SellingCost(3, 8, 10), new SellingCost(5, 8, 30),
				new SellingCost(10, 5, 5), new SellingCost(4, 6, 6));
	};

	// borrar luego, para pruebas locales
	@DeleteMapping("/flushall")
	public Mono<String> flushAll() {
		return redisConnectionFactory.getReactiveConnection().serverCommands().flushAll()
				.then(Mono.just("Todas las keys borradas ok."));
	}

}
