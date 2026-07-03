package com.example.campus_card_backend.mapper;

import com.example.campus_card_backend.entity.Merchant;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MerchantMapper {

    // 查询所有商户
    @Select("SELECT merchant_id, merchant_name, merchant_type, location, business_status FROM merchant")
    List<Merchant> findAll();

    // 根据商户编号查询
    @Select("SELECT merchant_id, merchant_name, merchant_type, location, business_status FROM merchant WHERE merchant_id = #{merchantId}")
    Merchant findById(String merchantId);

    // 按类型和营业状态筛选
    @Select("""
            SELECT merchant_id, merchant_name, merchant_type, location, business_status
            FROM merchant
            WHERE (#{merchantType} IS NULL OR #{merchantType} = '' OR merchant_type = #{merchantType})
              AND (#{businessStatus} IS NULL OR business_status = #{businessStatus})
            """)
    List<Merchant> search(@Param("merchantType") String merchantType,
                          @Param("businessStatus") Integer businessStatus);

    // 新增商户
    @Insert("""
            INSERT INTO merchant(merchant_id, merchant_name, merchant_type, location, business_status)
            VALUES(#{merchantId}, #{merchantName}, #{merchantType}, #{location}, #{businessStatus})
            """)
    int insert(Merchant merchant);

    // 修改商户
    @Update("""
            UPDATE merchant
            SET merchant_name = #{merchantName},
                merchant_type = #{merchantType},
                location = #{location},
                business_status = #{businessStatus}
            WHERE merchant_id = #{merchantId}
            """)
    int update(Merchant merchant);

    // 启停商户
    @Update("UPDATE merchant SET business_status = #{businessStatus} WHERE merchant_id = #{merchantId}")
    int updateStatus(@Param("merchantId") String merchantId,
                     @Param("businessStatus") Integer businessStatus);

    // 删除商户
    @Delete("DELETE FROM merchant WHERE merchant_id = #{merchantId}")
    int delete(String merchantId);

    // 检查商户是否存在历史消费记录
    @Select("SELECT COUNT(*) FROM consumption_record WHERE merchant_id = #{merchantId}")
    int countConsumptionByMerchantId(String merchantId);

    // 查找 M 格式的最大商户编号
    @Select("SELECT MAX(merchant_id) FROM merchant WHERE merchant_id LIKE 'M%'")
    String findMaxMerchantId();
}