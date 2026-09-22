package com.nexora.service;

import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.entity.po.CourseEnrollment;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.CourseLessonQuiz;
import com.nexora.entity.po.CourseStudyLessonProgress;
import com.nexora.entity.po.StudentLearningRecord;
import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.entity.query.CourseEnrollmentQuery;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.query.CourseStudyLessonProgressQuery;
import com.nexora.entity.query.StudentLearningRecordQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseChapterLessonResourceService;
import com.nexora.service.CourseEnrollmentService;
import com.nexora.service.CourseInfoService;
import com.nexora.service.CourseLessonQuizService;
import com.nexora.service.CourseStudyLessonProgressService;
import com.nexora.utils.StringTools;
import com.nexora.vo.CourseProgressVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生端课程学习上报（学习进度一期）：
 * 学习内容强制加入校验 → 打开课时资源即记课时完成 → 写 VIEW 行为流水（同人同资源同日去重）。
 */
@Slf4j
@Service
public class CourseStudyBiz {

    /** 学习行为类型：浏览资源 */
    private static final String ACTION_VIEW = "VIEW";

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private CourseChapterLessonResourceService courseChapterLessonResourceService;

    @Resource
    private CourseEnrollmentService courseEnrollmentService;

    @Resource
    private CourseLessonQuizService courseLessonQuizService;

    @Resource
    private CourseStudyLessonProgressService courseStudyLessonProgressService;

    @Resource
    private StudentLearningRecordService studentLearningRecordService;

    @Resource
    private CourseInfoService courseInfoService;

