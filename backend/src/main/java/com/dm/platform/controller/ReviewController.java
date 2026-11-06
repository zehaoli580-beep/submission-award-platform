package com.dm.platform.controller;

import com.dm.platform.common.Auths;
import com.dm.platform.common.Result;
import com.dm.platform.dto.ReviewTaskView;
import com.dm.platform.dto.SubmitOpinionRequest;
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
 * 审稿人接口: 我的评审任务 / 提交审稿意见
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** 我的评审任务列表 */
    @GetMapping("/mine")
    public Result<List<ReviewTaskView>> mine(HttpSession session) {
        var reviewer = Auths.requireRole(session, "REVIEWER");
        return Result.ok(reviewService.myTasks(reviewer));
    }

    /** 提交审稿意见(一人一票, 提交后不可修改) */
    @PostMapping("/{assignmentId}/opinion")
    public Result<Void> submitOpinion(HttpSession session, @PathVariable Long assignmentId,
                                      @Valid @RequestBody SubmitOpinionRequest req) {
        var reviewer = Auths.requireRole(session, "REVIEWER");
        reviewService.submitOpinion(reviewer, assignmentId, req);
        return Result.ok();
    }
}
