# Nexora 数据库设计（sql.md）

> 本文档是 Nexora 的表结构设计稿，评审通过后据此生成 `nexora-java/nexora.sql`（版本化 DDL，冻结后禁止修改）。
> 设计原则：**知识点为领域中心**；角色仅管理员 + 学生；无教师 / 班级 / 考试 / 试卷；查询主路径适当冗余，写路径保证一致。

## 1、设计约定

- 引擎 InnoDB，字符集 utf8mb4（mb4 兼容 emoji，低龄学生 UI 需要）。
- 主键策略：业务实体 `varchar(32)` UUID（课程 / 章节 / 课时 / 资源 / 知识点 / 文档 / 题目 / 会话 / 消息 / 路径 / 生成记录）；基础数据 `int` 自增（用户 / 菜单 / 通知 / 系统配置 / 动画模板）；高频写入的记录型大表 `bigint` 自增（practice_record / course_study_log）。
- 公共字段：`create_time` / `update_time` datetime；序列化统一 `yyyy-MM-dd HH:mm:ss`（GMT+8）。
- 状态 / 枚举字段统一 `tinyint` 或约定字符串，取值在"说明"列注明；前后端枚举值保持一致，禁止自造同义词。
- 学段统一 `varchar(20)`：`PRIMARY_LOW` / `PRIMARY_HIGH` / `JUNIOR` / `SENIOR`。
- 冗余字段在说明列标注【冗余】，并注明一致性维护方式；总原则见文末"冗余设计说明"。

## 2、表清单总览（28 张）

| 分组 | 表 |
|---|---|
| 用户与权限 | user_info、system_menu、system_role_menu |
| 消息通知 | message_info、message_user、system_notice |
| 课程与资源 | course_info、course_chapter、course_chapter_lesson、course_chapter_lesson_resource、resource_info |
| 知识点与知识库 | knowledge_point、knowledge_doc |
| 题库与练习 | question_info、question_option、practice_record |
| 学习数据 | course_study_progress、course_study_lesson_progress、course_study_log |
| 对话智能体 | agent_session、agent_message |
| 多模态生成域 | ai_generation_record、animation_template |
| 个性化路径 | learning_path、learning_path_item、knowledge_mastery |
| 智能体配置域 | prompt_template、system_config |

## 3、用户与权限

### user_info 用户表

| 字段 | 类型 | 说明 |
|---|---|---|
| user_id | int PK AI | 用户 ID |
| username | varchar(32) | 登录名，唯一 |
| email | varchar(100) | 邮箱，登录核心字段，可空（管理员可不填） |
| password | varchar(64) | 密码（MD5 存储） |
| nick_name | varchar(32) | 昵称 |
| avatar | varchar(255) | 头像 URL |
| role_type | tinyint | 角色：0 管理员 / 1 学生 |
| stage | varchar(20) | 学段【冗余核心：全局自适应开关】；学生必填，管理员为空 |
| grade | varchar(20) | 年级（如"三年级"），可空 |
| interests | varchar(500) | 兴趣标签，JSON 数组字符串 |
| learning_style_tags | varchar(500) | 学习风格标签 JSON【冗余：规则引擎按行为周期计算后落地，读侧免实时聚合】 |
| sex | tinyint | 0 女 / 1 男 / 2 保密 |
| status | tinyint | 0 禁用 / 1 启用 |
| last_login_time | datetime | 最后登录时间 |
| create_time / update_time | datetime | 公共字段 |

唯一索引：`(email)`（MySQL 唯一索引允许多个 NULL，管理员无邮箱时不冲突）。

### system_menu 系统菜单表

| 字段 | 类型 | 说明 |
|---|---|---|
| menu_id | int PK AI | 菜单 ID |
| parent_id | int | 父菜单，0 为根 |
| menu_name | varchar(50) | 菜单名 |
| menu_code | varchar(50) | 权限编码，唯一；与管理端权限注解一一对应 |
| menu_type | tinyint | 0 目录 / 1 菜单 / 2 按钮 |
| path | varchar(100) | 前端路由路径 |
| icon | varchar(50) | 图标 |
| sort | int | 排序 |
| status | tinyint | 0 停用 / 1 启用 |
| create_time / update_time | datetime | 公共字段 |

