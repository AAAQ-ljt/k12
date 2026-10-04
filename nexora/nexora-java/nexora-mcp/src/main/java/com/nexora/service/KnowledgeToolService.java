package com.nexora.service;

import com.nexora.component.WikiKnowledgeComponent;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.po.UserInfo;
import com.nexora.utils.DateUtil;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MCP 个人知识页工具服务（数据操作白名单）：
 * 列表 / 读取 / 新建 / 覆盖 / 入库 五个工具，只做数据操作、不调用大模型
 * （AI 摘要、改写、归档整合由 web 端知识页工具完成后经本服务落库）。
 *
 * 安全约束：每个工具都以 userId 做归属校验（非本人知识页一律拒绝）；不提供删除工具（删除只在学生端抽屉人工执行）；
 * 所有工具返回结构化字符串，内部 try-catch 不抛未捕获异常。
 */
@Service
public class KnowledgeToolService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeToolService.class);

    /** 单次读取正文上限（超出截断，避免超长文档撑爆模型上下文） */
    private static final int READ_LIMIT = 20000;

    @Resource
    private WikiKnowledgeComponent wikiKnowledgeComponent;

    @Resource
    private UserInfoService userInfoService;

    @Tool(name = "listKnowledgePages", description = "查询某个学生个人知识库的知识页清单（ID/标题/状态/来源/所属目录/分块数/更新时间），"
            + "可按关键词过滤标题、按状态过滤、按目录过滤；返回值同时附「知识页目录清单」（含 folderId），"
            + "移动知识页（moveKnowledgePage）或指定父目录（createWikiFolder）前先查本工具拿目录ID")
    public String listKnowledgePages(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "标题关键词，可空") String keyword,
            @ToolParam(description = "向量状态过滤：0草稿 1向量化中 2已入库 3失败，可空表示不限") Integer vectorStatus,
            @ToolParam(description = "目录过滤：传目录ID只看该目录内的知识页；传 root 只看根目录；不传看全部") String folderId) {
        try {
            String checked = requireStudent(userId);
            if (checked == null) {
                return "参数错误：缺少学生用户ID";
            }
            List<KnowledgeDoc> list = wikiKnowledgeComponent.listPages(checked, keyword, vectorStatus,
                    StringTools.isEmpty(folderId) ? null : folderId.trim());
            List<ResourceDirectory> folders = wikiKnowledgeComponent.listWikiFolders(checked);
            if (list == null || list.isEmpty()) {
                return "该学生暂无匹配的知识页。\n" + folderCatalog(folders);
            }
            int draft = 0;
            int ingested = 0;
            StringBuilder sb = new StringBuilder("知识页清单（共 ").append(list.size()).append(" 页）：\n");
            int index = 1;
            for (KnowledgeDoc doc : list) {
                Integer status = doc.getVectorStatus();
                if (status != null && status == 0) {
                    draft++;
                }
                if (status != null && status == 2) {
                    ingested++;
                }
                String folderLabel = "根目录";
                if (!StringTools.isEmpty(doc.getFolderId())) {
                    for (ResourceDirectory folder : folders) {
                        if (doc.getFolderId().equals(folder.getDirId())) {
                            folderLabel = folder.getDirName();
                            break;
                        }
                    }
                }
                sb.append(index++).append(". 《").append(doc.getTitle()).append("》")
                        .append("（ID:").append(doc.getDocId())
                        .append("，状态:").append(wikiKnowledgeComponent.statusText(status))
                        .append("，来源:").append(wikiKnowledgeComponent.sourceText(doc))
                        .append("，目录:").append(folderLabel)
                        .append("，分块:").append(doc.getChunkCount() == null ? 0 : doc.getChunkCount())
                        .append("，更新:").append(formatTime(doc))
                        .append("）\n");
            }
            sb.append("汇总：草稿 ").append(draft).append(" 页，已入库 ").append(ingested).append(" 页。\n");
            sb.append(folderCatalog(folders));
            return sb.toString();
        } catch (Exception e) {
            log.warn("listKnowledgePages 失败", e);
            return "查询知识页失败：" + e.getMessage();
        }
    }

    /**
     * 知识页目录清单（含 folderId）：供模型在移动知识页 / 指定父目录时携带目标ID；
     * folders 含 wiki 系统根目录本身（dirType 非空），根目录不列为子目录
     */
    private String folderCatalog(List<ResourceDirectory> folders) {
        StringBuilder sb = new StringBuilder("知识页目录（folderId 供 moveKnowledgePage / createWikiFolder 使用）：\n");
        sb.append("  1. 根目录（folderId 留空或传 root）\n");
        int index = 2;
        if (folders != null) {
            for (ResourceDirectory folder : folders) {
                if (!StringTools.isEmpty(folder.getDirType())) {
                    continue;
                }
                sb.append("  ").append(index++).append(". 《").append(folder.getDirName())
                        .append("》 folderId:").append(folder.getDirId()).append("\n");
            }
        }
        if (index == 2) {
            sb.append("  （暂无子目录，可用 createWikiFolder 新建）\n");
        }
        return sb.toString();
    }

    @Tool(name = "createWikiFolder", description = "在学生的知识页里新建一个子文件夹（用于归类整理知识页）。"
            + "用户说「建一个《XX》文件夹」时调用；parentFolderId 不传则建在知识页根目录下")
    public String createWikiFolder(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "文件夹名称") String name,
            @ToolParam(description = "父文件夹ID，可空表示建在知识页根目录下；可先通过 listKnowledgePages 了解现有文件夹") String parentFolderId) {
        try {
            String checked = requireStudent(userId);
            if (checked == null) {
                return "参数错误：缺少学生用户ID";
            }
            if (StringTools.isEmpty(name)) {
                return "参数错误：缺少文件夹名称";
            }
            String folderId = wikiKnowledgeComponent.createWikiFolder(
                    checked, name.trim(), StringTools.isEmpty(parentFolderId) ? null : parentFolderId.trim());
            log.info("MCP 新建知识页文件夹 userId={} name={} folderId={}", checked, name.trim(), folderId);
            return "已创建文件夹「" + name.trim() + "」（folderId:" + folderId + "），"
                    + "可以用 moveKnowledgePage 把知识页移动进去";
        } catch (Exception e) {
            log.warn("createWikiFolder 失败", e);
            return "新建文件夹失败：" + e.getMessage();
        }
    }

    @Tool(name = "moveKnowledgePage", description = "把指定知识页移动到某个子文件夹（或移回知识页根目录），知识页内容不变。"
            + "用户说「把《XX》移到《YY》文件夹」时调用；folderId 不传表示移回根目录")
    public String moveKnowledgePage(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "知识页ID") String docId,
            @ToolParam(description = "目标文件夹ID，可空表示移回知识页根目录") String folderId) {
        try {
            String checked = requireStudent(userId);
            if (checked == null) {
                return "参数错误：缺少学生用户ID";
            }
            KnowledgeDoc doc = wikiKnowledgeComponent.requireOwnedDoc(checked, docId);
            wikiKnowledgeComponent.moveDocToFolder(checked, docId,
                    StringTools.isEmpty(folderId) ? null : folderId.trim());
            String target = StringTools.isEmpty(folderId) ? "知识页根目录" : "文件夹 " + folderId;
            log.info("MCP 移动知识页 userId={} docId={} target={}", checked, docId, target);
            return "已把《" + doc.getTitle() + "》移动到" + target;
        } catch (Exception e) {
            log.warn("moveKnowledgePage 失败", e);
            return "移动知识页失败：" + e.getMessage();
        }
    }

    @Tool(name = "readKnowledgePage", description = "读取某篇知识页的完整内容（Markdown 正文）与元信息，用于后续修改、总结、归档整合")
    public String readKnowledgePage(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "知识页ID") String docId) {
        try {
            String checked = requireStudent(userId);
            if (checked == null) {
                return "参数错误：缺少学生用户ID";
            }
            KnowledgeDoc doc = wikiKnowledgeComponent.requireOwnedDoc(checked, docId);
            String content = doc.getContent() == null ? "" : doc.getContent();
            StringBuilder sb = new StringBuilder();
            sb.append("标题：").append(doc.getTitle()).append("\n");
            sb.append("ID：").append(doc.getDocId()).append("\n");
            sb.append("状态：").append(wikiKnowledgeComponent.statusText(doc.getVectorStatus()))
                    .append("（来源:").append(wikiKnowledgeComponent.sourceText(doc))
                    .append("，更新:").append(formatTime(doc)).append("）\n");
            if (doc.getVectorStatus() != null && doc.getVectorStatus() == 3
                    && !StringTools.isEmpty(doc.getVectorError())) {
                sb.append("上次入库失败原因：").append(doc.getVectorError()).append("\n");
            }
            sb.append("正文：\n");
            if (content.length() > READ_LIMIT) {
                sb.append(content, 0, READ_LIMIT)
                        .append("\n\n（正文超长已截断，共 ").append(content.length())
                        .append(" 字符；如需处理全文请分段处理）");
            } else {
                sb.append(content);
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("readKnowledgePage 失败", e);
            return "读取知识页失败：" + e.getMessage();
        }
    }

    @Tool(name = "createKnowledgePage", description = "在学生的个人知识库中新建一篇知识页草稿（不会自动入库）。"
            + "若传入 sourceUrl 且该学生已有同来源的知识页，则覆盖那一页而不是新建（用于同一来源重复整理）")
    public String createKnowledgePage(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "学段编码：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR，可空") String stage,
            @ToolParam(description = "知识页标题") String title,
            @ToolParam(description = "Markdown 正文") String content,
            @ToolParam(description = "来源标识，可空；同来源重复写入会覆盖已有页，如 ai-summary:源页ID") String sourceUrl) {
        try {
            String checked = requireStudent(userId);
            if (checked == null) {
                return "参数错误：缺少学生用户ID";
            }
            KnowledgeDoc doc;
            if (StringTools.isEmpty(sourceUrl)) {
                doc = wikiKnowledgeComponent.createDraft(checked, stage, title, content, null);
            } else {
                // 带来源标识时按来源去重（命中即覆盖回草稿态）
                doc = wikiKnowledgeComponent.saveDraftBySource(checked, stage, title, null, content, sourceUrl.trim());
            }
            return "已保存知识页草稿：《" + doc.getTitle() + "》（ID:" + doc.getDocId()
                    + "，状态:草稿）。如需让 AI 助教检索到，请调用 ingestKnowledgePage 入库，或由学生在「知识页」中确认入库。";
        } catch (Exception e) {
            log.warn("createKnowledgePage 失败", e);
            return "新建知识页失败：" + e.getMessage();
        }
    }

    @Tool(name = "updateKnowledgePage", description = "覆盖某篇知识页的标题与正文，保存后回到草稿态（已入库的页会先清除旧向量，需要重新入库才会被检索）")
    public String updateKnowledgePage(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "知识页ID") String docId,
            @ToolParam(description = "新的标题，可空表示不改标题") String title,
            @ToolParam(description = "新的 Markdown 正文") String content) {
        try {
            String checked = requireStudent(userId);
            if (checked == null) {
                return "参数错误：缺少学生用户ID";
            }
            KnowledgeDoc doc = wikiKnowledgeComponent.overwriteAsDraft(checked, docId, title, content);
            return "已更新知识页草稿：《" + doc.getTitle() + "》（ID:" + doc.getDocId()
                    + "，状态:草稿）。旧向量已提交清理；如需重新可检索，请调用 ingestKnowledgePage 入库。";
        } catch (Exception e) {
            log.warn("updateKnowledgePage 失败", e);
            return "更新知识页失败：" + e.getMessage();
        }
    }

    @Tool(name = "ingestKnowledgePage", description = "把知识页确认入库（向量化），入库后 AI 助教才能检索到该内容。"
            + "仅在用户明确要求「入库 / 上架 / 让 AI 能搜到」时调用，不要擅自入库")
    public String ingestKnowledgePage(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "知识页ID") String docId) {
        try {
            String checked = requireStudent(userId);
            if (checked == null) {
                return "参数错误：缺少学生用户ID";
            }
            KnowledgeDoc doc = wikiKnowledgeComponent.markIngest(checked, docId);
            return "已提交入库：《" + doc.getTitle() + "》（ID:" + doc.getDocId()
                    + "），当前状态：向量化中，完成后状态会变为「已入库」，届时 AI 助教即可检索到该内容。";
        } catch (Exception e) {
            log.warn("ingestKnowledgePage 失败", e);
            return "知识页入库失败：" + e.getMessage();
        }
    }

    /**
     * 学生存在性校验，返回去除空格后的 userId；不存在返回 null
     */
    private String requireStudent(String userId) {
        if (StringTools.isEmpty(userId)) {
            return null;
        }
        String checked = userId.trim();
        UserInfo user = userInfoService.getUserInfoByUserId(checked);
        if (user == null) {
            throw new IllegalArgumentException("学生不存在：" + checked);
        }
        return checked;
    }

    private String formatTime(KnowledgeDoc doc) {
        if (doc.getUpdateTime() == null) {
            return "-";
        }
        return DateUtil.format(doc.getUpdateTime(), DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
    }
}
