package com.nailora.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyDepositForm {
    private String paymentRef; // เช่น Stripe PaymentIntent id
}
