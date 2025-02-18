package com.ignaciodm.challenge.models;

public class SellingCost {

	private Integer startingPoint;
	private Integer endingPoint;
	private Integer cost;
	
	public SellingCost() {}
	
	public SellingCost(int startingPoint, int endingPoint, int cost) {
		this.startingPoint = startingPoint;
		this.endingPoint = endingPoint;
		this.cost = cost;
	}

	public Integer getStartingPoint() {
		return startingPoint;
	}

	public void setStartingPoint(Integer startingPoint) {
		this.startingPoint = startingPoint;
	}

	public Integer getEndingPoint() {
		return endingPoint;
	}

	public void setEndingPoint(Integer endingPoint) {
		this.endingPoint = endingPoint;
	}

	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}
	
	public String getIdentifierKeyRoute() {
		return this.startingPoint + "-" + this.endingPoint;
	}
}