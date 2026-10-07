package com.nexora.service;

import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseEnrollment;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.CourseStudyLessonProgress;
import com.nexora.component.LearningPathComponent;
import com.nexora.entity.po.PracticeRecord;
import com.nexora.entity.query.PracticeRecordQuery;
import com.nexora.service.PracticeRecordService;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.LearningPath;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.po.StudentLearningRecord;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseEnrollmentQuery;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.query.CourseStudyLessonProgressQuery;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.query.StudentLearningRecordQuery;
import com.nexora.entity.vo.KnowledgeMasteryVO;
import com.nexora.mappers.LearningAnalysisMapper;
import com.nexora.service.CourseChapterLessonService;
import com.nexora.service.CourseEnrollmentService;
import com.nexora.service.CourseInfoService;
import com.nexora.service.CourseStudyLessonProgressService;
import com.nexora.service.KnowledgePointService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentLearningRecordService;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StageNormalizer;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 教学工具服务（教学域白名单，见 docs/开发流程.md 7.19）：
 * 只读查询 4 组 + 学习记录写入 1 个；所有工具返回结构化字符串，内部 try-catch 不抛未捕获异常。
 * 安全约束：写工具必须校验用户存在；不提供任意 SQL / 文件 / 系统操作。
 */
@Service
public class TeachingToolService {

    private static final Logger log = LoggerFactory.getLogger(TeachingToolService.class);

    /** 学习行为类型白名单（student_learning_record.action_type）；AI_CHAT 为对话场景预留（web 端尚未写入） */
    private static final List<String> ACTION_TYPES = List.of("VIEW", "COMPLETE", "PRACTICE", "ANIMATION", "PARSE", "AI_CHAT");

    /** 课程/资源列表单次返回上限 */
    private static final int LIST_LIMIT = 20;

    /**
     * 学段入参归一化：模型可能传"高中/高二/senior"等表达，统一为 StageEnum 编码；
     * 入参非空但无法识别时返回以"无法识别"开头的错误提示（含有效编码），识别成功返回编码，空返回 null（不过滤）
     */
    private String normalizeStageOrError(String raw) {
        if (StringTools.isEmpty(raw)) {
            return null;
        }
        String code = StageNormalizer.normalize(raw);
        if (code == null) {
            return "无法识别的学段：" + raw + "（有效：" + StageNormalizer.validCodes() + "）";
        }
        return code;
    }

    /** 学习路径（二期 7.60 第二批：路径类工具复用 common 的组件，不新写 SQL） */
    /** 练习记录（计划 C5：逐题复盘的数据源） */
    @Resource
    private PracticeRecordService practiceRecordService;

    @Resource
    private LearningPathComponent learningPathComponent;

    @Resource
    private KnowledgePointService knowledgePointService;

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private LearningAnalysisMapper learningAnalysisMapper;

    @Resource
    private StudentLearningRecordService studentLearningRecordService;

    @Resource
    private CourseEnrollmentService courseEnrollmentService;

    @Resource
    private CourseStudyLessonProgressService courseStudyLessonProgressService;

