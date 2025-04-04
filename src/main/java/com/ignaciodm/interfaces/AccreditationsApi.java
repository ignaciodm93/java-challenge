package com.ignaciodm.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import reactor.core.publisher.Mono;

public interface AccreditationsApi {

	String GET_ACCREDITATION_VALUE = "Get accreditation by ID";
	String GET_ACCREDITATION_NOTES = "Gets an accreditation by its selling point ID.";
	String CREATE_ACCREDITATION_VALUE = "Create an accreditation";
	String CREATE_ACCREDITATION_NOTES = "Creates a new accreditation from redis cache.";
	String ACCREDITATION_ID_DESCRIPTION = "ID of the accreditation to retrieve";
	String SELLING_POINT_ID_DESCRIPTION = "ID of the selling point to create the accreditation";
	String AMOUNT_DESCRIPTION = "Amount for the accreditation";
	String ACCREDITATION_NOT_FOUND = "Accreditation not found.";
	String SELLING_POINT_NOT_FOUND = "Selling point not found in cache, try another one or update the cache.";

	@Operation(summary = GET_ACCREDITATION_VALUE, description = GET_ACCREDITATION_NOTES, responses = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = ACCREDITATION_NOT_FOUND, content = @Content(mediaType = "text/plain")) })
	@GetMapping("get/{accreditationId}")
	Mono<ResponseEntity<Object>> getAccreditation(
			@Parameter(description = ACCREDITATION_ID_DESCRIPTION, required = true) @PathVariable Integer accreditationId);

	@Operation(summary = CREATE_ACCREDITATION_VALUE, description = CREATE_ACCREDITATION_NOTES, responses = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = SELLING_POINT_NOT_FOUND, content = @Content(mediaType = "text/plain")) })
	@PostMapping("create/{sellingPointId}/{amount}")
	Mono<ResponseEntity<Object>> saveAccreditation(
			@Parameter(description = SELLING_POINT_ID_DESCRIPTION, required = true) @PathVariable Integer sellingPointId,
			@Parameter(description = AMOUNT_DESCRIPTION, required = true) @PathVariable Double amount);
}