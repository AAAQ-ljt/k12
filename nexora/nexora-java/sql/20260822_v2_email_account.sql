-- ============================================================
-- 20260822 v2：账号体系以邮箱为唯一登录标识
-- 说明：
--   1) user_id 保留为内部主键（被 agent_message / resource_info / knowledge_doc
--      / student_learning_record / user_wiki_profile 等表作为逻辑外键引用，不可删除）；
--   2) email 为登录/注册唯一账号（uk_email 唯一键，已有）；登录接口仅按邮箱校验；
--   3) username 降级为昵称别名，不再要求全局唯一（学生端注册仅校验邮箱；
--      管理端登录为配置的虚拟账号，与 user_info.username 无关）。
-- 执行环境：nexora 库（MySQL 8）
-- ============================================================

ALTER TABLE `user_info` DROP INDEX `uk_username`;