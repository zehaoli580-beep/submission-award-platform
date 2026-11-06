package com.dm.platform.mapper;

import com.dm.platform.dto.NominationView;
import com.dm.platform.entity.Nomination;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 提名记录表数据访问
 */
@Mapper
public interface NominationMapper {

    @Insert("INSERT INTO nomination(award_id, manuscript_id, result) " +
            "VALUES(#{awardId}, #{manuscriptId}, #{result})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Nomination n);

    @Select("SELECT * FROM nomination WHERE id = #{id}")
    Nomination findById(Long id);

    @Select("SELECT COUNT(*) FROM nomination WHERE award_id = #{awardId} AND manuscript_id = #{manuscriptId}")
    int existsPair(Long awardId, Long manuscriptId);

    @Select("SELECT n.id, n.manuscript_id AS manuscriptId, m.title AS manuscriptTitle, " +
            "m.category, u.nickname AS authorNickname, n.final_score AS finalScore, n.result, " +
            "m.file_name AS fileName, m.file_url AS fileUrl, m.file_size AS fileSize " +
            "FROM nomination n " +
            "JOIN manuscript m ON m.id = n.manuscript_id " +
            "JOIN user u ON u.id = m.author_id " +
            "WHERE n.award_id = #{awardId} " +
            "ORDER BY n.final_score DESC, n.id ASC")
    List<NominationView> viewByAward(Long awardId);

    @Update("UPDATE nomination SET final_score = #{finalScore} WHERE id = #{id}")
    int updateFinalScore(Long id, java.math.BigDecimal finalScore);

    @Update("UPDATE nomination SET result = #{result} WHERE id = #{id}")
    int updateResult(Long id, String result);
}
