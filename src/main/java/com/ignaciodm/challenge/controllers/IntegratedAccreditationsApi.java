package com.ignaciodm.challenge.controllers;

import org.springframework.http.ResponseEntity;

import com.ignaciodm.challenge.models.AccreditationDocument;

import io.swagger.v3.oas.annotations.Operation;
import reactor.core.publisher.Mono;

public interface IntegratedAccreditationsApi {

	String CREATE_ACCREDITATION_VALUE = "Creates an accreditation";
	String CREATE_ACCREDITATION_NOTES = "Creates a new accreditation and stores it in mongo db";
	String GET_ACCREDITATION_VALUE = "Gets an accreditation";
	String GET_ACCREDITATION_NOTES = "Gets an accreditation using id as a parameter";

	@Operation(summary = CREATE_ACCREDITATION_VALUE, description = CREATE_ACCREDITATION_NOTES)
	Mono<ResponseEntity<AccreditationDocument>> createOrUpdateAccreditation(Double amount, int id,
			String sellingPointNam);

	@Operation(summary = GET_ACCREDITATION_VALUE, description = GET_ACCREDITATION_NOTES)
	Mono<ResponseEntity<AccreditationDocument>> getAccreditation(int id);

}
