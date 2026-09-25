-- ============================================================
-- 用户注册审核：user_info 新增 audit_status / audit_time
-- 用途：注册后账号处于「待审核」，管理员在用户管理页审核通过后才允许登录使用；
--      审核状态与 status（启用/禁用）分离——审核管能不能进站，启用/禁用管是否停用账号
-- 执行方式：mysql -uroot -p nexora < 20260925_user_audit_status.sql
-- 特性：只新增列，不改写既有列定义；脚本可重复执行
-- ============================================================

SET @column_missing = (
  SELECT COUNT(*) = 0 FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_info' AND COLUMN_NAME = 'audit_status'
);

-- 1) 新增列：默认 1，建列时存量账号自动落为「已通过」，不会被新上线的审核门禁挡在外面
SET @ddl = IF(@column_missing,
  'ALTER TABLE `user_info`
     ADD COLUMN `audit_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT ''审核状态：0待审核 1已通过 2已驳回'' AFTER `status`,
     ADD COLUMN `audit_time` datetime NULL DEFAULT NULL COMMENT ''审核时间，可空'' AFTER `audit_status`',
  'SELECT ''user_info.audit_status 已存在，跳过新增'' AS result');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 建列后把默认值改为 0（待审核）：默认拒绝——应用 insert 未显式写审核状态时，新账号一律待审核。
--    MODIFY 只改默认值与注释，不动已有数据，可重复执行
ALTER TABLE `user_info`
  MODIFY COLUMN `audit_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1已通过 2已驳回';
