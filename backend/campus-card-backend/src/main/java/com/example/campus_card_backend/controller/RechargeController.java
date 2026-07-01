package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.RechargeRecord;
import com.example.campus_card_backend.mapper.RechargeMapper;
import com.example.campus_card_backend.service.RechargeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    private final RechargeMapper rechargeMapper;
    private final RechargeService rechargeService;

    public RechargeController(RechargeMapper rechargeMapper, RechargeService rechargeService) {
        this.rechargeMapper = rechargeMapper;
        this.rechargeService = rechargeService;
    }

    // 查询全部充值记录
    @GetMapping("/list")
    public List<RechargeRecord> list() {
        return rechargeMapper.findAll();
    }

    // 按卡号查询充值记录
    @GetMapping("/card/{cardNumber}")
    public List<RechargeRecord> getByCardNumber(@PathVariable String cardNumber) {
        return rechargeMapper.findByCardNumber(cardNumber);
    }

    // 模拟充值
    @PostMapping
    public Map<String, Object> recharge(@RequestBody RechargeRecord record) {
        return rechargeService.recharge(record);
    }
}