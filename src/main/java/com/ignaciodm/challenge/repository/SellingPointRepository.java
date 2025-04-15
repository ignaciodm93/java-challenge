package com.ignaciodm.challenge.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.ignaciodm.challenge.models.SellingPoint;

import reactor.core.publisher.Mono;

public interface SellingPointRepository extends ReactiveMongoRepository<SellingPoint, Integer> {
	Mono<SellingPoint> findById(Integer id);

	Mono<Void> deleteAll();
}