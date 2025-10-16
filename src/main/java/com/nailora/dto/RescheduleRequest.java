package com.nailora.dto;

public record RescheduleRequest(String phone, Long newTimeSlotId, String reason) {

}
