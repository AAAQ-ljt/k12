package com.nexora.admin.controller;

import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.query.CourseChapterQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseChapterService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 章节管理 Controller
 */
@RestController("courseChapterController")
@RequestMapping("/courseChapter")
public class CourseChapterController extends ABaseController {

    private final CourseChapterService courseChapterService;

    public CourseChapterController(CourseChapterService courseChapterService) {
        this.courseChapterService = courseChapterService;
    }

    /**
     * 分页查询章节列表
     */
    @GetMapping("/loadDataList")
    public ResponseVO loadDataList(CourseChapterQuery query) {
        return getSuccessResponseVO(courseChapterService.findListByPage(query));
    }

    /**
     * 按课程ID查询章节列表
     */
    @GetMapping("/loadByCourseId")
    public ResponseVO loadByCourseId(@RequestParam String courseId) {
        CourseChapterQuery query = new CourseChapterQuery();
        query.setCourseId(courseId);
        List<CourseChapter> list = courseChapterService.findListByParam(query);
        return getSuccessResponseVO(list);
    }

    /**
     * 获取章节详情
     */
    @GetMapping("/getInfo")
    public ResponseVO getInfo(@RequestParam String chapterId) {
        CourseChapter courseChapter = courseChapterService.getCourseChapterByChapterId(chapterId);
        if (courseChapter == null) {
            throw new BusinessException("章节不存在");
        }
        return getSuccessResponseVO(courseChapter);
    }

    /**
     * 新增章节
     */
    @PostMapping("/add")
    public ResponseVO add(@RequestBody CourseChapter courseChapter) {
        courseChapterService.add(courseChapter);
        return getSuccessResponseVO(null);
    }

    /**
     * 编辑章节
     */
    @PutMapping("/update")
    public ResponseVO update(@RequestBody CourseChapter courseChapter) {
        if (courseChapter.getChapterId() == null || courseChapter.getChapterId().isEmpty()) {
            throw new BusinessException("章节ID不能为空");
        }
        courseChapterService.updateCourseChapterByChapterId(courseChapter, courseChapter.getChapterId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除章节
     */
    @DeleteMapping("/del")
    public ResponseVO del(@RequestParam String chapterId) {
        courseChapterService.deleteCourseChapterByChapterId(chapterId);
        return getSuccessResponseVO(null);
    }
}
