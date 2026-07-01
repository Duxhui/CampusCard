package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.ConsumptionRecord;
import com.example.campus_card_backend.mapper.ConsumptionMapper;
import com.example.campus_card_backend.service.ConsumptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumption")
public class ConsumptionController {

    private final ConsumptionMapper consumptionMapper;
    private final ConsumptionService consumptionService;

    public ConsumptionController(ConsumptionMapper consumptionMapper,
                                 ConsumptionService consumptionService) {
        this.consumptionMapper = consumptionMapper;
        this.consumptionService = consumptionService;
    }

    // 查询全部消费记录
    @GetMapping("/list")
    public List<ConsumptionRecord> list() {
        return consumptionMapper.findAll();
    }

    // 按卡号查询消费记录
    @GetMapping("/card/{cardNumber}")
    public List<ConsumptionRecord> getByCardNumber(@PathVariable String cardNumber) {
        return consumptionMapper.findByCardNumber(cardNumber);
    }

    // 按商户查询消费记录
    @GetMapping("/merchant/{merchantId}")
    public List<ConsumptionRecord> getByMerchantId(@PathVariable String merchantId) {
        return consumptionMapper.findByMerchantId(merchantId);
    }

    // 模拟消费
    @PostMapping
    public Map<String, Object> consume(@RequestBody ConsumptionRecord record) {
        return consumptionService.consume(record);
    }

    // 退款/冲正
    @PutMapping("/{consumptionId}/refund")
    public Map<String, Object> refund(@PathVariable Long consumptionId) {
        return consumptionService.refund(consumptionId);
    }
}