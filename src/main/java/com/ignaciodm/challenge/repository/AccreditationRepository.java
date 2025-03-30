package com.ignaciodm.challenge.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.ignaciodm.challenge.models.AccreditationDocument;

@Repository
public interface AccreditationRepository extends ReactiveMongoRepository<AccreditationDocument, Integer> {
	// Mono<AccreditationDocument> findBySellingPointId(int sellingPointId);
}