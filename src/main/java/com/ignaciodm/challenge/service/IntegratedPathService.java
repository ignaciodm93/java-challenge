package com.ignaciodm.challenge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.SellingCostDocument;
import com.ignaciodm.challenge.repository.SellingCostRepository;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Service
public class IntegratedPathService {

	private static final String FINAL_FARE = "final fare: ";
	private static final String PATH = "path: ";

	@Autowired
	private SellingCostRepository sellingDocumentRepository;

	@Autowired
	private RedissonReactiveClient redissonClient;

	private Map<Integer, Map<Integer, Integer>> sellingPointsPaths = new HashMap<>();
	private Mono<Void> initializationMono;

	@PostConstruct
	public void initialize() {
		this.initializationMono = initializePathConnectionsGraph().cache();
	}

	private Mono<Void> initializePathConnectionsGraph() {
		return sellingDocumentRepository.findAll().collectList().doOnNext(sellingCosts -> {
			sellingCosts.forEach(sellingCost -> {
				Map<Integer, Integer> startPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getStartingPoint(), path -> new HashMap<>());
				startPointConnections.put(sellingCost.getEndingPoint(), sellingCost.getCost());

				Map<Integer, Integer> endPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getEndingPoint(), path -> new HashMap<>());
				endPointConnections.put(sellingCost.getStartingPoint(), sellingCost.getCost());
			});
		}).then();
	}

	public Mono<Map<String, Object>> getFullCheapestPath(Integer startingPoint, Integer endingPoint) {
		String cacheKey = "path:" + startingPoint + ":" + endingPoint;
		System.out.println("cachekey: " + cacheKey);

		RMapReactive<String, Object> redisMap = redissonClient.getMap(cacheKey);
		System.out.println("CACHE: " + redisMap.get(cacheKey).subscribe());
		System.out.println("GET CACHE: " + redisMap.get(cacheKey).subscribe().toString());
		return redisMap.get(cacheKey).doOnNext(cachedPath -> System.out.println("valde redis: " + cachedPath))
				.flatMap(cachedPath -> {
					if (cachedPath != null) {
						System.out.println("fentro del flatMap, valor encontrado en redis ");
						try {
							return Mono.just((Map<String, Object>) cachedPath);
						} catch (ClassCastException e) {
							System.err.println("throw de prueba " + e.getMessage());
							return Mono.error(new RuntimeException("Error al castear valor de Redis"));
						}
					} else {
						System.out.println("no encontrado en redis");
						return Mono.fromCallable(() -> {
							Map<String, Object> pathAndFare = new HashMap<>();
							if (!sellingPointsPaths.containsKey(startingPoint)
									|| !sellingPointsPaths.containsKey(endingPoint)) {
								System.out.println("No se encuentran los puntos de venta en la ruta");
								return pathAndFare;
							}

							Map<Integer, Integer> lengths = new HashMap<>();
							Map<Integer, Integer> previous = new HashMap<>();
							PriorityQueue<int[]> nodesToProcess = new PriorityQueue<>(
									Comparator.comparingInt(a -> a[1]));
							initialize(startingPoint, lengths, previous, nodesToProcess);
							processPaths(endingPoint, lengths, previous, nodesToProcess);
							buildPath(endingPoint, pathAndFare, previous);

							System.out.println("resultado calculado desde la base, pathAndFare: " + pathAndFare);
							return pathAndFare;
						}).map(pathAndFare -> {
							System.out.println("dentro de map, pathAndFare: " + pathAndFare);
							return pathAndFare;
						}).flatMap(pathAndFare -> {
							System.out.println("guardando resultado en redis prueba");
							return redisMap.put(cacheKey, pathAndFare)
									.doOnSuccess(aVoid -> System.out.println("guardado en redis ok."))
									.doOnError(error -> {
										System.err.println("error al guardar en redis: " + error.getMessage());
										Mono.error(new RuntimeException("error al guardar en redis")); // prueba a ver
																										// si frena la
																										// ejecuciòn
									}).thenReturn(pathAndFare);
						});
					}
				}).doOnTerminate(() -> System.out.println("Proceso de consulta a Redis/MongoDB terminado."))
				.doOnError(error -> System.err.println("Error en el flujo: " + error.getMessage()));
	}

	// prueba
