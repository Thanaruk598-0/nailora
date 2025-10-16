package com.nailora.dto;

import java.math.BigDecimal;
import java.util.List;

public record PriceQuote(Long serviceId, List<Long> addOnIds, BigDecimal servicePrice, BigDecimal addOnPrice,
		BigDecimal discount, BigDecimal depositAmount, BigDecimal total) {

}
