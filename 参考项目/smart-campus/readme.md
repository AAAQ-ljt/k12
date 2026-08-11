# Smart Campus 智慧校园在线学习平台

## 一、项目简介

**Smart Campus** 是一套面向高校的在线学习平台，覆盖「教学管理 + 课程学习 + 在线考试 + 学习分析」全链路业务。采用 **前后端分离 + 服务隔离** 架构，后端拆分为「管理端」和「用户端」两个独立服务，前端对应「管理后台」和「学生端」两个独立工程，两端鉴权、登录态、接口路径完全隔离，互不复用。

平台面向三类角色：

- **系统管理员**：维护基础数据（院系/专业/班级/用户）、教学资源、教学业务全流程。
- **教师**：维护自己名下的课程、章节课时、试卷、考试、作业评估。
- **学生**：在线学习课程、参加考试、提交作业、查看个人学习数据。

------

## 二、技术栈

### 后端 (`smart-campus-java/`)

| 类别                 | 技术 / 版本                                             |
| -------------------- | ------------------------------------------------------- |
| 语言 / 运行时        | Java 21                                                 |
| 框架                 | Spring Boot 3.5.4                                       |
| Web / AOP / 校验     | spring-boot-starter-web / aop / validation / actuator   |
| 持久层               | MyBatis 3.0.5（自定义泛型 Mapper，**非 MyBatis Plus**） |
| 数据库               | MySQL 8.3                                               |
| 缓存 / 登录态 / 队列 | Redis + Redisson 4.0                                    |
| JSON                 | Fastjson2 2.0.58                                        |
| 网络                 | OkHttp 4.12、Netty 4.1                                  |
| 工具集               | commons-lang3、commons-codec、commons-io                |
| 验证码               | easy-captcha 1.6.2                                      |
| Office 处理          | Apache POI 5.2.5（导入导出）                            |
| 构建                 | Maven 多模块                                            |

**模块结构**：

复制代码

```
smart-campus-java/
├── smart-campus-common      公共模块  com.smart.campus.*
│   ├── entity (po/dto/vo/query/enums/constants)
│   ├── service / service.impl
│   ├── mappers (+ resources/mappers/*.xml)
│   ├── redis  (登录态 / 学习进度 / 上传 session / 资源处理队列)
│   ├── exception / utils / config
├── smart-campus-admin       管理端服务  com.smart.admin.*  (独立启动)
│   ├── controller / biz / annotation(权限) / task / config
└── smart-campus-web         用户端服务  com.smart.web.*    (独立启动)
    ├── controller / biz / entity(端专用 dto/vo) / task / config
```

依赖方向：`admin → common`、`web → common`，common 不反向依赖任何端模块。

### 前端 (`smart-campus-front/`)

| 类别               | 技术 / 版本                                |
| ------------------ | ------------------------------------------ |
| 框架               | Vue 3.5 + JavaScript                       |
| 构建               | Vite 8                                     |
| UI 组件库          | Element Plus 2.13                          |
| 状态管理           | Pinia 3.0                                  |
| 路由               | Vue Router 5                               |
| HTTP               | Axios 1.15                                 |
| 图表               | ECharts 6                                  |
| 视频播放           | ArtPlayer 5.4 + hls.js 1.6（HLS 切片播放） |
| Markdown 编辑器    | md-editor-v3 6.5                           |
| 代码高亮（学生端） | highlight.js 11                            |
| 样式               | Sass / sass-embedded                       |
| Node 要求          | ≥ 20.19 或 ≥ 22.12                         |

**前端工程**：

复制代码

```
smart-campus-front/
├── smart-campus-front-admin   管理后台前端
└── smart-campus-front-web     学生端前端
```

------

## 三、接口与鉴权规范

- **全局上下文**：`/api`，由 `server.servlet.context-path` 提供。
- **路径形式**：`/api/<模块名>/<动作>`，模块名小驼峰，路径**不重复**写 `admin` / `web`（由服务隔离）。
- **常用动作**：`loadDataList`、`add`、`update`、`delete`、`detail`、`getXxxOptions`。
- **管理端 Token**：Header `adminToken`，由管理端登录拦截器 + Redis 登录组件校验。
- **用户端 Token**：Header `studentToken`，由用户端登录拦截器校验。
- **权限控制**：管理端 Controller 强制权限注解，权限编码与菜单表一一对应。
- **统一响应**：`ResponseVO<T>` —— `status` / `code` / `info` / `data` 四字段结构。
- **分层规范**：`Controller → Biz → Service → Mapper → DB`，禁止越层。

------

## 四、核心业务域与数据模型

主要 PO（数据库实体）：

| 业务域   | 实体                                                         |
| -------- | ------------------------------------------------------------ |
| 基础数据 | DepartmentInfo（院系）、MajorInfo（专业）、ClassInfo（班级）、UserInfo（用户：教师/学生） |
| 课程教学 | CourseInfo、CourseChapter、CourseChapterLesson、CourseChapterLessonResource |
| 选课关系 | CourseClass（班级—课程关联）                                 |
| 题库试卷 | QuestionInfo、QuestionOption、PaperInfo、PaperQuestion       |
| 考试     | ExamInfo、ExamClass                                          |
| 作业评估 | CourseAssessmentSubmit、CourseAssessmentSubmitQuestion       |
| 学习记录 | CourseStudyProgress（课程级）、CourseStudyLessonProgress（含视频时间点）、CourseStudyLog |
| 资源     | ResourceInfo（视频/文档，分片上传 + Redis 队列异步处理）     |
| 学习计划 | StudyPlan、StudyPlanItem                                     |
| 个人     | CourseUserCollection（课程收藏）                             |
| 系统     | SystemMenu、SystemRoleMenu、SystemNotice、MessageInfo、MessageUser |

