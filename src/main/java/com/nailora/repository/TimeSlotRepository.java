package com.nailora.repository;

import com.nailora.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByStartAtBetween(LocalDateTime from, LocalDateTime to);

    List<TimeSlot> findByServiceItem_IdAndStartAtBetween(Long serviceItemId, LocalDateTime from, LocalDateTime to);

    @Query("""
           select (count(t) > 0) from TimeSlot t
           where t.serviceItem.id = :serviceId
             and t.startAt < :endAt
             and t.endAt > :startAt
           """)
    boolean existsOverlap(Long serviceId, LocalDateTime startAt, LocalDateTime endAt);

    @Query("""
           select t from TimeSlot t
           where t.serviceItem.id = :serviceId
             and t.id <> :excludeId
             and t.startAt < :endAt
             and t.endAt > :startAt
           """)
    List<TimeSlot> findOverlaps(Long serviceId, Long excludeId, LocalDateTime startAt, LocalDateTime endAt);
}
