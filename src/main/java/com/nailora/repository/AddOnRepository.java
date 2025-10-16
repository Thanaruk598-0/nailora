package com.nailora.repository;

import com.nailora.entity.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddOnRepository extends JpaRepository<AddOn, Long> {

	// ใช้บนหน้า confirm
	@Query("""
			    select a from AddOn a
			    where a.active = true
			    order by a.name asc
			""")
	List<AddOn> findActive();

	// ใช้บนหน้า my-bookings เพื่อดึงชื่อ add-on ของ booking แต่ละอัน
	@Query("""
			    select a.name
			    from BookingAddOn ba
			      join ba.addOn a
			    where ba.booking.id = :bookingId
			    order by a.name asc
			""")
	List<String> findAddOnNamesByBookingId(Long bookingId);

	boolean existsByNameIgnoreCase(String name);

	Optional<AddOn> findByName(String name);

	// ✅ สำคัญ: ต้องเป็น AddOn ไม่ใช่ ServiceItem
	Optional<AddOn> findTopByNameIgnoreCaseOrderByIdAsc(String name);
}
