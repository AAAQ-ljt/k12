package com.nexora.service;

import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.utils.StageNormalizer;
import com.nexora.utils.StringTools;
import com.nexora.utils.TextbookContentUtils;
import com.nexora.utils.TextbookContentUtils.TocEntry;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;

/**
 * MCP 教材知识检索工具服务（官方教材库白名单只读）：
 * 查书目 / 读章节目录 / 读指定章节正文 三个工具，只做数据操作、不调用大模型。
 *
 * 安全约束（对齐 KnowledgeToolService）：
 * 1. 仅官方教材库（owner_id IS NULL 且 status=1），docId 存在性校验；
 * 2. 学段由 web 端经 ToolContext 注入并强制过滤/校验（模型不可见、不可伪造），与 RAG 检索同口径；
 * 3. 输出设上限：书目 20 条（兜底节选 15）、目录 200 条、单节 8000 字符（offset 续读）；
 * 4. 只读，不提供任何新增/修改/删除工具；内部 try-catch 不抛未捕获异常，失败 log.warn 返回友好文案。
 */
@Service
public class TextbookToolService {

    private static final Logger log = LoggerFactory.getLogger(TextbookToolService.class);

    /** 书目单次返回上限 */
    private static final int CATALOG_LIMIT = 20;

    /** 未命中时同学段书目节选条数 */
    private static final int CATALOG_FALLBACK_LIMIT = 15;

    /** 单节正文返回上限（字符），超出用 offset 续读 */
    private static final int SECTION_MAX_CHARS = 8000;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Tool(name = "searchTextbooks", description = "查询官方教材书目（按学生学段过滤，标题关键词匹配）。"
            + "用户指名教材/章节（如\"必修一\"\"某一本书\"）时先调用本工具找书，再用 getTextbookToc 看目录")
    public String searchTextbooks(
            @ToolParam(description = "学段编码，由系统自动注入") String stage,
            @ToolParam(description = "书名关键词，如：必修一 / 政治 / 信息技术，多个词用空格分隔") String keyword) {
        try {
            List<KnowledgeDoc> docs = listOfficialDocs(stage);
            if (docs.isEmpty()) {
                return "该学段暂无教材数据";
            }
            List<KnowledgeDoc> matched = matchByKeyword(docs, keyword);
            if (matched.isEmpty()) {
                StringBuilder sb = new StringBuilder("未找到匹配「").append(keyword == null ? "" : keyword)
                        .append("」的教材。该学段现有教材节选（共 ").append(docs.size()).append(" 本，最多列 ")
                        .append(CATALOG_FALLBACK_LIMIT).append(" 本）：\n");
                int index = 1;
                for (KnowledgeDoc doc : docs) {
                    if (index > CATALOG_FALLBACK_LIMIT) {
                        break;
                    }
                    sb.append(index++).append(". 《").append(doc.getTitle()).append("》")
                            .append("（docId:").append(doc.getDocId()).append("）\n");
                }
                sb.append("可换关键词重查（学科名 / 必修一 / 选择性必修二 / 年级等）。");
                return sb.toString();
            }
            if (StringTools.isEmpty(keyword)) {
                // 全量书目：按学科分组展示，并在结尾写明「这是完整清单」，避免模型再凭印象补书或漏书
                return buildFullCatalog(docs, stage);
            }
            StringBuilder sb = new StringBuilder("匹配教材（共 ").append(matched.size()).append(" 本，按相关度排序）：\n");
            int index = 1;
            for (KnowledgeDoc doc : matched) {
                sb.append(index++).append(". 《").append(doc.getTitle()).append("》")
                        .append("（docId:").append(doc.getDocId())
                        .append("，约").append(doc.getContent() == null ? 0 : doc.getContent().length() / 10000).append("万字")
                        .append("，分块:").append(doc.getChunkCount() == null ? 0 : doc.getChunkCount())
                        .append("）\n");
            }
            sb.append("下一步：用 getTextbookToc 查看某本书的章节目录。");
            return sb.toString();
        } catch (Exception e) {
            log.warn("searchTextbooks 失败", e);
            return "查询教材书目失败：" + e.getMessage();
        }
    }

