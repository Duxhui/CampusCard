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

    // 查全部
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

    // 插入用户时指定 user_id（用于回收 ID）
    @Insert("INSERT INTO `user` (user_id, name, id_number, phone, email, password, user_type) " +
            "VALUES (#{userId}, #{name}, #{idNumber}, #{phone}, #{email}, #{password}, #{userType})")
    int insertWithIdSpecified(User user);

    // 精确查询：按字段名和值查询（字段名只能是 id/name/id_number/phone）
    @Select("SELECT * FROM `user` WHERE ${field} = #{value}")
    List<User> selectByField(@Param("field") String field, @Param("value") Object value);

    // 按姓名模糊查询
    @Select("SELECT * FROM `user` WHERE name LIKE CONCAT('%', #{value}, '%')")
    List<User> selectByNameLike(@Param("value") String value);

    // 按用户类型筛选
    @Select("SELECT * FROM `user` WHERE user_type = #{userType}")
    List<User> selectByType(@Param("userType") Integer userType);

    // 更新用户（包含密码，供我的信息页修改用）
    @Update("UPDATE `user` SET name=#{name}, id_number=#{idNumber}, phone=#{phone}, " +
            "email=#{email}, password=#{password}, user_type=#{userType} WHERE user_id=#{userId}")
    int update(User user);

    // 删除用户
    @Delete("DELETE FROM `user` WHERE user_id=#{userId}")
    int deleteById(@Param("userId") Long userId);

    // 根据手机号和密码查询用户，用于登录
    @Select("""
        SELECT user_id, name, id_number, phone, email, password, user_type, balance, register_time
        FROM `user`
        WHERE phone = #{phone}
          AND password = #{password}
        """)
    User login(@Param("phone") String phone, @Param("password") String password);
}