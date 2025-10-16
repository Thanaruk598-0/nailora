package com.nailora.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingSummary(Long id, String serviceName, String techName, LocalDateTime startAt, String status,
		String depositStatus, BigDecimal depositAmount, String receiptUrl) {

}
