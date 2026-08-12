-- ============================================================
-- Nexora 数据库 DDL + K12 AI 通识初始数据
-- 赛题 JBGS-2026-02：多模态 K12 人工智能通识课教学助手对话智能体
-- 版本: 1.0.0
-- 设计稿：nexora/docs/sql.md（字段含义与冗余设计说明以设计稿为准）
-- 说明：种子数据使用可读 ID（如 COURSE_PL），运行期业务主键统一 UUID；
--      演示账号密码均为 123456（MD5 存储）。
-- ============================================================

-- ============================================================
-- 1. 建库
-- ============================================================
CREATE DATABASE IF NOT EXISTS nexora DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nexora;
SET NAMES utf8mb4;

-- ============================================================
-- 2. DDL（28张表，按功能域分组）
-- ============================================================

-- ------------------------------------------------------------
-- 2.1 用户与权限域（3表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS user_info (
  user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
  username VARCHAR(32) NOT NULL COMMENT '登录名',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱，登录核心字段，可空（管理员可不填）',
  password VARCHAR(64) NOT NULL COMMENT '密码（MD5存储）',
  nick_name VARCHAR(32) DEFAULT NULL COMMENT '昵称',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  role_type TINYINT NOT NULL DEFAULT 1 COMMENT '角色：0管理员 1学生',
  stage VARCHAR(20) DEFAULT NULL COMMENT '学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR；学生必填，管理员为空',
  grade VARCHAR(20) DEFAULT NULL COMMENT '年级（如三年级）',
  interests VARCHAR(500) DEFAULT NULL COMMENT '兴趣标签，JSON数组字符串',
  learning_style_tags VARCHAR(500) DEFAULT NULL COMMENT '学习风格标签JSON【冗余：规则引擎按行为周期计算后落地】',
  sex TINYINT DEFAULT 2 COMMENT '性别：0女 1男 2保密',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS system_menu (
  menu_id INT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  parent_id INT NOT NULL DEFAULT 0 COMMENT '父菜单ID，0为根',
  menu_name VARCHAR(50) NOT NULL COMMENT '菜单名',
  menu_code VARCHAR(50) NOT NULL COMMENT '权限编码，与管理端权限注解一一对应',
  menu_type TINYINT NOT NULL COMMENT '类型：0目录 1菜单 2按钮',
  path VARCHAR(100) DEFAULT NULL COMMENT '前端路由路径',
  icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (menu_id),
  UNIQUE KEY uk_menu_code (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

CREATE TABLE IF NOT EXISTS system_role_menu (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_type TINYINT NOT NULL COMMENT '角色：0管理员（学生端无菜单权限，不进此表）',
  menu_id INT NOT NULL COMMENT '菜单ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ------------------------------------------------------------
-- 2.2 消息通知域（3表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS message_info (
  message_id INT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content TEXT COMMENT '内容',
  message_type TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0系统消息 1学习提醒',
  jump_path VARCHAR(200) DEFAULT NULL COMMENT '消息点击跳转路径，可空',
  create_by INT DEFAULT NULL COMMENT '发送人（管理员）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息主表';

CREATE TABLE IF NOT EXISTS message_user (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  message_id INT NOT NULL COMMENT '消息ID',
  user_id VARCHAR(32) NOT NULL COMMENT '接收人',
  read_status TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
  delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1已删除（学生隐藏消息）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_read (user_id, read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息关联表';

CREATE TABLE IF NOT EXISTS system_notice (
  notice_id INT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content TEXT COMMENT '内容',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- ------------------------------------------------------------
-- 2.3 课程与资源域（5表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS course_info (
  course_id VARCHAR(32) NOT NULL COMMENT '课程ID',
  course_name VARCHAR(100) NOT NULL COMMENT '课程名',
  cover VARCHAR(255) DEFAULT NULL COMMENT '封面URL',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余：学生端按学段过滤主筛选键】',
  subject VARCHAR(20) NOT NULL DEFAULT 'AI' COMMENT '学科',
  difficulty TINYINT NOT NULL DEFAULT 1 COMMENT '难度：1-3星',
  description VARCHAR(500) DEFAULT NULL COMMENT '简介',
  intro TEXT COMMENT '详细介绍',
  lesson_count INT NOT NULL DEFAULT 0 COMMENT '课时总数【冗余：课时增删时同事务维护】',
  study_count INT NOT NULL DEFAULT 0 COMMENT '学习人数【冗余：学习行为触发计数】',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  create_by INT DEFAULT NULL COMMENT '创建人（管理员）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (course_id),
  KEY idx_stage_status (stage, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程（教材）表';

CREATE TABLE IF NOT EXISTS course_chapter (
  chapter_id VARCHAR(32) NOT NULL COMMENT '章节ID',
  course_id VARCHAR(32) NOT NULL COMMENT '所属课程',
  chapter_name VARCHAR(100) NOT NULL COMMENT '章节名',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (chapter_id),
  KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节表';

CREATE TABLE IF NOT EXISTS course_chapter_lesson (
  lesson_id VARCHAR(32) NOT NULL COMMENT '课时ID',
  chapter_id VARCHAR(32) NOT NULL COMMENT '所属章节',
  course_id VARCHAR(32) NOT NULL COMMENT '所属课程【冗余：免join章节直查课程课时树】',
  lesson_name VARCHAR(100) NOT NULL COMMENT '课时名',
  summary VARCHAR(500) DEFAULT NULL COMMENT '课时摘要',
  video_duration INT DEFAULT NULL COMMENT '视频时长（秒）【冗余：自主视频资源同步，续播UI免查资源表】',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (lesson_id),
  KEY idx_chapter_id (chapter_id),
  KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时表';

CREATE TABLE IF NOT EXISTS course_chapter_lesson_resource (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  lesson_id VARCHAR(32) NOT NULL COMMENT '课时ID',
  course_id VARCHAR(32) NOT NULL COMMENT '课程ID【冗余】',
  resource_id VARCHAR(32) NOT NULL COMMENT '资源ID',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_lesson_id (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时资源关联表';

CREATE TABLE IF NOT EXISTS resource_info (
  resource_id VARCHAR(32) NOT NULL COMMENT '资源ID',
  resource_name VARCHAR(200) NOT NULL COMMENT '资源名',
  resource_type VARCHAR(20) NOT NULL COMMENT '类型：VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK',
  tags VARCHAR(500) DEFAULT NULL COMMENT '资源标签，多个逗号分隔',
  description VARCHAR(500) DEFAULT NULL COMMENT '资源简介',
  file_path VARCHAR(255) DEFAULT NULL COMMENT '文件地址',
  file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
  cover VARCHAR(255) DEFAULT NULL COMMENT '封面',
  duration INT DEFAULT NULL COMMENT '音视频时长（秒）',
  hls_path VARCHAR(255) DEFAULT NULL COMMENT 'HLS转码产物地址',
  stage VARCHAR(20) DEFAULT NULL COMMENT '归属学段，可空',
  knowledge_point_id VARCHAR(32) DEFAULT NULL COMMENT '关联知识点【冗余：recommendResource工具按知识点直查】',
  source TINYINT NOT NULL DEFAULT 0 COMMENT '来源：0后台上传 1AI生成',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0处理中 1可用 2失败',
  create_by INT DEFAULT NULL COMMENT '上传人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (resource_id),
  KEY idx_type_status (resource_type, status),
  KEY idx_knowledge_point_id (knowledge_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源信息表';

-- ------------------------------------------------------------
-- 2.4 知识点域（2表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS knowledge_point (
  knowledge_point_id VARCHAR(32) NOT NULL COMMENT '知识点ID',
  name VARCHAR(100) NOT NULL COMMENT '知识点名；同名跨学段多行，(name, stage)逻辑唯一',
  stage VARCHAR(20) NOT NULL COMMENT '学段',
  subject VARCHAR(20) NOT NULL DEFAULT 'AI' COMMENT '学科',
  difficulty TINYINT NOT NULL DEFAULT 1 COMMENT '难度：1-3',
  description VARCHAR(500) DEFAULT NULL COMMENT '描述',
  cover VARCHAR(255) DEFAULT NULL COMMENT '封面，可空',
  lesson_id VARCHAR(32) DEFAULT NULL COMMENT '关联课时，可空',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (knowledge_point_id),
  KEY idx_stage_subject (stage, subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点表（领域中心）';

CREATE TABLE IF NOT EXISTS knowledge_doc (
  doc_id VARCHAR(32) NOT NULL COMMENT '文档ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余：与ES metadata双写一致】',
  knowledge_point_id VARCHAR(32) NOT NULL COMMENT '知识点【冗余：检索过滤/管理筛选免join】',
  difficulty TINYINT NOT NULL DEFAULT 1 COMMENT '难度：1-3',
  data_type VARCHAR(20) NOT NULL DEFAULT 'KNOWLEDGE' COMMENT '数据类型，默认KNOWLEDGE',
  content LONGTEXT COMMENT '正文（Markdown）',
  source_type TINYINT NOT NULL DEFAULT 0 COMMENT '来源：0手动维护 1资料解析',
  source_resource_id VARCHAR(32) DEFAULT NULL COMMENT '来源资源ID（解析入库时回填），可空',
  vector_status TINYINT NOT NULL DEFAULT 0 COMMENT '向量状态：0待处理 1处理中 2已完成 3失败 4过期',
  vector_error VARCHAR(500) DEFAULT NULL COMMENT '向量化失败时的错误原因，可空',
  chunk_count INT NOT NULL DEFAULT 0 COMMENT '入库分块数',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
  create_by INT DEFAULT NULL COMMENT '维护人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (doc_id),
  KEY idx_stage_kp (stage, knowledge_point_id),
  KEY idx_vector_status (vector_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- ------------------------------------------------------------
-- 2.5 题库与练习域（3表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS question_info (
  question_id VARCHAR(32) NOT NULL COMMENT '题目ID',
  knowledge_point_id VARCHAR(32) NOT NULL COMMENT '知识点【冗余：出题/练习主筛选键】',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余：按学段抽题免join】',
  difficulty TINYINT NOT NULL DEFAULT 1 COMMENT '难度：1-3',
  question_type TINYINT NOT NULL COMMENT '题型：0单选 1多选 2判断 3填空',
  title TEXT NOT NULL COMMENT '题干',
  question_image VARCHAR(500) DEFAULT NULL COMMENT '题目配图，关联resource_info.resource_id，多个逗号分隔，可空',
  answer VARCHAR(500) DEFAULT NULL COMMENT '判断/填空答案；选择题答案在选项表',
  analysis TEXT COMMENT '解析',
  source TINYINT NOT NULL DEFAULT 0 COMMENT '来源：0管理员录入 1AI生成',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核：0待审核 1已上架 2已驳回',
  score INT NOT NULL DEFAULT 5 COMMENT '默认分值',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  create_by INT DEFAULT NULL COMMENT '录入人，可空（AI生成为空）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (question_id),
  KEY idx_kp_audit (knowledge_point_id, audit_status),
  KEY idx_stage_diff (stage, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

CREATE TABLE IF NOT EXISTS question_option (
  option_id INT NOT NULL AUTO_INCREMENT COMMENT '选项ID',
  question_id VARCHAR(32) NOT NULL COMMENT '题目ID',
  option_label VARCHAR(8) NOT NULL COMMENT '选项标号：A/B/C/D',
  option_content VARCHAR(500) NOT NULL COMMENT '选项内容',
  is_answer TINYINT NOT NULL DEFAULT 0 COMMENT '0否 1是',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (option_id),
  KEY idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目选项表';

CREATE TABLE IF NOT EXISTS practice_record (
  record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  user_id VARCHAR(32) NOT NULL COMMENT '学生',
  knowledge_point_id VARCHAR(32) NOT NULL COMMENT '知识点【冗余快照：提交时从题目复制】',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余快照：按学段分析免join】',
  question_id VARCHAR(32) NOT NULL COMMENT '题目ID',
  question_type TINYINT NOT NULL COMMENT '题型【冗余快照】',
  user_answer VARCHAR(500) DEFAULT NULL COMMENT '学生作答',
  is_correct TINYINT NOT NULL DEFAULT 0 COMMENT '0错 1对',
  score INT NOT NULL DEFAULT 0 COMMENT '得分',
  duration INT NOT NULL DEFAULT 0 COMMENT '用时（秒）',
  source TINYINT NOT NULL DEFAULT 0 COMMENT '来源：0对话练习 1路径快测 2遗忘复习',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (record_id),
  KEY idx_user_kp (user_id, knowledge_point_id),
  KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏化练习记录表';

-- ------------------------------------------------------------
-- 2.6 学习进度域（3表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS course_study_progress (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id VARCHAR(32) NOT NULL COMMENT '学生',
  course_id VARCHAR(32) NOT NULL COMMENT '课程',
  studied_lessons INT NOT NULL DEFAULT 0 COMMENT '已学课时数【冗余：Redis缓冲聚合后异步回写】',
  total_lessons INT NOT NULL DEFAULT 0 COMMENT '课时总数【冗余快照】',
  progress INT NOT NULL DEFAULT 0 COMMENT '进度百分比【冗余：列表直读】',
  study_duration INT NOT NULL DEFAULT 0 COMMENT '累计学习时长（秒）',
  last_lesson_id VARCHAR(32) DEFAULT NULL COMMENT '最近学习课时',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程学习进度表';

CREATE TABLE IF NOT EXISTS course_study_lesson_progress (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id VARCHAR(32) NOT NULL COMMENT '学生',
  course_id VARCHAR(32) NOT NULL COMMENT '课程【冗余】',
  lesson_id VARCHAR(32) NOT NULL COMMENT '课时',
  play_position INT NOT NULL DEFAULT 0 COMMENT '视频最后播放位置（秒），续播锚点',
  study_duration INT NOT NULL DEFAULT 0 COMMENT '学习时长（秒）',
  finished TINYINT NOT NULL DEFAULT 0 COMMENT '0未完成 1已完成',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_lesson (user_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时学习进度表';

CREATE TABLE IF NOT EXISTS course_study_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id VARCHAR(32) NOT NULL COMMENT '学生',
  course_id VARCHAR(32) NOT NULL COMMENT '课程',
  lesson_id VARCHAR(32) NOT NULL COMMENT '课时',
  study_date DATE NOT NULL COMMENT '学习日期【冗余：连续打卡/时长统计按天聚合】',
  duration INT NOT NULL DEFAULT 0 COMMENT '本次时长（秒）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_date (user_id, study_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习日志表';

-- ------------------------------------------------------------
-- 2.7 AI对话域（2表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS agent_session (
  session_id VARCHAR(32) NOT NULL COMMENT '会话ID',
  user_id VARCHAR(32) NOT NULL COMMENT '学生',
  title VARCHAR(100) DEFAULT NULL COMMENT '会话标题（首条消息摘要）',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余快照：会话创建时学段】',
  knowledge_point_id VARCHAR(32) DEFAULT NULL COMMENT '当前学习知识点，可空',
  scene TINYINT NOT NULL DEFAULT 0 COMMENT '场景：0自由对话 1课程引导 2路径引导',
  message_count INT NOT NULL DEFAULT 0 COMMENT '消息数【冗余：会话列表展示】',
  last_message_time DATETIME DEFAULT NULL COMMENT '最后消息时间【冗余：列表排序免max聚合】',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常 1归档',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (session_id),
  KEY idx_user_time (user_id, last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';

CREATE TABLE IF NOT EXISTS agent_message (
  message_id VARCHAR(32) NOT NULL COMMENT '消息ID（HTTP发送接口返回值）',
  session_id VARCHAR(32) NOT NULL COMMENT '会话ID',
  user_id VARCHAR(32) NOT NULL COMMENT '学生【冗余：学习分析免join会话表】',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余快照】',
  knowledge_point_id VARCHAR(32) DEFAULT NULL COMMENT '知识点，可空【冗余】',
  user_message TEXT COMMENT '用户消息',
  assistant_message LONGTEXT COMMENT 'AI回复（流式完成后落库）',
  intent VARCHAR(20) DEFAULT NULL COMMENT '意图：EXPLAIN/RECOMMEND/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT',
  biz_type VARCHAR(20) DEFAULT NULL COMMENT '产物类型，可空：ANIMATION/PICTURE_BOOK/QUIZ/RESOURCE_LIST/CODE',
  biz_data LONGTEXT COMMENT '产物结构化JSON（卡片数据）',
  generation_id VARCHAR(32) DEFAULT NULL COMMENT '关联生成记录，可空',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0处理中 1完成 2取消 3错误',
  error_info VARCHAR(500) DEFAULT NULL COMMENT '错误信息，可空',
  prompt_tokens INT NOT NULL DEFAULT 0 COMMENT '输入token用量，默认0',
  completion_tokens INT NOT NULL DEFAULT 0 COMMENT '输出token用量，默认0',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (message_id),
  KEY idx_session_time (session_id, create_time),
  KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

-- ------------------------------------------------------------
-- 2.8 多模态生成域（2表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ai_generation_record (
  record_id VARCHAR(32) NOT NULL COMMENT '记录ID',
  user_id VARCHAR(32) DEFAULT NULL COMMENT '学生，可空（管理员预置无学生）',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余：预置绘本库按学段过滤】',
  knowledge_point_id VARCHAR(32) DEFAULT NULL COMMENT '知识点，可空',
  type VARCHAR(20) NOT NULL COMMENT '类型：ANIMATION/PICTURE_BOOK/DRAW/PPT/WORD/CODE',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content LONGTEXT COMMENT '结构化内容JSON（SVG分步脚本/绘本分页等）',
  file_url VARCHAR(255) DEFAULT NULL COMMENT '产物文件地址（Word/PPT/图片）',
  cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面',
  source TINYINT NOT NULL DEFAULT 0 COMMENT '来源：0学生生成 1管理员预置',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0生成中 1完成 2失败 3已发布',
  saved TINYINT NOT NULL DEFAULT 0 COMMENT '学生是否已保存到"我的"：0否 1是',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核：0待审核 1通过 2驳回（动画审核流程）',
  create_by INT DEFAULT NULL COMMENT '管理员预置时记录操作人ID，可空',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (record_id),
  KEY idx_user_type (user_id, type),
  KEY idx_source_status (source, status, stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成记录表';

CREATE TABLE IF NOT EXISTS animation_template (
  template_id INT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  template_type VARCHAR(30) NOT NULL COMMENT '动画类型，对应意图：EXPLAIN/CONCEPT/PROCESS等',
  stage VARCHAR(20) NOT NULL COMMENT '学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR/ALL',
  description VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
  template_content LONGTEXT COMMENT 'SVG模板JSON，含分步脚本结构',
  preview_url VARCHAR(500) DEFAULT NULL COMMENT '预览图URL，可空',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  create_by INT DEFAULT NULL COMMENT '创建人（管理员ID）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (template_id),
  KEY idx_stage_type_status (stage, template_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动画模板库';

-- ------------------------------------------------------------
-- 2.9 个性化学习路径域（3表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS learning_path (
  path_id VARCHAR(32) NOT NULL COMMENT '路径ID',
  user_id VARCHAR(32) NOT NULL COMMENT '学生',
  title VARCHAR(100) NOT NULL COMMENT '学习分类/目标名（学生自建或AI命名）',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余快照】',
  source TINYINT NOT NULL DEFAULT 0 COMMENT '来源：0规则生成 1AI生成',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0进行中 1已完成 2已放弃',
  total_items INT NOT NULL DEFAULT 0 COMMENT '节点总数【冗余：节点增删时维护】',
  finished_items INT NOT NULL DEFAULT 0 COMMENT '已完成节点数【冗余】',
  progress INT NOT NULL DEFAULT 0 COMMENT '进度百分比【冗余：列表直读免聚合】',
  current_item_id VARCHAR(32) DEFAULT NULL COMMENT '当前节点，AI主动引导锚点',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (path_id),
  KEY idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习路径表';

CREATE TABLE IF NOT EXISTS learning_path_item (
  item_id VARCHAR(32) NOT NULL COMMENT '节点ID',
  path_id VARCHAR(32) NOT NULL COMMENT '所属路径',
  user_id VARCHAR(32) NOT NULL COMMENT '学生【冗余：到期复习直查免join路径表】',
  knowledge_point_id VARCHAR(32) NOT NULL COMMENT '知识点',
  knowledge_point_name VARCHAR(100) NOT NULL COMMENT '知识点名【冗余快照】',
  branch_type TINYINT NOT NULL DEFAULT 0 COMMENT '0主线 1兴趣分支',
  branch_name VARCHAR(50) DEFAULT NULL COMMENT '分支名，可空',
  item_type TINYINT NOT NULL DEFAULT 0 COMMENT '0学习 1复习（遗忘曲线复习节点）',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0未解锁 1进行中 2已掌握',
  due_date DATE DEFAULT NULL COMMENT '复习到期日（item_type=1时有效）',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (item_id),
  KEY idx_path_sort (path_id, sort),
  KEY idx_user_due (user_id, due_date, item_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路径节点表';

CREATE TABLE IF NOT EXISTS knowledge_mastery (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id VARCHAR(32) NOT NULL COMMENT '学生',
  knowledge_point_id VARCHAR(32) NOT NULL COMMENT '知识点',
  stage VARCHAR(20) NOT NULL COMMENT '学段【冗余：雷达图按学段聚合免join】',
  mastery_score INT NOT NULL DEFAULT 0 COMMENT '掌握度0-100',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0未解锁 1进行中 2已掌握',
  practice_count INT NOT NULL DEFAULT 0 COMMENT '练习次数【冗余计数：批改后原子+1】',
  correct_count INT NOT NULL DEFAULT 0 COMMENT '答对次数【冗余：正确率=correct_count/practice_count】',
  last_practice_time DATETIME DEFAULT NULL COMMENT '最近练习时间',
  last_master_time DATETIME DEFAULT NULL COMMENT '掌握时间（遗忘曲线计时起点）',
  next_review_time DATETIME DEFAULT NULL COMMENT '下次复习时间',
  review_stage TINYINT NOT NULL DEFAULT 0 COMMENT '遗忘曲线阶段0-4（对应1/3/7/15天间隔）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_kp (user_id, knowledge_point_id),
  KEY idx_user_review (user_id, next_review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点掌握度表';

-- ------------------------------------------------------------
-- 2.10 智能体配置域（2表）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS prompt_template (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  stage VARCHAR(20) NOT NULL COMMENT '学段；ALL表示通用模板',
  scene VARCHAR(20) NOT NULL COMMENT '场景/意图：EXPLAIN/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT等',
  template_name VARCHAR(50) NOT NULL COMMENT '模板名',
  content TEXT NOT NULL COMMENT '提示词内容（必须含"知识库无相关内容时如实说明，不要编造"类约束）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_stage_scene (stage, scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表';

CREATE TABLE IF NOT EXISTS system_config (
  config_id INT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  config_group VARCHAR(50) NOT NULL COMMENT '分组：AI_MODEL/RAG/PYODIDE/SYSTEM/SECURITY',
  config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  config_value TEXT COMMENT '配置值，支持字符串/JSON',
  config_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '值类型：STRING/INT/FLOAT/BOOLEAN/JSON',
  description VARCHAR(200) DEFAULT NULL COMMENT '配置说明',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_group_key (config_group, config_key),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统全局配置表';

-- ============================================================
-- 3. 初始数据
-- ============================================================

-- ------------------------------------------------------------
-- 3.A 演示账号（user_info，5条）
-- 密码均为 123456 → MD5 = e10adc3949ba59abbe56e057f20f883e
-- ------------------------------------------------------------
INSERT INTO user_info (user_id, username, email, password, nick_name, avatar, role_type, stage, grade, interests, learning_style_tags, sex, status, create_time, update_time) VALUES
('admin_001', 'admin', 'admin@nexora.com', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', NULL, 0, NULL, NULL, NULL, NULL, 2, 1, NOW(), NOW()),
('1000000001', 'student_low', 'student_low@nexora.com', 'e10adc3949ba59abbe56e057f20f883e', '小明（小学低）', NULL, 1, 'PRIMARY_LOW', '三年级', '["AI绘画","动画"]', NULL, 1, 1, NOW(), NOW()),
('1000000002', 'student_high', 'student_high@nexora.com', 'e10adc3949ba59abbe56e057f20f883e', '小华（小学高）', NULL, 1, 'PRIMARY_HIGH', '五年级', '["编程","机器人"]', NULL, 1, 1, NOW(), NOW()),
('1000000003', 'student_junior', 'student_junior@nexora.com', 'e10adc3949ba59abbe56e057f20f883e', '小李（初中）', NULL, 1, 'JUNIOR', '七年级', '["算法","数据分析"]', NULL, 0, 1, NOW(), NOW()),
('1000000004', 'student_senior', 'student_senior@nexora.com', 'e10adc3949ba59abbe56e057f20f883e', '小张（高中）', NULL, 1, 'SENIOR', '高一', '["深度学习","Python"]', NULL, 1, 1, NOW(), NOW());

-- ------------------------------------------------------------
-- 3.B 管理端菜单（system_menu 26条 + system_role_menu 26条）
-- ------------------------------------------------------------
INSERT INTO system_menu (menu_id, parent_id, menu_name, menu_code, menu_type, path, icon, sort, status) VALUES
(1,  0, '工作台',             'dashboard',                         1, '/dashboard',                 'DashboardOutlined',    10, 1),
(2,  0, '用户管理',           'user_manage',                       0, NULL,                         'UserOutlined',         20, 1),
(3,  2, '用户列表',           'user_manage:list',                  1, '/user/list',                 NULL,                   21, 1),
(4,  0, '知识库管理',         'knowledge_manage',                  0, NULL,                         'BookOutlined',         30, 1),
(5,  4, '文档列表',           'knowledge_manage:doc',              1, '/knowledge/doc',             NULL,                   31, 1),
(6,  4, '上传资源',           'knowledge_manage:resource',         1, '/knowledge/resource',        NULL,                   32, 1),
(7,  4, '向量化状态',         'knowledge_manage:vector',           1, '/knowledge/vector',          NULL,                   33, 1),
(8,  0, '知识图谱与学习路径', 'knowledge_graph',                   0, NULL,                         'ApartmentOutlined',    40, 1),
(9,  8, '知识点树',           'knowledge_graph:point',             1, '/knowledge-graph/point',     NULL,                   41, 1),
(10, 8, '前置依赖',           'knowledge_graph:dependency',        1, '/knowledge-graph/dependency', NULL,                  42, 1),
(11, 8, '资源关联',           'knowledge_graph:relation',          1, '/knowledge-graph/relation',  NULL,                   43, 1),
(12, 0, '动画管理',           'animation_manage',                  0, NULL,                         'PlayCircleOutlined',   50, 1),
(13, 12,'模板库',             'animation_manage:template',         1, '/animation/template',        NULL,                   51, 1),
(14, 12,'学生生成记录',       'animation_manage:record',           1, '/animation/record',          NULL,                   52, 1),
(15, 12,'动画审核',           'animation_manage:audit',            1, '/animation/audit',           NULL,                   53, 1),
(16, 0, '智能体配置',         'agent_config',                      0, NULL,                         'RobotOutlined',        60, 1),
(17, 16,'System Prompt',     'agent_config:prompt',               1, '/agent-config/prompt',       NULL,                   61, 1),
(18, 16,'大模型选择',         'agent_config:model',                1, '/agent-config/model',        NULL,                   62, 1),
(19, 16,'RAG参数',           'agent_config:rag',                  1, '/agent-config/rag',          NULL,                   63, 1),
(20, 16,'意图路由',           'agent_config:route',                1, '/agent-config/route',        NULL,                   64, 1),
(21, 16,'安全策略',           'agent_config:security',             1, '/agent-config/security',     NULL,                   65, 1),
(22, 0, '系统设置',           'system_settings',                   0, NULL,                         'SettingOutlined',      70, 1),
(23, 22,'学段配置',           'system_settings:stage',             1, '/system-settings/stage',     NULL,                   71, 1),
(24, 22,'文生图模型',         'system_settings:image_model',       1, '/system-settings/image-model', NULL,                 72, 1),
(25, 22,'Embedding模型',     'system_settings:embedding_model',   1, '/system-settings/embedding-model', NULL,             73, 1),
(26, 22,'Pyodide配置',       'system_settings:pyodide',           1, '/system-settings/pyodide',   NULL,                   74, 1);

INSERT INTO system_role_menu (role_type, menu_id, create_time) VALUES
(0, 1, NOW()), (0, 2, NOW()), (0, 3, NOW()), (0, 4, NOW()), (0, 5, NOW()), (0, 6, NOW()), (0, 7, NOW()), (0, 8, NOW()),
(0, 9, NOW()), (0, 10, NOW()), (0, 11, NOW()), (0, 12, NOW()), (0, 13, NOW()), (0, 14, NOW()), (0, 15, NOW()), (0, 16, NOW()),
(0, 17, NOW()), (0, 18, NOW()), (0, 19, NOW()), (0, 20, NOW()), (0, 21, NOW()), (0, 22, NOW()), (0, 23, NOW()), (0, 24, NOW()),
(0, 25, NOW()), (0, 26, NOW());

-- ------------------------------------------------------------
-- 3.C 4学段示例AI通识课程（course_info + course_chapter + course_chapter_lesson）
-- ------------------------------------------------------------
INSERT INTO course_info (course_id, course_name, cover, stage, subject, difficulty, description, intro, lesson_count, study_count, sort, status, create_by) VALUES
('COURSE_PL', 'AI启蒙之旅', NULL, 'PRIMARY_LOW',  'AI', 1, '面向小学低年级的AI启蒙课程，用故事和游戏认识AI', '通过生动的故事和互动游戏，带领低年级学生走进AI的世界，认识身边的人工智能。', 6,  0, 1, 1, 1),
('COURSE_PH', 'AI探索世界', NULL, 'PRIMARY_HIGH', 'AI', 1, '面向小学高年级的AI探索课程，动手体验AI技术', '从认识AI到动手实践，探索机器学习、编程入门，培养计算思维。', 9,  0, 2, 1, 1),
('COURSE_JR', 'AI通识基础', NULL, 'JUNIOR',       'AI', 2, '面向初中的AI通识课程，系统学习AI核心概念', '系统学习人工智能发展史、机器学习、神经网络、计算机视觉等核心技术，兼顾伦理思考。', 12, 0, 3, 1, 1),
('COURSE_SR', 'AI进阶实践', NULL, 'SENIOR',       'AI', 3, '面向高中的AI进阶课程，深入原理与工程实践', '深入机器学习原理与前沿技术，结合Python工程实践，培养AI素养与工程能力。', 12, 0, 4, 1, 1);

INSERT INTO course_chapter (chapter_id, course_id, chapter_name, sort, status) VALUES
('CHAP_PL_01', 'COURSE_PL', '身边的AI',     1, 0),
('CHAP_PL_02', 'COURSE_PL', 'AI小实验',     2, 0),
('CHAP_PH_01', 'COURSE_PH', 'AI的基础',     1, 0),
('CHAP_PH_02', 'COURSE_PH', '机器学习入门', 2, 0),
('CHAP_PH_03', 'COURSE_PH', 'AI与编程',     3, 0),
('CHAP_JR_01', 'COURSE_JR', '人工智能概述', 1, 0),
('CHAP_JR_02', 'COURSE_JR', 'AI核心技术',  2, 0),
('CHAP_JR_03', 'COURSE_JR', 'AI应用与伦理', 3, 0),
('CHAP_SR_01', 'COURSE_SR', '深入机器学习', 1, 0),
('CHAP_SR_02', 'COURSE_SR', 'AI前沿技术',  2, 0),
('CHAP_SR_03', 'COURSE_SR', 'AI工程实践',  3, 0);

INSERT INTO course_chapter_lesson (lesson_id, chapter_id, course_id, lesson_name, summary, video_duration, sort, status) VALUES
('LESSON_PL_0101', 'CHAP_PL_01', 'COURSE_PL', '什么是人工智能',     '了解AI的基本概念',         600, 1, 0),
('LESSON_PL_0102', 'CHAP_PL_01', 'COURSE_PL', 'AI在哪里',           '发现生活中的AI',           500, 2, 0),
('LESSON_PL_0103', 'CHAP_PL_01', 'COURSE_PL', '和AI聊天',           '体验与AI对话',             550, 3, 0),
('LESSON_PL_0201', 'CHAP_PL_02', 'COURSE_PL', 'AI画画的秘密',       '了解AI如何生成图片',       500, 1, 0),
('LESSON_PL_0202', 'CHAP_PL_02', 'COURSE_PL', 'AI识别图片',         '体验图像识别技术',         500, 2, 0),
('LESSON_PL_0203', 'CHAP_PL_02', 'COURSE_PL', '动手试一试',         '综合实践',                 600, 3, 0),
('LESSON_PH_0101', 'CHAP_PH_01', 'COURSE_PH', '认识人工智能',       '深入理解AI概念',           700, 1, 0),
('LESSON_PH_0102', 'CHAP_PH_01', 'COURSE_PH', '数据是什么',         '认识数据的作用',           650, 2, 0),
('LESSON_PH_0103', 'CHAP_PH_01', 'COURSE_PH', '算法初探',           '了解算法的基本概念',       700, 3, 0),
('LESSON_PH_0201', 'CHAP_PH_02', 'COURSE_PH', '什么是机器学习',     '机器学习入门',             750, 1, 0),
('LESSON_PH_0202', 'CHAP_PH_02', 'COURSE_PH', '监督与无监督',       '学习方式分类',             700, 2, 0),
('LESSON_PH_0203', 'CHAP_PH_02', 'COURSE_PH', '训练与预测',         '模型训练过程',             750, 3, 0),
('LESSON_PH_0301', 'CHAP_PH_03', 'COURSE_PH', 'Python初体验',       '编写第一行代码',           800, 1, 0),
('LESSON_PH_0302', 'CHAP_PH_03', 'COURSE_PH', '排序算法',           '学习冒泡排序',             750, 2, 0),
('LESSON_PH_0303', 'CHAP_PH_03', 'COURSE_PH', 'AI小项目',           '综合实践项目',             800, 3, 0),
('LESSON_JR_0101', 'CHAP_JR_01', 'COURSE_JR', 'AI发展简史',         '人工智能的历史脉络',       900, 1, 0),
('LESSON_JR_0102', 'CHAP_JR_01', 'COURSE_JR', '机器学习原理',       '理解学习机制',             900, 2, 0),
('LESSON_JR_0103', 'CHAP_JR_01', 'COURSE_JR', '神经网络基础',       '神经元与网络结构',         850, 3, 0),
('LESSON_JR_0104', 'CHAP_JR_01', 'COURSE_JR', '深度学习入门',       '从神经网络到深度学习',     900, 4, 0),
('LESSON_JR_0201', 'CHAP_JR_02', 'COURSE_JR', '计算机视觉',         '让机器看懂世界',           850, 1, 0),
('LESSON_JR_0202', 'CHAP_JR_02', 'COURSE_JR', '自然语言处理',       '让机器理解语言',           850, 2, 0),
('LESSON_JR_0203', 'CHAP_JR_02', 'COURSE_JR', '排序与搜索算法',     '经典算法实践',             900, 3, 0),
('LESSON_JR_0204', 'CHAP_JR_02', 'COURSE_JR', 'Python编程实践',     '代码编写与调试',           900, 4, 0),
('LESSON_JR_0301', 'CHAP_JR_03', 'COURSE_JR', 'AI伦理思考',         '技术背后的伦理',           800, 1, 0),
('LESSON_JR_0302', 'CHAP_JR_03', 'COURSE_JR', '数据隐私',           '保护个人数据',             800, 2, 0),
('LESSON_JR_0303', 'CHAP_JR_03', 'COURSE_JR', 'AI与未来',           '展望AI发展',               800, 3, 0),
('LESSON_JR_0304', 'CHAP_JR_03', 'COURSE_JR', '综合实践',           'AI应用实践项目',           900, 4, 0),
('LESSON_SR_0101', 'CHAP_SR_01', 'COURSE_SR', '机器学习进阶',       '深入学习算法',             1000, 1, 0),
('LESSON_SR_0102', 'CHAP_SR_01', 'COURSE_SR', '神经网络与深度学习', '网络结构与反向传播',       1000, 2, 0),
('LESSON_SR_0103', 'CHAP_SR_01', 'COURSE_SR', '模型训练与评估',     '训练流程与评估指标',       950, 3, 0),
('LESSON_SR_0104', 'CHAP_SR_01', 'COURSE_SR', '过拟合与正则化',     '优化模型泛化能力',         950, 4, 0),
('LESSON_SR_0201', 'CHAP_SR_02', 'COURSE_SR', '计算机视觉应用',     '图像分类与目标检测',       1000, 1, 0),
('LESSON_SR_0202', 'CHAP_SR_02', 'COURSE_SR', '自然语言处理进阶',   '文本分析与生成',           1000, 2, 0),
('LESSON_SR_0203', 'CHAP_SR_02', 'COURSE_SR', '大语言模型',         'LLM原理与应用',           950, 3, 0),
('LESSON_SR_0204', 'CHAP_SR_02', 'COURSE_SR', '生成式AI',           'AIGC技术实践',            950, 4, 0),
('LESSON_SR_0301', 'CHAP_SR_03', 'COURSE_SR', 'Python高级编程',     '面向对象与数据结构',       1000, 1, 0),
('LESSON_SR_0302', 'CHAP_SR_03', 'COURSE_SR', '排序算法分析',       '时间复杂度与算法对比',     950, 2, 0),
('LESSON_SR_0303', 'CHAP_SR_03', 'COURSE_SR', 'AI伦理与治理',       '负责任的AI',              900, 3, 0),
('LESSON_SR_0304', 'CHAP_SR_03', 'COURSE_SR', '项目实战',           'AI项目全流程实践',         1000, 4, 0);

-- ------------------------------------------------------------
-- 3.D 知识点库（knowledge_point，23条）
-- 8主题 × 适配学段，同名跨学段多行，难度递增
-- ------------------------------------------------------------
INSERT INTO knowledge_point (knowledge_point_id, name, stage, subject, difficulty, description, sort, status) VALUES
('KP_PL_001', '什么是人工智能', 'PRIMARY_LOW',  'AI', 1, '用简单语言介绍AI是什么，生活中的AI例子', 1, 1),
('KP_PH_001', '什么是人工智能', 'PRIMARY_HIGH', 'AI', 1, '深入理解AI的定义、能力和局限', 1, 1),
('KP_JR_001', '什么是人工智能', 'JUNIOR',       'AI', 2, '系统理解AI概念、发展历程和分类', 1, 1),
('KP_SR_001', '人工智能概述',   'SENIOR',       'AI', 3, '从学科视角理解AI的研究范畴与范式', 1, 1),
('KP_PH_002', '机器学习入门',   'PRIMARY_HIGH', 'AI', 1, '用通俗的方式理解机器学习的基本思想', 2, 1),
('KP_JR_002', '机器学习原理',   'JUNIOR',       'AI', 2, '理解监督学习、无监督学习、强化学习', 2, 1),
('KP_SR_002', '机器学习进阶',   'SENIOR',       'AI', 3, '深入学习算法原理、模型评估与优化', 2, 1),
('KP_JR_003', '神经网络基础',   'JUNIOR',       'AI', 2, '了解神经元模型与神经网络结构', 3, 1),
('KP_SR_003', '神经网络与深度学习', 'SENIOR',   'AI', 3, '深入网络结构、反向传播与深度学习', 3, 1),
('KP_JR_004', '计算机视觉',     'JUNIOR',       'AI', 2, '了解图像识别、目标检测等视觉任务', 4, 1),
('KP_SR_004', '计算机视觉应用', 'SENIOR',       'AI', 3, '深入CNN、Transformer在视觉中的应用', 4, 1),
('KP_JR_005', '自然语言处理',   'JUNIOR',       'AI', 2, '了解文本分析、情感分析等NLP任务', 5, 1),
('KP_SR_005', '自然语言处理进阶', 'SENIOR',     'AI', 3, '深入注意力机制、预训练模型与LLM', 5, 1),
('KP_PH_003', '排序算法入门',   'PRIMARY_HIGH', 'AI', 1, '用可视化方式理解冒泡排序', 6, 1),
('KP_JR_006', '排序与搜索算法', 'JUNIOR',       'AI', 2, '掌握冒泡、选择、插入排序及二分搜索', 6, 1),
('KP_SR_006', '排序算法分析',   'SENIOR',       'AI', 3, '分析时间复杂度，学习快排、归并排序', 6, 1),
('KP_PH_004', 'Python编程基础', 'PRIMARY_HIGH', 'AI', 1, '学习变量、数据类型、输入输出', 7, 1),
('KP_JR_007', 'Python编程实践', 'JUNIOR',       'AI', 2, '掌握控制流、函数、列表与字典', 7, 1),
('KP_SR_007', 'Python高级编程', 'SENIOR',       'AI', 3, '面向对象、模块化、数据处理库', 7, 1),
('KP_PL_002', 'AI与生活',       'PRIMARY_LOW',  'AI', 1, '认识AI在生活中的正面与负面影响', 8, 1),
('KP_PH_005', 'AI伦理入门',     'PRIMARY_HIGH', 'AI', 1, '了解AI使用中的隐私与安全问题', 8, 1),
('KP_JR_008', 'AI伦理思考',     'JUNIOR',       'AI', 2, '思考算法偏见、责任归属等伦理问题', 8, 1),
('KP_SR_008', 'AI伦理与治理',   'SENIOR',       'AI', 3, '深入AI治理框架、法规与负责任AI', 8, 1);

-- ------------------------------------------------------------
-- 3.E 提示词模板（prompt_template，12条）
-- 每条 content 均包含"知识库无相关内容时如实说明，不要编造"约束
-- ------------------------------------------------------------
INSERT INTO prompt_template (stage, scene, template_name, content, status, remark) VALUES
('ALL', 'EXPLAIN', '知识点解释通用模板', '你是Nexora AI教学助手，负责为学生解释知识点。\n当前学生学段：{stage}\n知识点：{knowledge_point}\n\n要求：\n1. 结合知识库检索内容进行讲解，语言适配学生学段\n2. 多用类比和生活中的例子帮助理解\n3. 知识库无相关内容时如实说明，不要编造\n4. 回答控制在300字以内', 1, '解释知识点默认提示词'),
('ALL', 'QUIZ', '出题通用模板', '你是Nexora AI教学助手，负责根据知识点为学生出练习题。\n当前学生学段：{stage}\n知识点：{knowledge_point}\n难度：{difficulty}\n\n要求：\n1. 出3道选择题，每题4个选项\n2. 题目语言适配学段，避免超纲概念\n3. 知识库无相关内容时如实说明，不要编造题目\n4. 返回JSON格式：{questions:[{title,options:[{label,content,isAnswer}],analysis}]}', 1, '出题默认提示词'),
('PRIMARY_LOW', 'PICTURE_BOOK', '绘本生成-小学低', '你是Nexora AI教学助手，负责为小学低年级学生生成绘本故事。\n主题：{topic}\n\n要求：\n1. 语言简单易懂，适合6-9岁儿童\n2. 生成5页绘本，每页含一段文字描述和配图提示词\n3. 故事要有教育意义，传递AI知识\n4. 知识库无相关内容时如实说明，不要编造\n5. 返回JSON格式：{pages:[{text,image_prompt}]}', 1, '小学低年级绘本提示词'),
('PRIMARY_HIGH', 'PICTURE_BOOK', '绘本生成-小学高', '你是Nexora AI教学助手，负责为小学高年级学生生成绘本故事。\n主题：{topic}\n\n要求：\n1. 语言生动有趣，适合10-12岁儿童\n2. 生成5页绘本，每页含一段文字描述和配图提示词\n3. 故事要有教育意义，融入AI知识点\n4. 知识库无相关内容时如实说明，不要编造\n5. 返回JSON格式：{pages:[{text,image_prompt}]}', 1, '小学高年级绘本提示词'),
('ALL', 'DRAW', 'AI绘画通用模板', '你是Nexora AI教学助手，负责根据学生描述生成AI绘画提示词。\n学生学段：{stage}\n描述：{description}\n\n要求：\n1. 将学生描述转化为适合文生图模型的英文提示词\n2. 提示词要包含风格、色彩、构图等要素\n3. 内容必须健康积极，适合K12学生\n4. 知识库无相关内容时如实说明，不要编造\n5. 返回JSON格式：{prompt,negative_prompt}', 1, 'AI绘画提示词'),
('ALL', 'ANIMATION', '动画讲解通用模板', '你是Nexora AI教学助手，负责生成SVG动画讲解知识点。\n学生学段：{stage}\n知识点：{knowledge_point}\n\n要求：\n1. 生成分步SVG动画脚本，每步展示一个概念要点\n2. 动画元素要简洁清晰，适合对应学段\n3. 知识库无相关内容时如实说明，不要编造\n4. 返回JSON格式：{steps:[{name,svg_code,description}]}', 1, '动画讲解提示词'),
('PRIMARY_HIGH', 'CODING', '编程指导-小学高', '你是Nexora AI教学助手，负责指导小学高年级学生学习Python编程。\n知识点：{knowledge_point}\n\n要求：\n1. 用简单易懂的方式讲解代码\n2. 提供可运行的示例代码\n3. 代码要在Pyodide环境中可运行\n4. 知识库无相关内容时如实说明，不要编造\n5. 鼓励学生动手实践', 1, '小学高年级编程提示词'),
('JUNIOR', 'CODING', '编程指导-初中', '你是Nexora AI教学助手，负责指导初中生学习Python编程。\n知识点：{knowledge_point}\n\n要求：\n1. 讲解清晰，代码示例规范\n2. 提供可运行的示例代码\n3. 代码要在Pyodide环境中可运行\n4. 知识库无相关内容时如实说明，不要编造\n5. 引导学生理解编程思想', 1, '初中编程提示词'),
('SENIOR', 'CODING', '编程指导-高中', '你是Nexora AI教学助手，负责指导高中生学习Python编程。\n知识点：{knowledge_point}\n\n要求：\n1. 深入讲解编程原理和最佳实践\n2. 提供可运行的示例代码\n3. 代码要在Pyodide环境中可运行\n4. 知识库无相关内容时如实说明，不要编造\n5. 培养工程思维和算法能力', 1, '高中编程提示词'),
('ALL', 'PLAN', '学习路径规划通用模板', '你是Nexora AI教学助手，负责为学生规划个性化学习路径。\n学生学段：{stage}\n学习目标：{goal}\n当前掌握情况：{mastery}\n\n要求：\n1. 根据知识图谱和学生掌握度生成学习路径\n2. 路径包含主线节点和兴趣分支\n3. 按难度递进排列知识点\n4. 知识库无相关内容时如实说明，不要编造\n5. 返回JSON格式：{items:[{knowledge_point_id,name,branch_type,item_type,sort}]}', 1, '学习路径规划提示词'),
('ALL', 'PROGRESS', '进度分析通用模板', '你是Nexora AI教学助手，负责分析学生学习进度。\n学生学段：{stage}\n学习数据：{progress_data}\n\n要求：\n1. 分析学习时长、练习正确率、知识点掌握度\n2. 给出针对性的学习建议\n3. 知识库无相关内容时如实说明，不要编造\n4. 生成学习雷达图数据', 1, '进度分析提示词'),
('ALL', 'CHAT', '通用对话模板', '你是Nexora AI教学助手，负责与学生进行通用对话。\n学生学段：{stage}\n\n要求：\n1. 友善、耐心，语言适配学段\n2. 引导学生探索AI知识\n3. 可以进行闲聊、答疑、鼓励\n4. 知识库无相关内容时如实说明，不要编造\n5. 注意保护学生隐私，不收集敏感信息', 1, '通用对话提示词');

-- ------------------------------------------------------------
-- 3.F 系统配置（system_config，14条）
-- ------------------------------------------------------------
INSERT INTO system_config (config_group, config_key, config_value, config_type, description, status) VALUES
('AI_MODEL',  'chat_model',              'qwen3-32b',             'STRING',  '对话大模型', 1),
('AI_MODEL',  'text_image_model',        'wanx-v1',               'STRING',  '文生图模型', 1),
('AI_MODEL',  'embedding_model',         'text-embedding-v4',     'STRING',  '向量嵌入模型', 1),
('RAG',       'top_k',                   '5',                     'INT',     '检索返回Top-K', 1),
('RAG',       'similarity_threshold',    '0.7',                   'FLOAT',   '相似度阈值', 1),
('RAG',       'chunk_size',              '500',                   'INT',     '文档分块大小（字符）', 1),
('RAG',       'chunk_overlap',           '100',                   'INT',     '分块重叠（字符）', 1),
('PYODIDE',   'pyodide_packages',        'pandas,numpy,matplotlib', 'STRING', 'Pyodide预装包', 1),
('PYODIDE',   'pyodide_version',         '0.26.2',                'STRING',  'Pyodide版本', 1),
('SYSTEM',    'max_file_size',           '5MB',                   'STRING',  '最大上传文件大小', 1),
('SYSTEM',    'upload_session_ttl_minutes', '120',                'INT',     '分片上传会话超时（分钟）', 1),
('SYSTEM',    'max_messages_per_day',    '100',                   'INT',     '每日最大对话消息数', 1),
('SECURITY',  'sensitive_word_filter',   'true',                  'BOOLEAN', '敏感词过滤开关', 1),
('SECURITY',  'rate_limit_per_minute',   '20',                    'INT',     '每分钟请求限流', 1);

-- ------------------------------------------------------------
-- 3.G 动画模板（animation_template，3条）
-- ------------------------------------------------------------
INSERT INTO animation_template (template_name, template_type, stage, description, template_content, preview_url, status, create_by) VALUES
('排序算法可视化', 'EXPLAIN', 'PRIMARY_HIGH', '通过SVG动画展示排序算法的比较与交换过程', '{"name":"排序算法可视化","type":"SVG","steps":[{"step":1,"title":"初始数组","elements":[{"type":"rect","x":50,"y":50,"w":50,"h":40,"fill":"#4CAF50","text":"5"},{"type":"rect","x":110,"y":50,"w":50,"h":40,"fill":"#4CAF50","text":"3"},{"type":"rect","x":170,"y":50,"w":50,"h":40,"fill":"#4CAF50","text":"8"}]},{"step":2,"title":"比较与交换","elements":[{"type":"rect","x":50,"y":50,"w":50,"h":40,"fill":"#FF9800","text":"5"},{"type":"rect","x":110,"y":50,"w":50,"h":40,"fill":"#FF9800","text":"3"}]},{"step":3,"title":"排序完成","elements":[{"type":"rect","x":50,"y":50,"w":50,"h":40,"fill":"#2196F3","text":"3"},{"type":"rect","x":110,"y":50,"w":50,"h":40,"fill":"#2196F3","text":"5"},{"type":"rect","x":170,"y":50,"w":50,"h":40,"fill":"#2196F3","text":"8"}]}]}', NULL, 1, 1),
('神经网络结构', 'CONCEPT', 'SENIOR', '展示神经网络输入层、隐藏层、输出层的结构', '{"name":"神经网络结构","type":"SVG","steps":[{"step":1,"title":"输入层","elements":[{"type":"circle","cx":100,"cy":100,"r":20,"fill":"#E91E63","text":"x1"},{"type":"circle","cx":100,"cy":160,"r":20,"fill":"#E91E63","text":"x2"}]},{"step":2,"title":"隐藏层","elements":[{"type":"circle","cx":200,"cy":80,"r":20,"fill":"#9C27B0","text":"h1"},{"type":"circle","cx":200,"cy":140,"r":20,"fill":"#9C27B0","text":"h2"},{"type":"circle","cx":200,"cy":200,"r":20,"fill":"#9C27B0","text":"h3"}]},{"step":3,"title":"输出层","elements":[{"type":"circle","cx":300,"cy":130,"r":20,"fill":"#3F51B5","text":"y"}]}]}', NULL, 1, 1),
('冒泡排序过程', 'PROCESS', 'JUNIOR', '分步展示冒泡排序的每轮比较与交换', '{"name":"冒泡排序过程","type":"SVG","steps":[{"step":1,"title":"第一轮比较","elements":[{"type":"rect","x":50,"y":50,"w":60,"h":40,"fill":"#4CAF50","text":"4"},{"type":"rect","x":120,"y":50,"w":60,"h":40,"fill":"#FF9800","text":"2"},{"type":"rect","x":190,"y":50,"w":60,"h":40,"fill":"#4CAF50","text":"7"},{"type":"rect","x":260,"y":50,"w":60,"h":40,"fill":"#4CAF50","text":"1"}]},{"step":2,"title":"继续比较","elements":[{"type":"rect","x":50,"y":50,"w":60,"h":40,"fill":"#4CAF50","text":"2"},{"type":"rect","x":120,"y":50,"w":60,"h":40,"fill":"#4CAF50","text":"4"},{"type":"rect","x":190,"y":50,"w":60,"h":40,"fill":"#FF9800","text":"7"},{"type":"rect","x":260,"y":50,"w":60,"h":40,"fill":"#FF9800","text":"1"}]},{"step":3,"title":"排序完成","elements":[{"type":"rect","x":50,"y":50,"w":60,"h":40,"fill":"#2196F3","text":"1"},{"type":"rect","x":120,"y":50,"w":60,"h":40,"fill":"#2196F3","text":"2"},{"type":"rect","x":190,"y":50,"w":60,"h":40,"fill":"#2196F3","text":"4"},{"type":"rect","x":260,"y":50,"w":60,"h":40,"fill":"#2196F3","text":"7"}]}]}', NULL, 1, 1);

-- ------------------------------------------------------------
-- 3.H 系统公告（system_notice，1条）
-- ------------------------------------------------------------
INSERT INTO system_notice (title, content, status, publish_time) VALUES
('欢迎使用 Nexora AI 教学助手', '欢迎来到 Nexora！\n\nNexora 是一款面向 K12 全学段的人工智能通识课教学助手。\n在这里，你可以：\n- 与 AI 助教对话，探索人工智能的奥秘\n- 学习分学段定制的 AI 通识课程\n- 动手体验 AI 绘画、动画生成、Python 编程\n- 获取个性化学习路径，科学高效地学习\n\n让我们一起开启 AI 学习之旅吧！', 1, NOW());

-- ------------------------------------------------------------
-- 3.I 知识文档（knowledge_doc，5条）
-- vector_status=0（待处理），content 为简化 Markdown 示例
-- ------------------------------------------------------------
INSERT INTO knowledge_doc (doc_id, title, stage, knowledge_point_id, difficulty, data_type, content, source_type, vector_status, chunk_count, status, create_by) VALUES
('DOC_001', '什么是人工智能', 'PRIMARY_LOW', 'KP_PL_001', 1, 'KNOWLEDGE', '# 什么是人工智能\n\n人工智能（AI）就是让机器像人一样思考和学习的技术。\n\n## 生活中的AI\n- 语音助手（如Siri）\n- 自动驾驶汽车\n- 人脸识别\n\n## 小贴士\nAI就在我们身边，它可以帮助我们做很多事情！\n\n> 思考：你今天用到AI了吗？', 0, 0, 0, 1, 1),
('DOC_002', '机器学习入门', 'JUNIOR', 'KP_JR_002', 2, 'KNOWLEDGE', '# 机器学习入门\n\n机器学习是人工智能的核心技术之一，让计算机从数据中学习规律。\n\n## 基本概念\n- **训练数据**：用于学习的示例\n- **模型**：学到的规律\n- **预测**：用模型对新数据做出判断\n\n## 学习方式\n1. 监督学习：有标注数据\n2. 无监督学习：无标注数据\n3. 强化学习：通过奖励学习\n\n> 机器学习的核心是从数据中提取模式。', 0, 0, 0, 1, 1),
('DOC_003', 'Python变量与数据类型', 'PRIMARY_HIGH', 'KP_PH_004', 1, 'KNOWLEDGE', '# Python变量与数据类型\n\n## 变量\n变量是存储数据的容器。\n\n```python\nname = "小明"\nage = 12\nheight = 1.5\n```\n\n## 数据类型\n- **int**：整数\n- **float**：小数\n- **str**：字符串\n- **bool**：布尔值\n\n> 动手试一试：创建你自己的变量！', 0, 0, 0, 1, 1),
('DOC_004', '神经网络基础', 'SENIOR', 'KP_SR_003', 3, 'KNOWLEDGE', '# 神经网络与深度学习\n\n## 神经元模型\n神经网络模拟人脑神经元结构，每个神经元接收输入、加权求和、通过激活函数输出。\n\n## 网络结构\n- **输入层**：接收特征数据\n- **隐藏层**：提取特征\n- **输出层**：给出预测结果\n\n## 激活函数\n- Sigmoid\n- ReLU\n- Tanh\n\n> 深度学习 = 多隐藏层神经网络。', 0, 0, 0, 1, 1),
('DOC_005', 'AI伦理思考', 'JUNIOR', 'KP_JR_008', 2, 'KNOWLEDGE', '# AI伦理思考\n\n## AI带来的伦理问题\n- **隐私保护**：AI需要大量数据\n- **算法偏见**：训练数据可能带有偏见\n- **责任归属**：AI决策出错谁来负责\n\n## 负责任的AI\n1. 透明性：AI决策可解释\n2. 公平性：避免歧视\n3. 安全性：不造成伤害\n\n> 技术发展需要伦理约束。', 0, 0, 0, 1, 1);
