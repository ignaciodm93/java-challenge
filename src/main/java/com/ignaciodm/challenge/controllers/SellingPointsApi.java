package com.ignaciodm.challenge.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.ignaciodm.challenge.models.SellingPoint;

import io.swagger.v3.oas.annotations.Operation;
import reactor.core.publisher.Mono;

public interface SellingPointsApi {

	String GET_ALL_SELLING_POINTS_VALUE = "Get all selling points";
	String GET_ALL_SELLING_POINTS_NOTES = "Returns a list of all registered selling points.";
	String ADD_SELLING_POINT_VALUE = "Add a new selling point";
	String ADD_SELLING_POINT_NOTES = "Creates a new selling point if the identifier is not in use.";
	String UPDATE_SELLING_POINT_VALUE = "Update a selling point";
	String UPDATE_SELLING_POINT_NOTES = "Updates a selling point if it already exists.";
	String DELETE_SELLING_POINT_VALUE = "Delete a selling point";
	String DELETE_SELLING_POINT_NOTES = "Deletes a selling point by its identifier.";

	@Operation(summary = GET_ALL_SELLING_POINTS_VALUE, description = GET_ALL_SELLING_POINTS_NOTES)
	Mono<ResponseEntity<List<SellingPoint>>> getAll();

	@Operation(summary = ADD_SELLING_POINT_VALUE, description = ADD_SELLING_POINT_NOTES)
	Mono<ResponseEntity<String>> add(@RequestBody SellingPoint sellingPoint);

	@Operation(summary = UPDATE_SELLING_POINT_VALUE, description = UPDATE_SELLING_POINT_NOTES)
	Mono<ResponseEntity<String>> update(@RequestBody SellingPoint sellingPoint);

	@Operation(summary = DELETE_SELLING_POINT_VALUE, description = DELETE_SELLING_POINT_NOTES)
	Mono<ResponseEntity<String>> delete(@PathVariable Integer id);
}
