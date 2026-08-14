-- 教学业务：课程与题目按年级过滤（独立迁移，不改写既有 DDL）
ALTER TABLE course_info
    ADD COLUMN grade VARCHAR(20) DEFAULT NULL COMMENT '年级' AFTER stage,
    ADD KEY idx_grade_status (grade, status);

ALTER TABLE question_info
    ADD COLUMN grade VARCHAR(20) DEFAULT NULL COMMENT '年级' AFTER stage,
    ADD KEY idx_grade_diff (grade, difficulty);
