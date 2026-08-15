-- 考试管理（独立迁移，不改写既有 DDL）
CREATE TABLE IF NOT EXISTS exam_info (
    exam_id VARCHAR(15) NOT NULL COMMENT '考试ID',
    exam_name VARCHAR(100) NOT NULL COMMENT '考试名称',
    stage VARCHAR(20) DEFAULT NULL COMMENT '学段冗余',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
    paper_id VARCHAR(15) NOT NULL COMMENT '试卷ID',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    duration_minutes INT NOT NULL DEFAULT 60 COMMENT '考试时长（分钟）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0未发布 1进行中 2已结束',
    create_by INT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    PRIMARY KEY (exam_id),
    KEY idx_grade_status (grade, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';
