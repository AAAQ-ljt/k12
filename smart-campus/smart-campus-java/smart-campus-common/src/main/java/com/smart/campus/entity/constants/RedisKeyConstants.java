package com.smart.campus.entity.constants;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    public static final String ROOT_PREFIX = "smart-campus";

    public static final String ADMIN_LOGIN_TOKEN_PREFIX = buildPrefix(ROOT_PREFIX, "admin", "login", "token");

    public static final String ADMIN_LOGIN_CAPTCHA_PREFIX = buildPrefix(ROOT_PREFIX, "admin", "login", "captcha");

    public static final String WEB_LOGIN_TOKEN_PREFIX = buildPrefix(ROOT_PREFIX, "web", "login", "token");

    public static final String WEB_LOGIN_CAPTCHA_PREFIX = buildPrefix(ROOT_PREFIX, "web", "login", "captcha");

    public static final String RESOURCE_UPLOAD_SESSION_PREFIX = buildPrefix(ROOT_PREFIX, "resource", "upload", "session");

    public static final String RESOURCE_TASK_QUEUE_KEY = buildPrefix(ROOT_PREFIX, "resource", "task", "queue");

    public static final String COURSE_STUDY_PROGRESS_PREFIX = buildPrefix(ROOT_PREFIX, "course", "study", "progress");

    public static final String COURSE_STUDY_LESSON_PROGRESS_PREFIX = buildPrefix(ROOT_PREFIX, "course", "study", "lesson", "progress");

    public static final String COURSE_STUDY_DIRTY_COURSE_SET_KEY = buildPrefix(ROOT_PREFIX, "course", "study", "dirty", "course");

    public static final String COURSE_STUDY_DIRTY_LESSON_SET_KEY = buildPrefix(ROOT_PREFIX, "course", "study", "dirty", "lesson");

    public static String buildKey(String prefix, String suffix) {
        return buildPrefix(prefix, suffix);
    }

    public static String buildPrefix(String... parts) {
        StringBuilder builder = new StringBuilder();
        if (parts == null) {
            return builder.toString();
        }
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(':');
            }
            builder.append(part.trim());
        }
        return builder.toString();
    }
}
