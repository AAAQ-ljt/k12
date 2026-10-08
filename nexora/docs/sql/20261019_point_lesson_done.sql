-- 学完课时积分配置（2026-10-08）：没有配通关测验的课时，学完即发分
-- 背景：此前只有「通过通关测验」才发分，无测验课时学完既无积分也无明细记录（线上实证）
INSERT INTO system_config (config_group, config_key, config_value, config_type, description, status, create_time, update_time)
SELECT 'GAME', 'POINT_LESSON_DONE', '10', 'INT', '学完课时积分（该课时没有配通关测验时发放）', 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT config_key FROM system_config) t WHERE t.config_key = 'POINT_LESSON_DONE');
