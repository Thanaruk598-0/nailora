package com.nailora.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminBookingsPageController {

    @GetMapping("/admin/bookings")
    public String bookingsPage(Model model) {
        model.addAttribute("title", "Admin • Bookings");
        return "admin/bookings"; // templates/admin/bookings.html
    }
}
