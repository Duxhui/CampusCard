package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.Card;
import com.example.campus_card_backend.entity.User;
import com.example.campus_card_backend.mapper.CardMapper;
import com.example.campus_card_backend.mapper.ConsumptionMapper;
import com.example.campus_card_backend.mapper.RechargeMapper;
import com.example.campus_card_backend.mapper.RecycledUserIdMapper;
import com.example.campus_card_backend.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final CardMapper cardMapper;
    private final ConsumptionMapper consumptionMapper;
    private final RechargeMapper rechargeMapper;
    private final RecycledUserIdMapper recycledUserIdMapper;

    public UserController(UserMapper userMapper, CardMapper cardMapper,
                          ConsumptionMapper consumptionMapper,
                          RechargeMapper rechargeMapper,
                          RecycledUserIdMapper recycledUserIdMapper) {
        this.userMapper = userMapper;
        this.cardMapper = cardMapper;
        this.consumptionMapper = consumptionMapper;
        this.rechargeMapper = rechargeMapper;
        this.recycledUserIdMapper = recycledUserIdMapper;
    }

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
        // 优先使用回收 ID（删除用户后存入回收池的 ID）
        Long recycledId = recycledUserIdMapper.selectRecycledId();
        if (recycledId != null) {
            user.setUserId(recycledId);
            userMapper.insertWithIdSpecified(user);
            recycledUserIdMapper.deleteRecycledId(recycledId);
        } else {
            userMapper.insertWithId(user);
        }

        // 自动发卡
        Card card = new Card();
        card.setUserId(user.getUserId());
        card.setCardNumber(generateCardNumber());
        cardMapper.insert(card);

        return user;
    }
    
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
        // 级联删除：消费/充值 → 卡片 → 用户
        List<Card> cards = cardMapper.findByUserId(id);
        for (Card card : cards) {
            consumptionMapper.deleteByCardNumber(card.getCardNumber());
            rechargeMapper.deleteByCardNumber(card.getCardNumber());
        }
        cardMapper.deleteCardsByUserId(id);        // 删除所有卡片
        userMapper.deleteById(id);                  // 删除用户
        recycledUserIdMapper.insertRecycledId(id);  // 回收 ID
    }

    // 生成唯一卡号（格式：CARD + 自增序号，与 CardController 保持一致）
    private String generateCardNumber() {
        String maxCardNumber = cardMapper.findMaxCardNumber();
        if (maxCardNumber == null || maxCardNumber.isEmpty()) {
            return "CARD1001";
        }
        long nextNum = Long.parseLong(maxCardNumber.replace("CARD", "")) + 1;
        return "CARD" + nextNum;
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