### system_role_menu 角色菜单关联表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int PK AI | 主键 |
| role_type | tinyint | 角色：0 管理员（学生端无菜单权限，不进此表） |
| menu_id | int | 菜单 ID |
| create_time | datetime | 创建时间 |

## 4、消息通知

### message_info 消息主表

| 字段 | 类型 | 说明 |
|---|---|---|
| message_id | int PK AI | 消息 ID |
| title | varchar(200) | 标题 |
| content | text | 内容 |
| message_type | tinyint | 0 系统消息 / 1 学习提醒 |
| jump_path | varchar(200) | 消息点击跳转路径，可空（学习提醒跳转课时 / 路径节点） |
| create_by | int | 发送人（管理员） |
| create_time | datetime | 创建时间 |

### message_user 用户消息关联表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int PK AI | 主键 |
| message_id | int | 消息 ID |
| user_id | int | 接收人 |
| read_status | tinyint | 0 未读 / 1 已读 |
| read_time | datetime | 阅读时间 |
| delete_flag | tinyint | 0 正常 / 1 已删除（学生隐藏消息） |
| create_time | datetime | 创建时间 |

索引：`(user_id, read_status)`。

### system_notice 系统通知表

| 字段 | 类型 | 说明 |
|---|---|---|
| notice_id | int PK AI | 通知 ID |
| title | varchar(200) | 标题 |
| content | text | 内容 |
| status | tinyint | 0 草稿 / 1 已发布 |
| publish_time | datetime | 发布时间 |
| create_time / update_time | datetime | 公共字段 |

## 5、课程与资源

### course_info 课程（教材）表

| 字段 | 类型 | 说明 |
|---|---|---|
| course_id | varchar(32) PK | 课程 ID（UUID） |
| course_name | varchar(100) | 课程名 |
| cover | varchar(255) | 封面 URL |
| stage | varchar(20) | 学段【冗余：学生端按学段过滤的主筛选键】 |
| subject | varchar(20) | 学科，默认 `AI` |
| difficulty | tinyint | 难度：1-3 星 |
| description | varchar(500) | 简介 |
| intro | text | 详细介绍 |
| lesson_count | int | 课时总数【冗余：课时增删时同事务维护，课程列表免聚合】 |
| study_count | int | 学习人数【冗余：学习行为触发计数】 |
| sort | int | 排序 |
| status | tinyint | 1 上架 / 0 下架（与全局 status=1 启用语义一致） |
| create_by | int | 创建人（管理员） |
| create_time / update_time | datetime | 公共字段 |

索引：`(stage, status)`。

### course_chapter 章节表

| 字段 | 类型 | 说明 |
|---|---|---|
| chapter_id | varchar(32) PK | 章节 ID |
| course_id | varchar(32) | 所属课程 |
| chapter_name | varchar(100) | 章节名 |
| sort | int | 排序 |
| status | tinyint | 0 正常 / 1 停用 |
| create_time / update_time | datetime | 公共字段 |

索引：`(course_id)`。

### course_chapter_lesson 课时表

| 字段 | 类型 | 说明 |
|---|---|---|
| lesson_id | varchar(32) PK | 课时 ID |
| chapter_id | varchar(32) | 所属章节 |
| course_id | varchar(32) | 所属课程【冗余：免 join 章节直查课程课时树】 |
| lesson_name | varchar(100) | 课时名 |
| summary | varchar(500) | 课时摘要 |
| video_duration | int | 视频时长（秒）【冗余：自主视频资源同步，续播 UI 免查资源表】 |
| sort | int | 排序 |
| status | tinyint | 0 正常 / 1 停用 |
| create_time / update_time | datetime | 公共字段 |

索引：`(chapter_id)`、`(course_id)`。

### course_chapter_lesson_resource 课时资源关联表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int PK AI | 主键 |
| lesson_id | varchar(32) | 课时 ID |
| course_id | varchar(32) | 课程 ID【冗余】 |
| resource_id | varchar(32) | 资源 ID |
| sort | int | 排序 |
| create_time | datetime | 创建时间 |

索引：`(lesson_id)`。

