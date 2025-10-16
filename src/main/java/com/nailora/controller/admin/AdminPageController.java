package com.nailora.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    /** root ของแอดมิน -> ไปหน้า slots (ให้ AdminSlotsController จัดการ /admin/slots เอง) */
    @GetMapping("/admin")
    public String adminRoot() {
        return "redirect:/admin/slots";
    }

    // สำคัญ: อย่าใส่ @GetMapping("/admin/slots"), @GetMapping("/admin/services"), หรือ @GetMapping("/login") ในไฟล์นี้
}
