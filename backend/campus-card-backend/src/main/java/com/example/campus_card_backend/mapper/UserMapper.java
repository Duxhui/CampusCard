package com.example.campus_card_backend.mapper;

import com.example.campus_card_backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    // 按 ID 查单个
    @Select("SELECT * FROM `user` WHERE user_id = #{userId}")
    User selectById(Long userId);

    // 查全部（后面前端列表用）
    @Select("SELECT * FROM `user`")
    List<User> selectAll();
}