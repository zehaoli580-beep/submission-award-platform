package com.dm.platform.dto;

import java.math.BigDecimal;

/**
 * 奖项提名列表条目(带稿件标题、作者昵称)
 */
public class NominationView {

    private Long id;
    private Long manuscriptId;
    private String manuscriptTitle;
    private String category;
    private String authorNickname;
    private BigDecimal finalScore;
    private String result;
    private String fileName;
    private String fileUrl;
    private Long fileSize;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getManuscriptId() {
        return manuscriptId;
    }

    public void setManuscriptId(Long manuscriptId) {
        this.manuscriptId = manuscriptId;
    }

    public String getManuscriptTitle() {
        return manuscriptTitle;
    }

    public void setManuscriptTitle(String manuscriptTitle) {
        this.manuscriptTitle = manuscriptTitle;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
