package com.nexora.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识文档分块：优先按 Markdown 标题分段，超长再按自然边界切分。
 */
public final class TextChunker {

    private TextChunker() {
    }

    public static List<String> split(String content, int maxLen) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return chunks;
        }
        content = stripFrontmatter(content);
        int size = Math.max(200, maxLen);
        for (String section : splitByHeading(content)) {
            if (section.length() <= size) {
                if (!section.isBlank()) {
                    chunks.add(section.trim());
                }
                continue;
            }
            chunks.addAll(splitByLength(section, size));
        }
        return chunks;
    }

    public static String stripFrontmatter(String content) {
        if (content == null || !content.startsWith("---")) {
            return content;
        }
        int firstEnd = content.indexOf("\n---", 4);
        if (firstEnd < 0) {
            firstEnd = content.indexOf("\r\n---", 4);
        }
        if (firstEnd < 0) {
            return content;
        }
        int contentStart = content.startsWith("\r\n", firstEnd) ? firstEnd + 5 : firstEnd + 4;
        while (contentStart < content.length() && (content.charAt(contentStart) == '\n'
                || content.charAt(contentStart) == '\r')) {
            contentStart++;
        }
        return content.substring(contentStart);
    }

    private static List<String> splitByHeading(String content) {
        List<String> sections = new ArrayList<>();
        String[] lines = content.split("\n", -1);
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("#") && current.length() > 0) {
                sections.add(current.toString());
                current = new StringBuilder();
            }
            current.append(line).append("\n");
        }
        if (current.length() > 0) {
            sections.add(current.toString());
        }
        return sections;
    }

    private static List<String> splitByLength(String text, int maxLen) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLen, text.length());
            if (end < text.length()) {
                int boundary = findBoundary(text, start, end);
                if (boundary > start) {
                    end = boundary;
                }
            }
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end >= text.length()) {
                break;
            }
            start = Math.max(end - 50, start + 1);
        }
        return chunks;
    }

    private static int findBoundary(String text, int start, int preferredEnd) {
        int searchStart = Math.max(start + 1, preferredEnd - 60);
        for (int i = preferredEnd; i >= searchStart; i--) {
            char c = text.charAt(i);
            if (c == '\n' || c == '。' || c == '.' || c == '!' || c == '？' || c == '?') {
                return i + 1;
            }
        }
        for (int i = preferredEnd; i >= searchStart; i--) {
            if (text.charAt(i) == ' ') {
                return i + 1;
            }
        }
        return preferredEnd;
    }
}
