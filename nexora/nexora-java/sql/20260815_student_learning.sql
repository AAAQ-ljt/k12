-- 学生端重构 DDL 设计（初中/高中闭环）：个人知识库 + 动画产物 + 学习行为记录
-- 先落设计与文档，未确认前不执行。

-- 资源信息：增加归属人（NULL=管理端公共资源）与产物扩展信息
ALTER TABLE resource_info
    ADD COLUMN owner_id VARCHAR(32) DEFAULT NULL COMMENT '归属用户ID；NULL=管理端公共资源' AFTER create_by,
    ADD COLUMN ext_json TEXT COMMENT '产物扩展信息（动画步骤/绘本分页/播放配置等）' AFTER hls_path,
    ADD KEY idx_owner_status (owner_id, status);

-- 资源目录：增加归属人，学生目录与学生资源同 owner 隔离
ALTER TABLE resource_directory
    ADD COLUMN owner_id VARCHAR(32) DEFAULT NULL COMMENT '归属用户ID；NULL=管理端公共目录' AFTER parent_id,
    ADD KEY idx_owner_parent (owner_id, parent_id, sort);

-- 知识文档：NULL=官方知识库；非空=学生个人知识库
ALTER TABLE knowledge_doc
    ADD COLUMN owner_id VARCHAR(32) DEFAULT NULL COMMENT 'NULL=官方知识库；非空=学生个人知识库' AFTER knowledge_point_id,
    ADD KEY idx_owner_status (owner_id, status);

-- 学生学习行为记录：学习路径与“最懂你的 AI 教师”的数据基础
CREATE TABLE IF NOT EXISTS student_learning_record (
    record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id VARCHAR(32) NOT NULL COMMENT '学生',
    resource_id VARCHAR(32) DEFAULT NULL COMMENT '资源ID，可空',
    course_id VARCHAR(32) DEFAULT NULL COMMENT '课程ID，可空',
    lesson_id VARCHAR(32) DEFAULT NULL COMMENT '课时ID，可空',
    action_type VARCHAR(20) NOT NULL COMMENT 'VIEW/COMPLETE/PRACTICE/ANIMATION/PARSE',
    duration INT NOT NULL DEFAULT 0 COMMENT '时长（秒）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (record_id),
    KEY idx_user_time (user_id, create_time),
    KEY idx_user_type (user_id, action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生学习行为记录';
