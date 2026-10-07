-- =============================================================
-- 二期·积分游戏化底座（2026-10-07）
--
-- 用途：统一积分账户 + 幂等积分流水 + 徽章体系，作为全站「学习激励总线」的底座。
--       口径见 docs/二期规划设计-20261006.md §3（A-1 数据模型 / A-2 规则 / A-4 徽章）。
--
-- 三张新表 + 一组配置：
--   ① student_point_account   积分账户（一人一行；等级/连续天数/排行榜数据源）
--   ② student_point_record    积分流水（唯一索引 uk_user_biz = 数据库层面防重放）
--   ③ game_badge              徽章定义（声明式规则 rule_type + rule_value）
--   ④ student_badge_record    徽章解锁记录
--   ⑤ system_config(GAME 组)  各规则分值 / 每日上限 / 等级阶梯 / 连击档位（不建新表，复用既有配置页范式）
--
-- 执行：mysql --default-character-set=utf8mb4 -uroot -p nexora < 20261007_point_gamification.sql
-- 特性：幂等（CREATE TABLE IF NOT EXISTS + INSERT ... ON DUPLICATE KEY UPDATE），可重复执行。
--
-- 幂等键约定（决定积分是否会被重复发放）：
--   一次性事件（课时/题目/知识点/资源）→ biz_id = 业务主键（每样只奖一次）
--   每日可重复事件（签到/对话练习/绘本创作）→ biz_id = yyyyMMdd 或 yyyyMMdd:{业务ID}
--   徽章 → biz_id = badgeId
--   ★ 编程比赛的积分排名是独立体系，**不产生全局积分**，故不在 biz_type 列表内。
-- =============================================================

-- 客户端字符集：徽章/图标里有 emoji（4 字节 UTF-8），Windows 下 mysql 客户端默认字符集
-- 不是 utf8mb4 会报 1366 Incorrect string value，这里显式声明（Navicat 等图形客户端同样受益）。
SET NAMES utf8mb4;

