package com.nailora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nailora.entity.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

}