### resource_info 资源信息表

| 字段 | 类型 | 说明 |
|---|---|---|
| resource_id | varchar(32) PK | 资源 ID |
| resource_name | varchar(200) | 资源名 |
| resource_type | varchar(20) | VIDEO / DOCUMENT / PPT / WORD / IMAGE / PICTURE_BOOK |
| tags | varchar(500) | 资源标签，多个逗号分隔 |
| description | varchar(500) | 资源简介 |
| file_path | varchar(255) | 文件地址 |
| file_size | bigint | 文件大小（字节） |
| cover | varchar(255) | 封面 |
| duration | int | 音视频时长（秒） |
| hls_path | varchar(255) | HLS 转码产物地址 |
| stage | varchar(20) | 归属学段，可空 |
| knowledge_point_id | varchar(32) | 关联知识点，可空【冗余：`recommendResource` 工具按知识点直查】 |
| source | tinyint | 0 后台上传 / 1 AI 生成 |
| status | tinyint | 0 处理中 / 1 可用 / 2 失败 |
| create_by | int | 上传人 |
| create_time / update_time | datetime | 公共字段 |

索引：`(resource_type, status)`、`(knowledge_point_id)`。

## 6、知识点与知识库

### knowledge_point 知识点表（领域中心）

| 字段 | 类型 | 说明 |
|---|---|---|
| knowledge_point_id | varchar(32) PK | 知识点 ID |
| name | varchar(100) | 知识点名；同名跨学段多行（如"排序算法"小高 / 初中 / 高中各一行，难度不同），`(name, stage)` 逻辑唯一 |
| stage | varchar(20) | 学段 |
| subject | varchar(20) | 学科，默认 `AI` |
| difficulty | tinyint | 难度：1-3 |
| description | varchar(500) | 描述 |
| cover | varchar(255) | 封面，可空 |
| lesson_id | varchar(32) | 关联课时，可空 |
| sort | int | 排序 |
| status | tinyint | 0 停用 / 1 启用 |
| create_time / update_time | datetime | 公共字段 |

索引：`(stage, subject)`。

### knowledge_doc 知识库文档表

| 字段 | 类型 | 说明 |
|---|---|---|
| doc_id | varchar(32) PK | 文档 ID |
| title | varchar(200) | 标题 |
| stage | varchar(20) | 学段【冗余：与 ES metadata 双写一致，重新入库时同步】 |
| knowledge_point_id | varchar(32) | 知识点【冗余：检索过滤 / 管理筛选免 join】 |
| difficulty | tinyint | 难度：1-3 |
| data_type | varchar(20) | 数据类型，默认 `KNOWLEDGE`；字段命名稳定，禁止改名 |
| content | longtext | 正文（Markdown） |
| source_type | tinyint | 0 手动维护 / 1 资料解析 |
| source_resource_id | varchar(32) | 来源资源 ID（解析入库时回填），可空 |
| vector_status | tinyint | 向量状态：0 待处理 / 1 处理中 / 2 已完成 / 3 失败 / 4 过期 |
| vector_error | varchar(500) | 向量化失败时的错误原因，可空 |
| chunk_count | int | 入库分块数 |
| status | tinyint | 0 下架 / 1 上架 |
| create_by | int | 维护人 |
| create_time / update_time | datetime | 公共字段 |

索引：`(stage, knowledge_point_id)`、`(vector_status)`。

## 7、题库与练习

### question_info 题目表

| 字段 | 类型 | 说明 |
|---|---|---|
| question_id | varchar(32) PK | 题目 ID |
| knowledge_point_id | varchar(32) | 知识点【冗余：出题 / 练习主筛选键】 |
| stage | varchar(20) | 学段【冗余：按学段抽题免 join】 |
| difficulty | tinyint | 难度：1-3 |
| question_type | tinyint | 0 单选 / 1 多选 / 2 判断 / 3 填空 |
| title | text | 题干 |
| question_image | varchar(500) | 题目配图，关联 resource_info.resource_id，多个逗号分隔，可空（K12 低年级看图题） |
| answer | varchar(500) | 判断 / 填空答案；选择题答案在选项表 |
| analysis | text | 解析 |
| source | tinyint | 0 管理员录入 / 1 AI 生成 |
| audit_status | tinyint | 审核：0 待审核 / 1 已上架 / 2 已驳回；AI 出题落库必须为 0 |
| score | int | 默认分值，默认 5 |
| status | tinyint | 0 停用 / 1 启用 |
| create_by | int | 录入人，可空（AI 生成为空） |
| create_time / update_time | datetime | 公共字段 |

