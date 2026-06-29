package com.example.campus_card_backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;          // user_id
    private String name;
    private String idNumber;
    private String phone;
    private String email;
    private String password;
    private Integer userType;     // 1学生/2教职工
    private BigDecimal balance;   // DECIMAL(10,2) → BigDecimal，别用 Double
    private LocalDateTime registerTime;
}