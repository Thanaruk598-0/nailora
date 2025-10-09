package com.nailora.service;

public interface DepositService {

	void voidDeposit(Long bookingId, String reason);

	void refundFull(Long bookingId, String refundRef, String reason);

}
