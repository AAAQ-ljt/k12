package com.nexora.service;

import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.po.StudentLearningRecord;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.query.StudentLearningRecordQuery;
import com.nexora.entity.vo.KnowledgeMasteryVO;
import com.nexora.mappers.LearningAnalysisMapper;
import com.nexora.service.CourseChapterLessonService;
import com.nexora.service.CourseInfoService;
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
import java.util.List;

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

    @Tool(name = "queryCourse", description = "按学段与关键词查询上架课程（含学科/难度/课时数/学习人数）")
    public String queryCourse(
            @ToolParam(description = "学段编码，可空；支持高中/初中等中文，自动归一化") String stage,
            @ToolParam(description = "课程名关键词，可空") String keyword) {
        try {
            String stageCode = normalizeStageOrError(stage);
            if (stageCode != null && stageCode.startsWith("无法识别")) {
                return stageCode;
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

    @Tool(name = "queryLesson", description = "按课程ID查询课时列表（含摘要/视频时长），或按课时ID查课时详情")
    public String queryLesson(
            @ToolParam(description = "课程ID，可空") String courseId,
            @ToolParam(description = "课时ID，可空") String lessonId,
            @ToolParam(description = "学段编码，由系统自动注入") String stage) {
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
                if (stageCode != null && course != null && !stageCode.equals(course.getStage())) {
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
            if (stageCode != null && !stageCode.equals(course.getStage())) {
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
            return "知识点共 " + list.size() + " 个：已掌握 " + mastered + "、进行中 " + inProgress
                    + "、未解锁 " + locked + "，平均分 " + totalScore / list.size() + "。\n" + sb;
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
}