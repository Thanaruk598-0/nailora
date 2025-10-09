package com.nailora.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {
    @GetMapping("/services")
    public String servicesPage() {
        return "admin/services"; // ไม่มี .html ต่อท้าย
    }
}

