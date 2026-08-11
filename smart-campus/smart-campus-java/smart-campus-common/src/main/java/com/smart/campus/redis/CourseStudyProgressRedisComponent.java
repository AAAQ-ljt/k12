package com.smart.campus.redis;

import com.smart.campus.entity.constants.RedisKeyConstants;
import com.smart.campus.entity.po.CourseStudyLessonProgress;
import com.smart.campus.entity.po.CourseStudyProgress;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class CourseStudyProgressRedisComponent {

    private static final Duration STUDY_PROGRESS_TTL = Duration.ofDays(7);

    @Resource
    private WebLoginRedisComponent.RedisUtils redisUtils;

    public void saveCourseProgress(CourseStudyProgress progress) {
        if (progress == null || progress.getUserId() == null || progress.getCourseId() == null) {
            return;
        }
        redisUtils.setJson(buildCourseKey(progress.getUserId(), progress.getCourseId()), progress, STUDY_PROGRESS_TTL);
        redisUtils.addSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_COURSE_SET_KEY, buildCourseDirtyMember(progress.getUserId(), progress.getCourseId()));
    }

    public CourseStudyProgress getCourseProgress(Integer userId, String courseId) {
        return redisUtils.getJson(buildCourseKey(userId, courseId), CourseStudyProgress.class);
    }

    public void deleteCourseProgress(Integer userId, String courseId) {
        redisUtils.delete(buildCourseKey(userId, courseId));
        redisUtils.removeSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_COURSE_SET_KEY, buildCourseDirtyMember(userId, courseId));
    }

    public void saveLessonProgress(CourseStudyLessonProgress progress) {
        if (progress == null || progress.getUserId() == null || progress.getLessonId() == null) {
            return;
        }
        redisUtils.setJson(buildLessonKey(progress.getUserId(), progress.getLessonId()), progress, STUDY_PROGRESS_TTL);
        redisUtils.addSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_LESSON_SET_KEY, buildLessonDirtyMember(progress.getUserId(), progress.getLessonId()));
    }

    public CourseStudyLessonProgress getLessonProgress(Integer userId, String lessonId) {
        return redisUtils.getJson(buildLessonKey(userId, lessonId), CourseStudyLessonProgress.class);
    }

    public void deleteLessonProgress(Integer userId, String lessonId) {
        redisUtils.delete(buildLessonKey(userId, lessonId));
        redisUtils.removeSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_LESSON_SET_KEY, buildLessonDirtyMember(userId, lessonId));
    }

    public Set<String> getDirtyCourseMembers() {
        return redisUtils.getSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_COURSE_SET_KEY);
    }

    public Set<String> getDirtyLessonMembers() {
        return redisUtils.getSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_LESSON_SET_KEY);
    }

    public void removeDirtyCourseMember(String member) {
        redisUtils.removeSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_COURSE_SET_KEY, member);
    }

    public void removeDirtyLessonMember(String member) {
        redisUtils.removeSetMembers(RedisKeyConstants.COURSE_STUDY_DIRTY_LESSON_SET_KEY, member);
    }

    public String buildCourseDirtyMember(Integer userId, String courseId) {
        return userId + ":" + courseId;
    }

    public String buildLessonDirtyMember(Integer userId, String lessonId) {
        return userId + ":" + lessonId;
    }

    private String buildCourseKey(Integer userId, String courseId) {
        return RedisKeyConstants.buildKey(RedisKeyConstants.COURSE_STUDY_PROGRESS_PREFIX, userId + ":" + courseId);
    }

    private String buildLessonKey(Integer userId, String lessonId) {
        return RedisKeyConstants.buildKey(RedisKeyConstants.COURSE_STUDY_LESSON_PROGRESS_PREFIX, userId + ":" + lessonId);
    }
}
