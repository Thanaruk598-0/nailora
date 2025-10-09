package com.nailora.dto;

import java.time.LocalDateTime;

public record CalendarEventDTO(
        Long id,
        Long serviceItemId,
        String serviceName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer capacity,
        int booked,
        boolean active,
        boolean full
) {}
