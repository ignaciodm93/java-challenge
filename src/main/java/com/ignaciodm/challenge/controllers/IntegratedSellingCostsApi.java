package com.ignaciodm.challenge.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.ignaciodm.challenge.models.SellingCostDocument;

import io.swagger.v3.oas.annotations.Operation;
import reactor.core.publisher.Mono;

public interface IntegratedSellingCostsApi {

	String GET_ALL_SELLING_COSTS_VALUE = "Get all selling costs";
	String GET_ALL_SELLING_COSTS_NOTES = "Returns a list of all registered selling costs.";
	String ADD_OR_UPDATE_SELLING_COST_VALUE = "Create or update a selling cost";
	String ADD_OR_UPDATE_SELLING_COST_NOTES = "Creates a new selling cost or updates an existing one.";
	String REMOVE_SELLING_COST_VALUE = "Remove a selling cost";
	String REMOVE_SELLING_COST_NOTES = "Removes a selling cost between two provided selling points.";
	String GET_DIRECT_CONNECTION_VALUE = "Get direct connections";
	String GET_DIRECT_CONNECTION_NOTES = "Gets the direct connections for a specific selling point.";
	String GET_CHEAPEST_PATH_VALUE = "Get the cheapest path";
	String GET_CHEAPEST_PATH_NOTES = "Gets the cheapest path between two selling points.";

	@Operation(summary = GET_ALL_SELLING_COSTS_VALUE, description = GET_ALL_SELLING_COSTS_NOTES)
	Mono<ResponseEntity<List<SellingCostDocument>>> getAllSellingCosts();

	@Operation(summary = ADD_OR_UPDATE_SELLING_COST_VALUE, description = ADD_OR_UPDATE_SELLING_COST_NOTES)
	Mono<ResponseEntity<SellingCostDocument>> addOrUpdateSellingCost(SellingCostDocument sellingCostDocument);

	@Operation(summary = REMOVE_SELLING_COST_VALUE, description = REMOVE_SELLING_COST_NOTES)
	Mono<ResponseEntity<String>> removeSellingCost(@RequestParam Integer startingPoint,
			@RequestParam Integer endingPoint);

	@Operation(summary = GET_DIRECT_CONNECTION_VALUE, description = GET_DIRECT_CONNECTION_NOTES)
	Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnection(
			@RequestParam Integer sellingPointToDiscoverPathsId);

	@Operation(summary = GET_CHEAPEST_PATH_VALUE, description = GET_CHEAPEST_PATH_NOTES)
	Mono<ResponseEntity<Object>> getFullCheapestPath(@RequestParam Integer startingPoint,
			@RequestParam Integer endingPoint);
}