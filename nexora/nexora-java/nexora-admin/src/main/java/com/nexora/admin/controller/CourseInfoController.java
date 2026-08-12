package com.nexora.admin.controller;

import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseInfoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程管理 Controller
 */
@RestController("courseInfoController")
@RequestMapping("/courseInfo")
public class CourseInfoController extends ABaseController {

    private final CourseInfoService courseInfoService;

    public CourseInfoController(CourseInfoService courseInfoService) {
        this.courseInfoService = courseInfoService;
    }

    /**
     * 分页查询课程列表
     */
    @GetMapping("/loadDataList")
    public ResponseVO loadDataList(CourseInfoQuery query) {
        return getSuccessResponseVO(courseInfoService.findListByPage(query));
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/getInfo")
    public ResponseVO getInfo(@RequestParam String courseId) {
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(courseId);
        if (courseInfo == null) {
            throw new BusinessException("课程不存在");
        }
        return getSuccessResponseVO(courseInfo);
    }

    /**
     * 新增课程
     */
    @PostMapping("/add")
    public ResponseVO add(@RequestBody CourseInfo courseInfo) {
        courseInfoService.add(courseInfo);
        return getSuccessResponseVO(null);
    }

    /**
     * 编辑课程
     */
    @PutMapping("/update")
    public ResponseVO update(@RequestBody CourseInfo courseInfo) {
        if (courseInfo.getCourseId() == null || courseInfo.getCourseId().isEmpty()) {
            throw new BusinessException("课程ID不能为空");
        }
        courseInfoService.updateCourseInfoByCourseId(courseInfo, courseInfo.getCourseId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/del")
    public ResponseVO del(@RequestParam String courseId) {
        courseInfoService.deleteCourseInfoByCourseId(courseId);
        return getSuccessResponseVO(null);
    }
}
