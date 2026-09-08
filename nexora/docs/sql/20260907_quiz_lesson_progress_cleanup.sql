-- 学习进度完成口径修复（2026-09-07）：清理"配置了启用测验但无任何作答记录"却被标记完成的课时进度
-- 根因：reportStudy 曾对配置测验的课时也标记 finished=1，导致课时卡显示"测验已通过"但从未作答、查看结果为空
-- 修复后口径：配置了启用测验的课时仅通过测验才算完成
-- 执行方式：mysql -uroot -p nexora < 20260907_quiz_lesson_progress_cleanup.sql
-- 影响范围：只清理"有启用测验 + 零作答"的完成标记；有作答记录或无测验课时的完成标记不受影响
-- 注意：course_lesson_quiz 与 course_study_lesson_progress 的 lesson_id 排序规则不同
-- （general_ci vs unicode_ci），JOIN 需 CONVERT 统一，否则报 Illegal mix of collations

DELETE p FROM course_study_lesson_progress p
INNER JOIN course_lesson_quiz q
    ON CONVERT(q.lesson_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = p.lesson_id
    AND q.quiz_mode > 0
    AND q.status = 1
WHERE p.finished = 1
  AND NOT EXISTS (
      SELECT 1 FROM practice_record r
      WHERE r.user_id = p.user_id
        AND r.biz_id = p.lesson_id
        AND r.source = 3
  );
