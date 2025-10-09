package com.nailora.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping("/services")
    public String servicesPage() {
        return "admin/services";
    }

    @GetMapping("/reports")
    public String reportsPage() {
        return "admin/reports";
    }

    @GetMapping("/requests")
    public String requestsPage() {
        return "admin/requests";
    }
    @GetMapping("/bookings")
    public String bookingsPage() {
        return "admin/bookings"; // 👉 templates/admin/bookings.html
    }
}
