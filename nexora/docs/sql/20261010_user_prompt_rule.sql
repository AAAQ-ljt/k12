-- =====================================================================
-- 计划 C2：用户端提示词规则（user_prompt_rule）
--
-- 用途：学生用受约束的自定义规则调整 AI 的回答方式（称呼、长短、风格、难度、示例偏好、禁止事项）。
--       规则拼进系统提示词的**第四层**（优先级低于平台安全规则、平台事实规则与学段模板）。
--
-- 幂等：建表 IF NOT EXISTS；配置用 ON DUPLICATE KEY UPDATE config_key = config_key（存在则保留现值）。
-- 执行方式：mysql -uroot -p nexora < 20261010_user_prompt_rule.sql
-- 【禁止改已存在的表结构】本脚本只新增表与配置行。
-- =====================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_prompt_rule` (
  `rule_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `user_id` varchar(32) NOT NULL COMMENT '学生ID',
  `rule_type` varchar(32) NOT NULL COMMENT '类型：CALL_ME 称呼/LENGTH 长短/STYLE 风格/DIFFICULTY 难度/EXAMPLE 示例偏好/LANGUAGE 语言/FORBID 禁止事项',
  `rule_value` varchar(120) NOT NULL COMMENT '规则内容（学生原话，长度上限 60 字）',
  `scope` varchar(16) NOT NULL DEFAULT 'GLOBAL' COMMENT '生效范围：GLOBAL 全部对话 / SCENE 仅某场景（预留）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `source` varchar(16) NOT NULL DEFAULT 'STUDENT' COMMENT '来源：STUDENT 学生自填 / AI_SUGGEST AI提议后学生确认 / SYSTEM 系统默认 / ADMIN 管理端',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`) USING BTREE,
  INDEX `idx_user_status`(`user_id`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户端提示词规则（计划C2）' ROW_FORMAT = Dynamic;

-- ---------- 灰度开关与黑名单（GAME 组之外新开 AI_MODEL 组键，与对话供应商同组便于一处管理） ----------
INSERT INTO `system_config` (`config_group`, `config_key`, `config_value`, `config_type`, `description`, `status`)
VALUES
  ('AI_MODEL', 'user_rule_enabled',  '1', 'INT', '用户端提示词规则总开关（0=关闭，全部学生规则不注入）', 1),
  ('AI_MODEL', 'user_rule_max_count','5', 'INT', '每名学生最多可启用的规则条数', 1),
  ('AI_MODEL', 'user_rule_blacklist','忽略以上,忽略前面,忽略所有,系统提示词,系统指令,提示词全文,developer mode,越狱,jailbreak,无限制模式,扮演不受限',
   'STRING', '用户规则黑名单（命中即拒绝保存，逗号分隔；越权提示词注入防护）', 1)
ON DUPLICATE KEY UPDATE
  `config_key` = `config_key`;
