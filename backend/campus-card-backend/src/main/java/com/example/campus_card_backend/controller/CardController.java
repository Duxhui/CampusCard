package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.Card;
import com.example.campus_card_backend.mapper.CardMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/card")
public class CardController {

    private final CardMapper cardMapper;

    public CardController(CardMapper cardMapper) {
        this.cardMapper = cardMapper;
    }

    // 查询全部卡片
    @GetMapping("/list")
    public List<Card> list() {
        return cardMapper.findAll();
    }

    // 根据卡号查询卡片
    @GetMapping("/{cardNumber}")
    public Card getByCardNumber(@PathVariable String cardNumber) {
        return cardMapper.findByCardNumber(cardNumber);
    }

    // 按用户查询卡片
    @GetMapping("/user/{userId}")
    public List<Card> getByUserId(@PathVariable Long userId) {
        return cardMapper.findByUserId(userId);
    }

    // 为已有用户发卡
    @PostMapping("/issue/{userId}")
    public Map<String, Object> issueCard(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        int userCount = cardMapper.countUserById(userId);
        if (userCount == 0) {
            result.put("success", false);
            result.put("message", "用户不存在，无法发卡");
            return result;
        }

        int normalCardCount = cardMapper.countNormalCardByUserId(userId);
        if (normalCardCount > 0) {
            result.put("success", false);
            result.put("message", "该用户已有正常使用的一卡通，不能重复发卡");
            return result;
        }

        String cardNumber = generateCardNumber();

        while (cardMapper.findByCardNumber(cardNumber) != null) {
            cardNumber = generateCardNumber();
        }

        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setUserId(userId);

        int rows = cardMapper.insert(card);

        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "发卡成功" : "发卡失败");
        result.put("cardNumber", cardNumber);
        return result;
    }

    // 挂失
    @PutMapping("/{cardNumber}/lost")
    public Map<String, Object> lost(@PathVariable String cardNumber) {
        Map<String, Object> result = new HashMap<>();

        Card card = cardMapper.findByCardNumber(cardNumber);
        if (card == null) {
            result.put("success", false);
            result.put("message", "卡片不存在");
            return result;
        }

        if (card.getCardStatus() == 3) {
            result.put("success", false);
            result.put("message", "卡片已注销，不能挂失");
            return result;
        }

        int rows = cardMapper.updateStatus(cardNumber, 2);

        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "挂失成功" : "挂失失败");
        return result;
    }

    // 解挂
    @PutMapping("/{cardNumber}/unlock")
    public Map<String, Object> unlock(@PathVariable String cardNumber) {
        Map<String, Object> result = new HashMap<>();

        Card card = cardMapper.findByCardNumber(cardNumber);
        if (card == null) {
            result.put("success", false);
            result.put("message", "卡片不存在");
            return result;
        }

        if (card.getCardStatus() == 3) {
            result.put("success", false);
            result.put("message", "卡片已注销，不能解挂");
            return result;
        }

        int rows = cardMapper.updateStatus(cardNumber, 1);

        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "解挂成功" : "解挂失败");
        return result;
    }

    // 注销卡片：注销前将该用户余额清零
    @PutMapping("/{cardNumber}/cancel")
    public Map<String, Object> cancel(@PathVariable String cardNumber) {
        Map<String, Object> result = new HashMap<>();

        Card card = cardMapper.findByCardNumber(cardNumber);
        if (card == null) {
            result.put("success", false);
            result.put("message", "卡片不存在");
            return result;
        }

        if (card.getCardStatus() == 3) {
            result.put("success", false);
            result.put("message", "卡片已经注销");
            return result;
        }

        cardMapper.clearUserBalance(card.getUserId());
        int rows = cardMapper.cancelCard(cardNumber);

        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "注销成功，用户余额已清零" : "注销失败");
        return result;
    }

    // 生成唯一卡号
    // 格式：CARD + 自增序号，与数据库中已有卡号保持一致
    // 例：CARD1001, CARD1002, ...
    private String generateCardNumber() {
        String maxCardNumber = cardMapper.findMaxCardNumber();
        if (maxCardNumber == null || maxCardNumber.isEmpty()) {
            return "CARD1001";
        }
        long nextNum = Long.parseLong(maxCardNumber.replace("CARD", "")) + 1;
        return "CARD" + nextNum;
    }
}