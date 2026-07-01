package com.example.campus_card_backend.service;

import com.example.campus_card_backend.entity.ConsumptionRecord;
import com.example.campus_card_backend.mapper.ConsumptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConsumptionService {

    private final ConsumptionMapper consumptionMapper;

    public ConsumptionService(ConsumptionMapper consumptionMapper) {
        this.consumptionMapper = consumptionMapper;
    }

    /**
     * 模拟消费：
     * 1. 校验卡号；
     * 2. 校验商户编号；
     * 3. 校验消费金额；
     * 4. 校验卡片是否存在；
     * 5. 校验卡片状态是否正常；
     * 6. 校验商户是否存在；
     * 7. 校验商户是否营业；
     * 8. 校验余额是否充足；
     * 9. 插入消费记录。
     *
     * 注意：
     * 余额扣减由数据库触发器 trg_consumption_insert 自动完成，
     * 所以后端这里不再手动调用 decreaseBalance，否则余额会重复扣减。
     */
    @Transactional
    public Map<String, Object> consume(ConsumptionRecord record) {
        Map<String, Object> result = new HashMap<>();

        // 1. 校验卡号
        if (record.getCardNumber() == null || record.getCardNumber().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "卡号不能为空");
            return result;
        }

        // 2. 校验商户编号
        if (record.getMerchantId() == null || record.getMerchantId().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "商户编号不能为空");
            return result;
        }

        // 3. 校验消费金额
        if (record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            result.put("success", false);
            result.put("message", "消费金额必须大于0");
            return result;
        }

        // 4. 查询卡片状态和用户余额
        Map<String, Object> cardInfo = consumptionMapper.findCardAndBalance(record.getCardNumber());

        if (cardInfo == null) {
            result.put("success", false);
            result.put("message", "卡片不存在");
            return result;
        }

        Integer cardStatus = ((Number) cardInfo.get("cardStatus")).intValue();
        BigDecimal balance = (BigDecimal) cardInfo.get("balance");

        // 5. 判断卡片状态
        if (cardStatus != 1) {
            result.put("success", false);
            result.put("message", "卡片状态异常，只有正常状态的卡片可以消费");
            return result;
        }

        // 6. 查询商户状态
        Integer merchantStatus = consumptionMapper.findMerchantStatus(record.getMerchantId());

        if (merchantStatus == null) {
            result.put("success", false);
            result.put("message", "商户不存在");
            return result;
        }

        // 7. 判断商户是否营业
        if (merchantStatus != 1) {
            result.put("success", false);
            result.put("message", "商户处于停业状态，不能消费");
            return result;
        }

        // 8. 判断余额是否充足
        // 虽然数据库触发器会自动扣余额，但余额是否足够必须由后端先判断
        if (balance.compareTo(record.getAmount()) < 0) {
            result.put("success", false);
            result.put("message", "余额不足，消费失败");
            return result;
        }

        // 9. 插入消费记录
        // 插入成功后，数据库触发器会自动扣减 user.balance
        int insertRows = consumptionMapper.insert(record);

        result.put("success", insertRows > 0);
        result.put("message", insertRows > 0 ? "消费成功，余额已由数据库触发器自动扣减" : "消费失败");

        return result;
    }

    /**
     * 退款/冲正：
     * 1. 查询消费记录是否存在；
     * 2. 判断是否已经退款；
     * 3. 将消费金额返还到用户余额；
     * 4. 将原消费记录标记为已退款。
     *
     * 注意：
     * 当前数据库只写了“插入消费记录自动扣余额”的触发器，
     * 没有写“退款自动返还余额”的触发器，
     * 所以退款这里仍然由 Java 后端手动返还余额。
     */
    @Transactional
    public Map<String, Object> refund(Long consumptionId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询消费记录
        ConsumptionRecord record = consumptionMapper.findById(consumptionId);

        if (record == null) {
            result.put("success", false);
            result.put("message", "消费记录不存在");
            return result;
        }

        // 2. 防止重复退款
        if (record.getNote() != null && record.getNote().contains("已退款")) {
            result.put("success", false);
            result.put("message", "该消费记录已经退款，不能重复退款");
            return result;
        }

        // 3. 返还余额
        int updateBalanceRows = consumptionMapper.increaseBalance(record.getCardNumber(), record.getAmount());

        // 4. 标记原消费记录为已退款
        int markRows = consumptionMapper.markRefunded(consumptionId);

        result.put("success", updateBalanceRows > 0 && markRows > 0);
        result.put("message", updateBalanceRows > 0 && markRows > 0 ? "退款成功，余额已返还" : "退款失败");

        return result;
    }
}