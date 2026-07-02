package com.example.campus_card_backend.mapper;

import com.example.campus_card_backend.entity.ConsumptionRecord;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ConsumptionMapper {

    // 查询所有消费记录，关联显示卡号、持卡人、商户名称等信息
    @Select("""
            SELECT cr.consumption_id,
                   cr.card_number,
                   cr.merchant_id,
                   cr.amount,
                   cr.consumption_time,
                   cr.note,
                   c.user_id,
                   u.name AS user_name,
                   u.balance AS balance,
                   m.merchant_name,
                   m.merchant_type,
                   m.business_status
            FROM consumption_record cr
            LEFT JOIN card c ON cr.card_number = c.card_number
            LEFT JOIN `user` u ON c.user_id = u.user_id
            LEFT JOIN merchant m ON cr.merchant_id = m.merchant_id
            ORDER BY cr.consumption_id ASC
            """)
    List<ConsumptionRecord> findAll();

    // 按卡号查询消费记录
    @Select("""
            SELECT cr.consumption_id,
                   cr.card_number,
                   cr.merchant_id,
                   cr.amount,
                   cr.consumption_time,
                   cr.note,
                   c.user_id,
                   u.name AS user_name,
                   u.balance AS balance,
                   m.merchant_name,
                   m.merchant_type,
                   m.business_status
            FROM consumption_record cr
            LEFT JOIN card c ON cr.card_number = c.card_number
            LEFT JOIN `user` u ON c.user_id = u.user_id
            LEFT JOIN merchant m ON cr.merchant_id = m.merchant_id
            WHERE cr.card_number = #{cardNumber}
            ORDER BY cr.consumption_time DESC
            """)
    List<ConsumptionRecord> findByCardNumber(String cardNumber);

    // 按商户查询消费记录
    @Select("""
            SELECT cr.consumption_id,
                   cr.card_number,
                   cr.merchant_id,
                   cr.amount,
                   cr.consumption_time,
                   cr.note,
                   c.user_id,
                   u.name AS user_name,
                   u.balance AS balance,
                   m.merchant_name,
                   m.merchant_type,
                   m.business_status
            FROM consumption_record cr
            LEFT JOIN card c ON cr.card_number = c.card_number
            LEFT JOIN `user` u ON c.user_id = u.user_id
            LEFT JOIN merchant m ON cr.merchant_id = m.merchant_id
            WHERE cr.merchant_id = #{merchantId}
            ORDER BY cr.consumption_time DESC
            """)
    List<ConsumptionRecord> findByMerchantId(String merchantId);

    // 根据消费ID查询单条消费记录
    @Select("""
            SELECT cr.consumption_id,
                   cr.card_number,
                   cr.merchant_id,
                   cr.amount,
                   cr.consumption_time,
                   cr.note,
                   c.user_id,
                   u.name AS user_name,
                   u.balance AS balance,
                   m.merchant_name,
                   m.merchant_type,
                   m.business_status
            FROM consumption_record cr
            LEFT JOIN card c ON cr.card_number = c.card_number
            LEFT JOIN `user` u ON c.user_id = u.user_id
            LEFT JOIN merchant m ON cr.merchant_id = m.merchant_id
            WHERE cr.consumption_id = #{consumptionId}
            """)
    ConsumptionRecord findById(Long consumptionId);

    // 查询卡片状态和用户余额
    @Select("""
            SELECT c.card_status AS cardStatus,
                   c.user_id AS userId,
                   u.balance AS balance
            FROM card c
            LEFT JOIN `user` u ON c.user_id = u.user_id
            WHERE c.card_number = #{cardNumber}
            """)
    Map<String, Object> findCardAndBalance(String cardNumber);

    // 查询商户营业状态
    @Select("SELECT business_status FROM merchant WHERE merchant_id = #{merchantId}")
    Integer findMerchantStatus(String merchantId);

    // 新增消费记录
    @Insert("""
            INSERT INTO consumption_record(card_number, merchant_id, amount, consumption_time, note)
            VALUES(#{cardNumber}, #{merchantId}, #{amount}, NOW(), #{note})
            """)
    int insert(ConsumptionRecord record);

    // 消费成功后，根据卡号找到用户并扣减余额
    @Update("""
            UPDATE `user` u
            JOIN card c ON u.user_id = c.user_id
            SET u.balance = u.balance - #{amount}
            WHERE c.card_number = #{cardNumber}
            """)
    int decreaseBalance(@Param("cardNumber") String cardNumber,
                        @Param("amount") BigDecimal amount);

    // 退款时返还余额
    @Update("""
            UPDATE `user` u
            JOIN card c ON u.user_id = c.user_id
            SET u.balance = u.balance + #{amount}
            WHERE c.card_number = #{cardNumber}
            """)
    int increaseBalance(@Param("cardNumber") String cardNumber,
                        @Param("amount") BigDecimal amount);

    // 将原消费记录标记为已退款
    @Update("""
            UPDATE consumption_record
            SET note = CONCAT(IFNULL(note, ''), '；已退款')
            WHERE consumption_id = #{consumptionId}
            """)
    int markRefunded(Long consumptionId);

    // 删除某卡号的所有消费记录
    @Delete("DELETE FROM consumption_record WHERE card_number = #{cardNumber}")
    int deleteByCardNumber(String cardNumber);
}