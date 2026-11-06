package com.dm.platform.controller;

import com.dm.platform.common.BusinessException;
import com.dm.platform.common.Result;
import com.dm.platform.dto.UserView;
import com.dm.platform.entity.Manuscript;
import com.dm.platform.entity.OperationLog;
import com.dm.platform.service.AuthService;
import com.dm.platform.service.ManuscriptService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 稿件接口(作者视角): 投稿 / 我的稿件 / 详情与流转记录
 * 步骤 2 会在此基础上增加管理员初审、分配审稿人等接口
 */
@RestController
@RequestMapping("/api/manuscripts")
public class ManuscriptController {

    private final ManuscriptService manuscriptService;

    public ManuscriptController(ManuscriptService manuscriptService) {
        this.manuscriptService = manuscriptService;
    }

    /**
     * 投稿: multipart/form-data
     * 字段: title / category / description / file(作品文件)
     */
    @PostMapping
    public Result<Manuscript> submit(HttpSession session,
                                     @RequestParam("title") String title,
                                     @RequestParam("category") String category,
                                     @RequestParam(value = "description", required = false) String description,
                                     @RequestParam("file") MultipartFile file) {
        UserView author = loginUser(session);
        return Result.ok(manuscriptService.submit(author, title, category, description, file));
    }

    /** 我的稿件列表(只能看到自己的) */
    @GetMapping("/mine")
    public Result<List<Manuscript>> mine(HttpSession session) {
        UserView author = loginUser(session);
        return Result.ok(manuscriptService.myManuscripts(author));
    }

    /** 稿件详情(按角色鉴权: 管理员全部 / 作者仅自己的 / 审稿人仅被分配的且不暴露作者) */
    @GetMapping("/{id}")
    public Result<Manuscript> detail(HttpSession session, @PathVariable Long id) {
        UserView user = loginUser(session);
        return Result.ok(manuscriptService.viewDetail(user, id));
    }

    /** 稿件流转记录(操作日志), 鉴权规则同详情 */
    @GetMapping("/{id}/logs")
    public Result<List<OperationLog>> logs(HttpSession session, @PathVariable Long id) {
        UserView user = loginUser(session);
        manuscriptService.viewDetail(user, id); // 复用权限校验
        return Result.ok(manuscriptService.logs(id));
    }

    /**
     * 作者修改重投(稿件被"退修改"后):
     * multipart/form-data, file 与 description 均可选, 至少更新一项
     */
    @PostMapping("/{id}/revisions")
    public Result<Manuscript> revise(HttpSession session, @PathVariable Long id,
                                     @RequestParam(value = "description", required = false) String description,
                                     @RequestParam(value = "file", required = false) MultipartFile file) {
        UserView author = loginUser(session);
        return Result.ok(manuscriptService.revise(author, id, description, file));
    }

    private UserView loginUser(HttpSession session) {
        UserView user = (UserView) session.getAttribute(AuthService.SESSION_KEY);
        if (user == null) {
            throw new BusinessException("请先登录");
        }
        return user;
    }
}
