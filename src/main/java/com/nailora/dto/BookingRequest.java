package com.nailora.dto;

import java.util.List;

public record BookingRequest(Long timeSlotId, String customerName, String phone, String note, List<Long> addOnIds) {

}
