package com.ignaciodm.challenge.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ignaciodm.challenge.models.SellingPoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SellingPointApi {

	public static final String NO_CONTENT = "No Content";
	public static final String OK = "OK";
	public static final String BAD_REQUEST_PLEASE_TRY_ANOTHER_IDENTIFIER = "Bad request, please try another identifier.";
	public static final String CREATED = "Created";
	String GET_ALL_SELLING_POINTS_VALUE = "Get all selling points";
	String GET_ALL_SELLING_POINTS_NOTES = "Gets a list of all selling points.";
	String GET_SELLING_POINT_BY_ID_VALUE = "Get a selling point by ID";
	String GET_SELLING_POINT_BY_ID_NOTES = "Gets a selling point by its ID.";
	String CREATE_SELLING_POINT_VALUE = "Create a selling point";
	String CREATE_SELLING_POINT_NOTES = "Creates a new selling point.";
	String UPDATE_SELLING_POINT_VALUE = "Update a selling point";
	String UPDATE_SELLING_POINT_NOTES = "Updates an existing selling point.";
	String DELETE_SELLING_POINT_VALUE = "Delete a selling point";
	String DELETE_SELLING_POINT_NOTES = "Deletes a selling point by its ID.";
	String SAVE_INITIAL_DATA_VALUE = "Save initial data";
	String SAVE_INITIAL_DATA_NOTES = "Saves initial data for selling points.";
	String CLEAR_CACHE_VALUE = "Clear cache";
	String CLEAR_CACHE_NOTES = "Clears the cache for selling points.";
	String SELLING_POINT_ID_DESCRIPTION = "ID of the selling point to retrieve";
	String SELLING_POINT_ID_NOT_FOUND = "Selling point not found";
	String SELLING_POINT_CREATE_DESCRIPTION = "Selling point to be created";
	String SELLING_POINT_UPDATE_DESCRIPTION = "Selling point to update";

	@Operation(summary = GET_ALL_SELLING_POINTS_VALUE, description = GET_ALL_SELLING_POINTS_NOTES, responses = {
			@ApiResponse(responseCode = "200", description = OK, content = @Content(schema = @Schema(implementation = SellingPoint.class))) })
	@GetMapping
	Flux<SellingPoint> getAllSellingPoints();

	@Operation(summary = GET_SELLING_POINT_BY_ID_VALUE, description = GET_SELLING_POINT_BY_ID_NOTES, responses = {
			@ApiResponse(responseCode = "200", description = OK, content = @Content(schema = @Schema(implementation = SellingPoint.class))),
			@ApiResponse(responseCode = "404", description = SELLING_POINT_ID_NOT_FOUND, content = @Content(schema = @Schema(implementation = SellingPoint.class))) })
	@GetMapping("/{id}")
	Mono<ResponseEntity<SellingPoint>> getSellingPointById(
			@Parameter(description = SELLING_POINT_ID_DESCRIPTION, required = true) @PathVariable Integer id);

	@Operation(summary = CREATE_SELLING_POINT_VALUE, description = CREATE_SELLING_POINT_NOTES, responses = {
			@ApiResponse(responseCode = "201", description = CREATED, content = @Content(schema = @Schema(implementation = SellingPoint.class))),
			@ApiResponse(responseCode = "400", description = BAD_REQUEST_PLEASE_TRY_ANOTHER_IDENTIFIER, content = @Content(schema = @Schema(implementation = SellingPoint.class))) })
	@PostMapping
	Mono<ResponseEntity<SellingPoint>> createSellingPoint(
			@Parameter(description = SELLING_POINT_CREATE_DESCRIPTION, required = true) @RequestBody SellingPoint sellingPoint);

	@Operation(summary = UPDATE_SELLING_POINT_VALUE, description = UPDATE_SELLING_POINT_NOTES, responses = {
			@ApiResponse(responseCode = "200", description = OK, content = @Content(schema = @Schema(implementation = SellingPoint.class))),
			@ApiResponse(responseCode = "404", description = SELLING_POINT_ID_NOT_FOUND, content = @Content(schema = @Schema(implementation = SellingPoint.class))) })
	@PutMapping("/{id}")
	Mono<ResponseEntity<SellingPoint>> updateOrCreateSellingPoint(@PathVariable Integer id,
			@Parameter(description = SELLING_POINT_UPDATE_DESCRIPTION, required = true) @RequestBody SellingPoint sellingPoint);

	@Operation(summary = DELETE_SELLING_POINT_VALUE, description = DELETE_SELLING_POINT_NOTES, responses = {
			@ApiResponse(responseCode = "204", description = NO_CONTENT, content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "400", description = BAD_REQUEST_PLEASE_TRY_ANOTHER_IDENTIFIER, content = @Content(schema = @Schema(implementation = Void.class))) })
	@DeleteMapping("/{id}")
	Mono<ResponseEntity<Void>> deleteSellingPoint(
			@Parameter(description = SELLING_POINT_ID_DESCRIPTION, required = true) @PathVariable Integer id);

}
