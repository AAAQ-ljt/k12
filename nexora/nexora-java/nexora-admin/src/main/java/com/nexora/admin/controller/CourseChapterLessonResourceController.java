package com.nexora.admin.controller;

import com.nexora.admin.biz.CourseBiz;
import com.nexora.admin.dto.LessonResourceBindDTO;
import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.CourseLessonResourceVO;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课时资源绑定 Controller
 */
@RestController
@RequestMapping("/courseChapterLessonResource")
public class CourseChapterLessonResourceController extends ABaseController {

    @Resource
    private CourseBiz courseBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<List<CourseLessonResourceVO>> loadDataList(@RequestParam String lessonId) {
        return getSuccessResponseVO(courseBiz.lessonResourceList(lessonId));
    }

    @PostMapping("/bind")
    public ResponseVO<Void> bind(@RequestBody LessonResourceBindDTO dto) {
        courseBiz.bindResources(dto);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam Integer id) {
        courseBiz.unbindResource(id);
        return getSuccessResponseVO(null);
    }
}
