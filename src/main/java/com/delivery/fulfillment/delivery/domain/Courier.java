package com.delivery.fulfillment.delivery.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad que representa un courier (repartidor).
 * Actor responsable de realizar las entregas.
 */
@Entity
@Table(name = "couriers")
public class Courier {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(nullable = false, unique = true)
	private String name;
	
	@Column(nullable = false)
	private String phoneNumber;
	
	@Column(nullable = false)
	private boolean available;
	
	@Column(nullable = false)
	private Instant createdAt;
	
	@Column
	private Instant updatedAt;
	
	// Constructor para JPA
	protected Courier() {
	}
	
	public Courier(String name, String phoneNumber) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.available = true;
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();
	}
	
	/**
	 * Marca el courier como disponible o no disponible.
	 */
	public void setAvailable(boolean available) {
		this.available = available;
		this.updatedAt = Instant.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = Instant.now();
	}
	
	// Getters
	public UUID getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public boolean isAvailable() {
		return available;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public Instant getUpdatedAt() {
		return updatedAt;
	}
}

