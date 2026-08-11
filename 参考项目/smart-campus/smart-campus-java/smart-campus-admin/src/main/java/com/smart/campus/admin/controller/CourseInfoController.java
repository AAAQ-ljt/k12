package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.CourseAdminBiz;
import com.smart.campus.admin.biz.CourseHomeworkAdminBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.dto.CourseHomeworkJudgeDTO;
import com.smart.campus.entity.dto.CourseSaveDTO;
import com.smart.campus.entity.dto.CourseStructureSaveDTO;
import com.smart.campus.entity.query.CourseHomeworkSubmitManageQuery;
import com.smart.campus.entity.query.CourseInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("teaching:course")
@Validated
@RestController("courseInfoController")
@RequestMapping("/courseInfo")
public class CourseInfoController extends ABaseController {

    private final CourseAdminBiz courseAdminBiz;
    private final CourseHomeworkAdminBiz courseHomeworkAdminBiz;

    public CourseInfoController(CourseAdminBiz courseAdminBiz, CourseHomeworkAdminBiz courseHomeworkAdminBiz) {
        this.courseAdminBiz = courseAdminBiz;
        this.courseHomeworkAdminBiz = courseHomeworkAdminBiz;
    }

    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(CourseInfoQuery query) {
        return getSuccessResponseVO(courseAdminBiz.loadDataList(query));
    }

    @RequestMapping("/getCourseInfoById")
    public ResponseVO getCourseInfoById(@NotBlank(message = "课程ID不能为空") String courseId) {
        return getSuccessResponseVO(courseAdminBiz.getCourseInfoById(courseId));
    }

    @RequestMapping("/loadHomeworkSubmitList")
    public ResponseVO loadHomeworkSubmitList(CourseHomeworkSubmitManageQuery query) {
        return getSuccessResponseVO(courseHomeworkAdminBiz.loadHomeworkSubmitList(query));
    }

    @RequestMapping("/getHomeworkSubmitDetail")
    public ResponseVO getHomeworkSubmitDetail(@NotBlank(message = "课程ID不能为空") String courseId,
                                              @NotBlank(message = "课时ID不能为空") String lessonId,
                                              @NotNull(message = "学生ID不能为空") Integer studentId) {
        return getSuccessResponseVO(courseHomeworkAdminBiz.getHomeworkSubmitDetail(courseId, lessonId, studentId));
    }

    @RequestMapping("/judgeHomeworkSubmit")
    public ResponseVO judgeHomeworkSubmit(@RequestBody @Validated @Valid CourseHomeworkJudgeDTO dto) {
        courseHomeworkAdminBiz.judgeHomeworkSubmit(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated(CourseSaveDTO.Create.class) CourseSaveDTO dto) {
        return getSuccessResponseVO(courseAdminBiz.add(dto));
    }

    @RequestMapping("/updateCourseInfoById")
    public ResponseVO updateCourseInfoById(@RequestBody @Validated(CourseSaveDTO.Update.class) CourseSaveDTO dto) {
        courseAdminBiz.updateCourseInfoById(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/finishRecord")
    public ResponseVO finishRecord(@NotBlank(message = "课程ID不能为空") String courseId) {
        courseAdminBiz.finishRecord(courseId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/saveStructure")
    public ResponseVO saveStructure(@RequestBody @Validated CourseStructureSaveDTO dto) {
        courseAdminBiz.saveCourseStructure(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteCourseInfoById")
    public ResponseVO deleteCourseInfoById(@NotBlank(message = "课程ID不能为空") String courseId) {
        courseAdminBiz.deleteCourseInfoById(courseId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(@NotBlank(message = "请选择需要删除的课程") String ids) {
        courseAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }
}
