package com.nailora.repository;

import com.nailora.entity.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddOnRepository extends JpaRepository<AddOn, Long> {
    boolean existsByNameIgnoreCase(String name);
}
