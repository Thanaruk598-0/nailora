package com.nailora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nailora.entity.ServiceItem;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

}