    @Tool(name = "queryCourse", description = "查询课程清单（含年级/学科/难度/课时数/学习人数）。"
            + "默认返回**全部学段**的上架课程；用户问「我加入的课程 / 我的课程 / 我学过哪些课」时传 mine=true，"
            + "返回该学生已加入的全部课程（跨学段，含学习进度）；只有用户明确提到年级或学段（如「三年级的课」）才传 stage。")
    public String queryCourse(
            @ToolParam(description = "是否只查当前学生已加入的课程（跨学段）；用户问「我的课程」时传 true，可空") Boolean mine,
            @ToolParam(description = "学段编码过滤，可空；仅在用户明确提到年级/学段时传，支持高中/初中等中文，自动归一化") String stage,
            @ToolParam(description = "课程名关键词，可空") String keyword,
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId) {
        try {
            String stageCode = normalizeStageOrError(stage);
            if (stageCode != null && stageCode.startsWith("无法识别")) {
                return stageCode;
            }
            if (Boolean.TRUE.equals(mine)) {
                return queryMyCourses(userId, keyword, stageCode);
            }
            CourseInfoQuery query = new CourseInfoQuery();
            query.setStatus(1);
            if (stageCode != null) {
                query.setStage(stageCode);
            }
            if (!StringTools.isEmpty(keyword)) {
                query.setCourseNameFuzzy(keyword.trim());
            }
            query.setOrderBy("sort asc");
            List<CourseInfo> list = courseInfoService.findListByParam(query);
            if (list.isEmpty()) {
                return "未找到匹配的课程";
            }
            int limit = Math.min(list.size(), LIST_LIMIT);
            StringBuilder sb = new StringBuilder("课程列表（共 ").append(list.size())
                    .append(" 门，最多展示 ").append(limit).append(" 门）：\n");
            for (int i = 0; i < limit; i++) {
                CourseInfo course = list.get(i);
                sb.append(i + 1).append(". ").append(course.getCourseName())
                        .append("（年级:").append(course.getGrade() == null ? "-" : course.getGrade())
                        .append("，学科:").append(course.getSubject() == null ? "-" : course.getSubject())
                        .append("，难度:").append(course.getDifficulty() == null ? "-" : course.getDifficulty()).append("星")
                        .append("，课时:").append(course.getLessonCount() == null ? 0 : course.getLessonCount())
                        .append("，学习人数:").append(course.getStudyCount() == null ? 0 : course.getStudyCount())
                        .append("，课程ID:").append(course.getCourseId());
                if (!StringTools.isEmpty(course.getDescription())) {
                    String desc = course.getDescription();
                    sb.append("，简介:").append(desc.length() > 40 ? desc.substring(0, 40) + "…" : desc);
                }
                sb.append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryCourse 失败", e);
            return "查询课程失败：" + e.getMessage();
        }
    }

    /**
     * 我加入的课程：按 course_enrollment 取该学生已加入的全部课程（**跨学段**）。
     *
     * 口径与「我的课程」页面一致（2026-10-04 起，见开发流程 7.49）：学生升级 / 换年级后仍能看到并学习
     * 此前加入的课程，所以这里**不按当前学段过滤**；只有用户明确提到年级/学段时才用 stage 过滤。
     * 进度取 course_study_progress（一次批量取回，不在循环里查库）。
     */
    private String queryMyCourses(String userId, String keyword, String stageCode) {
        if (StringTools.isEmpty(userId)) {
            return "未获取到学生身份，无法查询已加入的课程";
        }
        CourseEnrollmentQuery enrollQuery = new CourseEnrollmentQuery();
        enrollQuery.setUserId(userId.trim());
        enrollQuery.setStatus(1);
        List<String> courseIds = courseEnrollmentService.findListByParam(enrollQuery).stream()
                .map(CourseEnrollment::getCourseId)
                .filter(id -> !StringTools.isEmpty(id))
                .distinct()
                .toList();
        if (courseIds.isEmpty()) {
            return "该学生还没有加入任何课程（可在「课程教材」页选择想学的课程加入）";
        }
        CourseInfoQuery query = new CourseInfoQuery();
        query.setCourseIds(new java.util.ArrayList<>(courseIds));
        query.setStatus(1);
        if (stageCode != null) {
            query.setStage(stageCode);
        }
        if (!StringTools.isEmpty(keyword)) {
            query.setCourseNameFuzzy(keyword.trim());
        }
        query.setOrderBy("sort asc");
        List<CourseInfo> list = courseInfoService.findListByParam(query);
        if (list.isEmpty()) {
            return stageCode == null
                    ? "该学生已加入的课程里没有匹配的课程"
                    : "该学生已加入的课程里没有「" + stageCode + "」学段的课程（其它学段的课程可在不指定学段时查看）";
        }
        Map<String, Integer> finishedCountMap = new HashMap<>();
        CourseStudyLessonProgressQuery lessonProgressQuery = new CourseStudyLessonProgressQuery();
        lessonProgressQuery.setUserId(userId.trim());
        lessonProgressQuery.setFinished(1);
        for (CourseStudyLessonProgress progress : courseStudyLessonProgressService.findListByParam(lessonProgressQuery)) {
            finishedCountMap.merge(progress.getCourseId(), 1, Integer::sum);
        }
        int limit = Math.min(list.size(), LIST_LIMIT);
        StringBuilder sb = new StringBuilder("该学生已加入的课程（共 ").append(list.size())
                .append(" 门，跨学段全部展示，最多列出 ").append(limit).append(" 门）：\n");
        for (int i = 0; i < limit; i++) {
            CourseInfo course = list.get(i);
            sb.append(i + 1).append(". ").append(course.getCourseName())
                    .append("（年级:").append(course.getGrade() == null ? "-" : course.getGrade())
                    .append("，学段:").append(course.getStage() == null ? "-" : course.getStage())
                    .append("，学科:").append(course.getSubject() == null ? "-" : course.getSubject())
                    .append("，难度:").append(course.getDifficulty() == null ? "-" : course.getDifficulty()).append("星")
                    .append("，已学:")
                    .append(finishedCountMap.getOrDefault(course.getCourseId(), 0)).append("/")
                    .append(course.getLessonCount() == null ? 0 : course.getLessonCount())
                    .append(" 课时，课程ID:").append(course.getCourseId());
            if (!StringTools.isEmpty(course.getDescription())) {
                String desc = course.getDescription();
                sb.append("，简介:").append(desc.length() > 40 ? desc.substring(0, 40) + "…" : desc);
            }
            sb.append("）\n");
        }
        return sb.toString();
    }

    /**
     * 学生是否已加入该课程（跨学段可见性判定用，口径与「我的课程」页面一致：2026-10-04 起
     * 已加入的课程不再受当前学段限制）。
     */
    private boolean isEnrolled(String userId, String courseId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(courseId)) {
            return false;
        }
        CourseEnrollmentQuery query = new CourseEnrollmentQuery();
        query.setUserId(userId.trim());
        query.setCourseId(courseId.trim());
        query.setStatus(1);
        return !courseEnrollmentService.findListByParam(query).isEmpty();
    }

