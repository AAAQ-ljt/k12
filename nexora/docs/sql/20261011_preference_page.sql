-- =====================================================================
-- 计划 C3：《我的学习偏好》受保护系统页（knowledge_doc.system_type）
--
-- 用途：偏好页在学生个人知识库里是"系统页"——根目录置顶、不可移动、不可删除、不做向量化、可重置。
--       system_type 取值：NULL=普通知识页，PREFERENCE=学习偏好页。
--
-- 幂等：字段不存在才加（动态 SQL 判断 information_schema）。
-- 执行方式：mysql -uroot -p nexora < 20261011_preference_page.sql
-- 【禁止改已存在的表结构】除本次新增字段外不动其它列。
-- =====================================================================

SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'knowledge_doc' AND column_name = 'system_type'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `knowledge_doc` ADD COLUMN `system_type` varchar(20) NULL DEFAULT NULL COMMENT ''系统页类型：NULL=普通页，PREFERENCE=学习偏好页（置顶/禁移动/禁删除/禁向量化）'' AFTER `folder_id`',
  'SELECT ''system_type 已存在，跳过''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) AS system_type_ready FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'knowledge_doc' AND column_name = 'system_type';
