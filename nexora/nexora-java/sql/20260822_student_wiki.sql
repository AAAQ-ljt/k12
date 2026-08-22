-- ============================================================
-- 20260822 学生个人知识库三层化（P0 主线 1）
-- raw / wiki / attachments 系统目录 + 学习档案表
-- 执行环境：nexora 库（MySQL 8）
-- 注意：仅在既有 DDL 之上做增量，不改动任何已有脚本
-- ============================================================

-- 1) resource_directory 增加目录类型：raw/wiki/attachments 为系统目录，NULL=普通目录
ALTER TABLE `resource_directory`
    ADD COLUMN `dir_type` varchar(20) DEFAULT NULL COMMENT '目录类型：raw/wiki/attachments 系统目录；NULL=普通目录' AFTER `parent_id`;

-- 2) 学生学习档案（个人 Wiki schema 的用户可见视图：学习目标 / 关键问题 / 兴趣 / 术语叫法）
CREATE TABLE IF NOT EXISTS `user_wiki_profile` (
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生ID',
  `learning_goal` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学习目标',
  `key_questions` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键问题（多个用分号分隔）',
  `interest_subjects` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '感兴趣学科/主题（多个用分号分隔）',
  `alias_terms` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自己的术语叫法（多个用分号分隔）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生学习档案（个人Wiki用户视图）';