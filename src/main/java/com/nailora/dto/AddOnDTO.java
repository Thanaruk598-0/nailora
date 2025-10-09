package com.nailora.dto;

import java.math.BigDecimal;

public record AddOnDTO(
        Long id,
        String name,
        Integer extraMinutes,
        BigDecimal extraPrice,
        Boolean active
) {}
