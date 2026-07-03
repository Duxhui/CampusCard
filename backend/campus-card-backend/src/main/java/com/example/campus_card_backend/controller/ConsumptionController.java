package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.ConsumptionRecord;
import com.example.campus_card_backend.mapper.ConsumptionMapper;
import com.example.campus_card_backend.service.ConsumptionService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumption")
public class ConsumptionController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ConsumptionMapper consumptionMapper;
    private final ConsumptionService consumptionService;

    public ConsumptionController(ConsumptionMapper consumptionMapper,
                                 ConsumptionService consumptionService) {
        this.consumptionMapper = consumptionMapper;
        this.consumptionService = consumptionService;
    }

    // 查询全部消费记录
    //@GetMapping("/list")
    //public List<ConsumptionRecord> list() {
    //    return consumptionMapper.findAll();
    //}

    // 查询消费记录：默认只查询本月数据，避免页面一打开加载全部历史记录
    @GetMapping("/list")
    public List<ConsumptionRecord> list() {
        String month = normalizeMonth(null);
        return consumptionMapper.findByMonth(
                getMonthStart(month),
                getNextMonthStart(month),
                null,
                null
        );
    }

    // 管理员按月份查询消费记录，可同时按卡号、商户编号筛选
    @GetMapping("/month")
    public List<ConsumptionRecord> listByMonth(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String merchantId
    ) {
        String safeMonth = normalizeMonth(month);

        return consumptionMapper.findByMonth(
                getMonthStart(safeMonth),
                getNextMonthStart(safeMonth),
                emptyToNull(cardNumber),
                emptyToNull(merchantId)
        );
    }

    // 普通用户按月份查询自己的消费记录
    @GetMapping("/user/{userId}/month")
    public List<ConsumptionRecord> listUserByMonth(
            @PathVariable Long userId,
            @RequestParam(required = false) String month
    ) {
        String safeMonth = normalizeMonth(month);

        return consumptionMapper.findByUserIdAndMonth(
                userId,
                getMonthStart(safeMonth),
                getNextMonthStart(safeMonth)
        );
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

    // 如果月份为空或格式错误，则默认使用当前月份
    private String normalizeMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return YearMonth.now().toString();
        }

        try {
            return YearMonth.parse(month.trim()).toString();
        } catch (DateTimeParseException e) {
            return YearMonth.now().toString();
        }
    }

    // 获取某月第一天 00:00:00
    private String getMonthStart(String month) {
        return YearMonth.parse(month)
                .atDay(1)
                .atStartOfDay()
                .format(DATE_TIME_FORMATTER);
    }

    // 获取下个月第一天 00:00:00
    private String getNextMonthStart(String month) {
        return YearMonth.parse(month)
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay()
                .format(DATE_TIME_FORMATTER);
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}