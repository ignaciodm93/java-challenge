package com.ignaciodm.challenge.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.repository.SellingCostRepository;
import com.ignaciodm.challenge.repository.SellingPointRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SellingCostsService {

	private static final String SELLING_COST_ALREADY_EXISTS = "There is an existing selling cost already for this combination, please try another one.";
	private static final String AT_LEAST_ONE_SELLING_POINT_DOES_NOT_EXISTS = "At least one selling point does not exists.";
	private static final String HYPHEN = "-";
	private static final String CHEAPEST_PATH = "cheapestPath:";
	private static final String DIRECT_CONNECTIONS = "directConnections:";
	private static final String INITIAL_SELLING_COSTS_SAVED_SUCCESSFULLY_AND_CACHED_IN_REDIS = "Initial selling costs saved successfully and cached in Redis.";
	private static final int REDIS_TTL = 300;
	private static final int REDIS_TTL_SHORT = 10;

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

	@Autowired
	private SellingPointRepository sellingPointRepository;

	public Mono<Map<Integer, Map<Integer, Integer>>> getSellingPointsPaths() {
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

	public Mono<SellingCost> addSellingCost(SellingCost sellingCost) {
		Mono<Boolean> startingPointExists = sellingPointRepository.existsById(sellingCost.getStartingPoint());
		Mono<Boolean> endingPointExists = sellingPointRepository.existsById(sellingCost.getEndingPoint());

		return Mono.zip(startingPointExists, endingPointExists).flatMap(tuple -> {
			boolean startExists = tuple.getT1();
			boolean endExists = tuple.getT2();

			if (!startExists || !endExists) {
				return Mono.error(new IllegalArgumentException(AT_LEAST_ONE_SELLING_POINT_DOES_NOT_EXISTS));
			}

			return sellingCostDocumentRepository
					.findByStartingPointAndEndingPoint(sellingCost.getStartingPoint(), sellingCost.getEndingPoint())
					.flatMap((SellingCost existingCost) -> {
						return Mono.<SellingCost>error(new DuplicateKeyException(
								SELLING_COST_ALREADY_EXISTS));
					}).switchIfEmpty(sellingCostDocumentRepository.save(sellingCost));
		});
	}

	public Mono<SellingCost> updateSellingCost(Integer startingPoint, Integer endingPoint, SellingCost sellingCost) {
		String redisKey = DIRECT_CONNECTIONS + startingPoint;

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
									.thenReturn(updatedSellingCost);
						}).switchIfEmpty(Mono.defer(() -> {
							Map<Integer, Integer> newMap = new HashMap<>();
							newMap.put(endingPoint, updatedSellingCost.getCost());
							return redisTemplateSellingCost.opsForValue()
									.set(redisKey, newMap, Duration.ofSeconds(REDIS_TTL))
									.thenReturn(updatedSellingCost);
						}));
					});
				});
	}

	public Mono<Void> deleteSellingCost(Integer startingPoint, Integer endingPoint) {
		String redisKey = DIRECT_CONNECTIONS + startingPoint;

		return sellingCostDocumentRepository.findByStartingPointAndEndingPoint(startingPoint, endingPoint)
				.flatMap(existingSellingCost -> {
					return sellingCostDocumentRepository.delete(existingSellingCost)
							.then(redisTemplateSellingCost.opsForValue().get(redisKey).flatMap(directConnections -> {
								if (directConnections != null) {
									directConnections.remove(endingPoint);
									return redisTemplateSellingCost.delete(redisKey).then();
								} else {
									return Mono.empty();
								}
							}));
				}).then();
	}

	public Mono<Map<Integer, Integer>> getDirectConnections(Integer startingPoint) {
		String redisKey = DIRECT_CONNECTIONS + startingPoint;

		return redisTemplateSellingCost.opsForValue().get(redisKey)
				.switchIfEmpty(sellingCostDocumentRepository.findByStartingPoint(startingPoint)
						.collectMap(SellingCost::getEndingPoint, SellingCost::getCost).flatMap(connections -> {
							if (connections.isEmpty()) {
								return Mono.empty();
							}
							return redisTemplateSellingCost.opsForValue()
									.set(redisKey, connections, Duration.ofSeconds(REDIS_TTL)).thenReturn(connections);
						}));
	}

	public Mono<Map<String, Object>> getFullCheapestPath(Integer startingPoint, Integer endingPoint) {
		String key = CHEAPEST_PATH + startingPoint + HYPHEN + endingPoint;
		Mono<Map<String, Object>> redisMono = redisTemplateCheapestPath.opsForValue().get(key);
		Mono<Map<String, Object>> mongoMono = getSellingPointsPaths().flatMap(sellingPointsPaths -> {
			return integratedPathService.getFullCheapestPath(sellingPointsPaths, startingPoint, endingPoint)
					.flatMap(pathAndFare -> {
						return redisTemplateCheapestPath.opsForValue()
								.set(key, pathAndFare, Duration.ofSeconds(REDIS_TTL_SHORT)).thenReturn(pathAndFare);
					});
		});
		return redisMono.switchIfEmpty(mongoMono);
	}

	public Mono<String> saveInitialData() {
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
							.thenReturn(INITIAL_SELLING_COSTS_SAVED_SUCCESSFULLY_AND_CACHED_IN_REDIS);
				}));
	}

	private List<SellingCost> getInitialSellingCosts() {
		return List.of(new SellingCost(1, 2, 2), new SellingCost(1, 3, 3), new SellingCost(2, 3, 5),
				new SellingCost(2, 4, 10), new SellingCost(1, 4, 11), new SellingCost(4, 5, 5),
				new SellingCost(2, 5, 14), new SellingCost(6, 7, 32), new SellingCost(8, 9, 11),
				new SellingCost(10, 7, 5), new SellingCost(3, 8, 10), new SellingCost(5, 8, 30),
				new SellingCost(10, 5, 5), new SellingCost(4, 6, 6));
	};

	public Mono<String> flushAll() {
		return redisConnectionFactory.getReactiveConnection().serverCommands().flushAll()
				.then(Mono.just("Todas las keys borradas ok."));
	}
}