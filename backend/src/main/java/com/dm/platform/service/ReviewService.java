package com.dm.platform.service;

import com.dm.platform.common.BusinessException;
import com.dm.platform.common.ManuscriptStatus;
import com.dm.platform.dto.OpinionView;
import com.dm.platform.dto.ReviewTaskView;
import com.dm.platform.dto.SubmitOpinionRequest;
import com.dm.platform.dto.UserView;
import com.dm.platform.entity.Manuscript;
import com.dm.platform.entity.ReviewAssignment;
import com.dm.platform.entity.ReviewOpinion;
import com.dm.platform.entity.User;
import com.dm.platform.mapper.ManuscriptMapper;
import com.dm.platform.mapper.ReviewAssignmentMapper;
import com.dm.platform.mapper.ReviewOpinionMapper;
import com.dm.platform.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 评审服务: 分配审稿人 / 审稿人任务与意见 / 管理员终审
 */
@Service
public class ReviewService {

    private final ReviewAssignmentMapper assignmentMapper;
    private final ReviewOpinionMapper opinionMapper;
    private final ManuscriptMapper manuscriptMapper;
    private final UserMapper userMapper;
    private final ManuscriptService manuscriptService;

    public ReviewService(ReviewAssignmentMapper assignmentMapper,
                         ReviewOpinionMapper opinionMapper,
                         ManuscriptMapper manuscriptMapper,
                         UserMapper userMapper,
                         ManuscriptService manuscriptService) {
        this.assignmentMapper = assignmentMapper;
        this.opinionMapper = opinionMapper;
        this.manuscriptMapper = manuscriptMapper;
        this.userMapper = userMapper;
        this.manuscriptService = manuscriptService;
    }

    /**
     * 初审通过并分配审稿人(一次操作, 单一事务):
     * 状态 PENDING_INITIAL -> UNDER_REVIEW, 然后写入分配记录.
     * 事务保证: 分配失败(如选了非审稿人)时初审状态一并回滚, 不会出现"外审中却没人审"的脏状态.
     */
    @Transactional
    public void passInitialAndAssign(UserView admin, Long manuscriptId, List<Long> reviewerIds) {
        Manuscript m = manuscriptService.detail(manuscriptId);
        if (!ManuscriptStatus.PENDING_INITIAL.equals(m.getStatus())) {
            throw new BusinessException("只有\"待初审\"的稿件才能初审");
        }
        manuscriptService.transition(admin, manuscriptId, ManuscriptStatus.UNDER_REVIEW,
                "初审通过", "格式与内容规范核验通过");
        assign(admin, manuscriptId, reviewerIds);
    }

    /**
     * 管理员分配审稿人(初审通过时或之后追加), 稿件必须已进入外审中.
     * 分配属于"动作"而非"状态变更", 因此只写操作日志, 不走状态机流转.
     */
    @Transactional
    public int assign(UserView admin, Long manuscriptId, List<Long> reviewerIds) {
        Manuscript m = manuscriptService.detail(manuscriptId);
        if (!ManuscriptStatus.UNDER_REVIEW.equals(m.getStatus())) {
            throw new BusinessException("只有\"外审中\"的稿件才能分配审稿人");
        }
        int created = 0;
        for (Long reviewerId : reviewerIds.stream().distinct().toList()) {
            if (assignmentMapper.existsAssigned(manuscriptId, reviewerId) > 0) {
                throw new BusinessException("该稿件已分配过此审稿人, 不能重复分配");
            }
            User reviewer = userMapper.findById(reviewerId);
            if (reviewer == null || !"REVIEWER".equals(reviewer.getRole())) {
                throw new BusinessException("用户 " + reviewerId + " 不是审稿人");
            }
            ReviewAssignment a = new ReviewAssignment();
            a.setManuscriptId(manuscriptId);
            a.setReviewerId(reviewerId);
            a.setStatus("ASSIGNED");
            assignmentMapper.insert(a);
            created++;
        }
        manuscriptService.note(admin, manuscriptId, "分配审稿人",
                "本次分配 " + created + " 名审稿人: " + reviewerIds);
        return created;
    }

