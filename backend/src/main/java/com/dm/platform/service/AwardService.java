package com.dm.platform.service;

import com.dm.platform.common.BusinessException;
import com.dm.platform.dto.NominationView;
import com.dm.platform.dto.UserView;
import com.dm.platform.entity.Award;
import com.dm.platform.entity.Manuscript;
import com.dm.platform.entity.Nomination;
import com.dm.platform.entity.NominationScore;
import com.dm.platform.mapper.AwardMapper;
import com.dm.platform.mapper.ManuscriptMapper;
import com.dm.platform.mapper.NominationMapper;
import com.dm.platform.mapper.NominationScoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * 评奖服务: 奖项批次 / 提名 / 评委打分 / 公示
 *
 * 流程: 创建奖项(PREPARING) -> 开启评审(REVIEWING) -> 提名录用稿件
 *       -> 评委打分(可反复修改, 保存即更新) -> 公示(计算终评分并排名)
 */
@Service
public class AwardService {

    private final AwardMapper awardMapper;
    private final NominationMapper nominationMapper;
    private final NominationScoreMapper scoreMapper;
    private final ManuscriptMapper manuscriptMapper;
    private final ManuscriptService manuscriptService;

    public AwardService(AwardMapper awardMapper,
                        NominationMapper nominationMapper,
                        NominationScoreMapper scoreMapper,
                        ManuscriptMapper manuscriptMapper,
                        ManuscriptService manuscriptService) {
        this.awardMapper = awardMapper;
        this.nominationMapper = nominationMapper;
        this.scoreMapper = scoreMapper;
        this.manuscriptMapper = manuscriptMapper;
        this.manuscriptService = manuscriptService;
    }

    /** 创建奖项批次 */
    public Award create(UserView admin, String name, String description) {
        Award a = new Award();
        a.setName(name);
        a.setDescription(description);
        a.setStatus("PREPARING");
        awardMapper.insert(a);
        return a;
    }

    /** 开启评审: PREPARING -> REVIEWING, 之后才能提名/打分 */
    public Award startReview(UserView admin, Long awardId) {
        Award a = get(awardId);
        if (!"PREPARING".equals(a.getStatus())) {
            throw new BusinessException("只有\"筹备中\"的奖项才能开启评审");
        }
        awardMapper.updateStatus(awardId, "REVIEWING");
        return get(awardId);
    }

    /** 提名: 只能提名"已录用"的稿件, 且一个奖项里同一稿件只提名一次 */
    public Nomination nominate(UserView admin, Long awardId, Long manuscriptId) {
        Award a = get(awardId);
        if (!"REVIEWING".equals(a.getStatus())) {
            throw new BusinessException("奖项不在评审中, 不能提名");
        }
        Manuscript m = manuscriptService.detail(manuscriptId);
        if (!"ACCEPTED".equals(m.getStatus())) {
            throw new BusinessException("只能提名\"已录用\"的稿件");
        }
        if (nominationMapper.existsPair(awardId, manuscriptId) > 0) {
            throw new BusinessException("该稿件已在此奖项中提名过");
        }
        Nomination n = new Nomination();
        n.setAwardId(awardId);
        n.setManuscriptId(manuscriptId);
        n.setResult("NOMINATED");
        nominationMapper.insert(n);
        return n;
    }

    /** 评委打分: 奖项须在评审中; 同一评委重复打分即覆盖更新 */
    public void score(UserView judge, Long awardId, Long nominationId, Integer score) {
        Award a = get(awardId);
        if (!"REVIEWING".equals(a.getStatus())) {
            throw new BusinessException("奖项不在评审中, 不能打分");
        }
        Nomination n = nominationMapper.findById(nominationId);
        if (n == null || !n.getAwardId().equals(awardId)) {
            throw new BusinessException("提名记录不存在");
        }
        NominationScore s = new NominationScore();
        s.setNominationId(nominationId);
        s.setJudgeId(judge.getId());
        s.setScore(score);
        scoreMapper.upsert(s);
    }

    /**
     * 公示并计算终评分(评奖规则的核心):
     *   - 每个提名: 评委数 >= 3 时去掉最高分和最低分后取均值, 否则直接取均值
     *   - 按终评分从高到低排名, 前 winnerCount 名 -> WON, 其余 -> LOST
     */
    @Transactional
    public Award publish(UserView admin, Long awardId, int winnerCount) {
        Award a = get(awardId);
        if (!"REVIEWING".equals(a.getStatus())) {
            throw new BusinessException("只有\"评审中\"的奖项才能公示");
        }
        List<NominationView> nominations = nominationMapper.viewByAward(awardId);
        if (nominations.isEmpty()) {
            throw new BusinessException("该奖项还没有提名作品, 不能公示");
        }
        for (NominationView v : nominations) {
            List<Integer> scores = scoreMapper.findByNomination(v.getId())
                    .stream().map(NominationScore::getScore).sorted().toList();
            if (scores.isEmpty()) {
                throw new BusinessException("作品《" + v.getManuscriptTitle() + "》还没有评委打分, 不能公示");
            }
            List<Integer> used = scores.size() >= 3
                    ? scores.subList(1, scores.size() - 1)  // 去掉最高分和最低分
                    : scores;
            BigDecimal avg = BigDecimal.valueOf(used.stream().mapToInt(Integer::intValue).average().orElse(0))
                    .setScale(2, RoundingMode.HALF_UP);
            nominationMapper.updateFinalScore(v.getId(), avg);
        }
        // 重新按终评分排名, 定获奖名单
        List<NominationView> ranked = nominationMapper.viewByAward(awardId).stream()
                .sorted(Comparator.comparing(NominationView::getFinalScore).reversed())
                .toList();
        int winners = Math.min(winnerCount, ranked.size());
        for (int i = 0; i < ranked.size(); i++) {
            nominationMapper.updateResult(ranked.get(i).getId(), i < winners ? "WON" : "LOST");
        }
        awardMapper.markPublished(awardId);
        return get(awardId);
    }

    /** 查看提名列表: 公示后所有人可见, 公示前仅管理员/审稿人可见 */
    public List<NominationView> nominations(UserView viewer, Long awardId) {
        Award a = get(awardId);
        boolean privileged = "ADMIN".equals(viewer.getRole()) || "REVIEWER".equals(viewer.getRole());
        if (!"PUBLISHED".equals(a.getStatus()) && !privileged) {
            throw new BusinessException("奖项尚未公示");
        }
        return nominationMapper.viewByAward(awardId);
    }

    /**
     * 评委查自己对某奖项各提名的打分(前端回显用):
     * 返回 { nominationId -> score }, 未打分的提名不在 map 里
     */
    public java.util.Map<Long, Integer> myScores(UserView judge, Long awardId) {
        java.util.Map<Long, Integer> map = new java.util.HashMap<>();
        for (NominationView v : nominationMapper.viewByAward(awardId)) {
            Integer s = scoreMapper.findMyScore(v.getId(), judge.getId());
            if (s != null) {
                map.put(v.getId(), s);
            }
        }
        return map;
    }

    public List<Award> all() {
        return awardMapper.findAll();
    }

    public Award get(Long id) {
        Award a = awardMapper.findById(id);
        if (a == null) {
            throw new BusinessException("奖项不存在");
        }
        return a;
    }
}
