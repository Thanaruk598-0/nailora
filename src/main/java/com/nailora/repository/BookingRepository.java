package com.nailora.repository;

import com.nailora.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ใช้ตอน list bookings → ดึง TimeSlot + ServiceItem มาด้วย (กัน N+1)
    @Query("""
           select b from Booking b
           join fetch b.timeSlot ts
           join fetch ts.serviceItem s
           where (:status is null or b.status = :status)
           order by ts.startAt desc, b.id desc
           """)
    List<Booking> findAllWithSlot(@Param("status") Booking.Status status);

    // ใช้ตอน get booking by id → ดึง TimeSlot + ServiceItem ด้วย
    @Query("""
           select b from Booking b
           join fetch b.timeSlot ts
           join fetch ts.serviceItem s
           where b.id = :id
           """)
    Optional<Booking> findByIdWithSlot(@Param("id") Long id);

    // เช็คว่ามี booking ซ้ำ (คนเดียวกันจอง slot เดียวกัน)
    boolean existsByTimeSlotIdAndPhone(Long timeSlotId, String phone);

    // เช็คจำนวน booking ที่จองอยู่ใน slot (เอาไว้ตรวจ capacity)
    long countByTimeSlotIdAndStatus(Long timeSlotId, Booking.Status status);
}
