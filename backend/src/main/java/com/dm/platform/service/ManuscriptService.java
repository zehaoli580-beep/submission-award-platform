package com.dm.platform.service;

import com.dm.platform.common.BusinessException;
import com.dm.platform.common.ManuscriptStatus;
import com.dm.platform.dto.UserView;
import com.dm.platform.entity.Manuscript;
import com.dm.platform.entity.OperationLog;
import com.dm.platform.mapper.ManuscriptMapper;
import com.dm.platform.mapper.OperationLogMapper;
import com.dm.platform.mapper.ReviewAssignmentMapper;
import com.dm.platform.mapper.ReviewOpinionMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 稿件服务: 投稿(文件上传) / 我的稿件 / 状态机流转 + 留痕
 *
 * 文件存储设计(报告重点):
 *   - 文件本体保存在磁盘 upload/ 目录, 数据库只存 file_url 路径
 *   - 文件名改为 UUID 前缀, 防止重名覆盖 + 防路径穿越
 *   - 校验扩展名白名单; 大小上限由 multipart 配置兜底
 */
@Service
public class ManuscriptService {

    private final ManuscriptMapper manuscriptMapper;
    private final OperationLogMapper logMapper;
    private final ReviewAssignmentMapper assignmentMapper;
    private final ReviewOpinionMapper opinionMapper;
    private final Path uploadDir;
    private final List<String> allowedExtensions;

    public ManuscriptService(ManuscriptMapper manuscriptMapper,
                             OperationLogMapper logMapper,
                             ReviewAssignmentMapper assignmentMapper,
                             ReviewOpinionMapper opinionMapper,
                             @Value("${platform.upload-dir}") String uploadDir,
                             @Value("${platform.allowed-extensions}") String allowedExtensions) {
        this.manuscriptMapper = manuscriptMapper;
        this.logMapper = logMapper;
        this.assignmentMapper = assignmentMapper;
        this.opinionMapper = opinionMapper;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = Arrays.asList(allowedExtensions.toLowerCase().split(","));
    }

    /** 作者投稿: 表单元数据 + 作品文件 */
    public Manuscript submit(UserView author, String title, String category,
                            String description, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传作品文件");
        }
        String originalName = file.getOriginalFilename();
        String ext = extensionOf(originalName);
        if (!allowedExtensions.contains(ext)) {
            throw new BusinessException("不支持的文件类型: " + ext + ", 允许: " + allowedExtensions);
        }

