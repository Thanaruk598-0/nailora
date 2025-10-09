package com.nailora.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // ตอนนี้ไม่ต้อง seed user/role
        System.out.println("✅ DataInitializer loaded (แต่ไม่ได้สร้าง user/role)");
    }
}
