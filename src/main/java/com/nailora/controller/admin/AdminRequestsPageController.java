package com.nailora.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminRequestsPageController {

    @GetMapping("/admin/requests")
    public String requestsPage(Model model) {
        model.addAttribute("title", "Admin • Requests");
        return "admin/requests"; 
    }
}
