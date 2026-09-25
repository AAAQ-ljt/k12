-- ============================================================
-- AI 消耗统计表 ai_usage_record
-- 用途：工作台「Token 总消耗 / 图片生成次数」的数据源
--       CHAT   = 大模型调用（对话、意图路由、绘本文案、动画脚本、学习路径、出题、文档整理、模型测试）
--       IMAGE  = 文生图调用（学生绘本插图 / 单页补画 / 管理端生图测试），按张计次
-- 写入方：AiUsageRecordComponent（对话类由 AiUsageAdvisor 统一上报，生图类由 ImageProviderRouter 埋点）
-- 执行方式：mysql -uroot -p nexora < 20260925_ai_usage_record.sql
-- 特性：只新增表，不改动任何既有表结构；脚本可重复执行
-- ============================================================

CREATE TABLE IF NOT EXISTS `ai_usage_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `usage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消耗类型：CHAT大模型调用 IMAGE文生图调用',
  `model` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '模型或供应商编码【对话=模型名，生图=供应商编码，历史回填为空串】',
  `call_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '调用次数',
  `prompt_tokens` bigint(20) NOT NULL DEFAULT 0 COMMENT '输入token累计【生图为0】',
  `completion_tokens` bigint(20) NOT NULL DEFAULT 0 COMMENT '输出token累计【生图为0】',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `uk_date_type_model`(`stat_date`, `usage_type`, `model`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 消耗统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 历史回填：agent_message 已落库的对话 token 按天汇总进统计表（新表首次执行时补历史）
-- 只回填「今天之前」：今天起的消耗由应用实时写入，重复回填会与实时行重复计数
-- 重复执行安全：命中唯一键时按回填值覆盖（同日同类型同模型的回填结果恒定）
-- ----------------------------
INSERT INTO `ai_usage_record` (`stat_date`, `usage_type`, `model`, `call_count`, `prompt_tokens`, `completion_tokens`)
SELECT DATE(`create_time`), 'CHAT', '', COUNT(*), COALESCE(SUM(`prompt_tokens`), 0), COALESCE(SUM(`completion_tokens`), 0)
FROM `agent_message`
WHERE `create_time` < CURDATE()
  AND (`prompt_tokens` > 0 OR `completion_tokens` > 0)
GROUP BY DATE(`create_time`)
ON DUPLICATE KEY UPDATE
  `call_count` = VALUES(`call_count`),
  `prompt_tokens` = VALUES(`prompt_tokens`),
  `completion_tokens` = VALUES(`completion_tokens`);

-- ----------------------------
-- 历史回填：存量的绘本插图与 AI 绘画按「已产出图片张数」补进统计表
-- 绘本按 ext_json.pages 里实际带 imageFile 的页数计（供应商出图 1 次 1 张，等价于调用张数）
-- 覆盖不到的部分（管理端生图测试、失败重试）无历史落库，无法追溯
-- 同样只回填「今天之前」，且重复执行按回填值覆盖
-- ----------------------------
INSERT INTO `ai_usage_record` (`stat_date`, `usage_type`, `model`, `call_count`, `prompt_tokens`, `completion_tokens`)
SELECT DATE(`create_time`), 'IMAGE', '',
       SUM(CASE WHEN `resource_type` = 'IMAGE' THEN 1
                ELSE (LENGTH(COALESCE(`ext_json`, '')) - LENGTH(REPLACE(COALESCE(`ext_json`, ''), '"imageFile"', '')))
                     / LENGTH('"imageFile"')
           END),
       0, 0
FROM `resource_info`
WHERE `resource_type` IN ('PICTURE_BOOK', 'IMAGE')
  AND `create_time` < CURDATE()
GROUP BY DATE(`create_time`)
ON DUPLICATE KEY UPDATE
  `call_count` = VALUES(`call_count`);
