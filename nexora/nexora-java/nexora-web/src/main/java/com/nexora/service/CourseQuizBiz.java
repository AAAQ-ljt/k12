package com.nexora.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.dto.LessonQuizAnswerDTO;
import com.nexora.dto.LessonQuizSubmitDTO;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseEnrollment;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.CourseLessonQuiz;
import com.nexora.entity.po.CourseStudyLessonProgress;
import com.nexora.entity.po.PracticeRecord;
import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.po.QuestionOption;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseChapterQuery;
import com.nexora.entity.query.QuestionInfoQuery;
import com.nexora.entity.query.QuestionOptionQuery;
import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import com.nexora.vo.LessonQuizStatusVO;
import com.nexora.vo.LessonQuizSubmitResultVO;
import com.nexora.vo.LessonQuizVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 学生端课时通关测验：答题面板数据、判分落练习流水、达标写课时完成、课程解锁状态计算。
 */
@Slf4j
@Service
public class CourseQuizBiz {

    /** 练习流水来源：课时通关测验 */
    private static final int PRACTICE_SOURCE_QUIZ = 3;

    private static final int QUIZ_MODE_CLOSED = 0;

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private CourseLessonQuizService courseLessonQuizService;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private QuestionOptionService questionOptionService;

    @Resource
    private PracticeRecordService practiceRecordService;

    @Resource
    private CourseStudyLessonProgressService courseStudyLessonProgressService;

    @Resource
    private CourseEnrollmentService courseEnrollmentService;

    /**
     * 课时测验答题面板数据；未配置/关闭时返回 null
     */
    public LessonQuizVO quiz(String userId, String lessonId) {
        CourseChapterLesson lesson = requireLesson(lessonId);
        requireCourseVisible(lesson.getCourseId());
        requireEnrolled(userId, lesson.getCourseId());
        CourseLessonQuiz quiz = courseLessonQuizService.getCourseLessonQuizByLessonId(lessonId);
        if (quiz == null || QUIZ_MODE_CLOSED == quiz.getQuizMode() || quiz.getStatus() == null || quiz.getStatus() != 1) {
            return null;
        }
        List<String> ids = parseQuestionIds(quiz.getQuestionIds());
        if (ids.isEmpty()) {
            return null;
        }
        List<QuestionInfo> questions = loadQuestions(ids);
        Map<String, Integer> scoreMap = parseQuestionScores(quiz.getQuizConfig());
        Map<String, List<QuestionOption>> optionMap = loadOptions(ids);

        LessonQuizVO vo = new LessonQuizVO();
        vo.setLessonId(lessonId);
        vo.setQuizMode(quiz.getQuizMode());
        vo.setPassScore(quiz.getPassScore() == null ? 60 : quiz.getPassScore());
        vo.setUnlockNext(quiz.getUnlockNext() == null ? 0 : quiz.getUnlockNext());
        List<LessonQuizVO.Question> questionVOs = new ArrayList<>();
        for (QuestionInfo question : questions) {
            LessonQuizVO.Question questionVO = new LessonQuizVO.Question();
            questionVO.setQuestionId(question.getQuestionId());
            questionVO.setQuestionType(question.getQuestionType());
            questionVO.setTitle(question.getTitle());
            questionVO.setScore(effectiveScore(question, scoreMap));
            List<LessonQuizVO.Option> optionVOs = new ArrayList<>();
            List<QuestionOption> options = optionMap.getOrDefault(question.getQuestionId(), List.of());
            // 判断题无选项时补 "正确/错误" 选项，保证前端有作答入口
            if (options.isEmpty() && isJudge(question.getQuestionType())) {
                options = judgeDefaultOptions();
            }
            for (QuestionOption option : options) {
                LessonQuizVO.Option optionVO = new LessonQuizVO.Option();
                optionVO.setOptionId(option.getOptionId());
                optionVO.setOptionLabel(option.getOptionLabel());
                optionVO.setOptionContent(option.getOptionContent());
                optionVOs.add(optionVO);
            }
            questionVO.setOptions(optionVOs);
            questionVOs.add(questionVO);
        }
        vo.setQuestions(questionVOs);
        return vo;
    }

