package com.example.campus_card_backend.mapper;

import com.example.campus_card_backend.entity.Card;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CardMapper {

    // 查询全部卡片，并关联显示持卡人姓名和余额
    @Select("""
            SELECT c.card_number,
                   c.user_id,
                   c.card_status,
                   c.issue_date,
                   c.cancel_date,
                   u.name AS user_name,
                   u.balance AS balance
            FROM card c
            LEFT JOIN `user` u ON c.user_id = u.user_id
            ORDER BY c.issue_date DESC
            """)
    List<Card> findAll();

    // 根据卡号查询卡片
    @Select("""
            SELECT c.card_number,
                   c.user_id,
                   c.card_status,
                   c.issue_date,
                   c.cancel_date,
                   u.name AS user_name,
                   u.balance AS balance
            FROM card c
            LEFT JOIN `user` u ON c.user_id = u.user_id
            WHERE c.card_number = #{cardNumber}
            """)
    Card findByCardNumber(String cardNumber);

    // 按用户查询卡片
    @Select("""
            SELECT c.card_number,
                   c.user_id,
                   c.card_status,
                   c.issue_date,
                   c.cancel_date,
                   u.name AS user_name,
                   u.balance AS balance
            FROM card c
            LEFT JOIN `user` u ON c.user_id = u.user_id
            WHERE c.user_id = #{userId}
            ORDER BY c.issue_date DESC
            """)
    List<Card> findByUserId(Long userId);

    // 查询用户是否存在
    @Select("SELECT COUNT(*) FROM `user` WHERE user_id = #{userId}")
    int countUserById(Long userId);

    // 发卡
    @Insert("""
            INSERT INTO card(card_number, user_id, card_status, issue_date)
            VALUES(#{cardNumber}, #{userId}, 1, NOW())
            """)
    int insert(Card card);

    // 修改卡片状态
    @Update("UPDATE card SET card_status = #{cardStatus} WHERE card_number = #{cardNumber}")
    int updateStatus(@Param("cardNumber") String cardNumber,
                     @Param("cardStatus") Integer cardStatus);

    // 注销卡片
    @Update("""
            UPDATE card
            SET card_status = 3,
                cancel_date = NOW()
            WHERE card_number = #{cardNumber}
            """)
    int cancelCard(String cardNumber);

    // 注销时清空该用户余额
    @Update("UPDATE `user` SET balance = 0 WHERE user_id = #{userId}")
    int clearUserBalance(Long userId);

    // 查询某用户是否已经存在正常卡
    @Select("""
            SELECT COUNT(*)
            FROM card
            WHERE user_id = #{userId}
              AND card_status = 1
            """)
    int countNormalCardByUserId(Long userId);
}