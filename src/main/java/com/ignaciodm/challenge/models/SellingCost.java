package com.ignaciodm.challenge.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sellingCosts")
public class SellingCost {

	@Id
	private String id;
	private int startingPoint;
	private int endingPoint;
	private int cost;

	public SellingCost() {
	}

	public SellingCost(int startingPoint, int endingPoint, int cost) {
		this.startingPoint = startingPoint;
		this.endingPoint = endingPoint;
		this.cost = cost;
	}

	public int getStartingPoint() {
		return startingPoint;
	}

	public void setStartingPoint(int startingPoint) {
		this.startingPoint = startingPoint;
	}

	public int getEndingPoint() {
		return endingPoint;
	}

	public void setEndingPoint(int endingPoint) {
		this.endingPoint = endingPoint;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String getIdentifierKeyRoute() {
		return this.startingPoint + "-" + this.endingPoint;
	}
}
