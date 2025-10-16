package com.nailora.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportsController {

	@GetMapping
	public String reportsPage() {
		// เรนเดอร์ templates/admin/reports.html
		return "admin/reports";
	}
}
