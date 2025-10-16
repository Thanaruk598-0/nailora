package com.nailora.dto;

import java.time.LocalDateTime;

public record BookingCreatedResponse(Long bookingId, LocalDateTime depositDueAt) {

}