索引：`(knowledge_point_id, audit_status)`、`(stage, difficulty)`。

### question_option 题目选项表

| 字段 | 类型 | 说明 |
|---|---|---|
| option_id | int PK AI | 选项 ID |
| question_id | varchar(32) | 题目 ID |
| option_label | varchar(8) | 选项标号：A / B / C / D |
| option_content | varchar(500) | 选项内容 |
| is_answer | tinyint | 0 否 / 1 是 |
| sort | int | 排序 |
| create_time | datetime | 创建时间 |

索引：`(question_id)`。

### practice_record 游戏化练习记录表

| 字段 | 类型 | 说明 |
|---|---|---|
| record_id | bigint PK AI | 记录 ID |
| user_id | int | 学生 |
| knowledge_point_id | varchar(32) | 知识点【冗余快照：提交时从题目复制，掌握度聚合免 join】 |
| stage | varchar(20) | 学段【冗余快照：按学段分析免 join】 |
| question_id | varchar(32) | 题目 ID |
| question_type | tinyint | 题型【冗余快照：题型表现统计 → 学习风格标签】 |
| user_answer | varchar(500) | 学生作答 |
| is_correct | tinyint | 0 错 / 1 对 |
| score | int | 得分 |
| duration | int | 用时（秒） |
| source | tinyint | 来源：0 对话练习 / 1 路径快测 / 2 遗忘复习 |
| create_time | datetime | 创建时间 |

索引：`(user_id, knowledge_point_id)`、`(user_id, create_time)`。

## 8、学习数据

### course_study_progress 课程学习进度表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int PK AI | 主键 |
| user_id | int | 学生 |
| course_id | varchar(32) | 课程 |
| studied_lessons | int | 已学课时数【冗余：Redis 缓冲聚合后异步回写】 |
| total_lessons | int | 课时总数【冗余快照：课时增删时刷新】 |
| progress | int | 进度百分比【冗余：列表直读】 |
| study_duration | int | 累计学习时长（秒） |
| last_lesson_id | varchar(32) | 最近学习课时 |
| finish_time | datetime | 完成时间 |
| create_time / update_time | datetime | 公共字段 |

唯一索引：`(user_id, course_id)`。

### course_study_lesson_progress 课时学习进度表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int PK AI | 主键 |
| user_id | int | 学生 |
| course_id | varchar(32) | 课程【冗余】 |
| lesson_id | varchar(32) | 课时 |
| play_position | int | 视频最后播放位置（秒），续播锚点 |
| study_duration | int | 学习时长（秒） |
| finished | tinyint | 0 未完成 / 1 已完成 |
| finish_time | datetime | 完成时间 |
| create_time / update_time | datetime | 公共字段 |

唯一索引：`(user_id, lesson_id)`。

### course_study_log 学习日志表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint PK AI | 主键 |
| user_id | int | 学生 |
| course_id | varchar(32) | 课程 |
| lesson_id | varchar(32) | 课时 |
| study_date | date | 学习日期【冗余：连续打卡 / 时长统计按天聚合，免函数索引】 |
| duration | int | 本次时长（秒） |
| create_time | datetime | 创建时间 |

索引：`(user_id, study_date)`。

## 9、对话智能体

### agent_session AI 会话表

| 字段 | 类型 | 说明 |
|---|---|---|
| session_id | varchar(32) PK | 会话 ID |
| user_id | int | 学生 |
| title | varchar(100) | 会话标题（首条消息摘要） |
| stage | varchar(20) | 学段【冗余快照：会话创建时学段，学生后续切换学段不影响旧会话】 |
| knowledge_point_id | varchar(32) | 当前学习知识点，可空 |
| scene | tinyint | 场景：0 自由对话 / 1 课程引导 / 2 路径引导 |
| message_count | int | 消息数【冗余：会话列表展示】 |
| last_message_time | datetime | 最后消息时间【冗余：列表排序免 max 聚合】 |
| status | tinyint | 0 正常 / 1 归档 |
| create_time / update_time | datetime | 公共字段 |

