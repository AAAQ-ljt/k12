/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50739 (5.7.39-log)
 Source Host           : localhost:3306
 Source Schema         : smart-campus

 Target Server Type    : MySQL
 Target Server Version : 50739 (5.7.39-log)
 File Encoding         : 65001

 Date: 12/05/2026 10:37:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for class_info
-- ----------------------------
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info`  (
  `class_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `department_id` int(11) NOT NULL COMMENT '所属院系ID',
  `major_id` int(11) NOT NULL COMMENT '所属专业ID',
  `class_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级名称',
  `counselor_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '辅导员姓名',
  `head_teacher_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班主任姓名',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班级说明',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值',
  PRIMARY KEY (`class_id`) USING BTREE,
  INDEX `idx_department_id`(`department_id`) USING BTREE,
  INDEX `idx_major_id`(`major_id`) USING BTREE,
  INDEX `idx_class_name`(`class_name`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10171 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '班级表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class_info
-- ----------------------------

-- ----------------------------
-- Table structure for course_assessment_submit
-- ----------------------------
DROP TABLE IF EXISTS `course_assessment_submit`;
CREATE TABLE `course_assessment_submit`  (
  `submit_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务ID，对应course_chapter_lesson.lesson_id或者考试ID',
  `task_type` tinyint(4) NOT NULL COMMENT '任务类型快照: 1作业 2考试',
  `paper_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '试卷ID',
  `user_id` int(11) NOT NULL COMMENT '学生ID，对应 user_info.user_id',
  `submit_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '提交状态:0待开始 1作答中 2草稿 3已提交',
  `judge_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '批改状态: 0未批改 1自动判分完成 2待人工批改 3人工批改完成',
  `started_time` datetime NULL DEFAULT NULL COMMENT '开始作答时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `forced_submit_time` datetime NULL DEFAULT NULL COMMENT '强制交卷时间，如超时系统自动提交',
  `used_seconds` int(11) NOT NULL DEFAULT 0 COMMENT '本次作答耗时，单位秒',
  `objective_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '客观题得分',
  `subjective_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '主观题得分',
  `submit_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '整卷提交补充内容，建议存JSON，如总体说明、附件等',
  `teacher_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师评语',
  `judge_time` datetime NULL DEFAULT NULL COMMENT '批改完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`submit_id`) USING BTREE,
  UNIQUE INDEX `uk_task_user_attempt`(`task_id`, `user_id`, `paper_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_submit_time`(`submit_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程作业/考试学生提交表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_assessment_submit
-- ----------------------------

