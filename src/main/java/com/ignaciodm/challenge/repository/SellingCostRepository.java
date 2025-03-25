package com.ignaciodm.challenge.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.ignaciodm.challenge.models.SellingCostDocument;

import reactor.core.publisher.Mono;

public interface SellingCostRepository extends ReactiveMongoRepository<SellingCostDocument, Integer> {
	Mono<SellingCostDocument> findByStartingPointAndEndingPoint(int startingPoint, int endingPoint);
}