    @Tool(name = "getTextbookToc", description = "读取指定教材的章节目录（序号+标题+本节字数）。"
            + "用户要读某本书的某节课/章节前，先用本工具看目录定位序号")
    public String getTextbookToc(
            @ToolParam(description = "教材docId（来自 searchTextbooks）") String docId,
            @ToolParam(description = "学段编码，由系统自动注入") String stage) {
        try {
            KnowledgeDoc doc = requireOfficialDoc(docId, stage);
            if (doc == null) {
                return "教材不存在或不属于当前学段，请先用 searchTextbooks 确认";
            }
            List<TocEntry> toc = TextbookContentUtils.parseToc(doc.getContent());
            if (toc.isEmpty()) {
                return "《" + doc.getTitle() + "》未解析到章节结构，无法按章节读取";
            }
            StringBuilder sb = new StringBuilder("《").append(doc.getTitle()).append("》章节目录（共 ")
                    .append(toc.size()).append(" 节）：\n");
            for (TocEntry entry : toc) {
                sb.append(entry.index()).append(". ").append(entry.title())
                        .append("（本节约").append(entry.length()).append("字）\n");
            }
            if (toc.size() >= TextbookContentUtils.MAX_TOC_ENTRIES) {
                sb.append("（目录已达上限被截断，可用 readTextbookSection 按已列序号读取）");
            }
            sb.append("下一步：用 readTextbookSection 按 docId + 序号或标题读取章节正文。");
            return sb.toString();
        } catch (Exception e) {
            log.warn("getTextbookToc 失败", e);
            return "读取教材目录失败：" + e.getMessage();
        }
    }

    @Tool(name = "readTextbookSection", description = "读取指定教材某章节的正文（Markdown，按目录序号或章节标题定位）。"
            + "内容超长时返回截断标记，用 offset 参数续读剩余部分")
    public String readTextbookSection(
            @ToolParam(description = "教材docId") String docId,
            @ToolParam(description = "章节定位：目录序号（如 3）或章节标题（如 第一课）") String section,
            @ToolParam(description = "续读偏移字符数，首次读取不传或传 0") Integer offset,
            @ToolParam(description = "学段编码，由系统自动注入") String stage) {
        try {
            KnowledgeDoc doc = requireOfficialDoc(docId, stage);
            if (doc == null) {
                return "教材不存在或不属于当前学段，请先用 searchTextbooks 确认";
            }
            List<TocEntry> toc = TextbookContentUtils.parseToc(doc.getContent());
            if (toc.isEmpty()) {
                return "《" + doc.getTitle() + "》未解析到章节结构，无法按章节读取";
            }
            TocEntry entry = resolveEntry(toc, section);
            if (entry == null) {
                return "未找到章节「" + section + "」，可用 getTextbookToc 查看目录后按序号重试";
            }
            String content = doc.getContent();
            int from = entry.start() + (offset == null || offset < 0 ? 0 : offset);
            if (from >= entry.end()) {
                return "偏移已超出本节范围（本节共 " + entry.length() + " 字），请减小 offset";
            }
            int to = Math.min(entry.end(), from + SECTION_MAX_CHARS);
            StringBuilder sb = new StringBuilder("《").append(doc.getTitle()).append("》·")
                    .append(entry.title())
                    .append("（本节共 ").append(entry.length()).append(" 字，当前返回 ")
                    .append(from - entry.start()).append("~").append(to - entry.start()).append("）\n\n");
            sb.append(content, from, to);
            if (to < entry.end()) {
                sb.append("\n\n（本节未完，续读请传 offset=").append(to - entry.start()).append("）");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("readTextbookSection 失败", e);
            return "读取教材章节失败：" + e.getMessage();
        }
    }

    /**
     * 官方教材列表：owner_id IS NULL + status=1 + 已入库，stage 非空时过滤；
     * 注入的 stage 正常为编码，这里再做一次归一化兜底（兼容直接调 MCP 传中文的场景）
     */
    private List<KnowledgeDoc> listOfficialDocs(String stage) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setStatus(1);
        query.setOwnerIdNull(Boolean.TRUE);
        query.setVectorStatus(2);
        if (!StringTools.isEmpty(stage)) {
            String code = StageNormalizer.normalize(stage);
            query.setStage(code == null ? stage : code);
        }
        return knowledgeDocService.findListByParam(query);
    }

    /** 学科分组顺序与识别关键词（教材标题里带学科名，按此归组；未识别的进「其他」） */
    private static final String[][] SUBJECT_GROUPS = {
            {"语文", "语文"}, {"数学", "数学"}, {"英语", "英语"}, {"物理", "物理"}, {"化学", "化学"},
            {"生物", "生物"}, {"历史", "历史"}, {"地理", "地理"}, {"政治", "政治"}, {"道德与法治", "道德与法治"},
            {"信息技术", "信息技术"}, {"信息", "信息"}, {"科学", "科学"},
    };

