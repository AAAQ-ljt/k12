package com.nexora.component;

import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.entity.po.CourseEnrollment;
import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.service.CourseChapterLessonResourceService;
import com.nexora.service.CourseEnrollmentService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 课程教材资源的「已加入课程」访问判定（学生端资源详情 / AI 问答来源过滤共用）。
 *
 * 产品口径（2026-10-05 用户确认）：学生只要已加入某门课程，就能学习 / 阅览 / 答题该课程下的全部资源，
 * **不再按学段（年段）限制**——此前三年级学生打开一年级课程的资源会被学段等值校验拦成
 * 「资源不存在或暂不可用」（前端详情页表现为空白页、也不上报学习进度）。
 * 未绑定课时的公共资源（个人资源、推荐资料等）仍按学段推荐 / 过滤。
 */
@Component
public class CourseResourceAccessComponent {

    @Resource
    private CourseChapterLessonResourceService courseChapterLessonResourceService;

    @Resource
    private CourseEnrollmentService courseEnrollmentService;

    /**
     * 资源与课程绑定的访问状态
     */
    public enum BoundCourseAccess {

        /** 未绑定到任何课时（个人 / 推荐等公共资源）：应由调用方按学段等原有规则处理 */
        NOT_BOUND,

        /** 绑定到课时，但该用户未加入对应课程：拒绝访问 */
        NOT_ENROLLED,

        /** 绑定到课时且用户已加入对应课程：豁免学段限制 */
        ENROLLED
    }

    /**
     * 判定用户对「绑定课时的课程教材资源」的访问状态；未登录（userId 为空）按未绑定处理，保持原有宽松行为
     */
    public BoundCourseAccess resolve(String resourceId, String userId) {
        if (StringTools.isEmpty(resourceId) || StringTools.isEmpty(userId)) {
            return BoundCourseAccess.NOT_BOUND;
        }
        CourseChapterLessonResourceQuery bindQuery = new CourseChapterLessonResourceQuery();
        bindQuery.setResourceId(resourceId);
        List<CourseChapterLessonResource> binds = courseChapterLessonResourceService.findListByParam(bindQuery);
        if (binds == null || binds.isEmpty()) {
            return BoundCourseAccess.NOT_BOUND;
        }
        boolean enrolled = binds.stream().map(CourseChapterLessonResource::getCourseId).distinct()
                .anyMatch(courseId -> {
                    CourseEnrollment enrollment = courseEnrollmentService
                            .getCourseEnrollmentByUserIdAndCourseId(userId, courseId);
                    return enrollment != null && enrollment.getStatus() != null && enrollment.getStatus() == 1;
                });
        return enrolled ? BoundCourseAccess.ENROLLED : BoundCourseAccess.NOT_ENROLLED;
    }
}
