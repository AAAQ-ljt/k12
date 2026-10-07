-- =====================================================================
-- 计划 C1：学生画像（student_profile）
--
-- 用途：让 AI 助教在**不调工具**时也知道学生大概情况（学段、积分、最弱知识点、当前路径节点），
--       对话时把一份 120 字左右的"画像速览"注入系统提示词；细节仍由 MCP 工具按需查询。
--
-- 幂等：建表 IF NOT EXISTS；字段全部可空（增量重建画像时按需覆盖）。
-- 执行方式：mysql -uroot -p nexora < 20261009_student_profile.sql
-- 【禁止改已存在的表结构】本脚本只新增表。
-- =====================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `student_profile` (
  `user_id` varchar(32) NOT NULL COMMENT '学生ID',
  `stage` varchar(20) NULL DEFAULT NULL COMMENT '学段快照',
  `grade` varchar(20) NULL DEFAULT NULL COMMENT '年级快照',
  `mastery_summary` varchar(600) NULL DEFAULT NULL COMMENT '掌握度摘要（已掌握/进行中/未解锁数量、平均分、最弱3个知识点）',
  `path_summary` varchar(400) NULL DEFAULT NULL COMMENT '路径摘要（进行中路径数、当前路径与节点、进度）',
  `weak_points` varchar(400) NULL DEFAULT NULL COMMENT '薄弱知识点清单（掌握度<70 且有练习，最多5条）',
  `point_summary` varchar(200) NULL DEFAULT NULL COMMENT '积分摘要（累计/可用/连续天数/等级称谓）',
  `preference_doc_id` varchar(32) NULL DEFAULT NULL COMMENT '《我的学习偏好》知识页ID（计划C3，会话锚点）',
  `dirty` tinyint(4) NOT NULL DEFAULT 0 COMMENT '待刷新标记：1=学习事件已发生，下次对话前重建画像',
  `refresh_time` datetime NULL DEFAULT NULL COMMENT '最近一次重建时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_dirty`(`dirty`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生画像（计划C1）' ROW_FORMAT = Dynamic;
