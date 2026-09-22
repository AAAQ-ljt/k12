package com.nexora.component;

import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.LearningPath;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.entity.query.KnowledgeMasteryQuery;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.query.LearningPathItemQuery;
import com.nexora.entity.query.LearningPathQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgeMasteryService;
import com.nexora.service.KnowledgePointService;
import com.nexora.service.LearningPathItemService;
import com.nexora.service.LearningPathService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 个性化学习路径落地组件（learning_path / learning_path_item 的唯一写入方）。
 *
 * 职责：
 * 1. **路径节点化**：把 AI 生成的主线 + 兴趣分支节点落成 path + item（节点粒度 = 知识点）；
 * 2. **自动建点**：节点主题在 knowledge_point 中没有同（学段 + 名称）记录时自动创建（幂等），
 *    因为 learning_path_item.knowledge_point_id 非空且知识库知识点稀少，缺了这步节点墙会空白；
 * 3. **三态由掌握度驱动**：{@link #listMyPaths} / {@link #getMyPath} 读取时按 knowledge_mastery 重算节点状态
 *    （2 已掌握 / 1 进行中 / 0 未解锁）与路径进度，并只持久化发生变化的节点。
 *
 * 解锁规则：主线中「学习类」节点按 sort 顺序串联——前一个已掌握才解锁下一个；兴趣分支节点与复习节点不参与解锁链，随时可开始。
 */
@Slf4j
@Component
public class LearningPathComponent {

    /** 节点/路径状态：未解锁 */
    public static final int ITEM_STATUS_LOCKED = 0;
    /** 节点状态：进行中 */
    public static final int ITEM_STATUS_LEARNING = 1;
    /** 节点状态：已掌握（与 knowledge_mastery.status 一致） */
    public static final int ITEM_STATUS_MASTERED = 2;

    /** 路径状态：进行中 */
    private static final int PATH_STATUS_RUNNING = 0;
    /** 路径状态：已完成 */
    private static final int PATH_STATUS_FINISHED = 1;
    /** 路径状态：已放弃 */
    private static final int PATH_STATUS_DROPPED = 2;

    /** 来源：AI 生成 */
    private static final int SOURCE_AI = 1;

    /** 主线 / 兴趣分支 */
    public static final int BRANCH_TYPE_MAIN = 0;
    public static final int BRANCH_TYPE_INTEREST = 1;

    /** 节点类型：学习 / 复习 */
    public static final int ITEM_TYPE_LEARN = 0;
    public static final int ITEM_TYPE_REVIEW = 1;

    /** 自动创建知识点的描述前缀（便于管理端识别来源） */
    private static final String AUTO_POINT_REMARK = "由学习路径自动创建";

    @Resource
    private LearningPathService learningPathService;

    @Resource
    private LearningPathItemService learningPathItemService;

    @Resource
    private KnowledgePointService knowledgePointService;

    @Resource
    private KnowledgeMasteryService knowledgeMasteryService;

    /** AI 生成的节点草稿 */
    public record NodeDraft(String title, String desc, String kind) {
    }

    /** AI 生成的兴趣分支草稿 */
    public record BranchDraft(String name, List<NodeDraft> nodes) {
    }

    /** 路径 + 节点（读取结果） */
    public record PathWithItems(LearningPath path, List<LearningPathItem> items) {
    }

    /**
     * 创建路径与节点：主线按顺序编号，分支节点排在其后（branch_name 分组）
     *
     * @return 新建路径ID
     */
    public String createPath(String userId, String stage, String title, List<NodeDraft> mainLine,
                             List<BranchDraft> branches) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("缺少用户标识");
        }
        if (StringTools.isEmpty(title)) {
            throw new BusinessException("路径标题不能为空");
        }
        if ((mainLine == null || mainLine.isEmpty()) && (branches == null || branches.isEmpty())) {
            throw new BusinessException("学习路径节点为空");
        }
        Date now = new Date();
        String pathId = newId();
        int sort = 0;
        List<LearningPathItem> items = new ArrayList<>();
        if (mainLine != null) {
            for (NodeDraft node : mainLine) {
                LearningPathItem item = buildItem(userId, stage, pathId, node, BRANCH_TYPE_MAIN, null, sort++, now);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        if (branches != null) {
            for (BranchDraft branch : branches) {
                if (branch == null || branch.nodes() == null || branch.nodes().isEmpty()) {
                    continue;
                }
                for (NodeDraft node : branch.nodes()) {
                    LearningPathItem item = buildItem(userId, stage, pathId, node, BRANCH_TYPE_INTEREST,
                            branch.name(), sort++, now);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        }
        if (items.isEmpty()) {
            throw new BusinessException("学习路径节点为空");
        }
        LearningPath path = new LearningPath();
        path.setPathId(pathId);
        path.setUserId(userId);
        path.setTitle(title.trim());
        path.setStage(stage);
        path.setSource(SOURCE_AI);
        path.setStatus(PATH_STATUS_RUNNING);
        path.setTotalItems(items.size());
        path.setFinishedItems(0);
        path.setProgress(0);
        path.setCurrentItemId(items.get(0).getItemId());
        path.setCreateTime(now);
        path.setUpdateTime(now);
        learningPathService.add(path);
        learningPathItemService.addBatch(items);
        log.info("学习路径已创建 userId={} pathId={} 节点数={}", userId, pathId, items.size());
        return pathId;
    }

    /**
     * 我的路径（含节点）：读取时按掌握度刷新状态与进度
     */
    public List<PathWithItems> listMyPaths(String userId) {
        KnowledgeMasteryQuery masteryQuery = new KnowledgeMasteryQuery();
        masteryQuery.setUserId(userId);
        Map<String, KnowledgeMastery> masteryMap = indexMastery(
                knowledgeMasteryService.findListByParam(masteryQuery));

        LearningPathQuery pathQuery = new LearningPathQuery();
        pathQuery.setUserId(userId);
        pathQuery.setOrderBy("create_time desc");
        List<LearningPath> paths = learningPathService.findListByParam(pathQuery);
        if (paths == null || paths.isEmpty()) {
            return new ArrayList<>();
        }
        // 一次查出该生全部节点，按路径分组（避免按路径循环查库）
        Map<String, List<LearningPathItem>> itemMap = loadItemsByPath(userId);
        List<PathWithItems> result = new ArrayList<>();
        for (LearningPath path : paths) {
            List<LearningPathItem> items = itemMap.getOrDefault(path.getPathId(), new ArrayList<>());
            result.add(new PathWithItems(refresh(userId, path, items, masteryMap), items));
        }
        return result;
    }

    /**
     * 单条路径（含节点），归属校验 + 掌握度刷新
     */
    public PathWithItems getMyPath(String userId, String pathId) {
        LearningPath path = requireOwnedPath(userId, pathId);
        LearningPathItemQuery itemQuery = new LearningPathItemQuery();
        itemQuery.setPathId(pathId);
        itemQuery.setOrderBy("sort asc");
        List<LearningPathItem> items = learningPathItemService.findListByParam(itemQuery);
        KnowledgeMasteryQuery masteryQuery = new KnowledgeMasteryQuery();
        masteryQuery.setUserId(userId);
        Map<String, KnowledgeMastery> masteryMap = indexMastery(
                knowledgeMasteryService.findListByParam(masteryQuery));
        return new PathWithItems(refresh(userId, path, items, masteryMap), items);
    }

    /**
     * 删除路径（级联删除节点）
     */
    public void deletePath(String userId, String pathId) {
        requireOwnedPath(userId, pathId);
        LearningPathItemQuery itemQuery = new LearningPathItemQuery();
        itemQuery.setPathId(pathId);
        learningPathItemService.deleteByParam(itemQuery);
        learningPathService.deleteLearningPathByPathId(pathId);
        log.info("学习路径已删除 userId={} pathId={}", userId, pathId);
    }

    public LearningPath requireOwnedPath(String userId, String pathId) {
        if (StringTools.isEmpty(pathId)) {
            throw new BusinessException("路径ID不能为空");
        }
        LearningPath path = learningPathService.getLearningPathByPathId(pathId);
        if (path == null || !userId.equals(path.getUserId())) {
            throw new BusinessException("学习路径不存在或无权操作");
        }
        return path;
    }

    // ==================== 内部实现 ====================

    private LearningPathItem buildItem(String userId, String stage, String pathId, NodeDraft node,
                                       int branchType, String branchName, int sort, Date now) {
        if (node == null || StringTools.isEmpty(node.title())) {
            return null;
        }
        String name = node.title().trim();
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        String knowledgePointId = resolveOrCreatePoint(stage, name, node.desc());
        LearningPathItem item = new LearningPathItem();
        item.setItemId(newId());
        item.setPathId(pathId);
        item.setUserId(userId);
        item.setKnowledgePointId(knowledgePointId);
        item.setKnowledgePointName(name);
        item.setBranchType(branchType);
        item.setBranchName(StringTools.isEmpty(branchName) ? null : branchName.trim());
        // AI 的 review 视为遗忘曲线复习节点，learn / practice 归为学习节点（练习由节点快测承载）
        item.setItemType("review".equalsIgnoreCase(node.kind()) ? ITEM_TYPE_REVIEW : ITEM_TYPE_LEARN);
        item.setStatus(ITEM_STATUS_LOCKED);
        item.setSort(sort);
        item.setCreateTime(now);
        item.setUpdateTime(now);
        return item;
    }

    /**
     * 取知识点：同（学段 + 名称）已存在则复用，否则新建（幂等，名称精确匹配）
     */
    private String resolveOrCreatePoint(String stage, String name, String learningTip) {
        KnowledgePointQuery query = new KnowledgePointQuery();
        query.setStage(stage);
        query.setName(name);
        query.setOrderBy("sort asc, create_time asc");
        List<KnowledgePoint> existing = knowledgePointService.findListByParam(query);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0).getKnowledgePointId();
        }
        Date now = new Date();
        KnowledgePoint point = new KnowledgePoint();
        point.setKnowledgePointId(newId());
        point.setName(name);
        point.setStage(stage);
        point.setDescription(trimDescription(AUTO_POINT_REMARK
                + (StringTools.isEmpty(learningTip) ? "" : "｜学习建议：" + learningTip.trim())));
        point.setStatus(1);
        point.setCreateTime(now);
        point.setUpdateTime(now);
        knowledgePointService.add(point);
        log.info("学习路径自动创建知识点 stage={} name={} pointId={}", stage, name, point.getKnowledgePointId());
        return point.getKnowledgePointId();
    }

    private String trimDescription(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }

    private Map<String, KnowledgeMastery> indexMastery(List<KnowledgeMastery> list) {
        Map<String, KnowledgeMastery> map = new HashMap<>();
        if (list != null) {
            for (KnowledgeMastery mastery : list) {
                map.put(mastery.getKnowledgePointId(), mastery);
            }
        }
        return map;
    }

    private Map<String, List<LearningPathItem>> loadItemsByPath(String userId) {
        LearningPathItemQuery query = new LearningPathItemQuery();
        query.setUserId(userId);
        query.setOrderBy("sort asc");
        List<LearningPathItem> all = learningPathItemService.findListByParam(query);
        Map<String, List<LearningPathItem>> map = new LinkedHashMap<>();
        if (all != null) {
            for (LearningPathItem item : all) {
                map.computeIfAbsent(item.getPathId(), key -> new ArrayList<>()).add(item);
            }
        }
        return map;
    }

    /**
     * 按掌握度重算节点三态与路径进度，只持久化发生变化的节点
     */
    private LearningPath refresh(String userId, LearningPath path, List<LearningPathItem> items,
                                 Map<String, KnowledgeMastery> masteryMap) {
        List<LearningPathItem> ordered = new ArrayList<>(items);
        ordered.sort(Comparator.comparing(item -> item.getSort() == null ? 0 : item.getSort()));
        boolean previousMainMastered = true;
        int finished = 0;
        String currentItemId = null;
        String currentItemName = null;
        Date now = new Date();
        for (LearningPathItem item : ordered) {
            KnowledgeMastery mastery = masteryMap.get(item.getKnowledgePointId());
            boolean mastered = mastery != null && mastery.getStatus() != null
                    && mastery.getStatus() == ITEM_STATUS_MASTERED;
            boolean inMainChain = isInUnlockChain(item);
            int newStatus;
            if (mastered) {
                newStatus = ITEM_STATUS_MASTERED;
            } else if (inMainChain && !previousMainMastered) {
                newStatus = ITEM_STATUS_LOCKED;
            } else {
                newStatus = ITEM_STATUS_LEARNING;
            }
            if (inMainChain) {
                previousMainMastered = previousMainMastered && mastered;
            }
            if (newStatus == ITEM_STATUS_MASTERED) {
                finished++;
            } else if (newStatus == ITEM_STATUS_LEARNING && currentItemId == null) {
                currentItemId = item.getItemId();
                currentItemName = item.getKnowledgePointName();
            }
            Integer oldStatus = item.getStatus();
            if (oldStatus == null || oldStatus != newStatus) {
                LearningPathItem update = new LearningPathItem();
                update.setStatus(newStatus);
                update.setUpdateTime(now);
                if (newStatus == ITEM_STATUS_MASTERED) {
                    update.setFinishTime(item.getFinishTime() == null ? now : item.getFinishTime());
                }
                learningPathItemService.updateLearningPathItemByItemId(update, item.getItemId());
                item.setStatus(newStatus);
                if (update.getFinishTime() != null) {
                    item.setFinishTime(update.getFinishTime());
                }
            }
        }
        int total = ordered.size();
        int progress = total == 0 ? 0 : (int) Math.round(finished * 100.0 / total);
        boolean allMainMastered = isAllMainLineMastered(ordered);
        Integer pathStatus = path.getStatus();
        int expectedStatus = pathStatus != null && pathStatus == PATH_STATUS_DROPPED
                ? PATH_STATUS_DROPPED
                : (allMainMastered ? PATH_STATUS_FINISHED : PATH_STATUS_RUNNING);
        boolean changed = !equalsInt(path.getFinishedItems(), finished)
                || !equalsInt(path.getProgress(), progress)
                || !equalsInt(path.getTotalItems(), total)
                || !equalsInt(path.getStatus(), expectedStatus)
                || !equalsStr(path.getCurrentItemId(), currentItemId);
        if (changed) {
            LearningPath update = new LearningPath();
            update.setTotalItems(total);
            update.setFinishedItems(finished);
            update.setProgress(progress);
            update.setStatus(expectedStatus);
            // current_item_id 为 null 时不写（<if> 更新语义写不进 null），仅在有值时更新
            if (!StringTools.isEmpty(currentItemId)) {
                update.setCurrentItemId(currentItemId);
            }
            update.setUpdateTime(now);
            learningPathService.updateLearningPathByPathId(update, path.getPathId());
            path.setTotalItems(total);
            path.setFinishedItems(finished);
            path.setProgress(progress);
            path.setStatus(expectedStatus);
            if (!StringTools.isEmpty(currentItemId)) {
                path.setCurrentItemId(currentItemId);
            }
        }
        path.setUpdateTime(path.getUpdateTime() == null ? now : path.getUpdateTime());
        return path;
    }

    /**
     * 主线学习节点是否全部掌握（没有主线节点时视为未完成）
     */
    private boolean isAllMainLineMastered(List<LearningPathItem> items) {
        boolean hasMain = false;
        for (LearningPathItem item : items) {
            if (!isInUnlockChain(item)) {
                continue;
            }
            hasMain = true;
            if (item.getStatus() == null || item.getStatus() != ITEM_STATUS_MASTERED) {
                return false;
            }
        }
        return hasMain;
    }

    /**
     * 是否参与解锁链：主线节点按 sort 顺序串联解锁；兴趣分支节点不参与；
     * 遗忘曲线「到期复习节点」（带 due_date）不参与——到期复习应随时可做。
     */
    private boolean isInUnlockChain(LearningPathItem item) {
        boolean main = item.getBranchType() != null && item.getBranchType() == BRANCH_TYPE_MAIN;
        return main && item.getDueDate() == null;
    }

    private boolean equalsInt(Integer left, Integer right) {
        return left == null ? right == null : left.equals(right);
    }

    private boolean equalsStr(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
