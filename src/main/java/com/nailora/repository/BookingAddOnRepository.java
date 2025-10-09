package com.nailora.repository;

import com.nailora.entity.BookingAddOn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingAddOnRepository extends JpaRepository<BookingAddOn, Long> {
	List<BookingAddOn> findByBookingId(Long bookingId);
}
