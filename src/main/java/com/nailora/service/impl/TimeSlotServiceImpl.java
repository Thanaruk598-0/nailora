package com.nailora.service.impl;

import com.nailora.dto.CalendarEventDTO;
import com.nailora.dto.TimeSlotRequest;
import com.nailora.entity.TimeSlot;
import com.nailora.repository.ServiceItemRepository;
import com.nailora.repository.TimeSlotRepository;
import com.nailora.service.TimeSlotService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepo;
    private final ServiceItemRepository serviceItemRepo;

    // ✅ เขียน constructor เองแทน Lombok กันพลาด
    public TimeSlotServiceImpl(TimeSlotRepository timeSlotRepo, ServiceItemRepository serviceItemRepo) {
        this.timeSlotRepo = timeSlotRepo;
        this.serviceItemRepo = serviceItemRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getCalendarEvents(LocalDateTime from, LocalDateTime to, Long serviceItemId) {
        List<TimeSlot> slots = (serviceItemId == null)
                ? timeSlotRepo.findByStartAtBetween(from, to)
                : timeSlotRepo.findByServiceItem_IdAndStartAtBetween(serviceItemId, from, to);

        // ตอนนี้ไม่สน booking count เอา booked = 0 ไว้ก่อน
        return slots.stream()
                .map(slot -> toCalendarDTO(slot, 0))
                .toList();
    }

    @Override
    @Transactional
    public CalendarEventDTO createSlot(TimeSlotRequest request) {
        var serviceItem = serviceItemRepo.findById(request.serviceItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service item not found"));

        TimeSlot slot = new TimeSlot();
        slot.setServiceItem(serviceItem);
        slot.setStartAt(request.startAt());
        slot.setEndAt(request.endAt());
        slot.setCapacity(request.capacity());
        slot.setActive(request.active() != null ? request.active() : true);

        TimeSlot saved = timeSlotRepo.save(slot);
        return toCalendarDTO(saved, 0);
    }

    @Override
    @Transactional
    public CalendarEventDTO updateSlot(Long id, TimeSlotRequest request) {
        TimeSlot slot = timeSlotRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));

        var serviceItem = serviceItemRepo.findById(request.serviceItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service item not found"));

        slot.setServiceItem(serviceItem);
        slot.setStartAt(request.startAt());
        slot.setEndAt(request.endAt());
        slot.setCapacity(request.capacity());
        slot.setActive(request.active() != null ? request.active() : slot.getActive());

        TimeSlot saved = timeSlotRepo.save(slot);
        return toCalendarDTO(saved, 0);
    }

    @Override
    @Transactional
    public void deleteSlot(Long id, boolean force) {
        TimeSlot slot = timeSlotRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
        timeSlotRepo.delete(slot);
    }

    @Override
    @Transactional
    public CalendarEventDTO toggleActive(Long id, boolean active) {
        TimeSlot slot = timeSlotRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
        slot.setActive(active);
        TimeSlot saved = timeSlotRepo.save(slot);
        return toCalendarDTO(saved, 0);
    }

    @Override
    @Transactional
    public CalendarEventDTO updateCapacity(Long id, int capacity) {
        TimeSlot slot = timeSlotRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
        slot.setCapacity(capacity);
        TimeSlot saved = timeSlotRepo.save(slot);
        return toCalendarDTO(saved, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.nailora.dto.BookingDTO> getBookings(Long slotId) {
        // ลดสโคป: return ว่างๆ ไปก่อน
        return List.of();
    }

    @Override
    @Transactional
    public int bulkGenerate(Long serviceItemId,
                            LocalDateTime startDateTime,
                            LocalDateTime endDateTime,
                            int slotMinutes,
                            int capacity,
                            boolean active) {
        var serviceItem = serviceItemRepo.findById(serviceItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service item not found"));

        LocalDateTime cursor = startDateTime;
        int created = 0;
        while (cursor.plusMinutes(slotMinutes).isBefore(endDateTime)) {
            LocalDateTime slotStart = cursor;
            LocalDateTime slotEnd = cursor.plusMinutes(slotMinutes);

            TimeSlot slot = new TimeSlot();
            slot.setServiceItem(serviceItem);
            slot.setStartAt(slotStart);
            slot.setEndAt(slotEnd);
            slot.setCapacity(capacity);
            slot.setActive(active);
            timeSlotRepo.save(slot);
            created++;

            cursor = slotEnd;
        }
        return created;
    }

    private CalendarEventDTO toCalendarDTO(TimeSlot slot, int booked) {
        return new CalendarEventDTO(
                slot.getId(),
                slot.getServiceItem().getId(),
                slot.getServiceItem().getName(),
                slot.getStartAt(),
                slot.getEndAt(),
                slot.getCapacity(),
                booked,
                Boolean.TRUE.equals(slot.getActive()),
                booked >= slot.getCapacity()
        );
    }
}