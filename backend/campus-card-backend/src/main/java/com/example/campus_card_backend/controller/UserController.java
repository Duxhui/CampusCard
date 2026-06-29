package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.User;
import com.example.campus_card_backend.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    public UserController(UserMapper userMapper) { this.userMapper = userMapper; }

    // GET http://localhost:8080/api/user/1
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userMapper.selectById(id);
    }

    // GET http://localhost:8080/api/user/list
    @GetMapping("/list")
    public List<User> list() {
        return userMapper.selectAll();
    }
}