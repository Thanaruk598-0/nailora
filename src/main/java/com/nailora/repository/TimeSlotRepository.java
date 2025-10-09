// com.nailora.repository.TimeSlotRepository
package com.nailora.repository;

import com.nailora.entity.TimeSlot;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

	@Query("""
			select t from TimeSlot t
			join fetch t.service s
			where t.id = :id
			""")
	Optional<TimeSlot> findByIdWithService(@Param("id") Long id);

	@Query("""
			select t from TimeSlot t
			where t.service.id = :serviceId
			  and t.startAt >= :from and t.startAt < :to
			order by t.startAt
			""")
	List<TimeSlot> findByServiceAndDay(@Param("serviceId") Long serviceId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	List<TimeSlot> findByServiceIdAndStartAtBetweenOrderByStartAtAsc(Long serviceId, java.time.LocalDateTime from,
			java.time.LocalDateTime to);

}