主键策略：业务实体多为 String UUID（课程/章节/课时/资源），基础数据多为自增整数。

------

## 五、功能模块

### 5.1 管理端（`smart-campus-front-admin` + `smart-campus-admin`）

#### 1）登录与权限

- 管理员登录、验证码、Token 注销
- 基于菜单的权限编码体系，按角色分配菜单权限
- `SystemPermissionManagement` 权限管理

#### 2）仪表盘（Dashboard）

- 用户/课程/资源/考试等关键指标统计
- 图表化展示运营数据

#### 3）基础数据管理（BasicData）

- 院系管理（DepartmentManagement）
- 专业管理（MajorManagement）
- 班级管理（ClassManagement）
- 学生管理（StudentManagement）
- 教师管理（TeacherManagement）
- 支持下拉级联（院系→专业→班级）、批量导入导出

#### 4）教学业务管理（Teaching）

- **课程管理**：课程归属一名教师，教师只能维护自己的课程
- **章节管理**：两级结构（章节 → 课时），课时下挂资源
- **题库管理**（ExerciseManagement）：单选/多选/判断/填空/简答
- **试卷管理**（PaperManagement）：题目组卷、客观/主观题混合
- **考试管理**（ExamManagement）：考试与班级关联，时间窗管理

#### 5）资源管理（Resource）

- 视频/文档上传（分片上传 + 断点续传）
- 上传任务走 Redis 队列异步处理（视频转 HLS 切片）
- 资源列表、详情、删除

#### 6）系统管理（System）

- 系统公告管理（SystemNoticeManagement）
- 权限菜单管理（SystemPermissionManagement）

### 5.2 学生端（`smart-campus-front-web` + `smart-campus-web`）

#### 1）登录与个人中心

- 学生登录（studentToken）
- 个人资料（ProfileCenter）

#### 2）首页（Home）

- 系统公告、推荐课程、学习入口

#### 3）我的课程（Course）

- **MyCourse**：通过班级关联的可学课程列表

- CourseStudy

  ：章节课时学习页

  - ArtPlayer + hls.js 视频播放
  - 服务端实时记录视频时间点 / 学习时长
  - 课时进度自动恢复（CourseStudyLessonProgress）
  - 文档资源在线预览

- **CourseHomework**：课程作业提交、查看评分

#### 4）考试中心（Exam）

- **ExamList**：可参加考试列表

- CourseExam

  ：在线答题

  - 客观题自动评分（QuestionInfo + QuestionOption）
  - 主观题人工评分
  - 倒计时、自动交卷

#### 5）学习计划（Plan）

- 自定义学习计划（StudyPlan + StudyPlanItem）
- 计划项打卡、进度跟踪

#### 6）学习分析（Analysis）

- 个人学习时长、课程进度、考试成绩可视化（ECharts）
- 学习行为画像、薄弱知识点提示

#### 7）消息中心（MessageCenter）

- 站内消息、系统通知（MessageInfo + MessageUser）

------

## 六、关键技术亮点

1. **服务级隔离**：管理端 / 用户端拆为两个 Spring Boot 服务，独立部署、独立端口、独立拦截器与登录态，互不干扰。
2. **统一响应规范**：全链路 `ResponseVO<T>`，前后端字段严格驼峰一致，禁止前端起别名。
3. **资源处理流水线**：分片上传 + Redis 队列异步转码（HLS 切片），前端 hls.js 流式播放。
4. **学习进度持久化**：CourseStudyProgress（课程级） + CourseStudyLessonProgress（含视频时间点），实现「断点续学」。
5. **AI 赋能**：Spring AI 接入，用于学习计划生成、学习分析建议。
6. **自研泛型 Mapper**：不使用 MyBatis Plus，单表 CRUD 走泛型基类，复杂查询走 XML。
7. **Redis 多场景复用**：登录态、上传 Session、学习进度、任务队列统一封装。
8. **时间字段强约束**：统一 `yyyy-MM-dd HH:mm:ss`（GMT+8），禁止返回时间戳。

------

## 七、数据库初始化

后端工程根目录提供版本化初始化脚本：

- `smart-campus.sql`：完整建表 + 业务初始数据
- `smart-campus_only_basedata.sql`：仅基础数据（院系/专业/班级/角色菜单）

------

## 八、启动方式

### 后端

bash复制代码

```
# 在项目根目录
cd smart-campus-java
mvn clean install -DskipTests

# 启动管理端
cd smart-campus-admin && mvn spring-boot:run

# 启动用户端
cd smart-campus-web   && mvn spring-boot:run
```

### 前端

bash复制代码

```
# 管理后台
cd smart-campus-front/smart-campus-front-admin
npm install
npm run dev

# 学生端
cd smart-campus-front/smart-campus-front-web
npm install
npm run dev
```

------

## 九、目录速查

复制代码

```
smart-campus/
├── smart-campus-java/               后端
│   ├── pom.xml
│   ├── smart-campus.sql             完整 DDL + 数据
│   ├── smart-campus_only_basedata.sql
│   ├── smart-campus-common/         公共模块（PO/DTO/VO/Service/Mapper/Redis）
│   ├── smart-campus-admin/          管理端（Controller/Biz/权限注解/Task）
│   └── smart-campus-web/            用户端（Controller/Biz/Task）
└── smart-campus-front/              前端
    ├── smart-campus-front-admin/    管理后台
    └── smart-campus-front-web/      学生端
```