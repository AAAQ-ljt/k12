package com.easymall.constants;

public class Constants {

    //正则
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
    public static final String ZERO_STR = "0";
    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_30 = 30;

    public static final String PING = "ping";

    public static final String FILE_FOLDER_FILE = "file/";

    public static final String TOKEN_WEB = "token";


    /**
     * redis key 相关
     */

    /**
     * 过期时间 1分钟
     */
    public static final Long REDIS_KEY_EXPIRES_ONE_MIN = 60L;

    /**
     * 过期时间 1天
     */
    public static final Long REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;

    private static final String REDIS_KEY_PREFIX = "mall:";

    public static final String TOKEN_ADMIN = "adminToken";

    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREFIX + "token:admin:";

    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";

    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX + "token:web:";

    public static final String REDIS_KEY_TOKEN_USERID_WEB = REDIS_KEY_PREFIX + "token:web:userId:";

    public static final String REDIS_KEY_WS_USER_HEART_BEAT = REDIS_KEY_PREFIX + "ws:user:heartbeat";


    public static final String REDIS_KEY_CATEGORY_LIST = REDIS_KEY_PREFIX + "category:list:";

    public static final String CART_PAY_NAME = "购物车支付-%d件商品";

    //支付订单延时队列
    public static final String REDIS_KEY_ORDER_DELAY_QUEUE = REDIS_KEY_PREFIX + "order:delay:queue:";

    //自动发货队列
    public static final String REDIS_KEY_ORDER_DELAY_QUEUE_DELIVERY = REDIS_KEY_PREFIX + "order:delay:queue:delivery:";

    public static final String REDIS_KEY_ORDER_DELAY_QUEUE_CONFIRM = REDIS_KEY_PREFIX + "order:delay:queue:confirm:";

    public static final String REDIS_KEY_SETTING_LOGISTICS = REDIS_KEY_PREFIX + "setting:logistics:";

    public static final String REDIS_KEY_ORDER_LOGISTICS_QUEUE = REDIS_KEY_PREFIX + "order:logistics:queue:";

    //向量数据库队列
    public static final String REDIS_QUEUE_RAG_DATA = REDIS_KEY_PREFIX + "queue:rag:";

    public static final String REDIS_KEY_CANCEL_AGENT_MESSAGE = REDIS_KEY_PREFIX + "agent:message:";

    //提示词
    public static final String REDIS_KEY_PROMPT = REDIS_KEY_PREFIX + "prompt:";

    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail";

    //最近订单天数
    public static final Integer LATEST_ORDER_DAYS = 15;

}
