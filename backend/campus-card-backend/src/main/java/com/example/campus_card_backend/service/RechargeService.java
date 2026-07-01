package com.example.campus_card_backend.service;

import com.example.campus_card_backend.entity.RechargeRecord;
import com.example.campus_card_backend.mapper.RechargeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class RechargeService {

    private final RechargeMapper rechargeMapper;

    public RechargeService(RechargeMapper rechargeMapper) {
        this.rechargeMapper = rechargeMapper;
    }

    /**
     * 模拟充值：
     * 1. 校验卡号是否为空；
     * 2. 校验充值金额是否合法；
     * 3. 校验卡片是否存在；
     * 4. 校验卡片状态是否正常；
     * 5. 插入充值记录。
     *
     * 注意：
     * 余额增加由数据库触发器 trg_recharge_insert 自动完成，
     * 所以后端这里不再手动调用 increaseBalance，否则余额会重复增加。
     */
    @Transactional
    public Map<String, Object> recharge(RechargeRecord record) {
        Map<String, Object> result = new HashMap<>();

        // 1. 校验卡号
        if (record.getCardNumber() == null || record.getCardNumber().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "卡号不能为空");
            return result;
        }

        // 2. 校验充值金额
        if (record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            result.put("success", false);
            result.put("message", "充值金额必须大于0");
            return result;
        }

        // 3. 查询卡片状态
        Integer cardStatus = rechargeMapper.findCardStatusByCardNumber(record.getCardNumber());

        if (cardStatus == null) {
            result.put("success", false);
            result.put("message", "卡片不存在");
            return result;
        }

        // 4. 只有正常状态的卡片可以充值
        if (cardStatus != 1) {
            result.put("success", false);
            result.put("message", "卡片状态异常，只有正常状态的卡片可以充值");
            return result;
        }

        // 5. 如果没有传充值方式，默认设置为 0
        if (record.getRechargeMethod() == null) {
            record.setRechargeMethod(0);
        }

        // 6. 设置充值状态为成功
        // status = 1 表示充值成功，触发器会根据这个状态自动增加余额
        record.setStatus(1);

        // 7. 插入充值记录
        // 插入成功后，数据库触发器会自动更新 user.balance
        int insertRows = rechargeMapper.insert(record);

        result.put("success", insertRows > 0);
        result.put("message", insertRows > 0 ? "充值成功，余额已由数据库触发器自动更新" : "充值失败");

        return result;
    }
}