-- ---------- ① 积分账户 ----------
CREATE TABLE IF NOT EXISTS `student_point_account` (
  `user_id` varchar(32) NOT NULL COMMENT '学生ID',
  `stage` varchar(20) NOT NULL COMMENT '学段【冗余快照：本学段榜免join】',
  `total_points` int(11) NOT NULL DEFAULT 0 COMMENT '累计获得积分（只增不减，等级/成就依据）',
  `available_points` int(11) NOT NULL DEFAULT 0 COMMENT '可用积分（兑换扣减；二期先与 total 同值）',
  `level` int(11) NOT NULL DEFAULT 1 COMMENT '当前等级（由 total_points 按阶梯折算）',
  `streak_days` int(11) NOT NULL DEFAULT 0 COMMENT '连续学习天数',
  `last_streak_date` date NULL DEFAULT NULL COMMENT '连续天数最近归属日期（判断断签）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_stage_total`(`stage`, `total_points`) USING BTREE,
  INDEX `idx_total`(`total_points`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生积分账户' ROW_FORMAT = Dynamic;

-- ---------- ② 积分流水（幂等键 = user_id + biz_type + biz_id） ----------
CREATE TABLE IF NOT EXISTS `student_point_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(32) NOT NULL COMMENT '学生ID',
  `stage` varchar(20) NOT NULL COMMENT '学段【冗余快照】',
  `biz_type` varchar(32) NOT NULL COMMENT '来源：SIGN_IN/LESSON_QUIZ/PATH_TEST/CODING_PROBLEM/PICTURE_BOOK/ANIMATION/WIKI_CONFIRM/MASTERY/BADGE/STREAK/COMBO/EXCHANGE/ADMIN_ADJUST',
  `biz_id` varchar(64) NOT NULL COMMENT '业务幂等键（课时ID/题目ID/知识点ID/日期等）',
  `points` int(11) NOT NULL COMMENT '积分变动（正=获得 负=消耗）',
  `balance_after` int(11) NOT NULL COMMENT '变动后累计积分',
  `reason` varchar(200) NULL DEFAULT NULL COMMENT '展示文案，如「通关《冒泡排序》」',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `uk_user_biz`(`user_id`, `biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_user_time`(`user_id`, `create_time`) USING BTREE,
  INDEX `idx_biz_type_time`(`biz_type`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生积分流水' ROW_FORMAT = Dynamic;

-- ---------- ③ 徽章定义 ----------
CREATE TABLE IF NOT EXISTS `game_badge` (
  `badge_id` varchar(32) NOT NULL COMMENT '徽章ID',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `description` varchar(200) NULL DEFAULT NULL COMMENT '解锁条件文案（学生端展示）',
  `icon` varchar(200) NULL DEFAULT NULL COMMENT '图标（emoji 或资源ID）',
  `stage_scope` varchar(100) NULL DEFAULT NULL COMMENT '可见学段，多学段逗号分隔，NULL=全学段',
  `rule_type` varchar(32) NOT NULL COMMENT '规则：FIRST_PASS/STREAK/TOTAL_POINTS/MASTERY_COUNT/CODING_COUNT/COMBO_MAX/CREATION',
  `rule_value` int(11) NOT NULL DEFAULT 0 COMMENT '阈值',
  `reward_points` int(11) NOT NULL DEFAULT 0 COMMENT '解锁奖励积分',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`badge_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '徽章定义' ROW_FORMAT = Dynamic;

-- ---------- ④ 徽章解锁记录 ----------
CREATE TABLE IF NOT EXISTS `student_badge_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(32) NOT NULL COMMENT '学生ID',
  `badge_id` varchar(32) NOT NULL COMMENT '徽章ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `uk_user_badge`(`user_id`, `badge_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生徽章解锁记录' ROW_FORMAT = Dynamic;

-- ---------- ⑤ 规则配置（复用 system_config，config_group='GAME'）----------
INSERT INTO `system_config` (`config_group`, `config_key`, `config_value`, `config_type`, `description`, `status`)
VALUES
  ('GAME', 'POINT_SIGN_IN',        '5',            'INT',    '每日首次学习（签到）积分', 1),
  ('GAME', 'POINT_LESSON_QUIZ',    '20',           'INT',    '课时通关测验通过积分（另有得分率加成）', 1),
  ('GAME', 'POINT_PATH_TEST',      '30',           'INT',    '学习路径节点快测通过积分', 1),
  ('GAME', 'POINT_CODING_PROBLEM', '0',            'INT',    '编程题积分（0=用题目自带分数；看答案按 30%）', 1),
  ('GAME', 'POINT_MASTERY',        '50',           'INT',    '知识点「已掌握」奖励积分', 1),
  ('GAME', 'POINT_PICTURE_BOOK',   '15',           'INT',    '完成绘本创作积分（每日上限 3 次）', 1),
  ('GAME', 'POINT_ANIMATION',      '5',            'INT',    '动画讲解学完积分（每日上限 5 次）', 1),
  ('GAME', 'POINT_WIKI_CONFIRM',   '10',           'INT',    '知识页确认入库积分（每日上限 5 次）', 1),
  ('GAME', 'POINT_COMBO',          '2,5,10',       'STRING', '连击加成档位（连对 3/5/10 题各档奖励）', 1),
  ('GAME', 'POINT_DAILY_CAP',      '200',          'INT',    '每日积分上限（只约束可重复类规则）', 1),
  ('GAME', 'STREAK_BONUS',         '10,20,40,80',  'STRING', '连续学习 3/7/15/30 天阶梯奖励', 1),
  ('GAME', 'LEVEL_STEP',           '0,100,300,600,1000,1500,2100,2800,3600,4500', 'STRING', '1-10 级累计积分阶梯', 1)
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `config_type`  = VALUES(`config_type`),
  `description`  = VALUES(`description`),
  `status`       = VALUES(`status`);

-- ---------- 徽章预置（12 枚起步；比赛类荣誉不进全局徽章墙，见 A-5 分离说明）----------
INSERT INTO `game_badge` (`badge_id`, `name`, `description`, `icon`, `stage_scope`, `rule_type`, `rule_value`, `reward_points`, `sort`, `status`)
VALUES
  ('bdg_first_pass',   '首次通关',     '第一次通过任意测验或编程题',        '🎯', NULL,                        'FIRST_PASS',    1,  10,  1, 1),
  ('bdg_streak_3',     '坚持 3 天',    '连续学习 3 天',                    '🔥', NULL,                        'STREAK',        3,  10,  2, 1),
  ('bdg_streak_7',     '坚持 7 天',    '连续学习 7 天',                    '🔥', NULL,                        'STREAK',        7,  20,  3, 1),
  ('bdg_streak_15',    '坚持 15 天',   '连续学习 15 天',                   '🔥', NULL,                        'STREAK',       15,  40,  4, 1),
  ('bdg_streak_30',    '坚持 30 天',   '连续学习 30 天',                   '🏅', NULL,                        'STREAK',       30,  80,  5, 1),
  ('bdg_combo_10',     '十全十美',     '一次练习连续答对 10 题',            '💯', NULL,                        'COMBO_MAX',    10,  30,  6, 1),
  ('bdg_mastery_10',   '知识点收割者', '掌握 10 个知识点',                 '🌾', NULL,                        'MASTERY_COUNT',10,  30,  7, 1),
  ('bdg_points_1000',  '积分达人',     '累计获得 1000 积分',               '💎', NULL,                        'TOTAL_POINTS',1000,  50,  8, 1),
  ('bdg_coding_5',     '编程新星',     '通过 5 道编程题',                  '⭐', 'PRIMARY_HIGH,JUNIOR,SENIOR','CODING_COUNT',  5,  20,  9, 1),
  ('bdg_coding_20',    '编程达人',     '通过 20 道编程题',                 '🚀', 'PRIMARY_HIGH,JUNIOR,SENIOR','CODING_COUNT', 20,  50, 10, 1),
  ('bdg_creation_1',   '小小创作者',   '完成第一本绘本或第一个动画',        '🎨', 'PRIMARY_LOW,PRIMARY_HIGH',  'CREATION',      1,  15, 11, 1),
  ('bdg_creation_10',  '知识整理家',   '确认入库 10 个知识页',             '📚', NULL,                        'CREATION',     10,  30, 12, 1)
ON DUPLICATE KEY UPDATE
  `name`          = VALUES(`name`),
  `description`   = VALUES(`description`),
  `icon`          = VALUES(`icon`),
  `stage_scope`   = VALUES(`stage_scope`),
  `rule_type`     = VALUES(`rule_type`),
  `rule_value`    = VALUES(`rule_value`),
  `reward_points` = VALUES(`reward_points`),
  `sort`          = VALUES(`sort`);

-- 复核：4 张表 + GAME 配置 + 徽章
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('student_point_account','student_point_record','game_badge','student_badge_record');
SELECT COUNT(*) AS game_config_rows FROM system_config WHERE config_group = 'GAME';
SELECT COUNT(*) AS badge_rows FROM game_badge;
