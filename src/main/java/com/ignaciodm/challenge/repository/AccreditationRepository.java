package com.ignaciodm.challenge.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.ignaciodm.challenge.models.AccreditationDocument;

@Repository
public interface AccreditationRepository extends ReactiveMongoRepository<AccreditationDocument, Integer> {
	// Mono<AccreditationDocument> findBySellingPointId(int sellingPointId);
	// se comenta por el momento para que no afecte la refarcotrizacion. Revisar
	// leugo si el tipo de id queda como integer o no
}