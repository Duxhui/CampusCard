package com.example.campus_card_backend.mapper;

import com.example.campus_card_backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // 精确查询：按字段名和值查询（字段名只能是 id/name/id_number/phone）
    @Select("SELECT * FROM `user` WHERE ${field} = #{value}")
    List<User> selectByField(@Param("field") String field, @Param("value") Object value);

    // 按姓名模糊查询
    @Select("SELECT * FROM `user` WHERE name LIKE CONCAT('%', #{value}, '%')")
    List<User> selectByNameLike(@Param("value") String value);

    // 按用户类型查询
    @Select("SELECT * FROM `user` WHERE user_type = #{userType}")
    List<User> selectByType(@Param("userType") Integer userType);

    // 查询全部
    @Select("SELECT * FROM `user`")
    List<User> selectAll();

    // 新增用户（返回自增主键）
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    @Insert("INSERT INTO `user` (name, id_number, phone, email, password, user_type) " +
            "VALUES (#{name}, #{idNumber}, #{phone}, #{email}, #{password}, #{userType})")
    int insert(User user);

    // 更新用户
    @Update("UPDATE `user` SET name=#{name}, id_number=#{idNumber}, phone=#{phone}, " +
            "email=#{email}, user_type=#{userType} WHERE user_id=#{userId}")
    int update(User user);

    // 删除用户
    @Delete("DELETE FROM `user` WHERE user_id=#{userId}")
    int deleteById(@Param("userId") Long userId);
}