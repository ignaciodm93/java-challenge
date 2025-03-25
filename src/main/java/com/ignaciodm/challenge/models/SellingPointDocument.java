package com.ignaciodm.challenge.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sellingPoints")
public class SellingPointDocument {

	@Id
	private int id;
	private String name;

	public SellingPointDocument() {
	}

	public SellingPointDocument(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}