-- =============================================================
-- AI 会话置顶（2026-10-06）
--
-- 用途：学生端「AI 助教」会话列表支持置顶（排序改为 置顶优先 + 最后消息时间倒序），
--       配合 agent_session.scene 的场景筛选（3 = 编程练习，会话按来源分开）。
-- 执行：mysql --default-character-set=utf8mb4 -uroot -p < 20261006_agent_session_top.sql
-- 特性：幂等（列/索引已存在则跳过），可重复执行。
-- 说明：scene 取值扩为 0 自由对话 / 1 课程引导 / 2 路径引导 / 3 编程练习，无需改表结构。
-- =============================================================

SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'agent_session' AND COLUMN_NAME = 'top');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `agent_session` ADD COLUMN `top` tinyint(4) NOT NULL DEFAULT 0 COMMENT ''置顶：0否 1是'' AFTER `status`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'agent_session' AND INDEX_NAME = 'idx_user_top_time');
SET @ddl2 := IF(@idx_exists = 0,
  'ALTER TABLE `agent_session` ADD INDEX `idx_user_top_time`(`user_id`, `top`, `last_message_time`)',
  'SELECT 1');
PREPARE stmt2 FROM @ddl2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 历史数据：把「编程环境」AI 陪学产生的旧会话标记为编程场景（scene=3），
-- 使其在学生端会话列表里归入「编程练习」，不再混在普通对话里（按标题前缀识别，幂等）。
UPDATE `agent_session`
SET `scene` = 3
WHERE `scene` = 0 AND `title` LIKE '%编程环境%';

-- 比赛教练模式的历史会话（提问前缀是「这是比赛中的题目…」）同样归入编程场景
UPDATE `agent_session`
SET `scene` = 3
WHERE `scene` = 0 AND `title` LIKE '这是比赛中的题目%';