    /**
     * 全量教材书目（不含关键词时使用）：按学科分组列全，并明确声明「本清单已完整」。
     *
     * 之所以要声明完整：之前这里会截断到固定条数，模型据此得出「历史只有 1 本」这类错误结论；
     * 现在给全量 + 明示总数，模型就能如实回答而不再"补书"或"漏书"。
     */
    private String buildFullCatalog(List<KnowledgeDoc> docs, String stage) {
        Map<String, List<KnowledgeDoc>> grouped = new LinkedHashMap<>();
        List<KnowledgeDoc> others = new ArrayList<>();
        for (KnowledgeDoc doc : docs) {
            String title = TextbookContentUtils.normalizeTitle(doc.getTitle());
            String hit = null;
            for (String[] group : SUBJECT_GROUPS) {
                if (title.contains(group[1])) {
                    hit = group[0];
                    break;
                }
            }
            if (hit == null) {
                others.add(doc);
            } else {
                grouped.computeIfAbsent(hit, key -> new ArrayList<>()).add(doc);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("教材书目（学段：").append(StringTools.isEmpty(stage) ? "全部" : stage)
                .append("，共 ").append(docs.size()).append(" 本，以下为**完整清单**，不要再自行增删）：\n");
        int index = 1;
        for (Map.Entry<String, List<KnowledgeDoc>> entry : grouped.entrySet()) {
            sb.append("\n【").append(entry.getKey()).append("】").append(entry.getValue().size()).append(" 本\n");
            for (KnowledgeDoc doc : entry.getValue()) {
                sb.append(index++).append(". 《").append(doc.getTitle()).append("》")
                        .append("（docId:").append(doc.getDocId()).append("）\n");
            }
        }
        if (!others.isEmpty()) {
            sb.append("\n【其他】").append(others.size()).append(" 本\n");
            for (KnowledgeDoc doc : others) {
                sb.append(index++).append(". 《").append(doc.getTitle()).append("》")
                        .append("（docId:").append(doc.getDocId()).append("）\n");
            }
        }
        sb.append("\n以上合计 ").append(docs.size()).append(" 本。学生问某本书的目录或内容时，用 getTextbookToc / readTextbookSection 按 docId 继续查。");
        return sb.toString();
    }

    /**
     * 关键词匹配：关键词按空白拆 token，标题归一化（错别字/全半角兼容）后按命中 token 数打分排序；
     * 任一 token 都不命中视为不相关。keyword 为空时按原顺序返回全部（截断到上限）
     */
    private List<KnowledgeDoc> matchByKeyword(List<KnowledgeDoc> docs, String keyword) {
        if (StringTools.isEmpty(keyword)) {
            // 学生问「有哪些书」时不能截断：截断会让模型以为库里只有这些书，
            // 进而给出「历史只有 1 本」这类错误结论（2026-10-07 用户反馈）。教材本数有限，直接给全量。
            return docs;
        }
        String[] tokens = keyword.trim().split("\\s+");
        List<KnowledgeDoc> matched = new ArrayList<>();
        for (KnowledgeDoc doc : docs) {
            String title = TextbookContentUtils.normalizeTitle(doc.getTitle());
            int score = 0;
            for (String token : tokens) {
                if (title.contains(TextbookContentUtils.normalizeTitle(token))) {
                    score++;
                }
            }
            if (score > 0) {
                matched.add(doc);
            }
        }
        matched.sort(Comparator.comparingInt(
                (KnowledgeDoc doc) -> {
                    String title = TextbookContentUtils.normalizeTitle(doc.getTitle());
                    int score = 0;
                    for (String token : tokens) {
                        if (title.contains(TextbookContentUtils.normalizeTitle(token))) {
                            score++;
                        }
                    }
                    return -score;
                }).thenComparing(KnowledgeDoc::getTitle));
        // 关键词命中也不再截断：命中本数本来就不多，截断同样会让模型误判「库里只有这几本」
        return matched;
    }

    /**
     * 校验并返回官方教材：docId 非空、存在、status=1、owner_id 为空、学段一致（stage 空表示不限）
     */
    private KnowledgeDoc requireOfficialDoc(String docId, String stage) {
        if (StringTools.isEmpty(docId)) {
            return null;
        }
        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId.trim());
        if (doc == null || doc.getStatus() == null || doc.getStatus() != 1
                || !StringTools.isEmpty(doc.getOwnerId())) {
            return null;
        }
        if (!StringTools.isEmpty(stage)) {
            String code = StageNormalizer.normalize(stage);
            String normalized = code == null ? stage : code;
            if (!normalized.equals(doc.getStage())) {
                return null;
            }
        }
        return doc;
    }

    /**
     * 章节定位：纯数字按目录序号精确匹配；否则标题归一化模糊匹配（包含即命中，取第一个）
     */
    private TocEntry resolveEntry(List<TocEntry> toc, String section) {
        if (StringTools.isEmpty(section)) {
            return null;
        }
        String target = section.trim();
        if (target.matches("\\d+")) {
            int index = Integer.parseInt(target);
            for (TocEntry entry : toc) {
                if (entry.index() == index) {
                    return entry;
                }
            }
            return null;
        }
        String normalized = TextbookContentUtils.normalizeTitle(target);
        for (TocEntry entry : toc) {
            if (TextbookContentUtils.normalizeTitle(entry.title()).contains(normalized)) {
                return entry;
            }
        }
        return null;
    }
}
