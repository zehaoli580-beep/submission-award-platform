package com.dm.platform.mapper;

import com.dm.platform.entity.ReviewAssignment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 评审分配表数据访问
 */
@Mapper
public interface ReviewAssignmentMapper {

    @Insert("INSERT INTO review_assignment(manuscript_id, reviewer_id, status) " +
            "VALUES(#{manuscriptId}, #{reviewerId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReviewAssignment a);

    @Select("SELECT * FROM review_assignment WHERE id = #{id}")
    ReviewAssignment findById(Long id);

    @Select("SELECT * FROM review_assignment WHERE manuscript_id = #{manuscriptId}")
    List<ReviewAssignment> findByManuscriptId(Long manuscriptId);

    @Select("SELECT * FROM review_assignment WHERE reviewer_id = #{reviewerId} ORDER BY assign_time DESC")
    List<ReviewAssignment> findByReviewer(Long reviewerId);

    /** 该稿件是否还有未完成(待审)的分配 */
    @Select("SELECT COUNT(*) FROM review_assignment WHERE manuscript_id = #{manuscriptId} AND status = 'ASSIGNED'")
    int countUnfinished(Long manuscriptId);

    @Select("SELECT COUNT(*) FROM review_assignment WHERE manuscript_id = #{manuscriptId}")
    int countByManuscript(Long manuscriptId);

    /** 某稿件是否已分配给某审稿人(防重复分配) */
    @Select("SELECT COUNT(*) FROM review_assignment WHERE manuscript_id = #{manuscriptId} AND reviewer_id = #{reviewerId}")
    int existsAssigned(Long manuscriptId, Long reviewerId);

    /** 修改重投时清除上一轮分配, 让稿件重新走完整外审 */
    @Delete("DELETE FROM review_assignment WHERE manuscript_id = #{manuscriptId}")
    int deleteByManuscript(Long manuscriptId);

    @Update("UPDATE review_assignment SET status = 'DONE' WHERE id = #{id}")
    int markDone(Long id);
}
