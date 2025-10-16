package com.nailora.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AddOnRequest(@NotBlank String name, @NotNull Integer extraMinutes,
		@NotNull @DecimalMin("0.00") BigDecimal extraPrice, Boolean active) {
}