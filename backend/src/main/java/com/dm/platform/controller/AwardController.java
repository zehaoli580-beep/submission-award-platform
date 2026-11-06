package com.dm.platform.controller;

import com.dm.platform.common.Auths;
import com.dm.platform.common.Result;
import com.dm.platform.dto.CreateAwardRequest;
import com.dm.platform.dto.NominationView;
import com.dm.platform.dto.ScoreNominationRequest;
import com.dm.platform.entity.Award;
import com.dm.platform.entity.Nomination;
import com.dm.platform.service.AwardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 评奖接口: 奖项批次 / 提名 / 打分 / 公示
 * 权限: 创建/开启/提名/公示仅管理员; 打分仅审稿人; 列表和公示结果所有人可见
 */
@RestController
@RequestMapping("/api/awards")
public class AwardController {

    private final AwardService awardService;

    public AwardController(AwardService awardService) {
        this.awardService = awardService;
    }

    /** 奖项列表(所有人可见) */
    @GetMapping
    public Result<List<Award>> list(HttpSession session) {
        Auths.user(session);
        return Result.ok(awardService.all());
    }

    /** 创建奖项批次(管理员) */
    @PostMapping
    public Result<Award> create(HttpSession session, @Valid @RequestBody CreateAwardRequest req) {
        var admin = Auths.requireRole(session, "ADMIN");
        return Result.ok(awardService.create(admin, req.getName(), req.getDescription()));
    }

    /** 开启评审(管理员): 筹备中 -> 评审中 */
    @PostMapping("/{id}/start")
    public Result<Award> start(HttpSession session, @PathVariable Long id) {
        var admin = Auths.requireRole(session, "ADMIN");
        return Result.ok(awardService.startReview(admin, id));
    }

    /** 提名录用稿件(管理员) */
    @PostMapping("/{id}/nominations")
    public Result<Nomination> nominate(HttpSession session, @PathVariable Long id,
                                       @RequestBody Map<String, Long> body) {
        var admin = Auths.requireRole(session, "ADMIN");
        Long manuscriptId = body.get("manuscriptId");
        if (manuscriptId == null) {
            return Result.error("缺少 manuscriptId");
        }
        return Result.ok(awardService.nominate(admin, id, manuscriptId));
    }

    /** 提名列表: 公示前仅管理员/审稿人可见, 公示后所有人可见 */
    @GetMapping("/{id}/nominations")
    public Result<List<NominationView>> nominations(HttpSession session, @PathVariable Long id) {
        var user = Auths.user(session);
        return Result.ok(awardService.nominations(user, id));
    }

    /** 评委查自己对该奖项各提名的打分(前端回显) */
    @GetMapping("/{id}/myscores")
    public Result<Map<Long, Integer>> myScores(HttpSession session, @PathVariable Long id) {
        var judge = Auths.requireRole(session, "REVIEWER");
        return Result.ok(awardService.myScores(judge, id));
    }

    /** 评委打分(重复提交即覆盖更新) */
    @PostMapping("/{id}/nominations/{nominationId}/score")
    public Result<Void> score(HttpSession session, @PathVariable Long id,
                              @PathVariable Long nominationId,
                              @Valid @RequestBody ScoreNominationRequest req) {
        var judge = Auths.requireRole(session, "REVIEWER");
        awardService.score(judge, id, nominationId, req.getScore());
        return Result.ok();
    }

    /** 公示(管理员): 计算终评分(去最高最低取均值)并排名定奖 */
    @PostMapping("/{id}/publish")
    public Result<Award> publish(HttpSession session, @PathVariable Long id,
                                 @RequestParam(defaultValue = "1") int winnerCount) {
        var admin = Auths.requireRole(session, "ADMIN");
        return Result.ok(awardService.publish(admin, id, winnerCount));
    }
}
