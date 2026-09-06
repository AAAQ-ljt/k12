package com.nexora.admin.controller;

import com.nexora.admin.biz.CourseBiz;
import com.nexora.admin.dto.LessonQuizSaveDTO;
import com.nexora.admin.component.LessonQuizTaskComponent;
import com.nexora.admin.vo.LessonQuizTaskVO;
import com.nexora.admin.vo.LessonQuizDetailVO;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
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
 * 课程课时管理 Controller
 */
@RestController
@RequestMapping("/courseChapterLesson")
public class CourseChapterLessonController extends ABaseController {

    @Resource
    private CourseBiz courseBiz;

    @Resource
    private LessonQuizTaskComponent lessonQuizTaskComponent;

    @GetMapping("/loadDataList")
    public ResponseVO<List<CourseChapterLesson>> loadDataList(@RequestParam(required = false) String chapterId,
                                                              @RequestParam(required = false) String courseId) {
        return getSuccessResponseVO(courseBiz.lessonList(chapterId, courseId));
    }

    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody CourseChapterLesson bean) {
        return getSuccessResponseVO(courseBiz.addLesson(bean));
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody CourseChapterLesson bean) {
        courseBiz.updateLesson(bean);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String lessonId) {
        courseBiz.deleteLesson(lessonId);
        return getSuccessResponseVO(null);
    }

    @GetMapping("/quizDetail")
    public ResponseVO<LessonQuizDetailVO> quizDetail(@RequestParam String lessonId) {
        return getSuccessResponseVO(courseBiz.lessonQuizDetail(lessonId));
    }

    @PostMapping("/quizSave")
    public ResponseVO<Void> quizSave(@RequestBody LessonQuizSaveDTO dto) {
        courseBiz.saveLessonQuiz(dto);
        return getSuccessResponseVO(null);
    }

    /**
     * AI 出题异步任务提交：立即返回 taskId，前端轮询 quizTask 获取进度；完成后自动写入课时测验配置
     */
    @PostMapping("/quizGenerateAsync")
    public ResponseVO<LessonQuizTaskVO> quizGenerateAsync(@RequestBody LessonQuizSaveDTO dto) {
        return getSuccessResponseVO(courseBiz.startQuizGenerateAsync(dto));
    }

    @GetMapping("/quizTask")
    public ResponseVO<LessonQuizTaskVO> quizTask(@RequestParam String taskId) {
        return getSuccessResponseVO(lessonQuizTaskComponent.get(taskId));
    }

    @DeleteMapping("/quizDel")
    public ResponseVO<Void> quizDel(@RequestParam String lessonId) {
        courseBiz.deleteLessonQuiz(lessonId);
        return getSuccessResponseVO(null);
    }
}
