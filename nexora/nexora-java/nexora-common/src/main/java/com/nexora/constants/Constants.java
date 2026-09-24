package com.nexora.constants;

/**
 * 系统常量
 */
public class Constants {

    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
    public static final String ZERO_STR = "0";
    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_30 = 30;

    /**
     * WebSocket 心跳消息
     */
    public static final String PING = "ping";

    /**
     * WebSocket 心跳响应
     */
    public static final String PONG = "pong";

    /**
     * Redis key 前缀：用户心跳
     */
    public static final String REDIS_KEY_HEART_BEAT = "heart_beat:";

    /**
     * Redis key 前缀：用户 Token
     */
    public static final String REDIS_KEY_TOKEN = "token:";

    /**
     * Redis key 前缀：图形验证码
     */
    public static final String REDIS_KEY_CHECK_CODE = "check_code:";

    /**
     * Redis key 前缀：AI 消息取消标记
     */
    public static final String REDIS_KEY_AI_CANCEL = "ai_cancel:";

    /**
     * Redis key 前缀：提示词模板覆盖
     */
    public static final String REDIS_KEY_PROMPT_TEMPLATE = "prompt_template:";

    /**
     * Redis key 前缀：资源分片上传会话
     */
    public static final String REDIS_KEY_RESOURCE_UPLOAD_SESSION = "resource:upload:session:";

    /**
     * Redis key 前缀：资源分片上传已收分片集合
     */
    public static final String REDIS_KEY_RESOURCE_UPLOAD_SHARDS = "resource:upload:shards:";

    /**
     * Redis key 前缀：资源分片合并标记
     */
    public static final String REDIS_KEY_RESOURCE_UPLOAD_MERGED = "resource:upload:merged:";

    /**
     * Redis key：资源异步处理队列
     */
    public static final String REDIS_KEY_RESOURCE_UPLOAD_QUEUE = "resource:upload:queue";

    /**
     * Redis key：学生个人资源异步处理队列
     */
    public static final String REDIS_KEY_STUDENT_RESOURCE_UPLOAD_QUEUE = "student:resource:upload:queue";

    /**
     * Redis key：学生个人知识文档解析入库异步队列
     */
    public static final String REDIS_KEY_STUDENT_KNOWLEDGE_QUEUE = "student:knowledge:import:queue";

    /**
     * Redis key：学生个人知识页向量清理队列（无向量库的进程投递，由 web 端消费执行）
     */
    public static final String REDIS_KEY_STUDENT_KNOWLEDGE_CLEANUP_QUEUE = "student:knowledge:cleanup:queue";

    /**
     * Redis key：绘本生成异步任务队列与任务前缀（任务体 = 绘本任务 JSON）
     */
    public static final String REDIS_KEY_PICTURE_BOOK_TASK_QUEUE = "picturebook:task:queue";
    public static final String REDIS_KEY_PICTURE_BOOK_TASK_PREFIX = "picturebook:task:";

    /**
     * Redis key：绘本单页补画任务状态体与运行锁（resourceId:page 粒度，前端 2s 轮询）
     */
    public static final String REDIS_KEY_PICTURE_BOOK_PAGE_FIX_PREFIX = "picturebook:pagefix:";
    public static final String REDIS_KEY_PICTURE_BOOK_PAGE_FIX_RUNNING = "picturebook:pagefix:running:";

    /**
     * Redis key：课时测验 AI 出题异步任务前缀（任务体 = 测验任务 JSON）
     */
    public static final String REDIS_KEY_QUIZ_TASK_PREFIX = "quiztask:";

    /**
     * Redis key：动画讲解生成异步任务队列与任务前缀（任务体 = 动画任务 JSON）
     */
    public static final String REDIS_KEY_ANIMATION_TASK_QUEUE = "animation:task:queue";
    public static final String REDIS_KEY_ANIMATION_TASK_PREFIX = "animation:task:";

