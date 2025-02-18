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

	@Autowired
	CacheService cacheService;
	
	private Map<Integer, Map<Integer, Integer>> sellingPointsPaths = new HashMap<>();
	private final Mono<Void> initializationMono; //Necesario para un funcionamiento reactivo, seguiría "funcionando" si usase de base el sellingPOintsPaths pero no de manera reactiva.
	
	public PathService(CacheService cacheService) {
        this.cacheService = cacheService;
        this.initializationMono = initializePathConnectionsGraph().cache(); // Inicializa y cachea el Mono tenerlo todo listo cuando se ejecuten las consultas
    }
	
	@PostConstruct //Inicialmente lo cree sin el Mono y que solo inicialice pero entiendo que para que mantenga la propiedad reactiva debe devolver este tipo para que se guarde en el initializationMono, si no pierde la cualidad.
	private Mono<Void> initializePathConnectionsGraph() {
		return cacheService.getCostsCache().readAllMap()
        .doOnNext(sellingCosts -> {
            sellingCosts.forEach((key, sellingCost) -> {
            	//Se guardan las relaciones dejando el punto de venta en cuestión y los costos adyacentes existentes en cada caso
                Map<Integer, Integer> startPointConnections = sellingPointsPaths.computeIfAbsent(sellingCost.getStartingPoint(), path -> new HashMap<>());
                startPointConnections.put(sellingCost.getEndingPoint(), sellingCost.getCost());
                //Mismo procesidmiento pero inverso para que las consultas puedan ser bidireccionales (básicamente lo mismo pero replicando el mapa de nodos al reves
                Map<Integer, Integer> endPointConnections = sellingPointsPaths.computeIfAbsent(sellingCost.getEndingPoint(), path -> new HashMap<>());
                endPointConnections.put(sellingCost.getStartingPoint(), sellingCost.getCost());
            });
        }).then();
	}

	public Mono<Map<Integer, Integer>> getDirectConnection(int sellingPoint) {
        return initializationMono.then(Mono.just(sellingPointsPaths.getOrDefault(sellingPoint, new HashMap<>())));
    }
	
	public Mono<Map<String, Object>> getFullCheapestPath(Integer startingPoint, Integer endingPoint) {
        return initializationMono.then(Mono.fromCallable(() -> {
            // Por las dudas chequear antes si los nodos existen en el grafo
        	Map<String, Object> pathAndFare = new HashMap<>();//para devolver al controlador 
            if (!sellingPointsPaths.containsKey(startingPoint) || !sellingPointsPaths.containsKey(endingPoint)) {
                return pathAndFare; //retornamos un mapa vacío para poder evaluarlo en el controlador
            }

            // Estructuras de datos auxiliares
            Map<Integer, Integer> lengths = new HashMap<>(); // Distancias mínimas desde el inicio
            Map<Integer, Integer> previous = new HashMap<>(); // Nodos previos para reconstruir el camino
            PriorityQueue<int[]> nodesToProcess = new PriorityQueue<>(Comparator.comparingInt(a -> a[1])); // Cola de nodos no setteados (nodo, distancia), con el comparador obtenemos el de menor distancia de los disponibles (propiedad almacenada en nodo[1] ya que nodo[0] es el nodo o selling point en cuestión)
            
            // Inicialización, intero por todos los nodos
            initialize(startingPoint, lengths, previous, nodesToProcess);

            // Procesamiento del algoritmo
            processPaths(endingPoint, lengths, previous, nodesToProcess);

            // Reconstruir el camino mínimo y calcular el costo total
            return buildPath(endingPoint, pathAndFare, previous);
        }));
    }

	private void initialize(int startingPoint, Map<Integer, Integer> lengths, Map<Integer, Integer> previous,
			PriorityQueue<int[]> nodesToProcess) {
		for (Integer node : sellingPointsPaths.keySet()) {
		    if (node == startingPoint) {
		        lengths.put(node, 0); // La distancia al nodo inicial es 0 (primero nodo, luego distancia hasta el origen)
		        nodesToProcess.add(new int[]{node, 0});
		    } else {
		        lengths.put(node, Integer.MAX_VALUE); // Distancia infinita para los demás nodos según lo que se indica en el algoritmo/baeldung
		    }
		    previous.put(node, null);
		}
	}

	private void processPaths(int endingPoint, Map<Integer, Integer> lengths, Map<Integer, Integer> previous, PriorityQueue<int[]> nodesToProcess) {
		while (!nodesToProcess.isEmpty()) {
		    int[] current = nodesToProcess.poll();
		    System.out.println("Nodo a trabajar: " + current[0]);
		    System.out.println("Con distancia: " + current[1]);
		    int currentNode = current[0];
		    int currentLenght = current[1];

		    // Si llegamos al nodo final, terminamos ya que no hace falta evaluar mas
		    if (currentNode == endingPoint) {
		        break;
		    }

		    // Explorar vecinos del nodo actual (usando el get del mapa consigo los adyacentes, que son los que posean la key igual al currentNode que es en el que estoy parado, ya que es igual a que se pueda ir de un lado a otro eso)
		    for (Map.Entry<Integer, Integer> adjacent : sellingPointsPaths.get(currentNode).entrySet()) {
		        int adjacentNode = adjacent.getKey();
		        int adjacentCost = adjacent.getValue();

		        // Calcular la nueva distancia (se suma la length al momento adicionandole el costo del punto evaluado pero se iguala a newLenght, porque el proceso se debe ahcer con todos los que se levanten como adyacentes)
		        int newLenght = currentLenght + adjacentCost;

		        // comparamos el new lenght temporal Si encontramos un camino más corto, actualizamos.
		        System.out.println("distancia a comparar: " + newLenght + " menor que " + lengths.get(adjacentNode) + "? :" + (newLenght < lengths.get(adjacentNode)));
		        if (newLenght < lengths.get(adjacentNode)) {
		            lengths.put(adjacentNode, newLenght);
		            previous.put(adjacentNode, currentNode);
		            nodesToProcess.add(new int[]{adjacentNode, newLenght});
		            System.out.println("nuevo nodo agregado: " + adjacentNode);
		        }
		    }
		}
	}

	private Map<String, Object> buildPath(Integer endingPoint, Map<String, Object> pathAndFare, Map<Integer, Integer> previous) {
		List<Integer> path = new ArrayList<>();
		int fare = 0;
		for (Integer sp = endingPoint; sp != null; sp = previous.get(sp)) {
		    path.add(sp);

		    // Calcular el costo total
		    if (previous.get(sp) != null) {
		    	fare += sellingPointsPaths.get(previous.get(sp)).get(sp);
		    }
		}
		Collections.reverse(path);

		// Devolver el camino y el costo total en un Map
		pathAndFare.put("path: ", path);
		pathAndFare.put("final fare: ", fare);

		return pathAndFare;
	}
}
