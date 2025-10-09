package com.nailora.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookingRequest {
    // สำหรับ create ให้ส่งมา, สำหรับ update จะไม่เปลี่ยน slot ถ้าไม่ส่งค่า
    private Long timeSlotId;

    private String customerName;
    private String phone;
    private String note;

    // snapshot ตอนจอง (สร้างแล้วแก้ทีหลังได้เฉพาะฟิลด์ลูกค้า)
    private BigDecimal servicePrice;
    private BigDecimal addOnPrice;
    private BigDecimal depositAmount;
}
