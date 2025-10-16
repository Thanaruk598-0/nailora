package com.nailora.repository;

import com.nailora.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	boolean existsByTimeSlotIdAndPhone(Long timeSlotId, String phone);

	@Query("""
			  select count(b) from Booking b
			  where b.timeSlot.id=:slotId
			    and b.status = com.nailora.entity.Booking$Status.BOOKED
			    and (
			      b.depositStatus = com.nailora.entity.Booking$DepositStatus.PAID
			      or (
			        b.depositStatus in (com.nailora.entity.Booking$DepositStatus.UNPAID, com.nailora.entity.Booking$DepositStatus.PROCESSING)
			        and b.depositDueAt > :now
			      )
			    )
			""")
	long countHoldingSeats(Long slotId, LocalDateTime now);

	@Query("""
			  select b from Booking b
			  where b.status = com.nailora.entity.Booking$Status.BOOKED
			    and b.depositStatus = com.nailora.entity.Booking$DepositStatus.UNPAID
			    and b.depositDueAt < :now
			""")
	List<Booking> findExpiredUnpaid(LocalDateTime now);

	@Query("""
			      select b
			      from Booking b
			      join fetch b.timeSlot t
			      join fetch t.serviceItem s
			      where b.phone = :phone
			      order by b.createdAt desc
			""")
	List<Booking> findByPhoneWithSlotAndService(@org.springframework.data.repository.query.Param("phone") String phone);

	Optional<Booking> findByPaymentRef(String paymentIntentId);

}