    /** 审稿人: 我的评审任务列表(联查稿件信息) */
    public List<ReviewTaskView> myTasks(UserView reviewer) {
        List<ReviewTaskView> views = new ArrayList<>();
        for (ReviewAssignment a : assignmentMapper.findByReviewer(reviewer.getId())) {
            Manuscript m = manuscriptMapper.findById(a.getManuscriptId());
            if (m == null) {
                continue; // 稿件被删除等异常情况, 跳过
            }
            ReviewTaskView v = new ReviewTaskView();
            v.setAssignmentId(a.getId());
            v.setManuscriptId(m.getId());
            v.setTitle(m.getTitle());
            v.setCategory(m.getCategory());
            v.setDescription(m.getDescription());
            v.setFileName(m.getFileName());
            v.setFileUrl(m.getFileUrl());
            v.setManuscriptStatus(m.getStatus());
            v.setAssignStatus(a.getStatus());
            v.setSubmitTime(m.getSubmitTime());
            views.add(v);
        }
        return views;
    }

    /**
     * 审稿人提交意见(一人一票):
     * 校验"分配给我 + 还没交过", 提交后分配记录标记 DONE
     */
    @Transactional
    public void submitOpinion(UserView reviewer, Long assignmentId, SubmitOpinionRequest req) {
        ReviewAssignment a = assignmentMapper.findById(assignmentId);
        if (a == null) {
            throw new BusinessException("评审任务不存在");
        }
        if (!a.getReviewerId().equals(reviewer.getId())) {
            throw new BusinessException("只能操作分配给自己的评审任务");
        }
        if (!"ASSIGNED".equals(a.getStatus())) {
            throw new BusinessException("该任务已提交过意见, 不能重复提交");
        }
        Manuscript m = manuscriptMapper.findById(a.getManuscriptId());
        if (m == null || !ManuscriptStatus.UNDER_REVIEW.equals(m.getStatus())) {
            throw new BusinessException("该稿件已结束本轮评审, 无需再提交意见");
        }
        ReviewOpinion o = new ReviewOpinion();
        o.setAssignmentId(assignmentId);
        o.setScore(req.getScore());
        o.setComment(req.getComment());
        o.setRecommendation(req.getRecommendation());
        opinionMapper.insert(o);
        assignmentMapper.markDone(assignmentId);
    }

    /** 管理员: 查某稿件收到的全部意见(终审依据) */
    public List<OpinionView> opinions(Long manuscriptId) {
        return opinionMapper.viewByManuscript(manuscriptId);
    }

    /**
     * 管理员终审: UNDER_REVIEW -> ACCEPTED / REVISE / REJECTED
     * 前提: 至少 1 条分配, 且所有分配都已完成(避免意见没齐就下结论)
     */
    @Transactional
    public void finalReview(UserView admin, Long manuscriptId, String decision) {
        Manuscript m = manuscriptService.detail(manuscriptId);
        if (!ManuscriptStatus.UNDER_REVIEW.equals(m.getStatus())) {
            throw new BusinessException("只有\"外审中\"的稿件才能终审");
        }
        if (assignmentMapper.countByManuscript(manuscriptId) == 0) {
            throw new BusinessException("该稿件尚未分配审稿人, 不能终审");
        }
        if (assignmentMapper.countUnfinished(manuscriptId) > 0) {
            throw new BusinessException("还有审稿人未提交意见, 不能终审");
        }
        String action = switch (decision) {
            case ManuscriptStatus.ACCEPTED -> "终审录用";
            case ManuscriptStatus.REVISE -> "终审退修改";
            default -> "终审退稿";
        };
        manuscriptService.transition(admin, manuscriptId, decision, action, "管理员依据外审意见作出终审决议");
    }
}
