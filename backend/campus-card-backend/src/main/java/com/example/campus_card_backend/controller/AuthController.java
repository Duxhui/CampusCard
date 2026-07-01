package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.User;
import com.example.campus_card_backend.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户登录接口
     * 前端传入手机号和密码，后端根据 user 表进行验证。
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginForm) {
        Map<String, Object> result = new HashMap<>();

        String phone = loginForm.get("phone");
        String password = loginForm.get("password");

        if (phone == null || phone.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "手机号不能为空");
            return result;
        }

        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            return result;
        }

        User user = userMapper.login(phone, password);

        if (user == null) {
            result.put("success", false);
            result.put("message", "手机号或密码错误");
            return result;
        }

        // 不直接返回完整 user，避免把密码返回给前端
        Map<String, Object> safeUser = new HashMap<>();
        safeUser.put("userId", user.getUserId());
        safeUser.put("name", user.getName());
        safeUser.put("idNumber", user.getIdNumber());
        safeUser.put("phone", user.getPhone());
        safeUser.put("email", user.getEmail());
        safeUser.put("userType", user.getUserType());
        safeUser.put("balance", user.getBalance());
        safeUser.put("registerTime", user.getRegisterTime());

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("user", safeUser);

        return result;
    }
}