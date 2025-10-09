package com.nailora.service;

import java.math.BigDecimal;
import java.util.List;

public interface PriceEngine {
	record Quote(BigDecimal servicePrice, BigDecimal addOnPrice, BigDecimal totalPrice, BigDecimal depositAmount) {
	}

	Quote quote(Long serviceId, List<Long> addOnIds, String coupon);
}
