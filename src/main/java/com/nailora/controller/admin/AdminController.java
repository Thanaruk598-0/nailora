package com.nailora.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AdminController {

	// เผื่อกด /admin ตรง ๆ จะพาไป /admin/services
	@GetMapping("/admin")
	public String adminHome() {
		return "redirect:/admin/services";
	}

	@GetMapping("/admin/services")
	public String services(Model model, Principal principal) {
		// เอาชื่อ user ที่ล็อกอินมาโชว์
		model.addAttribute("username", principal != null ? principal.getName() : "admin");
		return "admin/services";
	}
}
