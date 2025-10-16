package com.nailora.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.nailora.entity.User;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home(Model model) {
		boolean isAdmin = false;

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
			isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_ADMIN"));
		}

		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("title", "NAILORA Home");
		return "home"; // templates/home.html
	}
}
