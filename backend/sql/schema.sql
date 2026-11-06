-- =====================================================
-- 数字媒体作品投稿/评奖平台 数据库建表脚本
-- 数据库: dm_platform (utf8mb4)
-- 执行方式: mysql -uroot -p < schema.sql
--   (连接串已带 createDatabaseIfNotExist=true，也可不执行本行)
-- =====================================================
CREATE DATABASE IF NOT EXISTS dm_platform DEFAULT CHARACTER SET utf8mb4;
USE dm_platform;

-- 1. 用户表: 作者 / 审稿人 / 管理员
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`   VARCHAR(50) NOT NULL COMMENT '登录名',
    `password`   VARCHAR(80) NOT NULL COMMENT 'BCrypt 加密后的密码',
    `role`       VARCHAR(20) NOT NULL COMMENT '角色: AUTHOR-作者 REVIEWER-审稿人 ADMIN-管理员',
    `nickname`   VARCHAR(50) NOT NULL COMMENT '昵称',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '用户表';

-- 2. 稿件表
CREATE TABLE IF NOT EXISTS `manuscript` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '稿件ID',
    `author_id`   BIGINT       NOT NULL COMMENT '作者用户ID',
    `title`       VARCHAR(255) NOT NULL COMMENT '作品标题',
    `category`    VARCHAR(50)  NOT NULL COMMENT '作品分类(短视频/平面设计/动画/摄影等)',
    `description` VARCHAR(1000)         DEFAULT NULL COMMENT '作品简介',
    `file_name`   VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_url`    VARCHAR(500) NOT NULL COMMENT '存储访问路径(/files/xxx)',
    `file_size`   BIGINT       NOT NULL DEFAULT 0 COMMENT '文件字节数',
    `status`      VARCHAR(30)  NOT NULL DEFAULT 'PENDING_INITIAL' COMMENT '状态机',
    `submit_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投稿时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_author` (`author_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT = '稿件表';

-- 3. 评审分配表: 管理员把稿件分给若干审稿人(一篇稿可多人审)
CREATE TABLE IF NOT EXISTS `review_assignment` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `manuscript_id` BIGINT      NOT NULL COMMENT '稿件ID',
    `reviewer_id`   BIGINT      NOT NULL COMMENT '审稿人ID',
    `status`        VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' COMMENT 'ASSIGNED-待审 DONE-已提交意见',
    `assign_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_manuscript_reviewer` (`manuscript_id`, `reviewer_id`),
    KEY `idx_reviewer` (`reviewer_id`)
) ENGINE = InnoDB COMMENT = '评审分配表';

-- 4. 审稿意见表: 一条分配记录对应一份意见(一人一票)
CREATE TABLE IF NOT EXISTS `review_opinion` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `assignment_id` BIGINT      NOT NULL COMMENT '对应分配记录ID',
    `score`         INT         NOT NULL COMMENT '评分 0-100',
    `comment`       VARCHAR(1000)        DEFAULT NULL COMMENT '评审意见',
    `recommendation` VARCHAR(20) NOT NULL COMMENT '推荐结论: ACCEPT-录用 REVISE-退修改 REJECT-退稿',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assignment` (`assignment_id`)
) ENGINE = InnoDB COMMENT = '审稿意见表';

-- 5. 奖项批次表
CREATE TABLE IF NOT EXISTS `award` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(100) NOT NULL COMMENT '奖项名称, 如 2026 数字媒体设计大赛',
    `description`  VARCHAR(500)          DEFAULT NULL COMMENT '奖项说明',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'PREPARING' COMMENT 'PREPARING-筹备 REVIEWING-评审中 PUBLISHED-已公示 ARCHIVED-已归档',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `publish_time` DATETIME              DEFAULT NULL COMMENT '公示时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '奖项批次表';

-- 6. 提名表: 录用稿件进入某奖项的提名记录
CREATE TABLE IF NOT EXISTS `nomination` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `award_id`      BIGINT        NOT NULL COMMENT '奖项ID',
    `manuscript_id` BIGINT        NOT NULL COMMENT '稿件ID',
    `final_score`   DECIMAL(5, 2)          DEFAULT NULL COMMENT '终评得分(多评委去最高最低后均值)',
    `result`        VARCHAR(20)   NOT NULL DEFAULT 'NOMINATED' COMMENT 'NOMINATED-提名 WON-获奖 LOST-落选',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_award_manuscript` (`award_id`, `manuscript_id`)
) ENGINE = InnoDB COMMENT = '提名记录表';

-- 7. 操作日志表: 状态机每次流转都留痕, 谁在什么时候把稿件从什么状态改成什么状态
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `manuscript_id` BIGINT      NOT NULL COMMENT '稿件ID',
    `operator_id`   BIGINT      NOT NULL COMMENT '操作人ID',
    `action`        VARCHAR(50) NOT NULL COMMENT '动作: 投稿/初审通过/分配审稿人/提交审稿意见/终审录用/退稿...',
    `from_status`   VARCHAR(30)          DEFAULT NULL COMMENT '流转前状态',
    `to_status`     VARCHAR(30)          DEFAULT NULL COMMENT '流转后状态',
    `detail`        VARCHAR(500)         DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_manuscript` (`manuscript_id`)
) ENGINE = InnoDB COMMENT = '操作日志表';

-- 8. 提名打分表: 每个评委对每个提名作品的一条打分记录(一人一票)
CREATE TABLE IF NOT EXISTS `nomination_score` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT,
    `nomination_id`  BIGINT      NOT NULL COMMENT '提名记录ID',
    `judge_id`       BIGINT      NOT NULL COMMENT '评委(审稿人)用户ID',
    `score`          INT         NOT NULL COMMENT '评分 0-100',
    `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_nomination_judge` (`nomination_id`, `judge_id`)
) ENGINE = InnoDB COMMENT = '提名打分表';

-- 演示账号由后端 DataInitializer 首次启动时自动创建, 不在 SQL 里写死密码哈希