        // 落盘: UUID + 扩展名, 避免中文文件名/重名问题
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(storedName).toFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败, 请重试");
        }

        Manuscript m = new Manuscript();
        m.setAuthorId(author.getId());
        m.setTitle(title);
        m.setCategory(category);
        m.setDescription(description);
        m.setFileName(originalName);
        m.setFileUrl("/files/" + storedName);
        m.setFileSize(file.getSize());
        m.setStatus(ManuscriptStatus.PENDING_INITIAL);
        m.setSubmitTime(LocalDateTime.now());
        manuscriptMapper.insert(m);

        insertLog(m.getId(), author.getId(), "投稿", null, ManuscriptStatus.PENDING_INITIAL,
                "《" + title + "》提交成功");
        return m;
    }

    /** 作者查自己的稿件列表 */
    public List<Manuscript> myManuscripts(UserView author) {
        return manuscriptMapper.findByAuthor(author.getId());
    }

    /**
     * 作者修改重投(状态机中 REVISE -> UNDER_REVIEW 的唯一入口):
     * 只有稿件归属作者本人且处于"退修改"状态才能操作, 可同时更新作品文件与简介.
     * 重投意味着重新走完整外审: 清除上一轮的分配与意见记录
     * (历史结论已永久留痕在 operation_log, 不丢失审计链), 管理员需重新分配审稿人.
     */
    @Transactional
    public Manuscript revise(UserView author, Long manuscriptId, String description, MultipartFile file) {
        Manuscript m = detail(manuscriptId);
        if (!m.getAuthorId().equals(author.getId())) {
            throw new BusinessException("只能修改自己的稿件");
        }
        if (file != null && !file.isEmpty()) {
            String ext = extensionOf(file.getOriginalFilename());
            if (!allowedExtensions.contains(ext)) {
                throw new BusinessException("不支持的文件类型: " + ext);
            }
            String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            try {
                Files.createDirectories(uploadDir);
                file.transferTo(uploadDir.resolve(storedName).toFile());
            } catch (IOException e) {
                throw new BusinessException("文件保存失败, 请重试");
            }
            manuscriptMapper.updateWorkFile(manuscriptId, file.getOriginalFilename(),
                    "/files/" + storedName, file.getSize());
        }
        if (description != null && !description.isBlank()) {
            manuscriptMapper.updateDescription(manuscriptId, description);
        }
        opinionMapper.deleteByManuscript(manuscriptId);   // 先删意见(联查分配表)
        assignmentMapper.deleteByManuscript(manuscriptId); // 再删分配记录
        transition(author, manuscriptId, ManuscriptStatus.UNDER_REVIEW, "修改重投", "作者根据意见修改后重新送审, 上轮评审分配已重置");
        return detail(manuscriptId);
    }

    /**
     * 稿件详情的权限查看(接口层用):
     * 管理员可看全部; 作者只能看自己的; 审稿人只能看分配给自己的(盲审: 返回副本抹去 author_id).
     */
    public Manuscript viewDetail(UserView viewer, Long id) {
        Manuscript m = detail(id);
        boolean isAuthor = m.getAuthorId().equals(viewer.getId());
        boolean isAssignedReviewer = "REVIEWER".equals(viewer.getRole())
                && assignmentMapper.existsAssigned(id, viewer.getId()) > 0;
        if (!"ADMIN".equals(viewer.getRole()) && !isAuthor && !isAssignedReviewer) {
            throw new BusinessException("无权查看该稿件");
        }
        if (isAssignedReviewer && !"AUTHOR".equals(viewer.getRole())) {
            // 盲审: 不向审稿人暴露作者身份
            Manuscript blind = new Manuscript();
            blind.setId(m.getId());
            blind.setTitle(m.getTitle());
            blind.setCategory(m.getCategory());
            blind.setDescription(m.getDescription());
            blind.setFileName(m.getFileName());
            blind.setFileUrl(m.getFileUrl());
            blind.setFileSize(m.getFileSize());
            blind.setStatus(m.getStatus());
            blind.setSubmitTime(m.getSubmitTime());
            return blind;
        }
        return m;
    }

    /** 按状态查稿件池(管理员) */
    public List<Manuscript> listByStatus(String status) {
        return manuscriptMapper.findByStatus(status);
    }

    public Manuscript detail(Long id) {
        Manuscript m = manuscriptMapper.findById(id);
        if (m == null) {
            throw new BusinessException("稿件不存在");
        }
        return m;
    }

    public List<OperationLog> logs(Long manuscriptId) {
        return logMapper.findByManuscriptId(manuscriptId);
    }

    /**
     * 状态机流转(全系统唯一的状态变更入口, 报告核心设计点):
     * 1. 查合法流转表, 非法流转直接抛异常 -> 作者无法把自己稿件改成"已录用"
     * 2. UPDATE ... WHERE status = from(乐观思路) 防并发下重复流转
     * 3. 无论成败都写 operation_log 留痕
     */
    public void transition(UserView operator, Long manuscriptId, String toStatus, String action, String detail) {
        Manuscript m = detail(manuscriptId);
        String from = m.getStatus();
        if (!ManuscriptStatus.canTransit(from, toStatus)) {
            throw new BusinessException("非法状态流转: " + cn(from) + " 不能变更为 " + cn(toStatus));
        }
        int rows = manuscriptMapper.updateStatusCas(manuscriptId, from, toStatus);
        if (rows == 0) {
            throw new BusinessException("状态已变更, 请刷新后重试");
        }
        insertLog(manuscriptId, operator.getId(), action, from, toStatus, detail);
    }

    /**
     * 非状态变更类动作的留痕入口(如"分配审稿人"不改状态, 只记日志)
     */
    public void note(UserView operator, Long manuscriptId, String action, String detail) {
        Manuscript m = detail(manuscriptId);
        insertLog(manuscriptId, operator.getId(), action, m.getStatus(), m.getStatus(), detail);
    }

    private void insertLog(Long manuscriptId, Long operatorId, String action,
                           String from, String to, String detail) {
        OperationLog log = new OperationLog();
        log.setManuscriptId(manuscriptId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setDetail(detail);
        logMapper.insert(log);
    }

    private String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException("文件名缺少扩展名");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String cn(String status) {
        return ManuscriptStatus.CN_NAMES.getOrDefault(status, status);
    }
}
