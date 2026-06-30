package com.example.campus_card_backend.mapper;

import com.example.campus_card_backend.entity.User;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {

    // 按 ID 查单个
    @Select("SELECT * FROM `user` WHERE user_id = #{userId}")
    User selectById(Long userId);

    // 查全部（后面前端列表用）
    @Select("SELECT * FROM `user`")
    List<User> selectAll();

    // 插入用户，返回影响行数
    @Insert("INSERT INTO `user` (name, id_number, phone, email, password, user_type) " +
            "VALUES (#{name}, #{idNumber}, #{phone}, #{email}, #{password}, #{userType})")
    int insert(User user);


    // 插入后获取自增主键
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    @Insert("INSERT INTO `user` (name, id_number, phone, email, password, user_type) " +
            "VALUES (#{name}, #{idNumber}, #{phone}, #{email}, #{password}, #{userType})")
    int insertWithId(User user);

    // 模糊搜索：按姓名、身份证号、手机号搜索
    @Select("SELECT * FROM `user` WHERE name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR id_number LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%')")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    // 按用户类型筛选
    @Select("SELECT * FROM `user` WHERE user_type = #{userType}")
    List<User> selectByType(@Param("userType") Integer userType);

    // 更新用户
    @Update("UPDATE `user` SET name=#{name}, id_number=#{idNumber}, phone=#{phone}, " +
            "email=#{email}, user_type=#{userType} WHERE user_id=#{userId}")
    int update(User user);

    // 删除用户
    @Delete("DELETE FROM `user` WHERE user_id=#{userId}")
    int deleteById(@Param("userId") Long userId);
}