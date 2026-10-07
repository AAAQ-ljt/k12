-- =====================================================================
-- 复习闭环（设计点 ④：静音 / 取消提醒）
--
-- knowledge_mastery 增加两列：
--   muted      tinyint  0 正常 / 1 长期不再提醒该知识点
--   mute_until datetime 临时静音到期时间（「今天不用提醒」= 推到明天）
-- 静音项不计入「待复习」计数（由 ReviewComponent 统一判定），但掌握度详情里仍可见——不隐藏事实。
--
-- 幂等：字段不存在才加。执行：mysql -uroot -p nexora < 20261017_mastery_review_mute.sql
-- 【禁止改已存在的表结构】除本次新增两列外不动其它列。
-- =====================================================================
SET NAMES utf8mb4;

SET @c1 := (SELECT COUNT(*) FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'knowledge_mastery' AND column_name = 'muted');
SET @d1 := IF(@c1 = 0,
  'ALTER TABLE `knowledge_mastery` ADD COLUMN `muted` tinyint(4) NOT NULL DEFAULT 0 COMMENT ''是否不再提醒复习：0正常 1长期静音'' AFTER `review_stage`',
  'SELECT ''muted 已存在，跳过''');
PREPARE s1 FROM @d1; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @c2 := (SELECT COUNT(*) FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'knowledge_mastery' AND column_name = 'mute_until');
SET @d2 := IF(@c2 = 0,
  'ALTER TABLE `knowledge_mastery` ADD COLUMN `mute_until` datetime NULL DEFAULT NULL COMMENT ''临时静音到期时间（到点后恢复提醒）'' AFTER `muted`',
  'SELECT ''mute_until 已存在，跳过''');
PREPARE s2 FROM @d2; EXECUTE s2; DEALLOCATE PREPARE s2;

SELECT COUNT(*) AS 两列就绪 FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'knowledge_mastery' AND column_name IN ('muted', 'mute_until');
