-- =====================================================================
-- 二期 A-7：积分兑换（上传额度扩容 / 朗读音色解锁）
--
-- 幂等：建表 IF NOT EXISTS；配置用 ON DUPLICATE KEY UPDATE config_key = config_key
--       （已存在就保留现值，不覆盖运营在管理端改过的价格）；徽章按主键幂等。
--
-- 执行方式（与既往增量脚本一致，必须带库名，脚本内用 DATABASE()）：
--   mysql -uroot -p nexora < 20261008_point_exchange.sql
-- 【禁止改已存在的表结构】本脚本只新增表/配置/徽章数据。
-- =====================================================================

-- ---------- ① 兑换记录（一次兑换一条，单号为主键天然幂等）----------
CREATE TABLE IF NOT EXISTS `student_point_exchange` (
  `exchange_id` varchar(32) NOT NULL COMMENT '兑换单号',
  `user_id` varchar(32) NOT NULL COMMENT '学生ID',
  `stage` varchar(20) NULL DEFAULT NULL COMMENT '学段【冗余快照】',
  `item_code` varchar(40) NOT NULL COMMENT '商品码：UPLOAD_QUOTA / VOICE_<音色码>',
  `item_name` varchar(60) NOT NULL COMMENT '商品名（快照，便于流水展示）',
  `cost_points` int(11) NOT NULL COMMENT '花费可用积分',
  `balance_after` int(11) NOT NULL COMMENT '兑换后可用积分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  PRIMARY KEY (`exchange_id`) USING BTREE,
  INDEX `idx_user_time`(`user_id`, `create_time`) USING BTREE,
  INDEX `idx_user_item`(`user_id`, `item_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生积分兑换记录' ROW_FORMAT = Dynamic;

-- ---------- ② GAME 组新增：兑换与路径/连击奖励参数 ----------
INSERT INTO `system_config` (`config_group`, `config_key`, `config_value`, `config_type`, `description`, `status`)
VALUES
  ('GAME', 'EXCHANGE_UPLOAD_QUOTA_COST', '200', 'INT',    '兑换「上传额度扩容」花费积分', 1),
  ('GAME', 'EXCHANGE_UPLOAD_QUOTA_MB',   '200', 'INT',    '每次额度扩容增加的存储空间（MB）', 1),
  ('GAME', 'EXCHANGE_UPLOAD_QUOTA_MAX',  '3',   'INT',    '额扩容最多可兑换次数（0=不限制）', 1),
  ('GAME', 'EXCHANGE_VOICE_COST',        '150', 'INT',    '解锁一个朗读音色花费积分', 1),
  ('GAME', 'VOICE_FREE_CODES',           'mimo_default,冰糖,白桦', 'STRING', '免费朗读音色（系统默认 + 小低/小高默认）', 1),
  ('GAME', 'VOICE_UNLOCK_CODES',         '茉莉,苏打,Mia,Chloe,Milo,Dean', 'STRING', '可兑换解锁的朗读音色（逗号分隔）', 1),
  ('GAME', 'POINT_PATH_NODE',            '15',  'INT',    '学习路径节点完成（节点内课时全部学完）积分', 1),
  ('GAME', 'POINT_PATH_DONE',            '50',  'INT',    '整条学习路径完成积分', 1)
ON DUPLICATE KEY UPDATE
  `config_key` = `config_key`;

-- ---------- ③ 徽章新增：路径通学者（整条学习路径完成）----------
INSERT INTO `game_badge` (`badge_id`, `name`, `description`, `icon`, `stage_scope`, `rule_type`, `rule_value`, `reward_points`, `sort`, `status`)
VALUES
  ('bdg_path_done', '路径通学者', '完整走完一条学习路径', '🧭', 'JUNIOR,SENIOR', 'PATH_DONE_COUNT', 1, 30, 13, 1)
ON DUPLICATE KEY UPDATE
  `badge_id` = `badge_id`;
