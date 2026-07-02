package com.example.campus_card_backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * recycled_user_id 表用于回收被删除的用户ID。
 */
@Mapper
public interface RecycledUserIdMapper {

    /** 获取最小的可回收 ID */
    @Select("SELECT MIN(user_id) FROM recycled_user_id")
    Long selectRecycledId();

    /** 删除已使用的回收 ID */
    @Delete("DELETE FROM recycled_user_id WHERE user_id = #{userId}")
    int deleteRecycledId(Long userId);

    /** 删除用户时存入回收 ID */
    @Insert("INSERT INTO recycled_user_id VALUES (#{userId})")
    int insertRecycledId(Long userId);
}
