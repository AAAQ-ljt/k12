-- 答题批阅功能（2026-09-06）：practice_record 增加人工批阅列
-- 用途：管理端「答题批阅」页——主观题（简答/解答/论述/材料）人工打分与评语
-- 执行方式：mysql -uroot -p nexora < 20260906_practice_review.sql
-- 注意：执行后请将 docs/sql/nexora.sql 全量基线重新导出同步给团队

ALTER TABLE `practice_record`
  ADD COLUMN `review_status` tinyint NOT NULL DEFAULT 2 COMMENT '批阅状态：0待批阅 1已批阅 2无需批阅(客观题)',
  ADD COLUMN `review_score` int NULL DEFAULT NULL COMMENT '人工批阅得分',
  ADD COLUMN `reviewer_id` varchar(40) NULL DEFAULT NULL COMMENT '批阅人（管理员账号）',
  ADD COLUMN `review_comment` varchar(500) NULL DEFAULT NULL COMMENT '批阅评语',
  ADD COLUMN `review_time` datetime NULL DEFAULT NULL COMMENT '批阅时间',
  ADD INDEX `idx_review_status`(`review_status`);

-- 存量回填：主观题流水进入待批阅队列（客观题保持默认 2 无需批阅）
UPDATE practice_record SET review_status = 0 WHERE question_type >= 4;