索引：`(user_id, last_message_time)`。

### agent_message AI 消息表

| 字段 | 类型 | 说明 |
|---|---|---|
| message_id | varchar(32) PK | 消息 ID（HTTP 发送接口返回值） |
| session_id | varchar(32) | 会话 ID |
| user_id | int | 学生【冗余：学习分析免 join 会话表】 |
| stage | varchar(20) | 学段【冗余快照】 |
| knowledge_point_id | varchar(32) | 知识点，可空【冗余】 |
| user_message | text | 用户消息 |
| assistant_message | longtext | AI 回复（流式完成后落库） |
| intent | varchar(20) | 意图：EXPLAIN / RECOMMEND / QUIZ / PICTURE_BOOK / DRAW / ANIMATION / CODING / PLAN / PROGRESS / CHAT |
| biz_type | varchar(20) | 产物类型，可空：ANIMATION / PICTURE_BOOK / QUIZ / RESOURCE_LIST / CODE |
| biz_data | longtext | 产物结构化 JSON（卡片数据） |
| generation_id | varchar(32) | 关联生成记录，可空 |
| status | tinyint | 0 处理中 / 1 完成 / 2 取消 / 3 错误 |
| error_info | varchar(500) | 错误信息，可空 |
| prompt_tokens | int | 输入 token 用量，默认 0 |
| completion_tokens | int | 输出 token 用量，默认 0（项目报告大模型调用策略数据支撑） |
| create_time / update_time | datetime | 公共字段 |

索引：`(session_id, create_time)`、`(user_id, create_time)`。

## 10、多模态生成域

### ai_generation_record AI 生成记录表

| 字段 | 类型 | 说明 |
|---|---|---|
| record_id | varchar(32) PK | 记录 ID |
| user_id | int | 学生，可空（管理员预置无学生） |
| stage | varchar(20) | 学段【冗余：预置绘本库按学段过滤】 |
| knowledge_point_id | varchar(32) | 知识点，可空 |
| type | varchar(20) | ANIMATION / PICTURE_BOOK / DRAW / PPT / WORD / CODE |
| title | varchar(200) | 标题 |
| content | longtext | 结构化内容 JSON（SVG 分步脚本 / 绘本分页等） |
| file_url | varchar(255) | 产物文件地址（Word / PPT / 图片） |
| cover_url | varchar(255) | 封面 |
| source | tinyint | 0 学生生成 / 1 管理员预置 |
| status | tinyint | 0 生成中 / 1 完成 / 2 失败 / 3 已发布 |
| saved | tinyint | 学生是否已保存到"我的"：0 否 / 1 是 |
| audit_status | tinyint | 审核：0 待审核 / 1 通过 / 2 驳回（动画审核流程） |
| create_by | int | 管理员预置时记录操作人 ID，可空 |
| create_time / update_time | datetime | 公共字段 |

索引：`(user_id, type)`、`(source, status, stage)`。

### animation_template 动画模板库

| 字段 | 类型 | 说明 |
|---|---|---|
| template_id | int PK AI | 模板 ID |
| template_name | varchar(100) | 模板名称 |
| template_type | varchar(30) | 动画类型，对应意图：EXPLAIN / CONCEPT / PROCESS 等 |
| stage | varchar(20) | 学段：PRIMARY_LOW / PRIMARY_HIGH / JUNIOR / SENIOR / ALL |
| description | varchar(500) | 模板描述 |
| template_content | longtext | SVG 模板 JSON，含分步脚本结构 |
| preview_url | varchar(500) | 预览图 URL，可空 |
| status | tinyint | 0 停用 / 1 启用 |
| create_by | int | 创建人（管理员 ID） |
| create_time / update_time | datetime | 公共字段 |

索引：`(stage, template_type, status)`。

## 11、个性化路径

### learning_path 学习路径表

