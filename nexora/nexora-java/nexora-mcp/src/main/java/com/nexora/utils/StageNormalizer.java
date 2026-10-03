package com.nexora.utils;

import com.nexora.entity.enums.StageEnum;

/**
 * 学段归一化（MCP 工具入参防线）：把模型给出的各种学段表达统一为 StageEnum 编码。
 * 支持：编码大小写兼容（senior→SENIOR）、中文别名（高中/初中/小学低年级/小学高年级）、
 * 年级词映射（高一/高二/高三→SENIOR；初一/初二/初三→JUNIOR；一/二年级→PRIMARY_LOW；三~六年级→PRIMARY_HIGH）。
 * 归一化失败返回 null——调用方须先自行判断原始入参是否为空（空=不过滤），
 * 非空但归一化为 null 时应返回"无法识别的学段"提示并附有效编码列表。
 */
public final class StageNormalizer {

    private StageNormalizer() {
    }

    /**
     * 归一化为 StageEnum 编码；无法识别返回 null（与"未传学段"同值，调用方需先判空原始入参）
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim();
        for (StageEnum item : StageEnum.values()) {
            if (item.getCode().equalsIgnoreCase(text)) {
                return item.getCode();
            }
            if (text.contains(item.getDesc())) {
                return item.getCode();
            }
        }
        // 年级词映射（先高年级段后低年级段，避免"一年级"误吞"初一"以外的表达）
        if (text.contains("高一") || text.contains("高二") || text.contains("高三")) {
            return StageEnum.SENIOR.getCode();
        }
        if (text.contains("初一") || text.contains("初二") || text.contains("初三")) {
            return StageEnum.JUNIOR.getCode();
        }
        if (text.contains("一年级") || text.contains("二年级")) {
            return StageEnum.PRIMARY_LOW.getCode();
        }
        if (text.contains("三年级") || text.contains("四年级")
                || text.contains("五年级") || text.contains("六年级")) {
            return StageEnum.PRIMARY_HIGH.getCode();
        }
        return null;
    }

    /**
     * 有效学段编码提示文案（供工具返回给模型）
     */
    public static String validCodes() {
        StringBuilder sb = new StringBuilder();
        for (StageEnum item : StageEnum.values()) {
            if (sb.length() > 0) {
                sb.append("/");
            }
            sb.append(item.getCode()).append("(").append(item.getDesc()).append(")");
        }
        return sb.toString();
    }
}
