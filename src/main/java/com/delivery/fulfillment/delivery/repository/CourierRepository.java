package com.delivery.fulfillment.delivery.repository;

import com.delivery.fulfillment.delivery.domain.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {
	
	List<Courier> findByAvailableTrue();
}

