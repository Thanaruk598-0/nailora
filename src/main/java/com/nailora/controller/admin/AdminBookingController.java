package com.nailora.controller.admin;

import com.nailora.dto.*;
import com.nailora.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public List<BookingDTO> getAll() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public BookingDTO getOne(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @PostMapping
    public BookingDTO create(@RequestBody BookingRequest req) {
        return bookingService.createBooking(req);
    }

    @PutMapping("/{id}")
    public BookingDTO update(@PathVariable Long id, @RequestBody BookingRequest req) {
        return bookingService.updateBooking(id, req);
    }

    @PutMapping("/{id}/cancel")
    public BookingDTO cancel(@PathVariable Long id, @RequestBody CancelRequest req) {
        return bookingService.cancelBooking(id, req);
    }

    @PutMapping("/{id}/deposit/verify")
    public BookingDTO verify(@PathVariable Long id, @RequestBody VerifyDepositForm req) {
        return bookingService.verifyDeposit(id, req);
    }

    @PutMapping("/{id}/deposit/refund")
    public BookingDTO refund(@PathVariable Long id, @RequestBody RefundForm req) {
        return bookingService.refundDeposit(id, req);
    }

    @PutMapping("/{id}/deposit/void")
    public BookingDTO voidPay(@PathVariable Long id, @RequestBody VoidForm req) {
        return bookingService.voidDeposit(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }
}
