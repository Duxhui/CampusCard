package com.example.campus_card_backend.mapper;

import com.example.campus_card_backend.entity.RechargeRecord;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RechargeMapper {

    // 查询所有充值记录，并关联显示卡号、持卡人、充值方式、充值状态等信息
    @Select("""
            SELECT r.recharge_id,
                   r.card_number,
                   r.amount,
                   r.recharge_method,
                   r.recharge_time,
                   r.status,
                   c.user_id,
                   u.name AS user_name
            FROM recharge_record r
            LEFT JOIN card c ON r.card_number = c.card_number
            LEFT JOIN `user` u ON c.user_id = u.user_id
            ORDER BY r.recharge_time DESC, r.recharge_id DESC
            """)
    List<RechargeRecord> findAll();

    // 管理员按月份查询充值记录，可同时按卡号筛选
    @Select("""
        <script>
        SELECT r.recharge_id,
               r.card_number,
               r.amount,
               r.recharge_method,
               r.recharge_time,
               r.status,
               c.user_id,
               u.name AS user_name
        FROM recharge_record r
        LEFT JOIN card c ON r.card_number = c.card_number
        LEFT JOIN `user` u ON c.user_id = u.user_id
        WHERE r.recharge_time &gt;= #{startTime}
          AND r.recharge_time &lt; #{endTime}
        <if test="cardNumber != null and cardNumber != ''">
            AND r.card_number = #{cardNumber}
        </if>
        ORDER BY r.recharge_time DESC, r.recharge_id DESC
        </script>
        """)
    List<RechargeRecord> findByMonth(@Param("startTime") String startTime,
                                     @Param("endTime") String endTime,
                                     @Param("cardNumber") String cardNumber);

    // 普通用户按月份查询自己的充值记录
    @Select("""
        <script>
        SELECT r.recharge_id,
               r.card_number,
               r.amount,
               r.recharge_method,
               r.recharge_time,
               r.status,
               c.user_id,
               u.name AS user_name
        FROM recharge_record r
        LEFT JOIN card c ON r.card_number = c.card_number
        LEFT JOIN `user` u ON c.user_id = u.user_id
        WHERE c.user_id = #{userId}
          AND r.recharge_time &gt;= #{startTime}
          AND r.recharge_time &lt; #{endTime}
        ORDER BY r.recharge_time DESC, r.recharge_id DESC
        </script>
        """)
    List<RechargeRecord> findByUserIdAndMonth(@Param("userId") Long userId,
                                              @Param("startTime") String startTime,
                                              @Param("endTime") String endTime);

    // 按卡号查询充值记录
    @Select("""
            SELECT r.recharge_id,
                   r.card_number,
                   r.amount,
                   r.recharge_method,
                   r.recharge_time,
                   r.status,
                   c.user_id,
                   u.name AS user_name
            FROM recharge_record r
            LEFT JOIN card c ON r.card_number = c.card_number
            LEFT JOIN `user` u ON c.user_id = u.user_id
            WHERE r.card_number = #{cardNumber}
            ORDER BY r.recharge_time DESC, r.recharge_id DESC
            """)
    List<RechargeRecord> findByCardNumber(String cardNumber);

    // 根据卡号查询卡片状态：1正常/2挂失/3注销
    @Select("SELECT card_status FROM card WHERE card_number = #{cardNumber}")
    Integer findCardStatusByCardNumber(String cardNumber);

    // 新增充值记录
    @Insert("""
            INSERT INTO recharge_record(card_number, amount, recharge_method, recharge_time, status)
            VALUES(#{cardNumber}, #{amount}, #{rechargeMethod}, NOW(), #{status})
            """)
    int insert(RechargeRecord record);

    // 充值成功后，根据卡号找到用户并增加用户余额
    @Update("""
            UPDATE `user` u
            JOIN card c ON u.user_id = c.user_id
            SET u.balance = u.balance + #{amount}
            WHERE c.card_number = #{cardNumber}
            """)
    int increaseBalance(@Param("cardNumber") String cardNumber,
                        @Param("amount") BigDecimal amount);

    // 删除某卡号的所有充值记录
    @Delete("DELETE FROM recharge_record WHERE card_number = #{cardNumber}")
    int deleteByCardNumber(String cardNumber);
}
