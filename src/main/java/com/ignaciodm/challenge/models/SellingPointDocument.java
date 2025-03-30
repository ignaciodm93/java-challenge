package com.ignaciodm.challenge.models;

import java.io.Serializable;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("SellingPoint")
public class SellingPointDocument implements Serializable {

//	@Id
//	private int id;
//	private String name;
//
//	public SellingPointDocument() {
//	}
//
//	public SellingPointDocument(int id, String name) {
//		this.id = id;
//		this.name = name;
//	}
//
//	public int getId() {
//		return id;
//	}
//
//	public void setId(int id) {
//		this.id = id;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
}