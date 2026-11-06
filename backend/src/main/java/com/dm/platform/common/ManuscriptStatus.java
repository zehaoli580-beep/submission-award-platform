package com.dm.platform.common;

import java.util.Map;
import java.util.Set;

/**
 * 稿件状态机定义
 *
 * 状态取值:
 *   PENDING_INITIAL  待初审
 *   UNDER_REVIEW     外审中(已分配给审稿人)
 *   REVISE           退修改(外审意见后作者需修改重投)
 *   ACCEPTED         已录用
 *   REJECTED         已退稿
 *
 * 合法流转表 ALLOWED_TRANSITIONS 是本系统"防越权"的核心:
 * 任何状态变更都必须先查这张表, 不在表里的流转一律抛异常.
 */
public final class ManuscriptStatus {

    public static final String PENDING_INITIAL = "PENDING_INITIAL";
    public static final String UNDER_REVIEW    = "UNDER_REVIEW";
    public static final String REVISE          = "REVISE";
    public static final String ACCEPTED        = "ACCEPTED";
    public static final String REJECTED        = "REJECTED";

    /** key = 当前状态, value = 允许流转到的目标状态集合 */
    public static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            PENDING_INITIAL, Set.of(UNDER_REVIEW, REJECTED), // 初审: 通过进入外审 / 不合规范直接退稿
            UNDER_REVIEW,    Set.of(REVISE, ACCEPTED, REJECTED), // 终审: 依外审意见退修改/录用/退稿
            REVISE,          Set.of(UNDER_REVIEW, REJECTED)  // 修改重投: 再送审 / 放弃被退稿
    );

    /** 状态 -> 中文显示名(供前端展示用) */
    public static final Map<String, String> CN_NAMES = Map.of(
            PENDING_INITIAL, "待初审",
            UNDER_REVIEW,    "外审中",
            REVISE,          "退修改",
            ACCEPTED,        "已录用",
            REJECTED,        "已退稿"
    );

    private ManuscriptStatus() {
    }

    /** 判断 from -> to 是否为合法流转 */
    public static boolean canTransit(String from, String to) {
        Set<String> targets = ALLOWED_TRANSITIONS.get(from);
        return targets != null && targets.contains(to);
    }
}
