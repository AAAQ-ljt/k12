package com.nexora.component;

import com.nexora.entity.vo.PreferencePageVO;
import com.nexora.mappers.PreferencePageMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 《我的学习偏好》受保护系统页（计划 C3）。
 *
 * 定位：学生在个人知识库里的一篇**系统页**（`knowledge_doc.system_type = 'PREFERENCE'`）——
 * 根目录置顶、不可移动、不可删除、**不做向量化**、可重置；正文里写明用途与边界。
 *
 * 正文结构（渲染式，写进库的是快照）：
 * ```
 * 【顶部说明】这篇文档是干什么的（固定，不可删）
 * <!-- AI-RULES-START -->
 * ## 规则段（系统生成，按 C2 的规则表渲染；直接改这里下次同步会失效）
 * <!-- AI-RULES-END -->
 * ## 自由段（学生自己写，永久保留，AI 也会参考）
 * ```
 * 规则变更（学生加/删/停用偏好）→ 调 {@link #syncRules} 重新渲染规则段，自由段原样保留；
 * 学生在知识页里直接编辑正文 → 调 {@link #saveFromEditor} 把自由段取出来重新拼装，避免把规则段改坏。
 */
@Slf4j
@Component
public class PreferencePageComponent {

    /** 系统页类型标识 */
    public static final String SYSTEM_TYPE = "PREFERENCE";

    /** 标题（固定，学生改名也不影响系统按 docId 找到它） */
    public static final String PAGE_TITLE = "我的学习偏好";

    /** 规则段标记：渲染与解析都依赖它，改动需前后兼容 */
    private static final String RULES_START = "<!-- AI-RULES-START -->";
    private static final String RULES_END = "<!-- AI-RULES-END -->";

    /** 顶部固定说明（学生可读、不可删；每行都是一句人话） */
    private static final String HEADER = """
            > 这篇文档是「AI 助教对你的个人设定」：你写在这里的偏好，AI 每次回答你时都会参考。
            > · 它固定放在这里（不能移动、不能删除），想恢复原样点「重置」即可；
            > · 它不会参与资料检索（不做向量化），所以不会在回答学习问题时被当作教材资料引用；
            > · 带标记的「规则段」由系统按「我的 → AI 偏好设置」里的规则生成，直接改会被下次同步覆盖；
            > · 想加规则请去「我的 → AI 偏好设置」（或在知识中心点「我的学习偏好」）。
            """;

    /** 自由段的默认示例（学生照抄改写即可） */
    private static final String DEFAULT_FREE = """
            - 例：以后叫我小明
            - 例：回答控制在 3 句内，太长我看不下去
            - 例：先举一个生活中的例子，再讲原理
            - 例：不要把答案直接告诉我，先提示思路
            - 例：我记历史喜欢画时间线，讲历史时尽量按时间顺序说
            """;

    @Resource
    private PreferencePageMapper preferencePageMapper;

    @Resource
    private UserPromptRuleComponent userPromptRuleComponent;

    @Resource
    private StudentProfileComponent studentProfileComponent;

    /**
     * 取偏好页（不存在就按默认内容创建并绑定锚点）。任何异常都返回 null，调用方按"没有该页"处理，不影响其它功能。
     */
    public PreferencePageVO page(String userId, String stage) {
        if (StringTools.isEmpty(userId)) {
            return null;
        }
        try {
            PreferencePageVO page = preferencePageMapper.selectByOwner(userId);
            if (page == null) {
                // 并发保护：两个标签页（知识中心 + AI 助手的知识页抽屉）同时首次进入时，
                // 原来的"查到没有就插入"会各插一行、产生两篇《我的学习偏好》（2026-10-08 修）
                synchronized (this) {
                    page = preferencePageMapper.selectByOwner(userId);
                    if (page == null) {
                        page = create(userId, stage);
                    }
                }
            }
            return page;
        } catch (Exception e) {
            log.warn("读取偏好页失败 userId={}", userId, e);
            return null;
        }
    }

    /** 规则变更后同步规则段（自由段保留）；失败只记日志——偏好规则本身已生效，页面同步失败不影响对话 */
    public void syncRules(String userId, String stage) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        try {
            PreferencePageVO page = preferencePageMapper.selectByOwner(userId);
            if (page == null) {
                create(userId, stage);
                return;
            }
            String free = parseFreeSection(page.getContent());
            preferencePageMapper.updateContent(page.getDocId(), PAGE_TITLE, render(userId, free));
        } catch (Exception e) {
            log.warn("偏好页规则段同步失败 userId={}", userId, e);
        }
    }

    /**
     * 学生在知识页编辑器里保存后的处理：只取自由段，重新拼装（规则段始终由系统渲染），避免把规则段改坏。
     */
    public boolean saveFromEditor(String userId, String docId, String editedContent) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(docId)) {
            return false;
        }
        if (!isPreferencePage(docId, userId)) {
            return false;
        }
        try {
            String free = parseFreeSection(editedContent);
            preferencePageMapper.updateContent(docId, PAGE_TITLE, render(userId, free));
            return true;
        } catch (Exception e) {
            log.warn("偏好页保存失败 userId={} docId={}", userId, docId, e);
            return false;
        }
    }

    /**
     * 重置：清空学生自填规则（保留系统默认）并把正文恢复成初始状态。
     */
    public PreferencePageVO reset(String userId, String stage) {
        if (StringTools.isEmpty(userId)) {
            return null;
        }
        userPromptRuleComponent.resetStudentRules(userId);
        PreferencePageVO page = preferencePageMapper.selectByOwner(userId);
        if (page == null) {
            return create(userId, stage);
        }
        preferencePageMapper.updateContent(page.getDocId(), PAGE_TITLE, render(userId, DEFAULT_FREE.trim()));
        return preferencePageMapper.selectByOwner(userId);
    }

    /** 该 docId 是否是这个学生的偏好页（删除/移动/入库/编辑前的保护判定） */
    public boolean isPreferencePage(String docId, String userId) {
        if (StringTools.isEmpty(docId) || StringTools.isEmpty(userId)) {
            return false;
        }
        try {
            Integer count = preferencePageMapper.countPreferencePage(docId, userId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("偏好页判定失败 docId={}", docId, e);
            return false;
        }
    }

    /**
     * 自由段正文（注入系统提示词用，最低优先级）。
     *
     * <p>页面头部写着「你写在这里的偏好，AI 每次回答你时都会参考」，而自由段既不落规则表也不向量化，
     * 之前**没有任何地方读它** → 学生写了等于没写（2026-10-08 修）。这里剥掉规则段与提示语、只取自由段；
     * 仍是默认示例（学生没写过）时返回空串，不污染提示词。
     */
    public String freeSectionForPrompt(String userId) {
        if (StringTools.isEmpty(userId)) {
            return "";
        }
        try {
            PreferencePageVO page = preferencePageMapper.selectByOwner(userId);
            if (page == null || StringTools.isEmpty(page.getContent())) {
                return "";
            }
            String free = parseFreeSection(page.getContent());
            if (StringTools.isEmpty(free) || free.trim().equals(DEFAULT_FREE.trim())) {
                return "";
            }
            // 只保留有内容的行，去掉示例行，避免把「例：…」当成学生的真实偏好喂给模型
            StringBuilder sb = new StringBuilder();
            for (String line : free.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("- 例：") || trimmed.startsWith("例：")
                        || trimmed.startsWith("## ")) {
                    continue;
                }
                sb.append(trimmed).append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("读取偏好页自由段失败 userId={}", userId, e);
            return "";
        }
    }

    private PreferencePageVO create(String userId, String stage) {
        String docId = UUID.randomUUID().toString().replace("-", "");
        preferencePageMapper.insertPage(docId, userId, StringTools.isEmpty(stage) ? "" : stage,
                PAGE_TITLE, render(userId, DEFAULT_FREE.trim()));
        // 会话锚点写进画像（C1 已预留字段）：之后一律按 docId 定位，学生改标题/移动都不影响
        studentProfileComponent.bindPreferenceDoc(userId, docId);
        log.info("已为学生创建《我的学习偏好》系统页 userId={} docId={}", userId, docId);
        return preferencePageMapper.selectByOwner(userId);
    }

    /** 渲染完整正文：顶部说明 + 规则段（按规则表）+ 自由段 */
    private String render(String userId, String freeSection) {
        String rulesBlock = userPromptRuleComponent.promptBlock(userId);
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER.trim()).append("\n\n");
        sb.append(RULES_START).append("\n");
        sb.append("## 规则段（系统生成）\n");
        if (StringTools.isEmpty(rulesBlock)) {
            sb.append("（还没有设置偏好——去「我的 → AI 偏好设置」加一条，这里会自动更新）\n");
        } else {
            // 复用对话里注入的同一份规则文本，页面上看到的与 AI 看到的一致
            for (String line : rulesBlock.split("\n")) {
                if (line.startsWith("- ")) {
                    sb.append(line).append("\n");
                }
            }
        }
        sb.append(RULES_END).append("\n\n");
        sb.append("## 自由段（你自己写，AI 会参考）\n");
        sb.append(StringTools.isEmpty(freeSection) ? DEFAULT_FREE.trim() : freeSection);
        sb.append("\n");
        return sb.toString();
    }

    /** 从正文里取出自由段（标记之后的内容）；没有标记时把整篇（去掉顶部说明）当自由段，尽量不丢学生写的东西 */
    private String parseFreeSection(String content) {
        if (StringTools.isEmpty(content)) {
            return DEFAULT_FREE.trim();
        }
        int idx = content.lastIndexOf(RULES_END);
        if (idx >= 0) {
            String tail = content.substring(idx + RULES_END.length()).trim();
            return stripFreeTitle(tail);
        }
        String trimmed = content.trim();
        // 没标记：可能是学生把正文整体改写了，去掉顶部说明后全部当自由段保留
        int headerEnd = trimmed.indexOf("## ");
        return headerEnd > 0 ? trimmed.substring(headerEnd).trim() : trimmed;
    }

    private String stripFreeTitle(String text) {
        String result = text.trim();
        if (result.startsWith("## 自由段")) {
            int lineEnd = result.indexOf('\n');
            result = lineEnd > 0 ? result.substring(lineEnd + 1).trim() : "";
        }
        return StringTools.isEmpty(result) ? DEFAULT_FREE.trim() : result;
    }
}