| 字段 | 类型 | 说明 |
|---|---|---|
| path_id | varchar(32) PK | 路径 ID |
| user_id | int | 学生 |
| title | varchar(100) | 学习分类 / 目标名（学生自建或 AI 命名） |
| stage | varchar(20) | 学段【冗余快照】 |
| source | tinyint | 0 规则生成 / 1 AI 生成 |
| status | tinyint | 0 进行中 / 1 已完成 / 2 已放弃 |
| total_items | int | 节点总数【冗余：节点增删时维护】 |
| finished_items | int | 已完成节点数【冗余】 |
| progress | int | 进度百分比【冗余：列表直读免聚合】 |
| current_item_id | varchar(32) | 当前节点，AI 主动引导锚点 |
| create_time / update_time | datetime | 公共字段 |

索引：`(user_id, status)`。

### learning_path_item 路径节点表

| 字段 | 类型 | 说明 |
|---|---|---|
| item_id | varchar(32) PK | 节点 ID |
| path_id | varchar(32) | 所属路径 |
| user_id | int | 学生【冗余：到期复习直查免 join 路径表】 |
| knowledge_point_id | varchar(32) | 知识点 |
| knowledge_point_name | varchar(100) | 知识点名【冗余快照：知识点改名 / 删除不影响历史路径】 |
| branch_type | tinyint | 0 主线 / 1 兴趣分支 |
| branch_name | varchar(50) | 分支名，可空 |
| item_type | tinyint | 0 学习 / 1 复习（遗忘曲线复习节点） |
| status | tinyint | 0 未解锁 / 1 进行中 / 2 已掌握 |
| due_date | date | 复习到期日（item_type=1 时有效） |
| sort | int | 排序 |
| finish_time | datetime | 完成时间 |
| create_time / update_time | datetime | 公共字段 |

索引：`(path_id, sort)`、`(user_id, due_date, item_type)`。

### knowledge_mastery 知识点掌握度表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int PK AI | 主键 |
| user_id | int | 学生 |
| knowledge_point_id | varchar(32) | 知识点 |
| stage | varchar(20) | 学段【冗余：雷达图按学段聚合免 join】 |
| mastery_score | int | 掌握度 0-100 |
| status | tinyint | 0 未解锁 / 1 进行中 / 2 已掌握 |
| practice_count | int | 练习次数【冗余计数：批改后原子 +1】 |
| correct_count | int | 答对次数【冗余：正确率 = correct_count / practice_count，免聚合】 |
| last_practice_time | datetime | 最近练习时间 |
| last_master_time | datetime | 掌握时间（遗忘曲线计时起点） |
| next_review_time | datetime | 下次复习时间 |
| review_stage | tinyint | 遗忘曲线阶段 0-4（对应 1 / 3 / 7 / 15 天间隔） |
| create_time / update_time | datetime | 公共字段 |

唯一索引：`(user_id, knowledge_point_id)`；索引：`(user_id, next_review_time)`。

## 12、智能体配置域

### prompt_template 提示词模板表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int PK AI | 主键 |
| stage | varchar(20) | 学段；`ALL` 表示通用模板 |
| scene | varchar(20) | 场景 / 意图：EXPLAIN / QUIZ / PICTURE_BOOK / DRAW / ANIMATION / CODING / PLAN / PROGRESS / PROACTIVE（主动引导）/ RECOMMEND_FOLLOW（推荐追问）等 |
| template_name | varchar(50) | 模板名 |
| content | text | 提示词内容（必须含"知识库无相关内容时如实说明，不要编造"类约束） |
| status | tinyint | 0 停用 / 1 启用 |
| remark | varchar(200) | 备注 |
| create_time / update_time | datetime | 公共字段 |

唯一索引：`(stage, scene)`；读取顺序：Redis 覆盖 → 本表 → 枚举默认值。

### system_config 系统全局配置表

| 字段 | 类型 | 说明 |
|---|---|---|
| config_id | int PK AI | 配置 ID |
| config_group | varchar(50) | 分组：AI_MODEL / RAG / PYODIDE / SYSTEM / SECURITY |
| config_key | varchar(100) | 配置键，如 text_image_model / embedding_model / top_k / similarity_threshold |
| config_value | text | 配置值，支持字符串 / JSON |
| config_type | varchar(20) | 值类型：STRING / INT / FLOAT / BOOLEAN / JSON，默认 STRING |
| description | varchar(200) | 配置说明 |
| status | tinyint | 0 停用 / 1 启用 |
| create_time / update_time | datetime | 公共字段 |

