package com.dm.platform.mapper;

import com.dm.platform.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户表数据访问, 注解式 SQL(课程作业规模足够, 不用 XML)
 */
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT COUNT(*) FROM user")
    long count();

    /** 仪表盘: 按角色统计用户数 */
    @Select("SELECT COUNT(*) FROM user WHERE role = #{role}")
    long countByRole(String role);

    /** 审稿人列表(管理员分配弹窗用), 直接映射到脱敏视图(不含密码) */
    @Select("SELECT id, username, role, nickname FROM user WHERE role = 'REVIEWER' ORDER BY id")
    List<com.dm.platform.dto.UserView> findReviewers();

    @Insert("INSERT INTO user(username, password, role, nickname) " +
            "VALUES(#{username}, #{password}, #{role}, #{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
