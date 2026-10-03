package com.nexora.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 教材正文（Markdown）内容工具：书名归一化 + 章节目录解析。
 * 目录解析兼容 AI 文档整理产出的两种标题形态（2026-10-02 库内实测）：
 *   ① 行首 ATX 一级标题：# 第一章·1.1 数据、信息与知识（信息技术等，带 #）
 *   ② 裸行课节标记：第二课·1 新民主主义革命的胜利（政治等，无 #，整行即标题）
 * 二级以下 ATX 标题（## 摘要 等）是节内小节，不算章节，随所属章节整体切片；
 * 解析结果为有序目录（含正文字符区间），供按章节切片读取。
 */
public final class TextbookContentUtils {

    /** 目录条目上限：超长文档截断，由调用方提示按序号分段查看 */
    public static final int MAX_TOC_ENTRIES = 200;

    /** 裸行课节标记的最大行长：超过视为正文段落而非标题 */
    private static final int MAX_HEADING_LINE_LENGTH = 60;

    /** 行首 ATX 一级标题（仅 #，(?!#) 排除 ## / ###，二级以下为节内小节不入目录） */
    private static final Pattern ATX_HEADING = Pattern.compile("^#(?!#)\\s*(\\S.*)$");

    /** 裸行课节标记：第X课/单元/章/节/讲/框 开头（支持汉字与阿拉伯数字） */
    private static final Pattern LESSON_HEADING = Pattern.compile("^第[0-9一二三四五六七八九十百]+[课单元章节讲框]");

    /** 章节标题内不该出现的句读（出现即视为正文段落） */
    private static final String SENTENCE_PUNCTUATION = "。，；！？";

    private TextbookContentUtils() {
    }

    /** 标题匹配时要剔除的装饰符号：书名号/引号/间隔号/破折号等（避免"《红楼梦》内容讲解"匹配不上"红楼梦内容讲解"） */
    private static final String STRIPPED_TITLE_MARKS = "[《》「」『』【】·—－－\"']";

    /**
     * 书名归一化：兼容库内实测错别字（"选择性必须一"→"必修"）、全半角差异，去空白与书名号等装饰符号
     */
    public static String normalizeTitle(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim()
                .replace('（', '(')
                .replace('）', ')')
                .replace('：', ':')
                .replace("必须", "必修")
                .replaceAll(STRIPPED_TITLE_MARKS, "")
                .replaceAll("\\s+", "");
    }

    /**
     * 目录条目：index 从 1 开始；level 目前恒为 1（扁平目录，顺序即文档顺序）；
     * start/end 为正文字符区间 [start, end)，末条 end = content.length()
     */
    public record TocEntry(int index, int level, String title, int start, int end) {

        /**
         * 本节字符数
         */
        public int length() {
            return end - start;
        }
    }

    /**
     * 解析章节目录：扫描每一行，命中 ATX 一级标题或裸行课节标记即记一条。
     * 区间左端为标题行起点，右端为下一条目起点（末条到正文结尾）。
     */
    public static List<TocEntry> parseToc(String content) {
        List<TocEntry> toc = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return toc;
        }
        int lineStart = 0;
        while (lineStart <= content.length()) {
            int newline = content.indexOf('\n', lineStart);
            int lineEnd = newline < 0 ? content.length() : newline;
            String line = content.substring(lineStart, lineEnd);
            String title = headingTitle(line);
            if (title != null && toc.size() < MAX_TOC_ENTRIES) {
                if (!toc.isEmpty()) {
                    // 回填上一条的区间右端：到当前标题行之前
                    TocEntry last = toc.get(toc.size() - 1);
                    toc.set(toc.size() - 1, new TocEntry(last.index(), last.level(), last.title(), last.start(), lineStart));
                }
                toc.add(new TocEntry(toc.size() + 1, 1, title, lineStart, content.length()));
            }
            if (newline < 0) {
                break;
            }
            lineStart = newline + 1;
        }
        return toc;
    }

    /**
     * 判断一行是否章节标题：ATX 一级标题或裸行课节标记；
     * 裸行需行长短于上限且不含句读，防止正文段落误判；命中返回标题文本，否则 null
     */
    private static String headingTitle(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        Matcher atx = ATX_HEADING.matcher(trimmed);
        if (atx.matches()) {
            return atx.group(1).trim();
        }
        if (trimmed.length() <= MAX_HEADING_LINE_LENGTH
                && LESSON_HEADING.matcher(trimmed).find()
                && trimmed.indexOf('。') < 0 && trimmed.indexOf('，') < 0
                && trimmed.indexOf('；') < 0 && trimmed.indexOf('！') < 0
                && trimmed.indexOf('？') < 0) {
            return trimmed;
        }
        return null;
    }
}
