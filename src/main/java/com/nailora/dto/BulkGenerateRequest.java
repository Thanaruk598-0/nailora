package com.nailora.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.Set;

public record BulkGenerateRequest(
        @NotNull Long serviceItemId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull @Min(1) Integer slotMinutes,
        @NotNull @Min(1) Integer capacity,
        Set<DayOfWeek> days,     // เช่น [MONDAY, TUESDAY] ถ้า null แปลว่าทุกวัน
        Boolean active
) {}
