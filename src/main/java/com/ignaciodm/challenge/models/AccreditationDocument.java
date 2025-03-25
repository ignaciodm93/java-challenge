package com.ignaciodm.challenge.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accreditations")
public class AccreditationDocument {

	@Id
	private int id;
	private Double amount;
	private int sellingPointId;
	private String sellingPointName;
	private LocalDateTime receptionDate;

	public AccreditationDocument() {
	}

	public AccreditationDocument(Double amount, int sellingPointId, String sellingPointName,
			LocalDateTime receptionDate) {
		this.amount = amount;
		this.sellingPointId = sellingPointId;
		this.sellingPointName = sellingPointName;
		this.receptionDate = receptionDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public int getSellingPointId() {
		return sellingPointId;
	}

	public void setSellingPointId(int sellingPointId) {
		this.sellingPointId = sellingPointId;
	}

	public String getSellingPointName() {
		return sellingPointName;
	}

	public void setSellingPointName(String sellingPointName) {
		this.sellingPointName = sellingPointName;
	}

	public LocalDateTime getReceptionDate() {
		return receptionDate;
	}

	public void setReceptionDate(LocalDateTime receptionDate) {
		this.receptionDate = receptionDate;
	}
}