    @Tool(name = "queryLesson", description = "按课程ID查询课时列表（含摘要/视频时长），或按课时ID查课时详情。"
            + "学生**已加入**的课程不受学段限制（跨学段也能看课时）。")
    public String queryLesson(
            @ToolParam(description = "课程ID，可空") String courseId,
            @ToolParam(description = "课时ID，可空") String lessonId,
            @ToolParam(description = "学段编码，由系统自动注入") String stage,
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId) {
        try {
            String stageCode = normalizeStageOrError(stage);
            if (stageCode != null && stageCode.startsWith("无法识别")) {
                return stageCode;
            }
            if (!StringTools.isEmpty(lessonId)) {
                CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(lessonId.trim());
                if (lesson == null) {
                    return "课时不存在";
                }
                CourseInfo course = courseInfoService.getCourseInfoByCourseId(lesson.getCourseId());
                if (stageCode != null && course != null && !stageCode.equals(course.getStage())
                        && !isEnrolled(userId, course.getCourseId())) {
                    return "该课时所属课程不属于当前学段";
                }
                StringBuilder sb = new StringBuilder("课时：").append(lesson.getLessonName())
                        .append("（ID:").append(lesson.getLessonId())
                        .append("，所属课程:").append(course == null ? "-" : course.getCourseName())
                        .append("，章节:").append(lesson.getChapterId());
                if (lesson.getVideoDuration() != null && lesson.getVideoDuration() > 0) {
                    sb.append("，视频时长:").append(lesson.getVideoDuration()).append("秒");
                }
                if (!StringTools.isEmpty(lesson.getSummary())) {
                    String summary = lesson.getSummary();
                    sb.append("，摘要:").append(summary.length() > 60 ? summary.substring(0, 60) + "…" : summary);
                }
                return sb.append("）").toString();
            }
            if (StringTools.isEmpty(courseId)) {
                return "请提供课程ID或课时ID";
            }
            CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId.trim());
            if (course == null) {
                return "课程不存在：" + courseId;
            }
            if (stageCode != null && !stageCode.equals(course.getStage())
                    && !isEnrolled(userId, course.getCourseId())) {
                return "该课程不属于当前学段";
            }
            CourseChapterLessonQuery query = new CourseChapterLessonQuery();
            query.setCourseId(courseId.trim());
            // 课时状态 DDL 语义：0=正常 1=停用（与课程表相反，勿改）
            query.setStatus(0);
            query.setOrderBy("sort asc");
            List<CourseChapterLesson> list = courseChapterLessonService.findListByParam(query);
            if (list.isEmpty()) {
                return "该课程暂无课时";
            }
            int limit = Math.min(list.size(), LIST_LIMIT);
            StringBuilder sb = new StringBuilder("《").append(course.getCourseName()).append("》课时列表（共 ")
                    .append(list.size()).append(" 节，最多展示 ").append(limit).append(" 节）：\n");
            for (int i = 0; i < limit; i++) {
                CourseChapterLesson lesson = list.get(i);
                sb.append(i + 1).append(". ").append(lesson.getLessonName())
                        .append("（ID:").append(lesson.getLessonId());
                if (lesson.getVideoDuration() != null && lesson.getVideoDuration() > 0) {
                    sb.append("，视频:").append(lesson.getVideoDuration()).append("秒");
                }
                sb.append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryLesson 失败", e);
            return "查询课时失败：" + e.getMessage();
        }
    }

