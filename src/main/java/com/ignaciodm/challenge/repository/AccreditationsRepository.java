package com.ignaciodm.challenge.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.ignaciodm.challenge.models.AccreditationDocument;

import reactor.core.publisher.Flux;

@Repository
public interface AccreditationsRepository extends ReactiveMongoRepository<AccreditationDocument, String> {

	Flux<AccreditationDocument> findBySellingPointId(Integer accreditationSellingPointId);

}