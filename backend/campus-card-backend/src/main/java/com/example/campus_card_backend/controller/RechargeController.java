package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.RechargeRecord;
import com.example.campus_card_backend.mapper.RechargeMapper;
import com.example.campus_card_backend.service.RechargeService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RechargeMapper rechargeMapper;
    private final RechargeService rechargeService;

    public RechargeController(RechargeMapper rechargeMapper, RechargeService rechargeService) {
        this.rechargeMapper = rechargeMapper;
        this.rechargeService = rechargeService;
    }

    // 查询充值记录：默认只查询本月数据，避免页面一打开加载全部历史记录
    @GetMapping("/list")
    public List<RechargeRecord> list() {
        String month = normalizeMonth(null);
        return rechargeMapper.findByMonth(
                getMonthStart(month),
                getNextMonthStart(month),
                null
        );
    }

    // 管理员按月份查询充值记录，可同时按卡号筛选
    @GetMapping("/month")
    public List<RechargeRecord> listByMonth(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String cardNumber
    ) {
        String safeMonth = normalizeMonth(month);

        return rechargeMapper.findByMonth(
                getMonthStart(safeMonth),
                getNextMonthStart(safeMonth),
                emptyToNull(cardNumber)
        );
    }

    // 普通用户按月份查询自己的充值记录
    @GetMapping("/user/{userId}/month")
    public List<RechargeRecord> listUserByMonth(
            @PathVariable Long userId,
            @RequestParam(required = false) String month
    ) {
        String safeMonth = normalizeMonth(month);

        return rechargeMapper.findByUserIdAndMonth(
                userId,
                getMonthStart(safeMonth),
                getNextMonthStart(safeMonth)
        );
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