    /**
     * 我的课程学习进度（我的页面 / 我的课程卡复用）：
     * 一次查出该生全部课时完成记录按课程聚合，一次查已加入课程，内存组装，避免循环查库。
     * 口径沿用现有完成链路：无测验课时打开资源即完成；启用测验课时以测验通过计完成。
     */
    public List<CourseProgressVO> loadMyCourseProgress(String userId) {
        List<CourseProgressVO> result = new ArrayList<>();
        if (StringTools.isEmpty(userId)) {
            return result;
        }
        // 已加入课程（按加入时间正序）
        CourseEnrollmentQuery enrollmentQuery = new CourseEnrollmentQuery();
        enrollmentQuery.setUserId(userId);
        enrollmentQuery.setStatus(1);
        enrollmentQuery.setOrderBy("create_time asc");
        List<CourseEnrollment> enrollments = courseEnrollmentService.findListByParam(enrollmentQuery);
        if (enrollments == null || enrollments.isEmpty()) {
            return result;
        }
        List<String> courseIds = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            if (!courseIds.contains(enrollment.getCourseId())) {
                courseIds.add(enrollment.getCourseId());
            }
        }
        // 一次查课程信息
        CourseInfoQuery courseQuery = new CourseInfoQuery();
        courseQuery.setCourseIds(courseIds);
        courseQuery.setStatus(1);
        Map<String, CourseInfo> courseMap = new LinkedHashMap<>();
        List<CourseInfo> courseList = courseInfoService.findListByParam(courseQuery);
        if (courseList != null) {
            for (CourseInfo course : courseList) {
                courseMap.put(course.getCourseId(), course);
            }
        }
        // 一次查全部课时完成记录，按课程聚合
        CourseStudyLessonProgressQuery progressQuery = new CourseStudyLessonProgressQuery();
        progressQuery.setUserId(userId);
        Map<String, Integer> finishedMap = new LinkedHashMap<>();
        List<CourseStudyLessonProgress> progressList = courseStudyLessonProgressService.findListByParam(progressQuery);
        if (progressList != null) {
            for (CourseStudyLessonProgress progress : progressList) {
                if (progress.getFinished() == null || progress.getFinished() != 1) {
                    continue;
                }
                finishedMap.merge(progress.getCourseId(), 1, Integer::sum);
            }
        }
        for (String courseId : courseIds) {
            CourseInfo course = courseMap.get(courseId);
            if (course == null) {
                continue;
            }
            CourseProgressVO vo = new CourseProgressVO();
            vo.setCourseId(course.getCourseId());
            vo.setCourseName(course.getCourseName());
            vo.setCover(course.getCover());
            vo.setStage(course.getStage());
            int total = course.getLessonCount() == null ? 0 : course.getLessonCount();
            int finished = finishedMap.getOrDefault(courseId, 0);
            vo.setLessonCount(total);
            vo.setFinishedLessons(total > 0 ? Math.min(finished, total) : finished);
            vo.setProgress(total <= 0 ? 0 : Math.min(100, (int) Math.round(finished * 100.0 / total)));
            result.add(vo);
        }
        return result;
    }

    /**
     * 上报课时资源学习：校验加入与资源归属，记课时完成（幂等）并落当日去重的 VIEW 流水。
     */
    public void reportStudy(String userId, String lessonId, String resourceId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("请先登录");
        }
        if (StringTools.isEmpty(lessonId) || StringTools.isEmpty(resourceId)) {
            throw new BusinessException("课时ID和资源ID不能为空");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(lessonId);
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        CourseChapterLessonResourceQuery bindQuery = new CourseChapterLessonResourceQuery();
        bindQuery.setLessonId(lessonId);
        bindQuery.setResourceId(resourceId);
        if (courseChapterLessonResourceService.findCountByParam(bindQuery) <= 0) {
            throw new BusinessException("资源不属于该课时");
        }
        // 学习内容强制加入：未加入课程不允许记录学习
        requireEnrolled(userId, lesson.getCourseId());

        Date now = new Date();
        // 完成口径：配置了启用测验的课时仅通过测验才算完成（走 CourseQuizBiz 既有链路），
        // 打开资源只记 VIEW 行为流水；无测验课时打开资源即记完成
        if (!lessonHasActiveQuiz(lesson.getLessonId())) {
            markLessonCompleted(userId, lesson, now);
        }
        saveViewRecord(userId, lesson, resourceId, now);
    }

    /** 课时是否配置了启用中的通关测验（quizMode>0 且 status=1） */
    private boolean lessonHasActiveQuiz(String lessonId) {
        CourseLessonQuiz quiz = courseLessonQuizService.getCourseLessonQuizByLessonId(lessonId);
        return quiz != null && quiz.getQuizMode() != null && quiz.getQuizMode() > 0
                && quiz.getStatus() != null && quiz.getStatus() == 1;
    }

    private void requireEnrolled(String userId, String courseId) {
        CourseEnrollment enrollment =
                courseEnrollmentService.getCourseEnrollmentByUserIdAndCourseId(userId, courseId);
        if (enrollment == null || enrollment.getStatus() == null || enrollment.getStatus() != 1) {
            throw new BusinessException("请先加入该课程后再学习");
        }
    }

    /** 课时完成标记（幂等）：无测验课时打开资源即完成；测验完成链路已写过的直接跳过 */
    private void markLessonCompleted(String userId, CourseChapterLesson lesson, Date now) {
        CourseStudyLessonProgress progress = courseStudyLessonProgressService
                .getCourseStudyLessonProgressByUserIdAndLessonId(userId, lesson.getLessonId());
        if (progress == null) {
            CourseStudyLessonProgress insertBean = new CourseStudyLessonProgress();
            insertBean.setUserId(userId);
            insertBean.setCourseId(lesson.getCourseId());
            insertBean.setLessonId(lesson.getLessonId());
            insertBean.setFinished(1);
            insertBean.setFinishTime(now);
            insertBean.setCreateTime(now);
            insertBean.setUpdateTime(now);
            courseStudyLessonProgressService.add(insertBean);
        } else if (progress.getFinished() == null || progress.getFinished() != 1) {
            CourseStudyLessonProgress updateBean = new CourseStudyLessonProgress();
            updateBean.setFinished(1);
            updateBean.setFinishTime(now);
            updateBean.setUpdateTime(now);
            courseStudyLessonProgressService.updateCourseStudyLessonProgressById(updateBean, progress.getId());
        }
    }

    /** VIEW 行为流水：同人同资源同日仅记一条 */
    private void saveViewRecord(String userId, CourseChapterLesson lesson, String resourceId, Date now) {
        try {
            StudentLearningRecordQuery dedupQuery = new StudentLearningRecordQuery();
            dedupQuery.setUserId(userId);
            dedupQuery.setResourceId(resourceId);
            dedupQuery.setActionType(ACTION_VIEW);
            dedupQuery.setCreateTimeStart(new SimpleDateFormat("yyyy-MM-dd").format(now));
            if (studentLearningRecordService.findCountByParam(dedupQuery) > 0) {
                return;
            }
            StudentLearningRecord record = new StudentLearningRecord();
            record.setUserId(userId);
            record.setResourceId(resourceId);
            record.setCourseId(lesson.getCourseId());
            record.setLessonId(lesson.getLessonId());
            record.setActionType(ACTION_VIEW);
            record.setCreateTime(now);
            studentLearningRecordService.add(record);
        } catch (Exception e) {
            // 行为流水失败不阻断学习主流程
            log.warn("学习行为流水写入失败 userId={} lessonId={}", userId, lesson.getLessonId(), e);
        }
    }
}
