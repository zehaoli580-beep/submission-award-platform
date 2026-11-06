package com.dm.platform.mapper;

import com.dm.platform.entity.Manuscript;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 稿件表数据访问
 */
@Mapper
public interface ManuscriptMapper {

    @Insert("INSERT INTO manuscript(author_id, title, category, description, file_name, file_url, file_size, status) " +
            "VALUES(#{authorId}, #{title}, #{category}, #{description}, #{fileName}, #{fileUrl}, #{fileSize}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Manuscript m);

    @Select("SELECT * FROM manuscript WHERE id = #{id}")
    Manuscript findById(Long id);

    @Select("SELECT * FROM manuscript WHERE author_id = #{authorId} ORDER BY submit_time DESC")
    List<Manuscript> findByAuthor(Long authorId);

    /** 管理员工作台: 按状态查稿件池 */
    @Select("SELECT * FROM manuscript WHERE status = #{status} ORDER BY submit_time ASC")
    List<Manuscript> findByStatus(String status);

    /** 仪表盘: 按状态统计稿件数 */
    @Select("SELECT COUNT(*) FROM manuscript WHERE status = #{status}")
    long countByStatus(String status);

    /** 作者修改重投: 更新作品文件信息 */
    @Update("UPDATE manuscript SET file_name = #{fileName}, file_url = #{fileUrl}, file_size = #{fileSize} " +
            "WHERE id = #{id}")
    int updateWorkFile(Long id, String fileName, String fileUrl, Long fileSize);

    /** 作者修改重投: 更新作品简介 */
    @Update("UPDATE manuscript SET description = #{description} WHERE id = #{id}")
    int updateDescription(Long id, String description);

    /**
     * 只按 id + 期望的当前状态更新, 影响行数=0 说明状态已被别人改过(乐观锁思路),
     * 并发下可防止基于旧状态的重复流转
     */
    @Update("UPDATE manuscript SET status = #{toStatus} WHERE id = #{id} AND status = #{fromStatus}")
    int updateStatusCas(Long id, String fromStatus, String toStatus);
}
