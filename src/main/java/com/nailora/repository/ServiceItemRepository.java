package com.nailora.repository;

import com.nailora.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

	Optional<ServiceItem> findTopByNameIgnoreCaseOrderByIdAsc(String name);
}
