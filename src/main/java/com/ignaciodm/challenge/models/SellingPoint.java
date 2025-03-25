package com.ignaciodm.challenge.models;

public class SellingPoint {

	private int id;

	private String name;

	public SellingPoint() {
	}

	public SellingPoint(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}
}