//	public Mono<Map<String, Object>> getFullCheapestPath(Integer startingPoint, Integer endingPoint) {
//		System.out.println("Calculando ruta más económica para: " + startingPoint + " -> " + endingPoint);
//
//		return Mono.fromCallable(() -> {
//			Map<String, Object> pathAndFare = new HashMap<>();
//			if (!sellingPointsPaths.containsKey(startingPoint) || !sellingPointsPaths.containsKey(endingPoint)) {
//				System.out.println("No se encuentran los puntos de venta en la ruta.");
//				return pathAndFare;
//			}
//
//			Map<Integer, Integer> lengths = new HashMap<>();
//			Map<Integer, Integer> previous = new HashMap<>();
//			PriorityQueue<int[]> nodesToProcess = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
//			initialize(startingPoint, lengths, previous, nodesToProcess);
//			processPaths(endingPoint, lengths, previous, nodesToProcess);
//			buildPath(endingPoint, pathAndFare, previous);
//
//			System.out.println("Resultado calculado desde MongoDB, pathAndFare: " + pathAndFare);
//			return pathAndFare;
//		}).doOnTerminate(() -> System.out.println("Proceso de consulta a MongoDB terminado."))
//				.doOnError(error -> System.err.println("Error en el flujo: " + error.getMessage()));
//	}

	// Inicialización de los nodos y cálculo de la ruta más corta
	private void initialize(int startingPoint, Map<Integer, Integer> lengths, Map<Integer, Integer> previous,
			PriorityQueue<int[]> nodesToProcess) {
		for (Integer node : sellingPointsPaths.keySet()) {
			if (node == startingPoint) {
				lengths.put(node, 0);
				nodesToProcess.add(new int[] { node, 0 });
			} else {
				lengths.put(node, Integer.MAX_VALUE);
			}
			previous.put(node, null);
		}
	}

	private void processPaths(int endingPoint, Map<Integer, Integer> lengths, Map<Integer, Integer> previous,
			PriorityQueue<int[]> nodesToProcess) {
		while (!nodesToProcess.isEmpty()) {
			int[] current = nodesToProcess.poll();
			int currentNode = current[0];
			int currentLength = current[1];

			if (currentNode == endingPoint) {
				break;
			}

			for (Map.Entry<Integer, Integer> adjacent : sellingPointsPaths.get(currentNode).entrySet()) {
				int adjacentNode = adjacent.getKey();
				int adjacentCost = adjacent.getValue();

				int newLength = currentLength + adjacentCost;

				if (newLength < lengths.get(adjacentNode)) {
					lengths.put(adjacentNode, newLength);
					previous.put(adjacentNode, currentNode);
					nodesToProcess.add(new int[] { adjacentNode, newLength });
				}
			}
		}
	}

	private Map<String, Object> buildPath(Integer endingPoint, Map<String, Object> pathAndFare,
			Map<Integer, Integer> previous) {
		List<Integer> path = new ArrayList<>();
		int fare = 0;
		for (Integer sp = endingPoint; sp != null; sp = previous.get(sp)) {
			path.add(sp);
			if (previous.get(sp) != null) {
				fare += sellingPointsPaths.get(previous.get(sp)).get(sp);
			}
		}
		Collections.reverse(path);
		pathAndFare.put(PATH, path);
		pathAndFare.put(FINAL_FARE, fare);
		return pathAndFare;
	}

	public Mono<List<SellingCostDocument>> getAllSellingCosts() {
		return sellingDocumentRepository.findAll().collectList();
	}

	public Mono<SellingCostDocument> saveSellingCost(SellingCostDocument sellingCostDocument) {
		return sellingDocumentRepository.save(sellingCostDocument);
	}

	public Mono<Void> deleteSellingCost(Integer startingPoint, Integer endingPoint) {
		return sellingDocumentRepository.findByStartingPointAndEndingPoint(startingPoint, endingPoint)
				.flatMap(sellingCostDocument -> sellingDocumentRepository.delete(sellingCostDocument));
	}

	// prueba
	public Mono<Map<Integer, Integer>> getDirectConnection(int sellingPoint) {
		String cacheKey = "directConnection:" + sellingPoint;
		RMapReactive<String, Object> redisMap = redissonClient.getMap(cacheKey);

		// ver si la key està en memoria
		return redisMap.get(cacheKey).flatMap(cachedConnections -> {
			// se devuelve y evalùa
			if (cachedConnections != null) {
				return Mono.just((Map<Integer, Integer>) cachedConnections);
			} else {
				// si no se encuentra en Redis, consultar base
				return sellingDocumentRepository.findAll().collectList().flatMap(sellingCosts -> {
					Map<Integer, Integer> connections = new HashMap<>();
					sellingCosts.forEach(sellingCost -> {
						if (sellingCost.getStartingPoint() == sellingPoint) {
							connections.put(sellingCost.getEndingPoint(), sellingCost.getCost());
						}
					});

					// se guarda en redis, ahora si, para que la prox vez estè cacheado
					redisMap.put(cacheKey, connections).subscribe();

					// se devuelve lo encontrado en base
					return Mono.just(connections);
				});
			}
		});
	}

}