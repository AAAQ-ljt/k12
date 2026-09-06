-- 学生加入课程记录表（2026-09-06）
-- 用途：学生端「加入课程」功能，学生须先加入课程才可学习课程内容
-- 执行方式：mysql -uroot -p nexora < 20260906_course_enrollment.sql
-- 注意：执行后请重新导出 docs/sql/nexora_base.sql 全量基线（当前基线已落后线上库，含 practice_record.biz_id 漂移）

CREATE TABLE IF NOT EXISTS `course_enrollment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生ID',
  `course_id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1已加入 0已退出',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`,`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生加入课程记录表';

-- 存量选择题答案回填：从选项表 is_answer 反向推导 question_info.answer（只补空值，不改已有值）
-- 背景：修复前手动新增的选择题 answer 落库为空，学生端判分全错
UPDATE question_info qi
SET qi.answer = (
    SELECT GROUP_CONCAT(qo.option_label ORDER BY qo.option_label SEPARATOR '')
    FROM question_option qo
    WHERE qo.question_id = qi.question_id AND qo.is_answer = 1
)
WHERE qi.question_type IN (0, 1)
  AND (qi.answer IS NULL OR qi.answer = '')
  AND EXISTS (
    SELECT 1 FROM question_option qo2
    WHERE qo2.question_id = qi.question_id AND qo2.is_answer = 1
  );
