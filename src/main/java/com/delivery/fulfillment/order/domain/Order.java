package com.delivery.fulfillment.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity that represents an order.
 * Handles the complete order lifecycle from creation to completion.
 */
@Entity
@Table(name = "orders")
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(nullable = false)
	private UUID customerId;
	
	@Column(nullable = false)
	private String deliveryAddress;
	
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal totalAmount;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;
	
	@Column
	private String failureReason;
	
	@Column(nullable = false)
	private Instant createdAt;
	
	@Column
	private Instant updatedAt;
	
	@Column
	private UUID deliveryId; // Reference to the associated delivery
	
	// Constructor for JPA
	protected Order() {
	}
	
	public Order(UUID customerId, String deliveryAddress, BigDecimal totalAmount) {
		this.id = UUID.randomUUID();
		this.customerId = customerId;
		this.deliveryAddress = deliveryAddress;
		this.totalAmount = totalAmount;
		this.status = OrderStatus.PLACED;
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();
	}
	
	/**
	 * Transitions the order state validating that the transition is valid.
	 * @throws IllegalStateException if the transition is not valid
	 */
	public void transitionTo(OrderStatus newStatus) {
		if (!this.status.canTransitionTo(newStatus)) {
			throw new IllegalStateException(
				String.format("Invalid state transition from %s to %s for order %s", 
					this.status, newStatus, this.id));
		}
		this.status = newStatus;
		this.updatedAt = Instant.now();
	}
	
	/**
	 * Marks the order as failed with a reason.
	 */
	public void markAsFailed(String reason) {
		transitionTo(OrderStatus.FAILED);
		this.failureReason = reason;
	}
	
	/**
	 * Associates a delivery to this order.
	 */
	public void assignDelivery(UUID deliveryId) {
		this.deliveryId = deliveryId;
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
	
	public UUID getCustomerId() {
		return customerId;
	}
	
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	
	public OrderStatus getStatus() {
		return status;
	}
	
	public String getFailureReason() {
		return failureReason;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public Instant getUpdatedAt() {
		return updatedAt;
	}
	
	public UUID getDeliveryId() {
		return deliveryId;
	}
}

