package com.dm.platform.controller;

import com.dm.platform.common.Auths;
import com.dm.platform.common.Result;
import com.dm.platform.dto.AdminRequests;
import com.dm.platform.dto.AdminStats;
import com.dm.platform.dto.OpinionView;
import com.dm.platform.dto.UserView;
import com.dm.platform.entity.Manuscript;
import com.dm.platform.mapper.AwardMapper;
import com.dm.platform.mapper.ManuscriptMapper;
import com.dm.platform.mapper.UserMapper;
import com.dm.platform.service.ManuscriptService;
import com.dm.platform.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员接口: 稿件池 / 初审 / 分配审稿人 / 终审
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ManuscriptService manuscriptService;
    private final ReviewService reviewService;
    private final UserMapper userMapper;
    private final ManuscriptMapper manuscriptMapper;
    private final AwardMapper awardMapper;

    public AdminController(ManuscriptService manuscriptService, ReviewService reviewService,
                           UserMapper userMapper, ManuscriptMapper manuscriptMapper, AwardMapper awardMapper) {
        this.manuscriptService = manuscriptService;
        this.reviewService = reviewService;
        this.userMapper = userMapper;
        this.manuscriptMapper = manuscriptMapper;
        this.awardMapper = awardMapper;
    }

    /** 首页仪表盘统计数据 */
    @GetMapping("/stats")
    public Result<AdminStats> stats(HttpSession session) {
        Auths.requireRole(session, "ADMIN");
        AdminStats s = new AdminStats();
        s.setPendingCount(manuscriptMapper.countByStatus("PENDING_INITIAL"));
        s.setReviewingCount(manuscriptMapper.countByStatus("UNDER_REVIEW"));
        s.setReviseCount(manuscriptMapper.countByStatus("REVISE"));
        s.setAcceptedCount(manuscriptMapper.countByStatus("ACCEPTED"));
        s.setRejectedCount(manuscriptMapper.countByStatus("REJECTED"));
        s.setAuthorCount(userMapper.countByRole("AUTHOR"));
        s.setReviewerCount(userMapper.countByRole("REVIEWER"));
        s.setAwardCount(awardMapper.count());
        s.setPublishedAwardCount(awardMapper.countByStatus("PUBLISHED"));
        return Result.ok(s);
    }

    /** 审稿人列表(分配审稿人弹窗的数据源) */
    @GetMapping("/reviewers")
    public Result<List<UserView>> reviewers(HttpSession session) {
        Auths.requireRole(session, "ADMIN");
        return Result.ok(userMapper.findReviewers());
    }

    /** 待初审稿件池 */
    @GetMapping("/manuscripts/pending")
    public Result<List<Manuscript>> pending(HttpSession session) {
        Auths.requireRole(session, "ADMIN");
        return Result.ok(manuscriptService.listByStatus("PENDING_INITIAL"));
    }

    /** 外审中稿件池(终审工作台) */
    @GetMapping("/manuscripts/reviewing")
    public Result<List<Manuscript>> reviewing(HttpSession session) {
        Auths.requireRole(session, "ADMIN");
        return Result.ok(manuscriptService.listByStatus("UNDER_REVIEW"));
    }

    /** 已录用稿件池(评奖提名的数据源) */
    @GetMapping("/manuscripts/accepted")
    public Result<List<Manuscript>> accepted(HttpSession session) {
        Auths.requireRole(session, "ADMIN");
        return Result.ok(manuscriptService.listByStatus("ACCEPTED"));
    }

    /** 初审通过并分配审稿人(一次操作完成两个动作, 单一事务) */
    @PostMapping("/manuscripts/{id}/pass-initial")
    public Result<Void> passInitial(HttpSession session, @PathVariable Long id,
                                    @Valid @RequestBody AdminRequests.AssignRequest req) {
        var admin = Auths.requireRole(session, "ADMIN");
        reviewService.passInitialAndAssign(admin, id, req.getReviewerIds());
        return Result.ok();
    }

    /** 初审直接退稿(不合规范) */
    @PostMapping("/manuscripts/{id}/reject-initial")
    public Result<Void> rejectInitial(HttpSession session, @PathVariable Long id) {
        var admin = Auths.requireRole(session, "ADMIN");
        manuscriptService.transition(admin, id, "REJECTED", "初审退稿", "格式或内容不合规范");
        return Result.ok();
    }

    /** 给外审中的稿件追加审稿人 */
    @PostMapping("/manuscripts/{id}/assign")
    public Result<Void> assign(HttpSession session, @PathVariable Long id,
                               @Valid @RequestBody AdminRequests.AssignRequest req) {
        var admin = Auths.requireRole(session, "ADMIN");
        reviewService.assign(admin, id, req.getReviewerIds());
        return Result.ok();
    }

    /** 查看某稿件收到的全部审稿意见(终审依据) */
    @GetMapping("/manuscripts/{id}/opinions")
    public Result<List<OpinionView>> opinions(HttpSession session, @PathVariable Long id) {
        Auths.requireRole(session, "ADMIN");
        return Result.ok(reviewService.opinions(id));
    }

    /** 终审决议: 录用 / 退修改 / 退稿 */
    @PostMapping("/manuscripts/{id}/final")
    public Result<Void> finalReview(HttpSession session, @PathVariable Long id,
                                    @Valid @RequestBody AdminRequests.FinalRequest req) {
        var admin = Auths.requireRole(session, "ADMIN");
        reviewService.finalReview(admin, id, req.getDecision());
        return Result.ok();
    }
}
