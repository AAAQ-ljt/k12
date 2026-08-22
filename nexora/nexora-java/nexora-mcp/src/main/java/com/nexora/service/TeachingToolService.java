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

    /** 学习行为类型白名单（student_learning_record.action_type） */
    private static final List<String> ACTION_TYPES = List.of("VIEW", "COMPLETE", "PRACTICE", "ANIMATION", "PARSE", "AI_CHAT");

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

    @Tool(name = "queryKnowledgePoint", description = "按学段与关键词查询官方知识点（ID/学段/学科/名称）")
    public String queryKnowledgePoint(
            @ToolParam(description = "学段编码：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR，可空") String stage,
            @ToolParam(description = "知识点名称关键词，可空") String keyword) {
        try {
            KnowledgePointQuery query = new KnowledgePointQuery();
            if (!StringTools.isEmpty(stage)) {
                query.setStage(stage.trim());
            }
            if (!StringTools.isEmpty(keyword)) {
                query.setNameFuzzy(keyword.trim());
            }
            query.setOrderBy("sort asc");
            List<KnowledgePoint> list = knowledgePointService.findListByParam(query);
            if (list.isEmpty()) {
                return "未找到匹配的知识点";
            }
            StringBuilder sb = new StringBuilder("知识点列表：\n");
            int index = 1;
            for (KnowledgePoint point : list) {
                sb.append(index++).append(". ").append(point.getName())
                        .append("（学段:").append(point.getStage())
                        .append("，学科:").append(point.getSubject())
                        .append("，ID:").append(point.getKnowledgePointId()).append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryKnowledgePoint 失败", e);
            return "查询知识点失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryCourse", description = "按学段与关键词查询上架课程")
    public String queryCourse(
            @ToolParam(description = "学段编码，可空") String stage,
            @ToolParam(description = "课程名关键词，可空") String keyword) {
        try {
            CourseInfoQuery query = new CourseInfoQuery();
            query.setStatus(1);
            if (!StringTools.isEmpty(stage)) {
                query.setStage(stage.trim());
            }
            if (!StringTools.isEmpty(keyword)) {
                query.setCourseNameFuzzy(keyword.trim());
            }
            query.setOrderBy("sort asc");
            List<CourseInfo> list = courseInfoService.findListByParam(query);
            if (list.isEmpty()) {
                return "未找到匹配的课程";
            }
            StringBuilder sb = new StringBuilder("课程列表：\n");
            int index = 1;
            for (CourseInfo course : list) {
                sb.append(index++).append(". ").append(course.getCourseName())
                        .append("（年级:").append(course.getGrade() == null ? "-" : course.getGrade())
                        .append("，课程ID:").append(course.getCourseId()).append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryCourse 失败", e);
            return "查询课程失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryLesson", description = "按课程ID查询课时，或按课时ID查详情")
    public String queryLesson(
            @ToolParam(description = "课程ID，可空") String courseId,
            @ToolParam(description = "课时ID，可空") String lessonId) {
        try {
            if (!StringTools.isEmpty(lessonId)) {
                CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(lessonId.trim());
                return lesson == null ? "课时不存在"
                        : "课时：" + lesson.getLessonName() + "（ID:" + lesson.getLessonId() + "）";
            }
            if (StringTools.isEmpty(courseId)) {
                return "请提供课程ID或课时ID";
            }
            CourseChapterLessonQuery query = new CourseChapterLessonQuery();
            query.setCourseId(courseId.trim());
            query.setStatus(0);
            query.setOrderBy("sort asc");
            List<CourseChapterLesson> list = courseChapterLessonService.findListByParam(query);
            if (list.isEmpty()) {
                return "该课程暂无课时";
            }
            StringBuilder sb = new StringBuilder("课程课时列表：\n");
            int index = 1;
            for (CourseChapterLesson lesson : list) {
                sb.append(index++).append(". ").append(lesson.getLessonName())
                        .append("（ID:").append(lesson.getLessonId()).append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("queryLesson 失败", e);
            return "查询课时失败：" + e.getMessage();
        }
    }

    @Tool(name = "recommendResource", description = "推荐官方学习资源卡片（按学段/知识点/类型）")
    public String recommendResource(
            @ToolParam(description = "学段编码，可空") String stage,
            @ToolParam(description = "知识点ID，可空") String knowledgePointId,
            @ToolParam(description = "资源类型：VIDEO/IMAGE/DOCUMENT/LINK 等，可空") String type) {
        try {
            ResourceInfoQuery query = new ResourceInfoQuery();
            query.setOwnerIdNull(Boolean.TRUE);
            query.setStatus(1);
            if (!StringTools.isEmpty(stage)) {
                query.setStage(stage.trim());
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

    @Tool(name = "queryMastery", description = "查询学生知识点掌握度概览（含知识点名称与得分）")
    public String queryMastery(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "学段编码，可空") String stage) {
        try {
            if (StringTools.isEmpty(userId)) {
                return "参数错误：缺少学生ID";
            }
            List<KnowledgeMasteryVO> list = learningAnalysisMapper.selectMasteryList(userId.trim());
            if (list.isEmpty()) {
                return "该学生暂无掌握度数据";
            }
            int mastered = 0;
            int totalScore = 0;
            StringBuilder sb = new StringBuilder();
            int index = 1;
            for (KnowledgeMasteryVO item : list) {
                totalScore += item.getMasteryScore() == null ? 0 : item.getMasteryScore();
                if (item.getStatus() != null && item.getStatus() == 2) {
                    mastered++;
                }
                if (index <= 10) {
                    sb.append(index++).append(". ").append(item.getKnowledgePointName() == null ? "知识点" : item.getKnowledgePointName())
                            .append(" 得分:").append(item.getMasteryScore() == null ? 0 : item.getMasteryScore()).append("\n");
                }
            }
            return "已掌握知识点 " + mastered + " 个，平均分 " + (list.isEmpty() ? 0 : totalScore / list.size()) + "。\n" + sb;
        } catch (Exception e) {
            log.warn("queryMastery 失败", e);
            return "查询掌握度失败：" + e.getMessage();
        }
    }

    @Tool(name = "saveLearningRecord", description = "记录学生学习行为（VIEW/COMPLETE/PRACTICE/ANIMATION/PARSE/AI_CHAT），写入前校验学生存在")
    public String saveLearningRecord(
            @ToolParam(description = "学生用户ID") String userId,
            @ToolParam(description = "行为对象ID（资源/课程/课时ID，自动识别类型）") String targetId,
            @ToolParam(description = "行为类型：VIEW/COMPLETE/PRACTICE/ANIMATION/PARSE/AI_CHAT") String actionType,
            @ToolParam(description = "行为说明，可空") String detail) {
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
            record.setDuration(0);
            record.setCreateTime(new Date());
            if (!StringTools.isEmpty(targetId)) {
                String target = targetId.trim();
                // 按资源/课程/课时顺序识别目标类型
                if (resourceInfoService.getResourceInfoByResourceId(target) != null) {
                    record.setResourceId(target);
                } else if (courseInfoService.getCourseInfoByCourseId(target) != null) {
                    record.setCourseId(target);
                } else if (courseChapterLessonService.getCourseChapterLessonByLessonId(target) != null) {
                    record.setLessonId(target);
                } else {
                    return "目标对象不存在：" + targetId;
                }
            }
            studentLearningRecordService.add(record);
            log.info("MCP 学习行为记录成功 userId={} type={} target={}", user.getUserId(), type,
                    record.getResourceId() != null ? record.getResourceId()
                            : record.getCourseId() != null ? record.getCourseId() : record.getLessonId());
            return "学习行为已记录（" + type + "）";
        } catch (Exception e) {
            log.warn("saveLearningRecord 失败", e);
            return "记录学习行为失败：" + e.getMessage();
        }
    }
}