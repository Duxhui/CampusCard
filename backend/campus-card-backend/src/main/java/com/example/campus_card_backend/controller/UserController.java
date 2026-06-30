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

    @PostMapping
    public User add(@RequestBody User user) {
        userMapper.insertWithId(user);
        return user;  // 返回包含自增 ID 的完整用户对象
    }
    
    // 搜索用户（支持关键字和类型筛选）
    @GetMapping("/search")
    public List<User> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType) {
        if (keyword != null && !keyword.isEmpty()) {
            return userMapper.searchByKeyword(keyword);
        } else if (userType != null) {
            return userMapper.selectByType(userType);
        } else {
            return userMapper.selectAll();
        }
    }

    // 更新用户
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        user.setUserId(id);
        userMapper.update(user);
        return userMapper.selectById(id);
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userMapper.deleteById(id);
    }

}