package com.nailora.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SlotGenForm {
	@NotNull
	Long serviceId;

	// วันที่ที่ต้องการสร้าง (รูปแบบ yyyy-MM-dd)
	@NotBlank
	String date;

	// เวลาเริ่ม-จบ ช่วงทำงาน (รูปแบบ HH:mm)
	@NotBlank
	String fromTime;
	@NotBlank
	String toTime;

	// ความยาวบริการ (นาที) — ปกติเท่ากับ duration ของ service แต่เผื่อแก้ได้
	@Min(10)
	@Max(600)
	Integer durationMin = 60;

	// ความจุ/ชื่อช่าง/เปิด-ปิด
	@Min(1)
	Integer capacity = 1;
	@NotBlank
	String techName;
	Boolean open = true;

	// ช่องไฟระหว่างสลอต (นาที) เช่น 0 = หลังจบเริ่มทันที, 10 = เว้น 10 นาที
	@Min(0)
	@Max(120)
	Integer gapMin = 0;
}
