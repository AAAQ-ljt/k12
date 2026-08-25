package com.nexora.admin.biz;

import com.nexora.admin.dto.LessonResourceBindDTO;
import com.nexora.constants.Constants;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.entity.query.CourseChapterQuery;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.query.CourseStudyLessonProgressQuery;
import com.nexora.entity.query.CourseStudyLogQuery;
import com.nexora.entity.query.CourseStudyProgressQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.CourseChapterDetailVO;
import com.nexora.entity.vo.CourseDetailVO;
import com.nexora.entity.vo.CourseLessonDetailVO;
import com.nexora.entity.vo.CourseLessonResourceVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseChapterLessonResourceService;
import com.nexora.service.CourseChapterLessonService;
import com.nexora.service.CourseChapterService;
import com.nexora.service.CourseInfoService;
import com.nexora.service.CourseStudyLessonProgressService;
import com.nexora.service.CourseStudyLogService;
import com.nexora.service.CourseStudyProgressService;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 课程体系管理业务：课程、章节、课时、课时资源绑定。
 */
@Service
public class CourseBiz {

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private CourseChapterLessonResourceService courseChapterLessonResourceService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private CourseStudyProgressService courseStudyProgressService;

    @Resource
    private CourseStudyLogService courseStudyLogService;

    @Resource
    private CourseStudyLessonProgressService courseStudyLessonProgressService;

    public PaginationResultVO<CourseInfo> coursePage(CourseInfoQuery query) {
        return courseInfoService.findListByPage(query);
    }

    public CourseInfo getCourse(String courseId) {
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return course;
    }