-- ----------------------------
-- Table structure for course_assessment_submit_question
-- ----------------------------
DROP TABLE IF EXISTS `course_assessment_submit_question`;
CREATE TABLE `course_assessment_submit_question`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `submit_id` bigint(20) NOT NULL COMMENT '提交ID，对应 course_assessment_submit.submit_id',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID，对应 course_assessment_task.task_id',
  `paper_id` int(11) NOT NULL COMMENT '试卷题目编排ID，对应 paper_info.paper_id',
  `question_id` int(11) NULL DEFAULT NULL COMMENT '题目ID，对应 question_info.question_id',
  `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '学生答案，建议存JSON',
  `final_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '最终得分',
  `judge_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '判题状态: 0未判 1自动判分完成 2待人工批改 3人工批改完成',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submit_question`(`submit_id`, `question_id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_judge_status`(`judge_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 229 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程作业/考试学生答题明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_assessment_submit_question
-- ----------------------------

-- ----------------------------
-- Table structure for course_chapter
-- ----------------------------
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter`  (
  `chapter_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属课程ID',
  `chapter_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节说明',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值',
  PRIMARY KEY (`chapter_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_chapter_name`(`chapter_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程章节表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_chapter
-- ----------------------------

-- ----------------------------
-- Table structure for course_chapter_lesson
-- ----------------------------
DROP TABLE IF EXISTS `course_chapter_lesson`;
CREATE TABLE `course_chapter_lesson`  (
  `lesson_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属课程ID',
  `chapter_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属章节ID',
  `lesson_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课时名称',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值',
  PRIMARY KEY (`lesson_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id`) USING BTREE,
  INDEX `idx_lesson_name`(`lesson_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程课时表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_chapter_lesson
-- ----------------------------

-- ----------------------------
-- Table structure for course_chapter_lesson_resource
-- ----------------------------
DROP TABLE IF EXISTS `course_chapter_lesson_resource`;
CREATE TABLE `course_chapter_lesson_resource`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lesson_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课时ID',
  `resource_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源ID，对应resource_info.resource_id或习题id',
  `resource_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '资源类型: 1视频 2课件 3作业',
  `is_primary` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否主资源: 1是 0否',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lesson_id`(`lesson_id`) USING BTREE,
  INDEX `idx_resource_id`(`resource_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10142 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课时资源关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_chapter_lesson_resource
-- ----------------------------

-- ----------------------------
-- Table structure for course_class
-- ----------------------------
DROP TABLE IF EXISTS `course_class`;
CREATE TABLE `course_class`  (
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程ID',
  `class_id` int(11) NOT NULL COMMENT '班级ID',
  PRIMARY KEY (`course_id`, `class_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_class_id`(`class_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程班级关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_class
-- ----------------------------

-- ----------------------------
-- Table structure for course_info
-- ----------------------------
DROP TABLE IF EXISTS `course_info`;
CREATE TABLE `course_info`  (
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `course_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `cover_resource_id` int(11) NULL DEFAULT NULL COMMENT '课程封面资源ID，可为空，对应resource_info.resource_id',
  `teacher_id` int(11) NOT NULL COMMENT '授课老师ID，对应user_info.user_id',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课程简介',
  `record_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '录制状态: 0录制中 1录制完成',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '课程状态: 1正常 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`course_id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE,
  INDEX `idx_course_name`(`course_name`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_info
-- ----------------------------

-- ----------------------------
-- Table structure for course_study_lesson_progress
-- ----------------------------
DROP TABLE IF EXISTS `course_study_lesson_progress`;
CREATE TABLE `course_study_lesson_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '学生ID，对应 user_info.user_id',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程ID，对应 course_info.course_id',
  `chapter_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节ID，对应 course_chapter.chapter_id',
  `lesson_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课时ID，对应 course_chapter_lesson.lesson_id',
  `video_resource_id` int(11) NULL DEFAULT NULL COMMENT '视频资源ID，对应 resource_info.resource_id',
  `study_seconds` int(11) NOT NULL DEFAULT 0 COMMENT '累计学习时长，单位秒，重复观看可累计',
  `last_position_seconds` int(11) NOT NULL DEFAULT 0 COMMENT '上次播放位置，单位秒',
  `max_position_seconds` int(11) NOT NULL DEFAULT 0 COMMENT '历史最远播放位置，单位秒',
  `video_duration_seconds` int(11) NOT NULL DEFAULT 0 COMMENT '视频总时长，单位秒',
  `is_completed` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否完成: 0否 1是',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `last_study_time` datetime NULL DEFAULT NULL COMMENT '最后学习时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_lesson`(`user_id`, `lesson_id`) USING BTREE,
  INDEX `idx_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id`) USING BTREE,
  INDEX `idx_last_study_time`(`last_study_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生课时学习进度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_study_lesson_progress
-- ----------------------------

-- ----------------------------
-- Table structure for course_study_log
-- ----------------------------
DROP TABLE IF EXISTS `course_study_log`;
CREATE TABLE `course_study_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学习会话ID',
  `user_id` int(11) NOT NULL COMMENT '学生ID，对应 user_info.user_id',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程ID，对应 course_info.course_id',
  `chapter_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节ID，对应 course_chapter.chapter_id',
  `lesson_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课时ID，对应 course_chapter_lesson.lesson_id',
  `start_time` datetime NOT NULL COMMENT '本次学习开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '本次学习结束时间',
  `study_seconds` int(11) NOT NULL DEFAULT 0 COMMENT '本次学习时长，单位秒',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_id`(`session_id`) USING BTREE,
  INDEX `idx_user_time`(`user_id`, `start_time`) USING BTREE,
  INDEX `idx_user_course_time`(`user_id`, `course_id`, `start_time`) USING BTREE,
  INDEX `idx_user_lesson_time`(`user_id`, `lesson_id`, `start_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生学习流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_study_log
-- ----------------------------

-- ----------------------------
-- Table structure for course_study_progress
-- ----------------------------
DROP TABLE IF EXISTS `course_study_progress`;
CREATE TABLE `course_study_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '学生ID，对应 user_info.user_id',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程ID，对应 course_info.course_id',
  `current_chapter_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '当前学习章节ID，对应 course_chapter.chapter_id',
  `current_lesson_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '当前学习课时ID，对应 course_chapter_lesson.lesson_id',
  `study_seconds` int(11) NOT NULL DEFAULT 0 COMMENT '累计学习时长，单位秒，重复观看可累计',
  `last_study_time` datetime NULL DEFAULT NULL COMMENT '最后学习时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '学习状态: 0未开始 1学习中 2已完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_current_lesson_id`(`current_lesson_id`) USING BTREE,
  INDEX `idx_last_study_time`(`last_study_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生课程学习进度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_study_progress
-- ----------------------------

-- ----------------------------
-- Table structure for course_user_collection
-- ----------------------------
DROP TABLE IF EXISTS `course_user_collection`;
CREATE TABLE `course_user_collection`  (
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '主键ID',
  PRIMARY KEY (`course_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程收藏' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_user_collection
-- ----------------------------

-- ----------------------------
-- Table structure for department_info
-- ----------------------------
DROP TABLE IF EXISTS `department_info`;
CREATE TABLE `department_info`  (
  `department_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `department_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '院系编码',
  `department_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '院系名称',
  `leader_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '院系说明',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值',
  PRIMARY KEY (`department_id`) USING BTREE,
  UNIQUE INDEX `uk_department_code`(`department_code`) USING BTREE,
  INDEX `idx_department_name`(`department_name`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10020 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '院系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of department_info
-- ----------------------------
INSERT INTO `department_info` VALUES (10000, 'DEPT_10000', '计算机学院', '张伟', '13812345678', '计算机科学与技术、软件工程、网络工程等专业', 1, 0);
INSERT INTO `department_info` VALUES (10001, 'DEPT_10001', '软件学院', '李芳', '13923456789', '软件工程、数字媒体技术、数据科学等', 1, 1);
INSERT INTO `department_info` VALUES (10002, 'DEPT_10002', '人工智能学院', '王强', '15034567890', '人工智能、智能科学与技术、机器人工程', 1, 2);
INSERT INTO `department_info` VALUES (10003, 'DEPT_10003', '数据科学学院', '陈静', '15145678901', '数据科学与大数据技术、统计学', 1, 3);
INSERT INTO `department_info` VALUES (10004, 'DEPT_10004', '电子信息工程学院', '刘明', '15256789012', '电子信息工程、电子科学与技术', 1, 4);
INSERT INTO `department_info` VALUES (10005, 'DEPT_10005', '通信工程学院', '赵敏', '15367890123', '通信工程、信息工程、物联网工程', 1, 5);
INSERT INTO `department_info` VALUES (10006, 'DEPT_10006', '自动化学院', '周涛', '15478901234', '自动化、测控技术与仪器', 1, 6);
INSERT INTO `department_info` VALUES (10007, 'DEPT_10007', '机械工程学院', '吴迪', '15589012345', '机械设计制造及其自动化、车辆工程', 1, 7);
INSERT INTO `department_info` VALUES (10008, 'DEPT_10008', '电气工程学院', '郑爽', '15690123456', '电气工程及其自动化、智能电网', 1, 8);
INSERT INTO `department_info` VALUES (10009, 'DEPT_10009', '土木工程学院', '孙阳', '15701234567', '土木工程、给排水科学与工程', 1, 9);
INSERT INTO `department_info` VALUES (10010, 'DEPT_10010', '建筑学院', '林晨', '15812345678', '建筑学、城乡规划、风景园林', 1, 10);
INSERT INTO `department_info` VALUES (10011, 'DEPT_10011', '化学化工学院', '郭峰', '15923456789', '化学工程与工艺、应用化学', 1, 11);
INSERT INTO `department_info` VALUES (10012, 'DEPT_10012', '材料科学与工程学院', '唐雅', '17034567890', '材料科学与工程、高分子材料', 1, 12);
INSERT INTO `department_info` VALUES (10013, 'DEPT_10013', '环境科学与工程学院', '沈梦', '17145678901', '环境工程、环境科学', 1, 13);
INSERT INTO `department_info` VALUES (10014, 'DEPT_10014', '生物医学工程学院', '宋阳', '17256789012', '生物医学工程、医学信息工程', 1, 14);
INSERT INTO `department_info` VALUES (10015, 'DEPT_10015', '医学院', '许杰', '17367890123', '临床医学、护理学、基础医学', 1, 15);
INSERT INTO `department_info` VALUES (10016, 'DEPT_10016', '药学院', '何璐', '17478901234', '药学、药物制剂、临床药学', 1, 16);
INSERT INTO `department_info` VALUES (10017, 'DEPT_10017', '经济学院', '黄欣', '17589012345', '经济学、国际经济与贸易', 1, 17);
INSERT INTO `department_info` VALUES (10018, 'DEPT_10018', '金融学院', '丁宁', '17690123456', '金融学、金融工程、保险学', 1, 18);
INSERT INTO `department_info` VALUES (10019, 'DEPT_10019', '法学院', '魏晨', '17701234567', '法学、知识产权、政治学', 1, 19);

-- ----------------------------
-- Table structure for exam_class
-- ----------------------------
DROP TABLE IF EXISTS `exam_class`;
CREATE TABLE `exam_class`  (
  `exam_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试ID',
  `class_id` int(11) NOT NULL COMMENT '班级ID',
  UNIQUE INDEX `uk_exam_class`(`exam_id`, `class_id`) USING BTREE,
  INDEX `idx_class_id`(`class_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试班级关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_class
-- ----------------------------

-- ----------------------------
-- Table structure for exam_info
-- ----------------------------
DROP TABLE IF EXISTS `exam_info`;
CREATE TABLE `exam_info`  (
  `exam_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试ID',
  `exam_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试名称',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程ID',
  `paper_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '试卷ID',
  `teacher_id` int(11) NOT NULL COMMENT '教师ID',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '考试说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`exam_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_time_range`(`start_time`, `end_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '在线考试表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_info
-- ----------------------------

-- ----------------------------
-- Table structure for major_info
-- ----------------------------
DROP TABLE IF EXISTS `major_info`;
CREATE TABLE `major_info`  (
  `major_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `department_id` int(11) NOT NULL COMMENT '所属院系ID',
  `major_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '专业编码',
  `major_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '专业名称',
  `educational_system_type` tinyint(1) NULL DEFAULT NULL COMMENT '学制，如3年/4年',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专业简介',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值',
  PRIMARY KEY (`major_id`) USING BTREE,
  UNIQUE INDEX `uk_major_code`(`major_code`) USING BTREE,
  INDEX `idx_department_id`(`department_id`) USING BTREE,
  INDEX `idx_major_name`(`major_name`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10084 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '专业表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of major_info
-- ----------------------------
INSERT INTO `major_info` VALUES (10000, 10000, 'MAJ_10000', '计算机科学与技术', 4, '培养计算机系统设计、开发与应用的高级专门人才。', 1, 1);
INSERT INTO `major_info` VALUES (10001, 10000, 'MAJ_10001', '网络空间安全', 4, '培养网络安全防护、渗透测试与安全管理人才。', 1, 2);
INSERT INTO `major_info` VALUES (10002, 10000, 'MAJ_10002', '物联网工程', 4, '培养物联网系统开发、智能硬件集成人才。', 1, 3);
INSERT INTO `major_info` VALUES (10003, 10000, 'MAJ_10003', '区块链工程', 4, '培养区块链技术开发、智能合约与分布式应用人才。', 1, 4);
INSERT INTO `major_info` VALUES (10004, 10000, 'MAJ_10004', '智能科学与技术', 4, '培养智能系统、模式识别与机器学习人才。', 1, 5);
INSERT INTO `major_info` VALUES (10005, 10001, 'MAJ_10005', '软件工程', 4, '培养软件开发、测试、维护与项目管理人才。', 1, 6);
INSERT INTO `major_info` VALUES (10006, 10001, 'MAJ_10006', '数字媒体技术', 4, '培养游戏开发、虚拟现实与交互设计人才。', 1, 7);
INSERT INTO `major_info` VALUES (10007, 10001, 'MAJ_10007', '数据科学与大数据技术', 4, '培养大数据处理、分析与可视化人才。', 1, 8);
INSERT INTO `major_info` VALUES (10008, 10001, 'MAJ_10008', '电子与计算机工程', 4, '培养软硬件协同设计与嵌入式系统开发人才。', 1, 9);
INSERT INTO `major_info` VALUES (10009, 10002, 'MAJ_10009', '人工智能', 4, '培养AI算法、深度学习框架应用人才。', 1, 10);
INSERT INTO `major_info` VALUES (10010, 10002, 'MAJ_10010', '机器人工程', 4, '培养机器人设计、控制与系统集成人才。', 1, 11);
INSERT INTO `major_info` VALUES (10011, 10002, 'MAJ_10011', '智能测控工程', 4, '培养智能仪器、自动测试与感知技术人才。', 1, 12);
INSERT INTO `major_info` VALUES (10012, 10002, 'MAJ_10012', '计算机视觉', 4, '培养图像识别、视频分析与视觉感知算法人才。', 1, 13);
INSERT INTO `major_info` VALUES (10013, 10002, 'MAJ_10013', '自然语言处理', 4, '培养语言模型、机器翻译与文本挖掘人才。', 1, 14);
INSERT INTO `major_info` VALUES (10014, 10003, 'MAJ_10014', '统计学', 4, '培养数据建模、统计推断与数据挖掘人才。', 1, 15);
INSERT INTO `major_info` VALUES (10015, 10003, 'MAJ_10015', '信息管理与信息系统', 4, '培养数据分析与信息系统开发人才。', 1, 16);
INSERT INTO `major_info` VALUES (10016, 10003, 'MAJ_10016', '应用统计学', 4, '侧重金融、生物等领域统计应用。', 1, 17);
INSERT INTO `major_info` VALUES (10017, 10003, 'MAJ_10017', '数据计算及应用', 4, '培养数据科学算法与计算平台开发人才。', 1, 18);
INSERT INTO `major_info` VALUES (10018, 10004, 'MAJ_10018', '电子信息工程', 4, '培养电子系统、信号处理与嵌入式设计人才。', 1, 19);
INSERT INTO `major_info` VALUES (10019, 10004, 'MAJ_10019', '电子科学与技术', 4, '培养集成电路、微电子与光电子人才。', 1, 20);
INSERT INTO `major_info` VALUES (10020, 10004, 'MAJ_10020', '微电子科学与工程', 4, '培养芯片设计、半导体工艺人才。', 1, 21);
INSERT INTO `major_info` VALUES (10021, 10004, 'MAJ_10021', '光电信息科学与工程', 4, '培养光学、激光与光电检测人才。', 1, 22);
INSERT INTO `major_info` VALUES (10022, 10004, 'MAJ_10022', '集成电路设计与集成系统', 4, '培养数字/模拟IC设计、版图与验证人才。', 1, 23);
INSERT INTO `major_info` VALUES (10023, 10005, 'MAJ_10023', '通信工程', 4, '培养通信系统、网络协议与无线通信人才。', 1, 24);
INSERT INTO `major_info` VALUES (10024, 10005, 'MAJ_10024', '信息工程', 4, '培养信息编码、传输与交换人才。', 1, 25);
INSERT INTO `major_info` VALUES (10025, 10005, 'MAJ_10025', '电磁场与无线技术', 4, '培养射频电路、天线设计与电磁兼容人才。', 1, 26);
INSERT INTO `major_info` VALUES (10026, 10005, 'MAJ_10026', '电信工程及管理', 4, '培养通信工程建设与项目管理人才。', 1, 27);
INSERT INTO `major_info` VALUES (10027, 10006, 'MAJ_10027', '自动化', 4, '培养控制系统、运动控制与过程控制人才。', 1, 28);
INSERT INTO `major_info` VALUES (10028, 10006, 'MAJ_10028', '测控技术与仪器', 4, '培养智能仪器、自动测试系统人才。', 1, 29);
INSERT INTO `major_info` VALUES (10029, 10006, 'MAJ_10029', '轨道交通信号与控制', 4, '培养列车运行控制、信号系统设计人才。', 1, 30);
INSERT INTO `major_info` VALUES (10030, 10006, 'MAJ_10030', '机器人技术', 4, '培养工业机器人编程与集成应用人才。', 1, 31);
INSERT INTO `major_info` VALUES (10031, 10007, 'MAJ_10031', '机械设计制造及其自动化', 4, '培养机械设计、制造工艺与自动化装备人才。', 1, 32);
INSERT INTO `major_info` VALUES (10032, 10007, 'MAJ_10032', '车辆工程', 4, '培养汽车设计、新能源汽车技术人才。', 1, 33);
INSERT INTO `major_info` VALUES (10033, 10007, 'MAJ_10033', '机械电子工程', 4, '培养机电一体化、液压控制人才。', 1, 34);
INSERT INTO `major_info` VALUES (10034, 10007, 'MAJ_10034', '工业设计', 4, '培养产品造型、人机交互与设计思维人才。', 1, 35);
INSERT INTO `major_info` VALUES (10035, 10007, 'MAJ_10035', '智能制造工程', 4, '培养智能产线、数字孪生与工业机器人集成人才。', 1, 36);
INSERT INTO `major_info` VALUES (10036, 10008, 'MAJ_10036', '电气工程及其自动化', 4, '培养电力系统、电机电器与高电压技术人才。', 1, 37);
INSERT INTO `major_info` VALUES (10037, 10008, 'MAJ_10037', '智能电网信息工程', 4, '培养智能电网、新能源并网技术人才。', 1, 38);
INSERT INTO `major_info` VALUES (10038, 10008, 'MAJ_10038', '能源与动力工程', 4, '培养热能动力、制冷与新能源开发人才。', 1, 39);
INSERT INTO `major_info` VALUES (10039, 10008, 'MAJ_10039', '新能源科学与工程', 4, '培养风能、太阳能、氢能等新能源技术人才。', 1, 40);
INSERT INTO `major_info` VALUES (10040, 10009, 'MAJ_10040', '土木工程', 4, '培养建筑工程、道路桥梁与地下工程人才。', 1, 41);
INSERT INTO `major_info` VALUES (10041, 10009, 'MAJ_10041', '给排水科学与工程', 4, '培养城市给水排水、水处理工程人才。', 1, 42);
INSERT INTO `major_info` VALUES (10042, 10009, 'MAJ_10042', '建筑环境与能源应用工程', 4, '培养暖通空调、建筑节能技术人才。', 1, 43);
INSERT INTO `major_info` VALUES (10043, 10009, 'MAJ_10043', '工程管理', 4, '培养建设项目管理、造价与BIM人才。', 1, 44);
INSERT INTO `major_info` VALUES (10044, 10009, 'MAJ_10044', '智能建造', 4, '培养BIM技术、装配式建筑与施工机器人应用人才。', 1, 45);
INSERT INTO `major_info` VALUES (10045, 10010, 'MAJ_10045', '建筑学', 5, '培养建筑设计、城市规划与历史建筑保护人才。', 1, 46);
INSERT INTO `major_info` VALUES (10046, 10010, 'MAJ_10046', '城乡规划', 5, '培养国土空间规划、城市设计与交通规划人才。', 1, 47);
INSERT INTO `major_info` VALUES (10047, 10010, 'MAJ_10047', '风景园林', 4, '培养景观设计、园林植物与生态修复人才。', 1, 48);
INSERT INTO `major_info` VALUES (10048, 10010, 'MAJ_10048', '历史建筑保护工程', 4, '培养古建筑修复、遗产保护与监测人才。', 1, 49);
INSERT INTO `major_info` VALUES (10049, 10011, 'MAJ_10049', '化学工程与工艺', 4, '培养化工过程开发、设计及生产管理人才。', 1, 50);
INSERT INTO `major_info` VALUES (10050, 10011, 'MAJ_10050', '应用化学', 4, '培养精细化工、分析与材料化学人才。', 1, 51);
INSERT INTO `major_info` VALUES (10051, 10011, 'MAJ_10051', '能源化学工程', 4, '培养能源转化、电池材料与催化技术人才。', 1, 52);
INSERT INTO `major_info` VALUES (10052, 10011, 'MAJ_10052', '制药工程', 4, '培养药物合成、工艺设计与GMP管理人才。', 1, 53);
INSERT INTO `major_info` VALUES (10053, 10012, 'MAJ_10053', '材料科学与工程', 4, '培养金属、无机非、高分子等材料研发人才。', 1, 54);
INSERT INTO `major_info` VALUES (10054, 10012, 'MAJ_10054', '高分子材料与工程', 4, '培养塑料、橡胶、纤维及复合材料技术人才。', 1, 55);
INSERT INTO `major_info` VALUES (10055, 10012, 'MAJ_10055', '纳米材料与技术', 4, '培养纳米材料制备、表征与器件应用人才。', 1, 56);
INSERT INTO `major_info` VALUES (10056, 10012, 'MAJ_10056', '新能源材料与器件', 4, '培养锂电、光伏、氢能材料与器件人才。', 1, 57);
INSERT INTO `major_info` VALUES (10057, 10012, 'MAJ_10057', '材料物理', 4, '培养材料计算模拟、性能表征与功能材料人才。', 1, 58);
INSERT INTO `major_info` VALUES (10058, 10013, 'MAJ_10058', '环境工程', 4, '培养水、气、固废污染控制与治理人才。', 1, 59);
INSERT INTO `major_info` VALUES (10059, 10013, 'MAJ_10059', '环境科学', 4, '培养环境监测、评价与生态修复人才。', 1, 60);
INSERT INTO `major_info` VALUES (10060, 10013, 'MAJ_10060', '环境生态工程', 4, '培养生态修复、流域治理与生物多样性保护人才。', 1, 61);
INSERT INTO `major_info` VALUES (10061, 10013, 'MAJ_10061', '资源环境科学', 4, '培养资源循环、清洁生产与可持续发展人才。', 1, 62);
INSERT INTO `major_info` VALUES (10062, 10014, 'MAJ_10062', '生物医学工程', 4, '培养医学仪器、生物信号处理与医学影像人才。', 1, 63);
INSERT INTO `major_info` VALUES (10063, 10014, 'MAJ_10063', '假肢矫形工程', 4, '培养康复辅具设计、人体生物力学人才。', 1, 64);
INSERT INTO `major_info` VALUES (10064, 10014, 'MAJ_10064', '医学信息工程', 4, '培养医疗信息化、健康大数据与AI辅助诊断人才。', 1, 65);
INSERT INTO `major_info` VALUES (10065, 10014, 'MAJ_10065', '生物制药', 4, '培养生物药研发、细胞培养与纯化工艺人才。', 1, 66);
INSERT INTO `major_info` VALUES (10066, 10015, 'MAJ_10066', '临床医学', 5, '培养基础医学与临床诊疗技能，从事医疗工作的医师。', 1, 67);
INSERT INTO `major_info` VALUES (10067, 10015, 'MAJ_10067', '护理学', 4, '培养临床护理、社区护理与护理管理人才。', 1, 68);
INSERT INTO `major_info` VALUES (10068, 10015, 'MAJ_10068', '医学影像学', 5, '培养影像诊断、介入放射与核医学人才。', 1, 69);
INSERT INTO `major_info` VALUES (10069, 10015, 'MAJ_10069', '麻醉学', 5, '培养临床麻醉、重症监测与疼痛诊疗人才。', 1, 70);
INSERT INTO `major_info` VALUES (10070, 10015, 'MAJ_10070', '口腔医学', 5, '培养口腔疾病诊疗、修复与正畸人才。', 1, 71);
INSERT INTO `major_info` VALUES (10071, 10016, 'MAJ_10071', '药学', 4, '培养药物研发、生产、检验与临床合理用药人才。', 1, 72);
INSERT INTO `major_info` VALUES (10072, 10016, 'MAJ_10072', '药物制剂', 4, '培养制剂工艺、缓控释与新型给药系统人才。', 1, 73);
INSERT INTO `major_info` VALUES (10073, 10016, 'MAJ_10073', '临床药学', 5, '培养药学监护、药物评价与个体化用药人才。', 1, 74);
INSERT INTO `major_info` VALUES (10074, 10016, 'MAJ_10074', '中药学', 4, '培养中药鉴定、炮制与复方开发人才。', 1, 75);
INSERT INTO `major_info` VALUES (10075, 10017, 'MAJ_10075', '经济学', 4, '培养经济理论、政策分析与计量实证人才。', 1, 76);
INSERT INTO `major_info` VALUES (10076, 10017, 'MAJ_10076', '经济统计学', 4, '培养经济数据采集、核算与预测分析人才。', 1, 77);
INSERT INTO `major_info` VALUES (10077, 10017, 'MAJ_10077', '国民经济管理', 4, '培养宏观经济规划、产业分析与政策评估人才。', 1, 78);
INSERT INTO `major_info` VALUES (10078, 10018, 'MAJ_10078', '金融学', 4, '培养银行、证券、保险等金融业务人才。', 1, 79);
INSERT INTO `major_info` VALUES (10079, 10018, 'MAJ_10079', '金融工程', 4, '培养金融产品设计、风险管理与量化交易人才。', 1, 80);
INSERT INTO `major_info` VALUES (10080, 10018, 'MAJ_10080', '保险学', 4, '培养精算、核保理赔与保险产品设计人才。', 1, 81);
INSERT INTO `major_info` VALUES (10081, 10018, 'MAJ_10081', '投资学', 4, '培养资产配置、证券分析与投资管理人才。', 1, 82);
INSERT INTO `major_info` VALUES (10082, 10019, 'MAJ_10082', '法学', 4, '培养法律实务、诉讼与合规管理人才。', 1, 83);
INSERT INTO `major_info` VALUES (10083, 10019, 'MAJ_10083', '知识产权', 4, '培养专利、商标、版权及知识产权运营人才。', 1, 84);

-- ----------------------------
-- Table structure for message_info
-- ----------------------------
DROP TABLE IF EXISTS `message_info`;
CREATE TABLE `message_info`  (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息标题',
  `message_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `message_type` tinyint(4) NOT NULL COMMENT '消息类型: 1系统通知 2课程消息 3作业消息 4考试消息',
  `biz_type` tinyint(4) NULL DEFAULT 0 COMMENT '业务类型: 0通用 1课程 2作业 3考试',
  `biz_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务ID，如 courseId/taskId/examId',
  `sender_id` int(11) NULL DEFAULT NULL COMMENT '发送人ID，系统消息可为空',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送人名称，冗余展示',
  `send_scope` tinyint(4) NOT NULL DEFAULT 1 COMMENT '发送范围: 1单人 2多人 3全体学生',
  `jump_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '点击跳转路径',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_type_time`(`message_type`, `send_time`) USING BTREE,
  INDEX `idx_biz`(`biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_sender`(`sender_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '站内消息主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message_info
-- ----------------------------

-- ----------------------------
-- Table structure for message_user
-- ----------------------------
DROP TABLE IF EXISTS `message_user`;
CREATE TABLE `message_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `user_id` int(11) NOT NULL COMMENT '接收用户ID',
  `read_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已读: 0否 1是',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id`, `user_id`) USING BTREE,
  INDEX `idx_user_read`(`user_id`, `read_flag`, `create_time`) USING BTREE,
  INDEX `idx_user_type`(`user_id`, `delete_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 451 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户消息收件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message_user
-- ----------------------------

-- ----------------------------
-- Table structure for paper_info
-- ----------------------------
DROP TABLE IF EXISTS `paper_info`;
CREATE TABLE `paper_info`  (
  `paper_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `paper_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '试卷名称',
  `paper_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '试卷类型:1课后习题 2考试试卷',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '试卷说明',
  `total_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '试卷总分',
  `duration_minutes` int(11) NULL DEFAULT NULL COMMENT '考试时长，单位分钟，课后习题可为空',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`paper_id`) USING BTREE,
  INDEX `idx_paper_type`(`paper_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '试卷信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of paper_info
-- ----------------------------

-- ----------------------------
-- Table structure for paper_question
-- ----------------------------
DROP TABLE IF EXISTS `paper_question`;
CREATE TABLE `paper_question`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '试卷ID，对应paper_info.paper_id',
  `question_id` int(11) NULL DEFAULT NULL COMMENT '题目ID，对应question_info.question_id',
  `question_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '试卷内题目分值',
  `question_type` tinyint(1) NULL DEFAULT NULL COMMENT '题目类型:1单选 2多选 3判断 4填空',
  `section_type` tinyint(1) NULL DEFAULT NULL COMMENT '分组 类型 1:分组 0:题目 ',
  `section_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组名称，可为空，如单选题、多选题',
  `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '父ID,如果是分组父级ID为0',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '试卷内排序值',
  `question_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目快照，建议存JSON，包含标题、配图、选项、答案、答案解析等',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_paper_question`(`paper_id`, `question_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_question_type`(`question_type`) USING BTREE,
  INDEX `idx_sort_order`(`sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 94 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '试卷题目编排表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of paper_question
-- ----------------------------

-- ----------------------------
-- Table structure for question_info
-- ----------------------------
DROP TABLE IF EXISTS `question_info`;
CREATE TABLE `question_info`  (
  `question_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_type` tinyint(1) NOT NULL COMMENT '题目类型:1单选 2多选 3判断 4简答题',
  `question_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题目标题',
  `question_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题目配图，可为空，关联resource_info.resource_id多个用逗号隔开',
  `difficulty_level` tinyint(1) NOT NULL DEFAULT 3 COMMENT '难度等级:1简单 2较易 3中等 4较难 5困难',
  `correct_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标准答案，建议存JSON或统一文本，如果是选择题，存储选择题选项ID,exercise_question_option.option_id',
  `answer_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案解析',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`question_id`) USING BTREE,
  INDEX `idx_question_type`(`question_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 231 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '习题题目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question_info
-- ----------------------------
INSERT INTO `question_info` VALUES (1, 1, 'Java 是哪种类型的语言？', NULL, 1, '3', 'Java 是纯面向对象的编程语言（虽保留基本数据类型，但一切皆对象的行为在包装类体现）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (2, 1, '以下哪个是 Java 中正确的 main 方法声明？', NULL, 1, '7', '必须是 public static void main(String[] args)，方法名、参数类型固定，参数名可变。', '2026-04-30 10:46:38', '2026-04-30 10:46:38');
INSERT INTO `question_info` VALUES (3, 1, '下列哪个不是 Java 的关键字？', NULL, 2, '11', 'goto 是保留字但未使用，const 也是保留字。include 是 C 语言预处理指令，不是 Java 关键字。', '2026-04-30 10:46:38', '2026-04-30 10:46:38');
INSERT INTO `question_info` VALUES (4, 1, 'Java 中，以下哪个数据类型占用内存最小？', NULL, 2, '14', 'boolean 理论上 1 位，但实际虚拟机实现可能为 1 字节；byte 明确 1 字节。题目比较时按规范 byte 最小。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (5, 1, '关于 JVM 的说法，正确的是？', NULL, 3, '18', 'JVM 是 Java 字节码的执行环境，实现跨平台；JIT 是 JVM 的一部分，用于编译热点代码。', '2026-04-30 10:46:38', '2026-04-30 10:46:38');
INSERT INTO `question_info` VALUES (6, 1, '以下哪个修饰符可以使变量在同一包和子类中都可见？', NULL, 2, '23', 'protected 允许同包和不同包的子类访问。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (7, 1, 'Java 中的数组是对象吗？', NULL, 1, '25', '数组是动态创建的 Object 子类，具有 length 属性，可以调用 Object 的方法。', '2026-04-30 10:46:38', '2026-04-30 10:46:38');
INSERT INTO `question_info` VALUES (8, 1, '以下哪个集合类是线程安全的？', NULL, 2, '31', 'Vector 是 JDK 1.0 的线程安全集合，所有方法 synchronized；ArrayList 非线程安全。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (9, 1, '下列哪个接口不能用来遍历 Collection？', NULL, 2, '36', 'Iterator、ListIterator、Enumeration 均可遍历集合；Cloneable 是标记接口，用于克隆。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (10, 1, 'String 类被 final 修饰的作用是？', NULL, 2, '38', 'final 禁止继承，保证字符串不可变，从而可以安全地用于常量池和哈希缓存。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (11, 1, '下列哪个异常是受检异常（checked exception）？', NULL, 3, '43', 'IOException 是受检异常，必须处理或声明；RuntimeException 及其子类、Error 是非受检。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (12, 1, '以下哪个不是 Java 的访问控制修饰符？', NULL, 1, '48', 'static 是成员修饰符，不是访问控制修饰符。访问修饰符有 public, protected, private, 默认。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (13, 1, 'super 关键字的用途是？', NULL, 2, '49', 'super 用于访问父类的非私有成员或构造器，但不能访问父类 static 成员（应使用类名）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (14, 1, '以下哪一项是接口中允许定义的？', NULL, 2, '55', 'JDK 8+ 接口允许 default 实现方法和静态方法，以及常量（public static final）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (15, 1, '下列哪个是 Java 中的包装类？', NULL, 1, '59', 'Integer 是 int 的包装类；int 是基本类型，String 不是基本类型的包装，Void 是特殊包装。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (16, 1, '关于 ArrayList 和 LinkedList 的描述，正确的是？', NULL, 3, '63', 'ArrayList 基于数组，随机访问 O(1)；LinkedList 基于链表，插入删除头部 O(1)，随机访问 O(n)。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (17, 1, '以下哪个方法可以把字符串 \"123\" 转换成 int 类型？', NULL, 1, '66', 'Integer.parseInt(\"123\") 返回 int；Integer.valueOf(\"123\") 返回 Integer。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (18, 1, '下列哪个符号用于单行注释？', NULL, 1, '69', '// 单行注释；/* */ 多行注释；/** */ 文档注释。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (19, 1, '以下哪个是 Java 中的逻辑运算符？', NULL, 1, '75', '&& 是逻辑与；& 是按位与，也可用于逻辑但不会短路；|| 是逻辑或。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (20, 1, '以下关于构造函数的说法，正确的是？', NULL, 2, '80', '构造函数名必须与类名相同，没有返回值（void 也不行）。如果未定义，编译器默认提供无参构造。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (21, 1, '以下哪个是抽象类的特点？', NULL, 2, '83', '抽象类可以包含抽象方法和具体方法；抽象类不能实例化；可以有构造器（被子类调用）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (22, 1, '下列哪个关键字用于实现接口？', NULL, 1, '86', 'implements 用于类实现接口；extends 用于继承类或接口扩展接口。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (23, 1, '下面哪个是 Java 中正确创建线程的方法？', NULL, 2, '90', '实现 Runnable 并传入 Thread，或者继承 Thread。直接实例化 Thread 必须重写 run。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (24, 1, '以下哪个集合保证元素的插入顺序？', NULL, 2, '95', 'LinkedList 有序；HashSet 无序；TreeSet 按自然顺序或比较器排序，不是插入顺序。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (25, 1, 'Java 中，以下哪个类用于读取控制台输入？', NULL, 2, '100', 'Scanner 是常用方式；System.in 是输入流，直接使用较麻烦。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (26, 1, '以下哪个异常表示数组索引越界？', NULL, 1, '102', 'ArrayIndexOutOfBoundsException 发生时表明索引为负数或不小于数组长度。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (27, 1, '下面哪种情况可以编译通过？', NULL, 3, '107', 'byte 范围 -128~127，int 赋值给 byte 需要强制转换；float f = 3.14; 会报错，因为 3.14 默认为 double。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (28, 1, '关于 final 关键字的说法错误的是？', NULL, 2, '112', 'final 变量一旦赋值不可改变；final 方法不可重写；final 类不可继承；但 final 修饰的集合对象内容可以修改，只是引用不可变。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (29, 1, '下列哪个是 Java 中的保留字但未被使用？', NULL, 2, '115', 'goto 和 const 是保留字但在 Java 中无意义，不能用作标识符。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (30, 1, '以下哪个选项可以正确比较两个字符串的内容相等？', NULL, 1, '119', 'equals() 比较内容；== 比较引用地址。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (31, 1, '以下哪个是 Java 编译器的输入文件扩展名？', NULL, 1, '121', '.java 是源文件；.class 是字节码文件。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (32, 1, '方法重载（Overloading）要求什么不同？', NULL, 2, '127', '方法名相同，参数列表不同（类型、个数、顺序）；返回值类型和修饰符无关。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (33, 1, '以下哪个是 Java 中的内存区域（运行时数据区）？', NULL, 3, '130', '堆、栈、方法区、程序计数器、本地方法栈。栈区是其中之一，栈帧存放局部变量。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (34, 1, '下列哪个关键字用于处理异常？', NULL, 1, '136', 'try-catch-finally 是异常处理结构；throw 手动抛出异常；throws 声明异常。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (35, 1, '下面哪个是 Java 中合法的数组声明？', NULL, 1, '138', 'int[] arr = new int[5]; 是标准写法；int arr[] = new int[5]; 也合法但不推荐。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (36, 1, '以下哪个方法是 Object 类的方法？', NULL, 1, '142', 'wait() 是 Object 的实例方法，用于线程等待；sleep() 是 Thread 的静态方法。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (37, 1, '以下哪个集合不能存储重复元素？', NULL, 2, '147', 'Set 系列（HashSet、TreeSet）不允许重复；List 允许重复。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (38, 1, 'Java 中的 static 方法不能直接访问什么？', NULL, 2, '151', 'static 方法不能直接访问实例变量或实例方法（需要先创建对象）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (39, 1, '下面哪个是 Java 中同步的关键字？', NULL, 2, '154', 'synchronized 用于同步锁；volatile 保证可见性但不保证原子性。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (40, 1, '以下关于 StringBuffer 和 StringBuilder 的说法正确的是？', NULL, 2, '159', 'StringBuffer 线程安全（方法同步），StringBuilder 非线程安全但性能更高。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (41, 1, '下列哪个接口是 List 接口的实现类？', NULL, 1, '162', 'ArrayList 实现了 List；HashSet 实现 Set；HashMap 实现 Map。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (42, 1, '以下哪个运算符用于取模？', NULL, 1, '165', '% 是取模（余数）；/ 是除法。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (43, 1, '关于 break 和 continue 的说法正确的是？', NULL, 2, '171', 'break 退出整个循环；continue 跳过本次循环进入下一次。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (44, 1, '下面哪个是 Java 中用于抛出异常的关键字？', NULL, 1, '174', 'throw 用于手动抛出异常；throws 用于声明异常。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (45, 1, '以下哪个是泛型的好处？', NULL, 2, '178', '编译时类型安全，避免强制类型转换，提高代码复用。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (46, 1, '下面哪个是 Java 中的原始类型（primitive type）？', NULL, 1, '183', 'char 是基本数据类型；String 是引用类型。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (47, 1, '以下哪个 JVM 垃圾回收算法是分代的？', NULL, 3, '187', '分代收集算法（新生代、老年代）是现代 GC 的基础；标记-清除、复制、标记-整理是具体算法。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (48, 1, 'Java 中，以下哪个修饰符可以使类不能被继承？', NULL, 1, '191', 'final 修饰的类禁止继承；abstract 类必须被继承。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (49, 1, '以下哪个是 lambda 表达式在 Java 中依赖的核心概念？', NULL, 3, '194', 'Lambda 需要函数式接口（只有一个抽象方法的接口）支持。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (50, 1, '下列哪个类是位于 java.util 包中？', NULL, 1, '199', 'ArrayList 在 java.util 中；String 在 java.lang；System 在 java.lang。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (51, 1, '以下哪个方法会释放对象持有的锁？', NULL, 2, '202', 'wait() 会释放锁，并进入等待队列；sleep() 不会释放锁。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (52, 1, '下面哪个是 Java 中的移位运算符？', NULL, 2, '207', '>> 有符号右移；>>> 无符号右移；<< 左移。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (53, 1, '以下哪个接口是标记接口？', NULL, 2, '210', 'Serializable 是标记接口（无方法）；Cloneable 也是标记接口。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (54, 1, '关于 Java 中的自动装箱，以下说法正确的是？', NULL, 2, '214', '自动装箱是将基本类型自动转换为对应的包装类，例如 int -> Integer。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (55, 1, '下列哪个不是 Java 的访问修饰符？', NULL, 1, '220', 'final 不是访问修饰符；public、protected、private 是。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (56, 1, '以下哪个是 Java 中用于创建包的语句？', NULL, 1, '222', 'package 语句必须位于源文件第一行（非注释）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (57, 1, '下面哪个是 ArrayList 默认初始容量？', NULL, 2, '226', 'JDK 1.8 中默认容量为 10，但在第一次添加元素时才会分配。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (58, 1, '以下哪个类可以用来创建具有键值对映射的集合？', NULL, 1, '231', 'HashMap 实现了 Map 接口；HashSet 是 Set。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (59, 1, 'Java 中，以下哪个异常在程序运行时不能被捕获？', NULL, 3, '236', 'Error（如 StackOverflowError）通常表示严重问题，不建议捕获，但理论上可以捕获。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (60, 1, '以下哪个是内部类的正确描述？', NULL, 2, '239', '内部类可以访问外部类的所有成员（包括私有）；静态内部类不能直接访问外部类实例成员。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (61, 1, '下面哪个是 Java 8 引入的新特性？', NULL, 2, '243', 'Lambda 表达式、Stream API、日期时间 API 等。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (62, 1, '以下哪个是正确创建枚举的示例？', NULL, 2, '246', 'enum Color {RED, GREEN} 是标准语法；枚举隐式继承 java.lang.Enum。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (63, 1, '以下哪个注解用于标记方法已过时？', NULL, 2, '250', '@Deprecated 表示不推荐使用，但仍可用。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (64, 1, '关于 try-with-resources 说法正确的是？', NULL, 3, '255', '自动关闭资源，要求资源类实现 AutoCloseable 接口；在 JDK 7+ 支持。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (65, 1, '以下哪个是 Java 中用于创建随机数的类？', NULL, 1, '257', 'java.util.Random 是常用类；Math.random() 返回 double。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (66, 1, '下面哪个选项中，接口允许的内容在 JDK 8 之前？', NULL, 2, '263', '抽象方法和常量（public static final），没有具体方法。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (67, 1, '下列哪个是线程的状态？', NULL, 2, '267', 'BLOCKED 是线程状态之一（还包括 NEW、RUNNABLE、WAITING、TIMED_WAITING、TERMINATED）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (68, 1, '以下哪个是 Java 中 double 类型的字面量？', NULL, 1, '271', '3.14d 或 3.14D 表示 double；3.14 默认也是 double。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (69, 1, '关于 “==” 比较的说法，正确的是？', NULL, 2, '274', '对于基本类型比较值；对于引用类型比较内存地址。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (70, 1, '以下哪个是 Java 中的位运算符？', NULL, 2, '280', '| 是按位或；|| 是逻辑或（短路）。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (71, 1, '下列哪个方法可以将字符串转为 char 数组？', NULL, 1, '281', 'toCharArray() 是 String 类的实例方法', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (72, 1, '下面哪个是 Java 中正确的弱引用类？', NULL, 3, '286', 'WeakReference 是 java.lang.ref 包下的类。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (73, 1, '以下哪个是 JVM 参数用于设置最大堆内存？', NULL, 3, '290', '-Xmx 设置最大堆，例如 -Xmx512m；-Xms 设置初始堆。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (74, 1, '下面哪个是 Callable 和 Runnable 的区别？', NULL, 3, '295', 'Callable 的 call() 方法可以返回值和抛出受检异常；Runnable 的 run() 方法无返回值。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (75, 1, '以下哪个是 Java 中的注解保留策略（RetentionPolicy）？', NULL, 2, '299', 'RUNTIME 表示注解在运行时仍可访问；SOURCE 仅源码保留。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (76, 1, '下列哪个类是抽象类？', NULL, 2, '303', 'InputStream 是抽象类；ArrayList 是具体类。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (77, 1, '以下哪个是 Java 中并发的工具包？', NULL, 2, '308', 'java.util.concurrent 包是并发工具包，包含并发集合、锁等。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (78, 1, '下面哪个是正确的接口继承方式？', NULL, 1, '310', '接口可以使用 extends 继承多个接口，用逗号分隔。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (79, 1, '以下哪个是 Java 中的函数式接口？', NULL, 2, '314', 'Runnable 只有一个抽象方法 run()，因此是函数式接口。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (80, 1, '关于 transient 关键字，正确的是？', NULL, 2, '317', 'transient 修饰的字段不会被序列化。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (81, 1, '以下哪个是 java.lang 包中的类？', NULL, 1, '322', 'Math 在 java.lang 包中，默认导入。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (82, 1, '下面哪个代表字符串不可变？', NULL, 2, '327', 'String 对象一旦创建，其值不可变；StringBuilder 可变。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (83, 1, '下列哪个是正确创建二维数组的方式？', NULL, 2, '330', 'int[][] arr = new int[2][3]; 是标准语法。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (84, 1, '以下哪个异常表示尝试使用 null 对象引用？', NULL, 1, '334', 'NullPointerException 是空指针异常。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (85, 1, '以下哪个是 Java 中的强引用？', NULL, 2, '340', '默认创建的都是强引用，不会被 GC 回收，除非不可达。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (86, 1, '下面哪个是 LinkedList 实现的双向链表特点？', NULL, 2, '342', '支持高效的头尾插入删除，实现了 Deque 接口。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (87, 1, '以下哪个是 Java 中的程序入口点？', NULL, 1, '346', 'main 方法是标准入口；小应用程序（Applet）没有 main。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (88, 1, '下面哪个是正确多态的表现？', NULL, 2, '350', '父类引用指向子类对象，调用子类重写的方法。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (89, 1, '以下哪个方法属于 Thread 类？', NULL, 2, '354', 'start() 用于启动新线程，执行 run()。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (90, 1, '关于静态代码块，说法正确的是？', NULL, 2, '358', '类加载时执行一次，可用于初始化静态变量。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (91, 1, '下面哪个是 Java 中的字节流输入类？', NULL, 2, '362', 'FileInputStream 是字节输入流；FileReader 是字符流。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (92, 1, '以下哪个注解会抑制编译器警告？', NULL, 2, '366', '@SuppressWarnings 用于关闭特定警告。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (93, 1, '下列哪个是 Java 中正确的 long 类型赋值？', NULL, 1, '370', 'long num = 100L; 必须加 L 表示 long 字面量。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (94, 1, '关于方法重写（Override）的要求，错误的是？', NULL, 2, '375', '重写方法的访问权限不能比父类更严格，但可以更宽松。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (95, 1, '以下哪个是 Java 中的死锁条件？', NULL, 3, '380', '循环等待是死锁必要条件之一。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (96, 1, '下面哪个是 Optional 类的作用？', NULL, 3, '382', 'Optional 用于避免 NullPointerException，但不是完全替代 null。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (97, 1, '下列哪个是 Java 模块系统（JPMS）引入的版本？', NULL, 3, '386', 'Java 9 引入模块系统，使用 module-info.java。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (98, 1, '以下哪个是 Stream API 的终端操作？', NULL, 2, '391', 'collect 是终端操作，会触发流的执行；filter 是中间操作。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (99, 1, '关于 Java 中的记录（record），说法正确的是？', NULL, 3, '393', 'record 是 Java 16 正式发布，用于不可变数据载体的类。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (100, 1, '下列哪个是 JVM 中执行字节码的组件？', NULL, 3, '398', '执行引擎负责解释或即时编译（JIT）执行字节码。', '2026-04-30 10:46:38', '2026-04-30 11:28:47');
INSERT INTO `question_info` VALUES (101, 2, '以下哪些是 Java 中的访问修饰符？', NULL, 1, '401,402,403', 'public、private、protected 是访问修饰符；static 不是。', '2026-04-30 10:51:49', '2026-04-30 10:51:49');
INSERT INTO `question_info` VALUES (102, 2, '以下哪些是 Java 的基本数据类型？', NULL, 1, '406,407,408,409', 'boolean、char、byte、short、int、long、float、double 是基本类型；String 是引用类型。', '2026-04-30 10:51:49', '2026-04-30 10:51:49');
INSERT INTO `question_info` VALUES (103, 2, '以下哪些集合类是线程安全的？', NULL, 2, '413,414', 'Vector 和 Hashtable 是 JDK 1.0 的线程安全类；ConcurrentHashMap 也是线程安全的，但这里为了考察经典，选 Vector 和 Hashtable。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (104, 2, '以下哪些是 Java 中的引用类型？', NULL, 2, '416,417,418,419', '类、接口、数组、枚举、注解都是引用类型。int 是基本类型。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (105, 2, '以下哪些是方法重载的正确形式（与已有的 void print(int a) 构成重载）？', NULL, 2, '420,423,424', '重载要求方法名相同，参数列表不同（类型、顺序、个数）。返回值类型不同不构成重载。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (106, 2, '以下哪些是 Object 类中的方法？', NULL, 2, '425,426,427,428', 'equals()、hashCode()、toString()、wait()、notify()、getClass() 等都是 Object 的方法。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (107, 2, '以下哪些是 Java 中的异常处理关键字？', NULL, 1, '430,431,432,433,434', 'try、catch、finally、throw、throws 都是异常处理相关。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (108, 2, '以下哪些是 Java 8 引入的新特性？', NULL, 2, '435,436,437,439', 'Lambda 表达式、Stream API、新的日期时间 API（java.time）、接口默认方法等。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (109, 2, '以下哪些是 final 关键字的作用？', NULL, 2, '440,441,442', 'final 可以修饰类（不可继承）、方法（不可重写）、变量（不可重新赋值）。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (110, 2, '以下哪些是 Java 中的循环结构？', NULL, 1, '446,447,448', 'for、while、do-while 是循环结构；if-else 是分支结构。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (111, 2, '以下哪些是运行时异常（unchecked exception）？', NULL, 2, '452,453,454', 'NullPointerException、ArrayIndexOutOfBoundsException、ArithmeticException 等都是 RuntimeException 的子类。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (112, 2, '以下哪些是 Java 中用于实现多线程的方式？', NULL, 2, '455,456', '继承 Thread 类或实现 Runnable 接口。Callable 需要配合 Future 使用，也算是，但基本方式以 Thread 和 Runnable 为准。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (113, 2, '以下哪些是 List 接口的实现类？', NULL, 2, '460,461,463', 'ArrayList、LinkedList、Vector 都是 List 的实现。HashSet 是 Set。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (114, 2, '以下哪些是 Java 中的比较器接口？', NULL, 2, '465,466', 'Comparable（内比较器）和 Comparator（外比较器）都是。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (115, 2, '以下哪些是 Stream API 的中间操作？', NULL, 2, '470,471,472', 'filter、map、sorted 是中间操作（返回 Stream）；collect 是终端操作。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (116, 2, '以下哪些是 JVM 内存区域（运行时数据区）？', NULL, 2, '475,476,477,478', '堆、栈、方法区、程序计数器、本地方法栈。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (117, 2, '以下哪些是 Java 中的注解？', NULL, 2, '480,481,482', '@Override、@Deprecated、@SuppressWarnings 是内置注解。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (118, 2, '以下哪些是 Java 中用于同步的关键字或机制？', NULL, 2, '485,486', 'synchronized 和 volatile 都用于并发控制（volatile 保证可见性）。Lock 是接口，不是关键字。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (119, 2, '以下哪些是线程的状态？', NULL, 2, '490,491,492,493', 'NEW、RUNNABLE、BLOCKED、WAITING、TIMED_WAITING、TERMINATED。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (120, 2, '以下哪些是 Java 中用于处理输入输出的流基类（抽象类）？', NULL, 2, '495,496,497,498', 'InputStream、OutputStream、Reader、Writer 是四个抽象基类。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (121, 2, '以下哪些是 Java 中实现多态的必要条件？', NULL, 2, '500,501,502', '继承、重写、父类引用指向子类对象。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (122, 2, '以下哪些是 Java 中的包装类？', NULL, 2, '505,506,507,509', 'Integer、Double、Boolean 都是基本类型的包装类。String 不是包装类。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (123, 2, '以下哪些是 Set 接口的特点？', NULL, 2, '511,513', '元素无序（HashSet）、不可重复。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (124, 2, '以下哪些是 Java 中的函数式接口？', NULL, 2, '515,516,517', 'Runnable、Comparator、Consumer 等都只有一个抽象方法。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (125, 2, '以下哪些是 Java 中的访问权限从大到小？', NULL, 2, '520,521,522,523', 'public > protected > default > private。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (126, 2, '以下哪些是 Java 中创建字符串的方式？', NULL, 2, '525,526,527,528', '直接双引号、new String()、StringBuilder.toString() 等。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (127, 2, '以下哪些是 Java 中垃圾回收相关的算法？', NULL, 2, '530,531,532,534', '标记-清除、复制、标记-整理、分代收集。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (128, 2, '以下哪些是 Java 中的保留字？', NULL, 2, '535,536', 'goto 和 const 是保留字但未使用。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (129, 2, '以下哪些是 Java 中用于定义常量的方式？', NULL, 2, '540,541,542', 'static final 变量、枚举。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (130, 2, '以下哪些是 Java 中常见的类加载器？', NULL, 2, '545,546,547,548', '引导类加载器、扩展类加载器、应用类加载器。', '2026-04-30 10:51:49', '2026-04-30 11:28:55');
INSERT INTO `question_info` VALUES (131, 3, 'Java 是纯面向对象的编程语言。', NULL, 1, 'F', 'Java 有基本数据类型（int, double等），不是“纯”面向对象，但可以说“面向对象”。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (132, 3, 'Java 中的数组是对象。', NULL, 1, 'T', '数组是动态创建的对象，具有 length 属性，可调用 Object 的方法。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (133, 3, '在 Java 中，一个类可以实现多个接口。', NULL, 1, 'T', 'Java 支持多接口实现，弥补单继承的局限。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (134, 3, '静态方法可以直接访问实例变量。', NULL, 1, 'F', '静态方法属于类，不能直接访问实例变量，需要先创建对象。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (135, 3, 'final 修饰的类的对象不可修改其引用。', NULL, 2, 'F', 'final 类不可被继承，但对象引用仍可指向其他对象（除非引用本身是 final）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (136, 3, '抽象类不能有构造方法。', NULL, 2, 'F', '抽象类可以有构造方法，用于子类实例化时调用。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (137, 3, 'Java 中，字符串拼接使用 + 运算符效率较低，推荐使用 StringBuilder。', NULL, 2, 'T', '循环中大量使用 + 会创建多个中间 String 对象，StringBuilder 更高效。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (138, 3, 'try 块后面必须跟 catch 块。', NULL, 1, 'F', 'try 可以单独跟 finally 块，不需要 catch。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (139, 3, 'HashMap 允许 key 为 null。', NULL, 1, 'T', 'HashMap 允许一个 key 为 null，多个 value 为 null。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (140, 3, 'Java 中的泛型会在编译后擦除类型信息。', NULL, 2, 'T', 'Java 泛型是编译时检查，运行时会擦除（类型擦除）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (141, 3, '接口中的变量默认是 public static final 的。', NULL, 2, 'T', '接口中定义的变量隐式具有 public static final 修饰。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (142, 3, '重载（Overloading）发生在父子类之间。', NULL, 1, 'F', '重载发生在同一个类中，重写发生在父子类中。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (143, 3, 'continue 语句可以用于 switch 语句中。', NULL, 2, 'F', 'continue 只能用于循环（for, while, do-while），不能用于 switch。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (144, 3, 'Java 中，char 类型占用 2 个字节。', NULL, 1, 'T', 'char 使用 Unicode，占 16 位（2 字节）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (145, 3, 'LinkedList 的随机访问性能优于 ArrayList。', NULL, 1, 'F', 'ArrayList 基于数组，随机访问 O(1)；LinkedList 基于链表，随机访问 O(n)。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (146, 3, '被 private 修饰的成员变量只能在本类中被访问。', NULL, 1, 'T', 'private 是最小访问权限，仅限于本类。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (147, 3, 'Java 中，异常处理时 finally 块一定会执行（即使 try 中有 return）。', NULL, 2, 'T', 'finally 块在 try 或 catch 块执行 return 之前执行，除非 JVM 退出。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (148, 3, 'volatile 关键字可以保证原子性。', NULL, 2, 'F', 'volatile 保证可见性和有序性，但不保证复合操作的原子性（如 i++）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (149, 3, 'String 类是不可变的，所以 String 对象一旦创建内容不能改变。', NULL, 1, 'T', 'String 内部使用 final char[]，任何修改都会创建新对象。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (150, 3, 'Java 中的包（package）主要用于解决类名冲突和访问控制。', NULL, 1, 'T', '包可以组织类，避免命名冲突，并提供访问保护。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (151, 3, 'switch 语句支持 String 类型（JDK 7+）。', NULL, 1, 'T', 'JDK 7 开始 switch 支持 String 类型。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (152, 3, '一个 Java 源文件中可以有多个 public 类。', NULL, 1, 'F', '一个 .java 文件最多只能有一个 public 类，且与文件名相同。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (153, 3, 'HashSet 底层是基于 HashMap 实现的。', NULL, 2, 'T', 'HashSet 内部使用 HashMap 存储元素，元素作为 key，value 为固定对象。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (154, 3, 'Java 中，所有异常都必须被捕获或声明抛出。', NULL, 2, 'F', 'RuntimeException 和 Error 是非受检异常，可以不捕获。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (155, 3, '类的静态代码块在创建第一个对象时执行。', NULL, 2, 'F', '静态代码块在类加载时执行一次，与是否创建对象无关。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (156, 3, 'super() 调用父类构造器的语句必须是子类构造器的第一行。', NULL, 2, 'T', 'super() 或 this() 必须在构造器第一行。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (157, 3, 'Java 中的方法参数传递是引用传递。', NULL, 1, 'F', 'Java 只有值传递，参数传递时传递的是值的副本（引用类型的值是地址）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (158, 3, 'ArrayList 和 Vector 都是线程安全的。', NULL, 1, 'F', 'ArrayList 非线程安全，Vector 是线程安全的。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (159, 3, 'Java 中的枚举类型可以定义构造方法和字段。', NULL, 2, 'T', '枚举可以定义构造方法、字段和方法，构造方法默认 private。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (160, 3, '可以使用 instanceof 运算符判断对象是否为某个类的实例。', NULL, 1, 'T', 'instanceof 用于判断对象与类（或接口）的关系。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (161, 3, 'Java 中，float f = 3.14; 是合法的赋值。', NULL, 1, 'F', '3.14 默认为 double，需要加 f/F 后缀，或强制转换。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (162, 3, 'break 语句可以用于退出多层嵌套循环（带标签的 break）。', NULL, 2, 'T', '使用标签（label）可以跳出多层循环。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (163, 3, 'Java 中，子类构造器默认会调用父类的无参构造器（如果存在）。', NULL, 2, 'T', '子类构造器第一行隐式 super() 调用父类无参构造。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (164, 3, 'synchronized 关键字可以修饰类和接口。', NULL, 2, 'F', 'synchronized 不能修饰类或接口，可以修饰方法和代码块。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (165, 3, 'Java 中的反射会影响封装性，但提供了动态性。', NULL, 2, 'T', '反射可以访问私有成员，破坏封装，但常用于框架开发。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (166, 3, 'Stream API 的中间操作会立即执行。', NULL, 2, 'F', '中间操作是惰性的，只有在终端操作时才会执行。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (167, 3, 'Java 中，一个类可以实现多个接口，但只能继承一个类。', NULL, 1, 'T', '单继承多实现是 Java 的基本特性。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (168, 3, 'length() 方法是数组的属性。', NULL, 1, 'F', '数组的 length 是属性（无括号），String 的 length() 是方法。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (169, 3, 'Java 中的 JIT 编译器可以将热点字节码编译为本地机器码提高性能。', NULL, 2, 'T', 'JIT（Just-In-Time）编译优化频繁执行的方法。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (170, 3, '抽象方法不能有方法体。', NULL, 1, 'T', '抽象方法只有声明，没有实现（花括号）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (171, 3, 'Java 中的 wait() 和 notify() 方法必须在 synchronized 块中调用。', NULL, 2, 'T', '否则会抛出 IllegalMonitorStateException。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (172, 3, 'int 和 Integer 可以自动互转，称为自动装箱和拆箱。', NULL, 1, 'T', 'JDK 5 引入自动装箱/拆箱。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (173, 3, 'finally 块中如果抛出异常，会覆盖 try 块中抛出的异常。', NULL, 3, 'T', 'finally 中抛出的异常会传播，原来的异常丢失（除非使用 addSuppressed）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (174, 3, 'Java 中，静态内部类可以访问外部类的实例成员。', NULL, 2, 'F', '静态内部类不能直接访问外部类的非静态成员，需要创建外部类对象。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (175, 3, 'Java 中的泛型可以用于基本类型，如 List<int> 是合法的。', NULL, 1, 'F', '泛型只能使用引用类型，需要使用包装类如 List<Integer>。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (176, 3, '== 运算符比较两个字符串的内容是否相等。', NULL, 1, 'F', '== 比较引用地址，equals() 比较内容。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (177, 3, 'Java 中的枚举常量是唯一实例，可以通过 values() 方法遍历。', NULL, 2, 'T', '枚举隐式提供了 values() 方法返回所有枚举常量数组。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (178, 3, '当一个线程进入对象的 synchronized 方法，其他线程也可以进入该对象的其他非 synchronized 方法。', NULL, 2, 'T', 'synchronized 只阻塞同步方法/块，非同步方法仍可并发访问。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (179, 3, 'Java 中的 char 类型可以存储中文字符。', NULL, 1, 'T', 'char 使用 Unicode，支持中文。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (180, 3, 'Java 中的静态方法可以被重写。', NULL, 1, 'F', '静态方法属于类，不存在重写（可以隐藏）。', '2026-04-30 11:04:29', '2026-04-30 11:05:24');
INSERT INTO `question_info` VALUES (181, 4, '请简述 Java 中的“一次编写，到处运行”是如何实现的。', NULL, 2, 'Java 源代码编译成字节码（.class 文件），字节码运行在 Java 虚拟机（JVM）上，不同平台有不同 JVM 实现，但所有 JVM 都能执行相同的字节码。', '核心：JVM 和字节码。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (182, 4, '写出 Java 中 8 种基本数据类型及其占用的字节数。', NULL, 1, 'byte:1, short:2, int:4, long:8, float:4, double:8, char:2, boolean:1（实际大小依赖虚拟机，通常逻辑上为1位）', '记忆类型和大小。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (183, 4, '什么是构造方法？它有哪些特点？', NULL, 1, '构造方法是一种特殊的方法，与类同名，没有返回值类型。用于初始化对象。特点：1. 方法名与类名相同；2. 无返回值（void 也不行）；3. 创建对象时自动调用；4. 可以重载；5. 如果没有定义，编译器会提供默认无参构造。', '考察构造方法基本概念。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (184, 4, '请写出三种 Java 中注释的语法，并说明其用途。', NULL, 1, '1. 单行注释：// 注释内容；2. 多行注释：/* 注释内容 */；3. 文档注释：/** 注释内容 */，可用 javadoc 生成 API 文档。', '基础语法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (185, 4, '请说明 `==` 和 `equals()` 的区别。', NULL, 2, '对于基本类型，`==` 比较值；对于引用类型，`==` 比较内存地址。`equals()` 是 Object 类的方法，默认行为同 `==`，但子类可重写（如 String）来比较内容。', '面试常考。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (186, 4, '请写出代码：使用 for 循环计算 1 到 100 的和，并输出结果。', NULL, 2, 'public class Sum { public static void main(String[] args) { int sum = 0; for (int i = 1; i <= 100; i++) { sum += i; } System.out.println(sum); } }', '考察循环和累加。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (187, 4, '什么是方法重载？请举例说明。', NULL, 2, '重载指同一个类中多个方法名相同但参数列表不同（类型、个数、顺序）。例如：void print(int a) 和 void print(String s)，以及 void print(int a, int b)。', '重载的定义和示例。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (188, 4, '什么是方法重写？重写时需要注意哪些规则？', NULL, 2, '重写是子类重新定义父类中已有的方法。规则：方法名、参数列表、返回值类型必须相同；访问权限不能更严格；不能抛出比父类更宽泛的异常；final 方法不能重写；static 方法不能重写。', '重写规则。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (189, 4, '请写出代码：定义一个 Person 类，包含 name 和 age 属性，构造方法，以及 getter/setter 方法，并创建对象输出。', NULL, 2, 'public class Person { private String name; private int age; public Person(String name, int age){this.name=name; this.age=age;} public String getName(){return name;} public void setName(String name){this.name=name;} public int getAge(){return age;} public void setAge(int age){this.age=age;} }  // 使用：Person p = new Person(\"张三\",20); System.out.println(p.getName());', '考察类的基本定义和封装。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (190, 4, '请简述 static 关键字的作用，并举例说明。', NULL, 2, 'static 修饰成员变量和方法，属于类而非实例。静态变量被所有实例共享，静态方法可以直接通过类名调用，不能直接访问实例成员。例如：public static int count = 0; public static void printCount(){System.out.println(count);}', '静态概念。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (191, 4, '什么是数组？请写出创建一维数组的三种方式。', NULL, 2, '数组是存储相同类型元素的容器。三种方式：1. int[] arr = new int[5]; 2. int[] arr = new int[]{1,2,3}; 3. int[] arr = {1,2,3};', '数组创建语法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (192, 4, '请写出代码：使用冒泡排序对一个 int 数组进行升序排序。', NULL, 3, 'public static void bubbleSort(int[] arr) { int n = arr.length; for (int i = 0; i < n-1; i++) { for (int j = 0; j < n-1-i; j++) { if (arr[j] > arr[j+1]) { int temp = arr[j]; arr[j] = arr[j+1]; arr[j+1] = temp; } } } }', '冒泡排序算法实现。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (193, 4, '请简述 Java 中的访问修饰符（public, protected, default, private）的访问范围。', NULL, 2, 'public：任何地方；protected：同包+子类；default（无修饰符）：同包；private：仅本类。', '访问控制表。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (194, 4, '什么是继承？请写出一个简单的继承示例。', NULL, 2, '继承是子类复用父类属性和行为的机制，使用 extends 关键字。示例：class Animal { void eat(){...} } class Dog extends Animal { void bark(){...} }', '继承基本概念。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (195, 4, '什么是多态？请写出一个多态的代码示例。', NULL, 3, '多态指同一类型的引用指向不同对象，调用相同方法产生不同行为。示例：Animal a = new Dog(); a.eat(); // 如果 Dog 重写了 eat()，则调用 Dog 的 eat。', '多态示例。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (196, 4, '请写出代码：定义一个抽象类 Shape，包含抽象方法 area()；然后定义子类 Circle 和 Rectangle 实现 area() 方法。', NULL, 3, 'abstract class Shape { public abstract double area(); } class Circle extends Shape { private double r; public Circle(double r){this.r=r;} public double area(){return Math.PI*r*r;} } class Rectangle extends Shape { private double w,h; public Rectangle(double w,double h){this.w=w;this.h=h;} public double area(){return w*h;} }', '抽象类与实现。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (197, 4, '什么是接口？接口和抽象类的区别是什么？', NULL, 3, '接口是一种完全抽象的规范（JDK8后可包含 default/static 方法）。区别：接口多实现，抽象类单继承；接口方法默认 public abstract，抽象类可以有具体方法；接口变量默认 public static final；抽象类可以有构造器。', '接口与抽象类对比。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (198, 4, '请写出代码：定义一个接口 Playable，包含方法 play()；然后创建实现类 Piano 和 Guitar，并调用。', NULL, 3, 'interface Playable { void play(); } class Piano implements Playable { public void play(){System.out.println(\"弹钢琴\");} } class Guitar implements Playable { public void play(){System.out.println(\"弹吉他\");} } // 调用：Playable p = new Piano(); p.play();', '接口实现示例。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (199, 4, '简述 Java 中的异常体系，并说明受检异常和非受检异常的区别。', NULL, 2, '所有异常和错误继承自 Throwable。Exception 分为受检异常（编译时异常，必须处理）和运行时异常（RuntimeException，无需显式处理）。Error 属于严重错误。', '异常分类。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (200, 4, '请写出代码：使用 try-catch-finally 处理可能发生的算术异常（除零）。', NULL, 2, 'public class ExceptionDemo { public static void main(String[] args) { try { int a = 10/0; } catch (ArithmeticException e) { System.out.println(\"除数不能为0\"); } finally { System.out.println(\"finally 始终执行\"); } } }', '异常处理基本语法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (201, 4, '什么是 throws 和 throw？请说明区别。', NULL, 2, 'throws 用于方法声明，表示该方法可能抛出的异常类型，由调用者处理；throw 用于手动抛出异常对象，后面跟异常实例。', 'throws vs throw。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (202, 4, '请写出代码：自定义一个检查异常（CheckedException）和一个运行时异常（UncheckedException），并在方法中抛出。', NULL, 3, '// 自定义受检异常：class MyCheckedException extends Exception { } // 自定义运行时异常：class MyRuntimeException extends RuntimeException { } // 使用：public void test() throws MyCheckedException { throw new MyCheckedException(); } public void test2() { throw new MyRuntimeException(); }', '自定义异常。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (203, 4, '什么是包装类？Java 中提供了哪些基本类型的包装类？写出 int 和 Integer 之间的转换代码。', NULL, 2, '包装类将基本类型包装为对象。对应关系：byte-Byte, short-Short, int-Integer, long-Long, float-Float, double-Double, char-Character, boolean-Boolean。装箱：Integer i = Integer.valueOf(10); 或自动装箱；拆箱：int a = i.intValue(); 或自动拆箱。', '包装类概念。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (204, 4, '请写出代码：使用 ArrayList 存储 5 个学生的名字，然后遍历输出。', NULL, 2, 'import java.util.*; public class ListDemo { public static void main(String[] args) { List<String> names = new ArrayList<>(); names.add(\"张三\"); names.add(\"李四\"); names.add(\"王五\"); names.add(\"赵六\"); names.add(\"小明\"); for(String name : names) { System.out.println(name); } } }', '集合基本使用。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (205, 4, '请简述 ArrayList 和 LinkedList 的区别。', NULL, 2, 'ArrayList 基于动态数组，随机访问快（O(1)），插入删除慢（O(n)）；LinkedList 基于双向链表，随机访问慢（O(n)），头尾插入删除快（O(1)），实现了 Deque 接口。', '两种 List 对比。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (206, 4, '请写出代码：使用 HashMap 存储学生 ID 和姓名（ID 为 Integer，姓名为 String），添加 3 个键值对，然后根据 ID 取出姓名。', NULL, 2, 'Map<Integer,String> map = new HashMap<>(); map.put(1,\"张三\"); map.put(2,\"李四\"); map.put(3,\"王五\"); String name = map.get(2); System.out.println(name);', 'Map 基本操作。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (207, 4, '什么是泛型？有什么好处？写出一个泛型类的简单示例。', NULL, 2, '泛型是 JDK 5 引入的类型参数化机制，提高类型安全，消除强制转换。示例：class Box<T> { private T t; public void set(T t){this.t=t;} public T get(){return t;} } Box<String> box = new Box<>();', '泛型概念与示例。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (208, 4, '请写出代码：使用 Collections 工具类对 List<Integer> 进行排序和反转。', NULL, 2, 'List<Integer> list = new ArrayList<>(Arrays.asList(3,1,4,1,5)); Collections.sort(list); // 升序 System.out.println(list); Collections.reverse(list); // 反转 System.out.println(list);', 'Collections 用法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (209, 4, '简述 Java 的内存区域（运行时数据区）。', NULL, 3, '线程共享：堆（存放对象实例）、方法区（类结构、常量池、静态变量等）；线程私有：虚拟机栈（栈帧存放局部变量、操作数栈等）、本地方法栈、程序计数器（指向当前线程执行的字节码行号）。', 'JVM 内存模型。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (210, 4, '请写出代码：创建一个线程，通过实现 Runnable 接口，并在 run 方法中打印 1~5 的数字。', NULL, 2, 'public class MyRunnable implements Runnable { public void run() { for(int i=1;i<=5;i++){ System.out.println(i); } } public static void main(String[] args) { Thread t = new Thread(new MyRunnable()); t.start(); } }', '实现 Runnable 创建线程。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (211, 4, '请写出代码：使用继承 Thread 类的方式创建线程，并打印当前线程名称。', NULL, 2, 'class MyThread extends Thread { public void run() { System.out.println(Thread.currentThread().getName()); } } public class Test { public static void main(String[] args) { MyThread t = new MyThread(); t.start(); } }', '继承 Thread 方式。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (212, 4, '什么是线程同步？synchronized 关键字的用法有哪些？', NULL, 3, '线程同步用于解决多线程访问共享数据时的安全问题。synchronized 用法：1. 修饰实例方法（锁当前实例）；2. 修饰静态方法（锁 Class 对象）；3. 同步代码块，指定锁对象。', '同步概念和 synchronized 用法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (213, 4, '请写出代码：使用 synchronized 代码块保证两个线程对共享变量 count 的累加操作安全（累加 1000 次）。', NULL, 3, 'public class SyncDemo { private static int count = 0; private static final Object lock = new Object(); public static void main(String[] args) throws InterruptedException { Thread t1 = new Thread(() -> { for(int i=0;i<1000;i++){ synchronized(lock){ count++; } } }); Thread t2 = new Thread(() -> { for(int i=0;i<1000;i++){ synchronized(lock){ count++; } } }); t1.start(); t2.start(); t1.join(); t2.join(); System.out.println(count); // 期望 2000 } }', '同步代码块示例。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (214, 4, '什么是 String、StringBuilder、StringBuffer？区别是什么？', NULL, 2, 'String 不可变，任何修改都会产生新对象；StringBuilder 可变，线程不安全，效率高；StringBuffer 可变，线程安全（方法同步），效率较低。', '字符串类对比。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (215, 4, '请写出代码：将一个字符串 \"hello world\" 转换为大写，然后反转输出。', NULL, 2, 'String str = \"hello world\"; String upper = str.toUpperCase(); String reversed = new StringBuilder(upper).reverse().toString(); System.out.println(reversed);', '字符串操作链式调用。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (216, 4, '什么是 Java 的反射机制？有什么应用场景？', NULL, 3, '反射允许程序在运行时获取类的信息（方法、字段等）并动态调用。应用场景：框架开发（Spring）、动态代理、类加载器、IDE 自动补全等。', '反射概念与用途。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (217, 4, '请写出代码：使用反射获取 String 类的所有 public 方法并输出方法名。', NULL, 3, 'import java.lang.reflect.*; public class ReflectDemo { public static void main(String[] args) { Method[] methods = String.class.getMethods(); for(Method m : methods) { System.out.println(m.getName()); } } }', '反射简单示例。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (218, 4, '什么是 Lambda 表达式？写出使用 Lambda 对 List<Integer> 进行遍历的代码。', NULL, 2, 'Lambda 是匿名函数，可简化函数式接口的实现。示例：List<Integer> list = Arrays.asList(1,2,3); list.forEach(n -> System.out.println(n));', 'Lambda 基本用法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (219, 4, '请写出代码：使用 Stream API 筛选出 List<String> 中长度大于 2 的字符串，并转换为大写后收集到新列表。', NULL, 3, 'List<String> list = Arrays.asList(\"a\",\"ab\",\"abc\",\"abcd\"); List<String> result = list.stream().filter(s -> s.length() > 2).map(String::toUpperCase).collect(Collectors.toList()); System.out.println(result);', 'Stream 中间操作和终端操作。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (220, 4, '什么是 Optional 类？写出代码使用 Optional 避免空指针异常。', NULL, 3, 'Optional 是容器对象，用于更优雅地处理 null。示例：String s = null; Optional<String> opt = Optional.ofNullable(s); String result = opt.orElse(\"默认值\"); System.out.println(result);', 'Optional 用法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (221, 4, '请写出代码：使用 java.time 包获取当前日期时间，并格式化为 \"yyyy-MM-dd HH:mm:ss\"。', NULL, 2, 'import java.time.*; import java.time.format.*; LocalDateTime now = LocalDateTime.now(); DateTimeFormatter formatter = DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\"); String formatted = now.format(formatter); System.out.println(formatted);', '新日期时间 API。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (222, 4, '什么是枚举？请定义表示星期几的枚举，并遍历输出。', NULL, 2, 'enum Weekday { MON, TUE, WED, THU, FRI, SAT, SUN } // 遍历：for(Weekday w : Weekday.values()) { System.out.println(w); }', '枚举基础。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (223, 4, '请写出代码：使用 BufferedReader 读取文件 \"test.txt\" 的内容并逐行输出。', NULL, 3, 'try(BufferedReader br = new BufferedReader(new FileReader(\"test.txt\"))) { String line; while((line = br.readLine()) != null) { System.out.println(line); } } catch(IOException e){ e.printStackTrace(); }', '字符流读取文件，try-with-resources。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (224, 4, '请简述 Java 中 final、finally、finalize 的区别。', NULL, 2, 'final 修饰类、方法、变量；finally 是异常处理块，保证代码执行；finalize 是 Object 的方法，垃圾回收前调用（已弃用）。', '三者的区别。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (225, 4, '请写出代码：创建单例模式（饿汉式）和（懒汉式，线程安全）。', NULL, 3, '// 饿汉式：public class Singleton { private static final Singleton instance = new Singleton(); private Singleton(){} public static Singleton getInstance(){ return instance; } } // 懒汉式线程安全（双重检查锁）：public class Singleton { private volatile static Singleton instance; private Singleton(){} public static Singleton getInstance(){ if(instance==null){ synchronized(Singleton.class){ if(instance==null){ instance = new Singleton(); } } } return instance; } }', '单例模式两种写法。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (226, 4, '什么是死锁？写出一个简单的死锁代码示例。', NULL, 3, '死锁是指两个或以上线程互相持有对方需要的锁导致无限阻塞。示例：Object lock1 = new Object(); Object lock2 = new Object(); new Thread(()->{ synchronized(lock1){ try{Thread.sleep(100);}catch(Exception e){} synchronized(lock2){ } } }).start(); new Thread(()->{ synchronized(lock2){ synchronized(lock1){ } } }).start();', '死锁演示。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (227, 4, '请写出代码：使用 PriorityQueue 实现一个小顶堆，并添加若干整数，然后依次弹出最小值。', NULL, 3, 'Queue<Integer> pq = new PriorityQueue<>(); pq.offer(5); pq.offer(2); pq.offer(8); pq.offer(1); while(!pq.isEmpty()){ System.out.println(pq.poll()); } // 输出 1,2,5,8', '优先队列使用。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (228, 4, '什么是注解？请定义一个名为 @MyAnnotation 的注解，包含 String value() 属性，并设置默认值。', NULL, 2, '@interface MyAnnotation { String value() default \"default\"; } 使用：@MyAnnotation(\"test\")', '自定义注解。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (229, 4, '请写出代码：使用 JDBC 连接 MySQL 数据库，查询 user 表中的所有记录，并打印结果。', NULL, 4, 'Class.forName(\"com.mysql.cj.jdbc.Driver\"); try(Connection conn = DriverManager.getConnection(\"jdbc:mysql://localhost:3306/test\",\"root\",\"password\"); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(\"SELECT * FROM user\")) { while(rs.next()){ System.out.println(rs.getString(\"name\")); } } catch(Exception e){ e.printStackTrace(); }', 'JDBC 连接查询。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');
INSERT INTO `question_info` VALUES (230, 4, '请简述 JVM 垃圾回收中可达性分析算法的基本原理。', NULL, 3, '从 GC Roots 对象（栈帧中引用、静态变量引用等）出发，通过引用链向下搜索，不可达的对象被判定为可回收。GC Roots 之间不可达的对象也会被回收。', '可达性分析。', '2026-04-30 11:10:26', '2026-04-30 11:10:26');

-- ----------------------------
-- Table structure for question_option
-- ----------------------------
DROP TABLE IF EXISTS `question_option`;
CREATE TABLE `question_option`  (
  `option_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID，对应exercise_question.question_id',
  `option_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选项内容',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值',
  PRIMARY KEY (`option_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 550 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '习题选项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question_option
-- ----------------------------
INSERT INTO `question_option` VALUES (1, 1, '编译型语言', 1);
INSERT INTO `question_option` VALUES (2, 1, '解释型语言', 2);
INSERT INTO `question_option` VALUES (3, 1, '编译解释混合型', 3);
INSERT INTO `question_option` VALUES (4, 1, '脚本语言', 4);
INSERT INTO `question_option` VALUES (5, 2, 'public static int main(String[] args)', 1);
INSERT INTO `question_option` VALUES (6, 2, 'public void main(String[] args)', 2);
INSERT INTO `question_option` VALUES (7, 2, 'public static void main(String[] args)', 3);
INSERT INTO `question_option` VALUES (8, 2, 'public static void main(String args)', 4);
INSERT INTO `question_option` VALUES (9, 3, 'goto', 1);
INSERT INTO `question_option` VALUES (10, 3, 'const', 2);
INSERT INTO `question_option` VALUES (11, 3, 'include', 3);
INSERT INTO `question_option` VALUES (12, 3, 'assert', 4);
INSERT INTO `question_option` VALUES (13, 4, 'boolean', 1);
INSERT INTO `question_option` VALUES (14, 4, 'byte', 2);
INSERT INTO `question_option` VALUES (15, 4, 'char', 3);
INSERT INTO `question_option` VALUES (16, 4, 'short', 4);
INSERT INTO `question_option` VALUES (17, 5, 'JVM 是硬件设备', 1);
INSERT INTO `question_option` VALUES (18, 5, 'JVM 执行字节码', 2);
INSERT INTO `question_option` VALUES (19, 5, 'JVM 只运行 Java 程序', 3);
INSERT INTO `question_option` VALUES (20, 5, 'JVM 没有垃圾回收', 4);
INSERT INTO `question_option` VALUES (21, 6, 'private', 1);
INSERT INTO `question_option` VALUES (22, 6, 'default（无修饰符）', 2);
INSERT INTO `question_option` VALUES (23, 6, 'protected', 3);
INSERT INTO `question_option` VALUES (24, 6, 'public', 4);
INSERT INTO `question_option` VALUES (25, 7, '是', 1);
INSERT INTO `question_option` VALUES (26, 7, '否', 2);
INSERT INTO `question_option` VALUES (27, 7, '只有基本类型数组才是对象', 3);
INSERT INTO `question_option` VALUES (28, 7, '数组不是对象', 4);
INSERT INTO `question_option` VALUES (29, 8, 'ArrayList', 1);
INSERT INTO `question_option` VALUES (30, 8, 'LinkedList', 2);
INSERT INTO `question_option` VALUES (31, 8, 'Vector', 3);
INSERT INTO `question_option` VALUES (32, 8, 'HashSet', 4);
INSERT INTO `question_option` VALUES (33, 9, 'Iterator', 1);
INSERT INTO `question_option` VALUES (34, 9, 'ListIterator', 2);
INSERT INTO `question_option` VALUES (35, 9, 'Enumeration', 3);
INSERT INTO `question_option` VALUES (36, 9, 'Cloneable', 4);
INSERT INTO `question_option` VALUES (37, 10, '提高性能', 1);
INSERT INTO `question_option` VALUES (38, 10, '保证安全性和不可变性', 2);
INSERT INTO `question_option` VALUES (39, 10, '方便多线程', 3);
INSERT INTO `question_option` VALUES (40, 10, '允许继承', 4);
INSERT INTO `question_option` VALUES (41, 11, 'NullPointerException', 1);
INSERT INTO `question_option` VALUES (42, 11, 'ArrayIndexOutOfBoundsException', 2);
INSERT INTO `question_option` VALUES (43, 11, 'IOException', 3);
INSERT INTO `question_option` VALUES (44, 11, 'ArithmeticException', 4);
INSERT INTO `question_option` VALUES (45, 12, 'public', 1);
INSERT INTO `question_option` VALUES (46, 12, 'private', 2);
INSERT INTO `question_option` VALUES (47, 12, 'protected', 3);
INSERT INTO `question_option` VALUES (48, 12, 'static', 4);
INSERT INTO `question_option` VALUES (49, 13, '访问父类成员', 1);
INSERT INTO `question_option` VALUES (50, 13, '访问子类成员', 2);
INSERT INTO `question_option` VALUES (51, 13, '调用当前类构造器', 3);
INSERT INTO `question_option` VALUES (52, 13, '访问父类 static 变量', 4);
INSERT INTO `question_option` VALUES (53, 14, '实例字段', 1);
INSERT INTO `question_option` VALUES (54, 14, '构造方法', 2);
INSERT INTO `question_option` VALUES (55, 14, 'default 方法', 3);
INSERT INTO `question_option` VALUES (56, 14, '实例代码块', 4);
INSERT INTO `question_option` VALUES (57, 15, 'int', 1);
INSERT INTO `question_option` VALUES (58, 15, 'String', 2);
INSERT INTO `question_option` VALUES (59, 15, 'Integer', 3);
INSERT INTO `question_option` VALUES (60, 15, 'Void', 4);
INSERT INTO `question_option` VALUES (61, 16, 'ArrayList 插入删除快', 1);
INSERT INTO `question_option` VALUES (62, 16, 'LinkedList 随机访问快', 2);
INSERT INTO `question_option` VALUES (63, 16, 'ArrayList 随机访问 O(1)', 3);
INSERT INTO `question_option` VALUES (64, 16, 'LinkedList 中间插入慢', 4);
INSERT INTO `question_option` VALUES (65, 17, 'Integer.parseInteger(\"123\")', 1);
INSERT INTO `question_option` VALUES (66, 17, 'Integer.parseInt(\"123\")', 2);
INSERT INTO `question_option` VALUES (67, 17, '(int) \"123\"', 3);
INSERT INTO `question_option` VALUES (68, 17, 'String.toInt(\"123\")', 4);
INSERT INTO `question_option` VALUES (69, 18, '//', 1);
INSERT INTO `question_option` VALUES (70, 18, '/*', 2);
INSERT INTO `question_option` VALUES (71, 18, '*/', 3);
INSERT INTO `question_option` VALUES (72, 18, '#', 4);
INSERT INTO `question_option` VALUES (73, 19, '&', 1);
INSERT INTO `question_option` VALUES (74, 19, '|', 2);
INSERT INTO `question_option` VALUES (75, 19, '&&', 3);
INSERT INTO `question_option` VALUES (76, 19, '^', 4);
INSERT INTO `question_option` VALUES (77, 20, '构造函数可以有任何返回值', 1);
INSERT INTO `question_option` VALUES (78, 20, '构造函数名可以和类名不同', 2);
INSERT INTO `question_option` VALUES (79, 20, '构造函数不能是 private', 3);
INSERT INTO `question_option` VALUES (80, 20, '如果没有定义，编译器提供默认无参构造', 4);
INSERT INTO `question_option` VALUES (81, 21, '可以实例化', 1);
INSERT INTO `question_option` VALUES (82, 21, '不能有构造器', 2);
INSERT INTO `question_option` VALUES (83, 21, '可以包含具体方法', 3);
INSERT INTO `question_option` VALUES (84, 21, '不能用 abstract 修饰', 4);
INSERT INTO `question_option` VALUES (85, 22, 'extends', 1);
INSERT INTO `question_option` VALUES (86, 22, 'implements', 2);
INSERT INTO `question_option` VALUES (87, 22, 'import', 3);
INSERT INTO `question_option` VALUES (88, 22, 'package', 4);
INSERT INTO `question_option` VALUES (89, 23, 'new Runnable()', 1);
INSERT INTO `question_option` VALUES (90, 23, 'new Thread(runnable)', 2);
INSERT INTO `question_option` VALUES (91, 23, 'new Thread()', 3);
INSERT INTO `question_option` VALUES (92, 23, 'new Runnable(runnable)', 4);
INSERT INTO `question_option` VALUES (93, 24, 'HashSet', 1);
INSERT INTO `question_option` VALUES (94, 24, 'TreeSet', 2);
INSERT INTO `question_option` VALUES (95, 24, 'LinkedList', 3);
INSERT INTO `question_option` VALUES (96, 24, 'HashMap', 4);
INSERT INTO `question_option` VALUES (97, 25, 'System.in.read()', 1);
INSERT INTO `question_option` VALUES (98, 25, 'Console.read()', 2);
INSERT INTO `question_option` VALUES (99, 25, 'BufferedReader', 3);
INSERT INTO `question_option` VALUES (100, 25, 'Scanner', 4);
INSERT INTO `question_option` VALUES (101, 26, 'NullPointerException', 1);
INSERT INTO `question_option` VALUES (102, 26, 'ArrayIndexOutOfBoundsException', 2);
INSERT INTO `question_option` VALUES (103, 26, 'IllegalArgumentException', 3);
INSERT INTO `question_option` VALUES (104, 26, 'ClassCastException', 4);
INSERT INTO `question_option` VALUES (105, 27, 'byte b = 128;', 1);
INSERT INTO `question_option` VALUES (106, 27, 'float f = 3.14;', 2);
INSERT INTO `question_option` VALUES (107, 27, 'double d = 3.14f;', 3);
INSERT INTO `question_option` VALUES (108, 27, 'char c = \"a\";', 4);
INSERT INTO `question_option` VALUES (109, 28, 'final 修饰的类不能被继承', 1);
INSERT INTO `question_option` VALUES (110, 28, 'final 修饰的方法不能被重写', 2);
INSERT INTO `question_option` VALUES (111, 28, 'final 修饰的变量值不能改变', 3);
INSERT INTO `question_option` VALUES (112, 28, 'final 修饰的对象内容不可变', 4);
INSERT INTO `question_option` VALUES (113, 29, 'null', 1);
INSERT INTO `question_option` VALUES (114, 29, 'true', 2);
INSERT INTO `question_option` VALUES (115, 29, 'goto', 3);
INSERT INTO `question_option` VALUES (116, 29, 'class', 4);
INSERT INTO `question_option` VALUES (117, 30, '==', 1);
INSERT INTO `question_option` VALUES (118, 30, '=', 2);
INSERT INTO `question_option` VALUES (119, 30, 'equals()', 3);
INSERT INTO `question_option` VALUES (120, 30, 'compareTo()', 4);
INSERT INTO `question_option` VALUES (121, 31, '.java', 1);
INSERT INTO `question_option` VALUES (122, 31, '.class', 2);
INSERT INTO `question_option` VALUES (123, 31, '.jar', 3);
INSERT INTO `question_option` VALUES (124, 31, '.exe', 4);
INSERT INTO `question_option` VALUES (125, 32, '返回值类型不同', 1);
INSERT INTO `question_option` VALUES (126, 32, '方法名不同', 2);
INSERT INTO `question_option` VALUES (127, 32, '参数列表不同', 3);
INSERT INTO `question_option` VALUES (128, 32, '修饰符不同', 4);
INSERT INTO `question_option` VALUES (129, 33, 'CPU 寄存器', 1);
INSERT INTO `question_option` VALUES (130, 33, '堆区', 2);
INSERT INTO `question_option` VALUES (131, 33, '代码段', 3);
INSERT INTO `question_option` VALUES (132, 33, '数据段', 4);
INSERT INTO `question_option` VALUES (133, 34, 'try', 1);
INSERT INTO `question_option` VALUES (134, 34, 'catch', 2);
INSERT INTO `question_option` VALUES (135, 34, 'finally', 3);
INSERT INTO `question_option` VALUES (136, 34, 'throw', 4);
INSERT INTO `question_option` VALUES (137, 35, 'int arr = new int[5];', 1);
INSERT INTO `question_option` VALUES (138, 35, 'int[] arr = new int[5];', 2);
INSERT INTO `question_option` VALUES (139, 35, 'int arr[5] = new int[];', 3);
INSERT INTO `question_option` VALUES (140, 35, 'int[5] arr = new int[];', 4);
INSERT INTO `question_option` VALUES (141, 36, 'sleep()', 1);
INSERT INTO `question_option` VALUES (142, 36, 'notify()', 2);
INSERT INTO `question_option` VALUES (143, 36, 'start()', 3);
INSERT INTO `question_option` VALUES (144, 36, 'run()', 4);
INSERT INTO `question_option` VALUES (145, 37, 'ArrayList', 1);
INSERT INTO `question_option` VALUES (146, 37, 'LinkedList', 2);
INSERT INTO `question_option` VALUES (147, 37, 'HashSet', 3);
INSERT INTO `question_option` VALUES (148, 37, 'Vector', 4);
INSERT INTO `question_option` VALUES (149, 38, '静态变量', 1);
INSERT INTO `question_option` VALUES (150, 38, '静态方法', 2);
INSERT INTO `question_option` VALUES (151, 38, '实例变量', 3);
INSERT INTO `question_option` VALUES (152, 38, '常量', 4);
INSERT INTO `question_option` VALUES (153, 39, 'volatile', 1);
INSERT INTO `question_option` VALUES (154, 39, 'synchronized', 2);
INSERT INTO `question_option` VALUES (155, 39, 'lock', 3);
INSERT INTO `question_option` VALUES (156, 39, 'atomic', 4);
INSERT INTO `question_option` VALUES (157, 40, 'StringBuffer 是线程不安全', 1);
INSERT INTO `question_option` VALUES (158, 40, 'StringBuilder 线程安全', 2);
INSERT INTO `question_option` VALUES (159, 40, 'StringBuilder 性能更高', 3);
INSERT INTO `question_option` VALUES (160, 40, '两者完全相同', 4);
INSERT INTO `question_option` VALUES (161, 41, 'HashSet', 1);
INSERT INTO `question_option` VALUES (162, 41, 'ArrayList', 2);
INSERT INTO `question_option` VALUES (163, 41, 'HashMap', 3);
INSERT INTO `question_option` VALUES (164, 41, 'TreeMap', 4);
INSERT INTO `question_option` VALUES (165, 42, '%', 1);
INSERT INTO `question_option` VALUES (166, 42, '/', 2);
INSERT INTO `question_option` VALUES (167, 42, '&', 3);
INSERT INTO `question_option` VALUES (168, 42, '|', 4);
INSERT INTO `question_option` VALUES (169, 43, 'break 继续循环', 1);
INSERT INTO `question_option` VALUES (170, 43, 'continue 退出循环', 2);
INSERT INTO `question_option` VALUES (171, 43, 'break 退出当前整个循环', 3);
INSERT INTO `question_option` VALUES (172, 43, 'continue 终止程序', 4);
INSERT INTO `question_option` VALUES (173, 44, 'throws', 1);
INSERT INTO `question_option` VALUES (174, 44, 'throw', 2);
INSERT INTO `question_option` VALUES (175, 44, 'catch', 3);
INSERT INTO `question_option` VALUES (176, 44, 'finally', 4);
INSERT INTO `question_option` VALUES (177, 45, '只提高运行效率', 1);
INSERT INTO `question_option` VALUES (178, 45, '编译时类型安全', 2);
INSERT INTO `question_option` VALUES (179, 45, '只能用于集合', 3);
INSERT INTO `question_option` VALUES (180, 45, '减少运行时异常', 4);
INSERT INTO `question_option` VALUES (181, 46, 'String', 1);
INSERT INTO `question_option` VALUES (182, 46, 'Integer', 2);
INSERT INTO `question_option` VALUES (183, 46, 'char', 3);
INSERT INTO `question_option` VALUES (184, 46, 'Object', 4);
INSERT INTO `question_option` VALUES (185, 47, '标记-清除', 1);
INSERT INTO `question_option` VALUES (186, 47, '复制算法', 2);
INSERT INTO `question_option` VALUES (187, 47, '分代收集', 3);
INSERT INTO `question_option` VALUES (188, 47, '标记-整理', 4);
INSERT INTO `question_option` VALUES (189, 48, 'static', 1);
INSERT INTO `question_option` VALUES (190, 48, 'abstract', 2);
INSERT INTO `question_option` VALUES (191, 48, 'final', 3);
INSERT INTO `question_option` VALUES (192, 48, 'private', 4);
INSERT INTO `question_option` VALUES (193, 49, '匿名类', 1);
INSERT INTO `question_option` VALUES (194, 49, '函数式接口', 2);
INSERT INTO `question_option` VALUES (195, 49, '内部类', 3);
INSERT INTO `question_option` VALUES (196, 49, '泛型', 4);
INSERT INTO `question_option` VALUES (197, 50, 'String', 1);
INSERT INTO `question_option` VALUES (198, 50, 'System', 2);
INSERT INTO `question_option` VALUES (199, 50, 'ArrayList', 3);
INSERT INTO `question_option` VALUES (200, 50, 'Thread', 4);
INSERT INTO `question_option` VALUES (201, 51, 'sleep()', 1);
INSERT INTO `question_option` VALUES (202, 51, 'wait()', 2);
INSERT INTO `question_option` VALUES (203, 51, 'notify()', 3);
INSERT INTO `question_option` VALUES (204, 51, 'yield()', 4);
INSERT INTO `question_option` VALUES (205, 52, '>>', 1);
INSERT INTO `question_option` VALUES (206, 52, '>>>', 2);
INSERT INTO `question_option` VALUES (207, 52, '<<', 3);
INSERT INTO `question_option` VALUES (208, 52, '||', 4);
INSERT INTO `question_option` VALUES (209, 53, 'Runnable', 1);
INSERT INTO `question_option` VALUES (210, 53, 'Serializable', 2);
INSERT INTO `question_option` VALUES (211, 53, 'Comparable', 3);
INSERT INTO `question_option` VALUES (212, 53, 'Iterable', 4);
INSERT INTO `question_option` VALUES (213, 54, 'String -> StringBuilder', 1);
INSERT INTO `question_option` VALUES (214, 54, 'int -> Integer', 2);
INSERT INTO `question_option` VALUES (215, 54, 'Integer -> int', 3);
INSERT INTO `question_option` VALUES (216, 54, 'Object -> String', 4);
INSERT INTO `question_option` VALUES (217, 55, 'public', 1);
INSERT INTO `question_option` VALUES (218, 55, 'protected', 2);
INSERT INTO `question_option` VALUES (219, 55, 'private', 3);
INSERT INTO `question_option` VALUES (220, 55, 'final', 4);
INSERT INTO `question_option` VALUES (221, 56, 'import', 1);
INSERT INTO `question_option` VALUES (222, 56, 'package', 2);
INSERT INTO `question_option` VALUES (223, 56, 'class', 3);
INSERT INTO `question_option` VALUES (224, 56, 'interface', 4);
INSERT INTO `question_option` VALUES (225, 57, '5', 1);
INSERT INTO `question_option` VALUES (226, 57, '10', 2);
INSERT INTO `question_option` VALUES (227, 57, '15', 3);
INSERT INTO `question_option` VALUES (228, 57, '0', 4);
INSERT INTO `question_option` VALUES (229, 58, 'ArrayList', 1);
INSERT INTO `question_option` VALUES (230, 58, 'HashSet', 2);
INSERT INTO `question_option` VALUES (231, 58, 'HashMap', 3);
INSERT INTO `question_option` VALUES (232, 58, 'LinkedList', 4);
INSERT INTO `question_option` VALUES (233, 59, 'IOException', 1);
INSERT INTO `question_option` VALUES (234, 59, 'RuntimeException', 2);
INSERT INTO `question_option` VALUES (235, 59, 'NullPointerException', 3);
INSERT INTO `question_option` VALUES (236, 59, 'StackOverflowError', 4);
INSERT INTO `question_option` VALUES (237, 60, '内部类不能使用外部类成员', 1);
INSERT INTO `question_option` VALUES (238, 60, '静态内部类可以直接访问外部实例变量', 2);
INSERT INTO `question_option` VALUES (239, 60, '内部类可以访问外部类私有成员', 3);
INSERT INTO `question_option` VALUES (240, 60, '内部类只能定义在方法内', 4);
INSERT INTO `question_option` VALUES (241, 61, '泛型', 1);
INSERT INTO `question_option` VALUES (242, 61, '注解', 2);
INSERT INTO `question_option` VALUES (243, 61, 'Lambda', 3);
INSERT INTO `question_option` VALUES (244, 61, '枚举', 4);
INSERT INTO `question_option` VALUES (245, 62, 'enum Color [RED, GREEN]', 1);
INSERT INTO `question_option` VALUES (246, 62, 'enum Color {RED, GREEN}', 2);
INSERT INTO `question_option` VALUES (247, 62, 'Color enum {RED, GREEN}', 3);
INSERT INTO `question_option` VALUES (248, 62, 'enum Color = {RED, GREEN}', 4);
INSERT INTO `question_option` VALUES (249, 63, '@Override', 1);
INSERT INTO `question_option` VALUES (250, 63, '@Deprecated', 2);
INSERT INTO `question_option` VALUES (251, 63, '@SuppressWarnings', 3);
INSERT INTO `question_option` VALUES (252, 63, '@SafeVarargs', 4);
INSERT INTO `question_option` VALUES (253, 64, '需要手动关闭资源', 1);
INSERT INTO `question_option` VALUES (254, 64, '需要实现 Closeable 接口', 2);
INSERT INTO `question_option` VALUES (255, 64, '资源自动关闭，要求实现 AutoCloseable', 3);
INSERT INTO `question_option` VALUES (256, 64, '只能用于文件操作', 4);
INSERT INTO `question_option` VALUES (257, 65, 'java.util.Random', 1);
INSERT INTO `question_option` VALUES (258, 65, 'java.lang.Math', 2);
INSERT INTO `question_option` VALUES (259, 65, 'java.util.Scanner', 3);
INSERT INTO `question_option` VALUES (260, 65, 'java.io.Random', 4);
INSERT INTO `question_option` VALUES (261, 66, 'default 方法', 1);
INSERT INTO `question_option` VALUES (262, 66, 'static 方法', 2);
INSERT INTO `question_option` VALUES (263, 66, '抽象方法', 3);
INSERT INTO `question_option` VALUES (264, 66, 'private 方法', 4);
INSERT INTO `question_option` VALUES (265, 67, 'RUNNABLE', 1);
INSERT INTO `question_option` VALUES (266, 67, 'SLEEPING', 2);
INSERT INTO `question_option` VALUES (267, 67, 'BLOCKED', 3);
INSERT INTO `question_option` VALUES (268, 67, 'STOPPED', 4);
INSERT INTO `question_option` VALUES (269, 68, '3.14', 1);
INSERT INTO `question_option` VALUES (270, 68, '3.14f', 2);
INSERT INTO `question_option` VALUES (271, 68, '3.14d', 3);
INSERT INTO `question_option` VALUES (272, 68, '3.14L', 4);
INSERT INTO `question_option` VALUES (273, 69, '比较对象内容', 1);
INSERT INTO `question_option` VALUES (274, 69, '比较对象引用地址（基本类型比较值）', 2);
INSERT INTO `question_option` VALUES (275, 69, '总是比较哈希码', 3);
INSERT INTO `question_option` VALUES (276, 69, '比较对象类型', 4);
INSERT INTO `question_option` VALUES (277, 70, '&&', 1);
INSERT INTO `question_option` VALUES (278, 70, '||', 2);
INSERT INTO `question_option` VALUES (279, 70, '!', 3);
INSERT INTO `question_option` VALUES (280, 70, '|', 4);
INSERT INTO `question_option` VALUES (281, 71, 'toCharArray()', 1);
INSERT INTO `question_option` VALUES (282, 71, 'toArray()', 2);
INSERT INTO `question_option` VALUES (283, 71, 'getChars()', 3);
INSERT INTO `question_option` VALUES (284, 71, 'charAt()', 4);
INSERT INTO `question_option` VALUES (285, 72, 'SoftReference', 1);
INSERT INTO `question_option` VALUES (286, 72, 'WeakReference', 2);
INSERT INTO `question_option` VALUES (287, 72, 'PhantomReference', 3);
INSERT INTO `question_option` VALUES (288, 72, 'StrongReference', 4);
INSERT INTO `question_option` VALUES (289, 73, '-Xms', 1);
INSERT INTO `question_option` VALUES (290, 73, '-Xmx', 2);
INSERT INTO `question_option` VALUES (291, 73, '-Xss', 3);
INSERT INTO `question_option` VALUES (292, 73, '-XX:MaxPermSize', 4);
INSERT INTO `question_option` VALUES (293, 74, 'Callable 不能返回结果', 1);
INSERT INTO `question_option` VALUES (294, 74, 'Runnable 可以抛出异常', 2);
INSERT INTO `question_option` VALUES (295, 74, 'Callable 可以返回结果并抛出受检异常', 3);
INSERT INTO `question_option` VALUES (296, 74, '无区别', 4);
INSERT INTO `question_option` VALUES (297, 75, 'CLASS', 1);
INSERT INTO `question_option` VALUES (298, 75, 'SOURCE', 2);
INSERT INTO `question_option` VALUES (299, 75, 'RUNTIME', 3);
INSERT INTO `question_option` VALUES (300, 75, 'ALL', 4);
INSERT INTO `question_option` VALUES (301, 76, 'ArrayList', 1);
INSERT INTO `question_option` VALUES (302, 76, 'LinkedList', 2);
INSERT INTO `question_option` VALUES (303, 76, 'InputStream', 3);
INSERT INTO `question_option` VALUES (304, 76, 'HashMap', 4);
INSERT INTO `question_option` VALUES (305, 77, 'java.lang', 1);
INSERT INTO `question_option` VALUES (306, 77, 'java.util', 2);
INSERT INTO `question_option` VALUES (307, 77, 'java.io', 3);
INSERT INTO `question_option` VALUES (308, 77, 'java.util.concurrent', 4);
INSERT INTO `question_option` VALUES (309, 78, 'class A implements B, C', 1);
INSERT INTO `question_option` VALUES (310, 78, 'interface A extends B, C', 2);
INSERT INTO `question_option` VALUES (311, 78, 'interface A implements B, C', 3);
INSERT INTO `question_option` VALUES (312, 78, 'class A extends B, C', 4);
INSERT INTO `question_option` VALUES (313, 79, 'Thread', 1);
INSERT INTO `question_option` VALUES (314, 79, 'Runnable', 2);
INSERT INTO `question_option` VALUES (315, 79, 'Object', 3);
INSERT INTO `question_option` VALUES (316, 79, 'String', 4);
INSERT INTO `question_option` VALUES (317, 80, 'transient 字段不会被序列化', 1);
INSERT INTO `question_option` VALUES (318, 80, 'transient 字段必须被序列化', 2);
INSERT INTO `question_option` VALUES (319, 80, 'transient 只能修饰方法', 3);
INSERT INTO `question_option` VALUES (320, 80, 'transient 是并发关键字', 4);
INSERT INTO `question_option` VALUES (321, 81, 'ArrayList', 1);
INSERT INTO `question_option` VALUES (322, 81, 'Math', 2);
INSERT INTO `question_option` VALUES (323, 81, 'Scanner', 3);
INSERT INTO `question_option` VALUES (324, 81, 'HashMap', 4);
INSERT INTO `question_option` VALUES (325, 82, 'StringBuilder', 1);
INSERT INTO `question_option` VALUES (326, 82, 'StringBuffer', 2);
INSERT INTO `question_option` VALUES (327, 82, 'String', 3);
INSERT INTO `question_option` VALUES (328, 82, 'char[]', 4);
INSERT INTO `question_option` VALUES (329, 83, 'int arr[2,3]', 1);
INSERT INTO `question_option` VALUES (330, 83, 'int[][] arr = new int[2][3]', 2);
INSERT INTO `question_option` VALUES (331, 83, 'int[2][3] arr', 3);
INSERT INTO `question_option` VALUES (332, 83, 'int arr[][] = int[2][3]', 4);
INSERT INTO `question_option` VALUES (333, 84, 'ArrayStoreException', 1);
INSERT INTO `question_option` VALUES (334, 84, 'NullPointerException', 2);
INSERT INTO `question_option` VALUES (335, 84, 'IllegalStateException', 3);
INSERT INTO `question_option` VALUES (336, 84, 'ClassCastException', 4);
INSERT INTO `question_option` VALUES (337, 85, 'WeakReference', 1);
INSERT INTO `question_option` VALUES (338, 85, 'SoftReference', 2);
INSERT INTO `question_option` VALUES (339, 85, 'PhantomReference', 3);
INSERT INTO `question_option` VALUES (340, 85, '普通对象引用', 4);
INSERT INTO `question_option` VALUES (341, 86, '不支持队列操作', 1);
INSERT INTO `question_option` VALUES (342, 86, '实现了 Deque 接口', 2);
INSERT INTO `question_option` VALUES (343, 86, '元素有序不可重复', 3);
INSERT INTO `question_option` VALUES (344, 86, '基于数组', 4);
INSERT INTO `question_option` VALUES (345, 87, 'applet 的 start 方法', 1);
INSERT INTO `question_option` VALUES (346, 87, 'main 方法', 2);
INSERT INTO `question_option` VALUES (347, 87, 'init 方法', 3);
INSERT INTO `question_option` VALUES (348, 87, 'run 方法', 4);
INSERT INTO `question_option` VALUES (349, 88, '子类引用指向父类对象', 1);
INSERT INTO `question_option` VALUES (350, 88, '父类引用指向子类对象', 2);
INSERT INTO `question_option` VALUES (351, 88, '重载', 3);
INSERT INTO `question_option` VALUES (352, 88, '隐藏', 4);
INSERT INTO `question_option` VALUES (353, 89, 'run()', 1);
INSERT INTO `question_option` VALUES (354, 89, 'start()', 2);
INSERT INTO `question_option` VALUES (355, 89, 'sleep()', 3);
INSERT INTO `question_option` VALUES (356, 89, 'wait()', 4);
INSERT INTO `question_option` VALUES (357, 90, '每个对象创建时执行', 1);
INSERT INTO `question_option` VALUES (358, 90, '类加载时执行一次', 2);
INSERT INTO `question_option` VALUES (359, 90, '可以定义多个，按顺序执行', 3);
INSERT INTO `question_option` VALUES (360, 90, '不能访问静态变量', 4);
INSERT INTO `question_option` VALUES (361, 91, 'FileReader', 1);
INSERT INTO `question_option` VALUES (362, 91, 'FileInputStream', 2);
INSERT INTO `question_option` VALUES (363, 91, 'BufferedReader', 3);
INSERT INTO `question_option` VALUES (364, 91, 'InputStreamReader', 4);
INSERT INTO `question_option` VALUES (365, 92, '@Deprecated', 1);
INSERT INTO `question_option` VALUES (366, 92, '@SuppressWarnings', 2);
INSERT INTO `question_option` VALUES (367, 92, '@Override', 3);
INSERT INTO `question_option` VALUES (368, 92, '@SafeVarargs', 4);
INSERT INTO `question_option` VALUES (369, 93, 'long num = 100', 1);
INSERT INTO `question_option` VALUES (370, 93, 'long num = 100L', 2);
INSERT INTO `question_option` VALUES (371, 93, 'long num = 100l', 3);
INSERT INTO `question_option` VALUES (372, 93, 'long num = (long)100', 4);
INSERT INTO `question_option` VALUES (373, 94, '方法名必须相同', 1);
INSERT INTO `question_option` VALUES (374, 94, '参数列表必须相同', 2);
INSERT INTO `question_option` VALUES (375, 94, '返回类型可以不兼容', 3);
INSERT INTO `question_option` VALUES (376, 94, '访问权限不能更严格', 4);
INSERT INTO `question_option` VALUES (377, 95, '互斥', 1);
INSERT INTO `question_option` VALUES (378, 95, '请求与保持', 2);
INSERT INTO `question_option` VALUES (379, 95, '不可剥夺', 3);
INSERT INTO `question_option` VALUES (380, 95, '循环等待', 4);
INSERT INTO `question_option` VALUES (381, 96, '替代所有 null 检查', 1);
INSERT INTO `question_option` VALUES (382, 96, '优雅处理可能为空的值', 2);
INSERT INTO `question_option` VALUES (383, 96, '必须使用 get()', 3);
INSERT INTO `question_option` VALUES (384, 96, '性能更高', 4);
INSERT INTO `question_option` VALUES (385, 97, 'Java 8', 1);
INSERT INTO `question_option` VALUES (386, 97, 'Java 9', 2);
INSERT INTO `question_option` VALUES (387, 97, 'Java 11', 3);
INSERT INTO `question_option` VALUES (388, 97, 'Java 17', 4);
INSERT INTO `question_option` VALUES (389, 98, 'filter', 1);
INSERT INTO `question_option` VALUES (390, 98, 'map', 2);
INSERT INTO `question_option` VALUES (391, 98, 'collect', 3);
INSERT INTO `question_option` VALUES (392, 98, 'peek', 4);
INSERT INTO `question_option` VALUES (393, 99, 'record 是引用类型', 1);
INSERT INTO `question_option` VALUES (394, 99, 'record 可以继承其他类', 2);
INSERT INTO `question_option` VALUES (395, 99, 'record 是可变数据载体', 3);
INSERT INTO `question_option` VALUES (396, 99, 'record 必须手动写构造器', 4);
INSERT INTO `question_option` VALUES (397, 100, '类加载器', 1);
INSERT INTO `question_option` VALUES (398, 100, '执行引擎', 2);
INSERT INTO `question_option` VALUES (399, 100, '本地方法栈', 3);
INSERT INTO `question_option` VALUES (400, 100, '程序计数器', 4);
INSERT INTO `question_option` VALUES (401, 101, 'public', 1);
INSERT INTO `question_option` VALUES (402, 101, 'private', 2);
INSERT INTO `question_option` VALUES (403, 101, 'protected', 3);
INSERT INTO `question_option` VALUES (404, 101, 'static', 4);
INSERT INTO `question_option` VALUES (405, 102, 'String', 1);
INSERT INTO `question_option` VALUES (406, 102, 'boolean', 2);
INSERT INTO `question_option` VALUES (407, 102, 'char', 3);
INSERT INTO `question_option` VALUES (408, 102, 'byte', 4);
INSERT INTO `question_option` VALUES (409, 102, 'double', 5);
INSERT INTO `question_option` VALUES (410, 103, 'ArrayList', 1);
INSERT INTO `question_option` VALUES (411, 103, 'LinkedList', 2);
INSERT INTO `question_option` VALUES (412, 103, 'HashSet', 3);
INSERT INTO `question_option` VALUES (413, 103, 'Vector', 4);
INSERT INTO `question_option` VALUES (414, 103, 'Hashtable', 5);
INSERT INTO `question_option` VALUES (415, 104, 'int', 1);
INSERT INTO `question_option` VALUES (416, 104, 'String', 2);
INSERT INTO `question_option` VALUES (417, 104, 'int[]', 3);
INSERT INTO `question_option` VALUES (418, 104, 'ArrayList', 4);
INSERT INTO `question_option` VALUES (419, 104, 'Thread', 5);
INSERT INTO `question_option` VALUES (420, 105, 'void print(int a, int b)', 1);
INSERT INTO `question_option` VALUES (421, 105, 'int print(int a)', 2);
INSERT INTO `question_option` VALUES (422, 105, 'void print(int a)', 3);
INSERT INTO `question_option` VALUES (423, 105, 'void print(String a)', 4);
INSERT INTO `question_option` VALUES (424, 105, 'void print(double a)', 5);
INSERT INTO `question_option` VALUES (425, 106, 'clone()', 1);
INSERT INTO `question_option` VALUES (426, 106, 'equals()', 2);
INSERT INTO `question_option` VALUES (427, 106, 'hashCode()', 3);
INSERT INTO `question_option` VALUES (428, 106, 'notify()', 4);
INSERT INTO `question_option` VALUES (429, 106, 'sleep()', 5);
INSERT INTO `question_option` VALUES (430, 107, 'try', 1);
INSERT INTO `question_option` VALUES (431, 107, 'catch', 2);
INSERT INTO `question_option` VALUES (432, 107, 'finally', 3);
INSERT INTO `question_option` VALUES (433, 107, 'throw', 4);
INSERT INTO `question_option` VALUES (434, 107, 'throws', 5);
INSERT INTO `question_option` VALUES (435, 108, 'Lambda 表达式', 1);
INSERT INTO `question_option` VALUES (436, 108, 'Stream API', 2);
INSERT INTO `question_option` VALUES (437, 108, 'java.time 包', 3);
INSERT INTO `question_option` VALUES (438, 108, '模块化系统', 4);
INSERT INTO `question_option` VALUES (439, 108, '接口默认方法', 5);
INSERT INTO `question_option` VALUES (440, 109, '修饰类不能被继承', 1);
INSERT INTO `question_option` VALUES (441, 109, '修饰方法不能被重写', 2);
INSERT INTO `question_option` VALUES (442, 109, '修饰变量值不能改变', 3);
INSERT INTO `question_option` VALUES (443, 109, '修饰类不能被实例化', 4);
INSERT INTO `question_option` VALUES (444, 109, '修饰方法不能被调用', 5);
INSERT INTO `question_option` VALUES (445, 110, 'if-else', 1);
INSERT INTO `question_option` VALUES (446, 110, 'for', 2);
INSERT INTO `question_option` VALUES (447, 110, 'while', 3);
INSERT INTO `question_option` VALUES (448, 110, 'do-while', 4);
INSERT INTO `question_option` VALUES (449, 110, 'switch', 5);
INSERT INTO `question_option` VALUES (450, 111, 'IOException', 1);
INSERT INTO `question_option` VALUES (451, 111, 'SQLException', 2);
INSERT INTO `question_option` VALUES (452, 111, 'NullPointerException', 3);
INSERT INTO `question_option` VALUES (453, 111, 'ArrayIndexOutOfBoundsException', 4);
INSERT INTO `question_option` VALUES (454, 111, 'ArithmeticException', 5);
INSERT INTO `question_option` VALUES (455, 112, '继承 Thread 类', 1);
INSERT INTO `question_option` VALUES (456, 112, '实现 Runnable 接口', 2);
INSERT INTO `question_option` VALUES (457, 112, '实现 Callable 接口', 3);
INSERT INTO `question_option` VALUES (458, 112, '使用 Executor 框架', 4);
INSERT INTO `question_option` VALUES (459, 112, '实现 Serializable', 5);
INSERT INTO `question_option` VALUES (460, 113, 'ArrayList', 1);
INSERT INTO `question_option` VALUES (461, 113, 'LinkedList', 2);
INSERT INTO `question_option` VALUES (462, 113, 'HashSet', 3);
INSERT INTO `question_option` VALUES (463, 113, 'Vector', 4);
INSERT INTO `question_option` VALUES (464, 113, 'TreeMap', 5);
INSERT INTO `question_option` VALUES (465, 114, 'Comparable', 1);
INSERT INTO `question_option` VALUES (466, 114, 'Comparator', 2);
INSERT INTO `question_option` VALUES (467, 114, 'Runnable', 3);
INSERT INTO `question_option` VALUES (468, 114, 'Iterable', 4);
INSERT INTO `question_option` VALUES (469, 114, 'Cloneable', 5);
INSERT INTO `question_option` VALUES (470, 115, 'filter', 1);
INSERT INTO `question_option` VALUES (471, 115, 'map', 2);
INSERT INTO `question_option` VALUES (472, 115, 'sorted', 3);
INSERT INTO `question_option` VALUES (473, 115, 'collect', 4);
INSERT INTO `question_option` VALUES (474, 115, 'forEach', 5);
INSERT INTO `question_option` VALUES (475, 116, '堆', 1);
INSERT INTO `question_option` VALUES (476, 116, '栈', 2);
INSERT INTO `question_option` VALUES (477, 116, '方法区', 3);
INSERT INTO `question_option` VALUES (478, 116, '程序计数器', 4);
INSERT INTO `question_option` VALUES (479, 116, '直接内存', 5);
INSERT INTO `question_option` VALUES (480, 117, '@Override', 1);
INSERT INTO `question_option` VALUES (481, 117, '@Deprecated', 2);
INSERT INTO `question_option` VALUES (482, 117, '@SuppressWarnings', 3);
INSERT INTO `question_option` VALUES (483, 117, '@SafeVarargs', 4);
INSERT INTO `question_option` VALUES (484, 117, '@FunctionalInterface', 5);
INSERT INTO `question_option` VALUES (485, 118, 'synchronized', 1);
INSERT INTO `question_option` VALUES (486, 118, 'volatile', 2);
INSERT INTO `question_option` VALUES (487, 118, 'Lock', 3);
INSERT INTO `question_option` VALUES (488, 118, 'atomic', 4);
INSERT INTO `question_option` VALUES (489, 118, 'transient', 5);
INSERT INTO `question_option` VALUES (490, 119, 'NEW', 1);
INSERT INTO `question_option` VALUES (491, 119, 'RUNNABLE', 2);
INSERT INTO `question_option` VALUES (492, 119, 'BLOCKED', 3);
INSERT INTO `question_option` VALUES (493, 119, 'WAITING', 4);
INSERT INTO `question_option` VALUES (494, 119, 'DEAD', 5);
INSERT INTO `question_option` VALUES (495, 120, 'InputStream', 1);
INSERT INTO `question_option` VALUES (496, 120, 'OutputStream', 2);
INSERT INTO `question_option` VALUES (497, 120, 'Reader', 3);
INSERT INTO `question_option` VALUES (498, 120, 'Writer', 4);
INSERT INTO `question_option` VALUES (499, 120, 'File', 5);
INSERT INTO `question_option` VALUES (500, 121, '继承', 1);
INSERT INTO `question_option` VALUES (501, 121, '重写', 2);
INSERT INTO `question_option` VALUES (502, 121, '父类引用指向子类对象', 3);
INSERT INTO `question_option` VALUES (503, 121, '重载', 4);
INSERT INTO `question_option` VALUES (504, 121, 'private 方法', 5);
INSERT INTO `question_option` VALUES (505, 122, 'Integer', 1);
INSERT INTO `question_option` VALUES (506, 122, 'Double', 2);
INSERT INTO `question_option` VALUES (507, 122, 'Boolean', 3);
INSERT INTO `question_option` VALUES (508, 122, 'String', 4);
INSERT INTO `question_option` VALUES (509, 122, 'Character', 5);
INSERT INTO `question_option` VALUES (510, 123, '元素有序', 1);
INSERT INTO `question_option` VALUES (511, 123, '元素不可重复', 2);
INSERT INTO `question_option` VALUES (512, 123, '元素可重复', 3);
INSERT INTO `question_option` VALUES (513, 123, '允许 null 元素（HashSet）', 4);
INSERT INTO `question_option` VALUES (514, 123, '线程安全', 5);
INSERT INTO `question_option` VALUES (515, 124, 'Runnable', 1);
INSERT INTO `question_option` VALUES (516, 124, 'Comparator', 2);
INSERT INTO `question_option` VALUES (517, 124, 'Consumer', 3);
INSERT INTO `question_option` VALUES (518, 124, 'Serializable', 4);
INSERT INTO `question_option` VALUES (519, 124, 'Thread', 5);
INSERT INTO `question_option` VALUES (520, 125, 'public', 1);
INSERT INTO `question_option` VALUES (521, 125, 'protected', 2);
INSERT INTO `question_option` VALUES (522, 125, 'default', 3);
INSERT INTO `question_option` VALUES (523, 125, 'private', 4);
INSERT INTO `question_option` VALUES (524, 125, 'static', 5);
INSERT INTO `question_option` VALUES (525, 126, 'String s = \"hello\"', 1);
INSERT INTO `question_option` VALUES (526, 126, 'String s = new String(\"hello\")', 2);
INSERT INTO `question_option` VALUES (527, 126, 'String s = StringBuilder.toString()', 3);
INSERT INTO `question_option` VALUES (528, 126, 'String s = \"hello\" + \"world\"', 4);
INSERT INTO `question_option` VALUES (529, 126, 'String s = 123', 5);
INSERT INTO `question_option` VALUES (530, 127, '标记-清除', 1);
INSERT INTO `question_option` VALUES (531, 127, '复制', 2);
INSERT INTO `question_option` VALUES (532, 127, '标记-整理', 3);
INSERT INTO `question_option` VALUES (533, 127, '引用计数', 4);
INSERT INTO `question_option` VALUES (534, 127, '分代收集', 5);
INSERT INTO `question_option` VALUES (535, 128, 'goto', 1);
INSERT INTO `question_option` VALUES (536, 128, 'const', 2);
INSERT INTO `question_option` VALUES (537, 128, 'null', 3);
INSERT INTO `question_option` VALUES (538, 128, 'true', 4);
INSERT INTO `question_option` VALUES (539, 128, 'enum', 5);
INSERT INTO `question_option` VALUES (540, 129, 'static final 变量', 1);
INSERT INTO `question_option` VALUES (541, 129, '枚举', 2);
INSERT INTO `question_option` VALUES (542, 129, '接口中的常量', 3);
INSERT INTO `question_option` VALUES (543, 129, '局部变量', 4);
INSERT INTO `question_option` VALUES (544, 129, 'void', 5);
INSERT INTO `question_option` VALUES (545, 130, '引导类加载器（Bootstrap）', 1);
INSERT INTO `question_option` VALUES (546, 130, '扩展类加载器（Extension）', 2);
INSERT INTO `question_option` VALUES (547, 130, '应用类加载器（Application）', 3);
INSERT INTO `question_option` VALUES (548, 130, '自定义类加载器', 4);
INSERT INTO `question_option` VALUES (549, 130, '系统类加载器（System）与应用类加载器是同一个', 5);

-- ----------------------------
-- Table structure for resource_info
-- ----------------------------
DROP TABLE IF EXISTS `resource_info`;
CREATE TABLE `resource_info`  (
  `resource_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `teacher_id` int(11) NOT NULL COMMENT '教师ID',
  `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '父节点ID，0表示根节点',
  `node_type` tinyint(1) NOT NULL COMMENT '节点类型:1目录 2资源',
  `resource_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称，目录名或资源名',
  `resource_type` tinyint(1) NULL DEFAULT NULL COMMENT '资源类型:1 视频 2图片 3文档 4压缩包 5其他，目录为空',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '原始文件名，目录为空',
  `file_suffix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件后缀，目录为空',
  `file_size` bigint(20) NOT NULL DEFAULT 0 COMMENT '文件大小，目录为0',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件存储路径，目录为空',
  `cover_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图路径，可为空',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态:1上传中 2转码中 3上传成功  4转码失败 5上传失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `duration` int(11) NULL DEFAULT NULL COMMENT '持续时间',
  PRIMARY KEY (`resource_id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_node_type`(`node_type`) USING BTREE,
  INDEX `idx_resource_type`(`resource_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_teacher_parent`(`teacher_id`, `parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师资源信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of resource_info
-- ----------------------------

-- ----------------------------
-- Table structure for study_plan
-- ----------------------------
DROP TABLE IF EXISTS `study_plan`;
CREATE TABLE `study_plan`  (
  `plan_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学习计划ID',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程ID',
  `student_id` int(11) NOT NULL COMMENT '学生ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0未开始 1进行中 2完成',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '计划说明',
  PRIMARY KEY (`plan_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生学习计划主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of study_plan
-- ----------------------------

-- ----------------------------
-- Table structure for study_plan_item
-- ----------------------------
DROP TABLE IF EXISTS `study_plan_item`;
CREATE TABLE `study_plan_item`  (
  `item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '计划明细ID',
  `plan_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学习计划ID',
  `course_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程ID',
  `chapter_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节ID',
  `lesson_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课时ID',
  `start_time` int(11) NULL DEFAULT NULL COMMENT '开始时间',
  `complate_time` date NULL DEFAULT NULL COMMENT '完成日期',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0未开始 1进行中 2完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_plan_id`(`plan_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id`) USING BTREE,
  INDEX `idx_lesson_id`(`lesson_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生学习计划明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of study_plan_item
-- ----------------------------

-- ----------------------------
-- Table structure for system_notice
-- ----------------------------
DROP TABLE IF EXISTS `system_notice`;
CREATE TABLE `system_notice`  (
  `notice_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知ID',
  `notice_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知标题',
  `notice_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知内容',
  `target_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '发布范围: 1全部用户 2学生 3指定班级 4指定专业',
  `target_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发布目标ID: 班级ID/专业ID等',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态: 0草稿 1已发布 2已下线',
  `is_top` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否置顶: 0否 1是',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `create_user_id` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`notice_id`) USING BTREE,
  INDEX `idx_status_publish_time`(`status`, `publish_time`) USING BTREE,
  INDEX `idx_top_publish_time`(`is_top`, `publish_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统通知公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_notice
-- ----------------------------

-- ----------------------------
-- Table structure for system_menu
-- ----------------------------
DROP TABLE IF EXISTS `system_menu`;
CREATE TABLE `system_menu`  (
  `menu_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单编码',
  `menu_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级菜单编码',
  `menu_path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '前端路由路径',
  `route_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '前端路由名称',
  `menu_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '类型: 0目录 1菜单',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`menu_id`) USING BTREE,
  UNIQUE INDEX `uk_menu_code`(`menu_code`) USING BTREE,
  INDEX `idx_parent_code`(`parent_code`) USING BTREE,
  INDEX `idx_status_sort`(`status`, `sort_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_menu
-- ----------------------------
INSERT INTO `system_menu` VALUES (1, 'dashboard', '数据看板', NULL, '/dashboard', 'dashboard', 1, 10, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (2, 'basic-data', '基础数据', NULL, NULL, NULL, 0, 20, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (3, 'basic-data:department', '院系管理', 'basic-data', '/basic-data/department', 'basicDataDepartment', 1, 21, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (4, 'basic-data:major', '专业管理', 'basic-data', '/basic-data/major', 'basicDataMajor', 1, 22, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (5, 'basic-data:class', '班级管理', 'basic-data', '/basic-data/class', 'basicDataClass', 1, 23, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (6, 'basic-data:student', '学生管理', 'basic-data', '/basic-data/student', 'basicDataStudent', 1, 24, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (7, 'basic-data:teacher', '教师管理', 'basic-data', '/basic-data/teacher', 'basicDataTeacher', 1, 25, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (8, 'resource', '资源中心', NULL, NULL, NULL, 0, 30, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (9, 'resource:manage', '资源管理', 'resource', '/resource/manage', 'resourceManage', 1, 31, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (10, 'teaching', '教学业务', NULL, NULL, NULL, 0, 40, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (11, 'teaching:course', '课程管理', 'teaching', '/teaching/course', 'teachingCourse', 1, 41, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (12, 'teaching:exercise', '习题管理', 'teaching', '/teaching/exercise', 'teachingExercise', 1, 42, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (13, 'teaching:paper', '试卷管理', 'teaching', '/teaching/paper', 'teachingPaper', 1, 43, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (14, 'teaching:exam', '考试管理', 'teaching', '/teaching/exam', 'teachingExam', 1, 44, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (15, 'system', '系统管理', NULL, NULL, NULL, 0, 50, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (16, 'system:notice', '公告管理', 'system', '/system/notice', 'systemNotice', 1, 51, 1, NULL, NOW(), NULL);
INSERT INTO `system_menu` VALUES (17, 'system:permission', '权限管理', 'system', '/system/permission', 'systemPermission', 1, 52, 1, NULL, NOW(), NULL);

-- ----------------------------
-- Table structure for system_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `system_role_menu`;
CREATE TABLE `system_role_menu`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_type` tinyint(4) NOT NULL COMMENT '角色类型: 0管理员 1老师 2学生',
  `menu_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单编码',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_type`, `menu_code`) USING BTREE,
  INDEX `idx_menu_code`(`menu_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单权限关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_role_menu
-- ----------------------------
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'dashboard', NOW());
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'resource', NOW());
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'resource:manage', NOW());
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'teaching', NOW());
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'teaching:course', NOW());
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'teaching:exercise', NOW());
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'teaching:paper', NOW());
INSERT INTO `system_role_menu` (`role_type`, `menu_code`, `create_time`) VALUES (1, 'teaching:exam', NOW());

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户编号/学号/工号/管理员账号',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码',
  `real_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '真实姓名',
  `gender` tinyint(4) NOT NULL COMMENT '性别: 1男 2女 0未知',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像地址',
  `role_type` tinyint(1) NOT NULL COMMENT '角色类型: 0:管理员 1:老师  2:学生',
  `class_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班级ID',
  `title_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职称(教师用)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_user_no`(`user_no`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE,
  INDEX `idx_role_code`(`role_type`) USING BTREE,
  INDEX `idx_class_id`(`class_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 202605188 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES (202600000, 'ADMIN', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 0, '15672657968', 'admin@example.com', NULL, 0, NULL, NULL, 1, '2026-04-28 09:18:10');
INSERT INTO `user_info` VALUES (202600001, 'T0001', 'e10adc3949ba59abbe56e057f20f883e', '张建国', 1, '18286742714', '131000001@example.com', NULL, 1, '10000,10001,10002', '教授', 1, '2026-05-10 09:09:17');
INSERT INTO `user_info` VALUES (202600002, 'T0002', 'e10adc3949ba59abbe56e057f20f883e', '李芳', 2, '17694189879', '131000002@example.com', NULL, 1, '10003,10004,10005', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600003, 'T0003', 'e10adc3949ba59abbe56e057f20f883e', '王强', 1, '13808923305', '131000003@example.com', NULL, 1, '10006,10007,10008', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600004, 'T0004', 'e10adc3949ba59abbe56e057f20f883e', '陈静', 2, '19621833915', '131000004@example.com', NULL, 1, '10009,10010,10011', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600005, 'T0005', 'e10adc3949ba59abbe56e057f20f883e', '刘明', 1, '15934534675', '131000005@example.com', NULL, 1, '10012,10013,10014', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600006, 'T0006', 'e10adc3949ba59abbe56e057f20f883e', '赵敏', 2, '17759653625', '131000006@example.com', NULL, 1, '10015,10016,10017', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600007, 'T0007', 'e10adc3949ba59abbe56e057f20f883e', '周涛', 1, '15922606824', '131000007@example.com', NULL, 1, '10018,10019,10020', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600008, 'T0008', 'e10adc3949ba59abbe56e057f20f883e', '吴迪', 1, '19792647062', '131000008@example.com', NULL, 1, '10021,10022,10023', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600009, 'T0009', 'e10adc3949ba59abbe56e057f20f883e', '郑爽', 2, '19556832797', '131000009@example.com', NULL, 1, '10024,10025,10026', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600010, 'T0010', 'e10adc3949ba59abbe56e057f20f883e', '孙阳', 1, '14745999608', '131000010@example.com', NULL, 1, '10027,10028,10029', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600011, 'T0011', 'e10adc3949ba59abbe56e057f20f883e', '林晨', 2, '18262497421', '131000011@example.com', NULL, 1, '10030,10031,10032', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600012, 'T0012', 'e10adc3949ba59abbe56e057f20f883e', '郭峰', 1, '15573454553', '131000012@example.com', NULL, 1, '10033,10034,10035', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600013, 'T0013', 'e10adc3949ba59abbe56e057f20f883e', '唐雅', 2, '18633159754', '131000013@example.com', NULL, 1, '10036,10037,10038', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600014, 'T0014', 'e10adc3949ba59abbe56e057f20f883e', '沈梦', 2, '17868944378', '131000014@example.com', NULL, 1, '10039,10040,10041', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600015, 'T0015', 'e10adc3949ba59abbe56e057f20f883e', '宋阳', 1, '19211505421', '131000015@example.com', NULL, 1, '10042,10043,10044', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600016, 'T0016', 'e10adc3949ba59abbe56e057f20f883e', '许杰', 1, '13298103639', '131000016@example.com', NULL, 1, '10045,10046,10047', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600017, 'T0017', 'e10adc3949ba59abbe56e057f20f883e', '何璐', 2, '18660420383', '131000017@example.com', NULL, 1, '10048,10049,10050', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600018, 'T0018', 'e10adc3949ba59abbe56e057f20f883e', '黄欣', 2, '19665054552', '131000018@example.com', NULL, 1, '10051,10052,10053', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600019, 'T0019', 'e10adc3949ba59abbe56e057f20f883e', '丁宁', 1, '18092253071', '131000019@example.com', NULL, 1, '10054,10055,10056', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600020, 'T0020', 'e10adc3949ba59abbe56e057f20f883e', '魏晨', 1, '19567317409', '131000020@example.com', NULL, 1, '10057,10058,10059', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600021, 'T0021', 'e10adc3949ba59abbe56e057f20f883e', '冯雪', 2, '18648753169', '131000021@example.com', NULL, 1, '10060,10061,10062', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600022, 'T0022', 'e10adc3949ba59abbe56e057f20f883e', '韩梅', 2, '15620453563', '131000022@example.com', NULL, 1, '10063,10064,10065', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600023, 'T0023', 'e10adc3949ba59abbe56e057f20f883e', '彭博', 1, '13149839210', '131000023@example.com', NULL, 1, '10066,10067,10068', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600024, 'T0024', 'e10adc3949ba59abbe56e057f20f883e', '陆涛', 1, '16662862950', '131000024@example.com', NULL, 1, '10069,10070,10071', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600025, 'T0025', 'e10adc3949ba59abbe56e057f20f883e', '苏雅', 2, '19545843189', '131000025@example.com', NULL, 1, '10072,10073,10074', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600026, 'T0026', 'e10adc3949ba59abbe56e057f20f883e', '蒋欣', 2, '18505254083', '131000026@example.com', NULL, 1, '10075,10076,10077', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600027, 'T0027', 'e10adc3949ba59abbe56e057f20f883e', '蔡琴', 2, '13987238906', '131000027@example.com', NULL, 1, '10078,10079,10080', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600028, 'T0028', 'e10adc3949ba59abbe56e057f20f883e', '余凡', 1, '18810252505', '131000028@example.com', NULL, 1, '10081,10082,10083', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600029, 'T0029', 'e10adc3949ba59abbe56e057f20f883e', '杜宇', 1, '15109647476', '131000029@example.com', NULL, 1, '10084,10085,10086', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600030, 'T0030', 'e10adc3949ba59abbe56e057f20f883e', '戴月', 2, '18391029530', '131000030@example.com', NULL, 1, '10087,10088,10089', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600031, 'T0031', 'e10adc3949ba59abbe56e057f20f883e', '魏强', 1, '18342733815', '131000031@example.com', NULL, 1, '10090,10091,10092', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600032, 'T0032', 'e10adc3949ba59abbe56e057f20f883e', '夏雪', 2, '14792676751', '131000032@example.com', NULL, 1, '10093,10094,10095', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600033, 'T0033', 'e10adc3949ba59abbe56e057f20f883e', '钟诚', 1, '19676253960', '131000033@example.com', NULL, 1, '10096,10097,10098', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600034, 'T0034', 'e10adc3949ba59abbe56e057f20f883e', '田甜', 2, '13314734508', '131000034@example.com', NULL, 1, '10099,10100,10101', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600035, 'T0035', 'e10adc3949ba59abbe56e057f20f883e', '姜涛', 1, '17593670041', '131000035@example.com', NULL, 1, '10102,10103,10104', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600036, 'T0036', 'e10adc3949ba59abbe56e057f20f883e', '崔岩', 1, '15047120235', '131000036@example.com', NULL, 1, '10105,10106,10107', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600037, 'T0037', 'e10adc3949ba59abbe56e057f20f883e', '任静', 2, '18152254136', '131000037@example.com', NULL, 1, '10108,10109,10110', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600038, 'T0038', 'e10adc3949ba59abbe56e057f20f883e', '廖飞', 1, '19368746496', '131000038@example.com', NULL, 1, '10111,10112,10113', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600039, 'T0039', 'e10adc3949ba59abbe56e057f20f883e', '邢丽', 2, '19535896232', '131000039@example.com', NULL, 1, '10114,10115,10116', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600040, 'T0040', 'e10adc3949ba59abbe56e057f20f883e', '金鑫', 1, '13663309983', '131000040@example.com', NULL, 1, '10117,10118,10119', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600041, 'T0041', 'e10adc3949ba59abbe56e057f20f883e', '乔峰', 1, '18879823627', '131000041@example.com', NULL, 1, '10120,10121,10122', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600042, 'T0042', 'e10adc3949ba59abbe56e057f20f883e', '谭芳', 2, '18946859092', '131000042@example.com', NULL, 1, '10123,10124,10125', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600043, 'T0043', 'e10adc3949ba59abbe56e057f20f883e', '毛磊', 1, '13171910138', '131000043@example.com', NULL, 1, '10126,10127,10128', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600044, 'T0044', 'e10adc3949ba59abbe56e057f20f883e', '石磊', 1, '17744560826', '131000044@example.com', NULL, 1, '10129,10130,10131', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600045, 'T0045', 'e10adc3949ba59abbe56e057f20f883e', '顾佳', 2, '18499464486', '131000045@example.com', NULL, 1, '10132,10133,10134', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600046, 'T0046', 'e10adc3949ba59abbe56e057f20f883e', '孟欣', 2, '19988777837', '131000046@example.com', NULL, 1, '10135,10136,10137', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600047, 'T0047', 'e10adc3949ba59abbe56e057f20f883e', '欧阳', 1, '17791598562', '131000047@example.com', NULL, 1, '10138,10139,10140', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600048, 'T0048', 'e10adc3949ba59abbe56e057f20f883e', '诸葛', 1, '13141372179', '131000048@example.com', NULL, 1, '10141,10142,10143', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600049, 'T0049', 'e10adc3949ba59abbe56e057f20f883e', '司马', 1, '19961684376', '131000049@example.com', NULL, 1, '10144,10145,10146', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600050, 'T0050', 'e10adc3949ba59abbe56e057f20f883e', '上官', 2, '13799653568', '131000050@example.com', NULL, 1, '10147,10148,10149', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600051, 'T0051', 'e10adc3949ba59abbe56e057f20f883e', '慕容', 2, '17538258435', '131000051@example.com', NULL, 1, '10150,10151,10152', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600052, 'T0052', 'e10adc3949ba59abbe56e057f20f883e', '南宫', 1, '17630028178', '131000052@example.com', NULL, 1, '10153,10154,10155', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600053, 'T0053', 'e10adc3949ba59abbe56e057f20f883e', '端木', 1, '13126209512', '131000053@example.com', NULL, 1, '10156,10157,10158', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600054, 'T0054', 'e10adc3949ba59abbe56e057f20f883e', '东方', 1, '13927727147', '131000054@example.com', NULL, 1, '10159,10160,10161', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600055, 'T0055', 'e10adc3949ba59abbe56e057f20f883e', '令狐', 1, '18890930212', '131000055@example.com', NULL, 1, '10162,10163,10164', '教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600056, 'T0056', 'e10adc3949ba59abbe56e057f20f883e', '独孤', 1, '15278129739', '131000056@example.com', NULL, 1, '10165,10166,10167', '副教授', 1, NULL);
INSERT INTO `user_info` VALUES (202600057, 'T0057', 'e10adc3949ba59abbe56e057f20f883e', '夏侯', 1, '13066093166', '131000057@example.com', NULL, 1, '10168,10169,10170', '讲师', 1, NULL);
INSERT INTO `user_info` VALUES (202600058, '20251000001', 'e10adc3949ba59abbe56e057f20f883e', '张伟', 1, '15252602534', 'stu_10000_01@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600059, '20251000002', 'e10adc3949ba59abbe56e057f20f883e', '李芳', 2, '18704609682', 'stu_10000_02@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600060, '20251000003', 'e10adc3949ba59abbe56e057f20f883e', '王磊', 1, '13214161177', 'stu_10000_03@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600061, '20251000004', 'e10adc3949ba59abbe56e057f20f883e', '赵静', 2, '17827575262', 'stu_10000_04@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600062, '20251000005', 'e10adc3949ba59abbe56e057f20f883e', '陈强', 1, '18996479422', 'stu_10000_05@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600063, '20251000006', 'e10adc3949ba59abbe56e057f20f883e', '刘洋', 2, '17884931681', 'stu_10000_06@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600064, '20251000007', 'e10adc3949ba59abbe56e057f20f883e', '周敏', 1, '18245387179', 'stu_10000_07@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600065, '20251000008', 'e10adc3949ba59abbe56e057f20f883e', '吴婷', 2, '17597971297', 'stu_10000_08@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600066, '20251000009', 'e10adc3949ba59abbe56e057f20f883e', '郑爽', 1, '17652970071', 'stu_10000_09@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600067, '20251000010', 'e10adc3949ba59abbe56e057f20f883e', '孙莉', 2, '13724689707', 'stu_10000_10@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600068, '20251000011', 'e10adc3949ba59abbe56e057f20f883e', '林晨', 1, '18792373834', 'stu_10000_11@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600069, '20251000012', 'e10adc3949ba59abbe56e057f20f883e', '郭峰', 2, '15932895507', 'stu_10000_12@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600070, '20251000013', 'e10adc3949ba59abbe56e057f20f883e', '唐雅', 1, '15893650158', 'stu_10000_13@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600071, '20251000014', 'e10adc3949ba59abbe56e057f20f883e', '沈梦', 2, '17783293471', 'stu_10000_14@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600072, '20251000015', 'e10adc3949ba59abbe56e057f20f883e', '宋阳', 1, '18139724556', 'stu_10000_15@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600073, '20251000016', 'e10adc3949ba59abbe56e057f20f883e', '许杰', 2, '15005125359', 'stu_10000_16@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600074, '20251000017', 'e10adc3949ba59abbe56e057f20f883e', '何璐', 1, '17639860847', 'stu_10000_17@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600075, '20251000018', 'e10adc3949ba59abbe56e057f20f883e', '黄欣', 2, '17311227887', 'stu_10000_18@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600076, '20251000019', 'e10adc3949ba59abbe56e057f20f883e', '丁宁', 1, '13754582271', 'stu_10000_19@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600077, '20251000020', 'e10adc3949ba59abbe56e057f20f883e', '魏晨', 2, '13836012204', 'stu_10000_20@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600078, '20251000021', 'e10adc3949ba59abbe56e057f20f883e', '冯雪', 1, '13992991472', 'stu_10000_21@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600079, '20251000022', 'e10adc3949ba59abbe56e057f20f883e', '韩梅', 2, '13142728159', 'stu_10000_22@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600080, '20251000023', 'e10adc3949ba59abbe56e057f20f883e', '彭博', 1, '13074911806', 'stu_10000_23@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600081, '20251000024', 'e10adc3949ba59abbe56e057f20f883e', '陆涛', 2, '18738770924', 'stu_10000_24@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600082, '20251000025', 'e10adc3949ba59abbe56e057f20f883e', '苏雅', 1, '18962441334', 'stu_10000_25@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600083, '20251000026', 'e10adc3949ba59abbe56e057f20f883e', '蒋欣', 2, '19337447063', 'stu_10000_26@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600084, '20251000027', 'e10adc3949ba59abbe56e057f20f883e', '蔡琴', 1, '15549522221', 'stu_10000_27@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600085, '20251000028', 'e10adc3949ba59abbe56e057f20f883e', '余凡', 2, '17603898670', 'stu_10000_28@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600086, '20251000029', 'e10adc3949ba59abbe56e057f20f883e', '杜宇', 1, '18528374768', 'stu_10000_29@example.com', NULL, 2, '10000', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600087, '20251000030', 'e10adc3949ba59abbe56e057f20f883e', '戴月', 2, '15804032588', 'stu_10000_30@example.com', NULL, 2, '10000', NULL, 1, '2026-05-05 10:28:23');
INSERT INTO `user_info` VALUES (202600088, '20251000101', 'e10adc3949ba59abbe56e057f20f883e', '张明', 1, '13321246287', 'stu_10001_01@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600089, '20251000102', 'e10adc3949ba59abbe56e057f20f883e', '李红', 2, '19367386191', 'stu_10001_02@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600090, '20251000103', 'e10adc3949ba59abbe56e057f20f883e', '王强', 1, '19089121081', 'stu_10001_03@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600091, '20251000104', 'e10adc3949ba59abbe56e057f20f883e', '赵丽', 2, '13486824855', 'stu_10001_04@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600092, '20251000105', 'e10adc3949ba59abbe56e057f20f883e', '陈浩', 1, '13047582555', 'stu_10001_05@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600093, '20251000106', 'e10adc3949ba59abbe56e057f20f883e', '刘娜', 2, '15522644878', 'stu_10001_06@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600094, '20251000107', 'e10adc3949ba59abbe56e057f20f883e', '周涛', 1, '13602009727', 'stu_10001_07@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600095, '20251000108', 'e10adc3949ba59abbe56e057f20f883e', '吴迪', 2, '18534380949', 'stu_10001_08@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600096, '20251000109', 'e10adc3949ba59abbe56e057f20f883e', '郑爽', 1, '18535150507', 'stu_10001_09@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600097, '20251000110', 'e10adc3949ba59abbe56e057f20f883e', '孙阳', 2, '18758431128', 'stu_10001_10@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600098, '20251000111', 'e10adc3949ba59abbe56e057f20f883e', '林晨', 1, '18893836906', 'stu_10001_11@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600099, '20251000112', 'e10adc3949ba59abbe56e057f20f883e', '郭峰', 2, '17557021089', 'stu_10001_12@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600100, '20251000113', 'e10adc3949ba59abbe56e057f20f883e', '唐雅', 1, '15938868576', 'stu_10001_13@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600101, '20251000114', 'e10adc3949ba59abbe56e057f20f883e', '沈梦', 2, '18525313171', 'stu_10001_14@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600102, '20251000115', 'e10adc3949ba59abbe56e057f20f883e', '宋阳', 1, '13931133098', '20251000115@campus.edu.cn', 'avatar/202605/19f2f9227563457395346450b1d39278.jpg', 2, '10001', NULL, 1, '2026-05-10 09:54:54');
INSERT INTO `user_info` VALUES (202600103, '20251000116', 'e10adc3949ba59abbe56e057f20f883e', '许杰', 2, '19763710349', 'stu_10001_16@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600104, '20251000117', 'e10adc3949ba59abbe56e057f20f883e', '何璐', 1, '16630298789', 'stu_10001_17@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600105, '20251000118', 'e10adc3949ba59abbe56e057f20f883e', '黄欣', 2, '13802283880', 'stu_10001_18@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600106, '20251000119', 'e10adc3949ba59abbe56e057f20f883e', '丁宁', 1, '17870704656', 'stu_10001_19@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600107, '20251000120', 'e10adc3949ba59abbe56e057f20f883e', '魏晨', 2, '19522833574', 'stu_10001_20@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600108, '20251000121', 'e10adc3949ba59abbe56e057f20f883e', '冯雪', 1, '17797496041', 'stu_10001_21@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600109, '20251000122', 'e10adc3949ba59abbe56e057f20f883e', '韩梅', 2, '15146922638', 'stu_10001_22@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600110, '20251000123', 'e10adc3949ba59abbe56e057f20f883e', '彭博', 1, '17614065782', 'stu_10001_23@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600111, '20251000124', 'e10adc3949ba59abbe56e057f20f883e', '陆涛', 2, '13744946146', 'stu_10001_24@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600112, '20251000125', 'e10adc3949ba59abbe56e057f20f883e', '苏雅', 1, '18727384778', 'stu_10001_25@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600113, '20251000126', 'e10adc3949ba59abbe56e057f20f883e', '蒋欣', 2, '13815795490', 'stu_10001_26@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600114, '20251000127', 'e10adc3949ba59abbe56e057f20f883e', '蔡琴', 1, '13953651339', 'stu_10001_27@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600115, '20251000128', 'e10adc3949ba59abbe56e057f20f883e', '余凡', 2, '13377216060', 'stu_10001_28@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600116, '20251000129', 'e10adc3949ba59abbe56e057f20f883e', '杜宇', 1, '18384243131', 'stu_10001_29@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600117, '20251000130', 'e10adc3949ba59abbe56e057f20f883e', '戴月', 2, '15505674981', 'stu_10001_30@example.com', NULL, 2, '10001', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600118, '20251000201', 'e10adc3949ba59abbe56e057f20f883e', '张伟', 1, '15549636494', 'stu_10002_01@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600119, '20251000202', 'e10adc3949ba59abbe56e057f20f883e', '李芳', 2, '17592018593', 'stu_10002_02@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600120, '20251000203', 'e10adc3949ba59abbe56e057f20f883e', '王磊', 1, '13601624846', 'stu_10002_03@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600121, '20251000204', 'e10adc3949ba59abbe56e057f20f883e', '赵静', 2, '18202704779', 'stu_10002_04@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600122, '20251000205', 'e10adc3949ba59abbe56e057f20f883e', '陈强', 1, '15236767219', 'stu_10002_05@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600123, '20251000206', 'e10adc3949ba59abbe56e057f20f883e', '刘洋', 2, '19974234187', 'stu_10002_06@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600124, '20251000207', 'e10adc3949ba59abbe56e057f20f883e', '周敏', 1, '19179544619', 'stu_10002_07@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600125, '20251000208', 'e10adc3949ba59abbe56e057f20f883e', '吴婷', 2, '18043240112', 'stu_10002_08@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600126, '20251000209', 'e10adc3949ba59abbe56e057f20f883e', '郑爽', 1, '17505660591', 'stu_10002_09@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600127, '20251000210', 'e10adc3949ba59abbe56e057f20f883e', '孙莉', 2, '19518390254', 'stu_10002_10@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600128, '20251000211', 'e10adc3949ba59abbe56e057f20f883e', '林晨', 1, '15298469533', 'stu_10002_11@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600129, '20251000212', 'e10adc3949ba59abbe56e057f20f883e', '郭峰', 2, '13005408868', 'stu_10002_12@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600130, '20251000213', 'e10adc3949ba59abbe56e057f20f883e', '唐雅', 1, '15016542737', 'stu_10002_13@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600131, '20251000214', 'e10adc3949ba59abbe56e057f20f883e', '沈梦', 2, '13165632225', 'stu_10002_14@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600132, '20251000215', 'e10adc3949ba59abbe56e057f20f883e', '宋阳', 1, '13898127671', 'stu_10002_15@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600133, '20251000216', 'e10adc3949ba59abbe56e057f20f883e', '许杰', 2, '15673735753', 'stu_10002_16@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600134, '20251000217', 'e10adc3949ba59abbe56e057f20f883e', '何璐', 1, '18517830309', 'stu_10002_17@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600135, '20251000218', 'e10adc3949ba59abbe56e057f20f883e', '黄欣', 2, '19375154124', 'stu_10002_18@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600136, '20251000219', 'e10adc3949ba59abbe56e057f20f883e', '丁宁', 1, '13867455606', 'stu_10002_19@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600137, '20251000220', 'e10adc3949ba59abbe56e057f20f883e', '魏晨', 2, '19105269425', 'stu_10002_20@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600138, '20251000221', 'e10adc3949ba59abbe56e057f20f883e', '冯雪', 1, '19193018662', 'stu_10002_21@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600139, '20251000222', 'e10adc3949ba59abbe56e057f20f883e', '韩梅', 2, '13819929781', 'stu_10002_22@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600140, '20251000223', 'e10adc3949ba59abbe56e057f20f883e', '彭博', 1, '15941497920', 'stu_10002_23@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600141, '20251000224', 'e10adc3949ba59abbe56e057f20f883e', '陆涛', 2, '19509092485', 'stu_10002_24@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600142, '20251000225', 'e10adc3949ba59abbe56e057f20f883e', '苏雅', 1, '19399275967', 'stu_10002_25@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600143, '20251000226', 'e10adc3949ba59abbe56e057f20f883e', '蒋欣', 2, '15904411044', 'stu_10002_26@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600144, '20251000227', 'e10adc3949ba59abbe56e057f20f883e', '蔡琴', 1, '13090326160', 'stu_10002_27@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600145, '20251000228', 'e10adc3949ba59abbe56e057f20f883e', '余凡', 2, '17676497677', 'stu_10002_28@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600146, '20251000229', 'e10adc3949ba59abbe56e057f20f883e', '杜宇', 1, '15640520452', 'stu_10002_29@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202600147, '20251000230', 'e10adc3949ba59abbe56e057f20f883e', '戴月', 2, '13081409530', 'stu_10002_30@example.com', NULL, 2, '10002', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605158, '20251017001', 'e10adc3949ba59abbe56e057f20f883e', '张伟', 1, '13282097032', 'stu_10170_01@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605159, '20251017002', 'e10adc3949ba59abbe56e057f20f883e', '李芳', 2, '19827157872', 'stu_10170_02@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605160, '20251017003', 'e10adc3949ba59abbe56e057f20f883e', '王磊', 1, '17777126311', 'stu_10170_03@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605161, '20251017004', 'e10adc3949ba59abbe56e057f20f883e', '赵静', 2, '15220979097', 'stu_10170_04@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605162, '20251017005', 'e10adc3949ba59abbe56e057f20f883e', '陈强', 1, '13604466069', 'stu_10170_05@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605163, '20251017006', 'e10adc3949ba59abbe56e057f20f883e', '刘洋', 2, '19196043480', 'stu_10170_06@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605164, '20251017007', 'e10adc3949ba59abbe56e057f20f883e', '周敏', 1, '15684631520', 'stu_10170_07@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605165, '20251017008', 'e10adc3949ba59abbe56e057f20f883e', '吴婷', 2, '13843607598', 'stu_10170_08@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605166, '20251017009', 'e10adc3949ba59abbe56e057f20f883e', '郑爽', 1, '18166860704', 'stu_10170_09@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605167, '20251017010', 'e10adc3949ba59abbe56e057f20f883e', '孙莉', 2, '18077885037', 'stu_10170_10@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605168, '20251017011', 'e10adc3949ba59abbe56e057f20f883e', '林晨', 1, '14778069393', 'stu_10170_11@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605169, '20251017012', 'e10adc3949ba59abbe56e057f20f883e', '郭峰', 2, '14782353176', 'stu_10170_12@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605170, '20251017013', 'e10adc3949ba59abbe56e057f20f883e', '唐雅', 1, '15962085841', 'stu_10170_13@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605171, '20251017014', 'e10adc3949ba59abbe56e057f20f883e', '沈梦', 2, '19338452422', 'stu_10170_14@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605172, '20251017015', 'e10adc3949ba59abbe56e057f20f883e', '宋阳', 1, '15771823482', 'stu_10170_15@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605173, '20251017016', 'e10adc3949ba59abbe56e057f20f883e', '许杰', 2, '17518574387', 'stu_10170_16@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605174, '20251017017', 'e10adc3949ba59abbe56e057f20f883e', '何璐', 1, '17706502041', 'stu_10170_17@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605175, '20251017018', 'e10adc3949ba59abbe56e057f20f883e', '黄欣', 2, '18856369336', 'stu_10170_18@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605176, '20251017019', 'e10adc3949ba59abbe56e057f20f883e', '丁宁', 1, '18012499045', 'stu_10170_19@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605177, '20251017020', 'e10adc3949ba59abbe56e057f20f883e', '魏晨', 2, '19830157860', 'stu_10170_20@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605178, '20251017021', 'e10adc3949ba59abbe56e057f20f883e', '冯雪', 1, '18660507967', 'stu_10170_21@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605179, '20251017022', 'e10adc3949ba59abbe56e057f20f883e', '韩梅', 2, '19778230664', 'stu_10170_22@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605180, '20251017023', 'e10adc3949ba59abbe56e057f20f883e', '彭博', 1, '13641810942', 'stu_10170_23@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605181, '20251017024', 'e10adc3949ba59abbe56e057f20f883e', '陆涛', 2, '18390331964', 'stu_10170_24@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605182, '20251017025', 'e10adc3949ba59abbe56e057f20f883e', '苏雅', 1, '18240008874', 'stu_10170_25@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605183, '20251017026', 'e10adc3949ba59abbe56e057f20f883e', '蒋欣', 2, '13647180965', 'stu_10170_26@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605184, '20251017027', 'e10adc3949ba59abbe56e057f20f883e', '蔡琴', 1, '19936358491', 'stu_10170_27@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605185, '20251017028', 'e10adc3949ba59abbe56e057f20f883e', '余凡', 2, '19866182679', 'stu_10170_28@example.com', NULL, 2, '10170', NULL, 1, NULL);
INSERT INTO `user_info` VALUES (202605186, '20251017029', 'e10adc3949ba59abbe56e057f20f883e', '杜宇', 1, '17330244510', 'stu_10170_29@example.com', NULL, 2, '10170', NULL, 1, '2026-05-06 09:08:31');
INSERT INTO `user_info` VALUES (202605187, '20251017030', 'e10adc3949ba59abbe56e057f20f883e', '戴月', 2, '13679001620', 'stu_10170_30@example.com', NULL, 2, '10170', NULL, 1, '2026-05-06 09:08:55');

SET FOREIGN_KEY_CHECKS = 1;
