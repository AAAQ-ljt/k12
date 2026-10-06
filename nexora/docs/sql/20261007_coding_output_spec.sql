-- =============================================================
-- 编程题「输出契约」三列（2026-10-07）
--
-- 用途：严格格式输出的题目，学生需要知道「要打印成什么样」。输出要求 + 输出示例随题目
--       下发给学生端展示；numeric_tolerant 开启后，两边都能解析为数字的行按数值比较
--       （78.50 与 78.5 视为相同），避免「数值写法不同」被误判为没做对。
-- 口径：见 docs/二期规划设计-20261006.md §5.1.5（判分公平性修复与「输出契约」设计）。
-- 执行：mysql --default-character-set=utf8mb4 -uroot -p nexora < 20261007_coding_output_spec.sql
--       ★ 必须带库名 nexora：脚本用 DATABASE() 判断列是否存在，不带库名会报 ERROR 1046 No database selected
-- 特性：幂等（列已存在则跳过），可重复执行；不改动、不删除任何现有列。
-- 配套：判定方式=精确匹配的题目，管理端已强制要求填写「输出要求 + 输出示例」；
--       存量 44 题的 output_spec/output_example/expected_output 回填另行提供
--       （AI 出初稿 + 人工审核，重点看示例是否泄题、期望输出与参考答案真实输出是否逐字一致）。
-- =============================================================

-- 1. 输出要求
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'coding_problem' AND COLUMN_NAME = 'output_spec');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `coding_problem` ADD COLUMN `output_spec` text NULL COMMENT ''输出要求（打印什么/几行/小数位/单位/标点用英文半角），随题目下发'' AFTER `judge_type`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 输出示例（用另一组数据演示格式，禁止用本题数据以免泄题）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'coding_problem' AND COLUMN_NAME = 'output_example');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `coding_problem` ADD COLUMN `output_example` text NULL COMMENT ''输出示例（用另一组数据演示格式，禁止用本题数据以免泄题），随题目下发'' AFTER `output_spec`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 数值容差开关
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'coding_problem' AND COLUMN_NAME = 'numeric_tolerant');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `coding_problem` ADD COLUMN `numeric_tolerant` tinyint(4) NOT NULL DEFAULT 0 COMMENT ''数值容差：0否 1是（纯数值行按数值比较，78.50 与 78.5 相同）'' AFTER `expected_pattern`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