    @Transactional(rollbackFor = Exception.class)
    public String addCourse(CourseInfo bean) {
        if (StringTools.isEmpty(bean.getCourseName())) {
            throw new BusinessException("课程名称不能为空");
        }
        fillStageByGrade(bean);
        bean.setCourseId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (bean.getSort() == null) {
            bean.setSort(0);
        }
        if (bean.getStatus() == null) {
            bean.setStatus(1);
        }
        if (bean.getLessonCount() == null) {
            bean.setLessonCount(0);
        }
        if (bean.getStudyCount() == null) {
            bean.setStudyCount(0);
        }
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        courseInfoService.add(bean);
        return bean.getCourseId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseInfo bean) {
        if (StringTools.isEmpty(bean.getCourseId())) {
            throw new BusinessException("课程ID不能为空");
        }
        CourseInfo exist = courseInfoService.getCourseInfoByCourseId(bean.getCourseId());
        if (exist == null) {
            throw new BusinessException("课程不存在");
        }
        fillStageByGrade(bean);
        bean.setUpdateTime(new Date());
        courseInfoService.updateCourseInfoByCourseId(bean, bean.getCourseId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(String courseId) {
        if (StringTools.isEmpty(courseId)) {
            throw new BusinessException("课程ID不能为空");
        }
        courseInfoService.getCourseInfoByCourseId(courseId);

        deleteLessonResourcesByCourse(courseId);
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setCourseId(courseId);
        courseChapterLessonService.deleteByParam(lessonQuery);

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        courseChapterService.deleteByParam(chapterQuery);

        CourseStudyProgressQuery progressQuery = new CourseStudyProgressQuery();
        progressQuery.setCourseId(courseId);
        courseStudyProgressService.deleteByParam(progressQuery);

        CourseStudyLogQuery logQuery = new CourseStudyLogQuery();
        logQuery.setCourseId(courseId);
        courseStudyLogService.deleteByParam(logQuery);

        CourseStudyLessonProgressQuery lessonProgressQuery = new CourseStudyLessonProgressQuery();
        lessonProgressQuery.setCourseId(courseId);
        courseStudyLessonProgressService.deleteByParam(lessonProgressQuery);

        courseInfoService.deleteCourseInfoByCourseId(courseId);
    }

    public CourseDetailVO courseDetail(String courseId) {
        CourseInfo course = getCourse(courseId);
        CourseDetailVO detail = new CourseDetailVO();
        detail.setCourse(course);

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        chapterQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapter> chapters = courseChapterService.findListByParam(chapterQuery);

        List<CourseChapterDetailVO> chapterVOs = new ArrayList<>();
        for (CourseChapter chapter : chapters) {
            CourseChapterDetailVO chapterVO = new CourseChapterDetailVO();
            chapterVO.setChapter(chapter);
            chapterVO.setLessons(lessonDetails(chapter.getChapterId(), courseId));
            chapterVOs.add(chapterVO);
        }
        detail.setChapters(chapterVOs);
        return detail;
    }

    public List<CourseChapter> chapterList(String courseId) {
        if (StringTools.isEmpty(courseId)) {
            throw new BusinessException("课程ID不能为空");
        }
        CourseChapterQuery query = new CourseChapterQuery();
        query.setCourseId(courseId);
        query.setOrderBy("sort asc, create_time asc");
        return courseChapterService.findListByParam(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public String addChapter(CourseChapter bean) {
        if (StringTools.isEmpty(bean.getCourseId())) {
            throw new BusinessException("课程ID不能为空");
        }
        if (StringTools.isEmpty(bean.getChapterName())) {
            throw new BusinessException("章节名称不能为空");
        }
        getCourse(bean.getCourseId());
        bean.setChapterId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (bean.getSort() == null) {
            bean.setSort(nextChapterSort(bean.getCourseId()));
        }
        if (bean.getStatus() == null) {
            bean.setStatus(0);
        }
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        courseChapterService.add(bean);
        return bean.getChapterId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(CourseChapter bean) {
        if (StringTools.isEmpty(bean.getChapterId())) {
            throw new BusinessException("章节ID不能为空");
        }
        if (courseChapterService.getCourseChapterByChapterId(bean.getChapterId()) == null) {
            throw new BusinessException("章节不存在");
        }
        bean.setUpdateTime(new Date());
        courseChapterService.updateCourseChapterByChapterId(bean, bean.getChapterId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(String chapterId) {
        if (StringTools.isEmpty(chapterId)) {
            throw new BusinessException("章节ID不能为空");
        }
        CourseChapter chapter = courseChapterService.getCourseChapterByChapterId(chapterId);
        if (chapter == null) {
            throw new BusinessException("章节不存在");
        }
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setChapterId(chapterId);
        List<CourseChapterLesson> lessons = courseChapterLessonService.findListByParam(lessonQuery);
        for (CourseChapterLesson lesson : lessons) {
            deleteLessonResourcesByLesson(lesson.getLessonId());
            deleteLessonStudyData(lesson.getLessonId());
        }
        courseChapterLessonService.deleteByParam(lessonQuery);
        courseChapterService.deleteCourseChapterByChapterId(chapterId);
        refreshLessonCount(chapter.getCourseId());
    }

    public List<CourseChapterLesson> lessonList(String chapterId, String courseId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        if (!StringTools.isEmpty(chapterId)) {
            query.setChapterId(chapterId);
        }
        if (!StringTools.isEmpty(courseId)) {
            query.setCourseId(courseId);
        }
        query.setOrderBy("sort asc, create_time asc");
        return courseChapterLessonService.findListByParam(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public String addLesson(CourseChapterLesson bean) {
        if (StringTools.isEmpty(bean.getChapterId()) || StringTools.isEmpty(bean.getCourseId())) {
            throw new BusinessException("章节和课程不能为空");
        }
        if (StringTools.isEmpty(bean.getLessonName())) {
            throw new BusinessException("课时名称不能为空");
        }
        getCourse(bean.getCourseId());
        if (courseChapterService.getCourseChapterByChapterId(bean.getChapterId()) == null) {
            throw new BusinessException("章节不存在");
        }
        bean.setLessonId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (bean.getSort() == null) {
            bean.setSort(nextLessonSort(bean.getChapterId()));
        }
        if (bean.getStatus() == null) {
            bean.setStatus(0);
        }
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        courseChapterLessonService.add(bean);
        refreshLessonCount(bean.getCourseId());
        return bean.getLessonId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateLesson(CourseChapterLesson bean) {
        if (StringTools.isEmpty(bean.getLessonId())) {
            throw new BusinessException("课时ID不能为空");
        }
        if (courseChapterLessonService.getCourseChapterLessonByLessonId(bean.getLessonId()) == null) {
            throw new BusinessException("课时不存在");
        }
        bean.setUpdateTime(new Date());
        courseChapterLessonService.updateCourseChapterLessonByLessonId(bean, bean.getLessonId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLesson(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException("课时ID不能为空");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(lessonId);
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        deleteLessonResourcesByLesson(lessonId);
        deleteLessonStudyData(lessonId);
        courseChapterLessonService.deleteCourseChapterLessonByLessonId(lessonId);
        refreshLessonCount(lesson.getCourseId());
    }

    public List<CourseLessonResourceVO> lessonResourceList(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException("课时ID不能为空");
        }
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setLessonId(lessonId);
        query.setOrderBy("sort asc, id asc");
        List<CourseChapterLessonResource> binds = courseChapterLessonResourceService.findListByParam(query);
        List<CourseLessonResourceVO> result = new ArrayList<>();
        for (CourseChapterLessonResource bind : binds) {
            result.add(toLessonResourceVO(bind));
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindResources(LessonResourceBindDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getLessonId())) {
            throw new BusinessException("课时ID不能为空");
        }
        if (dto.getResourceIds() == null || dto.getResourceIds().isEmpty()) {
            throw new BusinessException("请选择要绑定的资源");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(dto.getLessonId());
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(lesson.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        String courseStage = course.getStage();

        CourseChapterLessonResourceQuery existQuery = new CourseChapterLessonResourceQuery();
        existQuery.setLessonId(dto.getLessonId());
        List<CourseChapterLessonResource> existList = courseChapterLessonResourceService.findListByParam(existQuery);
        Set<String> existResourceIds = new HashSet<>();
        int maxSort = 0;
        for (CourseChapterLessonResource item : existList) {
            existResourceIds.add(item.getResourceId());
            if (item.getSort() != null && item.getSort() > maxSort) {
                maxSort = item.getSort();
            }
        }

        int sort = maxSort;
        for (String resourceId : dto.getResourceIds()) {
            if (StringTools.isEmpty(resourceId) || existResourceIds.contains(resourceId)) {
                continue;
            }
            ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
            if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
                throw new BusinessException("资源不存在或不可用");
            }
            if (!StringTools.isEmpty(resource.getStage()) && !StringTools.isEmpty(courseStage)
                    && !courseStage.equals(resource.getStage())) {
                String resourceName = StringTools.isEmpty(resource.getResourceName())
                        ? resourceId : resource.getResourceName();
                throw new BusinessException("不能绑定《" + resourceName + "》：资源学段（"
                        + resource.getStage() + "）与课程学段（" + courseStage + "）不一致");
            }
            CourseChapterLessonResource bind = new CourseChapterLessonResource();
            bind.setLessonId(dto.getLessonId());
            bind.setCourseId(lesson.getCourseId());
            bind.setResourceId(resourceId);
            bind.setSort(++sort);
            bind.setCreateTime(new Date());
            courseChapterLessonResourceService.add(bind);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbindResource(Integer id) {
        if (id == null) {
            throw new BusinessException("资源绑定ID不能为空");
        }
        courseChapterLessonResourceService.deleteCourseChapterLessonResourceById(id);
    }

    private List<CourseLessonDetailVO> lessonDetails(String chapterId, String courseId) {
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setChapterId(chapterId);
        lessonQuery.setCourseId(courseId);
        lessonQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapterLesson> lessons = courseChapterLessonService.findListByParam(lessonQuery);
        List<CourseLessonDetailVO> lessonVOs = new ArrayList<>();
        for (CourseChapterLesson lesson : lessons) {
            CourseLessonDetailVO lessonVO = new CourseLessonDetailVO();
            lessonVO.setLesson(lesson);
            lessonVO.setResources(lessonResourceList(lesson.getLessonId()));
            lessonVOs.add(lessonVO);
        }
        return lessonVOs;
    }

    private CourseLessonResourceVO toLessonResourceVO(CourseChapterLessonResource bind) {
        CourseLessonResourceVO vo = new CourseLessonResourceVO();
        vo.setId(bind.getId());
        vo.setLessonId(bind.getLessonId());
        vo.setCourseId(bind.getCourseId());
        vo.setResourceId(bind.getResourceId());
        vo.setSort(bind.getSort());
        vo.setCreateTime(bind.getCreateTime());
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(bind.getResourceId());
        if (resource != null) {
            vo.setResourceName(resource.getResourceName());
            vo.setResourceType(resource.getResourceType());
            vo.setDescription(resource.getDescription());
            vo.setCover(resource.getCover());
            vo.setDuration(resource.getDuration());
        }
        return vo;
    }

    private void fillStageByGrade(CourseInfo bean) {
        if (!StringTools.isEmpty(bean.getGrade())) {
            String stage = StageEnum.matchByGrade(bean.getGrade());
            if (stage == null) {
                throw new BusinessException("非法的年级");
            }
            bean.setStage(stage);
        }
        if (StringTools.isEmpty(bean.getStage())) {
            throw new BusinessException("请选择年级");
        }
    }

    private int nextChapterSort(String courseId) {
        CourseChapterQuery query = new CourseChapterQuery();
        query.setCourseId(courseId);
        List<CourseChapter> list = courseChapterService.findListByParam(query);
        return list.stream().map(CourseChapter::getSort).filter(sort -> sort != null)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    private int nextLessonSort(String chapterId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setChapterId(chapterId);
        List<CourseChapterLesson> list = courseChapterLessonService.findListByParam(query);
        return list.stream().map(CourseChapterLesson::getSort).filter(sort -> sort != null)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    private void refreshLessonCount(String courseId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setCourseId(courseId);
        int count = courseChapterLessonService.findCountByParam(query);
        CourseInfo updateBean = new CourseInfo();
        updateBean.setLessonCount(count);
        updateBean.setUpdateTime(new Date());
        courseInfoService.updateCourseInfoByCourseId(updateBean, courseId);
    }

    private void deleteLessonResourcesByCourse(String courseId) {
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setCourseId(courseId);
        courseChapterLessonResourceService.deleteByParam(query);
    }

    private void deleteLessonResourcesByLesson(String lessonId) {
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setLessonId(lessonId);
        courseChapterLessonResourceService.deleteByParam(query);
    }

    private void deleteLessonStudyData(String lessonId) {
        CourseStudyLessonProgressQuery lessonProgressQuery = new CourseStudyLessonProgressQuery();
        lessonProgressQuery.setLessonId(lessonId);
        courseStudyLessonProgressService.deleteByParam(lessonProgressQuery);

        CourseStudyLogQuery logQuery = new CourseStudyLogQuery();
        logQuery.setLessonId(lessonId);
        courseStudyLogService.deleteByParam(logQuery);
    }
}
