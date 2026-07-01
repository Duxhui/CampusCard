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

    /**
     * 搜索用户
     * @param field  搜索字段：id / name / id_number / phone（可选）
     * @param value  搜索值（当 field 不为空时必填）
     * @param userType 用户类型：1学生 / 2教职工（可选，不传或传0表示全部）
     * @return 用户列表
     */
    @GetMapping("/search")
    public List<User> search(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String value,
            @RequestParam(required = false) Integer userType) {

        if (field != null && !field.isEmpty() && value != null && !value.isEmpty()) {
            String dbField = mapField(field);
            if (dbField == null) {
                throw new IllegalArgumentException("无效的搜索字段: " + field);
            }

            List<User> users;
            // 姓名使用模糊查询
            if ("name".equals(field)) {
                users = userMapper.selectByNameLike(value);
            } else {
                // 其他字段（id, id_number, phone）使用精确查询
                Object paramValue = "id".equals(field) ? Long.parseLong(value) : value;
                users = userMapper.selectByField(dbField, paramValue);
            }

            // 如果还传了 userType，进一步过滤
            if (userType != null && userType > 0) {
                users.removeIf(u -> !u.getUserType().equals(userType));
            }
            return users;
        }

        if (userType != null && userType > 0) {
            return userMapper.selectByType(userType);
        }

        return userMapper.selectAll();
    }

    // 新增用户
    @PostMapping
    public User add(@RequestBody User user) {
        userMapper.insert(user);
        return user;
    }

    // 更新用户
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        user.setUserId(id);
        userMapper.update(user);
        return userMapper.selectByField("user_id", id).get(0);
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userMapper.deleteById(id);
    }

    // 新增：验证用户密码
    @PostMapping("/verify")
    public boolean verifyPassword(@RequestParam Long userId, @RequestParam String password) {
        String storedPassword = userMapper.selectPasswordById(userId);
        return storedPassword != null && storedPassword.equals(password);
    }

    // 字段名映射（防止 SQL 注入）
    private String mapField(String field) {
        switch (field) {
            case "id": return "user_id";
            case "name": return "name";
            case "id_number": return "id_number";
            case "phone": return "phone";
            default: return null;
        }
    }
}