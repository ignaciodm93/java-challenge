package com.ignaciodm.challenge.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.service.CacheService;
import com.ignaciodm.challenge.service.PathService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/selling-costs")
public class SellingCostsController {

	@Autowired
    private CacheService cacheService;
	
	@Autowired
	private PathService pathService;

    @GetMapping("/all")
    public Mono<ResponseEntity<List<SellingCost>>> getAllSellingCosts() {
        return cacheService.getCostsCache().valueIterator().collectList().map(ResponseEntity::ok);
    }

    @PostMapping("/new-selling-cost")
    public Mono<ResponseEntity<String>> addOrUpdateSellingCost(@RequestBody SellingCost sellingCost) {
    	return cacheService.getCostsCache().put(sellingCost.getIdentifierKeyRoute(), sellingCost).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("New selling cost created or updated"));
    }
 
    @DeleteMapping("/remove-selling-cost")
    public Mono<ResponseEntity<String>> removeSellingCost(@RequestParam Integer startingPoint, @RequestParam Integer endingPoint) {
        String key = startingPoint + "-" + endingPoint;
        return cacheService.getCostsCache().containsKey(key)
            .flatMap(exists -> {
                if (exists) {
                    return cacheService.getCostsCache().remove(key).then(Mono.just(ResponseEntity.status(HttpStatus.OK).body("Selling cost removed succesfully.")));
                } else {
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connection not found, please try another one."));
                }
            });
    }
	
    @GetMapping("/direct-connection")
    public Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnection(@RequestParam Integer sellingPointToDiscoverPathsId) {
        return pathService.getDirectConnection(sellingPointToDiscoverPathsId).map(directConnections -> {
                if (directConnections.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(directConnections);
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(directConnections);
                }
            });
    }

	@GetMapping("/full-cheapest-path")
	public Mono<ResponseEntity<Object>> getFullCheapestPath(@RequestParam Integer startingPoint, @RequestParam Integer endingPoint) {
		return pathService.getFullCheapestPath(startingPoint, endingPoint).flatMap(pathAndFare -> {
				return Mono.just(ResponseEntity.status(HttpStatus.OK).body(pathAndFare));	
		});
	}
}
