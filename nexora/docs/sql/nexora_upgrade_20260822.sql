-- ============================================================
-- Nexora 数据库升级脚本（2026-08-22）
-- 用途：将现有 nexora 库无损升级到 docs/sql/nexora_base.sql 的 35 表基线
-- 特点：不 DROP 数据库、不 DROP 数据表，已有业务数据全部保留
-- 兼容：MySQL 5.7 / MySQL 8.x
-- 执行：mysql -uroot -proot --default-character-set=utf8mb4 nexora < 本文件
-- ============================================================

SET NAMES utf8mb4;

USE `nexora`;

-- 1. resource_directory 补充 dir_type（学生个人知识库系统目录标记）
SET @exists_dir_type := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resource_directory'
      AND COLUMN_NAME = 'dir_type'
);

SET @ddl_dir_type := IF(
    @exists_dir_type = 0,
    'ALTER TABLE `resource_directory` ADD COLUMN `dir_type` varchar(20) DEFAULT NULL COMMENT ''系统目录:raw/wiki/attachments;NULL=普通目录'' AFTER `parent_id`',
    'DO 1'
);

PREPARE stmt_dir_type FROM @ddl_dir_type;
EXECUTE stmt_dir_type;
DEALLOCATE PREPARE stmt_dir_type;

-- 2. 旧库若已存在新系统目录名，则回填 dir_type，避免重复初始化系统目录
UPDATE `resource_directory`
SET `dir_type` = CASE `dir_name`
    WHEN '原始资料' THEN 'raw'
    WHEN '知识页' THEN 'wiki'
    WHEN '附件' THEN 'attachments'
    ELSE `dir_type`
END
WHERE `owner_id` IS NOT NULL
  AND (`dir_type` IS NULL OR `dir_type` = '');

-- 3. 新增 user_wiki_profile（学生学习档案 / Wiki 用户画像）
CREATE TABLE IF NOT EXISTS `user_wiki_profile` (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生ID',
  `learning_goal` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学习目标',
  `key_questions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键问题（分号分隔）',
  `interest_subjects` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '感兴趣学科/主题（分号分隔）',
  `alias_terms` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义术语别名（分号分隔）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生学习档案（Wiki 用户画像）';

-- 4. 移除已废弃的 uk_username（20260822 v2：username 降级为昵称别名，可重复；登录唯一依据为 email）
SET @exists_uk_username := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_info'
      AND INDEX_NAME = 'uk_username'
);

SET @ddl_drop_uk_username := IF(
    @exists_uk_username > 0,
    'ALTER TABLE `user_info` DROP INDEX `uk_username`',
    'DO 1'
);

PREPARE stmt_drop_uk_username FROM @ddl_drop_uk_username;
EXECUTE stmt_drop_uk_username;
DEALLOCATE PREPARE stmt_drop_uk_username;

-- 5. 升级结果自检（应输出 user_wiki_profile 表 1 行、resource_directory.dir_type 列 1 行、uk_username 0 行）
SELECT TABLE_NAME AS check_item, 'table_exists' AS check_result
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_wiki_profile'
UNION ALL
SELECT CONCAT(TABLE_NAME, '.', COLUMN_NAME), 'column_exists'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resource_directory' AND COLUMN_NAME = 'dir_type'
UNION ALL
SELECT CONCAT(TABLE_NAME, '.', INDEX_NAME), 'index_exists'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_info' AND INDEX_NAME = 'uk_username';
