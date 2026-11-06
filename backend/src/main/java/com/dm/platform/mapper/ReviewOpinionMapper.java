package com.dm.platform.mapper;

import com.dm.platform.dto.OpinionView;
import com.dm.platform.entity.ReviewOpinion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审稿意见表数据访问
 */
@Mapper
public interface ReviewOpinionMapper {

    @Insert("INSERT INTO review_opinion(assignment_id, score, comment, recommendation) " +
            "VALUES(#{assignmentId}, #{score}, #{comment}, #{recommendation})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReviewOpinion o);

    /** 管理员终审页用: 意见 + 审稿人昵称联查 */
    @Select("SELECT o.id, u.nickname AS reviewerNickname, o.score, o.comment, o.recommendation, o.create_time " +
            "FROM review_opinion o " +
            "JOIN review_assignment a ON a.id = o.assignment_id " +
            "JOIN user u ON u.id = a.reviewer_id " +
            "WHERE a.manuscript_id = #{manuscriptId} ORDER BY o.create_time ASC")
    List<OpinionView> viewByManuscript(Long manuscriptId);

    @Select("SELECT * FROM review_opinion WHERE assignment_id = #{assignmentId}")
    ReviewOpinion findByAssignmentId(Long assignmentId);

    /** 修改重投时清除上一轮意见(历史仍保留在 operation_log 留痕里) */
    @Delete("DELETE o FROM review_opinion o " +
            "JOIN review_assignment a ON a.id = o.assignment_id " +
            "WHERE a.manuscript_id = #{manuscriptId}")
    int deleteByManuscript(Long manuscriptId);
}
