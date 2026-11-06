package com.dm.platform.mapper;

import com.dm.platform.entity.NominationScore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 提名打分表数据访问
 * 唯一键(nomination_id, judge_id) + ON DUPLICATE KEY UPDATE 实现打分的"保存即更新"
 */
@Mapper
public interface NominationScoreMapper {

    @Insert("INSERT INTO nomination_score(nomination_id, judge_id, score) " +
            "VALUES(#{nominationId}, #{judgeId}, #{score}) " +
            "ON DUPLICATE KEY UPDATE score = #{score}")
    int upsert(NominationScore s);

    @Select("SELECT * FROM nomination_score WHERE nomination_id = #{nominationId}")
    List<NominationScore> findByNomination(Long nominationId);

    @Select("SELECT score FROM nomination_score WHERE nomination_id = #{nominationId} AND judge_id = #{judgeId}")
    Integer findMyScore(Long nominationId, Long judgeId);
}
