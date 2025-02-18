package com.ignaciodm.challenge.controllers;

import java.util.List;

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

import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.service.CacheService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/selling-points")
public class SellingPointsController {

    private final CacheService cacheService;

    public SellingPointsController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<List<SellingPoint>>> getAll() {
        return cacheService.getSellingPointsCache().valueIterator().collectList().map(ResponseEntity::ok);
    }

    @PostMapping("/add")
    public Mono<ResponseEntity<String>> add(@RequestBody SellingPoint sellingPoint) {
        return cacheService.getSellingPointsCache().containsKey(sellingPoint.getId())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, identifier already in use."));
                } else {
                    return cacheService.getSellingPointsCache().put(sellingPoint.getId(), sellingPoint).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("New selling point created"));
                }
            }).onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown error trying to add new selling point.")));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<String>> update(@RequestBody SellingPoint sellingPoint) {
        return cacheService.getSellingPointsCache().containsKey(sellingPoint.getId()).flatMap(exists -> {
                    if (exists) {
                        return cacheService.getSellingPointsCache().put(sellingPoint.getId(), sellingPoint).thenReturn(new ResponseEntity<>(HttpStatus.OK));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, selling point doesn't exist."));
                    }
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable Integer id) {
    	return cacheService.getSellingPointsCache().remove(id).flatMap(deletedValue -> Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body("Selling point deleted."))).switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Selling point not found.")));
    }
}
