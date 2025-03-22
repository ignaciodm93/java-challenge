package com.ignaciodm.challenge.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.ignaciodm.challenge.models.SellingPointDocument;

@Repository
public interface SellingPointRepository extends ReactiveMongoRepository<SellingPointDocument, Long> {

}