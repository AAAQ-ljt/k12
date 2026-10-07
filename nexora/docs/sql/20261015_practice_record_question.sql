-- =====================================================================
-- 计划 C5-1：练习记录补"题干与参考答案"（让 AI 能逐题复盘）
--
-- 背景：practice_record 已存学生作答/对错/得分/来源/时间/归属节点(biz_id=itemId)，
--       但**没有题干与参考答案**（路径快测是 LLM 现场出题，question_id 为空串），
--       所以复盘只能停在汇总层面。
--
-- 幂等：字段不存在才加（动态 SQL 判断 information_schema）。
-- 执行方式：mysql -uroot -p nexora < 20261015_practice_record_question.sql
-- 【禁止改已存在的表结构】除本次新增两列外不动其它列。
-- =====================================================================

SET NAMES utf8mb4;

SET @c1 := (SELECT COUNT(*) FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'practice_record' AND column_name = 'question_text');
SET @ddl1 := IF(@c1 = 0,
  'ALTER TABLE `practice_record` ADD COLUMN `question_text` varchar(500) NULL DEFAULT NULL COMMENT ''题干（选项题含选项，截断 500 字；早期记录为空）'' AFTER `user_answer`',
  'SELECT ''question_text 已存在，跳过''');
PREPARE s1 FROM @ddl1; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @c2 := (SELECT COUNT(*) FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'practice_record' AND column_name = 'correct_answer');
SET @ddl2 := IF(@c2 = 0,
  'ALTER TABLE `practice_record` ADD COLUMN `correct_answer` varchar(200) NULL DEFAULT NULL COMMENT ''参考答案（客观题答案/主观题评分要点，截断 200 字）'' AFTER `question_text`',
  'SELECT ''correct_answer 已存在，跳过''');
PREPARE s2 FROM @ddl2; EXECUTE s2; DEALLOCATE PREPARE s2;

SELECT COUNT(*) AS 两列就绪 FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'practice_record'
  AND column_name IN ('question_text', 'correct_answer');
