package com.nailora.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookingDTO {
    private Long id;

    private TimeSlotDTO timeSlot;

    private String customerName;
    private String phone;
    private String note;

    private String status;
    private BigDecimal servicePrice;
    private BigDecimal addOnPrice;
    private BigDecimal depositAmount;
    private String depositStatus;

    private LocalDateTime depositDueAt;
    private String paymentRef;
    private LocalDateTime depositPaidAt;
    private String receiptUrl;
    private String gateway;

    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;
    private String cancelReason;

    // 🔹 เพิ่มตรงนี้
    private Integer qty;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TimeSlotDTO {
        private Long id;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private Integer capacity;
        private Boolean open;
        private String techName;
        private ServiceItemDTO serviceItem;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ServiceItemDTO {
        private Long id;
        private String name;
        private Integer durationMin;
        private BigDecimal price;
        private BigDecimal depositMin;
        private Boolean active;
    }
}
