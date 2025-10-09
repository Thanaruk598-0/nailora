package com.nailora.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestLombok {
    private String username;
    private int age;

    public static void main(String[] args) {
        TestLombok u = TestLombok.builder()
                .username("admin")
                .age(20)
                .build();

        System.out.println(u.getUsername() + " - " + u.getAge());
    }
}
