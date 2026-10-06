-- =============================================================
-- 课程「学习人数」回填（2026-10-07）
--
-- 背景：course_info.study_count 建表时设计为「冗余：学习行为触发计数」，但一直没有维护方
--       （全库无任何写入点，所有课程恒为 0）。2026-10-07 起：
--       ① 学生在「课程教材」页加入课程时 +1（退出后重新加入同样 +1，见 StudentCourseController.join）；
--       ② 提供用于「退出课程」的 decreaseStudyCount（当前无退出接口，接口上线时成对调用）；
--       ③ 本脚本按历史数据回填一次，口径 = 当前有效加入人数（course_enrollment.status = 1 的学生数）。
-- 执行：mysql --default-character-set=utf8mb4 -uroot -p nexora < 20261007_course_study_count.sql
-- 特性：幂等（重算覆盖，可重复执行）。
-- =============================================================

UPDATE `course_info` c
SET c.`study_count` = (
        SELECT COUNT(*) FROM `course_enrollment` e
        WHERE e.`course_id` = c.`course_id` AND e.`status` = 1
    ),
    c.`update_time` = NOW();

-- 复核：回填后与报名表一致性（应无输出）
SELECT c.course_id, c.course_name, c.study_count,
       (SELECT COUNT(*) FROM course_enrollment e WHERE e.course_id = c.course_id AND e.status = 1) AS enroll_count
FROM course_info c
WHERE c.study_count <> (SELECT COUNT(*) FROM course_enrollment e WHERE e.course_id = c.course_id AND e.status = 1);
