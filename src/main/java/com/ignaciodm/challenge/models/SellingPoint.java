package com.ignaciodm.challenge.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.RedisHash;

@Document(collection = "sellingPoints")
@RedisHash(value = "sellingPoint", timeToLive = 5)
public class SellingPoint {

	@Id
	private Integer id;
	private String name;

	public SellingPoint() {
	}

	public SellingPoint(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}