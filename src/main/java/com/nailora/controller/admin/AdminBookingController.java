package com.nailora.controller.admin;

import com.nailora.dto.BookingDTO;
import com.nailora.entity.Booking;
import com.nailora.mapper.BookingMapper;
import com.nailora.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin/api/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    // ✅ ดึงรายการทั้งหมด (Entity -> DTO)
    @GetMapping
    public List<BookingDTO> getAll() {
        return BookingMapper.toDtoList(bookingService.getAllBookings());
    }

    // ✅ ดึงตัวเดียว (Entity -> DTO)
    @GetMapping("/{id}")
    public BookingDTO getOne(@PathVariable Long id) {
        return BookingMapper.toDto(bookingService.getBooking(id));
    }

    // ✅ สร้าง booking (รับเป็น form/query param)
    @PostMapping
    public BookingDTO create(
            @RequestParam Long timeSlotId,
            @RequestParam String customerName,
            @RequestParam String phone,
            @RequestParam(required = false) String note,
            @RequestParam BigDecimal servicePrice,
            @RequestParam BigDecimal addOnPrice,
            @RequestParam BigDecimal depositAmount
    ) {
        Booking booking = bookingService.createBooking(
                timeSlotId, customerName, phone, note, servicePrice, addOnPrice, depositAmount
        );
        return BookingMapper.toDto(booking);
    }

    // ✅ อัปเดตข้อมูลลูกค้า/โน้ต
    @PutMapping("/{id}")
    public BookingDTO update(
            @PathVariable Long id,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String note
    ) {
        Booking booking = bookingService.updateBooking(id, customerName, phone, note);
        return BookingMapper.toDto(booking);
    }

    // ✅ อัปเดตสถานะ (BOOKED / CANCELLED)
    @PatchMapping("/{id}/status")
    public BookingDTO updateStatus(@PathVariable Long id, @RequestParam Booking.Status status) {
        Booking booking = bookingService.updateStatus(id, status);
        return BookingMapper.toDto(booking);
    }

    // ✅ ลบ
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }

    // ✅ Refund มัดจำ
    @PutMapping("/{id}/deposit/refund")
    public BookingDTO refundDeposit(@PathVariable Long id) {
        Booking booking = bookingService.refundDeposit(id, null);
        return BookingMapper.toDto(booking);
    }

    // ✅ Void มัดจำ
    @PutMapping("/{id}/deposit/void")
    public BookingDTO voidDeposit(@PathVariable Long id) {
        Booking booking = bookingService.voidDeposit(id, null);
        return BookingMapper.toDto(booking);
    }
}