    @Tool(name = "recommendResource", description = "推荐官方学习资源卡片（按学段/知识点/类型，"
            + "类型：VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK/ANIMATION）")
    public String recommendResource(
            @ToolParam(description = "学段编码，可空；支持高中/初中等中文，自动归一化") String stage,
            @ToolParam(description = "知识点ID，可空") String knowledgePointId,
            @ToolParam(description = "资源类型，可空") String type) {
        try {
            String stageCode = normalizeStageOrError(stage);
            if (stageCode != null && stageCode.startsWith("无法识别")) {
                return stageCode;
            }
            ResourceInfoQuery query = new ResourceInfoQuery();
            query.setOwnerIdNull(Boolean.TRUE);
            query.setStatus(1);
            if (stageCode != null) {
                query.setStage(stageCode);
            }
            if (!StringTools.isEmpty(knowledgePointId)) {
                query.setKnowledgePointId(knowledgePointId.trim());
            }
            if (!StringTools.isEmpty(type)) {
                query.setResourceType(type.trim().toUpperCase());
            }
            query.setOrderBy("create_time desc");
            List<ResourceInfo> list = resourceInfoService.findListByParam(query);
            if (list.isEmpty()) {
                return "未找到匹配的资源";
            }
            StringBuilder sb = new StringBuilder("推荐资源：\n");
            int index = 1;
            int limit = Math.min(list.size(), 10);
            for (int i = 0; i < limit; i++) {
                ResourceInfo resource = list.get(i);
                sb.append(index++).append(". ").append(resource.getResourceName())
                        .append("（类型:").append(resource.getResourceType())
                        .append("，ID:").append(resource.getResourceId());
                if (!StringTools.isEmpty(resource.getDescription())) {
                    sb.append("，简介:").append(resource.getDescription());
                }
                sb.append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("recommendResource 失败", e);
            return "查询资源失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryMastery", description = "查询学生知识点掌握度概览（按学生登录学段过滤，含状态分布与正确率）")
    public String queryMastery(
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId,
            @ToolParam(description = "学段编码，由系统自动注入") String stage) {
        try {
            if (StringTools.isEmpty(userId)) {
                return "参数错误：缺少学生ID";
            }
            String stageCode = normalizeStageOrError(stage);
            if (stageCode != null && stageCode.startsWith("无法识别")) {
                return stageCode;
            }
            List<KnowledgeMasteryVO> list = stageCode == null
                    ? learningAnalysisMapper.selectMasteryList(userId.trim())
                    : learningAnalysisMapper.selectMasteryListByStage(userId.trim(), stageCode);
            String stageFallbackNote = "";
            if (list.isEmpty() && stageCode != null) {
                // 登录学段查不到时回落到全部学段（2026-10-07 修复）：
                // 学生可能正在做**别的学段**的学习路径（例：初一学生做「高中」路线，掌握度记录 stage=SENIOR），
                // 只按登录学段查会空 → 回一句"暂无掌握度数据"，模型如实转述成"平台没有你的数据"，反而误导学生。
                List<KnowledgeMasteryVO> allStage = learningAnalysisMapper.selectMasteryList(userId.trim());
                if (!allStage.isEmpty()) {
                    list = allStage;
                    stageFallbackNote = "注意：按登录学段（" + stageCode + "）没有掌握度记录，"
                            + "以下是你" + "*" + "*全部学段" + "*" + "*的掌握度数据（可能来自其它学段的课程或学习路径）。" + "\n";
                }
            }
            if (list.isEmpty()) {
                return "该学生暂无掌握度数据";
            }
            int mastered = 0;
            int inProgress = 0;
            int locked = 0;
            int totalScore = 0;
            StringBuilder sb = new StringBuilder();
            int index = 1;
            for (KnowledgeMasteryVO item : list) {
                totalScore += item.getMasteryScore() == null ? 0 : item.getMasteryScore();
                int status = item.getStatus() == null ? 0 : item.getStatus();
                if (status == 2) {
                    mastered++;
                } else if (status == 1) {
                    inProgress++;
                } else {
                    locked++;
                }
                if (index <= 10) {
                    sb.append(index++).append(". ").append(item.getKnowledgePointName() == null ? "知识点" : item.getKnowledgePointName())
                            .append(" 得分:").append(item.getMasteryScore() == null ? 0 : item.getMasteryScore())
                            .append("（练习").append(item.getPracticeCount() == null ? 0 : item.getPracticeCount())
                            .append("次 正确率:").append(item.getAccuracy() == null ? 0 : item.getAccuracy()).append("%）\n");
                }
            }
            return stageFallbackNote + "知识点共 " + list.size() + " 个：已掌握 " + mastered + "、进行中 " + inProgress
                    + "、未解锁 " + locked + "，平均分 " + totalScore / list.size() + "。" + "\n" + sb;
        } catch (Exception e) {
            log.warn("queryMastery 失败", e);
            return "查询掌握度失败：" + e.getMessage();
        }
    }

    @Tool(name = "saveLearningRecord", description = "记录学生学习行为（VIEW/COMPLETE/PRACTICE/ANIMATION/PARSE/AI_CHAT），"
            + "仅在用户明确要求记录学习行为时调用；写入前校验学生存在，targetId 自动识别资源/课程/课时")
    public String saveLearningRecord(
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId,
            @ToolParam(description = "行为对象ID（资源/课程/课时ID，自动识别类型），可空") String targetId,
            @ToolParam(description = "行为类型：VIEW/COMPLETE/PRACTICE/ANIMATION/PARSE/AI_CHAT") String actionType,
            @ToolParam(description = "学习时长（秒），可空默认0") Integer duration) {
        try {
            if (StringTools.isEmpty(userId) || StringTools.isEmpty(actionType)) {
                return "参数错误：缺少学生ID或行为类型";
            }
            String type = actionType.trim().toUpperCase();
            if (!ACTION_TYPES.contains(type)) {
                return "非法的行为类型：" + actionType + "（允许：" + ACTION_TYPES + "）";
            }
            UserInfo user = userInfoService.getUserInfoByUserId(userId.trim());
            if (user == null) {
                return "学生不存在：" + userId;
            }
            StudentLearningRecord record = new StudentLearningRecord();
            record.setUserId(user.getUserId());
            record.setActionType(type);
            record.setDuration(duration == null || duration < 0 ? 0 : Math.min(duration, 86400));
            record.setCreateTime(new Date());
            String targetLog;
            if (!StringTools.isEmpty(targetId)) {
                String target = targetId.trim();
                // 按资源/课程/课时顺序识别目标类型
                if (resourceInfoService.getResourceInfoByResourceId(target) != null) {
                    record.setResourceId(target);
                    targetLog = "资源 " + target;
                } else if (courseInfoService.getCourseInfoByCourseId(target) != null) {
                    record.setCourseId(target);
                    targetLog = "课程 " + target;
                } else if (courseChapterLessonService.getCourseChapterLessonByLessonId(target) != null) {
                    record.setLessonId(target);
                    targetLog = "课时 " + target;
                } else {
                    return "目标对象不存在：" + targetId;
                }
            } else {
                targetLog = "无目标对象";
            }
            studentLearningRecordService.add(record);
            log.info("MCP 学习行为记录成功 userId={} type={} target={} duration={}s",
                    user.getUserId(), type, targetLog, record.getDuration());
            return "学习行为已记录（" + type + (record.getRecordId() == null ? "" : "，记录ID:" + record.getRecordId()) + "）";
        } catch (Exception e) {
            log.warn("saveLearningRecord 失败", e);
            return "记录学习行为失败：" + e.getMessage();
        }
    }

    // ==================== 学习路径工具（二期 7.60 第二批）====================

    @Tool(name = "queryLearningPath", description = "查询学生的全部学习路径：每条路径的标题、进行状态、进度、当前该学的节点，"
            + "以及还没掌握的节点状态（进行中/未解锁）。当学生问「我的学习路径是什么 / 我学到哪了 / 下一步学哪个节点」时先调用本工具。")
    public String queryLearningPath(
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId,
            @ToolParam(description = "学段编码，由系统自动注入") String stage) {
        try {
            if (StringTools.isEmpty(userId)) {
                return "参数错误：缺少学生ID";
            }
            List<LearningPathComponent.PathWithItems> paths = learningPathComponent.listMyPaths(userId.trim());
            if (paths.isEmpty()) {
                return "该学生还没有学习路径（可在学生端「学习路径」页生成一条）";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("共 ").append(paths.size()).append(" 条学习路径：\n");
            for (LearningPathComponent.PathWithItems each : paths) {
                LearningPath path = each.path();
                sb.append("\n【").append(path.getTitle()).append("】")
                        .append(path.getStatus() != null && path.getStatus() == 1 ? "（已完成）" : "（进行中）")
                        .append(" 进度 ").append(path.getProgress() == null ? 0 : path.getProgress()).append("%")
                        .append("，节点 ").append(path.getFinishedItems() == null ? 0 : path.getFinishedItems())
                        .append("/").append(path.getTotalItems() == null ? 0 : path.getTotalItems())
                        .append("；pathId=").append(path.getPathId()).append("\n");
                List<LearningPathItem> items = each.items() == null ? List.<LearningPathItem>of() : each.items();
                int index = 1;
                for (LearningPathItem item : items) {
                    if (item.getStatus() != null && item.getStatus() == 2) {
                        continue;
                    }
                    if (index > 6) {
                        sb.append("  …（其余节点见学生端）\n");
                        break;
                    }
                    sb.append("  ").append(index++).append(". ").append(item.getKnowledgePointName())
                            .append(item.getStatus() != null && item.getStatus() == 0 ? "（未解锁）" : "（进行中）");
                    if (item.getDueDate() != null) {
                        sb.append("，计划完成 ").append(item.getDueDate());
                    }
                    sb.append("；itemId=").append(item.getItemId()).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryLearningPath 失败", e);
            return "查询学习路径失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryPathNode", description = "查询某个学习路径节点的详情：节点状态、是否已到复习时间、计划/完成时间、本节点任务，"
            + "以及该知识点的掌握度、练习次数与下次复习时间。学生问「我这个节点学得怎么样 / 要不要复习某知识点」时调用。")
    public String queryPathNode(
            @ToolParam(description = "节点名或其中关键词（与知识点名一致）") String nodeName,
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId) {
        try {
            if (StringTools.isEmpty(userId) || StringTools.isEmpty(nodeName)) {
                return "参数错误：缺少学生ID或节点名";
            }
            String keyword = nodeName.trim();
            List<LearningPathComponent.PathWithItems> paths = learningPathComponent.listMyPaths(userId.trim());
            LearningPathItem hit = null;
            String hitPathTitle = null;
            for (LearningPathComponent.PathWithItems each : paths) {
                List<LearningPathItem> items = each.items() == null ? List.<LearningPathItem>of() : each.items();
                for (LearningPathItem item : items) {
                    String itemName = item.getKnowledgePointName() == null ? "" : item.getKnowledgePointName();
                    if (!itemName.isBlank() && (itemName.contains(keyword) || keyword.contains(itemName))) {
                        hit = item;
                        hitPathTitle = each.path().getTitle();
                        break;
                    }
                }
                if (hit != null) {
                    break;
                }
            }
            if (hit == null) {
                return "没有找到名为「" + keyword + "」的路径节点，可以先用 queryLearningPath 看学生的全部节点";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("节点《").append(hit.getKnowledgePointName()).append("》");
            if (hitPathTitle != null) {
                sb.append("（属于路径《").append(hitPathTitle).append("》）");
            }
            sb.append("\n- 状态：")
                    .append(hit.getStatus() == null ? "未知"
                            : (hit.getStatus() == 2 ? "已掌握" : hit.getStatus() == 0 ? "未解锁" : "进行中"))
                    .append("\n- 计划完成时间：")
                    .append(hit.getDueDate() == null ? "未设置"
                            : (DateUtil.format(hit.getDueDate(), DateTimePatternEnum.YYYY_MM_DD.getPattern())
                            + (hit.getDueDate().compareTo(new java.util.Date()) <= 0 ? "（已逾期）" : "")));
            if (hit.getDueDate() != null) {
                long overdueDays = (System.currentTimeMillis() - hit.getDueDate().getTime()) / 86400000L;
                if (overdueDays > 0) {
                    sb.append("，逾期 ").append(overdueDays).append(" 天");
                }
            }
            if (hit.getFinishTime() != null) {
                sb.append("\n- 实际完成：").append(hit.getFinishTime());
            }
            // 最近练习摘要（计划 C5-2）：让"查节点"这一步就带上答题情况，学生不必再专门问一次
            try {
                PracticeRecordQuery recentQuery = new PracticeRecordQuery();
                recentQuery.setUserId(userId.trim());
                recentQuery.setBizId(hit.getItemId());
                recentQuery.setPageSize(20);
                List<PracticeRecord> recent = practiceRecordService.findListByParam(recentQuery);
                if (recent != null && !recent.isEmpty()) {
                    int wrong = 0;
                    for (PracticeRecord record : recent) {
                        if (record.getIsCorrect() != null && record.getIsCorrect() == 0) {
                            wrong++;
                        }
                    }
                    sb.append("\n" + "- 最近练习：共 " + recent.size() + " 条作答，错 " + wrong + " 条"
                            + (wrong > 0 ? "（逐题明细见 queryNodeQuizRecords）" : ""));
                }
            } catch (Exception ignored) {
                // 练习摘要取不到不影响节点详情
            }
            List<KnowledgeMasteryVO> masteryList = learningAnalysisMapper.selectMasteryList(userId.trim());
            for (KnowledgeMasteryVO mastery : masteryList) {
                if (hit.getKnowledgePointId() != null && hit.getKnowledgePointId().equals(mastery.getKnowledgePointId())) {
                    sb.append("\n- 掌握度：").append(mastery.getMasteryScore() == null ? 0 : mastery.getMasteryScore())
                            .append("，已练习 ").append(mastery.getPracticeCount() == null ? 0 : mastery.getPracticeCount())
                            .append(" 次，正确率 ").append(mastery.getAccuracy() == null ? 0 : mastery.getAccuracy()).append("%");
                    if (mastery.getNextReviewTime() != null) {
                        long overdueDays = (System.currentTimeMillis() - mastery.getNextReviewTime().getTime()) / 86400000L;
                        sb.append("，下次复习 ")
                                .append(DateUtil.format(mastery.getNextReviewTime(), DateTimePatternEnum.YYYY_MM_DD.getPattern()));
                        if (overdueDays > 0) {
                            sb.append("（**已逾期 ").append(overdueDays).append(" 天，建议现在复习**）");
                        }
                    }
                    break;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryPathNode 失败", e);
            return "查询节点失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryNodeQuizRecords", description = "查询某学习路径节点**最近几次练习的逐题作答记录**（题面摘要、学生作答、对错、得分、时间）。"
            + "学生问「我哪道题错了 / 帮我复盘某个节点 / 我最近练得怎么样」时调用；返回里没有题面的老记录会标注，不要编造题面。")
    public String queryNodeQuizRecords(
            @ToolParam(description = "节点名或关键词，可空表示当前正在学的节点") String nodeName,
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId) {
        try {
            if (StringTools.isEmpty(userId)) {
                return "参数错误：缺少学生ID";
            }
            String uid = userId.trim();
            List<LearningPathComponent.PathWithItems> paths = learningPathComponent.listMyPaths(uid);
            String keyword = nodeName == null ? "" : nodeName.trim();
            String itemId = null;
            String hitName = null;
            for (LearningPathComponent.PathWithItems each : paths) {
                List<LearningPathItem> items = each.items() == null ? List.<LearningPathItem>of() : each.items();
                for (LearningPathItem item : items) {
                    String itemName = item.getKnowledgePointName() == null ? "" : item.getKnowledgePointName();
                    boolean matched = keyword.isEmpty()
                            ? item.getItemId() != null && item.getItemId().equals(each.path().getCurrentItemId())
                            : (!itemName.isBlank() && (itemName.contains(keyword) || keyword.contains(itemName)));
                    if (matched) {
                        itemId = item.getItemId();
                        hitName = itemName;
                        break;
                    }
                }
                if (itemId != null) {
                    break;
                }
            }
            if (itemId == null) {
                return "没有找到对应的路径节点：可以先用 queryLearningPath 看学生有哪些节点，再带上准确的节点名来查";
            }
            PracticeRecordQuery query = new PracticeRecordQuery();
            query.setUserId(uid);
            query.setBizId(itemId);
            query.setPageSize(30);
            List<PracticeRecord> records = practiceRecordService.findListByParam(query);
            if (records == null || records.isEmpty()) {
                return "节点《" + hitName + "》还没有练习记录（做一次节点快测后这里就有逐题明细了）";
            }
            records.sort((first, second) -> {
                if (first.getCreateTime() == null || second.getCreateTime() == null) {
                    return 0;
                }
                return second.getCreateTime().compareTo(first.getCreateTime());
            });
            int total = records.size();
            int wrong = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("节点《").append(hitName).append("》最近练习记录（按时间倒序，最多 10 条）：").append("\n");
            int shown = 0;
            int index = 1;
            for (PracticeRecord record : records) {
                if (record.getIsCorrect() != null && record.getIsCorrect() == 0) {
                    wrong++;
                }
                if (shown++ < 10) {
                    String questionText = StringTools.isEmpty(record.getQuestionText())
                            ? "（早期记录无题面）" : summarize(record.getQuestionText(), 60);
                    sb.append("  ").append(index++).append(". [")
                            .append(record.getIsCorrect() != null && record.getIsCorrect() == 1 ? "对" : "错").append("] ")
                            .append(questionText).append("\n");
                    sb.append("     我的作答：").append(summarize(record.getUserAnswer(), 40));
                    if (!StringTools.isEmpty(record.getCorrectAnswer())) {
                        sb.append("；参考答案：").append(summarize(record.getCorrectAnswer(), 40));
                    }
                    if (record.getCreateTime() != null) {
                        sb.append("；时间：").append(DateUtil.format(record.getCreateTime(),
                                DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
                    }
                    sb.append("\n");
                }
            }
            sb.append("汇总：最近 ").append(total).append(" 条作答，错 ").append(wrong).append(" 条。");
            if (wrong > 0) {
                sb.append("复盘时优先讲上面标 [错] 的题。").append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryNodeQuizRecords 失败", e);
            return "查询节点练习记录失败：" + e.getMessage();
        }
    }

    /** 文本摘要（压平空白并截断），供工具输出用 */
    private String summarize(String text, int max) {
        if (text == null) {
            return "";
        }
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() > max ? flat.substring(0, max) + "…" : flat;
    }
    @Tool(name = "planNextStep", description = "给出「下一步学什么、复习什么」的建议清单：依据学生的真实到期待复习节点、进行中的节点与掌握度薄弱知识点排优先级。"
            + "学生问「我下一步该学什么 / 我哪里薄弱 / 今天学点啥」时调用；必须基于返回的真实清单作答，不得只讲通用方法。")
    public String planNextStep(
            @ToolParam(description = "学生用户ID，由系统自动注入") String userId,
            @ToolParam(description = "学段编码，由系统自动注入") String stage) {
        try {
            if (StringTools.isEmpty(userId)) {
                return "参数错误：缺少学生ID";
            }
            String uid = userId.trim();
            List<LearningPathComponent.PathWithItems> paths = learningPathComponent.listMyPaths(uid);
            // 掌握度里"下次复习时间已过"的节点也算到期（学习路径的 due_date 常常是空的，只看它就永远不算到期）
            List<KnowledgeMasteryVO> masteryForDue = learningAnalysisMapper.selectMasteryList(uid);
            java.util.Set<String> reviewDueItemIds = new java.util.HashSet<>();
            java.util.Map<String, java.util.Date> reviewDueByPoint = new java.util.HashMap<>();
            for (KnowledgeMasteryVO mastery : masteryForDue) {
                if (mastery.getNextReviewTime() != null && mastery.getNextReviewTime().before(new java.util.Date())) {
                    reviewDueByPoint.put(mastery.getKnowledgePointId(), mastery.getNextReviewTime());
                }
            }
            List<String> review = new java.util.ArrayList<>();
            List<String> current = new java.util.ArrayList<>();
            java.util.Date now = new java.util.Date();
            for (LearningPathComponent.PathWithItems each : paths) {
                String title = each.path().getTitle();
                List<LearningPathItem> items = each.items() == null ? List.<LearningPathItem>of() : each.items();
                for (LearningPathItem item : items) {
                    if (item.getStatus() != null && item.getStatus() == 2) {
                        continue;
                    }
                    String label = item.getKnowledgePointName() + "（路径《" + title + "》）";
                    // 逾期与否在下面统一判断（先算 dueNow，再决定是否补逾期天数）
                    // 到期判定（2026-10-07 修复）：节点的 due_date 与掌握度的 next_review_time 取更早者
                    boolean dueNow = item.getDueDate() != null && item.getDueDate().compareTo(now) <= 0;
                    if (!dueNow && reviewDueItemIds.contains(item.getItemId())) {
                        dueNow = true;
                    }
                    if (dueNow) {
                        String pointId = item.getKnowledgePointId();
                        if (pointId != null && reviewDueByPoint.containsKey(pointId)) {
                            long overdueDays = (System.currentTimeMillis() - reviewDueByPoint.get(pointId).getTime()) / 86400000L;
                            label = label + "（下次复习已逾期 " + Math.max(overdueDays, 1) + " 天）";
                        }
                        review.add(label);
                    } else if (item.getStatus() != null && item.getStatus() == 1 && current.size() < 3) {
                        current.add(label);
                    }
                }
            }
            List<KnowledgeMasteryVO> masteryList = learningAnalysisMapper.selectMasteryList(uid);
            List<KnowledgeMasteryVO> weak = new java.util.ArrayList<>();
            for (KnowledgeMasteryVO item : masteryList) {
                int score = item.getMasteryScore() == null ? 0 : item.getMasteryScore();
                int practice = item.getPracticeCount() == null ? 0 : item.getPracticeCount();
                if (practice > 0 && score < 70) {
                    weak.add(item);
                }
            }
            weak.sort(java.util.Comparator.comparingInt(item -> item.getMasteryScore() == null ? 0 : item.getMasteryScore()));

            if (review.isEmpty() && current.isEmpty() && weak.isEmpty()) {
                return "该学生暂时没有到期待复习的节点与掌握度薄弱点，可建议他先按学习路径推进当前节点，或做一次练习以形成数据。";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("建议按下面的顺序安排（数据来自学生真实学习记录）：\n");
            int order = 1;
            if (!review.isEmpty()) {
                sb.append("\n【优先：到期待复习】\n");
                for (String item : review) {
                    sb.append(order++).append(". ").append(item).append("\n");
                }
            }
            if (!current.isEmpty()) {
                sb.append("\n【继续推进：进行中的节点】\n");
                for (String item : current) {
                    sb.append(order++).append(". ").append(item).append("\n");
                }
            }
            if (!weak.isEmpty()) {
                sb.append("\n【薄弱知识点（建议回炉）】\n");
                int limit = 0;
                for (KnowledgeMasteryVO item : weak) {
                    if (limit++ >= 5) {
                        break;
                    }
                    sb.append(order++).append(". ").append(item.getKnowledgePointName())
                            .append("（掌握度 ").append(item.getMasteryScore() == null ? 0 : item.getMasteryScore())
                            .append("，练习 ").append(item.getPracticeCount() == null ? 0 : item.getPracticeCount()).append(" 次）\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("planNextStep 失败", e);
            return "生成下一步建议失败：" + e.getMessage();
        }
    }
}
