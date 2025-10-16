package com.nailora.repository;

import com.nailora.entity.TimeSlot;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

	// ------------------- FETCH WITH SERVICE -------------------
	@Query("SELECT t FROM TimeSlot t JOIN FETCH t.serviceItem WHERE t.id = :id")
	Optional<TimeSlot> findByIdWithService(@Param("id") Long id);

	// ------------------- FIND BY RANGE -------------------
	List<TimeSlot> findByStartAtBetween(LocalDateTime from, LocalDateTime to);

	List<TimeSlot> findByServiceItem_IdAndStartAtBetween(Long serviceItemId, LocalDateTime from, LocalDateTime to);

	// ------------------- CHECK OVERLAPS -------------------
	@Query("""
			select (count(t) > 0) from TimeSlot t
			where t.serviceItem.id = :serviceId
			  and t.startAt < :endAt
			  and t.endAt > :startAt
			""")
	boolean existsOverlap(@Param("serviceId") Long serviceId, @Param("startAt") LocalDateTime startAt,
			@Param("endAt") LocalDateTime endAt);

	@Query("""
			select t from TimeSlot t
			where t.serviceItem.id = :serviceId
			  and t.id <> :excludeId
			  and t.startAt < :endAt
			  and t.endAt > :startAt
			""")
	List<TimeSlot> findOverlaps(@Param("serviceId") Long serviceId, @Param("excludeId") Long excludeId,
			@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

	// ------------------- FIND BY SERVICE AND DAY -------------------
	@Query("""
			select t from TimeSlot t
			where t.serviceItem.id = :serviceId
			  and t.startAt >= :from and t.startAt < :to
			order by t.startAt
			""")
	List<TimeSlot> findByServiceAndDay(@Param("serviceId") Long serviceId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	List<TimeSlot> findByServiceItem_IdAndStartAtBetweenOrderByStartAtAsc(Long serviceItemId, LocalDateTime from,
			LocalDateTime to);

	Optional<TimeSlot> findByServiceItem_IdAndStartAt(Long serviceItemId, LocalDateTime startAt);

	long countByServiceItem_Id(Long serviceItemId);

	boolean existsByServiceItem_IdAndStartAt(Long serviceItemId, LocalDateTime startAt);

	// ตัวช่วยหาแถวที่ยังเป็นปีพุทธ (ใช้ HQL year() ซึ่ง Hibernate map ไปเป็น
	// extract(year from ...) บน Postgres)
	@Query("""
			select t from TimeSlot t
			where (t.startAt is not null and t.startAt >= :cutoff)
			   or (t.endAt   is not null and t.endAt   >= :cutoff)
			""")
	List<TimeSlot> findWithBuddhistYear(@Param("cutoff") LocalDateTime cutoff);
}
