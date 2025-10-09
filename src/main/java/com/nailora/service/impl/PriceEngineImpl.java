package com.nailora.service.impl;

import com.nailora.repository.ServiceItemRepository;
import com.nailora.service.PriceEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceEngineImpl implements PriceEngine {

	private final ServiceItemRepository serviceRepo;
	private final JdbcTemplate jdbc;

	@Override
	public Quote quote(Long serviceId, List<Long> addOnIds, String coupon) {
		var svc = serviceRepo.findById(serviceId).orElseThrow();

		// 1) ราคา base ของบริการ
		BigDecimal servicePrice = svc.getPrice();

		// 2) รวมราคา add-on
		BigDecimal addOnPrice = BigDecimal.ZERO;
		if (addOnIds != null && !addOnIds.isEmpty()) {
			String inSql = String.join(",", addOnIds.stream().map(id -> "?").toList());
			BigDecimal sum = jdbc.queryForObject(
					"SELECT COALESCE(SUM(extra_price),0) FROM add_on WHERE id IN (" + inSql + ")", BigDecimal.class,
					addOnIds.toArray());
			addOnPrice = (sum == null ? BigDecimal.ZERO : sum);
		}

		// 3) ราคารวม
		BigDecimal totalPrice = servicePrice.add(addOnPrice);

		// 4) มัดจำ: depositMin + 20% ของราคา add-on
		BigDecimal depositAmount = svc.getDepositMin().add(addOnPrice.multiply(new BigDecimal("0.20"))).setScale(2,
				RoundingMode.HALF_UP);

		// (ถ้ามีคูปอง ค่อยหัก/ปรับเพิ่มในอนาคต)

		return new Quote(servicePrice, addOnPrice, totalPrice, depositAmount);
	}
}
