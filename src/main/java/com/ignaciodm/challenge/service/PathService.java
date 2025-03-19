package com.ignaciodm.challenge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Service
public class PathService {

	private static final String FINAL_FARE = "final fare: ";

	private static final String PATH = "path: ";

	@Autowired
	CacheService cacheService;

	Map<Integer, Map<Integer, Integer>> sellingPointsPaths = new HashMap<>();
	private final Mono<Void> initializationMono;

	public PathService(CacheService cacheService) {
		this.cacheService = cacheService;
		this.initializationMono = initializePathConnectionsGraph().cache();
	}

	@PostConstruct
	public Mono<Void> initializePathConnectionsGraph() {
		return cacheService.getCostsCache().readAllMap().doOnNext(sellingCosts -> {
			sellingCosts.forEach((key, sellingCost) -> {
				Map<Integer, Integer> startPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getStartingPoint(), path -> new HashMap<>());
				startPointConnections.put(sellingCost.getEndingPoint(), sellingCost.getCost());
				Map<Integer, Integer> endPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getEndingPoint(), path -> new HashMap<>());
				endPointConnections.put(sellingCost.getStartingPoint(), sellingCost.getCost());
			});
		}).then();
	}

	public Mono<Map<Integer, Integer>> getDirectConnection(int sellingPoint) {
		return initializationMono.then(Mono.just(sellingPointsPaths.getOrDefault(sellingPoint, new HashMap<>())));
	}

	public Mono<Map<String, Object>> getFullCheapestPath(Integer startingPoint, Integer endingPoint) {
		return initializationMono.then(Mono.fromCallable(() -> {
			Map<String, Object> pathAndFare = new HashMap<>();
			if (!sellingPointsPaths.containsKey(startingPoint) || !sellingPointsPaths.containsKey(endingPoint)) {
				return pathAndFare;
			}

			Map<Integer, Integer> lengths = new HashMap<>();
			Map<Integer, Integer> previous = new HashMap<>();
			PriorityQueue<int[]> nodesToProcess = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
			initialize(startingPoint, lengths, previous, nodesToProcess);
			processPaths(endingPoint, lengths, previous, nodesToProcess);
			return buildPath(endingPoint, pathAndFare, previous);
		}));
	}

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
			System.out.println("Nodo a trabajar: " + current[0]);
			System.out.println("Con distancia: " + current[1]);
			int currentNode = current[0];
			int currentLenght = current[1];

			if (currentNode == endingPoint) {
				break;
			}

			for (Map.Entry<Integer, Integer> adjacent : sellingPointsPaths.get(currentNode).entrySet()) {
				int adjacentNode = adjacent.getKey();
				int adjacentCost = adjacent.getValue();

				int newLenght = currentLenght + adjacentCost;

				System.out.println("distancia a comparar: " + newLenght + " menor que " + lengths.get(adjacentNode)
						+ "? :" + (newLenght < lengths.get(adjacentNode)));
				if (newLenght < lengths.get(adjacentNode)) {
					lengths.put(adjacentNode, newLenght);
					previous.put(adjacentNode, currentNode);
					nodesToProcess.add(new int[] { adjacentNode, newLenght });
					System.out.println("nuevo nodo agregado: " + adjacentNode);
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
}
