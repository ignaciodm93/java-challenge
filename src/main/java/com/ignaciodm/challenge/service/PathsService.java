package com.ignaciodm.challenge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class PathsService {

	private static final String FINAL_FARE = "final fare: ";
	private static final String PATH = "path: ";

	public Mono<Map<String, Object>> getFullCheapestPath(Map<Integer, Map<Integer, Integer>> sellingPointsPaths,
			Integer startingPoint, Integer endingPoint) {
		return Mono.fromCallable(() -> {
			Map<String, Object> pathAndFare = new HashMap<>();

			Map<Integer, Integer> lengths = new HashMap<>();
			Map<Integer, Integer> previous = new HashMap<>();
			PriorityQueue<int[]> nodesToProcess = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
			initialize(startingPoint, lengths, previous, nodesToProcess, sellingPointsPaths);
			processPaths(endingPoint, lengths, previous, nodesToProcess, sellingPointsPaths);
			return buildPath(endingPoint, pathAndFare, previous, sellingPointsPaths);
		});
	}

	private void initialize(int startingPoint, Map<Integer, Integer> lengths, Map<Integer, Integer> previous,
			PriorityQueue<int[]> nodesToProcess, Map<Integer, Map<Integer, Integer>> sellingPointsPaths) {
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
			PriorityQueue<int[]> nodesToProcess, Map<Integer, Map<Integer, Integer>> sellingPointsPaths) {
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
			Map<Integer, Integer> previous, Map<Integer, Map<Integer, Integer>> sellingPointsPaths) {
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