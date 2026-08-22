-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: nexora
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `agent_message`
--

DROP TABLE IF EXISTS `agent_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agent_message` (
  `message_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息ID（HTTP发送接口返回值）',
  `session_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余快照】',
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '知识点，可空【冗余】',
  `user_message` text COLLATE utf8mb4_unicode_ci COMMENT '用户消息',
  `assistant_message` longtext COLLATE utf8mb4_unicode_ci COMMENT 'AI回复（流式完成后落库）',
  `intent` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '意图：EXPLAIN/RECOMMEND/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT',
  `biz_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产物类型，可空：ANIMATION/PICTURE_BOOK/QUIZ/RESOURCE_LIST/CODE',
  `biz_data` longtext COLLATE utf8mb4_unicode_ci COMMENT '产物结构化JSON（卡片数据）',
  `generation_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联生成记录，可空',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0处理中 1完成 2取消 3错误',
  `error_info` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息，可空',
  `prompt_tokens` int NOT NULL DEFAULT '0' COMMENT '输入token用量，默认0',
  `completion_tokens` int NOT NULL DEFAULT '0' COMMENT '输出token用量，默认0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_session_time` (`session_id`,`create_time`),
  KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agent_session`
--

DROP TABLE IF EXISTS `agent_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agent_session` (
  `session_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话标题（首条消息摘要）',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余快照：会话创建时学段】',
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前学习知识点，可空',
  `scene` tinyint NOT NULL DEFAULT '0' COMMENT '场景：0自由对话 1课程引导 2路径引导',
  `message_count` int NOT NULL DEFAULT '0' COMMENT '消息数【冗余：会话列表展示】',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间【冗余：列表排序免max聚合】',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0正常 1归档',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`session_id`),
  KEY `idx_user_time` (`user_id`,`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_generation_record`
--

DROP TABLE IF EXISTS `ai_generation_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_generation_record` (
  `record_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余：预置绘本库按学段过滤】',
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '知识点，可空',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：ANIMATION/PICTURE_BOOK/DRAW/PPT/WORD/CODE',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` longtext COLLATE utf8mb4_unicode_ci COMMENT '结构化内容JSON（SVG分步脚本/绘本分页等）',
  `file_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产物文件地址（Word/PPT/图片）',
  `cover_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面',
  `source` tinyint NOT NULL DEFAULT '0' COMMENT '来源：0学生生成 1管理员预置',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0生成中 1完成 2失败 3已发布',
  `saved` tinyint NOT NULL DEFAULT '0' COMMENT '学生是否已保存到"我的"：0否 1是',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核：0待审核 1通过 2驳回（动画审核流程）',
  `create_by` int DEFAULT NULL COMMENT '管理员预置时记录操作人ID，可空',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_user_type` (`user_id`,`type`),
  KEY `idx_source_status` (`source`,`status`,`stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `animation_template`
--

DROP TABLE IF EXISTS `animation_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `animation_template` (
  `template_id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `template_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动画类型，对应意图：EXPLAIN/CONCEPT/PROCESS等',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR/ALL',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板描述',
  `template_content` longtext COLLATE utf8mb4_unicode_ci COMMENT 'SVG模板JSON，含分步脚本结构',
  `preview_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预览图URL，可空',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1启用',
  `create_by` int DEFAULT NULL COMMENT '创建人（管理员ID）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`template_id`),
  KEY `idx_stage_type_status` (`stage`,`template_type`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动画模板库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_chapter`
--

DROP TABLE IF EXISTS `course_chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_chapter` (
  `chapter_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '章节ID',
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属课程',
  `chapter_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '章节名',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`chapter_id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_chapter_lesson`
--

DROP TABLE IF EXISTS `course_chapter_lesson`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_chapter_lesson` (
  `lesson_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课时ID',
  `chapter_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属章节',
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属课程【冗余：免join章节直查课程课时树】',
  `lesson_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课时名',
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课时摘要',
  `video_duration` int DEFAULT NULL COMMENT '视频时长（秒）【冗余：自主视频资源同步，续播UI免查资源表】',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`lesson_id`),
  KEY `idx_chapter_id` (`chapter_id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_chapter_lesson_resource`
--

DROP TABLE IF EXISTS `course_chapter_lesson_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_chapter_lesson_resource` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lesson_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课时ID',
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程ID【冗余】',
  `resource_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源ID',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_lesson_id` (`lesson_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时资源关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_info`
--

DROP TABLE IF EXISTS `course_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_info` (
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名',
  `cover` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面URL',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余：学生端按学段过滤主筛选键】',
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级',
  `subject` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AI' COMMENT '学科',
  `difficulty` tinyint NOT NULL DEFAULT '1' COMMENT '难度：1-3星',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简介',
  `intro` text COLLATE utf8mb4_unicode_ci COMMENT '详细介绍',
  `lesson_count` int NOT NULL DEFAULT '0' COMMENT '课时总数【冗余：课时增删时同事务维护】',
  `study_count` int NOT NULL DEFAULT '0' COMMENT '学习人数【冗余：学习行为触发计数】',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1上架 0下架',
  `create_by` int DEFAULT NULL COMMENT '创建人（管理员）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`course_id`),
  KEY `idx_stage_status` (`stage`,`status`),
  KEY `idx_grade_status` (`grade`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程（教材）表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_study_lesson_progress`
--

DROP TABLE IF EXISTS `course_study_lesson_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_study_lesson_progress` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程【冗余】',
  `lesson_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课时',
  `play_position` int NOT NULL DEFAULT '0' COMMENT '视频最后播放位置（秒），续播锚点',
  `study_duration` int NOT NULL DEFAULT '0' COMMENT '学习时长（秒）',
  `finished` tinyint NOT NULL DEFAULT '0' COMMENT '0未完成 1已完成',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_lesson` (`user_id`,`lesson_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时学习进度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_study_log`
--

DROP TABLE IF EXISTS `course_study_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_study_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程',
  `lesson_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课时',
  `study_date` date NOT NULL COMMENT '学习日期【冗余：连续打卡/时长统计按天聚合】',
  `duration` int NOT NULL DEFAULT '0' COMMENT '本次时长（秒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`,`study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_study_progress`
--

DROP TABLE IF EXISTS `course_study_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_study_progress` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程',
  `studied_lessons` int NOT NULL DEFAULT '0' COMMENT '已学课时数【冗余：Redis缓冲聚合后异步回写】',
  `total_lessons` int NOT NULL DEFAULT '0' COMMENT '课时总数【冗余快照】',
  `progress` int NOT NULL DEFAULT '0' COMMENT '进度百分比【冗余：列表直读】',
  `study_duration` int NOT NULL DEFAULT '0' COMMENT '累计学习时长（秒）',
  `last_lesson_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近学习课时',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`,`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程学习进度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exam_info`
--

DROP TABLE IF EXISTS `exam_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_info` (
  `exam_id` varchar(15) NOT NULL COMMENT '考试ID',
  `exam_name` varchar(100) NOT NULL COMMENT '考试名称',
  `stage` varchar(20) DEFAULT NULL COMMENT '学段冗余',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级',
  `paper_id` varchar(15) NOT NULL COMMENT '试卷ID',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration_minutes` int NOT NULL DEFAULT '60' COMMENT '考试时长（分钟）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0未发布 1进行中 2已结束',
  `create_by` int DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`exam_id`),
  KEY `idx_grade_status` (`grade`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `knowledge_doc`
--

DROP TABLE IF EXISTS `knowledge_doc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_doc` (
  `doc_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余：与ES metadata双写一致】',
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点【冗余：检索过滤/管理筛选免join】',
  `owner_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'NULL=官方知识库；非空=学生个人知识库',
  `difficulty` tinyint NOT NULL DEFAULT '1' COMMENT '难度：1-3',
  `data_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'KNOWLEDGE' COMMENT '数据类型，默认KNOWLEDGE',
  `content` longtext COLLATE utf8mb4_unicode_ci COMMENT '正文（Markdown）',
  `source_type` tinyint NOT NULL DEFAULT '0' COMMENT '来源：0手动维护 1资料解析',
  `source_resource_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源资源ID（解析入库时回填），可空',
  `source_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资料链接',
  `vector_status` tinyint NOT NULL DEFAULT '0' COMMENT '向量状态：0待处理 1处理中 2已完成 3失败 4过期',
  `vector_error` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '向量化失败时的错误原因，可空',
  `chunk_count` int NOT NULL DEFAULT '0' COMMENT '入库分块数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0下架 1上架',
  `create_by` int DEFAULT NULL COMMENT '维护人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`doc_id`),
  KEY `idx_stage_kp` (`stage`,`knowledge_point_id`),
  KEY `idx_vector_status` (`vector_status`),
  KEY `idx_owner_status` (`owner_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `knowledge_mastery`
--

DROP TABLE IF EXISTS `knowledge_mastery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_mastery` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余：雷达图按学段聚合免join】',
  `mastery_score` int NOT NULL DEFAULT '0' COMMENT '掌握度0-100',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0未解锁 1进行中 2已掌握',
  `practice_count` int NOT NULL DEFAULT '0' COMMENT '练习次数【冗余计数：批改后原子+1】',
  `correct_count` int NOT NULL DEFAULT '0' COMMENT '答对次数【冗余：正确率=correct_count/practice_count】',
  `last_practice_time` datetime DEFAULT NULL COMMENT '最近练习时间',
  `last_master_time` datetime DEFAULT NULL COMMENT '掌握时间（遗忘曲线计时起点）',
  `next_review_time` datetime DEFAULT NULL COMMENT '下次复习时间',
  `review_stage` tinyint NOT NULL DEFAULT '0' COMMENT '遗忘曲线阶段0-4（对应1/3/7/15天间隔）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_kp` (`user_id`,`knowledge_point_id`),
  KEY `idx_user_review` (`user_id`,`next_review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点掌握度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `knowledge_point`
--

DROP TABLE IF EXISTS `knowledge_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_point` (
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点名；同名跨学段多行，(name, stage)逻辑唯一',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段',
  `subject` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AI' COMMENT '学科',
  `difficulty` tinyint NOT NULL DEFAULT '1' COMMENT '难度：1-3',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `cover` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面，可空',
  `lesson_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联课时，可空',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`knowledge_point_id`),
  KEY `idx_stage_subject` (`stage`,`subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点表（领域中心）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `learning_path`
--

DROP TABLE IF EXISTS `learning_path`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_path` (
  `path_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路径ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学习分类/目标名（学生自建或AI命名）',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余快照】',
  `source` tinyint NOT NULL DEFAULT '0' COMMENT '来源：0规则生成 1AI生成',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0进行中 1已完成 2已放弃',
  `total_items` int NOT NULL DEFAULT '0' COMMENT '节点总数【冗余：节点增删时维护】',
  `finished_items` int NOT NULL DEFAULT '0' COMMENT '已完成节点数【冗余】',
  `progress` int NOT NULL DEFAULT '0' COMMENT '进度百分比【冗余：列表直读免聚合】',
  `current_item_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前节点，AI主动引导锚点',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`path_id`),
  KEY `idx_user_status` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习路径表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `learning_path_item`
--

DROP TABLE IF EXISTS `learning_path_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_path_item` (
  `item_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点ID',
  `path_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属路径',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点',
  `knowledge_point_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点名【冗余快照】',
  `branch_type` tinyint NOT NULL DEFAULT '0' COMMENT '0主线 1兴趣分支',
  `branch_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分支名，可空',
  `item_type` tinyint NOT NULL DEFAULT '0' COMMENT '0学习 1复习（遗忘曲线复习节点）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0未解锁 1进行中 2已掌握',
  `due_date` date DEFAULT NULL COMMENT '复习到期日（item_type=1时有效）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`item_id`),
  KEY `idx_path_sort` (`path_id`,`sort`),
  KEY `idx_user_due` (`user_id`,`due_date`,`item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路径节点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message_info`
--

DROP TABLE IF EXISTS `message_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_info` (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `message_type` tinyint NOT NULL DEFAULT '0' COMMENT '类型：0系统消息 1学习提醒',
  `jump_path` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息点击跳转路径，可空',
  `create_by` int DEFAULT NULL COMMENT '发送人（管理员）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message_user`
--

DROP TABLE IF EXISTS `message_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_id` int NOT NULL COMMENT '消息ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `read_status` tinyint NOT NULL DEFAULT '0' COMMENT '0未读 1已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `delete_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1已删除（学生隐藏消息）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper_group`
--

DROP TABLE IF EXISTS `paper_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_group` (
  `group_id` varchar(15) NOT NULL COMMENT '大题ID',
  `paper_id` varchar(15) NOT NULL COMMENT '试卷ID',
  `group_name` varchar(100) NOT NULL COMMENT '大题名称',
  `group_sort` int NOT NULL DEFAULT '0' COMMENT '大题排序',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='试卷大题表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper_info`
--

DROP TABLE IF EXISTS `paper_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_info` (
  `paper_id` varchar(15) NOT NULL COMMENT '试卷ID',
  `paper_name` varchar(100) NOT NULL COMMENT '试卷名称',
  `paper_type` tinyint NOT NULL DEFAULT '0' COMMENT '0练习卷 1考试卷',
  `stage` varchar(20) DEFAULT NULL COMMENT '学段冗余',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级',
  `total_score` int NOT NULL DEFAULT '0' COMMENT '试卷总分',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0下架 1上架',
  `create_by` int DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`paper_id`),
  KEY `idx_grade_status` (`grade`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='试卷表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper_question`
--

DROP TABLE IF EXISTS `paper_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `paper_id` varchar(15) NOT NULL COMMENT '试卷ID',
  `group_id` varchar(15) NOT NULL COMMENT '大题ID',
  `question_id` varchar(15) NOT NULL COMMENT '题目ID',
  `score` int NOT NULL DEFAULT '5' COMMENT '本题分值',
  `sort` int NOT NULL DEFAULT '0' COMMENT '题号排序',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_paper_group` (`paper_id`,`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='试卷题目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `practice_record`
--

DROP TABLE IF EXISTS `practice_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practice_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点【冗余快照：提交时从题目复制】',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余快照：按学段分析免join】',
  `question_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
  `question_type` tinyint NOT NULL COMMENT '题型【冗余快照】',
  `user_answer` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学生作答',
  `is_correct` tinyint NOT NULL DEFAULT '0' COMMENT '0错 1对',
  `score` int NOT NULL DEFAULT '0' COMMENT '得分',
  `duration` int NOT NULL DEFAULT '0' COMMENT '用时（秒）',
  `source` tinyint NOT NULL DEFAULT '0' COMMENT '来源：0对话练习 1路径快测 2遗忘复习',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_user_kp` (`user_id`,`knowledge_point_id`),
  KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏化练习记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prompt_template`
--

DROP TABLE IF EXISTS `prompt_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prompt_template` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段；ALL表示通用模板',
  `scene` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景/意图：EXPLAIN/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT等',
  `template_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词内容（必须含"知识库无相关内容时如实说明，不要编造"类约束）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1启用',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stage_scene` (`stage`,`scene`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question_info`
--

DROP TABLE IF EXISTS `question_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_info` (
  `question_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点【冗余：出题/练习主筛选键】',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学段【冗余：按学段抽题免join】',
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级',
  `difficulty` tinyint NOT NULL DEFAULT '1' COMMENT '难度：1-3',
  `question_type` tinyint NOT NULL COMMENT '题型：0单选 1多选 2判断 3填空',
  `title` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干',
  `question_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '题目配图，关联resource_info.resource_id，多个逗号分隔，可空',
  `answer` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '判断/填空答案；选择题答案在选项表',
  `analysis` text COLLATE utf8mb4_unicode_ci COMMENT '解析',
  `source` tinyint NOT NULL DEFAULT '0' COMMENT '来源：0管理员录入 1AI生成',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核：0待审核 1已上架 2已驳回',
  `score` int NOT NULL DEFAULT '5' COMMENT '默认分值',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1启用',
  `create_by` int DEFAULT NULL COMMENT '录入人，可空（AI生成为空）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`question_id`),
  KEY `idx_kp_audit` (`knowledge_point_id`,`audit_status`),
  KEY `idx_stage_diff` (`stage`,`difficulty`),
  KEY `idx_grade_diff` (`grade`,`difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question_option`
--

DROP TABLE IF EXISTS `question_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_option` (
  `option_id` int NOT NULL AUTO_INCREMENT COMMENT '选项ID',
  `question_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
  `option_label` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '选项标号：A/B/C/D',
  `option_content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '选项内容',
  `is_answer` tinyint NOT NULL DEFAULT '0' COMMENT '0否 1是',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`option_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目选项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_directory`
--

DROP TABLE IF EXISTS `resource_directory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resource_directory` (
  `dir_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目录ID',
  `dir_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目录名称',
  `parent_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '上级目录ID，0表示顶级目录',
  `dir_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目录类型：raw/wiki/attachments 系统目录；NULL=普通目录',
  `owner_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归属用户ID；NULL=管理端公共目录',
  `sort` int NOT NULL DEFAULT '0' COMMENT '同级排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dir_id`),
  KEY `idx_parent_sort` (`parent_id`,`sort`),
  KEY `idx_owner_parent` (`owner_id`,`parent_id`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源目录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_info`
--

DROP TABLE IF EXISTS `resource_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resource_info` (
  `resource_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源ID',
  `resource_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源名',
  `resource_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源标签，多个逗号分隔',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源简介',
  `file_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件地址',
  `file_size` bigint DEFAULT '0' COMMENT '文件大小（字节）',
  `cover` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面',
  `duration` int DEFAULT NULL COMMENT '音视频时长（秒）',
  `hls_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'HLS转码产物地址',
  `ext_json` text COLLATE utf8mb4_unicode_ci COMMENT '产物扩展信息（动画步骤/绘本分页/播放配置等）',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归属学段，可空',
  `knowledge_point_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联知识点【冗余：recommendResource工具按知识点直查】',
  `directory_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属资源目录ID',
  `source` tinyint NOT NULL DEFAULT '0' COMMENT '来源：0后台上传 1AI生成',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0处理中 1可用 2失败',
  `create_by` int DEFAULT NULL COMMENT '上传人',
  `owner_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归属用户ID；NULL=管理端公共资源',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`resource_id`),
  KEY `idx_type_status` (`resource_type`,`status`),
  KEY `idx_knowledge_point_id` (`knowledge_point_id`),
  KEY `idx_directory_id` (`directory_id`),
  KEY `idx_owner_status` (`owner_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `student_learning_record`
--

DROP TABLE IF EXISTS `student_learning_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_learning_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生',
  `resource_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源ID，可空',
  `course_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程ID，可空',
  `lesson_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课时ID，可空',
  `action_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'VIEW/COMPLETE/PRACTICE/ANIMATION/PARSE',
  `duration` int NOT NULL DEFAULT '0' COMMENT '时长（秒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_user_time` (`user_id`,`create_time`),
  KEY `idx_user_type` (`user_id`,`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生学习行为记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_group` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组：AI_MODEL/RAG/PYODIDE/SYSTEM/SECURITY',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值，支持字符串/JSON',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STRING' COMMENT '值类型：STRING/INT/FLOAT/BOOLEAN/JSON',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置说明',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_group_key` (`config_group`,`config_key`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统全局配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_menu`
--

DROP TABLE IF EXISTS `system_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_menu` (
  `menu_id` int NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` int NOT NULL DEFAULT '0' COMMENT '父菜单ID，0为根',
  `menu_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名',
  `menu_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码，与管理端权限注解一一对应',
  `menu_type` tinyint NOT NULL COMMENT '类型：0目录 1菜单 2按钮',
  `path` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端路由路径',
  `icon` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`menu_id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_notice`
--

DROP TABLE IF EXISTS `system_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0草稿 1已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_role_menu`
--

DROP TABLE IF EXISTS `system_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_role_menu` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_type` tinyint NOT NULL COMMENT '角色：0管理员（学生端无菜单权限，不进此表）',
  `menu_id` int NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_type` (`role_type`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info` (
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱，登录核心字段，可空（管理员可不填）',
  `password` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（MD5存储）',
  `nick_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `role_type` tinyint NOT NULL DEFAULT '1' COMMENT '角色：0管理员 1学生',
  `stage` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR；学生必填，管理员为空',
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级（如三年级）',
  `interests` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '兴趣标签，JSON数组字符串',
  `learning_style_tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学习风格标签JSON【冗余：规则引擎按行为周期计算后落地】',
  `sex` tinyint DEFAULT '2' COMMENT '性别：0女 1男 2保密',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_wiki_profile`
--

DROP TABLE IF EXISTS `user_wiki_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_wiki_profile` (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生ID',
  `learning_goal` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学习目标',
  `key_questions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键问题（多个用分号分隔）',
  `interest_subjects` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '感兴趣学科/主题（多个用分号分隔）',
  `alias_terms` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自己的术语叫法（多个用分号分隔）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生学习档案（个人Wiki用户视图）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'nexora'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-22 13:47:51