    /**
     * Redis key：学习路径节点快测异步出题任务队列/任务前缀/用户运行锁（任务体 = 节点快测任务 JSON）
     */
    public static final String REDIS_KEY_LEARNING_PATH_QUIZ_TASK_QUEUE = "learningpath:quiztask:queue";
    public static final String REDIS_KEY_LEARNING_PATH_QUIZ_TASK_PREFIX = "learningpath:quiztask:";
    public static final String REDIS_KEY_LEARNING_PATH_QUIZ_RUNNING_PREFIX = "learningpath:quiztask:running:";

    /**
     * Redis key：学习路径 AI 生成异步任务队列/任务前缀/用户运行锁（任务体 = 路线生成任务 JSON）
     */
    public static final String REDIS_KEY_LEARNING_PATH_GEN_TASK_QUEUE = "learningpath:gentask:queue";
    public static final String REDIS_KEY_LEARNING_PATH_GEN_TASK_PREFIX = "learningpath:gentask:";
    public static final String REDIS_KEY_LEARNING_PATH_GEN_RUNNING_PREFIX = "learningpath:gentask:running:";

    /**
     * Redis key：知识库解析入库异步队列
     */
    public static final String REDIS_KEY_KNOWLEDGE_IMPORT_QUEUE = "knowledge:import:queue";

    /**
     * Redis key：知识库解析入库任务队列/任务前缀/文档运行锁（任务体 = 解析入库任务 JSON，docId 粒度互斥）
     */
    public static final String REDIS_KEY_KNOWLEDGE_IMPORT_TASK_QUEUE = "knowledge:import:task:queue";
    public static final String REDIS_KEY_KNOWLEDGE_IMPORT_TASK_PREFIX = "knowledge:import:task:";
    public static final String REDIS_KEY_KNOWLEDGE_IMPORT_RUNNING_PREFIX = "knowledge:import:task:running:";

    /**
     * Redis key：AI 文档整理任务队列/任务前缀/资源运行锁（任务体 = AI 整理任务 JSON，resourceId 粒度互斥）
     */
    public static final String REDIS_KEY_AI_ORGANIZE_TASK_QUEUE = "knowledge:aiorganize:task:queue";
    public static final String REDIS_KEY_AI_ORGANIZE_TASK_PREFIX = "knowledge:aiorganize:task:";
    public static final String REDIS_KEY_AI_ORGANIZE_RUNNING_PREFIX = "knowledge:aiorganize:task:running:";

    /**
     * Redis key：模型测试文生图异步任务队列/任务前缀/运行锁（任务体 = 生图测试任务 JSON，全局单任务互斥）
     */
    public static final String REDIS_KEY_MODEL_IMAGE_TASK_QUEUE = "modeltest:imagetask:queue";
    public static final String REDIS_KEY_MODEL_IMAGE_TASK_PREFIX = "modeltest:imagetask:";
    public static final String REDIS_KEY_MODEL_IMAGE_RUNNING = "modeltest:imagetask:running";

    /**
     * 心跳超时时间（秒）
     */
    public static final int HEART_BEAT_TIMEOUT = 30;

    /**
     * 图形验证码有效期（秒）
     */
    public static final int CHECK_CODE_EXPIRE_SECONDS = 600;

    /**
     * 请求 attribute 中用户信息的 key
     */
    public static final String ATTR_USER_INFO = "userInfo";

    /**
     * 管理端 token 请求头名称
     */
    public static final String HEADER_ADMIN_TOKEN = "adminToken";

    /**
     * 学生端 token 请求头名称
     */
    public static final String HEADER_STUDENT_TOKEN = "studentToken";

    /**
     * 角色：管理员
     */
    public static final Integer ROLE_ADMIN = 0;

    /**
     * 角色：学生
     */
    public static final Integer ROLE_STUDENT = 1;

    /**
     * 状态：启用
     */
    public static final Integer STATUS_ENABLE = 1;

    /**
     * 状态：禁用
     */
    public static final Integer STATUS_DISABLE = 0;
}
