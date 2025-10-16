package com.nailora.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

public record TimeSlotRequest(@NotNull Long serviceItemId,
		@NotNull @FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
		@NotNull @Future @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
		@NotNull @Min(1) Integer capacity, Boolean active) {
}