    /**
     * 提交测验：判分、写练习流水（source=3, bizId=lessonId）、达标写课时完成标记。
     */
    @Transactional(rollbackFor = Exception.class)
    public LessonQuizSubmitResultVO submit(String userId, String stage, LessonQuizSubmitDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getLessonId())) {
            throw new BusinessException("课时ID不能为空");
        }
        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new BusinessException("请先完成作答");
        }
        CourseChapterLesson lesson = requireLesson(dto.getLessonId());
        requireCourseVisible(lesson.getCourseId());
        requireEnrolled(userId, lesson.getCourseId());
        CourseLessonQuiz quiz = courseLessonQuizService.getCourseLessonQuizByLessonId(dto.getLessonId());
        if (quiz == null || QUIZ_MODE_CLOSED == quiz.getQuizMode() || quiz.getStatus() == null || quiz.getStatus() != 1) {
            throw new BusinessException("该课时未开启通关测验");
        }
        List<String> ids = parseQuestionIds(quiz.getQuestionIds());
        if (ids.isEmpty()) {
            throw new BusinessException("测验题目为空，请联系管理员");
        }
        List<QuestionInfo> questions = loadQuestions(ids);

        Map<String, QuestionInfo> questionMap = new LinkedHashMap<>();
        for (QuestionInfo question : questions) {
            questionMap.put(question.getQuestionId(), question);
        }

        int passScore = quiz.getPassScore() == null ? 60 : quiz.getPassScore();
        // 判分口径只统计客观题：totalCount/totalScore 均为客观题口径，主观题仅展示不判分
        int totalCount = 0;
        int correctCount = 0;
        int score = 0;
        int totalScore = 0;
        Date now = new Date();
        Map<String, Integer> scoreMap = parseQuestionScores(quiz.getQuizConfig());
        boolean partialCredit = parsePartialCredit(quiz.getQuizConfig());
        // practice_record.duration 非空约束：前端可传答题用时，缺省按 0 记录
        int durationSeconds = dto.getDuration() == null || dto.getDuration() < 0
                ? 0 : Math.min(dto.getDuration(), 24 * 3600);
        List<LessonQuizSubmitResultVO.QuestionResult> results = new ArrayList<>();
        List<PracticeRecord> records = new ArrayList<>();

        for (QuestionInfo question : questions) {
            int questionScore = effectiveScore(question, scoreMap);
            LessonQuizAnswerDTO answer = findAnswer(dto.getAnswers(), question.getQuestionId());
            String userAnswer = answer == null ? null : answer.getAnswer();
            int judgeType = question.getQuestionType() == null ? 0 : question.getQuestionType();
            // 主观题（简答/解答/论述/材料）不做自动判分，也不占用客观题判分口径的总分与题数
            boolean subjective = judgeType >= 4;
            int earned = 0;
            boolean correct = false;
            if (!subjective) {
                earned = judgeScore(question, userAnswer, questionScore, partialCredit);
                correct = questionScore > 0 && earned >= questionScore;
                totalScore += questionScore;
                score += earned;
                if (correct) {
                    correctCount++;
                }
            }
            LessonQuizSubmitResultVO.QuestionResult result = new LessonQuizSubmitResultVO.QuestionResult();
            result.setQuestionId(question.getQuestionId());
            result.setTitle(question.getTitle());
            result.setUserAnswer(userAnswer == null ? "" : userAnswer);
            result.setCorrectAnswer(question.getAnswer() == null ? "" : question.getAnswer());
            result.setCorrect(correct);
            result.setScore(earned);
            result.setQuestionScore(questionScore);
            result.setSubjective(subjective);
            result.setAnalysis(question.getAnalysis());
            results.add(result);

            PracticeRecord record = new PracticeRecord();
            record.setUserId(userId);
            // knowledge_point_id 非空约束：历史题目可能缺失知识点，落空串保证流水可写
            record.setKnowledgePointId(StringTools.isEmpty(question.getKnowledgePointId())
                    ? "" : question.getKnowledgePointId());
            record.setStage(stage);
            record.setQuestionId(question.getQuestionId());
            record.setQuestionType(question.getQuestionType());
            record.setUserAnswer(userAnswer == null ? "" : userAnswer);
            record.setIsCorrect(correct ? 1 : 0);
            record.setScore(earned);
            record.setDuration(durationSeconds);
            record.setSource(PRACTICE_SOURCE_QUIZ);
            record.setBizId(dto.getLessonId());
            record.setCreateTime(now);
            records.add(record);
        }
        if (!records.isEmpty()) {
            practiceRecordService.addBatch(records);
        }

        // 全部为主观题的测验无可判分内容，不自动判定通过
        boolean passed = totalScore > 0 && score >= passScore;
        if (passed) {
            markLessonCompleted(userId, lesson, now);
        }

        LessonQuizSubmitResultVO resultVO = new LessonQuizSubmitResultVO();
        resultVO.setPassed(passed);
        resultVO.setCorrectCount(correctCount);
        resultVO.setTotalCount(totalCount);
        resultVO.setScore(score);
        resultVO.setTotalScore(totalScore);
        resultVO.setPassScore(passScore);
        resultVO.setResults(results);
        return resultVO;
    }

    /**
     * 课程内各课时测验与解锁状态（按顺序计算：上一严格门禁课时通过才解锁下一课时）
     */
    public List<LessonQuizStatusVO> quizStatus(String userId, String courseId) {
        requireCourseVisible(courseId);
        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        chapterQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapter> chapters = courseChapterService.findListByParam(chapterQuery);

        List<LessonQuizStatusVO> statusList = new ArrayList<>();
        boolean prevUnlocked = true;
        boolean prevStrict = false;
        boolean prevPassed = true;
        for (CourseChapter chapter : chapters) {
            CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
            lessonQuery.setCourseId(courseId);
            lessonQuery.setChapterId(chapter.getChapterId());
            lessonQuery.setOrderBy("sort asc, create_time asc");
            List<CourseChapterLesson> lessons = courseChapterLessonService.findListByParam(lessonQuery);
            for (CourseChapterLesson lesson : lessons) {
                CourseLessonQuiz quiz = courseLessonQuizService.getCourseLessonQuizByLessonId(lesson.getLessonId());
                boolean hasQuiz = quiz != null && quiz.getQuizMode() != null && QUIZ_MODE_CLOSED != quiz.getQuizMode()
                        && quiz.getStatus() != null && quiz.getStatus() == 1;
                boolean strict = hasQuiz && quiz.getUnlockNext() != null && quiz.getUnlockNext() == 1;
                boolean passed = hasQuiz && lessonPassed(userId, lesson.getLessonId());

                boolean unlocked = prevUnlocked && (!prevStrict || prevPassed);

                LessonQuizStatusVO vo = new LessonQuizStatusVO();
                vo.setLessonId(lesson.getLessonId());
                vo.setHasQuiz(hasQuiz);
                vo.setStrict(strict);
                vo.setPassed(passed);
                vo.setUnlocked(unlocked);
                statusList.add(vo);

                prevUnlocked = unlocked;
                prevStrict = strict;
                prevPassed = passed;
            }
        }
        return statusList;
    }

    private LessonQuizAnswerDTO findAnswer(List<LessonQuizAnswerDTO> answers, String questionId) {
        for (LessonQuizAnswerDTO answer : answers) {
            if (questionId.equals(answer.getQuestionId())) {
                return answer;
            }
        }
        return null;
    }

    /**
     * 单题客观判分：返回 0..questionScore；全对得满分，多选开启部分给分时可能介于两者之间
     */
    private int judgeScore(QuestionInfo question, String userAnswer, int questionScore, boolean partialCredit) {
        if (StringTools.isEmpty(userAnswer)) {
            return 0;
        }
        int type = question.getQuestionType() == null ? 0 : question.getQuestionType();
        String answer = question.getAnswer();
        if (StringTools.isEmpty(answer)) {
            return 0;
        }
        return switch (type) {
            case 0 -> answer.trim().equalsIgnoreCase(userAnswer.trim()) ? questionScore : 0;
            case 1 -> multipleChoiceScore(answer, userAnswer, questionScore, partialCredit);
            case 2 -> judgeMatch(answer, userAnswer) ? questionScore : 0;
            case 3 -> fillMatch(userAnswer, answer) ? questionScore : 0;
            default -> 0;
        };
    }

    private Set<Character> parseOptionLetters(String text) {
        Set<Character> letters = new HashSet<>();
        for (char c : text.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'H') {
                letters.add(c);
            }
        }
        return letters;
    }

    /** 多选题：默认全对才得分；开启部分给分时，未错选且至少选对一个按比例向下取整给分 */
    private int multipleChoiceScore(String correctAnswer, String userAnswer, int questionScore, boolean partialCredit) {
        Set<Character> correct = parseOptionLetters(correctAnswer);
        Set<Character> user = parseOptionLetters(userAnswer);
        if (correct.isEmpty() || user.isEmpty()) {
            return 0;
        }
        if (correct.equals(user)) {
            return questionScore;
        }
        if (!partialCredit) {
            return 0;
        }
        boolean hasWrongPick = user.stream().anyMatch(c -> !correct.contains(c));
        if (hasWrongPick) {
            return 0;
        }
        long hitCount = user.stream().filter(correct::contains).count();
        return (int) (hitCount * questionScore / correct.size());
    }

    /** 判断题：支持字母（A/B）与文本（对/错/正确/错误/是/否/√/×）两种作答归一比较 */
    private boolean judgeMatch(String correctAnswer, String userAnswer) {
        String c = normalizeJudge(correctAnswer);
        String u = normalizeJudge(userAnswer);
        return !c.isEmpty() && c.equals(u);
    }

    private String normalizeJudge(String text) {
        if (StringTools.isEmpty(text)) {
            return "";
        }
        String t = text.trim().toUpperCase();
        if (Arrays.asList("A", "B").contains(t)) {
            return t.equals("A") ? "对" : "错";
        }
        t = t.replace("√", "对").replace("×", "错").replace("X", "错");
        switch (t) {
            case "正确", "对", "是", "T", "TRUE" -> {
                return "对";
            }
            case "错误", "错", "否", "F", "FALSE" -> {
                return "错";
            }
            default -> {
                return t;
            }
        }
    }

    /** 填空题：支持多标准答案（| 或 ；/; 分隔）；去空格后精确匹配，或作答包含完整标准答案（容忍多写修饰，防单字误判） */
    private boolean fillMatch(String userAnswer, String correctAnswer) {
        String u = stripSpaces(userAnswer);
        if (u.isEmpty()) {
            return false;
        }
        for (String candidate : correctAnswer.split("[|；;]")) {
            String c = stripSpaces(candidate);
            if (c.isEmpty()) {
                continue;
            }
            if (u.equalsIgnoreCase(c) || u.toLowerCase().contains(c.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /** quiz_config.partialCredit：多选题漏选（未错选）按比例部分给分，默认关闭 */
    private boolean parsePartialCredit(String quizConfig) {
        if (StringTools.isEmpty(quizConfig)) {
            return false;
        }
        try {
            JSONObject config = JSON.parseObject(quizConfig);
            return config != null && Boolean.TRUE.equals(config.getBoolean("partialCredit"));
        } catch (Exception e) {
            return false;
        }
    }

    private String stripSpaces(String text) {
        if (StringTools.isEmpty(text)) {
            return "";
        }
        return text.replaceAll("[\\s　\\u3000\\n\\r\\t]", "");
    }

    private boolean isJudge(Integer questionType) {
        return questionType != null && questionType == 2;
    }

    private List<QuestionOption> judgeDefaultOptions() {
        List<QuestionOption> list = new ArrayList<>();
        QuestionOption t = new QuestionOption();
        t.setOptionLabel("A");
        t.setOptionContent("正确");
        QuestionOption f = new QuestionOption();
        f.setOptionLabel("B");
        f.setOptionContent("错误");
        list.add(t);
        list.add(f);
        return list;
    }

    /** 生效分值：quiz_config.questionScores 优先，缺省取题目自带分值 */
    private int effectiveScore(QuestionInfo question, Map<String, Integer> scoreMap) {
        Integer configured = scoreMap == null ? null : scoreMap.get(question.getQuestionId());
        if (configured != null && configured > 0) {
            return Math.min(configured, 100);
        }
        return question.getScore() == null ? 5 : question.getScore();
    }

    private Map<String, Integer> parseQuestionScores(String quizConfig) {
        if (StringTools.isEmpty(quizConfig)) {
            return Map.of();
        }
        try {
            JSONObject config = JSON.parseObject(quizConfig);
            JSONObject scores = config == null ? null : config.getJSONObject("questionScores");
            Map<String, Integer> map = new LinkedHashMap<>();
            if (scores != null) {
                for (String key : scores.keySet()) {
                    Integer value = scores.getInteger(key);
                    if (value != null) {
                        map.put(key, value);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            log.warn("课时测验分值配置解析失败", e);
            return Map.of();
        }
    }

    private void markLessonCompleted(String userId, CourseChapterLesson lesson, Date now) {
        CourseStudyLessonProgress progress =
                courseStudyLessonProgressService.getCourseStudyLessonProgressByUserIdAndLessonId(userId, lesson.getLessonId());
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

    private boolean lessonPassed(String userId, String lessonId) {
        CourseStudyLessonProgress progress =
                courseStudyLessonProgressService.getCourseStudyLessonProgressByUserIdAndLessonId(userId, lessonId);
        return progress != null && progress.getFinished() != null && progress.getFinished() == 1;
    }

    private CourseChapterLesson requireLesson(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException("课时ID不能为空");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(lessonId);
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        return lesson;
    }

    private void requireCourseVisible(String courseId) {
        if (StringTools.isEmpty(courseId)) {
            throw new BusinessException("课程不存在");
        }
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或暂不可用");
        }
    }

    /** 课程须先加入才可学习（course_enrollment.status=1） */
    private void requireEnrolled(String userId, String courseId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("请先登录");
        }
        CourseEnrollment enrollment =
                courseEnrollmentService.getCourseEnrollmentByUserIdAndCourseId(userId, courseId);
        if (enrollment == null || enrollment.getStatus() == null || enrollment.getStatus() != 1) {
            throw new BusinessException("请先加入该课程后再学习");
        }
    }

    private List<String> parseQuestionIds(String jsonText) {
        if (StringTools.isEmpty(jsonText)) {
            return new ArrayList<>();
        }
        try {
            JSONArray array = JSON.parseArray(jsonText);
            List<String> ids = new ArrayList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    String id = array.getString(i);
                    if (!StringTools.isEmpty(id)) {
                        ids.add(id);
                    }
                }
            }
            return ids;
        } catch (Exception e) {
            log.warn("课时测验题目ID解析失败", e);
            return new ArrayList<>();
        }
    }

    private List<QuestionInfo> loadQuestions(List<String> questionIds) {
        QuestionInfoQuery query = new QuestionInfoQuery();
        query.setQuestionIds(questionIds);
        List<QuestionInfo> questions = questionInfoService.findListByParam(query);
        questions.sort((a, b) -> questionIds.indexOf(a.getQuestionId()) - questionIds.indexOf(b.getQuestionId()));
        return questions;
    }

    private Map<String, List<QuestionOption>> loadOptions(List<String> questionIds) {
        QuestionOptionQuery query = new QuestionOptionQuery();
        query.setQuestionIds(questionIds);
        query.setOrderBy("sort asc, option_id asc");
        List<QuestionOption> options = questionOptionService.findListByParam(query);
        Map<String, List<QuestionOption>> map = new LinkedHashMap<>();
        for (QuestionOption option : options) {
            map.computeIfAbsent(option.getQuestionId(), k -> new ArrayList<>()).add(option);
        }
        return map;
    }
}