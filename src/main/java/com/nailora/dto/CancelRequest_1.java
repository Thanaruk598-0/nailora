package com.nailora.dto;

import com.nailora.entity.Booking;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelRequest_1 {
	private Booking.CancelReason reason;
}
