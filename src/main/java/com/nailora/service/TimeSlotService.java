package com.nailora.service;

import com.nailora.dto.TimeSlotRequest;
import com.nailora.dto.CalendarEventDTO;
import com.nailora.dto.BookingDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotService {

    // ดึง events ของ calendar ตามช่วงเวลา
    List<CalendarEventDTO> getCalendarEvents(LocalDateTime from, LocalDateTime to, Long serviceItemId);

    // สร้าง slot ใหม่
    CalendarEventDTO createSlot(TimeSlotRequest request);

    // อัปเดต slot
    CalendarEventDTO updateSlot(Long id, TimeSlotRequest request);

    // ลบ slot (force ถ้ามี booking)
    void deleteSlot(Long id, boolean force);

    // เปิด/ปิดการใช้งาน slot
    CalendarEventDTO toggleActive(Long id, boolean active);

    // ปรับความจุของ slot
    CalendarEventDTO updateCapacity(Long id, int capacity);

    // ดู bookings ทั้งหมดใน slot
    List<BookingDTO> getBookings(Long slotId);

    // สร้าง slots แบบ bulk
    int bulkGenerate(Long serviceItemId,
                     LocalDateTime startDateTime,
                     LocalDateTime endDateTime,
                     int slotMinutes,
                     int capacity,
                     boolean active);
}