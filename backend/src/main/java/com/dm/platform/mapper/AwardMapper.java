package com.dm.platform.mapper;

import com.dm.platform.entity.Award;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 奖项批次表数据访问
 */
@Mapper
public interface AwardMapper {

    @Insert("INSERT INTO award(name, description, status) VALUES(#{name}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Award a);

    @Select("SELECT * FROM award WHERE id = #{id}")
    Award findById(Long id);

    @Select("SELECT * FROM award ORDER BY create_time DESC")
    List<Award> findAll();

    @Update("UPDATE award SET status = #{status} WHERE id = #{id}")
    int updateStatus(Long id, String status);

    @Update("UPDATE award SET status = 'PUBLISHED', publish_time = NOW() WHERE id = #{id}")
    int markPublished(Long id);

    /** 仪表盘: 全部奖项数 */
    @Select("SELECT COUNT(*) FROM award")
    long count();

    /** 仪表盘: 按状态统计奖项数 */
    @Select("SELECT COUNT(*) FROM award WHERE status = #{status}")
    long countByStatus(String status);
}