唯一索引：`(config_group, config_key)`；索引：`(status)`。

## 13、冗余设计说明

冗余只为查询主路径服务，每条都有明确的一致性维护方式：

| 冗余字段 | 维护方式 |
|---|---|
| user_info.stage / learning_style_tags | stage 注册与"我的"页切换时更新；风格标签由规则引擎按 practice_record / study_log 周期计算后落地 |
| course_chapter_lesson.course_id、lesson_resource.course_id | 课时不跨课程移动，创建时写入，课程删除时级联清理 |
| course_info.lesson_count / study_count | 课时增删、学生开始学习时同事务维护 |
| study_progress.studied_lessons / total_lessons / progress | 沿用 smart-campus 的 Redis 缓冲聚合后异步回写链路 |
| agent_session.last_message_time / message_count | 消息落库时同步更新 |
| agent_message.user_id / stage / knowledge_point_id | 写入时从会话快照复制，之后不随 user_info 变更（历史分析口径稳定） |
| practice_record.knowledge_point_id / stage / question_type | 提交答案时从题目快照；题目后续修改不影响历史作答统计 |
| learning_path_item.knowledge_point_name / user_id | 创建路径时快照 |
| learning_path.total_items / finished_items / progress | 节点状态变更时维护 |
| knowledge_mastery.practice_count / correct_count | 批改后原子自增（`update set count = count + 1`），禁止先查后改 |
| knowledge_doc.stage / knowledge_point_id / difficulty | 与 ES 向量 metadata 双写；重新入库时以本表为准同步 |
| knowledge_doc.vector_status / vector_error | 本地表追踪向量化管线状态（待处理 / 处理中 / 已完成 / 失败 / 过期）；ES 侧无对应字段，状态变更时仅更新本表 |

## 14、衍生数据不建表（约定）

- 连续打卡天数：`course_study_log.study_date` 去重后连续计算。
- 学习时长统计：`course_study_log` 按 `study_date` 聚合。
- 正确率：`knowledge_mastery.correct_count / practice_count`。
- AI 推荐路径 vs 实际路径对比：`learning_path_item`（sort / status）对照 `practice_record` / `course_study_lesson_progress` 时间线。
- 大模型 token 用量统计：`agent_message.prompt_tokens / completion_tokens` 按用户 / 学段 / 意图聚合。

## 15、初始数据清单（生成 nexora.sql 时落实）

- 4 学段示例 AI 通识课程（课程 + 章节 + 课时骨架）。
- 知识点库：什么是 AI / 机器学习 / 神经网络 / 计算机视觉 / 自然语言处理 / 排序算法 / Python 编程基础 / AI 伦理等，按学段 × 难度区分。
- system_menu / system_role_menu：管理端菜单与权限编码。
- prompt_template：各学段 × 各意图的默认提示词。
- system_config 默认配置项：
  - AI_MODEL 组：`text_image_model` = `wanx-v1`、`embedding_model` = `text-embedding-v4`
  - RAG 组：`top_k` = `5`（INT）、`similarity_threshold` = `0.7`（FLOAT）
  - PYODIDE 组：`pyodide_packages` = `pandas,numpy,matplotlib`（STRING）
  - SYSTEM 组：`intent_route_rules` = JSON（意图路由规则）
  - SECURITY 组：`content_safety_enabled` = `true`（BOOLEAN）
- animation_template 默认模板（2-3 个示例 SVG 动画模板，覆盖 EXPLAIN / CONCEPT 意图 × ALL 学段）。
- 示例知识文档（与 `knowledge/` 目录源文件对应）。
- 演示账号（密码均为 123456 MD5）：
  - 管理员：`admin@nexora.com`
  - 学生（小学低）：`student_low@nexora.com`
  - 学生（小学高）：`student_high@nexora.com`
  - 学生（初中）：`student_junior@nexora.com`
  - 学生（高中）：`student_senior@nexora.com`
