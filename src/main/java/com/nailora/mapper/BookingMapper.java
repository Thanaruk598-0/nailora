package com.nailora.mapper;

import com.nailora.dto.BookingDTO;
import com.nailora.entity.Booking;
import com.nailora.entity.ServiceItem;
import com.nailora.entity.TimeSlot;

import java.util.List;
import java.util.stream.Collectors;

public final class BookingMapper {

    private BookingMapper() {}

    public static BookingDTO toDto(Booking b) {
        TimeSlot ts = b.getTimeSlot();
        ServiceItem s = ts.getServiceItem();

        return BookingDTO.builder()
                .id(b.getId())
                .customerName(b.getCustomerName())
                .phone(b.getPhone())
                .note(b.getNote())
                .status(b.getStatus().name())
                .servicePrice(b.getServicePrice())
                .addOnPrice(b.getAddOnPrice())
                .depositAmount(b.getDepositAmount())
                .depositStatus(b.getDepositStatus().name())
                .depositDueAt(b.getDepositDueAt())
                .paymentRef(b.getPaymentRef())
                .depositPaidAt(b.getDepositPaidAt())
                .receiptUrl(b.getReceiptUrl())
                .gateway(b.getGateway().name())
                .createdAt(b.getCreatedAt())
                .canceledAt(b.getCanceledAt())
                .cancelReason(b.getCancelReason() != null ? b.getCancelReason().name() : null)
                .timeSlot(BookingDTO.TimeSlotDTO.builder()
                        .id(ts.getId())
                        .startAt(ts.getStartAt())
                        .endAt(ts.getEndAt())
                        .capacity(ts.getCapacity())
                        .open(ts.getOpen())
                        .techName(ts.getTechName())
                        .serviceItem(BookingDTO.ServiceItemDTO.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .durationMin(s.getDurationMin())
                                .price(s.getPrice())
                                .depositMin(s.getDepositMin())
                                .active(s.getActive())
                                .build())
                        .build())
                .build();
    }

    public static List<BookingDTO> toDtoList(List<Booking> list) {
        return list.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }
}