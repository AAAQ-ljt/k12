-- =====================================================================
-- 计划 D1：新手引导进度（student_onboarding）
--
-- 用途：记录学生是否看过欢迎卡、看到第几步、当前引导版本，支持"随时重看"与"引导有更新"提示。
--       步骤文案与目标选择器放在前端配置（按学段筛），后端只存进度，改文案不需要发版后端。
--
-- 幂等：建表 IF NOT EXISTS。
-- 执行方式：mysql -uroot -p nexora < 20261012_student_onboarding.sql
-- 【禁止改已存在的表结构】本脚本只新增表。
-- =====================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `student_onboarding` (
  `user_id` varchar(32) NOT NULL COMMENT '学生ID',
  `stage_snapshot` varchar(20) NULL DEFAULT NULL COMMENT '首次引导时的学段（用于判断脚本是否需要变）',
  `version` int(11) NOT NULL DEFAULT 0 COMMENT '已看完的引导版本号（与前端脚本版本比对，用于"有更新"提示）',
  `welcome_seen` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否看过欢迎卡：0否 1是',
  `skipped` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否选择"我先自己看看"（选过则不再自动弹）',
  `steps_done` varchar(400) NULL DEFAULT NULL COMMENT '已完成步骤（JSON 数组，如 ["ai-tutor","course"]），支持中途退出后续播',
  `finished_time` datetime NULL DEFAULT NULL COMMENT '最近一次走完导览的时间',
  `last_open_time` datetime NULL DEFAULT NULL COMMENT '最近一次打开「引导中心」的时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '新手引导进度（计划D1）' ROW_FORMAT = Dynamic;
