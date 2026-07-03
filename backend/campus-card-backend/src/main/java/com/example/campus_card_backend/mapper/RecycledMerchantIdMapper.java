package com.example.campus_card_backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * recycled_merchant_id 表用于回收被删除的商户编号。
 */
@Mapper
public interface RecycledMerchantIdMapper {

    @Select("SELECT MIN(merchant_id) FROM recycled_merchant_id")
    String selectRecycledId();

    @Delete("DELETE FROM recycled_merchant_id WHERE merchant_id = #{merchantId}")
    int deleteRecycledId(String merchantId);

    @Insert("INSERT INTO recycled_merchant_id VALUES (#{merchantId})")
    int insertRecycledId(String merchantId);
}
