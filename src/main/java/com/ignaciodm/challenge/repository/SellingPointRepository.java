package com.ignaciodm.challenge.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.ignaciodm.challenge.models.SellingPointDocument;

public interface SellingPointRepository extends ReactiveMongoRepository<SellingPointDocument, Integer> {

}