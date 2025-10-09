package com.nailora.service;

import com.nailora.dto.*;
import com.nailora.entity.Booking;
import com.nailora.entity.TimeSlot;
import com.nailora.exception.BadRequestException;
import com.nailora.exception.NotFoundException;
import com.nailora.mapper.BookingMapper;
import com.nailora.repository.BookingRepository;
import com.nailora.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return BookingMapper.toDtoList(bookingRepository.findAllWithSlot(null));
    }

    @Transactional(readOnly = true)
    public BookingDTO getBooking(Long id) {
        var b = bookingRepository.findByIdWithSlot(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));
        return BookingMapper.toDto(b);
    }

    @Transactional
    public BookingDTO createBooking(BookingRequest req) {
        if (req.getTimeSlotId() == null) {
            throw new BadRequestException("timeSlotId is required");
        }

        TimeSlot ts = timeSlotRepository.findById(req.getTimeSlotId())
                .orElseThrow(() -> new NotFoundException("TimeSlot not found with id " + req.getTimeSlotId()));

        long current = bookingRepository.countByTimeSlotIdAndStatus(ts.getId(), Booking.Status.BOOKED);
        if (current >= ts.getCapacity()) {
            throw new BadRequestException("This time slot is full (capacity reached)");
        }

        if (bookingRepository.existsByTimeSlotIdAndPhone(ts.getId(), req.getPhone())) {
            throw new BadRequestException("This phone already booked this time slot");
        }

        Booking b = Booking.builder()
                .timeSlot(ts)
                .customerName(req.getCustomerName())
                .phone(req.getPhone())
                .note(req.getNote())
                .status(Booking.Status.BOOKED)
                .servicePrice(req.getServicePrice())
                .addOnPrice(req.getAddOnPrice())
                .depositAmount(req.getDepositAmount())
                .depositStatus(Booking.DepositStatus.UNPAID)
                .depositDueAt(LocalDateTime.now().plusHours(2))
                .createdAt(LocalDateTime.now())
                .gateway(Booking.Gateway.STRIPE)
                .build();

        b = bookingRepository.save(b);
        b = bookingRepository.findByIdWithSlot(b.getId())
                .orElseThrow(() -> new NotFoundException("Created booking not found"));
        return BookingMapper.toDto(b);
    }

    @Transactional
    public BookingDTO updateBooking(Long id, BookingRequest req) {
        var b = bookingRepository.findByIdWithSlot(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

        // อนุญาตแก้เฉพาะข้อมูลลูกค้า/โน้ต (ไม่ย้าย slot/ราคา)
        if (req.getCustomerName() != null) b.setCustomerName(req.getCustomerName());
        if (req.getPhone() != null)        b.setPhone(req.getPhone());
        if (req.getNote() != null)         b.setNote(req.getNote());

        b = bookingRepository.save(b);
        return BookingMapper.toDto(b);
    }

    @Transactional
    public BookingDTO cancelBooking(Long id, CancelRequest req) {
        var b = bookingRepository.findByIdWithSlot(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

        b.setStatus(Booking.Status.CANCELLED);
        b.setCancelReason(req.getReason());
        b.setCanceledAt(LocalDateTime.now());

        b = bookingRepository.save(b);
        return BookingMapper.toDto(b);
    }

    @Transactional
    public BookingDTO verifyDeposit(Long id, VerifyDepositForm req) {
        var b = bookingRepository.findByIdWithSlot(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

        b.setDepositStatus(Booking.DepositStatus.PAID);
        b.setPaymentRef(req.getPaymentRef());
        b.setDepositPaidAt(LocalDateTime.now());

        b = bookingRepository.save(b);
        return BookingMapper.toDto(b);
    }

    @Transactional
    public BookingDTO refundDeposit(Long id, RefundForm req) {
        var b = bookingRepository.findByIdWithSlot(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));
        b.setDepositStatus(Booking.DepositStatus.REFUNDED);
        if (req.getPaymentRef() != null) b.setPaymentRef(req.getPaymentRef());
        b = bookingRepository.save(b);
        return BookingMapper.toDto(b);
    }

    @Transactional
    public BookingDTO voidDeposit(Long id, VoidForm req) {
        var b = bookingRepository.findByIdWithSlot(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));
        b.setDepositStatus(Booking.DepositStatus.VOIDED);
        if (req.getPaymentRef() != null) b.setPaymentRef(req.getPaymentRef());
        b = bookingRepository.save(b);
        return BookingMapper.toDto(b);
    }

    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }
}
