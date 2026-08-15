-- 试卷管理（独立迁移，不改写既有 DDL）
CREATE TABLE IF NOT EXISTS paper_info (
    paper_id VARCHAR(15) NOT NULL COMMENT '试卷ID',
    paper_name VARCHAR(100) NOT NULL COMMENT '试卷名称',
    paper_type TINYINT NOT NULL DEFAULT 0 COMMENT '0练习卷 1考试卷',
    stage VARCHAR(20) DEFAULT NULL COMMENT '学段冗余',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
    total_score INT NOT NULL DEFAULT 0 COMMENT '试卷总分',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    create_by INT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    PRIMARY KEY (paper_id),
    KEY idx_grade_status (grade, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

CREATE TABLE IF NOT EXISTS paper_group (
    group_id VARCHAR(15) NOT NULL COMMENT '大题ID',
    paper_id VARCHAR(15) NOT NULL COMMENT '试卷ID',
    group_name VARCHAR(100) NOT NULL COMMENT '大题名称',
    group_sort INT NOT NULL DEFAULT 0 COMMENT '大题排序',
    create_time DATETIME DEFAULT NULL,
    PRIMARY KEY (group_id),
    KEY idx_paper_id (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷大题表';

CREATE TABLE IF NOT EXISTS paper_question (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    paper_id VARCHAR(15) NOT NULL COMMENT '试卷ID',
    group_id VARCHAR(15) NOT NULL COMMENT '大题ID',
    question_id VARCHAR(15) NOT NULL COMMENT '题目ID',
    score INT NOT NULL DEFAULT 5 COMMENT '本题分值',
    sort INT NOT NULL DEFAULT 0 COMMENT '题号排序',
    create_time DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_paper_group (paper_id, group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目表';
