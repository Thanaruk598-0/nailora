package com.nailora.controller.admin;

import com.nailora.dto.BookingDTO;
import com.nailora.dto.CalendarEventDTO;
import com.nailora.dto.TimeSlotRequest;
import com.nailora.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/calendar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCalendarController {

    private final TimeSlotService timeSlotService;

    @GetMapping("/events")
    public List<CalendarEventDTO> listEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long serviceItemId
    ) {
        return timeSlotService.getCalendarEvents(from, to, serviceItemId);
    }

    // ✅ ดึง slot ทั้งหมด (ดูได้ใน browser)
    @GetMapping("/slots")
    public List<CalendarEventDTO> listAllSlots() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusYears(1);
        return timeSlotService.getCalendarEvents(now, future, null);
    }

    @PostMapping("/slots")
    public CalendarEventDTO create(@Valid @RequestBody TimeSlotRequest request) {
        return timeSlotService.createSlot(request);
    }

    @PutMapping("/slots/{id}")
    public CalendarEventDTO update(@PathVariable Long id, @Valid @RequestBody TimeSlotRequest request) {
        return timeSlotService.updateSlot(id, request);
    }

    @DeleteMapping("/slots/{id}")
    public void delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean force) {
        timeSlotService.deleteSlot(id, force);
    }

    @PatchMapping("/slots/{id}/active")
    public CalendarEventDTO toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        return timeSlotService.toggleActive(id, active);
    }

    @PatchMapping("/slots/{id}/capacity")
    public CalendarEventDTO updateCapacity(@PathVariable Long id, @RequestParam int value) {
        return timeSlotService.updateCapacity(id, value);
    }

    @GetMapping("/slots/{id}/bookings")
    public List<BookingDTO> bookings(@PathVariable Long id) {
        return timeSlotService.getBookings(id);
    }

    @PostMapping("/slots/bulk")
    public int bulkGenerate(@RequestParam Long serviceItemId,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                            @RequestParam int slotMinutes,
                            @RequestParam int capacity,
                            @RequestParam(defaultValue = "true") boolean active) {
        return timeSlotService.bulkGenerate(serviceItemId, from, to, slotMinutes, capacity, active);
    }
}
