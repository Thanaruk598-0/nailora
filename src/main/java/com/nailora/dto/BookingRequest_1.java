package com.nailora.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookingRequest_1 {
    private Long timeSlotId;
    private String customerName;
    private String phone;
    private String note;

    private BigDecimal servicePrice;
    private BigDecimal addOnPrice;
    private BigDecimal depositAmount;

    // ✅ เพิ่ม status เอาไว้ให้ตารางใน requests.html
    private String status; // NEW, APPROVED, REJECTED
}

